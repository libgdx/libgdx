/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.math;

import java.util.List;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Class offering various static methods for intersection testing between different geometric objects.
 * 
 * @author badlogicgames@gmail.com
 * 
 */
public final class Intersector {
	/**
	 * Returns the lowest positive root of the quadric equation given by a* x * x + b * x + c = 0. If no solution is given
	 * Float.Nan is returned.
	 * 
	 * @param a the first coefficient of the quadric equation
	 * @param b the second coefficient of the quadric equation
	 * @param c the third coefficient of the quadric equation
	 * @return the lowest positive root or Float.Nan
	 */
	public static float getLowestPositiveRoot (float a, float b, float c) {
		float det = b * b - 4 * a * c;
		if (det < 0) return Float.NaN;

		float sqrtD = (float)Math.sqrt(det);
		float invA = 1 / (2 * a);
		float r1 = (-b - sqrtD) * invA;
		float r2 = (-b + sqrtD) * invA;

		if (r1 > r2) {
			float tmp = r2;
			r2 = r1;
			r1 = tmp;
		}

		if (r1 > 0) return r1;

		if (r2 > 0) return r2;

		return Float.NaN;
	}

	private final static Vector3 v0 = new Vector3();
	private final static Vector3 v1 = new Vector3();
	private final static Vector3 v2 = new Vector3();

	/**
	 * Returns whether the given point is inside the triangle. This assumes that the point is on the plane of the triangle. No
	 * check is performed that this is the case.
	 * 
	 * @param point the point
	 * @param t1 the first vertex of the triangle
	 * @param t2 the second vertex of the triangle
	 * @param t3 the third vertex of the triangle
	 * @return whether the point is in the triangle
	 */
	public static boolean isPointInTriangle (Vector3 point, Vector3 t1, Vector3 t2, Vector3 t3) {
		v0.set(t1).sub(point);
		v1.set(t2).sub(point);
		v2.set(t3).sub(point);

		float ab = v0.dot(v1);
		float ac = v0.dot(v2);
		float bc = v1.dot(v2);
		float cc = v2.dot(v2);

		if (bc * ac - cc * ab < 0) return false;
		float bb = v1.dot(v1);
		if (ab * bc - ac * bb < 0) return false;
		return true;
	}

	public static boolean intersectSegmentPlane (Vector3 start, Vector3 end, Plane plane, Vector3 intersection) {
		Vector3 dir = end.tmp().sub(start);
		float denom = dir.dot(plane.getNormal());
		float t = -(start.dot(plane.getNormal()) + plane.getD()) / denom;
		if (t < 0 || t > 1) return false;

		intersection.set(start).add(dir.mul(t));
		return true;
	}

	/**
	 * Checks wheter the given point is in the polygon. Only the x and y coordinates of the provided {@link Vector3}s are used.
	 * 
	 * @param polygon The polygon vertices
	 * @param point The point
	 * @return Wheter the point is in the polygon
	 */
	public static boolean isPointInPolygon (List<Vector2> polygon, Vector2 point) {

		int j = polygon.size() - 1;
		boolean oddNodes = false;
		for (int i = 0; i < polygon.size(); i++) {
			if ((polygon.get(i).y < point.y && polygon.get(j).y >= point.y)
				|| (polygon.get(j).y < point.y && polygon.get(i).y >= point.y)) {
				if (polygon.get(i).x + (point.y - polygon.get(i).y) / (polygon.get(j).y - polygon.get(i).y)
					* (polygon.get(j).x - polygon.get(i).x) < point.x) {
					oddNodes = !oddNodes;
				}
			}
			j = i;
		}

		return oddNodes;
	}

	/**
	 * Returns the distance between the given line segment and point.
	 * 
	 * @param start The line start point
	 * @param end The line end point
	 * @param point The point
	 * 
	 * @return The distance between the line segment and the point.
	 */
	public static float distanceLinePoint (Vector2 start, Vector2 end, Vector2 point) {
		tmp.set(end.x, end.y, 0).sub(start.x, start.y, 0);
		float l = tmp.len();
		tmp2.set(start.x, start.y, 0).sub(point.x, point.y, 0);
		return tmp.crs(tmp2).len() / l;
	}

	/**
	 * Returns wheter the given line segment intersects the given circle.
	 * 
	 * @param start The start point of the line segment
	 * @param end The end point of the line segment
	 * @param center The center of the circle
	 * @param squareRadius The squared radius of the circle
	 * @return Wheter the line segment and the circle intersect
	 */
	public static boolean intersectSegmentCircle (Vector2 start, Vector2 end, Vector2 center, float squareRadius) {
		float u = (center.x - start.x) * (end.x - start.x) + (center.y - start.y) * (end.y - start.y);
		float d = start.dst(end);
		u /= (d * d);
		if (u < 0 || u > 1) return false;
		tmp.set(end.x, end.y, 0).sub(start.x, start.y, 0);
		tmp2.set(start.x, start.y, 0).add(tmp.mul(u));
		if (tmp2.dst2(center.x, center.y, 0) < squareRadius)
			return true;
		else
			return false;
	}

