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
/**
 * Created at 7:27:32 AM Jan 20, 2011
 */
package org.jbox2d.dynamics.joints;

import org.jbox2d.common.Mat22;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.SolverData;
import org.jbox2d.pooling.IWorldPool;

/**
 * @author Daniel Murphy
 */
public class FrictionJoint extends Joint {

  private final Vec2 m_localAnchorA;
  private final Vec2 m_localAnchorB;

  // Solver shared
  private final Vec2 m_linearImpulse;
  private float m_angularImpulse;
  private float m_maxForce;
  private float m_maxTorque;

  // Solver temp
  private int m_indexA;
  private int m_indexB;
  private final Vec2 m_rA = new Vec2();
  private final Vec2 m_rB = new Vec2();
  private final Vec2 m_localCenterA = new Vec2();
  private final Vec2 m_localCenterB = new Vec2();
  private float m_invMassA;
  private float m_invMassB;
  private float m_invIA;
  private float m_invIB;
  private final Mat22 m_linearMass = new Mat22();
  private float m_angularMass;

  protected FrictionJoint(IWorldPool argWorldPool, FrictionJointDef def) {
    super(argWorldPool, def);
    m_localAnchorA = new Vec2(def.localAnchorA);
    m_localAnchorB = new Vec2(def.localAnchorB);

    m_linearImpulse = new Vec2();
    m_angularImpulse = 0.0f;

    m_maxForce = def.maxForce;
    m_maxTorque = def.maxTorque;
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
    argOut.set(m_linearImpulse).mulLocal(inv_dt);
  }

  @Override
  public float getReactionTorque(float inv_dt) {
    return inv_dt * m_angularImpulse;
  }

  public void setMaxForce(float force) {
    assert (force >= 0.0f);
    m_maxForce = force;
  }

  public float getMaxForce() {
    return m_maxForce;
  }

  public void setMaxTorque(float torque) {
    assert (torque >= 0.0f);
    m_maxTorque = torque;
  }

  public float getMaxTorque() {
    return m_maxTorque;
  }

  /**
   * @see org.jbox2d.dynamics.joints.Joint#initVelocityConstraints(org.jbox2d.dynamics.TimeStep)
   */
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

    float aA = data.positions[m_indexA].a;
    Vec2 vA = data.velocities[m_indexA].v;
    float wA = data.velocities[m_indexA].w;

    float aB = data.positions[m_indexB].a;
    Vec2 vB = data.velocities[m_indexB].v;
    float wB = data.velocities[m_indexB].w;


    final Vec2 temp = pool.popVec2();
    final Rot qA = pool.popRot();
    final Rot qB = pool.popRot();

    qA.set(aA);
    qB.set(aB);

    // Compute the effective mass matrix.
    Rot.mulToOutUnsafe(qA, temp.set(m_localAnchorA).subLocal(m_localCenterA), m_rA);
    Rot.mulToOutUnsafe(qB, temp.set(m_localAnchorB).subLocal(m_localCenterB), m_rB);

    // J = [-I -r1_skew I r2_skew]
    // [ 0 -1 0 1]
    // r_skew = [-ry; rx]

    // Matlab
    // K = [ mA+r1y^2*iA+mB+r2y^2*iB, -r1y*iA*r1x-r2y*iB*r2x, -r1y*iA-r2y*iB]
    // [ -r1y*iA*r1x-r2y*iB*r2x, mA+r1x^2*iA+mB+r2x^2*iB, r1x*iA+r2x*iB]
    // [ -r1y*iA-r2y*iB, r1x*iA+r2x*iB, iA+iB]

    float mA = m_invMassA, mB = m_invMassB;
    float iA = m_invIA, iB = m_invIB;

    final Mat22 K = pool.popMat22();
    K.ex.x = mA + mB + iA * m_rA.y * m_rA.y + iB * m_rB.y * m_rB.y;
    K.ex.y = -iA * m_rA.x * m_rA.y - iB * m_rB.x * m_rB.y;
    K.ey.x = K.ex.y;
    K.ey.y = mA + mB + iA * m_rA.x * m_rA.x + iB * m_rB.x * m_rB.x;

