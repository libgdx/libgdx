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
	org.jbox2d.callbacks.ContactImpulse impulse;
	float[] tmp = new float[2];
	final float[] normalImpulses = new float[2];
	final float[] tangentImpulses = new float[2];

	ContactImpulse () {
	}

	public float[] getNormalImpulses () {
		for (int i = 0; i < impulse.count; i++) {
			normalImpulses[i] = impulse.normalImpulses[i];
		}
		return normalImpulses;
	}

	public float[] getTangentImpulses () {
		for (int i = 0; i < impulse.count; i++) {
			tangentImpulses[i] = impulse.tangentImpulses[i];
		}
		return tangentImpulses;
	}

	public int getCount () {
		return impulse.count;
	}
}
