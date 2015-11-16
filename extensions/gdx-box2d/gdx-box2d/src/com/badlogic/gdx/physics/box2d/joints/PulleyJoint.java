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

/** The pulley joint is connected to two bodies and two fixed ground points. The pulley supports a ratio such that: length1 + ratio
 * * length2 <= constant Yes, the force transmitted is scaled by the ratio. The pulley also enforces a maximum length limit on
 * both sides. This is useful to prevent one side of the pulley hitting the top. */
public class PulleyJoint extends Joint {
	// @off
	/*JNI
#include <Box2D/Box2D.h> 
	 */
	
	public PulleyJoint (World world, long addr) {
		super(world, addr);
	}

	/** Get the first ground anchor. */
	private final float[] tmp = new float[2];
	private final Vector2 groundAnchorA = new Vector2();

	public Vector2 getGroundAnchorA () {
		jniGetGroundAnchorA(addr, tmp);
		groundAnchorA.set(tmp[0], tmp[1]);
		return groundAnchorA;
	}

	private native void jniGetGroundAnchorA (long addr, float[] anchor); /*
		b2PulleyJoint* joint = (b2PulleyJoint*)addr;
		anchor[0] = joint->GetGroundAnchorA().x;
		anchor[1] = joint->GetGroundAnchorA().y;
	*/

	/** Get the second ground anchor. */
	private final Vector2 groundAnchorB = new Vector2();

	public Vector2 getGroundAnchorB () {
		jniGetGroundAnchorB(addr, tmp);
		groundAnchorB.set(tmp[0], tmp[1]);
		return groundAnchorB;
	}

	private native void jniGetGroundAnchorB (long addr, float[] anchor); /*
		b2PulleyJoint* joint = (b2PulleyJoint*)addr;
		anchor[0] = joint->GetGroundAnchorB().x;
		anchor[1] = joint->GetGroundAnchorB().y;
	*/

	/** Get the current length of the segment attached to body1. */
	public float getLength1 () {
		return jniGetLength1(addr);
	}

	private native float jniGetLength1 (long addr); /*
		b2PulleyJoint* joint = (b2PulleyJoint*)addr;
		return joint->GetLengthA();
	*/

	/** Get the current length of the segment attached to body2. */
	public float getLength2 () {
		return jniGetLength2(addr);
	}

	private native float jniGetLength2 (long addr); /*
		b2PulleyJoint* joint = (b2PulleyJoint*)addr;
		return joint->GetLengthB();
	*/

	/** Get the pulley ratio. */
	public float getRatio () {
		return jniGetRatio(addr);
	}

	private native float jniGetRatio (long addr); /*
		b2PulleyJoint* joint = (b2PulleyJoint*)addr;
		return joint->GetRatio();
	*/
}
