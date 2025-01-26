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

/** A weld joint essentially glues two bodies together. A weld joint may distort somewhat because the island constraint solver is
 * approximate. */
public class WeldJoint extends Joint {
	// @off
	/*JNI
		#include <box2d/box2d.h>
	 */ // @on

	private final float[] tmp = new float[2];
	private final Vector2 localAnchorA = new Vector2();
	private final Vector2 localAnchorB = new Vector2();

	public WeldJoint (World world, long addr) {
		super(world, addr);
	}

	public Vector2 getLocalAnchorA () {
		jniGetLocalAnchorA(addr, tmp);
		localAnchorA.set(tmp[0], tmp[1]);
		return localAnchorA;
	}

	private native void jniGetLocalAnchorA (long addr, float[] anchor); /*
		// @off
		b2WeldJoint* joint = (b2WeldJoint*)addr;
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
		b2WeldJoint* joint = (b2WeldJoint*)addr;
		anchor[0] = joint->GetLocalAnchorB().x;
		anchor[1] = joint->GetLocalAnchorB().y;
	*/ // @on

	public float getReferenceAngle () {
		return jniGetReferenceAngle(addr);
	}

	private native float jniGetReferenceAngle (long addr); /*
		// @off
		b2WeldJoint* joint = (b2WeldJoint*)addr;
		return joint->GetReferenceAngle();
	*/ // @on

	/** Set/get stiffness. */
	public void setStiffness (float stiffness) {
		jniSetStiffness(addr, stiffness);
	}

	private native void jniSetStiffness (long addr, float stiffness); /*
		// @off
		b2WeldJoint* joint = (b2WeldJoint*)addr;
		joint->SetStiffness( stiffness );
	*/ // @on

	/** Set/get stiffness. */
	public float getStiffness () {
		return jniGetStiffness(addr);
	}

	private native float jniGetStiffness (long addr); /*
		// @off
		b2WeldJoint* joint = (b2WeldJoint*)addr;
		return joint->GetStiffness();
	*/ // @on

	public float getDamping () {
		return jniGetDamping(addr);
	}

	private native float jniGetDamping (long addr); /*
		// @off
		b2WeldJoint* joint = (b2WeldJoint*)addr;
		return joint->GetDamping();
	*/ // @on

	public void setDamping (float ratio) {
		jniSetDamping(addr, ratio);
	}

	private native void jniSetDamping (long addr, float damping); /*
		// @off
		b2WeldJoint* joint = (b2WeldJoint*)addr;
		joint->SetDamping(damping);
	*/ // @on

}
