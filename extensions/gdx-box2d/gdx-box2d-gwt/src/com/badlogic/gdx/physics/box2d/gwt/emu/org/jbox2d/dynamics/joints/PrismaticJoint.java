/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.jbox2d.dynamics.joints;

import org.jbox2d.common.Mat22;
import org.jbox2d.common.Mat33;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.SolverData;
import org.jbox2d.pooling.IWorldPool;

//Linear constraint (point-to-line)
//d = p2 - p1 = x2 + r2 - x1 - r1
//C = dot(perp, d)
//Cdot = dot(d, cross(w1, perp)) + dot(perp, v2 + cross(w2, r2) - v1 - cross(w1, r1))
//   = -dot(perp, v1) - dot(cross(d + r1, perp), w1) + dot(perp, v2) + dot(cross(r2, perp), v2)
//J = [-perp, -cross(d + r1, perp), perp, cross(r2,perp)]
//
//Angular constraint
//C = a2 - a1 + a_initial
//Cdot = w2 - w1
//J = [0 0 -1 0 0 1]
//
//K = J * invM * JT
//
//J = [-a -s1 a s2]
//  [0  -1  0  1]
//a = perp
//s1 = cross(d + r1, a) = cross(p2 - x1, a)
//s2 = cross(r2, a) = cross(p2 - x2, a)


//Motor/Limit linear constraint
//C = dot(ax1, d)
//Cdot = = -dot(ax1, v1) - dot(cross(d + r1, ax1), w1) + dot(ax1, v2) + dot(cross(r2, ax1), v2)
//J = [-ax1 -cross(d+r1,ax1) ax1 cross(r2,ax1)]

//Block Solver
//We develop a block solver that includes the joint limit. This makes the limit stiff (inelastic) even
//when the mass has poor distribution (leading to large torques about the joint anchor points).
//
//The Jacobian has 3 rows:
//J = [-uT -s1 uT s2] // linear
//  [0   -1   0  1] // angular
//  [-vT -a1 vT a2] // limit
//
//u = perp
//v = axis
//s1 = cross(d + r1, u), s2 = cross(r2, u)
//a1 = cross(d + r1, v), a2 = cross(r2, v)

//M * (v2 - v1) = JT * df
//J * v2 = bias
//
//v2 = v1 + invM * JT * df
//J * (v1 + invM * JT * df) = bias
//K * df = bias - J * v1 = -Cdot
//K = J * invM * JT
//Cdot = J * v1 - bias
//
//Now solve for f2.
//df = f2 - f1
//K * (f2 - f1) = -Cdot
//f2 = invK * (-Cdot) + f1
//
//Clamp accumulated limit impulse.
//lower: f2(3) = max(f2(3), 0)
//upper: f2(3) = min(f2(3), 0)
//
//Solve for correct f2(1:2)
//K(1:2, 1:2) * f2(1:2) = -Cdot(1:2) - K(1:2,3) * f2(3) + K(1:2,1:3) * f1
//                    = -Cdot(1:2) - K(1:2,3) * f2(3) + K(1:2,1:2) * f1(1:2) + K(1:2,3) * f1(3)
//K(1:2, 1:2) * f2(1:2) = -Cdot(1:2) - K(1:2,3) * (f2(3) - f1(3)) + K(1:2,1:2) * f1(1:2)
//f2(1:2) = invK(1:2,1:2) * (-Cdot(1:2) - K(1:2,3) * (f2(3) - f1(3))) + f1(1:2)
//
//Now compute impulse to be applied:
//df = f2 - f1

/**
 * A prismatic joint. This joint provides one degree of freedom: translation along an axis fixed in
 * bodyA. Relative rotation is prevented. You can use a joint limit to restrict the range of motion
 * and a joint motor to drive the motion or to model joint friction.
 * 
 * @author Daniel
 */
public class PrismaticJoint extends Joint {

