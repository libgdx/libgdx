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

/** A wheel joint. This joint provides two degrees of freedom: translation along an axis fixed in body1 and rotation in the plane.
 * You can use a joint limit to restrict the range of motion and a joint motor to drive the rotation or to model rotational
 * friction. This joint is designed for vehicle suspensions. */
public class WheelJoint extends Joint {
	// @off
	/*JNI
#include <box2d/box2d.h>
	 */ // @on

	private final float[] tmp = new float[2];
	private final Vector2 localAnchorA = new Vector2();
	private final Vector2 localAnchorB = new Vector2();
	private final Vector2 localAxisA = new Vector2();

	public WheelJoint (World world, long addr) {
		super(world, addr);
	}

	public Vector2 getLocalAnchorA () {
		jniGetLocalAnchorA(addr, tmp);
		localAnchorA.set(tmp[0], tmp[1]);
		return localAnchorA;
	}

	private native void jniGetLocalAnchorA (long addr, float[] anchor); /*
		// @off
		b2WheelJoint* joint = (b2WheelJoint*)addr;
		anchor[0] = joint->GetLocalAnchorA().x;
		anchor[1] = joint->GetLocalAnchorA().y;
	*/ // @on

	public Vector2 getLocalAnchorB () {
		jniGetLocalAnchorB(addr, tmp);
		localAnchorB.set(tmp[0], tmp[1]);
		return localAnchorB;
	}

	private native void jniGetLocalAnchorB (long addr, float[] anchor); /*
		// @off
		b2WheelJoint* joint = (b2WheelJoint*)addr;
		anchor[0] = joint->GetLocalAnchorB().x;
		anchor[1] = joint->GetLocalAnchorB().y;
	*/ // @on

	public Vector2 getLocalAxisA () {
		jniGetLocalAxisA(addr, tmp);
		localAxisA.set(tmp[0], tmp[1]);
		return localAxisA;
	}

	private native void jniGetLocalAxisA (long addr, float[] anchor); /*
		// @off
		b2WheelJoint* joint = (b2WheelJoint*)addr;
		anchor[0] = joint->GetLocalAxisA().x;
		anchor[1] = joint->GetLocalAxisA().y;
	*/ // @on

	/** Get the current joint translation, usually in meters. */
	public float getJointTranslation () {
		return jniGetJointTranslation(addr);
	}

	private native float jniGetJointTranslation (long addr); /*
		// @off
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->GetJointTranslation();
	*/ // @on

	/** Get the current joint translation speed, usually in meters per second. */
	public float getJointAngularSpeed () {
		return jniGetJointAngularSpeed(addr);
	}

	private native float jniGetJointAngularSpeed (long addr); /*
		// @off
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->GetJointAngularSpeed();
	*/ // @on

	/** Is the joint motor enabled? */
	public boolean isLimitEnabled () {
		return jniIsLimitEnabled(addr);
	}

	private native boolean jniIsLimitEnabled (long addr); /*
		// @off
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->IsLimitEnabled();
	*/ // @on

	/** Enable/disable the joint limit. */
	public void enableLimit (boolean flag) {
		jniEnableLimit(addr, flag);
	}

	private native void jniEnableLimit (long addr, boolean flag); /*
		// @off
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		joint->EnableLimit(flag);
	*/ // @on

	/** Get the lower joint translation limit, usually in meters. */
	public float getLowerLimit () {
		return jniGetLowerLimit(addr);
	}

	private native float jniGetLowerLimit (long addr); /*
		// @off
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->GetLowerLimit();
	*/ // @on

	/** Get the upper joint translation limit, usually in meters. */
	public float GetUpperLimit () {
		return jniGetUpperLimit(addr);
	}

	private native float jniGetUpperLimit (long addr); /*
		// @off
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->GetUpperLimit();
	*/ // @on

	/** Set the joint translation limits, usually in meters. */
	public void setLimits (float lower, float upper) {
		jniSetLimits(addr, lower, upper);
	}

	private native void jniSetLimits (long addr, float lower, float upper); /*
		// @off
		b2WheelJoint* joint = (b2WheelJoint*)addr;
		joint->SetLimits(lower, upper);
	*/ // @on

	/** Is the joint motor enabled? */
	public boolean isMotorEnabled () {
		return jniIsMotorEnabled(addr);
	}

	private native boolean jniIsMotorEnabled (long addr); /*
		// @off
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->IsMotorEnabled();
	*/ // @on

	/** Enable/disable the joint motor. */
	public void enableMotor (boolean flag) {
		jniEnableMotor(addr, flag);
	}

	private native void jniEnableMotor (long addr, boolean flag); /*
		// @off
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		joint->EnableMotor(flag);
	*/ // @on

	/** Set the motor speed, usually in radians per second. */
	public void setMotorSpeed (float speed) {
		jniSetMotorSpeed(addr, speed);
	}

	private native void jniSetMotorSpeed (long addr, float speed); /*
		// @off
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		joint->SetMotorSpeed(speed);
	*/ // @on

	/** Get the motor speed, usually in radians per second. */
	public float getMotorSpeed () {
		return jniGetMotorSpeed(addr);
	}

	private native float jniGetMotorSpeed (long addr); /*
		// @off
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->GetMotorSpeed();
	*/ // @on

	/** Set/Get the maximum motor force, usually in N-m. */
	public void setMaxMotorTorque (float torque) {
		jniSetMaxMotorTorque(addr, torque);
	}

	private native void jniSetMaxMotorTorque (long addr, float torque); /*
		// @off
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		joint->SetMaxMotorTorque(torque);
	*/ // @on

	public float getMaxMotorTorque () {
		return jniGetMaxMotorTorque(addr);
	}

	private native float jniGetMaxMotorTorque (long addr); /*
		// @off
		b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->GetMaxMotorTorque();
	*/ // @on

	/** Get the current motor torque given the inverse time step, usually in N-m. */
	public float getMotorTorque (float invDt) {
		return jniGetMotorTorque(addr, invDt);
	}

	private native float jniGetMotorTorque (long addr, float invDt); /*
		// @off
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->GetMotorTorque(invDt);
	*/ // @on

	/** Set/Get spring stiffness. */
	public void setStiffness (float stiffness) {
		jniSetStiffness(addr, stiffness);
	}

	private native void jniSetStiffness (long addr, float stiffness); /*
		// @off
		b2WheelJoint* joint = (b2WheelJoint*)addr;
		joint->SetStiffness(stiffness);
	*/ // @on

	public float getSpringFrequencyHz () {
		return jniGetStiffness(addr);
	}

	private native float jniGetStiffness (long addr); /*
		// @off
		b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->GetStiffness();
	*/ // @on

	/** Set/Get damping. */
	public void setSpringDamping (float ratio) {
		jniSetDamping(addr, ratio);
	}

	private native void jniSetDamping (long addr, float ratio); /*
		// @off
		b2WheelJoint* joint = (b2WheelJoint*)addr;
		joint->SetDamping(ratio);
	*/ // @on

	public float getDamping () {
		return jniGetDamping(addr);
	}

	private native float jniGetDamping (long addr); /*
		// @off
		b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->GetDamping();
	*/ // @on

}
