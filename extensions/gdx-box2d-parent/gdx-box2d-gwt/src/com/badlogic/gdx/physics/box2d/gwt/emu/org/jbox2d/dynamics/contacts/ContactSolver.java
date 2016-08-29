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
package org.jbox2d.dynamics.contacts;

import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.ManifoldPoint;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Mat22;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.TimeStep;
import org.jbox2d.dynamics.contacts.ContactVelocityConstraint.VelocityConstraintPoint;

/**
 * @author Daniel
 */
public class ContactSolver {

  public static final boolean DEBUG_SOLVER = false;
  public static final float k_errorTol = 1e-3f;
  /**
   * For each solver, this is the initial number of constraints in the array, which expands as
   * needed.
   */
  public static final int INITIAL_NUM_CONSTRAINTS = 256;

  /**
   * Ensure a reasonable condition number. for the block solver
   */
  public static final float k_maxConditionNumber = 100.0f;

  public TimeStep m_step;
  public Position[] m_positions;
  public Velocity[] m_velocities;
  public ContactPositionConstraint[] m_positionConstraints;
  public ContactVelocityConstraint[] m_velocityConstraints;
  public Contact[] m_contacts;
  public int m_count;

  public ContactSolver() {
    m_positionConstraints = new ContactPositionConstraint[INITIAL_NUM_CONSTRAINTS];
    m_velocityConstraints = new ContactVelocityConstraint[INITIAL_NUM_CONSTRAINTS];
    for (int i = 0; i < INITIAL_NUM_CONSTRAINTS; i++) {
      m_positionConstraints[i] = new ContactPositionConstraint();
      m_velocityConstraints[i] = new ContactVelocityConstraint();
    }
  }

  public final void init(ContactSolverDef def) {
    // System.out.println("Initializing contact solver");
    m_step = def.step;
    m_count = def.count;

    if (m_positionConstraints.length < m_count) {
      ContactPositionConstraint[] old = m_positionConstraints;
      m_positionConstraints = new ContactPositionConstraint[MathUtils.max(old.length * 2, m_count)];
      System.arraycopy(old, 0, m_positionConstraints, 0, old.length);
      for (int i = old.length; i < m_positionConstraints.length; i++) {
        m_positionConstraints[i] = new ContactPositionConstraint();
      }
    }

    if (m_velocityConstraints.length < m_count) {
      ContactVelocityConstraint[] old = m_velocityConstraints;
      m_velocityConstraints = new ContactVelocityConstraint[MathUtils.max(old.length * 2, m_count)];
      System.arraycopy(old, 0, m_velocityConstraints, 0, old.length);
      for (int i = old.length; i < m_velocityConstraints.length; i++) {
        m_velocityConstraints[i] = new ContactVelocityConstraint();
      }
    }

    m_positions = def.positions;
    m_velocities = def.velocities;
    m_contacts = def.contacts;

    for (int i = 0; i < m_count; ++i) {
      // System.out.println("contacts: " + m_count);
      final Contact contact = m_contacts[i];

      final Fixture fixtureA = contact.m_fixtureA;
      final Fixture fixtureB = contact.m_fixtureB;
      final Shape shapeA = fixtureA.getShape();
      final Shape shapeB = fixtureB.getShape();
      final float radiusA = shapeA.m_radius;
      final float radiusB = shapeB.m_radius;
      final Body bodyA = fixtureA.getBody();
      final Body bodyB = fixtureB.getBody();
      final Manifold manifold = contact.getManifold();

      int pointCount = manifold.pointCount;
      assert (pointCount > 0);

      ContactVelocityConstraint vc = m_velocityConstraints[i];
      vc.friction = contact.m_friction;
      vc.restitution = contact.m_restitution;
      vc.tangentSpeed = contact.m_tangentSpeed;
      vc.indexA = bodyA.m_islandIndex;
      vc.indexB = bodyB.m_islandIndex;
      vc.invMassA = bodyA.m_invMass;
      vc.invMassB = bodyB.m_invMass;
      vc.invIA = bodyA.m_invI;
      vc.invIB = bodyB.m_invI;
      vc.contactIndex = i;
      vc.pointCount = pointCount;
      vc.K.setZero();
      vc.normalMass.setZero();

      ContactPositionConstraint pc = m_positionConstraints[i];
      pc.indexA = bodyA.m_islandIndex;
      pc.indexB = bodyB.m_islandIndex;
      pc.invMassA = bodyA.m_invMass;
      pc.invMassB = bodyB.m_invMass;
      pc.localCenterA.set(bodyA.m_sweep.localCenter);
      pc.localCenterB.set(bodyB.m_sweep.localCenter);
      pc.invIA = bodyA.m_invI;
      pc.invIB = bodyB.m_invI;
      pc.localNormal.set(manifold.localNormal);
      pc.localPoint.set(manifold.localPoint);
      pc.pointCount = pointCount;
      pc.radiusA = radiusA;
      pc.radiusB = radiusB;
      pc.type = manifold.type;

      // System.out.println("contact point count: " + pointCount);
      for (int j = 0; j < pointCount; j++) {
        ManifoldPoint cp = manifold.points[j];
        VelocityConstraintPoint vcp = vc.points[j];

        if (m_step.warmStarting) {
          // assert(cp.normalImpulse == 0);
          // System.out.println("contact normal impulse: " + cp.normalImpulse);
          vcp.normalImpulse = m_step.dtRatio * cp.normalImpulse;
          vcp.tangentImpulse = m_step.dtRatio * cp.tangentImpulse;
        } else {
          vcp.normalImpulse = 0;
          vcp.tangentImpulse = 0;
        }

        vcp.rA.setZero();
        vcp.rB.setZero();
        vcp.normalMass = 0;
        vcp.tangentMass = 0;
        vcp.velocityBias = 0;
        pc.localPoints[j].x = cp.localPoint.x;
        pc.localPoints[j].y = cp.localPoint.y;
      }
    }
  }