  // Solver shared
  protected final Vec2 m_localAnchorA;
  protected final Vec2 m_localAnchorB;
  protected final Vec2 m_localXAxisA;
  protected final Vec2 m_localYAxisA;
  protected float m_referenceAngle;
  private final Vec3 m_impulse;
  private float m_motorImpulse;
  private float m_lowerTranslation;
  private float m_upperTranslation;
  private float m_maxMotorForce;
  private float m_motorSpeed;
  private boolean m_enableLimit;
  private boolean m_enableMotor;
  private LimitState m_limitState;

  // Solver temp
  private int m_indexA;
  private int m_indexB;
  private final Vec2 m_localCenterA = new Vec2();
  private final Vec2 m_localCenterB = new Vec2();
  private float m_invMassA;
  private float m_invMassB;
  private float m_invIA;
  private float m_invIB;
  private final Vec2 m_axis, m_perp;
  private float m_s1, m_s2;
  private float m_a1, m_a2;
  private final Mat33 m_K;
  private float m_motorMass; // effective mass for motor/limit translational constraint.

  protected PrismaticJoint(IWorldPool argWorld, PrismaticJointDef def) {
    super(argWorld, def);
    m_localAnchorA = new Vec2(def.localAnchorA);
    m_localAnchorB = new Vec2(def.localAnchorB);
    m_localXAxisA = new Vec2(def.localAxisA);
    m_localXAxisA.normalize();
    m_localYAxisA = new Vec2();
    Vec2.crossToOutUnsafe(1f, m_localXAxisA, m_localYAxisA);
    m_referenceAngle = def.referenceAngle;

    m_impulse = new Vec3();
    m_motorMass = 0.0f;
    m_motorImpulse = 0.0f;

    m_lowerTranslation = def.lowerTranslation;
    m_upperTranslation = def.upperTranslation;
    m_maxMotorForce = def.maxMotorForce;
    m_motorSpeed = def.motorSpeed;
    m_enableLimit = def.enableLimit;
    m_enableMotor = def.enableMotor;
    m_limitState = LimitState.INACTIVE;

    m_K = new Mat33();
    m_axis = new Vec2();
    m_perp = new Vec2();
  }

  public Vec2 getLocalAnchorA() {
    return m_localAnchorA;
  }

  public Vec2 getLocalAnchorB() {
    return m_localAnchorB;
  }

  @Override
  public void getAnchorA(Vec2 argOut) {
    m_bodyA.getWorldPointToOut(m_localAnchorA, argOut);
  }

  @Override
  public void getAnchorB(Vec2 argOut) {
    m_bodyB.getWorldPointToOut(m_localAnchorB, argOut);
  }

  @Override
  public void getReactionForce(float inv_dt, Vec2 argOut) {
    Vec2 temp = pool.popVec2();
    temp.set(m_axis).mulLocal(m_motorImpulse + m_impulse.z);
    argOut.set(m_perp).mulLocal(m_impulse.x).addLocal(temp).mulLocal(inv_dt);
    pool.pushVec2(1);
  }

  @Override
  public float getReactionTorque(float inv_dt) {
    return inv_dt * m_impulse.y;
  }

  /**
   * Get the current joint translation, usually in meters.
   */
  public float getJointSpeed() {
    Body bA = m_bodyA;
    Body bB = m_bodyB;

    Vec2 temp = pool.popVec2();
    Vec2 rA = pool.popVec2();
    Vec2 rB = pool.popVec2();
    Vec2 p1 = pool.popVec2();
    Vec2 p2 = pool.popVec2();
    Vec2 d = pool.popVec2();
    Vec2 axis = pool.popVec2();
    Vec2 temp2 = pool.popVec2();
    Vec2 temp3 = pool.popVec2();

    temp.set(m_localAnchorA).subLocal(bA.m_sweep.localCenter);
    Rot.mulToOutUnsafe(bA.m_xf.q, temp, rA);

    temp.set(m_localAnchorB).subLocal(bB.m_sweep.localCenter);
    Rot.mulToOutUnsafe(bB.m_xf.q, temp, rB);

    p1.set(bA.m_sweep.c).addLocal(rA);
    p2.set(bB.m_sweep.c).addLocal(rB);

    d.set(p2).subLocal(p1);
    Rot.mulToOutUnsafe(bA.m_xf.q, m_localXAxisA, axis);

    Vec2 vA = bA.m_linearVelocity;
    Vec2 vB = bB.m_linearVelocity;
    float wA = bA.m_angularVelocity;
    float wB = bB.m_angularVelocity;


    Vec2.crossToOutUnsafe(wA, axis, temp);
    Vec2.crossToOutUnsafe(wB, rB, temp2);
    Vec2.crossToOutUnsafe(wA, rA, temp3);

    temp2.addLocal(vB).subLocal(vA).subLocal(temp3);
    float speed = Vec2.dot(d, temp) + Vec2.dot(axis, temp2);

    pool.pushVec2(9);

    return speed;
  }

