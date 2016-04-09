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

package com.badlogic.gdx.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.math.Plane.PlaneSide;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;

/** Class offering various static methods for intersection testing between different geometric objects.
 * 
 * @author badlogicgames@gmail.com
 * @author jan.stria
 * @author Nathan Sweet */
public final class Intersector {
	private final static Vector3 v0 = new Vector3();
	private final static Vector3 v1 = new Vector3();
	private final static Vector3 v2 = new Vector3();
	private final static FloatArray floatArray = new FloatArray();
	private final static FloatArray floatArray2 = new FloatArray();
	/** Returns whether the given point is inside the triangle. This assumes that the point is on the plane of the triangle. No
	 * check is performed that this is the case.
	 * 
	 * @param point the point
	 * @param t1 the first vertex of the triangle
	 * @param t2 the second vertex of the triangle
	 * @param t3 the third vertex of the triangle
	 * @return whether the point is in the triangle */
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

	/** Returns true if the given point is inside the triangle. */
	public static boolean isPointInTriangle (Vector2 p, Vector2 a, Vector2 b, Vector2 c) {
		float px1 = p.x - a.x;
		float py1 = p.y - a.y;
		boolean side12 = (b.x - a.x) * py1 - (b.y - a.y) * px1 > 0;
		if ((c.x - a.x) * py1 - (c.y - a.y) * px1 > 0 == side12) return false;
		if ((c.x - b.x) * (p.y - b.y) - (c.y - b.y) * (p.x - b.x) > 0 != side12) return false;
		return true;
	}

	/** Returns true if the given point is inside the triangle. */
	public static boolean isPointInTriangle (float px, float py, float ax, float ay, float bx, float by, float cx, float cy) {
		float px1 = px - ax;
		float py1 = py - ay;
		boolean side12 = (bx - ax) * py1 - (by - ay) * px1 > 0;
		if ((cx - ax) * py1 - (cy - ay) * px1 > 0 == side12) return false;
		if ((cx - bx) * (py - by) - (cy - by) * (px - bx) > 0 != side12) return false;
		return true;
	}

	public static boolean intersectSegmentPlane (Vector3 start, Vector3 end, Plane plane, Vector3 intersection) {
		Vector3 dir = v0.set(end).sub(start);
		float denom = dir.dot(plane.getNormal());
		float t = -(start.dot(plane.getNormal()) + plane.getD()) / denom;
		if (t < 0 || t > 1) return false;

		intersection.set(start).add(dir.scl(t));
		return true;
	}

	/** Determines on which side of the given line the point is. Returns -1 if the point is on the left side of the line, 0 if the
	 * point is on the line and 1 if the point is on the right side of the line. Left and right are relative to the lines direction
	 * which is linePoint1 to linePoint2. */
	public static int pointLineSide (Vector2 linePoint1, Vector2 linePoint2, Vector2 point) {
		return (int)Math.signum((linePoint2.x - linePoint1.x) * (point.y - linePoint1.y) - (linePoint2.y - linePoint1.y)
			* (point.x - linePoint1.x));
	}

	public static int pointLineSide (float linePoint1X, float linePoint1Y, float linePoint2X, float linePoint2Y, float pointX,
		float pointY) {
		return (int)Math.signum((linePoint2X - linePoint1X) * (pointY - linePoint1Y) - (linePoint2Y - linePoint1Y)
			* (pointX - linePoint1X));
	}

	/** Checks whether the given point is in the polygon.
	 * @param polygon The polygon vertices passed as an array
	 * @param point The point
	 * @return true if the point is in the polygon */
	public static boolean isPointInPolygon (Array<Vector2> polygon, Vector2 point) {
		Vector2 lastVertice = polygon.peek();
		boolean oddNodes = false;
		for (int i = 0; i < polygon.size; i++) {
			Vector2 vertice = polygon.get(i);
			if ((vertice.y < point.y && lastVertice.y >= point.y) || (lastVertice.y < point.y && vertice.y >= point.y)) {
				if (vertice.x + (point.y - vertice.y) / (lastVertice.y - vertice.y) * (lastVertice.x - vertice.x) < point.x) {
					oddNodes = !oddNodes;
				}
			}
			lastVertice = vertice;
		}
		return oddNodes;
	}

	/** Returns true if the specified point is in the polygon.
	 * @param offset Starting polygon index.
	 * @param count Number of array indices to use after offset. */
	public static boolean isPointInPolygon (float[] polygon, int offset, int count, float x, float y) {
		boolean oddNodes = false;
		int j = offset + count - 2;
		for (int i = offset, n = j; i <= n; i += 2) {
			float yi = polygon[i + 1];
			float yj = polygon[j + 1];
			if ((yi < y && yj >= y) || (yj < y && yi >= y)) {
				float xi = polygon[i];
				if (xi + (y - yi) / (yj - yi) * (polygon[j] - xi) < x) oddNodes = !oddNodes;
			}
			j = i;
		}
		return oddNodes;
	}

	private final static Vector2 ip = new Vector2();
	private final static Vector2 ep1 = new Vector2();
	private final static Vector2 ep2 = new Vector2();
	private final static Vector2 s = new Vector2();
	private final static Vector2 e = new Vector2();

