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

/** Revolute joint definition. This requires defining an anchor point where the bodies are joined. The definition uses local anchor
 * points so that the initial configuration can violate the constraint slightly. You also need to specify the initial relative
 * angle for joint limits. This helps when saving and loading a game. The local anchor points are measured from the body's origin
 * rather than the center of mass because: 1. you might not know where the center of mass will be. 2. if you add/remove shapes
 * from a body and recompute the mass, the joints will be broken. */
public class RevoluteJointDef extends JointDef {
	public RevoluteJointDef () {
		type = JointType.RevoluteJoint;
	}

	/** Initialize the bodies, anchors, and reference angle using a world anchor point. */
	public void initialize (Body bodyA, Body bodyB, Vector2 anchor) {
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		localAnchorA.set(bodyA.getLocalPoint(anchor));
		localAnchorB.set(bodyB.getLocalPoint(anchor));
		referenceAngle = bodyB.getAngle() - bodyA.getAngle();
	}

	/** The local anchor point relative to body1's origin. */
	public final Vector2 localAnchorA = new Vector2();

	/** The local anchor point relative to body2's origin. */
	public final Vector2 localAnchorB = new Vector2();;

	/** The body2 angle minus body1 angle in the reference state (radians). */
	public float referenceAngle = 0;

	/** A flag to enable joint limits. */
	public boolean enableLimit = false;

	/** The lower angle for the joint limit (radians). */
	public float lowerAngle = 0;

	/** The upper angle for the joint limit (radians). */
	public float upperAngle = 0;

	/** A flag to enable the joint motor. */
	public boolean enableMotor = false;

	/** The desired motor speed. Usually in radians per second. */
	public float motorSpeed = 0;

	/** The maximum motor torque used to achieve the desired motor speed. Usually in N-m. */
	public float maxMotorTorque = 0;
}