  public float getJointTranslation() {
    Vec2 pA = pool.popVec2(), pB = pool.popVec2(), axis = pool.popVec2();
    m_bodyA.getWorldPointToOut(m_localAnchorA, pA);
    m_bodyB.getWorldPointToOut(m_localAnchorB, pB);
    m_bodyA.getWorldVectorToOutUnsafe(m_localXAxisA, axis);
    pB.subLocal(pA);
    float translation = Vec2.dot(pB, axis);
    pool.pushVec2(3);
    return translation;
  }

  /**
   * Is the joint limit enabled?
   * 
   * @return
   */
  public boolean isLimitEnabled() {
    return m_enableLimit;
  }

  /**
   * Enable/disable the joint limit.
   * 
   * @param flag
   */
  public void enableLimit(boolean flag) {
    if (flag != m_enableLimit) {
      m_bodyA.setAwake(true);
      m_bodyB.setAwake(true);
      m_enableLimit = flag;
      m_impulse.z = 0.0f;
    }
  }

  /**
   * Get the lower joint limit, usually in meters.
   * 
   * @return
   */
  public float getLowerLimit() {
    return m_lowerTranslation;
  }

  /**
   * Get the upper joint limit, usually in meters.
   * 
   * @return
   */
  public float getUpperLimit() {
    return m_upperTranslation;
  }

  /**
   * Set the joint limits, usually in meters.
   * 
   * @param lower
   * @param upper
   */
  public void setLimits(float lower, float upper) {
    assert (lower <= upper);
    if (lower != m_lowerTranslation || upper != m_upperTranslation) {
      m_bodyA.setAwake(true);
      m_bodyB.setAwake(true);
      m_lowerTranslation = lower;
      m_upperTranslation = upper;
      m_impulse.z = 0.0f;
    }
  }

  /**
   * Is the joint motor enabled?
   * 
   * @return
   */
  public boolean isMotorEnabled() {
    return m_enableMotor;
  }

  /**
   * Enable/disable the joint motor.
   * 
   * @param flag
   */
  public void enableMotor(boolean flag) {
    m_bodyA.setAwake(true);
    m_bodyB.setAwake(true);
    m_enableMotor = flag;
  }

  /**
   * Set the motor speed, usually in meters per second.
   * 
   * @param speed
   */
  public void setMotorSpeed(float speed) {
    m_bodyA.setAwake(true);
    m_bodyB.setAwake(true);
    m_motorSpeed = speed;
  }

  /**
   * Get the motor speed, usually in meters per second.
   * 
   * @return
   */
  public float getMotorSpeed() {
    return m_motorSpeed;
  }

  /**
   * Set the maximum motor force, usually in N.
   * 
   * @param force
   */
  public void setMaxMotorForce(float force) {
    m_bodyA.setAwake(true);
    m_bodyB.setAwake(true);
    m_maxMotorForce = force;
  }

  /**
   * Get the current motor force, usually in N.
   * 
   * @param inv_dt
   * @return
   */
  public float getMotorForce(float inv_dt) {
    return m_motorImpulse * inv_dt;
  }

  public float getMaxMotorForce() {
    return m_maxMotorForce;
  }

  public float getReferenceAngle() {
    return m_referenceAngle;
  }

  public Vec2 getLocalAxisA() {
    return m_localXAxisA;
  }

