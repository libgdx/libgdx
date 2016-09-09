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
/*
 * JBox2D - A Java Port of Erin Catto's Box2D
 * 
 * JBox2D homepage: http://jbox2d.sourceforge.net/
 * Box2D homepage: http://www.box2d.org
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software. If you use this software
 * in a product, an acknowledgment in the product documentation would be
 * appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package org.jbox2d.dynamics.joints;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.SolverData;
import org.jbox2d.pooling.IWorldPool;

//C = norm(p2 - p1) - L
//u = (p2 - p1) / norm(p2 - p1)
//Cdot = dot(u, v2 + cross(w2, r2) - v1 - cross(w1, r1))
//J = [-u -cross(r1, u) u cross(r2, u)]
//K = J * invM * JT
//= invMass1 + invI1 * cross(r1, u)^2 + invMass2 + invI2 * cross(r2, u)^2

/**
 * A distance joint constrains two points on two bodies to remain at a fixed distance from each
 * other. You can view this as a massless, rigid rod.
 */
public class DistanceJoint extends Joint {

  private float m_frequencyHz;
  private float m_dampingRatio;
  private float m_bias;

  // Solver shared
  private final Vec2 m_localAnchorA;
  private final Vec2 m_localAnchorB;
  private float m_gamma;
  private float m_impulse;
  private float m_length;

  // Solver temp
  private int m_indexA;
  private int m_indexB;
  private final Vec2 m_u = new Vec2();
  private final Vec2 m_rA = new Vec2();
  private final Vec2 m_rB = new Vec2();
  private final Vec2 m_localCenterA = new Vec2();
  private final Vec2 m_localCenterB = new Vec2();
  private float m_invMassA;
  private float m_invMassB;
  private float m_invIA;
  private float m_invIB;
  private float m_mass;

  protected DistanceJoint(IWorldPool argWorld, final DistanceJointDef def) {
    super(argWorld, def);
    m_localAnchorA = def.localAnchorA.clone();
    m_localAnchorB = def.localAnchorB.clone();
    m_length = def.length;
    m_impulse = 0.0f;
    m_frequencyHz = def.frequencyHz;
    m_dampingRatio = def.dampingRatio;
    m_gamma = 0.0f;
    m_bias = 0.0f;
  }

  public void setFrequency(float hz) {
    m_frequencyHz = hz;
  }

  public float getFrequency() {
    return m_frequencyHz;
  }

  public float getLength() {
    return m_length;
  }

  public void setLength(float argLength) {
    m_length = argLength;
  }

  public void setDampingRatio(float damp) {
    m_dampingRatio = damp;
  }

  public float getDampingRatio() {
    return m_dampingRatio;
  }

  @Override
  public void getAnchorA(Vec2 argOut) {
    m_bodyA.getWorldPointToOut(m_localAnchorA, argOut);
  }

  @Override
  public void getAnchorB(Vec2 argOut) {
    m_bodyB.getWorldPointToOut(m_localAnchorB, argOut);
  }

  public Vec2 getLocalAnchorA() {
    return m_localAnchorA;
  }

  public Vec2 getLocalAnchorB() {
    return m_localAnchorB;
  }

  /**
   * Get the reaction force given the inverse time step. Unit is N.
   */
  @Override
  public void getReactionForce(float inv_dt, Vec2 argOut) {
    argOut.x = m_impulse * m_u.x * inv_dt;
    argOut.y = m_impulse * m_u.y * inv_dt;
  }