	/** Intersects two resulting polygons with the same winding and sets the overlap polygon
	 *  resulting from the intersection.
	 *  Follows the Sutherland-Hodgman algorithm.
	 *
	 * @param p1 The polygon that is being clipped
	 * @param p2 The clip polygon
	 * @param overlap The intersection of the two polygons (optional)
	 * @return Whether the two polygons intersect.
	 */
	public static boolean intersectPolygons (Polygon p1, Polygon p2, Polygon overlap) {
		//reusable points to trace edges around polygon
		floatArray2.clear();
		floatArray.clear();
		floatArray2.addAll(p1.getTransformedVertices());
		if (p1.getVertices().length == 0 || p2.getVertices().length == 0) {
			return false;
		}
		for (int i = 0; i < p2.getTransformedVertices().length; i += 2) {
			ep1.set(p2.getTransformedVertices()[i], p2.getTransformedVertices()[i+1]);
			//wrap around to beginning of array if index points to end;
			if (i < p2.getTransformedVertices().length - 2) {
				ep2.set(p2.getTransformedVertices()[i + 2], p2.getTransformedVertices()[i + 3]);
			} else {
				ep2.set(p2.getTransformedVertices()[0], p2.getTransformedVertices()[1]);
			}
			if (floatArray2.size == 0) {
				return false;
			}
			s.set(floatArray2.get(floatArray2.size - 2), floatArray2.get(floatArray2.size - 1));
			for (int j = 0; j < floatArray2.size; j += 2) {
				e.set(floatArray2.get(j), floatArray2.get(j + 1));
				//determine if point is inside clip edge
				if (Intersector.pointLineSide(ep2, ep1, e) > 0) {
					if (!(Intersector.pointLineSide(ep2, ep1, s) > 0)) {
						Intersector.intersectLines(s, e, ep1, ep2, ip);
						if (floatArray.size < 2 || floatArray.get(floatArray.size-2) != ip.x || floatArray.get(floatArray.size-1) != ip.y)
						{
							floatArray.add(ip.x);
							floatArray.add(ip.y);
						}
					}
					floatArray.add(e.x);
					floatArray.add(e.y);
				} else if (Intersector.pointLineSide(ep2, ep1, s) > 0) {
					Intersector.intersectLines(s, e, ep1, ep2, ip);
					floatArray.add(ip.x);
					floatArray.add(ip.y);
				}
				s.set(e.x, e.y);
			}
			floatArray2.clear();
			floatArray2.addAll(floatArray);
			floatArray.clear();
		}
		if (! (floatArray2.size == 0)) {
			overlap.setVertices(floatArray2.toArray());
			return true;
		} else {
			return false;
		}
	}

	/** Returns the distance between the given line and point. Note the specified line is not a line segment. */
	public static float distanceLinePoint (float startX, float startY, float endX, float endY, float pointX, float pointY) {
		float normalLength = (float)Math.sqrt((endX - startX) * (endX - startX) + (endY - startY) * (endY - startY));
		return Math.abs((pointX - startX) * (endY - startY) - (pointY - startY) * (endX - startX)) / normalLength;
	}

	/** Returns the distance between the given segment and point. */
	public static float distanceSegmentPoint (float startX, float startY, float endX, float endY, float pointX, float pointY) {
		return nearestSegmentPoint(startX, startY, endX, endY, pointX, pointY, v2tmp).dst(pointX, pointY);
	}

	/** Returns the distance between the given segment and point. */
	public static float distanceSegmentPoint (Vector2 start, Vector2 end, Vector2 point) {
		return nearestSegmentPoint(start, end, point, v2tmp).dst(point);
	}

	/** Returns a point on the segment nearest to the specified point. */
	public static Vector2 nearestSegmentPoint (Vector2 start, Vector2 end, Vector2 point, Vector2 nearest) {
		float length2 = start.dst2(end);
		if (length2 == 0) return nearest.set(start);
		float t = ((point.x - start.x) * (end.x - start.x) + (point.y - start.y) * (end.y - start.y)) / length2;
		if (t < 0) return nearest.set(start);
		if (t > 1) return nearest.set(end);
		return nearest.set(start.x + t * (end.x - start.x), start.y + t * (end.y - start.y));
	}

	/** Returns a point on the segment nearest to the specified point. */
	public static Vector2 nearestSegmentPoint (float startX, float startY, float endX, float endY, float pointX, float pointY,
		Vector2 nearest) {
		final float xDiff = endX - startX;
		final float yDiff = endY - startY;
		float length2 = xDiff * xDiff + yDiff * yDiff;
		if (length2 == 0) return nearest.set(startX, startY);
		float t = ((pointX - startX) * (endX - startX) + (pointY - startY) * (endY - startY)) / length2;
		if (t < 0) return nearest.set(startX, startY);
		if (t > 1) return nearest.set(endX, endY);
		return nearest.set(startX + t * (endX - startX), startY + t * (endY - startY));
	}

	/** Returns whether the given line segment intersects the given circle.
	 * @param start The start point of the line segment
	 * @param end The end point of the line segment
	 * @param center The center of the circle
	 * @param squareRadius The squared radius of the circle
	 * @return Whether the line segment and the circle intersect */
	public static boolean intersectSegmentCircle (Vector2 start, Vector2 end, Vector2 center, float squareRadius) {
		tmp.set(end.x - start.x, end.y - start.y, 0);
		tmp1.set(center.x - start.x, center.y - start.y, 0);
		float l = tmp.len();
		float u = tmp1.dot(tmp.nor());
		if (u <= 0) {
			tmp2.set(start.x, start.y, 0);
		} else if (u >= l) {
			tmp2.set(end.x, end.y, 0);
		} else {
			tmp3.set(tmp.scl(u)); // remember tmp is already normalized
			tmp2.set(tmp3.x + start.x, tmp3.y + start.y, 0);
		}

		float x = center.x - tmp2.x;
		float y = center.y - tmp2.y;

		return x * x + y * y <= squareRadius;
	}

	/** Checks whether the line segment and the circle intersect and returns by how much and in what direction the line has to move
	 * away from the circle to not intersect.
	 * 
	 * @param start The line segment starting point
	 * @param end The line segment end point
	 * @param point The center of the circle
	 * @param radius The radius of the circle
	 * @param displacement The displacement vector set by the method having unit length
	 * @return The displacement or Float.POSITIVE_INFINITY if no intersection is present */
	public static float intersectSegmentCircleDisplace (Vector2 start, Vector2 end, Vector2 point, float radius,
		Vector2 displacement) {
		float u = (point.x - start.x) * (end.x - start.x) + (point.y - start.y) * (end.y - start.y);
		float d = start.dst(end);
		u /= d * d;
		if (u < 0 || u > 1) return Float.POSITIVE_INFINITY;
		tmp.set(end.x, end.y, 0).sub(start.x, start.y, 0);
		tmp2.set(start.x, start.y, 0).add(tmp.scl(u));
		d = tmp2.dst(point.x, point.y, 0);
		if (d < radius) {
			displacement.set(point).sub(tmp2.x, tmp2.y).nor();
			return d;
		} else
			return Float.POSITIVE_INFINITY;
	}
	
