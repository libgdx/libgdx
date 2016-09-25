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
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;

/** A revolute joint constrains two bodies to share a common point while they are free to rotate about the point. The relative
 * rotation about the shared point is the joint angle. You can limit the relative rotation with a joint limit that specifies a
 * lower and upper angle. You can use a motor to drive the relative rotation about the shared point. A maximum motor torque is
 * provided so that infinite forces are not generated. */
public class RevoluteJoint extends Joint {
	org.jbox2d.dynamics.joints.RevoluteJoint joint;
	private final Vector2 localAnchorA = new Vector2();
	private final Vector2 localAnchorB = new Vector2();

	public RevoluteJoint (World world, org.jbox2d.dynamics.joints.RevoluteJoint joint) {
		super(world, joint);
		this.joint = joint;
	}

	/** Get the current joint angle in radians. */
	public float getJointAngle () {
		return joint.getJointAngle();
	}

	/** Get the current joint angle speed in radians per second. */
	public float getJointSpeed () {
		return joint.getJointSpeed();
	}

	/** Is the joint limit enabled? */
	public boolean isLimitEnabled () {
		return joint.isLimitEnabled();
	}

	/** Enable/disable the joint limit. */
	public void enableLimit (boolean flag) {
		joint.enableLimit(flag);
	}

	/** Get the lower joint limit in radians. */
	public float getLowerLimit () {
		return joint.getLowerLimit();
	}

	/** Get the upper joint limit in radians. */
	public float getUpperLimit () {
		return joint.getUpperLimit();
	}

	/** Set the joint limits in radians.
	 * @param upper */
	public void setLimits (float lower, float upper) {
		joint.setLimits(lower, upper);
	}

	/** Is the joint motor enabled? */
	public boolean isMotorEnabled () {
		return joint.isMotorEnabled();
	}

	/** Enable/disable the joint motor. */
	public void enableMotor (boolean flag) {
		joint.enableMotor(flag);
	}

	float motorSpeed = 0;

	/** Set the motor speed in radians per second. */
	public void setMotorSpeed (float speed) {
		motorSpeed = speed;
		joint.setMotorSpeed(speed);
	}

	/** Get the motor speed in radians per second. */
	public float getMotorSpeed () {
		return motorSpeed;
	}

	/** Set the maximum motor torque, usually in N-m. */
	public void setMaxMotorTorque (float torque) {
		joint.setMaxMotorTorque(torque);
	}

	/** Get the current motor torque, usually in N-m. */
	public float getMotorTorque (float invDt) {
		return joint.getMotorTorque(invDt);
	}

	public Vector2 getLocalAnchorA () {
		return localAnchorA.set(joint.getLocalAnchorA().x, joint.getLocalAnchorA().y);
	}

	public Vector2 getLocalAnchorB () {
		return localAnchorB.set(joint.getLocalAnchorB().x, joint.getLocalAnchorB().y);
	}

	/** Get the current motor torque, usually in N-m. */
	public float getReferenceAngle () {
		return joint.getReferenceAngle();
	}

	public float getMaxMotorTorque () {
		return joint.getMaxMotorTorque();
	}
}
