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

import org.jbox2d.pooling.normal.DefaultWorldPool;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.Rot;

/** Computes manifolds or overlap status for shapes. */
public class Collision {
	private static org.jbox2d.collision.Collision collision;
	private static Manifold m = new Manifold();
	static {
		collision = new org.jbox2d.collision.Collision(new DefaultWorldPool(0, 0)); // only used for Distance
	}

	private static org.jbox2d.common.Transform toB2Transform(Transform t) {
		Vec2 p = new Vec2();
		p.x = t.vals[Transform.POS_X];
		p.y = t.vals[Transform.POS_Y];
		Rot rot = new Rot();
		rot.c = t.vals[Transform.COS];
		rot.s = t.vals[Transform.SIN];
		return new org.jbox2d.common.Transform(p, rot);
	}

	/** Compute the collision manifold between two circles.
	 * Note: The manifold is reused.
	 */
	public static Manifold collideCircles (CircleShape circleA, Transform xfA, CircleShape circleB, Transform xfB) {
		org.jbox2d.collision.Manifold bM = new org.jbox2d.collision.Manifold();
		org.jbox2d.common.Transform bXfA = toB2Transform(xfA);
		org.jbox2d.common.Transform bXfB = toB2Transform(xfB);
		collision.collideCircles(bM, circleA.shape, bXfA, circleB.shape, bXfB);
		m.manifold = bM;
		return m;
	}

	/** Compute the collision manifold between a polygon and a circle.
	 * Note: The manifold is reused.
	 */
	public static Manifold collidePolygonAndCircle (PolygonShape polygon, Transform xfA, CircleShape circle, Transform xfB) {
		org.jbox2d.collision.Manifold bM = new org.jbox2d.collision.Manifold();
		org.jbox2d.common.Transform bXfA = toB2Transform(xfA);
		org.jbox2d.common.Transform bXfB = toB2Transform(xfB);
		collision.collidePolygonAndCircle(bM, polygon.shape, bXfA, circle.shape, bXfB);
		m.manifold = bM;
		return m;
	}

	/** Compute the collision manifold between two polygons.
	 * Note: The manifold is reused.
	 */
	public static Manifold collidePolygons (PolygonShape polygonA, Transform xfA, PolygonShape polygonB, Transform xfB) {
		org.jbox2d.collision.Manifold bM = new org.jbox2d.collision.Manifold();
		org.jbox2d.common.Transform bXfA = toB2Transform(xfA);
		org.jbox2d.common.Transform bXfB = toB2Transform(xfB);
		collision.collidePolygons(bM, polygonA.shape, bXfA, polygonB.shape, bXfB);
		m.manifold = bM;
		return m;
	}

	/** Compute the collision manifold between an edge and a circle.
	 * Note: The manifold is reused.
	 */
	public static Manifold collideEdgeAndCircle (EdgeShape edge, Transform xfA, CircleShape circle, Transform xfB) {
		org.jbox2d.collision.Manifold bM = new org.jbox2d.collision.Manifold();
		org.jbox2d.common.Transform bXfA = toB2Transform(xfA);
		org.jbox2d.common.Transform bXfB = toB2Transform(xfB);
		collision.collideEdgeAndCircle(bM, edge.shape, bXfA, circle.shape, bXfB);
		m.manifold = bM;
		return m;
	}

	/** Compute the collision manifold between an edge and a polygon.
	 * Note: The manifold is reused.
	 */
	public static Manifold collideEdgeAndPolygon (EdgeShape edge, Transform xfA, PolygonShape polygon, Transform xfB) {
		org.jbox2d.collision.Manifold bM = new org.jbox2d.collision.Manifold();
		org.jbox2d.common.Transform bXfA = toB2Transform(xfA);
		org.jbox2d.common.Transform bXfB = toB2Transform(xfB);
		collision.collideEdgeAndPolygon(bM, edge.shape, bXfA, polygon.shape, bXfB);
		m.manifold = bM;
		return m;
	}

	/** Determine if two generic shapes overlap.
	 */
	public static boolean testOverlap (Shape shapeA, int indexA, Shape shapeB, int indexB, Transform xfA, Transform xfB) {
		org.jbox2d.common.Transform bXfA = toB2Transform(xfA);
		org.jbox2d.common.Transform bXfB = toB2Transform(xfB);
		return collision.testOverlap(shapeA.shape, indexA, shapeB.shape, indexB, bXfA, bXfB);
	}
}
