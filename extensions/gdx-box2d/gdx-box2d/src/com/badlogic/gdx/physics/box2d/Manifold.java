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

import com.badlogic.gdx.math.Vector2;

public class Manifold {
	// @off
	/*JNI
#include <Box2D/Box2D.h>
	 */
	
	long addr;
	final ManifoldPoint[] points = new ManifoldPoint[] {new ManifoldPoint(), new ManifoldPoint()};
	final Vector2 localNormal = new Vector2();
	final Vector2 localPoint = new Vector2();

	final int[] tmpInt = new int[2];
	final float[] tmpFloat = new float[4];

	protected Manifold (long addr) {
		this.addr = addr;
	}

	public ManifoldType getType () {
		int type = jniGetType(addr);
		if (type == 0) return ManifoldType.Circle;
		if (type == 1) return ManifoldType.FaceA;
		if (type == 2) return ManifoldType.FaceB;
		return ManifoldType.Circle;
	}

	private native int jniGetType (long addr); /*
		b2Manifold* manifold = (b2Manifold*)addr;
		return manifold->type;
	*/

	public int getPointCount () {
		return jniGetPointCount(addr);
	}

	private native int jniGetPointCount (long addr); /*
	  	b2Manifold* manifold = (b2Manifold*)addr;
		return manifold->pointCount;
	*/

	public Vector2 getLocalNormal () {
		jniGetLocalNormal(addr, tmpFloat);
		localNormal.set(tmpFloat[0], tmpFloat[1]);
		return localNormal;
	}

	private native void jniGetLocalNormal (long addr, float[] values); /*
		b2Manifold* manifold = (b2Manifold*)addr;
		values[0] = manifold->localNormal.x;
		values[1] = manifold->localNormal.y;
	*/

	public Vector2 getLocalPoint () {
		jniGetLocalPoint(addr, tmpFloat);
		localPoint.set(tmpFloat[0], tmpFloat[1]);
		return localPoint;
	}

	private native void jniGetLocalPoint (long addr, float[] values); /*
		b2Manifold* manifold = (b2Manifold*)addr;
		values[0] = manifold->localPoint.x;
		values[1] = manifold->localPoint.y;
	*/

	public ManifoldPoint[] getPoints () {
		int count = jniGetPointCount(addr);

		for (int i = 0; i < count; i++) {
			int contactID = jniGetPoint(addr, tmpFloat, i);
			ManifoldPoint point = points[i];
			point.contactID = contactID;
			point.localPoint.set(tmpFloat[0], tmpFloat[1]);
			point.normalImpulse = tmpFloat[2];
			point.tangentImpulse = tmpFloat[3];
		}

		return points;
	}

	private native int jniGetPoint (long addr, float[] values, int idx); /*
		b2Manifold* manifold = (b2Manifold*)addr;
		  
		values[0] = manifold->points[idx].localPoint.x;
		values[1] = manifold->points[idx].localPoint.y;
		values[2] = manifold->points[idx].normalImpulse;
		values[3] = manifold->points[idx].tangentImpulse;  
		  
		return (jint)manifold->points[idx].id.key;
	*/

	public class ManifoldPoint {
		public final Vector2 localPoint = new Vector2();
		public float normalImpulse;
		public float tangentImpulse;
		public int contactID = 0;

		public String toString () {
			return "id: " + contactID + ", " + localPoint + ", " + normalImpulse + ", " + tangentImpulse;
		}
	}

	public enum ManifoldType {
		Circle, FaceA, FaceB
	}
}
