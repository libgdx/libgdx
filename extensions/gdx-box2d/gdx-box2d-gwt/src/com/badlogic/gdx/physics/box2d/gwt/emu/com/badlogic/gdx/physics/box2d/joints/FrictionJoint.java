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

import org.jbox2d.common.Vec2;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;

/** Friction joint. This is used for top-down friction. It provides 2D translational friction and angular friction. */
public class FrictionJoint extends Joint {
	org.jbox2d.dynamics.joints.FrictionJoint joint;

	Vector2 localAnchorA = new Vector2();
	Vector2 localAnchorB = new Vector2();

	public FrictionJoint (World world, org.jbox2d.dynamics.joints.FrictionJoint joint) {
		super(world, joint);
		this.joint = joint;
	}

	public Vector2 getLocalAnchorA () {
		Vec2 localAnchor = joint.getLocalAnchorA();
		localAnchorA.set(localAnchor.x, localAnchor.y);
		return localAnchorA;
	}

	public Vector2 getLocalAnchorB () {
		Vec2 localAnchor = joint.getLocalAnchorB();
		localAnchorB.set(localAnchor.x, localAnchor.y);
		return localAnchorB;
	}

	/** Set the maximum friction force in N. */
	public void setMaxForce (float force) {
		joint.setMaxForce(force);
	}

	/** Get the maximum friction force in N. */
	public float getMaxForce () {
		return joint.getMaxForce();
	}

	/** Set the maximum friction torque in N*m. */
	public void setMaxTorque (float torque) {
		joint.setMaxTorque(torque);
	}

	/** Get the maximum friction torque in N*m. */
	public float getMaxTorque () {
		return joint.getMaxTorque();
	}
}