  /**
   * Get the reaction torque given the inverse time step. Unit is N*m. This is always zero for a
   * distance joint.
   */
  @Override
  public float getReactionTorque(float inv_dt) {
    return 0.0f;
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

    qA.set(aA);
    qB.set(aB);

    // use m_u as temporary variable
    Rot.mulToOutUnsafe(qA, m_u.set(m_localAnchorA).subLocal(m_localCenterA), m_rA);
    Rot.mulToOutUnsafe(qB, m_u.set(m_localAnchorB).subLocal(m_localCenterB), m_rB);
    m_u.set(cB).addLocal(m_rB).subLocal(cA).subLocal(m_rA);

    pool.pushRot(2);

    // Handle singularity.
    float length = m_u.length();
    if (length > Settings.linearSlop) {
      m_u.x *= 1.0f / length;
      m_u.y *= 1.0f / length;
    } else {
      m_u.set(0.0f, 0.0f);
    }


    float crAu = Vec2.cross(m_rA, m_u);
    float crBu = Vec2.cross(m_rB, m_u);
    float invMass = m_invMassA + m_invIA * crAu * crAu + m_invMassB + m_invIB * crBu * crBu;

    // Compute the effective mass matrix.
    m_mass = invMass != 0.0f ? 1.0f / invMass : 0.0f;

    if (m_frequencyHz > 0.0f) {
      float C = length - m_length;

      // Frequency
      float omega = 2.0f * MathUtils.PI * m_frequencyHz;

      // Damping coefficient
      float d = 2.0f * m_mass * m_dampingRatio * omega;

      // Spring stiffness
      float k = m_mass * omega * omega;

      // magic formulas
      float h = data.step.dt;
      m_gamma = h * (d + h * k);
      m_gamma = m_gamma != 0.0f ? 1.0f / m_gamma : 0.0f;
      m_bias = C * h * k * m_gamma;

      invMass += m_gamma;
      m_mass = invMass != 0.0f ? 1.0f / invMass : 0.0f;
    } else {
      m_gamma = 0.0f;
      m_bias = 0.0f;
    }
    if (data.step.warmStarting) {

      // Scale the impulse to support a variable time step.
      m_impulse *= data.step.dtRatio;

      Vec2 P = pool.popVec2();
      P.set(m_u).mulLocal(m_impulse);

      vA.x -= m_invMassA * P.x;
      vA.y -= m_invMassA * P.y;
      wA -= m_invIA * Vec2.cross(m_rA, P);

      vB.x += m_invMassB * P.x;
      vB.y += m_invMassB * P.y;
      wB += m_invIB * Vec2.cross(m_rB, P);

      pool.pushVec2(1);
    } else {
      m_impulse = 0.0f;
    }
//    data.velocities[m_indexA].v.set(vA);
    data.velocities[m_indexA].w = wA;
//    data.velocities[m_indexB].v.set(vB);
    data.velocities[m_indexB].w = wB;
  }

  @Override
  public void solveVelocityConstraints(final SolverData data) {
    Vec2 vA = data.velocities[m_indexA].v;
    float wA = data.velocities[m_indexA].w;
    Vec2 vB = data.velocities[m_indexB].v;
    float wB = data.velocities[m_indexB].w;

    final Vec2 vpA = pool.popVec2();
    final Vec2 vpB = pool.popVec2();

    // Cdot = dot(u, v + cross(w, r))
    Vec2.crossToOutUnsafe(wA, m_rA, vpA);
    vpA.addLocal(vA);
    Vec2.crossToOutUnsafe(wB, m_rB, vpB);
    vpB.addLocal(vB);
    float Cdot = Vec2.dot(m_u, vpB.subLocal(vpA));

    float impulse = -m_mass * (Cdot + m_bias + m_gamma * m_impulse);
    m_impulse += impulse;


    float Px = impulse * m_u.x;
    float Py = impulse * m_u.y;

    vA.x -= m_invMassA * Px;
    vA.y -= m_invMassA * Py;
    wA -= m_invIA * (m_rA.x * Py - m_rA.y * Px);
    vB.x += m_invMassB * Px;
    vB.y += m_invMassB * Py;
    wB += m_invIB * (m_rB.x * Py - m_rB.y * Px);

//    data.velocities[m_indexA].v.set(vA);
    data.velocities[m_indexA].w = wA;
//    data.velocities[m_indexB].v.set(vB);
    data.velocities[m_indexB].w = wB;

    pool.pushVec2(2);
  }

  @Override
  public boolean solvePositionConstraints(final SolverData data) {
    if (m_frequencyHz > 0.0f) {
      return true;
    }
    final Rot qA = pool.popRot();
    final Rot qB = pool.popRot();
    final Vec2 rA = pool.popVec2();
    final Vec2 rB = pool.popVec2();
    final Vec2 u = pool.popVec2();

    Vec2 cA = data.positions[m_indexA].c;
    float aA = data.positions[m_indexA].a;
    Vec2 cB = data.positions[m_indexB].c;
    float aB = data.positions[m_indexB].a;

    qA.set(aA);
    qB.set(aB);

    Rot.mulToOutUnsafe(qA, u.set(m_localAnchorA).subLocal(m_localCenterA), rA);
    Rot.mulToOutUnsafe(qB, u.set(m_localAnchorB).subLocal(m_localCenterB), rB);
    u.set(cB).addLocal(rB).subLocal(cA).subLocal(rA);


    float length = u.normalize();
    float C = length - m_length;
    C = MathUtils.clamp(C, -Settings.maxLinearCorrection, Settings.maxLinearCorrection);

    float impulse = -m_mass * C;
    float Px = impulse * u.x;
    float Py = impulse * u.y;

    cA.x -= m_invMassA * Px;
    cA.y -= m_invMassA * Py;
    aA -= m_invIA * (rA.x * Py - rA.y * Px);
    cB.x += m_invMassB * Px;
    cB.y += m_invMassB * Py;
    aB += m_invIB * (rB.x * Py - rB.y * Px);

//    data.positions[m_indexA].c.set(cA);
    data.positions[m_indexA].a = aA;
//    data.positions[m_indexB].c.set(cB);
    data.positions[m_indexB].a = aB;

    pool.pushVec2(3);
    pool.pushRot(2);

    return MathUtils.abs(C) < Settings.linearSlop;
  }
}
