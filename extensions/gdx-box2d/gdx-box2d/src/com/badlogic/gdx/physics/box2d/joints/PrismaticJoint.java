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

/** A prismatic joint. This joint provides one degree of freedom: translation along an axis fixed in body1. Relative rotation is
 * prevented. You can use a joint limit to restrict the range of motion and a joint motor to drive the motion or to model joint
 * friction. */
public class PrismaticJoint extends Joint {
	// @off
	/*JNI
#include <Box2D/Box2D.h>
	 */

	private final float[] tmp = new float[2];
	private final Vector2 localAnchorA = new Vector2();
	private final Vector2 localAnchorB = new Vector2();
	private final Vector2 localAxisA = new Vector2();

	public PrismaticJoint (World world, long addr) {
		super(world, addr);
	}

	public Vector2 getLocalAnchorA () {
		jniGetLocalAnchorA(addr, tmp);
		localAnchorA.set(tmp[0], tmp[1]);
		return localAnchorA;
	}

	private native void jniGetLocalAnchorA (long addr, float[] anchor); /*
		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		anchor[0] = joint->GetLocalAnchorA().x;
		anchor[1] = joint->GetLocalAnchorA().y;
	*/

	public Vector2 getLocalAnchorB () {
		jniGetLocalAnchorB(addr, tmp);
		localAnchorB.set(tmp[0], tmp[1]);
		return localAnchorB;
	}

	private native void jniGetLocalAnchorB (long addr, float[] anchor); /*
		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		anchor[0] = joint->GetLocalAnchorB().x;
		anchor[1] = joint->GetLocalAnchorB().y;
	*/

	public Vector2 getLocalAxisA () {
		jniGetLocalAxisA(addr, tmp);
		localAxisA.set(tmp[0], tmp[1]);
		return localAxisA;
	}

	private native void jniGetLocalAxisA (long addr, float[] anchor); /*
		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		anchor[0] = joint->GetLocalAxisA().x;
		anchor[1] = joint->GetLocalAxisA().y;
	*/

	/** Get the current joint translation, usually in meters. */
	public float getJointTranslation () {
		return jniGetJointTranslation(addr);
	}

	private native float jniGetJointTranslation (long addr); /*
		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->GetJointTranslation();
	*/

	/** Get the current joint translation speed, usually in meters per second. */
	public float getJointSpeed () {
		return jniGetJointSpeed(addr);
	}

	private native float jniGetJointSpeed (long addr); /*
		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->GetJointSpeed();
	*/

	/** Is the joint limit enabled? */
	public boolean isLimitEnabled () {
		return jniIsLimitEnabled(addr);
	}

	private native boolean jniIsLimitEnabled (long addr); /*
		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->IsLimitEnabled();
	*/

	/** Enable/disable the joint limit. */
	public void enableLimit (boolean flag) {
		jniEnableLimit(addr, flag);
	}

	private native void jniEnableLimit (long addr, boolean flag); /*
		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		joint->EnableLimit(flag);
	*/

	/** Get the lower joint limit, usually in meters. */
	public float getLowerLimit () {
		return jniGetLowerLimit(addr);
	}

	private native float jniGetLowerLimit (long addr); /*
		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->GetLowerLimit();
	*/

	/** Get the upper joint limit, usually in meters. */
	public float getUpperLimit () {
		return jniGetUpperLimit(addr);
	}

	private native float jniGetUpperLimit (long addr); /*
		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->GetUpperLimit();
	*/

	/** Set the joint limits, usually in meters. */
	public void setLimits (float lower, float upper) {
		jniSetLimits(addr, lower, upper);
	}

	private native void jniSetLimits (long addr, float lower, float upper); /*
		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		joint->SetLimits(lower, upper );
	*/

	/** Is the joint motor enabled? */
	public boolean isMotorEnabled () {
		return jniIsMotorEnabled(addr);
	}

	private native boolean jniIsMotorEnabled (long addr); /*
		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->IsMotorEnabled();
	*/

	/** Enable/disable the joint motor. */
	public void enableMotor (boolean flag) {
		jniEnableMotor(addr, flag);
	}

	private native void jniEnableMotor (long addr, boolean flag); /*
		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		joint->EnableMotor(flag);
	*/

	/** Set the motor speed, usually in meters per second. */
	public void setMotorSpeed (float speed) {
		jniSetMotorSpeed(addr, speed);
	}

	private native void jniSetMotorSpeed (long addr, float speed); /*
		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		joint->SetMotorSpeed(speed);
	*/

	/** Get the motor speed, usually in meters per second. */
	public float getMotorSpeed () {
		return jniGetMotorSpeed(addr);
	}

	private native float jniGetMotorSpeed (long addr); /*
		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->GetMotorSpeed();
	*/

	/** Set the maximum motor force, usually in N. */
	public void setMaxMotorForce (float force) {
		jniSetMaxMotorForce(addr, force);
	}

	private native void jniSetMaxMotorForce (long addr, float force); /*
		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		joint->SetMaxMotorForce(force);
	*/

	/** Get the current motor force given the inverse time step, usually in N. */
	public float getMotorForce (float invDt) {
		return jniGetMotorForce(addr, invDt);
	}

	private native float jniGetMotorForce (long addr, float invDt); /*
		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->GetMotorForce(invDt);
	*/

	/** Get the max motor force, usually in N. */
	public float getMaxMotorForce () {
		return jniGetMaxMotorForce(addr);
	}

	private native float jniGetMaxMotorForce (long addr); /*
		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->GetMaxMotorForce();
	*/

	/** Get the reference angle.  */
	public float getReferenceAngle () {
		return jniGetReferenceAngle(addr);
	}

	private native float jniGetReferenceAngle (long addr); /*
		b2PrismaticJoint* joint = (b2PrismaticJoint*)addr;
		return joint->GetReferenceAngle();
	*/

}
