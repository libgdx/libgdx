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

import org.jbox2d.pooling.normal.DefaultWorldPool;

/** Computes manifolds or overlap status for shapes. */
public class Collision {
	private static org.jbox2d.collision.Collision collision;
	private final static Manifold m = new Manifold();
	private final static org.jbox2d.collision.Manifold bM = new org.jbox2d.collision.Manifold();
	private final static org.jbox2d.common.Transform bXfA = new org.jbox2d.common.Transform();
	private final static org.jbox2d.common.Transform bXfB = new org.jbox2d.common.Transform();

	static {
		collision = new org.jbox2d.collision.Collision(new DefaultWorldPool(0, 0)); // only used for Distance
	}

	private static void fillB2Transforms (Transform t1, Transform t2) {
		bXfA.p.x = t1.vals[Transform.POS_X];
		bXfA.p.y = t1.vals[Transform.POS_Y];
		bXfA.q.c = t1.vals[Transform.COS];
		bXfA.q.s = t1.vals[Transform.SIN];

		bXfB.p.x = t2.vals[Transform.POS_X];
		bXfB.p.y = t2.vals[Transform.POS_Y];
		bXfB.q.c = t2.vals[Transform.COS];
		bXfB.q.s = t2.vals[Transform.SIN];
	}

	/** Compute the collision manifold between two circles.
	 * Note: The manifold is reused. */
	public static Manifold collideCircles (CircleShape circleA, Transform xfA, CircleShape circleB, Transform xfB) {
		fillB2Transforms(xfA, xfB);
		collision.collideCircles(bM, circleA.shape, bXfA, circleB.shape, bXfB);
		return m;
	}

	/** Compute the collision manifold between a polygon and a circle.
	 * Note: The manifold is reused. */
	public static Manifold collidePolygonAndCircle (PolygonShape polygon, Transform xfA, CircleShape circle, Transform xfB) {
		fillB2Transforms(xfA, xfB);
		collision.collidePolygonAndCircle(bM, polygon.shape, bXfA, circle.shape, bXfB);
		return m;
	}

	/** Compute the collision manifold between two polygons.
	 * Note: The manifold is reused. */
	public static Manifold collidePolygons (PolygonShape polygonA, Transform xfA, PolygonShape polygonB, Transform xfB) {
		fillB2Transforms(xfA, xfB);
		collision.collidePolygons(bM, polygonA.shape, bXfA, polygonB.shape, bXfB);
		return m;
	}

	/** Compute the collision manifold between an edge and a circle.
	 * Note: The manifold is reused. */
	public static Manifold collideEdgeAndCircle (EdgeShape edge, Transform xfA, CircleShape circle, Transform xfB) {
		fillB2Transforms(xfA, xfB);
		collision.collideEdgeAndCircle(bM, edge.shape, bXfA, circle.shape, bXfB);
		return m;
	}

	/** Compute the collision manifold between an edge and a polygon.
	 * Note: The manifold is reused. */
	public static Manifold collideEdgeAndPolygon (EdgeShape edge, Transform xfA, PolygonShape polygon, Transform xfB) {
		fillB2Transforms(xfA, xfB);
		collision.collideEdgeAndPolygon(bM, edge.shape, bXfA, polygon.shape, bXfB);
		return m;
	}

	/** Determine if two generic shapes overlap. */
	public static boolean testOverlap (Shape shapeA, int indexA, Shape shapeB, int indexB, Transform xfA, Transform xfB) {
		fillB2Transforms(xfA, xfB);
		return collision.testOverlap(shapeA.shape, indexA, shapeB.shape, indexB, bXfA, bXfB);
	}
}