	/** Intersect two 2D Rays and return the scalar parameter of the first ray at the intersection point.
	 * You can get the intersection point by: Vector2 point(direction1).scl(scalar).add(start1);
	 * For more information, check: http://stackoverflow.com/a/565282/1091440
	 * @param start1 Where the first ray start
	 * @param direction1 The direction the first ray is pointing
	 * @param start2 Where the second ray start
	 * @param direction2 The direction the second ray is pointing
	 * @return scalar parameter on the first ray describing the point where the intersection happens. May be negative.
	 * In case the rays are collinear, Float.POSITIVE_INFINITY will be returned.
	 */
	public static float intersectRayRay(Vector2 start1, Vector2 direction1, Vector2 start2, Vector2 direction2) {
		float difx = start2.x - start1.x;
		float dify = start2.y - start1.y;
		float d1xd2 = direction1.x * direction2.y - direction1.y * direction2.x;
		if(d1xd2 == 0.0f) {
			return Float.POSITIVE_INFINITY; //collinear
		}
		float d2sx = direction2.x / d1xd2;
		float d2sy = direction2.y / d1xd2;
		return difx * d2sy - dify * d2sx;
	}

	/** Intersects a {@link Ray} and a {@link Plane}. The intersection point is stored in intersection in case an intersection is
	 * present.
	 * 
	 * @param ray The ray
	 * @param plane The plane
	 * @param intersection The vector the intersection point is written to (optional)
	 * @return Whether an intersection is present. */
	public static boolean intersectRayPlane (Ray ray, Plane plane, Vector3 intersection) {
		float denom = ray.direction.dot(plane.getNormal());
		if (denom != 0) {
			float t = -(ray.origin.dot(plane.getNormal()) + plane.getD()) / denom;
			if (t < 0) return false;

			if (intersection != null) intersection.set(ray.origin).add(v0.set(ray.direction).scl(t));
			return true;
		} else if (plane.testPoint(ray.origin) == Plane.PlaneSide.OnPlane) {
			if (intersection != null) intersection.set(ray.origin);
			return true;
		} else
			return false;
	}

	/** Intersects a line and a plane. The intersection is returned as the distance from the first point to the plane. In case an
	 * intersection happened, the return value is in the range [0,1]. The intersection point can be recovered by point1 + t *
	 * (point2 - point1) where t is the return value of this method.
	 * @param x
	 * @param y
	 * @param z
	 * @param x2
	 * @param y2
	 * @param z2
	 * @param plane */
	public static float intersectLinePlane (float x, float y, float z, float x2, float y2, float z2, Plane plane,
		Vector3 intersection) {
		Vector3 direction = tmp.set(x2, y2, z2).sub(x, y, z);
		Vector3 origin = tmp2.set(x, y, z);
		float denom = direction.dot(plane.getNormal());
		if (denom != 0) {
			float t = -(origin.dot(plane.getNormal()) + plane.getD()) / denom;
			if (intersection != null) intersection.set(origin).add(direction.scl(t));
			return t;
		} else if (plane.testPoint(origin) == Plane.PlaneSide.OnPlane) {
			if (intersection != null) intersection.set(origin);
			return 0;
		}

		return -1;
	}

	private static final Plane p = new Plane(new Vector3(), 0);
	private static final Vector3 i = new Vector3();

	/** Intersect a {@link Ray} and a triangle, returning the intersection point in intersection.
	 * 
	 * @param ray The ray
	 * @param t1 The first vertex of the triangle
	 * @param t2 The second vertex of the triangle
	 * @param t3 The third vertex of the triangle
	 * @param intersection The intersection point (optional)
	 * @return True in case an intersection is present. */
	public static boolean intersectRayTriangle (Ray ray, Vector3 t1, Vector3 t2, Vector3 t3, Vector3 intersection) {
		Vector3 edge1 = v0.set(t2).sub(t1);
		Vector3 edge2 = v1.set(t3).sub(t1);
		
		Vector3 pvec = v2.set(ray.direction).crs(edge2);
		float det = edge1.dot(pvec);
		if (MathUtils.isZero(det)) {
			p.set(t1, t2, t3);
			if (p.testPoint(ray.origin) == PlaneSide.OnPlane && Intersector.isPointInTriangle(ray.origin, t1, t2, t3)) {
				if (intersection != null) intersection.set(ray.origin);
				return true;
			}
			return false;
		}
		
		det = 1.0f / det;

		Vector3 tvec = i.set(ray.origin).sub(t1);
		float u = tvec.dot(pvec) * det;
		if (u < 0.0f || u > 1.0f) return false;
		
		Vector3 qvec = tvec.crs(edge1);
		float v = ray.direction.dot(qvec) * det;
		if (v < 0.0f || u + v > 1.0f) return false;
		
		float t = edge2.dot(qvec) * det;
		if (t < 0) return false;
		
		if (intersection != null) {
			if (t <= MathUtils.FLOAT_ROUNDING_ERROR) {
				intersection.set(ray.origin);
			} else {
				ray.getEndPoint(intersection, t);
			}
		}
		
		return true;
	}

	private static final Vector3 dir = new Vector3();
	private static final Vector3 start = new Vector3();

	/** Intersects a {@link Ray} and a sphere, returning the intersection point in intersection.
	 * 
	 * @param ray The ray, the direction component must be normalized before calling this method
	 * @param center The center of the sphere
	 * @param radius The radius of the sphere
	 * @param intersection The intersection point (optional, can be null)
	 * @return Whether an intersection is present. */
	public static boolean intersectRaySphere (Ray ray, Vector3 center, float radius, Vector3 intersection) {
		final float len = ray.direction.dot(center.x - ray.origin.x, center.y - ray.origin.y, center.z - ray.origin.z);
		if (len < 0.f) // behind the ray
			return false;
		final float dst2 = center.dst2(ray.origin.x + ray.direction.x * len, ray.origin.y + ray.direction.y * len, ray.origin.z
			+ ray.direction.z * len);
		final float r2 = radius * radius;
		if (dst2 > r2) return false;
		if (intersection != null) intersection.set(ray.direction).scl(len - (float)Math.sqrt(r2 - dst2)).add(ray.origin);
		return true;
	}