	/**
	 * Checks wheter the line segment and the circle intersect and returns by how much and in what direction the line has to move
	 * away from the circle to not intersect.
	 * 
	 * @param start The line segment starting point
	 * @param end The line segment end point
	 * @param point The center of the circle
	 * @param radius The radius of the circle
	 * @param displacement The displacement vector set by the method having unit length
	 * @return The displacement or Float.POSITIVE_INFINITY if no intersection is present
	 */
	public static float intersectSegmentCircleDisplace (Vector2 start, Vector2 end, Vector2 point, float radius,
		Vector2 displacement) {
		float u = (point.x - start.x) * (end.x - start.x) + (point.y - start.y) * (end.y - start.y);
		float d = start.dst(end);
		u /= (d * d);
		if (u < 0 || u > 1) return Float.POSITIVE_INFINITY;
		tmp.set(end.x, end.y, 0).sub(start.x, start.y, 0);
		tmp2.set(start.x, start.y, 0).add(tmp.mul(u));
		d = tmp2.dst(point.x, point.y, 0);
		if (d < radius) {
			displacement.set(point).sub(tmp2.x, tmp2.y).nor();
			return d;
		} else
			return Float.POSITIVE_INFINITY;
	}

	/**
	 * Intersects a {@link Ray} and a {@link Plane}. The intersection point is stored in intersection in case an intersection is
	 * present.
	 * 
	 * @param ray The ray
	 * @param plane The plane
	 * @param intersection The vector the intersection point is written to (optional)
	 * @return Whether an intersection is present.
	 */
	public static boolean intersectRayPlane (Ray ray, Plane plane, Vector3 intersection) {
		float denom = ray.direction.dot(plane.getNormal());
		if (denom != 0) {
			float t = -(ray.origin.dot(plane.getNormal()) + plane.getD()) / denom;
			if (t < 0) return false;

			if (intersection != null) intersection.set(ray.origin).add(ray.direction.tmp().mul(t));
			return true;
		} else if (plane.testPoint(ray.origin) == Plane.PlaneSide.OnPlane) {
			if (intersection != null) intersection.set(ray.origin);
			return true;
		} else
			return false;
	}

	private static final Plane p = new Plane(new Vector3(), 0);
	private static final Vector3 i = new Vector3();

	/**
	 * Intersect a {@link Ray} and a triangle, returning the intersection point in intersection.
	 * 
	 * @param ray The ray
	 * @param t1 The first vertex of the triangle
	 * @param t2 The second vertex of the triangle
	 * @param t3 The third vertex of the triangle
	 * @param intersection The intersection point (optional)
	 * @return True in case an intersection is present.
	 */
	public static boolean intersectRayTriangle (Ray ray, Vector3 t1, Vector3 t2, Vector3 t3, Vector3 intersection) {
		p.set(t1, t2, t3);
		if (!intersectRayPlane(ray, p, i)) return false;

		v0.set(t3).sub(t1);
		v1.set(t2).sub(t1);
		v2.set(i).sub(t1);

		float dot00 = v0.dot(v0);
		float dot01 = v0.dot(v1);
		float dot02 = v0.dot(v2);
		float dot11 = v1.dot(v1);
		float dot12 = v1.dot(v2);

		float denom = dot00 * dot11 - dot01 * dot01;
		if (denom == 0) return false;

		float u = (dot11 * dot02 - dot01 * dot12) / denom;
		float v = (dot00 * dot12 - dot01 * dot02) / denom;

		if (u >= 0 && v >= 0 && u + v <= 1) {
			if (intersection != null) intersection.set(i);
			return true;
		} else {
			return false;
		}

	}

	private static final Vector3 dir = new Vector3();
	private static final Vector3 start = new Vector3();