  public void warmStart() {
    // Warm start.
    for (int i = 0; i < m_count; ++i) {
      final ContactVelocityConstraint vc = m_velocityConstraints[i];

      int indexA = vc.indexA;
      int indexB = vc.indexB;
      float mA = vc.invMassA;
      float iA = vc.invIA;
      float mB = vc.invMassB;
      float iB = vc.invIB;
      int pointCount = vc.pointCount;

      Vec2 vA = m_velocities[indexA].v;
      float wA = m_velocities[indexA].w;
      Vec2 vB = m_velocities[indexB].v;
      float wB = m_velocities[indexB].w;

      Vec2 normal = vc.normal;
      float tangentx = 1.0f * normal.y;
      float tangenty = -1.0f * normal.x;

      for (int j = 0; j < pointCount; ++j) {
        VelocityConstraintPoint vcp = vc.points[j];
        float Px = tangentx * vcp.tangentImpulse + normal.x * vcp.normalImpulse;
        float Py = tangenty * vcp.tangentImpulse + normal.y * vcp.normalImpulse;

        wA -= iA * (vcp.rA.x * Py - vcp.rA.y * Px);
        vA.x -= Px * mA;
        vA.y -= Py * mA;
        wB += iB * (vcp.rB.x * Py - vcp.rB.y * Px);
        vB.x += Px * mB;
        vB.y += Py * mB;
      }
      m_velocities[indexA].w = wA;
      m_velocities[indexB].w = wB;
    }
  }

  // djm pooling, and from above
  private final Transform xfA = new Transform();
  private final Transform xfB = new Transform();
  private final WorldManifold worldManifold = new WorldManifold();

  public final void initializeVelocityConstraints() {

    // Warm start.
    for (int i = 0; i < m_count; ++i) {
      ContactVelocityConstraint vc = m_velocityConstraints[i];
      ContactPositionConstraint pc = m_positionConstraints[i];

      float radiusA = pc.radiusA;
      float radiusB = pc.radiusB;
      Manifold manifold = m_contacts[vc.contactIndex].getManifold();

      int indexA = vc.indexA;
      int indexB = vc.indexB;

      float mA = vc.invMassA;
      float mB = vc.invMassB;
      float iA = vc.invIA;
      float iB = vc.invIB;
      Vec2 localCenterA = pc.localCenterA;
      Vec2 localCenterB = pc.localCenterB;

      Vec2 cA = m_positions[indexA].c;
      float aA = m_positions[indexA].a;
      Vec2 vA = m_velocities[indexA].v;
      float wA = m_velocities[indexA].w;

      Vec2 cB = m_positions[indexB].c;
      float aB = m_positions[indexB].a;
      Vec2 vB = m_velocities[indexB].v;
      float wB = m_velocities[indexB].w;

      assert (manifold.pointCount > 0);

      final Rot xfAq = xfA.q;
      final Rot xfBq = xfB.q;
      xfAq.set(aA);
      xfBq.set(aB);
      xfA.p.x = cA.x - (xfAq.c * localCenterA.x - xfAq.s * localCenterA.y);
      xfA.p.y = cA.y - (xfAq.s * localCenterA.x + xfAq.c * localCenterA.y);
      xfB.p.x = cB.x - (xfBq.c * localCenterB.x - xfBq.s * localCenterB.y);
      xfB.p.y = cB.y - (xfBq.s * localCenterB.x + xfBq.c * localCenterB.y);

      worldManifold.initialize(manifold, xfA, radiusA, xfB, radiusB);

      final Vec2 vcnormal = vc.normal;
      vcnormal.x = worldManifold.normal.x;
      vcnormal.y = worldManifold.normal.y;

      int pointCount = vc.pointCount;
      for (int j = 0; j < pointCount; ++j) {
        VelocityConstraintPoint vcp = vc.points[j];
        Vec2 wmPj = worldManifold.points[j];
        final Vec2 vcprA = vcp.rA;
        final Vec2 vcprB = vcp.rB;
        vcprA.x = wmPj.x - cA.x;
        vcprA.y = wmPj.y - cA.y;
        vcprB.x = wmPj.x - cB.x;
        vcprB.y = wmPj.y - cB.y;

        float rnA = vcprA.x * vcnormal.y - vcprA.y * vcnormal.x;
        float rnB = vcprB.x * vcnormal.y - vcprB.y * vcnormal.x;

        float kNormal = mA + mB + iA * rnA * rnA + iB * rnB * rnB;

        vcp.normalMass = kNormal > 0.0f ? 1.0f / kNormal : 0.0f;

        float tangentx = 1.0f * vcnormal.y;
        float tangenty = -1.0f * vcnormal.x;

        float rtA = vcprA.x * tangenty - vcprA.y * tangentx;
        float rtB = vcprB.x * tangenty - vcprB.y * tangentx;

        float kTangent = mA + mB + iA * rtA * rtA + iB * rtB * rtB;

        vcp.tangentMass = kTangent > 0.0f ? 1.0f / kTangent : 0.0f;

        // Setup a velocity bias for restitution.
        vcp.velocityBias = 0.0f;
        float tempx = vB.x + -wB * vcprB.y - vA.x - (-wA * vcprA.y);
        float tempy = vB.y + wB * vcprB.x - vA.y - (wA * vcprA.x);
        float vRel = vcnormal.x * tempx + vcnormal.y * tempy;
        if (vRel < -Settings.velocityThreshold) {
          vcp.velocityBias = -vc.restitution * vRel;
        }
      }

      // If we have two points, then prepare the block solver.
      if (vc.pointCount == 2) {
        VelocityConstraintPoint vcp1 = vc.points[0];
        VelocityConstraintPoint vcp2 = vc.points[1];
        float rn1A = vcp1.rA.x * vcnormal.y - vcp1.rA.y * vcnormal.x;
        float rn1B = vcp1.rB.x * vcnormal.y - vcp1.rB.y * vcnormal.x;
        float rn2A = vcp2.rA.x * vcnormal.y - vcp2.rA.y * vcnormal.x;
        float rn2B = vcp2.rB.x * vcnormal.y - vcp2.rB.y * vcnormal.x;

        float k11 = mA + mB + iA * rn1A * rn1A + iB * rn1B * rn1B;
        float k22 = mA + mB + iA * rn2A * rn2A + iB * rn2B * rn2B;
        float k12 = mA + mB + iA * rn1A * rn2A + iB * rn1B * rn2B;
        if (k11 * k11 < k_maxConditionNumber * (k11 * k22 - k12 * k12)) {
          // K is safe to invert.
          vc.K.ex.x = k11;
          vc.K.ex.y = k12;
          vc.K.ey.x = k12;
          vc.K.ey.y = k22;
          vc.K.invertToOut(vc.normalMass);
        } else {
          // The constraints are redundant, just use one.
          // TODO_ERIN use deepest?
          vc.pointCount = 1;
        }
      }
    }
  }


