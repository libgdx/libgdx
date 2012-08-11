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

import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;

/** A wheel joint. This joint provides two degrees of freedom: translation along an axis fixed in body1 and rotation in the plane.
 * You can use a joint limit to restrict the range of motion and a joint motor to drive the rotation or to model rotational
 * friction. This joint is designed for vehicle suspensions. */
public class WheelJoint extends Joint {
	// FIXME not implemented by jbox2d

	public WheelJoint (World world) {
		super(world, null);
	}

	/** Get the current joint translation, usually in meters. */
	public float getJointTranslation () {
		return 0; // FIXMe
	}

	/** Get the current joint translation speed, usually in meters per second. */
	public float getJointSpeed () {
		return 0; // FIXME
	}

	/** Is the joint motor enabled? */
	private boolean isMotorEnabled () {
		return false; // FIXME
	}

	/** Enable/disable the joint motor. */
	public void enableMotor (boolean flag) {
		// FIXME
	}

	/** Set the motor speed, usually in radians per second. */
	public void setMotorSpeed (float speed) {
		// FIXME
	}

	/** Get the motor speed, usually in radians per second. */
	public float getMotorSpeed () {
		return 0; // FIXME
	}

	/** Set/Get the maximum motor force, usually in N-m. */
	public void setMaxMotorTorque (float torque) {
		// FIXME
	}

	public float getMaxMotorTorque () {
		return 0; // FIXME
	}

	/** Get the current motor torque given the inverse time step, usually in N-m. */
	public float getMotorTorque (float invDt) {
		return 0; // FIXME
	}

	/** Set/Get the spring frequency in hertz. Setting the frequency to zero disables the spring. */
	public void setSpringFrequencyHz (float hz) {
		// FIXME
	}

	public float getSpringFrequencyHz () {
		return 0; // FIXME
	}

	/** Set/Get the spring damping ratio */
	public void setSpringDampingRatio (float ratio) {
		// FIXME
	}

	public float getSpringDampingRatio () {
		return 0; // FIXME
	}
}