	/** Intersects a {@link Ray} and a {@link BoundingBox}, returning the intersection point in intersection.
	 * This intersection is defined as the point on the ray closest to the origin which is within the specified
	 * bounds.
	 * 
	 * <p>The returned intersection (if any) is guaranteed to be within the bounds of the bounding box, but
	 * it can occasionally diverge slightly from ray, due to small floating-point errors.</p>
	 * 
	 * <p>If the origin of the ray is inside the box, this method returns true and the intersection point is
	 * set to the origin of the ray, accordingly to the definition above.</p>
	 * 
	 * @param ray The ray
	 * @param box The box
	 * @param intersection The intersection point (optional)
	 * @return Whether an intersection is present. */
	public static boolean intersectRayBounds (Ray ray, BoundingBox box, Vector3 intersection) {
		if (box.contains(ray.origin)) {
			if (intersection != null) intersection.set(ray.origin);
			return true;
		}
		float lowest = 0, t;
		boolean hit = false;

		// min x
		if (ray.origin.x <= box.min.x && ray.direction.x > 0) {
			t = (box.min.x - ray.origin.x) / ray.direction.x;
			if (t >= 0) {
				v2.set(ray.direction).scl(t).add(ray.origin);
				if (v2.y >= box.min.y && v2.y <= box.max.y && v2.z >= box.min.z && v2.z <= box.max.z && (!hit || t < lowest)) {
					hit = true;
					lowest = t;
				}
			}
		}
		// max x
		if (ray.origin.x >= box.max.x && ray.direction.x < 0) {
			t = (box.max.x - ray.origin.x) / ray.direction.x;
			if (t >= 0) {
				v2.set(ray.direction).scl(t).add(ray.origin);
				if (v2.y >= box.min.y && v2.y <= box.max.y && v2.z >= box.min.z && v2.z <= box.max.z && (!hit || t < lowest)) {
					hit = true;
					lowest = t;
				}
			}
		}
		// min y
		if (ray.origin.y <= box.min.y && ray.direction.y > 0) {
			t = (box.min.y - ray.origin.y) / ray.direction.y;
			if (t >= 0) {
				v2.set(ray.direction).scl(t).add(ray.origin);
				if (v2.x >= box.min.x && v2.x <= box.max.x && v2.z >= box.min.z && v2.z <= box.max.z && (!hit || t < lowest)) {
					hit = true;
					lowest = t;
				}
			}
		}
		// max y
		if (ray.origin.y >= box.max.y && ray.direction.y < 0) {
			t = (box.max.y - ray.origin.y) / ray.direction.y;
			if (t >= 0) {
				v2.set(ray.direction).scl(t).add(ray.origin);
				if (v2.x >= box.min.x && v2.x <= box.max.x && v2.z >= box.min.z && v2.z <= box.max.z && (!hit || t < lowest)) {
					hit = true;
					lowest = t;
				}
			}
		}
		// min z
		if (ray.origin.z <= box.min.z && ray.direction.z > 0) {
			t = (box.min.z - ray.origin.z) / ray.direction.z;
			if (t >= 0) {
				v2.set(ray.direction).scl(t).add(ray.origin);
				if (v2.x >= box.min.x && v2.x <= box.max.x && v2.y >= box.min.y && v2.y <= box.max.y && (!hit || t < lowest)) {
					hit = true;
					lowest = t;
				}
			}
		}
		// max y
		if (ray.origin.z >= box.max.z && ray.direction.z < 0) {
			t = (box.max.z - ray.origin.z) / ray.direction.z;
			if (t >= 0) {
				v2.set(ray.direction).scl(t).add(ray.origin);
				if (v2.x >= box.min.x && v2.x <= box.max.x && v2.y >= box.min.y && v2.y <= box.max.y && (!hit || t < lowest)) {
					hit = true;
					lowest = t;
				}
			}
		}
		if (hit && intersection != null) {
			intersection.set(ray.direction).scl(lowest).add(ray.origin);
			if (intersection.x < box.min.x) {
				intersection.x = box.min.x;
			} else if (intersection.x > box.max.x) {
				intersection.x = box.max.x;
			}
			if (intersection.y < box.min.y) {
				intersection.y = box.min.y;
			} else if (intersection.y > box.max.y) {
				intersection.y = box.max.y;
			}
			if (intersection.z < box.min.z) {
				intersection.z = box.min.z;
			} else if (intersection.z > box.max.z) {
				intersection.z = box.max.z;
			}
		}
		return hit;
	}

	/** Quick check whether the given {@link Ray} and {@link BoundingBox} intersect.
	 * 
	 * @param ray The ray
	 * @param box The bounding box
	 * @return Whether the ray and the bounding box intersect. */
	static public boolean intersectRayBoundsFast (Ray ray, BoundingBox box) {
		return intersectRayBoundsFast(ray, box.getCenter(tmp1), box.getDimensions(tmp2));
	}

	/** Quick check whether the given {@link Ray} and {@link BoundingBox} intersect.
	 * 
	 * @param ray The ray
	 * @param center The center of the bounding box
	 * @param dimensions The dimensions (width, height and depth) of the bounding box
	 * @return Whether the ray and the bounding box intersect. */
	static public boolean intersectRayBoundsFast (Ray ray, Vector3 center, Vector3 dimensions) {
		final float divX = 1f / ray.direction.x;
		final float divY = 1f / ray.direction.y;
		final float divZ = 1f / ray.direction.z;

		float minx = ((center.x - dimensions.x * .5f) - ray.origin.x) * divX;
		float maxx = ((center.x + dimensions.x * .5f) - ray.origin.x) * divX;
		if (minx > maxx) {
			final float t = minx;
			minx = maxx;
			maxx = t;
		}

		float miny = ((center.y - dimensions.y * .5f) - ray.origin.y) * divY;
		float maxy = ((center.y + dimensions.y * .5f) - ray.origin.y) * divY;
		if (miny > maxy) {
			final float t = miny;
			miny = maxy;
			maxy = t;
		}

		float minz = ((center.z - dimensions.z * .5f) - ray.origin.z) * divZ;
		float maxz = ((center.z + dimensions.z * .5f) - ray.origin.z) * divZ;
		if (minz > maxz) {
			final float t = minz;
			minz = maxz;
			maxz = t;
		}

		float min = Math.max(Math.max(minx, miny), minz);
		float max = Math.min(Math.min(maxx, maxy), maxz);

		return max >= 0 && max >= min;
	}