	/**
	 * Intersects a {@link Ray} and a sphere, returning the intersection point in intersection.
	 * 
	 * @param ray The ray
	 * @param center The center of the sphere
	 * @param radius The radius of the sphere
	 * @param intersection The intersection point (optional)
	 * @return Whether an intersection is present.
	 */
	public static boolean intersectRaySphere (Ray ray, Vector3 center, float radius, Vector3 intersection) {
		dir.set(ray.direction).nor();
		start.set(ray.origin);
		float b = 2 * (dir.dot(start.tmp().sub(center)));
		float c = start.dst2(center) - radius * radius;
		float disc = b * b - 4 * c;
		if (disc < 0) return false;

		// compute q as described above
		float distSqrt = (float)Math.sqrt(disc);
		float q;
		if (b < 0)
			q = (-b - distSqrt) / 2.0f;
		else
			q = (-b + distSqrt) / 2.0f;

		// compute t0 and t1
		float t0 = q / 1;
		float t1 = c / q;

		// make sure t0 is smaller than t1
		if (t0 > t1) {
			// if t0 is bigger than t1 swap them around
			float temp = t0;
			t0 = t1;
			t1 = temp;
		}

		// if t1 is less than zero, the object is in the ray's negative
		// direction
		// and consequently the ray misses the sphere
		if (t1 < 0) return false;

		// if t0 is less than zero, the intersection point is at t1
		if (t0 < 0) {
			if (intersection != null) intersection.set(start).add(dir.tmp().mul(t1));
			return true;
		}
		// else the intersection point is at t0
		else {
			if (intersection != null) intersection.set(start).add(dir.tmp().mul(t0));
			return true;
		}
	}

	/**
	 * Quick check wheter the given {@link Ray} and {@link BoundingBox} intersect.
	 * 
	 * @param ray The ray
	 * @param box The bounding box
	 * @return Wheter the ray and the bounding box intersect.
	 */
	static public boolean intersectRayBoundsFast (Ray ray, BoundingBox box) {
		float a, b;
		float min, max;
		float divX = 1 / ray.direction.x;
		float divY = 1 / ray.direction.y;
		float divZ = 1 / ray.direction.z;

		a = (box.min.x - ray.origin.x) * divX;
		b = (box.max.x - ray.origin.x) * divX;
		if (a < b) {
			min = a;
			max = b;
		} else {
			min = b;
			max = a;
		}

		a = (box.min.y - ray.origin.y) * divY;
		b = (box.max.y - ray.origin.y) * divY;
		if (a > b) {
			float t = a;
			a = b;
			b = t;
		}

		if (a > min) min = a;
		if (b < max) max = b;

		a = (box.min.z - ray.origin.z) * divZ;
		b = (box.max.z - ray.origin.z) * divZ;
		if (a > b) {
			float t = a;
			a = b;
			b = t;
		}

		if (a > min) min = a;
		if (b < max) max = b;

		return (max >= 0) && (max >= min);
	}

	static Vector3 tmp = new Vector3();
	static Vector3 best = new Vector3();
	static Vector3 tmp1 = new Vector3();
	static Vector3 tmp2 = new Vector3();
	static Vector3 tmp3 = new Vector3();

	/**
	 * Intersects the given ray with list of triangles. Returns the nearest intersection point in intersection
	 * 
	 * @param ray The ray
	 * @param triangles The triangles, each successive 3 elements from a vertex
	 * @param intersection The nearest intersection point (optional)
	 * @return Whether the ray and the triangles intersect.
	 */
	public static boolean intersectRayTriangles (Ray ray, float[] triangles, Vector3 intersection) {
		float min_dist = Float.MAX_VALUE;
		boolean hit = false;

		if ((triangles.length / 3) % 3 != 0) throw new RuntimeException("triangle list size is not a multiple of 3");

		for (int i = 0; i < triangles.length - 6; i += 9) {
			boolean result = intersectRayTriangle(ray, tmp1.set(triangles[i], triangles[i + 1], triangles[i + 2]),
				tmp2.set(triangles[i + 3], triangles[i + 4], triangles[i + 5]),
				tmp3.set(triangles[i + 6], triangles[i + 7], triangles[i + 8]), tmp);

			if (result == true) {
				float dist = ray.origin.tmp().sub(tmp).len();
				if (dist < min_dist) {
					min_dist = dist;
					best.set(tmp);
					hit = true;
				}
			}
		}

		if (hit == false)
			return false;
		else {
			if (intersection != null) intersection.set(best);
			return true;
		}
	}

