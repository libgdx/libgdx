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

package com.badlogic.gdx.physics.box2d;

/** Contact impulses for reporting. Impulses are used instead of forces because sub-step forces may approach infinity for rigid
 * body collisions. These match up one-to-one with the contact points in b2Manifold.
 * @author mzechner */
public class ContactImpulse {
	// @off
	/*JNI
#include <Box2D/Box2D.h>
	 */
	
	final World world;
	long addr;
	float[] tmp = new float[2];
	final float[] normalImpulses = new float[2];
	final float[] tangentImpulses = new float[2];

	protected ContactImpulse (World world, long addr) {
		this.world = world;
		this.addr = addr;
	}

	public float[] getNormalImpulses () {
		jniGetNormalImpulses(addr, normalImpulses);
		return normalImpulses;
	}

	private native void jniGetNormalImpulses (long addr, float[] values); /*
		b2ContactImpulse* contactImpulse = (b2ContactImpulse*)addr;	
		values[0] = contactImpulse->normalImpulses[0];
		values[1] = contactImpulse->normalImpulses[1];
	*/

	public float[] getTangentImpulses () {
		jniGetTangentImpulses(addr, tangentImpulses);
		return tangentImpulses;
	}

	private native void jniGetTangentImpulses (long addr, float[] values); /*
	  	b2ContactImpulse* contactImpulse = (b2ContactImpulse*)addr;	
		values[0] = contactImpulse->tangentImpulses[0];
		values[1] = contactImpulse->tangentImpulses[1];
	*/

	public int getCount () {
		return jniGetCount(addr);
	}

	private native int jniGetCount (long addr); /*
		b2ContactImpulse* contactImpulse = (b2ContactImpulse*)addr;
		return contactImpulse->count;
	*/
}
