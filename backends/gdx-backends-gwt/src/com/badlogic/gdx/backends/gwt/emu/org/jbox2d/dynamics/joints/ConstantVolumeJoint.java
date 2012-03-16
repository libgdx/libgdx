/*******************************************************************************
 * Copyright (c) 2011, Daniel Murphy
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.SolverData;
import org.jbox2d.dynamics.TimeStep;
import org.jbox2d.dynamics.World;

// TODO(dmurph): clean this up a bit, add docs
public class ConstantVolumeJoint extends Joint {

  public final Body[] bodies;
  float[] targetLengths;
  public float targetVolume;
  // float relaxationFactor;//1.0 is perfectly stiff (but doesn't work, unstable)

  Vec2[] normals;

  TimeStep m_step;
  private float m_impulse = 0.0f;

  private World world;

  DistanceJoint[] distanceJoints;

  public final float frequencyHz;
  public final float dampingRatio;

  public Body[] getBodies() {
    return bodies;
  }

  public DistanceJoint[] getJoints() {
    return distanceJoints;
  }

  public void inflate(float factor) {
    targetVolume *= factor;
  }

  public ConstantVolumeJoint(World argWorld, ConstantVolumeJointDef def) {
    super(argWorld.getPool(), def);
    world = argWorld;
    if (def.bodies.size() <= 2) {
      throw new IllegalArgumentException(
          "You cannot create a constant volume joint with less than three bodies.");
    }
    bodies = def.bodies.toArray(new Body[0]);

    targetLengths = new float[bodies.length];
    for (int i = 0; i < targetLengths.length; ++i) {
      final int next = (i == targetLengths.length - 1) ? 0 : i + 1;
      float dist = bodies[i].getWorldCenter().sub(bodies[next].getWorldCenter()).length();
      targetLengths[i] = dist;
    }
    targetVolume = getArea();

    if (def.joints != null && def.joints.size() != def.bodies.size()) {
      throw new IllegalArgumentException(
          "Incorrect joint definition.  Joints have to correspond to the bodies");
    }
    if (def.joints == null) {
      final DistanceJointDef djd = new DistanceJointDef();
      distanceJoints = new DistanceJoint[bodies.length];
      for (int i = 0; i < targetLengths.length; ++i) {
        final int next = (i == targetLengths.length - 1) ? 0 : i + 1;
        djd.frequencyHz = def.frequencyHz;// 20.0f;
        djd.dampingRatio = def.dampingRatio;// 50.0f;
        djd.initialize(bodies[i], bodies[next], bodies[i].getWorldCenter(),
            bodies[next].getWorldCenter());
        distanceJoints[i] = (DistanceJoint) world.createJoint(djd);
      }
    } else {
      distanceJoints = def.joints.toArray(new DistanceJoint[0]);
    }

    frequencyHz = def.frequencyHz;
    dampingRatio = def.dampingRatio;

    normals = new Vec2[bodies.length];
    for (int i = 0; i < normals.length; ++i) {
      normals[i] = new Vec2();
    }

    this.m_bodyA = bodies[0];
    this.m_bodyB = bodies[1];
    this.m_collideConnected = false;
  }

  @Override
  public void destructor() {
    for (int i = 0; i < distanceJoints.length; ++i) {
      world.destroyJoint(distanceJoints[i]);
    }
  }

  private float getArea() {
    float area = 0.0f;
    // i'm glad i changed these all to member access
    area += bodies[bodies.length - 1].getWorldCenter().x * bodies[0].getWorldCenter().y
        - bodies[0].getWorldCenter().x * bodies[bodies.length - 1].getWorldCenter().y;
    for (int i = 0; i < bodies.length - 1; ++i) {
      area += bodies[i].getWorldCenter().x * bodies[i + 1].getWorldCenter().y
          - bodies[i + 1].getWorldCenter().x * bodies[i].getWorldCenter().y;
    }
    area *= .5f;
    return area;
  }

  /**
   * Apply the position correction to the particles.
   * 
   * @param step
   */
  public boolean constrainEdges(final TimeStep step) {
    float perimeter = 0.0f;
    for (int i = 0; i < bodies.length; ++i) {
      final int next = (i == bodies.length - 1) ? 0 : i + 1;
      float dx = bodies[next].getWorldCenter().x - bodies[i].getWorldCenter().x;
      float dy = bodies[next].getWorldCenter().y - bodies[i].getWorldCenter().y;
      float dist = MathUtils.sqrt(dx * dx + dy * dy);
      if (dist < Settings.EPSILON) {
        dist = 1.0f;
      }
      normals[i].x = dy / dist;
      normals[i].y = -dx / dist;
      perimeter += dist;
    }

    final Vec2 delta = pool.popVec2();

    float deltaArea = targetVolume - getArea();
    float toExtrude = 0.5f * deltaArea / perimeter; // *relaxationFactor
    // float sumdeltax = 0.0f;
    boolean done = true;
    for (int i = 0; i < bodies.length; ++i) {
      final int next = (i == bodies.length - 1) ? 0 : i + 1;
      delta.set(toExtrude * (normals[i].x + normals[next].x), toExtrude
          * (normals[i].y + normals[next].y));
      // sumdeltax += dx;
      float norm = delta.length();
      if (norm > Settings.maxLinearCorrection) {
        delta.mulLocal(Settings.maxLinearCorrection / norm);
      }
      if (norm > Settings.linearSlop) {
        done = false;
      }
      bodies[next].m_sweep.c.x += delta.x;
      bodies[next].m_sweep.c.y += delta.y;
      bodies[next].synchronizeTransform();
      // bodies[next].m_linearVelocity.x += delta.x * step.inv_dt;
      // bodies[next].m_linearVelocity.y += delta.y * step.inv_dt;
    }

    pool.pushVec2(1);
    // System.out.println(sumdeltax);
    return done;
  }

  @Override
  public void initVelocityConstraints(final SolverData data) {

    final Vec2[] d = pool.getVec2Array(bodies.length);

    for (int i = 0; i < bodies.length; ++i) {
      final int prev = (i == 0) ? bodies.length - 1 : i - 1;
      final int next = (i == bodies.length - 1) ? 0 : i + 1;
      d[i].set(bodies[next].getWorldCenter());
      d[i].subLocal(bodies[prev].getWorldCenter());
    }

    if (data.step.warmStarting) {
      m_impulse *= data.step.dtRatio;
      // float lambda = -2.0f * crossMassSum / dotMassSum;
      // System.out.println(crossMassSum + " " +dotMassSum);
      // lambda = MathUtils.clamp(lambda, -Settings.maxLinearCorrection,
      // Settings.maxLinearCorrection);
      // m_impulse = lambda;
      for (int i = 0; i < bodies.length; ++i) {
        bodies[i].m_linearVelocity.x += bodies[i].m_invMass * d[i].y * .5f * m_impulse;
        bodies[i].m_linearVelocity.y += bodies[i].m_invMass * -d[i].x * .5f * m_impulse;
      }
    } else {
      m_impulse = 0.0f;
    }
  }

  @Override
  public boolean solvePositionConstraints(final SolverData data) {
    return constrainEdges(data.step);
  }

  @Override
  public void solveVelocityConstraints(final SolverData data) {
    float crossMassSum = 0.0f;
    float dotMassSum = 0.0f;

    final Vec2 d[] = pool.getVec2Array(bodies.length);

    for (int i = 0; i < bodies.length; ++i) {
      final int prev = (i == 0) ? bodies.length - 1 : i - 1;
      final int next = (i == bodies.length - 1) ? 0 : i + 1;
      d[i].set(bodies[next].getWorldCenter());
      d[i].subLocal(bodies[prev].getWorldCenter());
      dotMassSum += (d[i].lengthSquared()) / bodies[i].getMass();
      crossMassSum += Vec2.cross(bodies[i].getLinearVelocity(), d[i]);
    }
    float lambda = -2.0f * crossMassSum / dotMassSum;
    // System.out.println(crossMassSum + " " +dotMassSum);
    // lambda = MathUtils.clamp(lambda, -Settings.maxLinearCorrection,
    // Settings.maxLinearCorrection);
    m_impulse += lambda;
    // System.out.println(m_impulse);
    for (int i = 0; i < bodies.length; ++i) {
      bodies[i].m_linearVelocity.x += bodies[i].m_invMass * d[i].y * .5f * lambda;
      bodies[i].m_linearVelocity.y += bodies[i].m_invMass * -d[i].x * .5f * lambda;
    }
  }

  @Override
  public void getAnchorA(Vec2 argOut) {
  }

  @Override
  public void getAnchorB(Vec2 argOut) {
  }

  @Override
  public void getReactionForce(float inv_dt, Vec2 argOut) {
  }

  @Override
  public float getReactionTorque(float inv_dt) {
    return 0;
  }

}