    K.invertToOut(m_linearMass);

    m_angularMass = iA + iB;
    if (m_angularMass > 0.0f) {
      m_angularMass = 1.0f / m_angularMass;
    }

    if (data.step.warmStarting) {
      // Scale impulses to support a variable time step.
      m_linearImpulse.mulLocal(data.step.dtRatio);
      m_angularImpulse *= data.step.dtRatio;

      final Vec2 P = pool.popVec2();
      P.set(m_linearImpulse);

      temp.set(P).mulLocal(mA);
      vA.subLocal(temp);
      wA -= iA * (Vec2.cross(m_rA, P) + m_angularImpulse);

      temp.set(P).mulLocal(mB);
      vB.addLocal(temp);
      wB += iB * (Vec2.cross(m_rB, P) + m_angularImpulse);

      pool.pushVec2(1);
    } else {
      m_linearImpulse.setZero();
      m_angularImpulse = 0.0f;
    }
//    data.velocities[m_indexA].v.set(vA);
    if( data.velocities[m_indexA].w != wA) {
      assert(data.velocities[m_indexA].w != wA);
    }
    data.velocities[m_indexA].w = wA;
//    data.velocities[m_indexB].v.set(vB);
    data.velocities[m_indexB].w = wB;

    pool.pushRot(2);
    pool.pushVec2(1);
    pool.pushMat22(1);
  }

  @Override
  public void solveVelocityConstraints(final SolverData data) {
    Vec2 vA = data.velocities[m_indexA].v;
    float wA = data.velocities[m_indexA].w;
    Vec2 vB = data.velocities[m_indexB].v;
    float wB = data.velocities[m_indexB].w;

    float mA = m_invMassA, mB = m_invMassB;
    float iA = m_invIA, iB = m_invIB;

    float h = data.step.dt;

    // Solve angular friction
    {
      float Cdot = wB - wA;
      float impulse = -m_angularMass * Cdot;

      float oldImpulse = m_angularImpulse;
      float maxImpulse = h * m_maxTorque;
      m_angularImpulse = MathUtils.clamp(m_angularImpulse + impulse, -maxImpulse, maxImpulse);
      impulse = m_angularImpulse - oldImpulse;

      wA -= iA * impulse;
      wB += iB * impulse;
    }

    // Solve linear friction
    {
      final Vec2 Cdot = pool.popVec2();
      final Vec2 temp = pool.popVec2();

      Vec2.crossToOutUnsafe(wA, m_rA, temp);
      Vec2.crossToOutUnsafe(wB, m_rB, Cdot);
      Cdot.addLocal(vB).subLocal(vA).subLocal(temp);

      final Vec2 impulse = pool.popVec2();
      Mat22.mulToOutUnsafe(m_linearMass, Cdot, impulse);
      impulse.negateLocal();


      final Vec2 oldImpulse = pool.popVec2();
      oldImpulse.set(m_linearImpulse);
      m_linearImpulse.addLocal(impulse);

      float maxImpulse = h * m_maxForce;

      if (m_linearImpulse.lengthSquared() > maxImpulse * maxImpulse) {
        m_linearImpulse.normalize();
        m_linearImpulse.mulLocal(maxImpulse);
      }

      impulse.set(m_linearImpulse).subLocal(oldImpulse);

      temp.set(impulse).mulLocal(mA);
      vA.subLocal(temp);
      wA -= iA * Vec2.cross(m_rA, impulse);

      temp.set(impulse).mulLocal(mB);
      vB.addLocal(temp);
      wB += iB * Vec2.cross(m_rB, impulse);
      
    }

//    data.velocities[m_indexA].v.set(vA);
    if( data.velocities[m_indexA].w != wA) {
      assert(data.velocities[m_indexA].w != wA);
    }
    data.velocities[m_indexA].w = wA;
   
//    data.velocities[m_indexB].v.set(vB);
    data.velocities[m_indexB].w = wB;

    pool.pushVec2(4);
  }

  @Override
  public boolean solvePositionConstraints(final SolverData data) {
    return true;
  }
}