  public final void solveVelocityConstraints() {
    for (int i = 0; i < m_count; ++i) {
      final ContactVelocityConstraint vc = m_velocityConstraints[i];

      int indexA = vc.indexA;
      int indexB = vc.indexB;

      float mA = vc.invMassA;
      float mB = vc.invMassB;
      float iA = vc.invIA;
      float iB = vc.invIB;
      int pointCount = vc.pointCount;

      Vec2 vA = m_velocities[indexA].v;
      float wA = m_velocities[indexA].w;
      Vec2 vB = m_velocities[indexB].v;
      float wB = m_velocities[indexB].w;

      Vec2 normal = vc.normal;
      final float normalx = normal.x;
      final float normaly = normal.y;
      float tangentx = 1.0f * vc.normal.y;
      float tangenty = -1.0f * vc.normal.x;
      final float friction = vc.friction;

      assert (pointCount == 1 || pointCount == 2);

      // Solve tangent constraints
      for (int j = 0; j < pointCount; ++j) {
        final VelocityConstraintPoint vcp = vc.points[j];
        final Vec2 a = vcp.rA;
        float dvx = -wB * vcp.rB.y + vB.x - vA.x + wA * a.y;
        float dvy = wB * vcp.rB.x + vB.y - vA.y - wA * a.x;

        // Compute tangent force
        final float vt = dvx * tangentx + dvy * tangenty - vc.tangentSpeed;
        float lambda = vcp.tangentMass * (-vt);

        // Clamp the accumulated force
        final float maxFriction = friction * vcp.normalImpulse;
        final float newImpulse =
            MathUtils.clamp(vcp.tangentImpulse + lambda, -maxFriction, maxFriction);
        lambda = newImpulse - vcp.tangentImpulse;
        vcp.tangentImpulse = newImpulse;

        // Apply contact impulse
        // Vec2 P = lambda * tangent;

        final float Px = tangentx * lambda;
        final float Py = tangenty * lambda;

        // vA -= invMassA * P;
        vA.x -= Px * mA;
        vA.y -= Py * mA;
        wA -= iA * (vcp.rA.x * Py - vcp.rA.y * Px);

        // vB += invMassB * P;
        vB.x += Px * mB;
        vB.y += Py * mB;
        wB += iB * (vcp.rB.x * Py - vcp.rB.y * Px);
      }

      // Solve normal constraints
      if (vc.pointCount == 1) {
        final VelocityConstraintPoint vcp = vc.points[0];

        // Relative velocity at contact
        // Vec2 dv = vB + Cross(wB, vcp.rB) - vA - Cross(wA, vcp.rA);

        float dvx = -wB * vcp.rB.y + vB.x - vA.x + wA * vcp.rA.y;
        float dvy = wB * vcp.rB.x + vB.y - vA.y - wA * vcp.rA.x;

        // Compute normal impulse
        final float vn = dvx * normalx + dvy * normaly;
        float lambda = -vcp.normalMass * (vn - vcp.velocityBias);

        // Clamp the accumulated impulse
        float a = vcp.normalImpulse + lambda;
        final float newImpulse = (a > 0.0f ? a : 0.0f);
        lambda = newImpulse - vcp.normalImpulse;
        vcp.normalImpulse = newImpulse;

        // Apply contact impulse
        float Px = normalx * lambda;
        float Py = normaly * lambda;

        // vA -= invMassA * P;
        vA.x -= Px * mA;
        vA.y -= Py * mA;
        wA -= iA * (vcp.rA.x * Py - vcp.rA.y * Px);

        // vB += invMassB * P;
        vB.x += Px * mB;
        vB.y += Py * mB;
        wB += iB * (vcp.rB.x * Py - vcp.rB.y * Px);
      } else {
        // Block solver developed in collaboration with Dirk Gregorius (back in 01/07 on
        // Box2D_Lite).
        // Build the mini LCP for this contact patch
        //
        // vn = A * x + b, vn >= 0, , vn >= 0, x >= 0 and vn_i * x_i = 0 with i = 1..2
        //
        // A = J * W * JT and J = ( -n, -r1 x n, n, r2 x n )
        // b = vn_0 - velocityBias
        //
        // The system is solved using the "Total enumeration method" (s. Murty). The complementary
        // constraint vn_i * x_i
        // implies that we must have in any solution either vn_i = 0 or x_i = 0. So for the 2D
        // contact problem the cases
        // vn1 = 0 and vn2 = 0, x1 = 0 and x2 = 0, x1 = 0 and vn2 = 0, x2 = 0 and vn1 = 0 need to be
        // tested. The first valid
        // solution that satisfies the problem is chosen.
        //
        // In order to account of the accumulated impulse 'a' (because of the iterative nature of
        // the solver which only requires
        // that the accumulated impulse is clamped and not the incremental impulse) we change the
        // impulse variable (x_i).
        //
        // Substitute:
        //
        // x = a + d
        //
        // a := old total impulse
        // x := new total impulse
        // d := incremental impulse
        //
        // For the current iteration we extend the formula for the incremental impulse
        // to compute the new total impulse:
        //
        // vn = A * d + b
        // = A * (x - a) + b
        // = A * x + b - A * a
        // = A * x + b'
        // b' = b - A * a;

        final VelocityConstraintPoint cp1 = vc.points[0];
        final VelocityConstraintPoint cp2 = vc.points[1];
        final Vec2 cp1rA = cp1.rA;
        final Vec2 cp1rB = cp1.rB;
        final Vec2 cp2rA = cp2.rA;
        final Vec2 cp2rB = cp2.rB;
        float ax = cp1.normalImpulse;
        float ay = cp2.normalImpulse;

        assert (ax >= 0.0f && ay >= 0.0f);
        // Relative velocity at contact
        // Vec2 dv1 = vB + Cross(wB, cp1.rB) - vA - Cross(wA, cp1.rA);
        float dv1x = -wB * cp1rB.y + vB.x - vA.x + wA * cp1rA.y;
        float dv1y = wB * cp1rB.x + vB.y - vA.y - wA * cp1rA.x;

        // Vec2 dv2 = vB + Cross(wB, cp2.rB) - vA - Cross(wA, cp2.rA);
        float dv2x = -wB * cp2rB.y + vB.x - vA.x + wA * cp2rA.y;
        float dv2y = wB * cp2rB.x + vB.y - vA.y - wA * cp2rA.x;

        // Compute normal velocity
        float vn1 = dv1x * normalx + dv1y * normaly;
        float vn2 = dv2x * normalx + dv2y * normaly;

        float bx = vn1 - cp1.velocityBias;
        float by = vn2 - cp2.velocityBias;

        // Compute b'
        Mat22 R = vc.K;
        bx -= R.ex.x * ax + R.ey.x * ay;
        by -= R.ex.y * ax + R.ey.y * ay;

        // final float k_errorTol = 1e-3f;
        // B2_NOT_USED(k_errorTol);
        for (;;) {
          //
          // Case 1: vn = 0
          //
          // 0 = A * x' + b'
          //
          // Solve for x':
          //
          // x' = - inv(A) * b'
          //
          // Vec2 x = - Mul(c.normalMass, b);
          Mat22 R1 = vc.normalMass;
          float xx = R1.ex.x * bx + R1.ey.x * by;
          float xy = R1.ex.y * bx + R1.ey.y * by;
          xx *= -1;
          xy *= -1;

          if (xx >= 0.0f && xy >= 0.0f) {
            // Get the incremental impulse
            // Vec2 d = x - a;
            float dx = xx - ax;
            float dy = xy - ay;

            // Apply incremental impulse
            // Vec2 P1 = d.x * normal;
            // Vec2 P2 = d.y * normal;
            float P1x = dx * normalx;
            float P1y = dx * normaly;
            float P2x = dy * normalx;
            float P2y = dy * normaly;

            /*
             * vA -= invMassA * (P1 + P2); wA -= invIA * (Cross(cp1.rA, P1) + Cross(cp2.rA, P2));
             * 
             * vB += invMassB * (P1 + P2); wB += invIB * (Cross(cp1.rB, P1) + Cross(cp2.rB, P2));
             */

            vA.x -= mA * (P1x + P2x);
            vA.y -= mA * (P1y + P2y);
            vB.x += mB * (P1x + P2x);
            vB.y += mB * (P1y + P2y);

            wA -= iA * (cp1rA.x * P1y - cp1rA.y * P1x + (cp2rA.x * P2y - cp2rA.y * P2x));
            wB += iB * (cp1rB.x * P1y - cp1rB.y * P1x + (cp2rB.x * P2y - cp2rB.y * P2x));

            // Accumulate
            cp1.normalImpulse = xx;
            cp2.normalImpulse = xy;

            /*
             * #if B2_DEBUG_SOLVER == 1 // Postconditions dv1 = vB + Cross(wB, cp1.rB) - vA -
             * Cross(wA, cp1.rA); dv2 = vB + Cross(wB, cp2.rB) - vA - Cross(wA, cp2.rA);
             * 
             * // Compute normal velocity vn1 = Dot(dv1, normal); vn2 = Dot(dv2, normal);
             * 
             * assert(Abs(vn1 - cp1.velocityBias) < k_errorTol); assert(Abs(vn2 - cp2.velocityBias)
             * < k_errorTol); #endif
             */
            if (DEBUG_SOLVER) {
              // Postconditions
              Vec2 dv1 = vB.add(Vec2.cross(wB, cp1rB).subLocal(vA).subLocal(Vec2.cross(wA, cp1rA)));
              Vec2 dv2 = vB.add(Vec2.cross(wB, cp2rB).subLocal(vA).subLocal(Vec2.cross(wA, cp2rA)));
              // Compute normal velocity
              vn1 = Vec2.dot(dv1, normal);
              vn2 = Vec2.dot(dv2, normal);

              assert (MathUtils.abs(vn1 - cp1.velocityBias) < k_errorTol);
              assert (MathUtils.abs(vn2 - cp2.velocityBias) < k_errorTol);
            }
            break;
          }

          //
          // Case 2: vn1 = 0 and x2 = 0
          //
          // 0 = a11 * x1' + a12 * 0 + b1'
          // vn2 = a21 * x1' + a22 * 0 + '
          //
          xx = -cp1.normalMass * bx;
          xy = 0.0f;
          vn1 = 0.0f;
          vn2 = vc.K.ex.y * xx + by;

          if (xx >= 0.0f && vn2 >= 0.0f) {
            // Get the incremental impulse
            float dx = xx - ax;
            float dy = xy - ay;

            // Apply incremental impulse
            // Vec2 P1 = d.x * normal;
            // Vec2 P2 = d.y * normal;
            float P1x = normalx * dx;
            float P1y = normaly * dx;
            float P2x = normalx * dy;
            float P2y = normaly * dy;

            /*
             * Vec2 P1 = d.x * normal; Vec2 P2 = d.y * normal; vA -= invMassA * (P1 + P2); wA -=
             * invIA * (Cross(cp1.rA, P1) + Cross(cp2.rA, P2));
             * 
             * vB += invMassB * (P1 + P2); wB += invIB * (Cross(cp1.rB, P1) + Cross(cp2.rB, P2));
             */

            vA.x -= mA * (P1x + P2x);
            vA.y -= mA * (P1y + P2y);
            vB.x += mB * (P1x + P2x);
            vB.y += mB * (P1y + P2y);

            wA -= iA * (cp1rA.x * P1y - cp1rA.y * P1x + (cp2rA.x * P2y - cp2rA.y * P2x));
            wB += iB * (cp1rB.x * P1y - cp1rB.y * P1x + (cp2rB.x * P2y - cp2rB.y * P2x));

            // Accumulate
            cp1.normalImpulse = xx;
            cp2.normalImpulse = xy;

            /*
             * #if B2_DEBUG_SOLVER == 1 // Postconditions dv1 = vB + Cross(wB, cp1.rB) - vA -
             * Cross(wA, cp1.rA);
             * 
             * // Compute normal velocity vn1 = Dot(dv1, normal);
             * 
             * assert(Abs(vn1 - cp1.velocityBias) < k_errorTol); #endif
             */
            if (DEBUG_SOLVER) {
              // Postconditions
              Vec2 dv1 = vB.add(Vec2.cross(wB, cp1rB).subLocal(vA).subLocal(Vec2.cross(wA, cp1rA)));
              // Compute normal velocity
              vn1 = Vec2.dot(dv1, normal);

              assert (MathUtils.abs(vn1 - cp1.velocityBias) < k_errorTol);
            }
            break;
          }

          //
          // Case 3: wB = 0 and x1 = 0
          //
          // vn1 = a11 * 0 + a12 * x2' + b1'
          // 0 = a21 * 0 + a22 * x2' + '
          //
          xx = 0.0f;
          xy = -cp2.normalMass * by;
          vn1 = vc.K.ey.x * xy + bx;
          vn2 = 0.0f;

          if (xy >= 0.0f && vn1 >= 0.0f) {
            // Resubstitute for the incremental impulse
            float dx = xx - ax;
            float dy = xy - ay;

            // Apply incremental impulse
            /*
             * Vec2 P1 = d.x * normal; Vec2 P2 = d.y * normal; vA -= invMassA * (P1 + P2); wA -=
             * invIA * (Cross(cp1.rA, P1) + Cross(cp2.rA, P2));
             * 
             * vB += invMassB * (P1 + P2); wB += invIB * (Cross(cp1.rB, P1) + Cross(cp2.rB, P2));
             */

            float P1x = normalx * dx;
            float P1y = normaly * dx;
            float P2x = normalx * dy;
            float P2y = normaly * dy;

            vA.x -= mA * (P1x + P2x);
            vA.y -= mA * (P1y + P2y);
            vB.x += mB * (P1x + P2x);
            vB.y += mB * (P1y + P2y);

            wA -= iA * (cp1rA.x * P1y - cp1rA.y * P1x + (cp2rA.x * P2y - cp2rA.y * P2x));
            wB += iB * (cp1rB.x * P1y - cp1rB.y * P1x + (cp2rB.x * P2y - cp2rB.y * P2x));

            // Accumulate
            cp1.normalImpulse = xx;
            cp2.normalImpulse = xy;

            /*
             * #if B2_DEBUG_SOLVER == 1 // Postconditions dv2 = vB + Cross(wB, cp2.rB) - vA -
             * Cross(wA, cp2.rA);
             * 
             * // Compute normal velocity vn2 = Dot(dv2, normal);
             * 
             * assert(Abs(vn2 - cp2.velocityBias) < k_errorTol); #endif
             */
            if (DEBUG_SOLVER) {
              // Postconditions
              Vec2 dv2 = vB.add(Vec2.cross(wB, cp2rB).subLocal(vA).subLocal(Vec2.cross(wA, cp2rA)));
              // Compute normal velocity
              vn2 = Vec2.dot(dv2, normal);

              assert (MathUtils.abs(vn2 - cp2.velocityBias) < k_errorTol);
            }
            break;
          }

          //
          // Case 4: x1 = 0 and x2 = 0
          //
          // vn1 = b1
          // vn2 = ;
          xx = 0.0f;
          xy = 0.0f;
          vn1 = bx;
          vn2 = by;

          if (vn1 >= 0.0f && vn2 >= 0.0f) {
            // Resubstitute for the incremental impulse
            float dx = xx - ax;
            float dy = xy - ay;

            // Apply incremental impulse
            /*
             * Vec2 P1 = d.x * normal; Vec2 P2 = d.y * normal; vA -= invMassA * (P1 + P2); wA -=
             * invIA * (Cross(cp1.rA, P1) + Cross(cp2.rA, P2));
             * 
             * vB += invMassB * (P1 + P2); wB += invIB * (Cross(cp1.rB, P1) + Cross(cp2.rB, P2));
             */

            float P1x = normalx * dx;
            float P1y = normaly * dx;
            float P2x = normalx * dy;
            float P2y = normaly * dy;

            vA.x -= mA * (P1x + P2x);
            vA.y -= mA * (P1y + P2y);
            vB.x += mB * (P1x + P2x);
            vB.y += mB * (P1y + P2y);

            wA -= iA * (cp1rA.x * P1y - cp1rA.y * P1x + (cp2rA.x * P2y - cp2rA.y * P2x));
            wB += iB * (cp1rB.x * P1y - cp1rB.y * P1x + (cp2rB.x * P2y - cp2rB.y * P2x));

            // Accumulate
            cp1.normalImpulse = xx;
            cp2.normalImpulse = xy;

            break;
          }

          // No solution, give up. This is hit sometimes, but it doesn't seem to matter.
          break;
        }
      }

      // m_velocities[indexA].v.set(vA);
      m_velocities[indexA].w = wA;
      // m_velocities[indexB].v.set(vB);
      m_velocities[indexB].w = wB;
    }
  }

