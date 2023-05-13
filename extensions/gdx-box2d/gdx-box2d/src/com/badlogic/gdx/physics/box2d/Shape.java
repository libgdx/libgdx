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
import com.badlogic.gdx.utils.Disposable;

/** A shape is used for collision detection. You can create a shape however you like. Shapes used for simulation in b2World are
 * created automatically when a b2Fixture is created. Shapes may encapsulate a one or more child shapes.
 * 
 * NOTE: YOU NEED TO DISPOSE SHAPES YOU CREATED YOURSELF AFTER YOU NO LONGER USE THEM! E.g. after calling body.createFixture();
 * @author mzechner */
public abstract class Shape implements Disposable {
	// @off
	/*JNI
#include <box2d/box2d.h>
	 */ // @on

	/** Enum describing the type of a shape
	 * @author mzechner */
	public enum Type {
		Circle, Edge, Polygon, Chain,
	};

	/** the address of the shape **/
	protected long addr;

	/** Get the type of this shape. You can use this to down cast to the concrete shape.
	 * @return the shape type. */
	public abstract Type getType ();

	/** Returns the radius of this shape */
	public float getRadius () {
		return jniGetRadius(addr);
	}

	private native float jniGetRadius (long addr); /*
		// @off
		b2Shape* shape = (b2Shape*)addr;
		return shape->m_radius;
	*/ // @on

	/** Sets the radius of this shape */
	public void setRadius (float radius) {
		jniSetRadius(addr, radius);
	}

	private native void jniSetRadius (long addr, float radius); /*
		// @off
		b2Shape* shape = (b2Shape*)addr;
		shape->m_radius = radius;
	*/ // @on

	/** Needs to be called when the shape is no longer used, e.g. after a fixture was created based on the shape. */
	@Override
	public void dispose () {
		jniDispose(addr);
	}

	private native void jniDispose (long addr); /*
		// @off
		b2Shape* shape = (b2Shape*)addr;
		delete shape;
	*/ // @on

	protected static native int jniGetType (long addr); /*
		// @off
		b2Shape* shape = (b2Shape*)addr;
		switch(shape->m_type) {
		case b2Shape::e_circle: return 0;
		case b2Shape::e_edge: return 1;
		case b2Shape::e_polygon: return 2;
		case b2Shape::e_chain: return 3;
		default: return -1;
		}
	*/ // @on

	/** Get the number of child primitives. */
	public int getChildCount () {
		return jniGetChildCount(addr);
	}

	private native int jniGetChildCount (long addr); /*
		// @off
		b2Shape* shape = (b2Shape*)addr;
		return shape->GetChildCount();
	*/ // @on

	public boolean testPoint (Transform transform, Vector2 point) {
		return jniTestPoint(addr, transform.vals[0], transform.vals[1], transform.vals[2], transform.vals[3], point.x, point.y);
	}

	public native boolean jniTestPoint (long addr, float transformX, float transformY, float transformC, float transformS,
		float pX, float pY); /*
		// @off
		b2Shape* shape = (b2Shape*)addr;
		b2Transform t;
		t.p.x = transformX;
		t.p.y = transformY;
		t.q.c = transformC;
		t.q.s = transformS;
		b2Vec2 p(pX, pY);
		return shape->TestPoint(t, p);
	*/ // @on

	private static final float[] verts = new float[4];

	public void computeAABB (Vector2 lowerBound, Vector2 upperBound, Transform transform, int childIndex) {
		jniComputeAABB(addr, verts, transform.vals[0], transform.vals[1], transform.vals[2], transform.vals[3], childIndex);
		lowerBound.x = verts[0];
		lowerBound.y = verts[1];
		upperBound.x = verts[2];
		upperBound.y = verts[3];
	}

	private native void jniComputeAABB (long addr, float[] verts, float val, float val1, float val2, float val3, int ix); /*
		// @off
    b2Shape* shape = (b2Shape*)addr;
    b2Transform t;
    t.p.x = val;
    t.p.y = val1;
    t.q.c = val2;
    t.q.s = val3;
    b2AABB aabb;
    shape->ComputeAABB(&aabb, t, ix);
    verts[0] = aabb.lowerBound.x;
    verts[1] = aabb.lowerBound.y;
    verts[2] = aabb.upperBound.x;
    verts[3] = aabb.upperBound.y;
  */

// /// Cast a ray against this shape.
// /// @param output the ray-cast results.
// /// @param input the ray-cast input parameters.
// /// @param transform the transform to be applied to the shape.
// virtual bool RayCast(b2RayCastOutput* output, const b2RayCastInput& input, const b2Transform& transform) const = 0;
//
// /// Compute the mass properties of this shape using its dimensions and density.
// /// The inertia tensor is computed about the local origin.
// /// @param massData returns the mass data for this shape.
// /// @param density the density in kilograms per meter squared.
// virtual void ComputeMass(b2MassData* massData, float32 density) const = 0;
}
