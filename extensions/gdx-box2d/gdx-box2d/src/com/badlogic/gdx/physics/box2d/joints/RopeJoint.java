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

/** A rope joint enforces a maximum distance between two points on two bodies. It has no other effect. Warning: if you attempt to
 * change the maximum length during the simulation you will get some non-physical behavior. A model that would allow you to
 * dynamically modify the length would have some sponginess, so I chose not to implement it that way. See b2DistanceJoint if you
 * want to dynamically control length. */
public class RopeJoint extends Joint {
	// @off
	/*JNI
#include <Box2D/Box2D.h>
	 */

	private final float[] tmp = new float[2];
	private final Vector2 localAnchorA = new Vector2();
	private final Vector2 localAnchorB = new Vector2();

	public RopeJoint (World world, long addr) {
		super(world, addr);
	}

	public Vector2 getLocalAnchorA () {
		jniGetLocalAnchorA(addr, tmp);
		localAnchorA.set(tmp[0], tmp[1]);
		return localAnchorA;
	}

	private native void jniGetLocalAnchorA (long addr, float[] anchor); /*
		b2RopeJoint* joint = (b2RopeJoint*)addr;
		anchor[0] = joint->GetLocalAnchorA().x;
		anchor[1] = joint->GetLocalAnchorA().y;
	*/

	public Vector2 getLocalAnchorB () {
		jniGetLocalAnchorB(addr, tmp);
		localAnchorB.set(tmp[0], tmp[1]);
		return localAnchorB;
	}

	private native void jniGetLocalAnchorB (long addr, float[] anchor); /*
		b2RopeJoint* joint = (b2RopeJoint*)addr;
		anchor[0] = joint->GetLocalAnchorB().x;
		anchor[1] = joint->GetLocalAnchorB().y;
	*/

	/** Get the maximum length of the rope. */
	public float getMaxLength () {
		return jniGetMaxLength(addr);
	}

	private native float jniGetMaxLength (long addr); /*
		b2RopeJoint* rope = (b2RopeJoint*)addr;
		return rope->GetMaxLength();
	*/

	/** Set the maximum length of the rope. */
	public void setMaxLength (float length) {
		jniSetMaxLength(addr, length);
	}

	private native float jniSetMaxLength (long addr, float length); /*
		b2RopeJoint* rope = (b2RopeJoint*)addr;
		rope->SetMaxLength(length);
	*/
}