  public void storeImpulses() {
    for (int i = 0; i < m_count; i++) {
      final ContactVelocityConstraint vc = m_velocityConstraints[i];
      final Manifold manifold = m_contacts[vc.contactIndex].getManifold();

      for (int j = 0; j < vc.pointCount; j++) {
        manifold.points[j].normalImpulse = vc.points[j].normalImpulse;
        manifold.points[j].tangentImpulse = vc.points[j].tangentImpulse;
      }
    }
  }

  /*
   * #if 0 // Sequential solver. bool ContactSolver::SolvePositionConstraints(float baumgarte) {
   * float minSeparation = 0.0f;
   * 
   * for (int i = 0; i < m_constraintCount; ++i) { ContactConstraint* c = m_constraints + i; Body*
   * bodyA = c.bodyA; Body* bodyB = c.bodyB; float invMassA = bodyA.m_mass * bodyA.m_invMass; float
   * invIA = bodyA.m_mass * bodyA.m_invI; float invMassB = bodyB.m_mass * bodyB.m_invMass; float
   * invIB = bodyB.m_mass * bodyB.m_invI;
   * 
   * Vec2 normal = c.normal;
   * 
   * // Solve normal constraints for (int j = 0; j < c.pointCount; ++j) { ContactConstraintPoint*
   * ccp = c.points + j;
   * 
   * Vec2 r1 = Mul(bodyA.GetXForm().R, ccp.localAnchorA - bodyA.GetLocalCenter()); Vec2 r2 =
   * Mul(bodyB.GetXForm().R, ccp.localAnchorB - bodyB.GetLocalCenter());
   * 
   * Vec2 p1 = bodyA.m_sweep.c + r1; Vec2 p2 = bodyB.m_sweep.c + r2; Vec2 dp = p2 - p1;
   * 
   * // Approximate the current separation. float separation = Dot(dp, normal) + ccp.separation;
   * 
   * // Track max constraint error. minSeparation = Min(minSeparation, separation);
   * 
   * // Prevent large corrections and allow slop. float C = Clamp(baumgarte * (separation +
   * _linearSlop), -_maxLinearCorrection, 0.0f);
   * 
   * // Compute normal impulse float impulse = -ccp.equalizedMass * C;
   * 
   * Vec2 P = impulse * normal;
   * 
   * bodyA.m_sweep.c -= invMassA * P; bodyA.m_sweep.a -= invIA * Cross(r1, P);
   * bodyA.SynchronizeTransform();
   * 
   * bodyB.m_sweep.c += invMassB * P; bodyB.m_sweep.a += invIB * Cross(r2, P);
   * bodyB.SynchronizeTransform(); } }
   * 
   * // We can't expect minSpeparation >= -_linearSlop because we don't // push the separation above
   * -_linearSlop. return minSeparation >= -1.5f * _linearSlop; }
   */