	static Vector3 best = new Vector3();
	static Vector3 tmp = new Vector3();
	static Vector3 tmp1 = new Vector3();
	static Vector3 tmp2 = new Vector3();
	static Vector3 tmp3 = new Vector3();
	static Vector2 v2tmp = new Vector2();

	/** Intersects the given ray with list of triangles. Returns the nearest intersection point in intersection
	 * 
	 * @param ray The ray
	 * @param triangles The triangles, each successive 3 elements from a vertex
	 * @param intersection The nearest intersection point (optional)
	 * @return Whether the ray and the triangles intersect. */
	public static boolean intersectRayTriangles (Ray ray, float[] triangles, Vector3 intersection) {
		float min_dist = Float.MAX_VALUE;
		boolean hit = false;

		if (triangles.length / 3 % 3 != 0) throw new RuntimeException("triangle list size is not a multiple of 3");

		for (int i = 0; i < triangles.length - 6; i += 9) {
			boolean result = intersectRayTriangle(ray, tmp1.set(triangles[i], triangles[i + 1], triangles[i + 2]),
				tmp2.set(triangles[i + 3], triangles[i + 4], triangles[i + 5]),
				tmp3.set(triangles[i + 6], triangles[i + 7], triangles[i + 8]), tmp);

			if (result == true) {
				float dist = ray.origin.dst2(tmp);
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

	/** Intersects the given ray with list of triangles. Returns the nearest intersection point in intersection
	 * 
	 * @param ray The ray
	 * @param vertices the vertices
	 * @param indices the indices, each successive 3 shorts index the 3 vertices of a triangle
	 * @param vertexSize the size of a vertex in floats
	 * @param intersection The nearest intersection point (optional)
	 * @return Whether the ray and the triangles intersect. */
	public static boolean intersectRayTriangles (Ray ray, float[] vertices, short[] indices, int vertexSize, Vector3 intersection) {
		float min_dist = Float.MAX_VALUE;
		boolean hit = false;

		if (indices.length % 3 != 0) throw new RuntimeException("triangle list size is not a multiple of 3");

		for (int i = 0; i < indices.length; i += 3) {
			int i1 = indices[i] * vertexSize;
			int i2 = indices[i + 1] * vertexSize;
			int i3 = indices[i + 2] * vertexSize;

			boolean result = intersectRayTriangle(ray, tmp1.set(vertices[i1], vertices[i1 + 1], vertices[i1 + 2]),
				tmp2.set(vertices[i2], vertices[i2 + 1], vertices[i2 + 2]),
				tmp3.set(vertices[i3], vertices[i3 + 1], vertices[i3 + 2]), tmp);

			if (result == true) {
				float dist = ray.origin.dst2(tmp);
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

	/** Intersects the given ray with list of triangles. Returns the nearest intersection point in intersection
	 * 
	 * @param ray The ray
	 * @param triangles The triangles
	 * @param intersection The nearest intersection point (optional)
	 * @return Whether the ray and the triangles intersect. */
	public static boolean intersectRayTriangles (Ray ray, List<Vector3> triangles, Vector3 intersection) {
		float min_dist = Float.MAX_VALUE;
		boolean hit = false;

		if (triangles.size() % 3 != 0) throw new RuntimeException("triangle list size is not a multiple of 3");

		for (int i = 0; i < triangles.size() - 2; i += 3) {
			boolean result = intersectRayTriangle(ray, triangles.get(i), triangles.get(i + 1), triangles.get(i + 2), tmp);

			if (result == true) {
				float dist = ray.origin.dst2(tmp);
				if (dist < min_dist) {
					min_dist = dist;
					best.set(tmp);
					hit = true;
				}
			}
		}

		if (!hit)
			return false;
		else {
			if (intersection != null) intersection.set(best);
			return true;
		}
	}

	/** Intersects the two lines and returns the intersection point in intersection.
	 * 
	 * @param p1 The first point of the first line
	 * @param p2 The second point of the first line
	 * @param p3 The first point of the second line
	 * @param p4 The second point of the second line
	 * @param intersection The intersection point
	 * @return Whether the two lines intersect */
	public static boolean intersectLines (Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4, Vector2 intersection) {
		float x1 = p1.x, y1 = p1.y, x2 = p2.x, y2 = p2.y, x3 = p3.x, y3 = p3.y, x4 = p4.x, y4 = p4.y;

		float d = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		if (d == 0) return false;

		if (intersection != null) {
			float ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / d;
			intersection.set(x1 + (x2 - x1) * ua, y1 + (y2 - y1) * ua);
		}
		return true;
	}

	/** Intersects the two lines and returns the intersection point in intersection.
	 * @param intersection The intersection point, or null.
	 * @return Whether the two lines intersect */
	public static boolean intersectLines (float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4,
		Vector2 intersection) {
		float d = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		if (d == 0) return false;

		if (intersection != null) {
			float ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / d;
			intersection.set(x1 + (x2 - x1) * ua, y1 + (y2 - y1) * ua);
		}
		return true;
	}

	/** Check whether the given line and {@link Polygon} intersect.
	 * @param p1 The first point of the line
	 * @param p2 The second point of the line
	 * @param polygon The polygon
	 * @return Whether polygon and line intersects */
	public static boolean intersectLinePolygon (Vector2 p1, Vector2 p2, Polygon polygon) {
		 float[] vertices = polygon.getTransformedVertices();
		 float x1 = p1.x, y1 = p1.y, x2 = p2.x, y2 = p2.y;
		 int n = vertices.length;
		 float x3 = vertices[n - 2], y3 = vertices[n - 1];
		 for (int i = 0; i < n; i += 2) {
			  float x4 = vertices[i], y4 = vertices[i + 1];
			  float d = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
			  if (d != 0) {
					float yd = y1 - y3;
					float xd = x1 - x3;
					float ua = ((x4 - x3) * yd - (y4 - y3) * xd) / d;
					if (ua >= 0 && ua <= 1) {
						 return true;
					}
			  }
			  x3 = x4;
			  y3 = y4;
		 }
		 return false;
	}

	/** Determines whether the given rectangles intersect and, if they do, sets the supplied {@code intersection} rectangle to the
	 * area of overlap.
	 * 
	 * @return Whether the rectangles intersect */
	static public boolean intersectRectangles (Rectangle rectangle1, Rectangle rectangle2, Rectangle intersection) {
		if (rectangle1.overlaps(rectangle2)) {
			intersection.x = Math.max(rectangle1.x, rectangle2.x);
			intersection.width = Math.min(rectangle1.x + rectangle1.width, rectangle2.x + rectangle2.width) - intersection.x;
			intersection.y = Math.max(rectangle1.y, rectangle2.y);
			intersection.height = Math.min(rectangle1.y + rectangle1.height, rectangle2.y + rectangle2.height) - intersection.y;
			return true;
		}
		return false;
	}

	 /** Check whether the given line segment and {@link Polygon} intersect.
	  * @param p1 The first point of the segment
	  * @param p2 The second point of the segment
	  * @return Whether polygon and segment intersect */
	 public static boolean intersectSegmentPolygon (Vector2 p1, Vector2 p2, Polygon polygon) {
		  float[] vertices = polygon.getTransformedVertices();
		  float x1 = p1.x, y1 = p1.y, x2 = p2.x, y2 = p2.y;
		  int n = vertices.length;
		  float x3 = vertices[n - 2], y3 = vertices[n - 1];
		  for (int i = 0; i < n; i += 2) {
				float x4 = vertices[i], y4 = vertices[i + 1];
				float d = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
				if (d != 0) {
					 float yd = y1 - y3;
					 float xd = x1 - x3;
					 float ua = ((x4 - x3) * yd - (y4 - y3) * xd) / d;
					 if (ua >= 0 && ua <= 1) {
						  float ub = ((x2 - x1) * yd - (y2 - y1) * xd) / d;
						  if (ub >= 0 && ub <= 1) {
								return true;
						  }
					 }
				}
				x3 = x4;
				y3 = y4;
		  }
		  return false;
	 }

	/** Intersects the two line segments and returns the intersection point in intersection.
	 * 
	 * @param p1 The first point of the first line segment
	 * @param p2 The second point of the first line segment
	 * @param p3 The first point of the second line segment
	 * @param p4 The second point of the second line segment
	 * @param intersection The intersection point (optional)
	 * @return Whether the two line segments intersect */
	public static boolean intersectSegments (Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4, Vector2 intersection) {
		float x1 = p1.x, y1 = p1.y, x2 = p2.x, y2 = p2.y, x3 = p3.x, y3 = p3.y, x4 = p4.x, y4 = p4.y;

		float d = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		if (d == 0) return false;

		float yd = y1 - y3;
		float xd = x1 - x3;
		float ua = ((x4 - x3) * yd - (y4 - y3) * xd) / d;
		if (ua < 0 || ua > 1) return false;

		float ub = ((x2 - x1) * yd - (y2 - y1) * xd) / d;
		if (ub < 0 || ub > 1) return false;

		if (intersection != null) intersection.set(x1 + (x2 - x1) * ua, y1 + (y2 - y1) * ua);
		return true;
	}

	public static boolean intersectSegments (float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4,
		Vector2 intersection) {
		float d = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		if (d == 0) return false;

		float yd = y1 - y3;
		float xd = x1 - x3;
		float ua = ((x4 - x3) * yd - (y4 - y3) * xd) / d;
		if (ua < 0 || ua > 1) return false;

		float ub = ((x2 - x1) * yd - (y2 - y1) * xd) / d;
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

	public static boolean overlaps (Circle c1, Circle c2) {
		return c1.overlaps(c2);
	}

	public static boolean overlaps (Rectangle r1, Rectangle r2) {
		return r1.overlaps(r2);
	}

	public static boolean overlaps (Circle c, Rectangle r) {
		float closestX = c.x;
		float closestY = c.y;

		if (c.x < r.x) {
			closestX = r.x;
		} else if (c.x > r.x + r.width) {
			closestX = r.x + r.width;
		}

		if (c.y < r.y) {
			closestY = r.y;
		} else if (c.y > r.y + r.height) {
			closestY = r.y + r.height;
		}

		closestX = closestX - c.x;
		closestX *= closestX;
		closestY = closestY - c.y;
		closestY *= closestY;

		return closestX + closestY < c.radius * c.radius;
	}

	/** Check whether specified counter-clockwise wound convex polygons overlap.
	 * 
	 * @param p1 The first polygon.
	 * @param p2 The second polygon.
	 * @return Whether polygons overlap. */
	public static boolean overlapConvexPolygons (Polygon p1, Polygon p2) {
		return overlapConvexPolygons(p1, p2, null);
	}

	/** Check whether specified counter-clockwise wound convex polygons overlap. If they do, optionally obtain a Minimum Translation Vector indicating the
	 * minimum magnitude vector required to push the polygon p1 out of collision with polygon p2.
	 * 
	 * @param p1 The first polygon.
	 * @param p2 The second polygon.
	 * @param mtv A Minimum Translation Vector to fill in the case of a collision, or null (optional).
	 * @return Whether polygons overlap. */
	public static boolean overlapConvexPolygons (Polygon p1, Polygon p2, MinimumTranslationVector mtv) {
		return overlapConvexPolygons(p1.getTransformedVertices(), p2.getTransformedVertices(), mtv);
	}

	/** @see #overlapConvexPolygons(float[], int, int, float[], int, int, MinimumTranslationVector) */
	public static boolean overlapConvexPolygons (float[] verts1, float[] verts2, MinimumTranslationVector mtv) {
		return overlapConvexPolygons(verts1, 0, verts1.length, verts2, 0, verts2.length, mtv);
	}

	/** Check whether polygons defined by the given counter-clockwise wound vertex arrays overlap. If they do, optionally obtain a Minimum Translation
	 * Vector indicating the minimum magnitude vector required to push the polygon defined by verts1 out of the collision with the polygon defined by verts2.
	 * 
	 * @param verts1 Vertices of the first polygon.
	 * @param verts2 Vertices of the second polygon.
	 * @param mtv A Minimum Translation Vector to fill in the case of a collision, or null (optional).
	 * @return Whether polygons overlap. */
	public static boolean overlapConvexPolygons (float[] verts1, int offset1, int count1, float[] verts2, int offset2, int count2,
		MinimumTranslationVector mtv) {
		float overlap = Float.MAX_VALUE;
		float smallestAxisX = 0;
		float smallestAxisY = 0;
		int numInNormalDir;

		int end1 = offset1 + count1;
		int end2 = offset2 + count2;

		// Get polygon1 axes
		for (int i = offset1; i < end1; i += 2) {
			float x1 = verts1[i];
			float y1 = verts1[i + 1];
			float x2 = verts1[(i + 2) % count1];
			float y2 = verts1[(i + 3) % count1];

			float axisX = y1 - y2;
			float axisY = -(x1 - x2);

			final float length = (float)Math.sqrt(axisX * axisX + axisY * axisY);
			axisX /= length;
			axisY /= length;

			// -- Begin check for separation on this axis --//

			// Project polygon1 onto this axis
			float min1 = axisX * verts1[0] + axisY * verts1[1];
			float max1 = min1;
			for (int j = offset1; j < end1; j += 2) {
				float p = axisX * verts1[j] + axisY * verts1[j + 1];
				if (p < min1) {
					min1 = p;
				} else if (p > max1) {
					max1 = p;
				}
			}

			// Project polygon2 onto this axis
			numInNormalDir = 0;
			float min2 = axisX * verts2[0] + axisY * verts2[1];
			float max2 = min2;
			for (int j = offset2; j < end2; j += 2) {
				// Counts the number of points that are within the projected area.
				numInNormalDir -= pointLineSide(x1, y1, x2, y2, verts2[j], verts2[j + 1]);
				float p = axisX * verts2[j] + axisY * verts2[j + 1];
				if (p < min2) {
					min2 = p;
				} else if (p > max2) {
					max2 = p;
				}
			}

			if (!(min1 <= min2 && max1 >= min2 || min2 <= min1 && max2 >= min1)) {
				return false;
			} else {
				float o = Math.min(max1, max2) - Math.max(min1, min2);
				if (min1 < min2 && max1 > max2 || min2 < min1 && max2 > max1) {
					float mins = Math.abs(min1 - min2);
					float maxs = Math.abs(max1 - max2);
					if (mins < maxs) {
						o += mins;
					} else {
						o += maxs;
					}
				}
				if (o < overlap) {
					overlap = o;
					// Adjusts the direction based on the number of points found
					smallestAxisX = numInNormalDir >= 0 ? axisX : -axisX;
					smallestAxisY = numInNormalDir >= 0 ? axisY : -axisY;
				}
			}
			// -- End check for separation on this axis --//
		}

		// Get polygon2 axes
		for (int i = offset2; i < end2; i += 2) {
			float x1 = verts2[i];
			float y1 = verts2[i + 1];
			float x2 = verts2[(i + 2) % count2];
			float y2 = verts2[(i + 3) % count2];

			float axisX = y1 - y2;
			float axisY = -(x1 - x2);

			final float length = (float)Math.sqrt(axisX * axisX + axisY * axisY);
			axisX /= length;
			axisY /= length;

			// -- Begin check for separation on this axis --//
			numInNormalDir = 0;

			// Project polygon1 onto this axis
			float min1 = axisX * verts1[0] + axisY * verts1[1];
			float max1 = min1;
			for (int j = offset1; j < end1; j += 2) {
				float p = axisX * verts1[j] + axisY * verts1[j + 1];
				// Counts the number of points that are within the projected area.
				numInNormalDir -= pointLineSide(x1, y1, x2, y2, verts1[j], verts1[j + 1]);
				if (p < min1) {
					min1 = p;
				} else if (p > max1) {
					max1 = p;
				}
			}

			// Project polygon2 onto this axis
			float min2 = axisX * verts2[0] + axisY * verts2[1];
			float max2 = min2;
			for (int j = offset2; j < end2; j += 2) {
				float p = axisX * verts2[j] + axisY * verts2[j + 1];
				if (p < min2) {
					min2 = p;
				} else if (p > max2) {
					max2 = p;
				}
			}

			if (!(min1 <= min2 && max1 >= min2 || min2 <= min1 && max2 >= min1)) {
				return false;
			} else {
				float o = Math.min(max1, max2) - Math.max(min1, min2);

				if (min1 < min2 && max1 > max2 || min2 < min1 && max2 > max1) {
					float mins = Math.abs(min1 - min2);
					float maxs = Math.abs(max1 - max2);
					if (mins < maxs) {
						o += mins;
					} else {
						o += maxs;
					}
				}

				if (o < overlap) {
					overlap = o;
					// Adjusts the direction based on the number of points found
					smallestAxisX = numInNormalDir < 0 ? axisX : -axisX;
					smallestAxisY = numInNormalDir < 0 ? axisY : -axisY;
				}
			}
			// -- End check for separation on this axis --//
		}
		if (mtv != null) {
			mtv.normal.set(smallestAxisX, smallestAxisY);
			mtv.depth = overlap;
		}
		return true;
	}

	/** Splits the triangle by the plane. The result is stored in the SplitTriangle instance. Depending on where the triangle is
	 * relative to the plane, the result can be:
	 * 
	 * <ul>
	 * <li>Triangle is fully in front/behind: {@link SplitTriangle#front} or {@link SplitTriangle#back} will contain the original
	 * triangle, {@link SplitTriangle#total} will be one.</li>
	 * <li>Triangle has two vertices in front, one behind: {@link SplitTriangle#front} contains 2 triangles,
	 * {@link SplitTriangle#back} contains 1 triangles, {@link SplitTriangle#total} will be 3.</li>
	 * <li>Triangle has one vertex in front, two behind: {@link SplitTriangle#front} contains 1 triangle,
	 * {@link SplitTriangle#back} contains 2 triangles, {@link SplitTriangle#total} will be 3.</li>
	 * </ul>
	 * 
	 * The input triangle should have the form: x, y, z, x2, y2, z2, x3, y3, z3. One can add additional attributes per vertex which
	 * will be interpolated if split, such as texture coordinates or normals. Note that these additional attributes won't be
	 * normalized, as might be necessary in case of normals.
	 * 
	 * @param triangle
	 * @param plane
	 * @param split output SplitTriangle */
	public static void splitTriangle (float[] triangle, Plane plane, SplitTriangle split) {
		int stride = triangle.length / 3;
		boolean r1 = plane.testPoint(triangle[0], triangle[1], triangle[2]) == PlaneSide.Back;
		boolean r2 = plane.testPoint(triangle[0 + stride], triangle[1 + stride], triangle[2 + stride]) == PlaneSide.Back;
		boolean r3 = plane.testPoint(triangle[0 + stride * 2], triangle[1 + stride * 2], triangle[2 + stride * 2]) == PlaneSide.Back;

		split.reset();

		// easy case, triangle is on one side (point on plane means front).
		if (r1 == r2 && r2 == r3) {
			split.total = 1;
			if (r1) {
				split.numBack = 1;
				System.arraycopy(triangle, 0, split.back, 0, triangle.length);
			} else {
				split.numFront = 1;
				System.arraycopy(triangle, 0, split.front, 0, triangle.length);
			}
			return;
		}

		// set number of triangles
		split.total = 3;
		split.numFront = (r1 ? 0 : 1) + (r2 ? 0 : 1) + (r3 ? 0 : 1);
		split.numBack = split.total - split.numFront;

		// hard case, split the three edges on the plane
		// determine which array to fill first, front or back, flip if we
		// cross the plane
		split.setSide(!r1);

		// split first edge
		int first = 0;
		int second = stride;
		if (r1 != r2) {
			// split the edge
			splitEdge(triangle, first, second, stride, plane, split.edgeSplit, 0);

			// add first edge vertex and new vertex to current side
			split.add(triangle, first, stride);
			split.add(split.edgeSplit, 0, stride);

			// flip side and add new vertex and second edge vertex to current side
			split.setSide(!split.getSide());
			split.add(split.edgeSplit, 0, stride);
		} else {
			// add both vertices
			split.add(triangle, first, stride);
		}

		// split second edge
		first = stride;
		second = stride + stride;
		if (r2 != r3) {
			// split the edge
			splitEdge(triangle, first, second, stride, plane, split.edgeSplit, 0);

			// add first edge vertex and new vertex to current side
			split.add(triangle, first, stride);
			split.add(split.edgeSplit, 0, stride);

			// flip side and add new vertex and second edge vertex to current side
			split.setSide(!split.getSide());
			split.add(split.edgeSplit, 0, stride);
		} else {
			// add both vertices
			split.add(triangle, first, stride);
		}

		// split third edge
		first = stride + stride;
		second = 0;
		if (r3 != r1) {
			// split the edge
			splitEdge(triangle, first, second, stride, plane, split.edgeSplit, 0);

			// add first edge vertex and new vertex to current side
			split.add(triangle, first, stride);
			split.add(split.edgeSplit, 0, stride);

			// flip side and add new vertex and second edge vertex to current side
			split.setSide(!split.getSide());
			split.add(split.edgeSplit, 0, stride);
		} else {
			// add both vertices
			split.add(triangle, first, stride);
		}

		// triangulate the side with 2 triangles
		if (split.numFront == 2) {
			System.arraycopy(split.front, stride * 2, split.front, stride * 3, stride * 2);
			System.arraycopy(split.front, 0, split.front, stride * 5, stride);
		} else {
			System.arraycopy(split.back, stride * 2, split.back, stride * 3, stride * 2);
			System.arraycopy(split.back, 0, split.back, stride * 5, stride);
		}
	}

	static Vector3 intersection = new Vector3();

	private static void splitEdge (float[] vertices, int s, int e, int stride, Plane plane, float[] split, int offset) {
		float t = Intersector.intersectLinePlane(vertices[s], vertices[s + 1], vertices[s + 2], vertices[e], vertices[e + 1],
			vertices[e + 2], plane, intersection);
		split[offset + 0] = intersection.x;
		split[offset + 1] = intersection.y;
		split[offset + 2] = intersection.z;
		for (int i = 3; i < stride; i++) {
			float a = vertices[s + i];
			float b = vertices[e + i];
			split[offset + i] = a + t * (b - a);
		}
	}
	
	public static class SplitTriangle {
		public float[] front;
		public float[] back;
		float[] edgeSplit;
		public int numFront;
		public int numBack;
		public int total;
		boolean frontCurrent = false;
		int frontOffset = 0;
		int backOffset = 0;

		/** Creates a new instance, assuming numAttributes attributes per triangle vertex.
		 * @param numAttributes must be >= 3 */
		public SplitTriangle (int numAttributes) {
			front = new float[numAttributes * 3 * 2];
			back = new float[numAttributes * 3 * 2];
			edgeSplit = new float[numAttributes];
		}

		@Override
		public String toString () {
			return "SplitTriangle [front=" + Arrays.toString(front) + ", back=" + Arrays.toString(back) + ", numFront=" + numFront
				+ ", numBack=" + numBack + ", total=" + total + "]";
		}

		void setSide (boolean front) {
			frontCurrent = front;
		}

		boolean getSide () {
			return frontCurrent;
		}

		void add (float[] vertex, int offset, int stride) {
			if (frontCurrent) {
				System.arraycopy(vertex, offset, front, frontOffset, stride);
				frontOffset += stride;
			} else {
				System.arraycopy(vertex, offset, back, backOffset, stride);
				backOffset += stride;
			}
		}

		void reset () {
			frontCurrent = false;
			frontOffset = 0;
			backOffset = 0;
			numFront = 0;
			numBack = 0;
			total = 0;
		}
	}

	/**
	 * Minimum translation required to separate two polygons.
	 */
	public static class MinimumTranslationVector {
		/** Unit length vector that indicates the direction for the separation */
		public Vector2 normal = new Vector2();
		/** Distance of the translation required for the separation */
		public float depth = 0;
	}
}