  @Override
  public void initVelocityConstraints(final SolverData data) {
    m_indexA = m_bodyA.m_islandIndex;
    m_indexB = m_bodyB.m_islandIndex;
    m_localCenterA.set(m_bodyA.m_sweep.localCenter);
    m_localCenterB.set(m_bodyB.m_sweep.localCenter);
    m_invMassA = m_bodyA.m_invMass;
    m_invMassB = m_bodyB.m_invMass;
    m_invIA = m_bodyA.m_invI;
    m_invIB = m_bodyB.m_invI;

    Vec2 cA = data.positions[m_indexA].c;
    float aA = data.positions[m_indexA].a;
    Vec2 vA = data.velocities[m_indexA].v;
    float wA = data.velocities[m_indexA].w;

    Vec2 cB = data.positions[m_indexB].c;
    float aB = data.positions[m_indexB].a;
    Vec2 vB = data.velocities[m_indexB].v;
    float wB = data.velocities[m_indexB].w;

    final Rot qA = pool.popRot();
    final Rot qB = pool.popRot();
    final Vec2 d = pool.popVec2();
    final Vec2 temp = pool.popVec2();
    final Vec2 rA = pool.popVec2();
    final Vec2 rB = pool.popVec2();

    qA.set(aA);
    qB.set(aB);

    // Compute the effective masses.
    Rot.mulToOutUnsafe(qA, d.set(m_localAnchorA).subLocal(m_localCenterA), rA);
    Rot.mulToOutUnsafe(qB, d.set(m_localAnchorB).subLocal(m_localCenterB), rB);
    d.set(cB).subLocal(cA).addLocal(rB).subLocal(rA);

    float mA = m_invMassA, mB = m_invMassB;
    float iA = m_invIA, iB = m_invIB;

    // Compute motor Jacobian and effective mass.
    {
      Rot.mulToOutUnsafe(qA, m_localXAxisA, m_axis);
      temp.set(d).addLocal(rA);
      m_a1 = Vec2.cross(temp, m_axis);
      m_a2 = Vec2.cross(rB, m_axis);

      m_motorMass = mA + mB + iA * m_a1 * m_a1 + iB * m_a2 * m_a2;
      if (m_motorMass > 0.0f) {
        m_motorMass = 1.0f / m_motorMass;
      }
    }

    // Prismatic constraint.
    {
      Rot.mulToOutUnsafe(qA, m_localYAxisA, m_perp);

      temp.set(d).addLocal(rA);
      m_s1 = Vec2.cross(temp, m_perp);
      m_s2 = Vec2.cross(rB, m_perp);

      float k11 = mA + mB + iA * m_s1 * m_s1 + iB * m_s2 * m_s2;
      float k12 = iA * m_s1 + iB * m_s2;
      float k13 = iA * m_s1 * m_a1 + iB * m_s2 * m_a2;
      float k22 = iA + iB;
      if (k22 == 0.0f) {
        // For bodies with fixed rotation.
        k22 = 1.0f;
      }
      float k23 = iA * m_a1 + iB * m_a2;
      float k33 = mA + mB + iA * m_a1 * m_a1 + iB * m_a2 * m_a2;

      m_K.ex.set(k11, k12, k13);
      m_K.ey.set(k12, k22, k23);
      m_K.ez.set(k13, k23, k33);
    }

    // Compute motor and limit terms.
    if (m_enableLimit) {

      float jointTranslation = Vec2.dot(m_axis, d);
      if (MathUtils.abs(m_upperTranslation - m_lowerTranslation) < 2.0f * Settings.linearSlop) {
        m_limitState = LimitState.EQUAL;
      } else if (jointTranslation <= m_lowerTranslation) {
        if (m_limitState != LimitState.AT_LOWER) {
          m_limitState = LimitState.AT_LOWER;
          m_impulse.z = 0.0f;
        }
      } else if (jointTranslation >= m_upperTranslation) {
        if (m_limitState != LimitState.AT_UPPER) {
          m_limitState = LimitState.AT_UPPER;
          m_impulse.z = 0.0f;
        }
      } else {
        m_limitState = LimitState.INACTIVE;
        m_impulse.z = 0.0f;
      }
    } else {
      m_limitState = LimitState.INACTIVE;
      m_impulse.z = 0.0f;
    }

    if (m_enableMotor == false) {
      m_motorImpulse = 0.0f;
    }

    if (data.step.warmStarting) {
      // Account for variable time step.
      m_impulse.mulLocal(data.step.dtRatio);
      m_motorImpulse *= data.step.dtRatio;

      final Vec2 P = pool.popVec2();
      temp.set(m_axis).mulLocal(m_motorImpulse + m_impulse.z);
      P.set(m_perp).mulLocal(m_impulse.x).addLocal(temp);

      float LA = m_impulse.x * m_s1 + m_impulse.y + (m_motorImpulse + m_impulse.z) * m_a1;
      float LB = m_impulse.x * m_s2 + m_impulse.y + (m_motorImpulse + m_impulse.z) * m_a2;

      vA.x -= mA * P.x;
      vA.y -= mA * P.y;
      wA -= iA * LA;

      vB.x += mB * P.x;
      vB.y += mB * P.y;
      wB += iB * LB;

      pool.pushVec2(1);
    } else {
      m_impulse.setZero();
      m_motorImpulse = 0.0f;
    }

    // data.velocities[m_indexA].v.set(vA);
    data.velocities[m_indexA].w = wA;
    // data.velocities[m_indexB].v.set(vB);
    data.velocities[m_indexB].w = wB;

    pool.pushRot(2);
    pool.pushVec2(4);
  }