  private final PositionSolverManifold psolver = new PositionSolverManifold();

  /**
   * Sequential solver.
   */
  public final boolean solvePositionConstraints() {
    float minSeparation = 0.0f;

    for (int i = 0; i < m_count; ++i) {
      ContactPositionConstraint pc = m_positionConstraints[i];

      int indexA = pc.indexA;
      int indexB = pc.indexB;

      float mA = pc.invMassA;
      float iA = pc.invIA;
      Vec2 localCenterA = pc.localCenterA;
      final float localCenterAx = localCenterA.x;
      final float localCenterAy = localCenterA.y;
      float mB = pc.invMassB;
      float iB = pc.invIB;
      Vec2 localCenterB = pc.localCenterB;
      final float localCenterBx = localCenterB.x;
      final float localCenterBy = localCenterB.y;
      int pointCount = pc.pointCount;

      Vec2 cA = m_positions[indexA].c;
      float aA = m_positions[indexA].a;
      Vec2 cB = m_positions[indexB].c;
      float aB = m_positions[indexB].a;

      // Solve normal constraints
      for (int j = 0; j < pointCount; ++j) {
        final Rot xfAq = xfA.q;
        final Rot xfBq = xfB.q;
        xfAq.set(aA);
        xfBq.set(aB);
        xfA.p.x = cA.x - xfAq.c * localCenterAx + xfAq.s * localCenterAy;
        xfA.p.y = cA.y - xfAq.s * localCenterAx - xfAq.c * localCenterAy;
        xfB.p.x = cB.x - xfBq.c * localCenterBx + xfBq.s * localCenterBy;
        xfB.p.y = cB.y - xfBq.s * localCenterBx - xfBq.c * localCenterBy;

        final PositionSolverManifold psm = psolver;
        psm.initialize(pc, xfA, xfB, j);
        final Vec2 normal = psm.normal;
        final Vec2 point = psm.point;
        final float separation = psm.separation;

        float rAx = point.x - cA.x;
        float rAy = point.y - cA.y;
        float rBx = point.x - cB.x;
        float rBy = point.y - cB.y;

        // Track max constraint error.
        minSeparation = MathUtils.min(minSeparation, separation);

        // Prevent large corrections and allow slop.
        final float C =
            MathUtils.clamp(Settings.baumgarte * (separation + Settings.linearSlop),
                -Settings.maxLinearCorrection, 0.0f);

        // Compute the effective mass.
        final float rnA = rAx * normal.y - rAy * normal.x;
        final float rnB = rBx * normal.y - rBy * normal.x;
        final float K = mA + mB + iA * rnA * rnA + iB * rnB * rnB;

        // Compute normal impulse
        final float impulse = K > 0.0f ? -C / K : 0.0f;

        float Px = normal.x * impulse;
        float Py = normal.y * impulse;
        
        cA.x -= Px * mA;
        cA.y -= Py * mA;
        aA -= iA * (rAx * Py - rAy * Px);

        cB.x += Px * mB;
        cB.y += Py * mB;
        aB += iB * (rBx * Py - rBy * Px);
      }

      // m_positions[indexA].c.set(cA);
      m_positions[indexA].a = aA;

      // m_positions[indexB].c.set(cB);
      m_positions[indexB].a = aB;
    }

    // We can't expect minSpeparation >= -linearSlop because we don't
    // push the separation above -linearSlop.
    return minSeparation >= -3.0f * Settings.linearSlop;
  }

