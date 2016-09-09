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

/** A prismatic joint. This joint provides one degree of freedom: translation along an axis fixed in body1. Relative rotation is
 * prevented. You can use a joint limit to restrict the range of motion and a joint motor to drive the motion or to model joint
 * friction. */
public class PrismaticJoint extends Joint {
	org.jbox2d.dynamics.joints.PrismaticJoint joint;

	Vector2 localAnchorA = new Vector2();
	Vector2 localAnchorB = new Vector2();
	Vector2 localAxisA = new Vector2();

	public PrismaticJoint (World world, org.jbox2d.dynamics.joints.PrismaticJoint joint) {
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

	public Vector2 getLocalAxisA () {
		Vec2 localAxis = joint.getLocalAxisA();
		localAxisA.set(localAxis.x, localAxis.y);
		return localAxisA;
	}

	/** Get the current joint translation, usually in meters. */
	public float getJointTranslation () {
		return joint.getJointTranslation();
	}

	/** Get the current joint translation speed, usually in meters per second. */
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

	/** Get the lower joint limit, usually in meters. */
	public float getLowerLimit () {
		return joint.getLowerLimit();
	}

	/** Get the upper joint limit, usually in meters. */
	public float getUpperLimit () {
		return joint.getUpperLimit();
	}

	/** Set the joint limits, usually in meters. */
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

	/** Set the motor speed, usually in meters per second. */
	public void setMotorSpeed (float speed) {
		joint.setMotorSpeed(speed);
	}

	/** Get the motor speed, usually in meters per second. */
	public float getMotorSpeed () {
		return joint.getMotorSpeed();
	}

	/** Set the maximum motor force, usually in N. */
	public void setMaxMotorForce (float force) {
		joint.setMaxMotorForce(force);
	}

	/** Get the current motor force given the inverse time step, usually in N. */
	public float getMotorForce (float invDt) {
		return joint.getMotorForce(invDt);
	}

	/** Get the max motor force, usually in N. */
	public float getMaxMotorForce () {
		return joint.getMaxMotorForce();
	}

	/** Get the reference angle.  */
	public float getReferenceAngle () {
		return joint.getReferenceAngle();
	}

}