  @Override
  public void solveVelocityConstraints(final SolverData data) {
    Vec2 vA = data.velocities[m_indexA].v;
    float wA = data.velocities[m_indexA].w;
    Vec2 vB = data.velocities[m_indexB].v;
    float wB = data.velocities[m_indexB].w;

    float mA = m_invMassA, mB = m_invMassB;
    float iA = m_invIA, iB = m_invIB;

    final Vec2 temp = pool.popVec2();

    // Solve linear motor constraint.
    if (m_enableMotor && m_limitState != LimitState.EQUAL) {
      temp.set(vB).subLocal(vA);
      float Cdot = Vec2.dot(m_axis, temp) + m_a2 * wB - m_a1 * wA;
      float impulse = m_motorMass * (m_motorSpeed - Cdot);
      float oldImpulse = m_motorImpulse;
      float maxImpulse = data.step.dt * m_maxMotorForce;
      m_motorImpulse = MathUtils.clamp(m_motorImpulse + impulse, -maxImpulse, maxImpulse);
      impulse = m_motorImpulse - oldImpulse;

      final Vec2 P = pool.popVec2();
      P.set(m_axis).mulLocal(impulse);
      float LA = impulse * m_a1;
      float LB = impulse * m_a2;

      vA.x -= mA * P.x;
      vA.y -= mA * P.y;
      wA -= iA * LA;

      vB.x += mB * P.x;
      vB.y += mB * P.y;
      wB += iB * LB;

      pool.pushVec2(1);
    }

    final Vec2 Cdot1 = pool.popVec2();
    temp.set(vB).subLocal(vA);
    Cdot1.x = Vec2.dot(m_perp, temp) + m_s2 * wB - m_s1 * wA;
    Cdot1.y = wB - wA;
    // System.out.println(Cdot1);

    if (m_enableLimit && m_limitState != LimitState.INACTIVE) {
      // Solve prismatic and limit constraint in block form.
      float Cdot2;
      temp.set(vB).subLocal(vA);
      Cdot2 = Vec2.dot(m_axis, temp) + m_a2 * wB - m_a1 * wA;

      final Vec3 Cdot = pool.popVec3();
      Cdot.set(Cdot1.x, Cdot1.y, Cdot2);

      final Vec3 f1 = pool.popVec3();
      final Vec3 df = pool.popVec3();

      f1.set(m_impulse);
      m_K.solve33ToOut(Cdot.negateLocal(), df);
      // Cdot.negateLocal(); not used anymore
      m_impulse.addLocal(df);

      if (m_limitState == LimitState.AT_LOWER) {
        m_impulse.z = MathUtils.max(m_impulse.z, 0.0f);
      } else if (m_limitState == LimitState.AT_UPPER) {
        m_impulse.z = MathUtils.min(m_impulse.z, 0.0f);
      }

      // f2(1:2) = invK(1:2,1:2) * (-Cdot(1:2) - K(1:2,3) * (f2(3) - f1(3))) +
      // f1(1:2)
      final Vec2 b = pool.popVec2();
      final Vec2 f2r = pool.popVec2();

      temp.set(m_K.ez.x, m_K.ez.y).mulLocal(m_impulse.z - f1.z);
      b.set(Cdot1).negateLocal().subLocal(temp);

      m_K.solve22ToOut(b, f2r);
      f2r.addLocal(f1.x, f1.y);
      m_impulse.x = f2r.x;
      m_impulse.y = f2r.y;

      df.set(m_impulse).subLocal(f1);

      final Vec2 P = pool.popVec2();
      temp.set(m_axis).mulLocal(df.z);
      P.set(m_perp).mulLocal(df.x).addLocal(temp);

      float LA = df.x * m_s1 + df.y + df.z * m_a1;
      float LB = df.x * m_s2 + df.y + df.z * m_a2;

      vA.x -= mA * P.x;
      vA.y -= mA * P.y;
      wA -= iA * LA;

      vB.x += mB * P.x;
      vB.y += mB * P.y;
      wB += iB * LB;

      pool.pushVec2(3);
      pool.pushVec3(3);
    } else {
      // Limit is inactive, just solve the prismatic constraint in block form.
      final Vec2 df = pool.popVec2();
      m_K.solve22ToOut(Cdot1.negateLocal(), df);
      Cdot1.negateLocal();

      m_impulse.x += df.x;
      m_impulse.y += df.y;

      final Vec2 P = pool.popVec2();
      P.set(m_perp).mulLocal(df.x);
      float LA = df.x * m_s1 + df.y;
      float LB = df.x * m_s2 + df.y;

      vA.x -= mA * P.x;
      vA.y -= mA * P.y;
      wA -= iA * LA;

      vB.x += mB * P.x;
      vB.y += mB * P.y;
      wB += iB * LB;

      pool.pushVec2(2);
    }

    // data.velocities[m_indexA].v.set(vA);
    data.velocities[m_indexA].w = wA;
    // data.velocities[m_indexB].v.set(vB);
    data.velocities[m_indexB].w = wB;

    pool.pushVec2(2);
  }