  // Sequential position solver for position constraints.
  public boolean solveTOIPositionConstraints(int toiIndexA, int toiIndexB) {
    float minSeparation = 0.0f;

    for (int i = 0; i < m_count; ++i) {
      ContactPositionConstraint pc = m_positionConstraints[i];

      int indexA = pc.indexA;
      int indexB = pc.indexB;
      Vec2 localCenterA = pc.localCenterA;
      Vec2 localCenterB = pc.localCenterB;
      final float localCenterAx = localCenterA.x;
      final float localCenterAy = localCenterA.y;
      final float localCenterBx = localCenterB.x;
      final float localCenterBy = localCenterB.y;
      int pointCount = pc.pointCount;

      float mA = 0.0f;
      float iA = 0.0f;
      if (indexA == toiIndexA || indexA == toiIndexB) {
        mA = pc.invMassA;
        iA = pc.invIA;
      }

      float mB = 0f;
      float iB = 0f;
      if (indexB == toiIndexA || indexB == toiIndexB) {
        mB = pc.invMassB;
        iB = pc.invIB;
      }

      Vec2 cA = m_positions[indexA].c;
      float aA = m_positions[indexA].a;

      Vec2 cB = m_positions[indexB].c;
      float aB = m_positions[indexB].a;

      // Solve normal constraints
      for (int j = 0; j < pointCount; ++j) {
        final Rot xfAq = xfA.q;
        final Rot xfBq = xfB.q;
        xfAq.set(aA);
        xfBq.set(aB);
        xfA.p.x = cA.x - xfAq.c * localCenterAx + xfAq.s * localCenterAy;
        xfA.p.y = cA.y - xfAq.s * localCenterAx - xfAq.c * localCenterAy;
        xfB.p.x = cB.x - xfBq.c * localCenterBx + xfBq.s * localCenterBy;
        xfB.p.y = cB.y - xfBq.s * localCenterBx - xfBq.c * localCenterBy;

        final PositionSolverManifold psm = psolver;
        psm.initialize(pc, xfA, xfB, j);
        Vec2 normal = psm.normal;

        Vec2 point = psm.point;
        float separation = psm.separation;

        float rAx = point.x - cA.x;
        float rAy = point.y - cA.y;
        float rBx = point.x - cB.x;
        float rBy = point.y - cB.y;

        // Track max constraint error.
        minSeparation = MathUtils.min(minSeparation, separation);

        // Prevent large corrections and allow slop.
        float C =
            MathUtils.clamp(Settings.toiBaugarte * (separation + Settings.linearSlop),
                -Settings.maxLinearCorrection, 0.0f);

        // Compute the effective mass.
        float rnA = rAx * normal.y - rAy * normal.x;
        float rnB = rBx * normal.y - rBy * normal.x;
        float K = mA + mB + iA * rnA * rnA + iB * rnB * rnB;

        // Compute normal impulse
        float impulse = K > 0.0f ? -C / K : 0.0f;

        float Px = normal.x * impulse;
        float Py = normal.y * impulse;
        
        cA.x -= Px * mA;
        cA.y -= Py * mA;
        aA -= iA * (rAx * Py - rAy * Px);

        cB.x += Px * mB;
        cB.y += Py * mB;
        aB += iB * (rBx * Py - rBy * Px);
      }

      // m_positions[indexA].c.set(cA);
      m_positions[indexA].a = aA;

      // m_positions[indexB].c.set(cB);
      m_positions[indexB].a = aB;
    }

    // We can't expect minSpeparation >= -_linearSlop because we don't
    // push the separation above -_linearSlop.
    return minSeparation >= -1.5f * Settings.linearSlop;
  }

