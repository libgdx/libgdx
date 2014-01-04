/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.physics.box2d.joints;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.JointDef;

/** Friction joint definition. */
public class FrictionJointDef extends JointDef {

	public FrictionJointDef () {
		type = JointType.FrictionJoint;
	}

	/** Initialize the bodies, anchors, axis, and reference angle using the world anchor and world axis. */
	public void initialize (Body bodyA, Body bodyB, Vector2 anchor) {
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		localAnchorA.set(bodyA.getLocalPoint(anchor));
		localAnchorB.set(bodyB.getLocalPoint(anchor));
	}

	/** The local anchor point relative to bodyA's origin. */
	public final Vector2 localAnchorA = new Vector2();

	/** The local anchor point relative to bodyB's origin. */
	public final Vector2 localAnchorB = new Vector2();

	/** The maximum friction force in N. */
	public float maxForce = 0;

	/** The maximum friction torque in N-m. */
	public float maxTorque = 0;

	@Override
	public org.jbox2d.dynamics.joints.JointDef toJBox2d () {
		org.jbox2d.dynamics.joints.FrictionJointDef jd = new org.jbox2d.dynamics.joints.FrictionJointDef();
		jd.bodyA = bodyA.body;
		jd.bodyB = bodyB.body;
		jd.collideConnected = collideConnected;
		jd.localAnchorA.set(localAnchorA.x, localAnchorA.y);
		jd.localAnchorB.set(localAnchorB.x, localAnchorB.y);
		jd.maxForce = maxForce;
		jd.maxTorque = maxTorque;
		jd.type = org.jbox2d.dynamics.joints.JointType.FRICTION;
		return jd;
	}
}