  @Override
  public boolean solvePositionConstraints(final SolverData data) {

    final Rot qA = pool.popRot();
    final Rot qB = pool.popRot();
    final Vec2 rA = pool.popVec2();
    final Vec2 rB = pool.popVec2();
    final Vec2 d = pool.popVec2();
    final Vec2 axis = pool.popVec2();
    final Vec2 perp = pool.popVec2();
    final Vec2 temp = pool.popVec2();
    final Vec2 C1 = pool.popVec2();

    final Vec3 impulse = pool.popVec3();

    Vec2 cA = data.positions[m_indexA].c;
    float aA = data.positions[m_indexA].a;
    Vec2 cB = data.positions[m_indexB].c;
    float aB = data.positions[m_indexB].a;

    qA.set(aA);
    qB.set(aB);

    float mA = m_invMassA, mB = m_invMassB;
    float iA = m_invIA, iB = m_invIB;

    // Compute fresh Jacobians
    Rot.mulToOutUnsafe(qA, temp.set(m_localAnchorA).subLocal(m_localCenterA), rA);
    Rot.mulToOutUnsafe(qB, temp.set(m_localAnchorB).subLocal(m_localCenterB), rB);
    d.set(cB).addLocal(rB).subLocal(cA).subLocal(rA);

    Rot.mulToOutUnsafe(qA, m_localXAxisA, axis);
    float a1 = Vec2.cross(temp.set(d).addLocal(rA), axis);
    float a2 = Vec2.cross(rB, axis);
    Rot.mulToOutUnsafe(qA, m_localYAxisA, perp);

    float s1 = Vec2.cross(temp.set(d).addLocal(rA), perp);
    float s2 = Vec2.cross(rB, perp);

    C1.x = Vec2.dot(perp, d);
    C1.y = aB - aA - m_referenceAngle;

    float linearError = MathUtils.abs(C1.x);
    float angularError = MathUtils.abs(C1.y);

    boolean active = false;
    float C2 = 0.0f;
    if (m_enableLimit) {
      float translation = Vec2.dot(axis, d);
      if (MathUtils.abs(m_upperTranslation - m_lowerTranslation) < 2.0f * Settings.linearSlop) {
        // Prevent large angular corrections
        C2 =
            MathUtils.clamp(translation, -Settings.maxLinearCorrection,
                Settings.maxLinearCorrection);
        linearError = MathUtils.max(linearError, MathUtils.abs(translation));
        active = true;
      } else if (translation <= m_lowerTranslation) {
        // Prevent large linear corrections and allow some slop.
        C2 =
            MathUtils.clamp(translation - m_lowerTranslation + Settings.linearSlop,
                -Settings.maxLinearCorrection, 0.0f);
        linearError = MathUtils.max(linearError, m_lowerTranslation - translation);
        active = true;
      } else if (translation >= m_upperTranslation) {
        // Prevent large linear corrections and allow some slop.
        C2 =
            MathUtils.clamp(translation - m_upperTranslation - Settings.linearSlop, 0.0f,
                Settings.maxLinearCorrection);
        linearError = MathUtils.max(linearError, translation - m_upperTranslation);
        active = true;
      }
    }

    if (active) {
      float k11 = mA + mB + iA * s1 * s1 + iB * s2 * s2;
      float k12 = iA * s1 + iB * s2;
      float k13 = iA * s1 * a1 + iB * s2 * a2;
      float k22 = iA + iB;
      if (k22 == 0.0f) {
        // For fixed rotation
        k22 = 1.0f;
      }
      float k23 = iA * a1 + iB * a2;
      float k33 = mA + mB + iA * a1 * a1 + iB * a2 * a2;

      final Mat33 K = pool.popMat33();
      K.ex.set(k11, k12, k13);
      K.ey.set(k12, k22, k23);
      K.ez.set(k13, k23, k33);

      final Vec3 C = pool.popVec3();
      C.x = C1.x;
      C.y = C1.y;
      C.z = C2;

      K.solve33ToOut(C.negateLocal(), impulse);
      pool.pushVec3(1);
      pool.pushMat33(1);
    } else {
      float k11 = mA + mB + iA * s1 * s1 + iB * s2 * s2;
      float k12 = iA * s1 + iB * s2;
      float k22 = iA + iB;
      if (k22 == 0.0f) {
        k22 = 1.0f;
      }

      final Mat22 K = pool.popMat22();
      K.ex.set(k11, k12);
      K.ey.set(k12, k22);

      // temp is impulse1
      K.solveToOut(C1.negateLocal(), temp);
      C1.negateLocal();

      impulse.x = temp.x;
      impulse.y = temp.y;
      impulse.z = 0.0f;

      pool.pushMat22(1);
    }

    float Px = impulse.x * perp.x + impulse.z * axis.x;
    float Py = impulse.x * perp.y + impulse.z * axis.y;
    float LA = impulse.x * s1 + impulse.y + impulse.z * a1;
    float LB = impulse.x * s2 + impulse.y + impulse.z * a2;

    cA.x -= mA * Px;
    cA.y -= mA * Py;
    aA -= iA * LA;
    cB.x += mB * Px;
    cB.y += mB * Py;
    aB += iB * LB;

    // data.positions[m_indexA].c.set(cA);
    data.positions[m_indexA].a = aA;
    // data.positions[m_indexB].c.set(cB);
    data.positions[m_indexB].a = aB;

    pool.pushVec2(7);
    pool.pushVec3(1);
    pool.pushRot(2);

    return linearError <= Settings.linearSlop && angularError <= Settings.angularSlop;
  }
}
