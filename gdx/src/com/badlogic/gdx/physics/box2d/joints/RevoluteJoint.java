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

/** A revolute joint constrains two bodies to share a common point while they are free to rotate about the point. The relative
 * rotation about the shared point is the joint angle. You can limit the relative rotation with a joint limit that specifies a
 * lower and upper angle. You can use a motor to drive the relative rotation about the shared point. A maximum motor torque is
 * provided so that infinite forces are not generated. */
public class RevoluteJoint extends Joint {
	// @off
	/*JNI
#include <Box2D/Box2D.h> 
	 */
	
	public RevoluteJoint (World world, long addr) {
		super(world, addr);
	}

	/** Get the current joint angle in radians. */
	public float getJointAngle () {
		return jniGetJointAngle(addr);
	}

	private native float jniGetJointAngle (long addr); /*
		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetJointAngle();
	*/

	/** Get the current joint angle speed in radians per second. */
	public float getJointSpeed () {
		return jniGetJointSpeed(addr);
	}

	private native float jniGetJointSpeed (long addr); /*
		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetJointSpeed();
	*/

	/** Is the joint limit enabled? */
	public boolean isLimitEnabled () {
		return jniIsLimitEnabled(addr);
	}

	private native boolean jniIsLimitEnabled (long addr); /*
		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->IsLimitEnabled();
	*/

	/** Enable/disable the joint limit. */
	public void enableLimit (boolean flag) {
		jniEnableLimit(addr, flag);
	}

	private native void jniEnableLimit (long addr, boolean flag); /*
		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->EnableLimit(flag);
	*/

	/** Get the lower joint limit in radians. */
	public float getLowerLimit () {
		return jniGetLowerLimit(addr);
	}

	private native float jniGetLowerLimit (long addr); /*
		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetLowerLimit();
	*/

	/** Get the upper joint limit in radians. */
	public float getUpperLimit () {
		return jniGetUpperLimit(addr);
	}

	private native float jniGetUpperLimit (long addr); /*
		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetUpperLimit();
	*/

	/** Set the joint limits in radians.
	 * @param upper */
	public void setLimits (float lower, float upper) {
		jniSetLimits(addr, lower, upper);
	}

	private native void jniSetLimits (long addr, float lower, float upper); /*
		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->SetLimits(lower, upper );
	*/

	/** Is the joint motor enabled? */
	public boolean isMotorEnabled () {
		return jniIsMotorEnabled(addr);
	}

	private native boolean jniIsMotorEnabled (long addr); /*
		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->IsMotorEnabled();
	*/

	/** Enable/disable the joint motor. */
	public void enableMotor (boolean flag) {
		jniEnableMotor(addr, flag);
	}

	private native void jniEnableMotor (long addr, boolean flag); /*
		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->EnableMotor(flag);
	*/

	/** Set the motor speed in radians per second. */
	public void setMotorSpeed (float speed) {
		jniSetMotorSpeed(addr, speed);
	}

	private native void jniSetMotorSpeed (long addr, float speed); /*
		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->SetMotorSpeed(speed);
	*/

	/** Get the motor speed in radians per second. */
	public float getMotorSpeed () {
		return jniGetMotorSpeed(addr);
	}

	private native float jniGetMotorSpeed (long addr); /*
		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetMotorSpeed();
	*/

	/** Set the maximum motor torque, usually in N-m. */
	public void setMaxMotorTorque (float torque) {
		jniSetMaxMotorTorque(addr, torque);
	}

	private native void jniSetMaxMotorTorque (long addr, float torque); /*
		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		joint->SetMaxMotorTorque(torque);
	*/

	/** Get the current motor torque, usually in N-m. */
	public float getMotorTorque (float invDt) {
		return jniGetMotorTorque(addr, invDt);
	}

	private native float jniGetMotorTorque (long addr, float invDt); /*
		b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
		return joint->GetMotorTorque(invDt);
	*/
}