  public static class ContactSolverDef {
    public TimeStep step;
    public Contact[] contacts;
    public int count;
    public Position[] positions;
    public Velocity[] velocities;
  }
}


class PositionSolverManifold {

  public final Vec2 normal = new Vec2();
  public final Vec2 point = new Vec2();
  public float separation;

  public void initialize(ContactPositionConstraint pc, Transform xfA, Transform xfB, int index) {
    assert (pc.pointCount > 0);

    final Rot xfAq = xfA.q;
    final Rot xfBq = xfB.q;
    final Vec2 pcLocalPointsI = pc.localPoints[index];
    switch (pc.type) {
      case CIRCLES: {
        // Transform.mulToOutUnsafe(xfA, pc.localPoint, pointA);
        // Transform.mulToOutUnsafe(xfB, pc.localPoints[0], pointB);
        // normal.set(pointB).subLocal(pointA);
        // normal.normalize();
        //
        // point.set(pointA).addLocal(pointB).mulLocal(.5f);
        // temp.set(pointB).subLocal(pointA);
        // separation = Vec2.dot(temp, normal) - pc.radiusA - pc.radiusB;
        final Vec2 plocalPoint = pc.localPoint;
        final Vec2 pLocalPoints0 = pc.localPoints[0];
        final float pointAx = (xfAq.c * plocalPoint.x - xfAq.s * plocalPoint.y) + xfA.p.x;
        final float pointAy = (xfAq.s * plocalPoint.x + xfAq.c * plocalPoint.y) + xfA.p.y;
        final float pointBx = (xfBq.c * pLocalPoints0.x - xfBq.s * pLocalPoints0.y) + xfB.p.x;
        final float pointBy = (xfBq.s * pLocalPoints0.x + xfBq.c * pLocalPoints0.y) + xfB.p.y;
        normal.x = pointBx - pointAx;
        normal.y = pointBy - pointAy;
        normal.normalize();

        point.x = (pointAx + pointBx) * .5f;
        point.y = (pointAy + pointBy) * .5f;
        final float tempx = pointBx - pointAx;
        final float tempy = pointBy - pointAy;
        separation = tempx * normal.x + tempy * normal.y - pc.radiusA - pc.radiusB;
        break;
      }

      case FACE_A: {
        // Rot.mulToOutUnsafe(xfAq, pc.localNormal, normal);
        // Transform.mulToOutUnsafe(xfA, pc.localPoint, planePoint);
        //
        // Transform.mulToOutUnsafe(xfB, pc.localPoints[index], clipPoint);
        // temp.set(clipPoint).subLocal(planePoint);
        // separation = Vec2.dot(temp, normal) - pc.radiusA - pc.radiusB;
        // point.set(clipPoint);
        final Vec2 pcLocalNormal = pc.localNormal;
        final Vec2 pcLocalPoint = pc.localPoint;
        normal.x = xfAq.c * pcLocalNormal.x - xfAq.s * pcLocalNormal.y;
        normal.y = xfAq.s * pcLocalNormal.x + xfAq.c * pcLocalNormal.y;
        final float planePointx = (xfAq.c * pcLocalPoint.x - xfAq.s * pcLocalPoint.y) + xfA.p.x;
        final float planePointy = (xfAq.s * pcLocalPoint.x + xfAq.c * pcLocalPoint.y) + xfA.p.y;

        final float clipPointx = (xfBq.c * pcLocalPointsI.x - xfBq.s * pcLocalPointsI.y) + xfB.p.x;
        final float clipPointy = (xfBq.s * pcLocalPointsI.x + xfBq.c * pcLocalPointsI.y) + xfB.p.y;
        final float tempx = clipPointx - planePointx;
        final float tempy = clipPointy - planePointy;
        separation = tempx * normal.x + tempy * normal.y - pc.radiusA - pc.radiusB;
        point.x = clipPointx;
        point.y = clipPointy;
        break;
      }

      case FACE_B: {
        // Rot.mulToOutUnsafe(xfBq, pc.localNormal, normal);
        // Transform.mulToOutUnsafe(xfB, pc.localPoint, planePoint);
        //
        // Transform.mulToOutUnsafe(xfA, pcLocalPointsI, clipPoint);
        // temp.set(clipPoint).subLocal(planePoint);
        // separation = Vec2.dot(temp, normal) - pc.radiusA - pc.radiusB;
        // point.set(clipPoint);
        //
        // // Ensure normal points from A to B
        // normal.negateLocal();
        final Vec2 pcLocalNormal = pc.localNormal;
        final Vec2 pcLocalPoint = pc.localPoint;
        normal.x = xfBq.c * pcLocalNormal.x - xfBq.s * pcLocalNormal.y;
        normal.y = xfBq.s * pcLocalNormal.x + xfBq.c * pcLocalNormal.y;
        final float planePointx = (xfBq.c * pcLocalPoint.x - xfBq.s * pcLocalPoint.y) + xfB.p.x;
        final float planePointy = (xfBq.s * pcLocalPoint.x + xfBq.c * pcLocalPoint.y) + xfB.p.y;

        final float clipPointx = (xfAq.c * pcLocalPointsI.x - xfAq.s * pcLocalPointsI.y) + xfA.p.x;
        final float clipPointy = (xfAq.s * pcLocalPointsI.x + xfAq.c * pcLocalPointsI.y) + xfA.p.y;
        final float tempx = clipPointx - planePointx;
        final float tempy = clipPointy - planePointy;
        separation = tempx * normal.x + tempy * normal.y - pc.radiusA - pc.radiusB;
        point.x = clipPointx;
        point.y = clipPointy;
        normal.x *= -1;
        normal.y *= -1;
      }
        break;
    }
  }
}
