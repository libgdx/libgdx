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

/** Computes manifolds or overlap status for shapes. */
public class Collision {
	/*JNI
#include <Box2D/Box2D.h>

	static void fillTransform(b2Transform &xf, const float *fields) {
		xf.p.x = fields[0];
		xf.p.y = fields[1];
		xf.q.c = fields[2];
		xf.q.s = fields[3];
	}
	 */

	private static long manifoldAddr;

	private native static long jniCreateManifold (); /*
		// NOTE: this leaks (if the class is initialized)
		return (jlong)(new b2Manifold);
	*/

	static {
		manifoldAddr = jniCreateManifold();
	}

	/*
	 * Compute the collision manifold between two circles.
	 * Note: The manifold is reused.
	 */
	public static Manifold collideCircles (CircleShape circleA, Transform xfA, CircleShape circleB, Transform xfB) {
		Manifold m = new Manifold(manifoldAddr);
		jniCollideCircles(m.addr, circleA.addr, xfA.vals, circleB.addr, xfB.vals);
		return m;
	}

	private native static void jniCollideCircles (long addr, long shapeA, float[] transform1, long shapeB, float[] transform2); /*
		b2Transform xfA, xfB;
		fillTransform(xfA, transform1);
		fillTransform(xfB, transform2);

		b2CollideCircles((b2Manifold*)addr, (b2CircleShape*)shapeA, xfA, (b2CircleShape*)shapeB, xfB);
	*/

	/*
	 * Compute the collision manifold between a polygon and a circle.
	 * Note: The manifold is reused.
	 */
	public static Manifold collidePolygonAndCircle (PolygonShape polygon, Transform xfA, CircleShape circle, Transform xfB) {
		Manifold m = new Manifold(manifoldAddr);
		jniCollidePolygonAndCircle(m.addr, polygon.addr, xfA.vals, circle.addr, xfB.vals);
		return m;
	}

	private native static void jniCollidePolygonAndCircle (long addr, long shapeA, float[] transform1, long shapeB, float[] transform2); /*
		b2Transform xfA, xfB;
		fillTransform(xfA, transform1);
		fillTransform(xfB, transform2);

		b2CollidePolygonAndCircle((b2Manifold*)addr, (b2PolygonShape*)shapeA, xfA, (b2CircleShape*)shapeB, xfB);
	*/

	/*
	 * Compute the collision manifold between two polygons.
	 * Note: The manifold is reused.
	 */
	public static Manifold collidePolygons (PolygonShape polygonA, Transform xfA, PolygonShape polygonB, Transform xfB) {
		Manifold m = new Manifold(manifoldAddr);
		jniCollidePolygons(m.addr, polygonA.addr, xfA.vals, polygonB.addr, xfB.vals);
		return m;
	}

	private static native void jniCollidePolygons (long addr, long shapeA, float[] transform1, long shapeB, float[] transform2); /*
		b2Transform xfA, xfB;
		fillTransform(xfA, transform1);
		fillTransform(xfB, transform2);

		b2CollidePolygons((b2Manifold*)addr, (b2PolygonShape*)shapeA, xfA, (b2PolygonShape*)shapeB, xfB);
	*/

	/*
	 * Compute the collision manifold between an edge and a circle.
	 * Note: The manifold is reused.
	 */
	public static Manifold collideEdgeAndCircle (EdgeShape edge, Transform xfA, CircleShape circle, Transform xfB) {
		Manifold m = new Manifold(manifoldAddr);
		jniCollideEdgeAndCircle(m.addr, edge.addr, xfA.vals, circle.addr, xfB.vals);
		return m;
	}

	private native static void jniCollideEdgeAndCircle (long addr, long shapeA, float[] transform1, long shapeB, float[] transform2); /*
		b2Transform xfA, xfB;
		fillTransform(xfA, transform1);
		fillTransform(xfB, transform2);

		b2CollideEdgeAndCircle((b2Manifold*)addr, (b2EdgeShape*)shapeA, xfA, (b2CircleShape*)shapeB, xfB);
	*/

	/*
	 * Compute the collision manifold between an edge and a polygon.
	 * Note: The manifold is reused.
	 */
	public static Manifold collideEdgeAndPolygon (EdgeShape edge, Transform xfA, PolygonShape polygon, Transform xfB) {
		Manifold m = new Manifold(manifoldAddr);
		jniCollideEdgeAndPolygon(m.addr, edge.addr, xfA.vals, polygon.addr, xfB.vals);
		return m;
	}

	private native static void jniCollideEdgeAndPolygon (long addr, long shapeA, float[] transform1, long shapeB, float[] transform2); /*
		b2Transform xfA, xfB;
		fillTransform(xfA, transform1);
		fillTransform(xfB, transform2);

		b2CollideEdgeAndPolygon((b2Manifold*)addr, (b2EdgeShape*)shapeA, xfA, (b2PolygonShape*)shapeB, xfB);
	*/

	/*
	 * Determine if two generic shapes overlap.
	 */
	public static boolean testOverlap (Shape shapeA, int indexA, Shape shapeB, int indexB, Transform xfA, Transform xfB) {
		return jniTestOverlap(shapeA.addr, indexA, shapeB.addr, indexB, xfA.vals, xfB.vals);
	}

	private static native boolean jniTestOverlap (long shapeA, int indexA, long shapeB, int indexB, float[] transform1, float[] transform2); /*
		b2Transform xfA, xfB;
		fillTransform(xfA, transform1);
		fillTransform(xfB, transform2);

		return b2TestOverlap((b2Shape*)shapeA, indexA, (b2Shape*)shapeB, indexB, xfA, xfB);
	*/
}