	/**
	 * Intersects the given ray with list of triangles. Returns the nearest intersection point in intersection
	 * 
	 * @param ray The ray
	 * @param vertices the vertices
	 * @param indices the indices, each successive 3 shorts index the 3 vertices of a triangle
	 * @param vertexSize the size of a vertex in floats
	 * @param intersection The nearest intersection point (optional)
	 * @return Whether the ray and the triangles intersect.
	 */
	public static boolean intersectRayTriangles (Ray ray, float[] vertices, short[] indices, int vertexSize, Vector3 intersection) {
		float min_dist = Float.MAX_VALUE;
		boolean hit = false;

		if ((indices.length % 3) != 0) throw new RuntimeException("triangle list size is not a multiple of 3");

		for (int i = 0; i < indices.length; i += 3) {
			int i1 = indices[i] * vertexSize;
			int i2 = indices[i + 1] * vertexSize;
			int i3 = indices[i + 2] * vertexSize;

			boolean result = intersectRayTriangle(ray, tmp1.set(vertices[i1], vertices[i1 + 1], vertices[i1 + 2]),
				tmp2.set(vertices[i2], vertices[i2 + 1], vertices[i2 + 2]),
				tmp3.set(vertices[i3], vertices[i3 + 1], vertices[i3 + 2]), tmp);

			if (result == true) {
				float dist = ray.origin.tmp().sub(tmp).len();
				if (dist < min_dist) {
					min_dist = dist;
					best.set(tmp);
					hit = true;
				}
			}
		}

		if (hit == false)
			return false;
		else {
			if (intersection != null) intersection.set(best);
			return true;
		}
	}

	/**
	 * Intersects the given ray with list of triangles. Returns the nearest intersection point in intersection
	 * 
	 * @param ray The ray
	 * @param triangles The triangles
	 * @param intersection The nearest intersection point (optional)
	 * @return Whether the ray and the triangles intersect.
	 */
	public static boolean intersectRayTriangles (Ray ray, List<Vector3> triangles, Vector3 intersection) {
		float min_dist = Float.MAX_VALUE;

		if (triangles.size() % 3 != 0) throw new RuntimeException("triangle list size is not a multiple of 3");

		for (int i = 0; i < triangles.size() - 2; i += 3) {
			boolean result = intersectRayTriangle(ray, triangles.get(i), triangles.get(i + 1), triangles.get(i + 2), tmp);

			if (result == true) {
				float dist = ray.origin.tmp().sub(tmp).len();
				if (dist < min_dist) {
					min_dist = dist;
					best.set(tmp);
				}
			}
		}

		if (best == null)
			return false;
		else {
			if (intersection != null) intersection.set(best);
			return true;
		}
	}

	/**
	 * Returns wheter the two rectangles intersect
	 * 
	 * @param a The first rectangle
	 * @param b The second rectangle
	 * @return Wheter the two rectangles intersect
	 */
	public static boolean intersectRectangles (Rectangle a, Rectangle b) {
		return !(a.getX() > b.getX() + b.getWidth() || a.getX() + a.getWidth() < b.getX() || a.getY() > b.getY() + b.getHeight() || a
			.getY() + a.getHeight() < b.getY());
	}

	/**
	 * Intersects the two lines and returns the intersection point in intersection.
	 * 
	 * @param p1 The first point of the first line
	 * @param p2 The second point of the first line
	 * @param p3 The first point of the second line
	 * @param p4 The second point of the second line
	 * @param intersection The intersection point
	 * @return Whether the two lines intersect
	 */
	public static boolean intersectLines (Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4, Vector2 intersection) {
		float x1 = p1.x, y1 = p1.y, x2 = p2.x, y2 = p2.y, x3 = p3.x, y3 = p3.y, x4 = p4.x, y4 = p4.y;

		float det1 = det(x1, y1, x2, y2);
		float det2 = det(x3, y3, x4, y4);
		float det3 = det(x1 - x2, y1 - y2, x3 - x4, y3 - y4);

		float x = det(det1, x1 - x2, det2, x3 - x4) / det3;
		float y = det(det1, y1 - y2, det2, y3 - y4) / det3;

		intersection.x = x;
		intersection.y = y;

		return true;
	}

	/**
	 * Intersects the two line segments and returns the intersection point in intersection.
	 * 
	 * @param p1 The first point of the first line segment
	 * @param p2 The second point of the first line segment
	 * @param p3 The first point of the second line segment
	 * @param p4 The second point of the second line segment
	 * @param intersection The intersection point (optional)
	 * @return Whether the two line segments intersect
	 */
	public static boolean intersectSegments (Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4, Vector2 intersection) {
		float x1 = p1.x, y1 = p1.y, x2 = p2.x, y2 = p2.y, x3 = p3.x, y3 = p3.y, x4 = p4.x, y4 = p4.y;

		float d = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		if (d == 0) return false;

		float ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / d;
		float ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / d;

		if (ua < 0 || ua > 1) return false;
		if (ub < 0 || ub > 1) return false;

		if (intersection != null) intersection.set(x1 + (x2 - x1) * ua, y1 + (y2 - y1) * ua);
		return true;
	}

	static float det (float a, float b, float c, float d) {
		return a * d - b * c;
	}

	static double detd (double a, double b, double c, double d) {
		return a * d - b * c;
	}
}
