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

/** A distance joint constrains two points on two bodies to remain at a fixed distance from each other. You can view this as a
 * massless, rigid rod. */
public class DistanceJoint extends Joint {
	// @off
	/*JNI
#include <Box2D/Box2D.h>
	 */
	
	public DistanceJoint (World world, long addr) {
		super(world, addr);
	}

	/** Set/get the natural length. Manipulating the length can lead to non-physical behavior when the frequency is zero. */
	public void setLength (float length) {
		jniSetLength(addr, length);
	}

	private native void jniSetLength (long addr, float length); /*
		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		joint->SetLength( length );
	*/

	/** Set/get the natural length. Manipulating the length can lead to non-physical behavior when the frequency is zero. */
	public float getLength () {
		return jniGetLength(addr);
	}

	private native float jniGetLength (long addr); /*
		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		return joint->GetLength();
	*/

	/** Set/get frequency in Hz. */
	public void setFrequency (float hz) {
		jniSetFrequency(addr, hz);
	}

	private native void jniSetFrequency (long addr, float hz); /*
		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		joint->SetFrequency( hz );
	*/

	/** Set/get frequency in Hz. */
	public float getFrequency () {
		return jniGetFrequency(addr);
	}

	private native float jniGetFrequency (long addr); /*
		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		return joint->GetFrequency();
	*/

	/** Set/get damping ratio. */
	public void setDampingRatio (float ratio) {
		jniSetDampingRatio(addr, ratio);
	}

	private native void jniSetDampingRatio (long addr, float ratio); /*
		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		joint->SetDampingRatio( ratio );
	*/

	/** Set/get damping ratio. */
	public float getDampingRatio () {
		return jniGetDampingRatio(addr);
	}

	private native float jniGetDampingRatio (long addr); /*
		b2DistanceJoint* joint = (b2DistanceJoint*)addr;
		return joint->GetDampingRatio();
	*/
}
