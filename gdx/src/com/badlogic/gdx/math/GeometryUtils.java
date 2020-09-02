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

/** @author Nathan Sweet */
public final class GeometryUtils {
	static private final Vector2 tmp1 = new Vector2(), tmp2 = new Vector2(), tmp3 = new Vector2();

	/** Computes the barycentric coordinates v,w for the specified point in the triangle.
	 * <p>
	 * If barycentric.x >= 0 && barycentric.y >= 0 && barycentric.x + barycentric.y <= 1 then the point is inside the triangle.
	 * <p>
	 * If vertices a,b,c have values aa,bb,cc then to get an interpolated value at point p:
	 * 
	 * <pre>
	 * GeometryUtils.barycentric(p, a, b, c, barycentric);
	 * float u = 1.f - barycentric.x - barycentric.y;
	 * float x = u * aa.x + barycentric.x * bb.x + barycentric.y * cc.x;
	 * float y = u * aa.y + barycentric.x * bb.y + barycentric.y * cc.y;
	 * </pre>
	 * 
	 * @return barycentricOut */
	static public Vector2 toBarycoord (Vector2 p, Vector2 a, Vector2 b, Vector2 c, Vector2 barycentricOut) {
		Vector2 v0 = tmp1.set(b).sub(a);
		Vector2 v1 = tmp2.set(c).sub(a);
		Vector2 v2 = tmp3.set(p).sub(a);
		float d00 = v0.dot(v0);
		float d01 = v0.dot(v1);
		float d11 = v1.dot(v1);
		float d20 = v2.dot(v0);
		float d21 = v2.dot(v1);
		float denom = d00 * d11 - d01 * d01;
		barycentricOut.x = (d11 * d20 - d01 * d21) / denom;
		barycentricOut.y = (d00 * d21 - d01 * d20) / denom;
		return barycentricOut;
	}

	/** Returns true if the barycentric coordinates are inside the triangle. */
	static public boolean barycoordInsideTriangle (Vector2 barycentric) {
		return barycentric.x >= 0 && barycentric.y >= 0 && barycentric.x + barycentric.y <= 1;
	}

	/** Returns interpolated values given the barycentric coordinates of a point in a triangle and the values at each vertex.
	 * @return interpolatedOut */
	static public Vector2 fromBarycoord (Vector2 barycentric, Vector2 a, Vector2 b, Vector2 c, Vector2 interpolatedOut) {
		float u = 1 - barycentric.x - barycentric.y;
		interpolatedOut.x = u * a.x + barycentric.x * b.x + barycentric.y * c.x;
		interpolatedOut.y = u * a.y + barycentric.x * b.y + barycentric.y * c.y;
		return interpolatedOut;
	}

	/** Returns an interpolated value given the barycentric coordinates of a point in a triangle and the values at each vertex.
	 * @return interpolatedOut */
	static public float fromBarycoord (Vector2 barycentric, float a, float b, float c) {
		float u = 1 - barycentric.x - barycentric.y;
		return u * a + barycentric.x * b + barycentric.y * c;
	}

	/** Returns the lowest positive root of the quadric equation given by a* x * x + b * x + c = 0. If no solution is given
	 * Float.Nan is returned.
	 * @param a the first coefficient of the quadric equation
	 * @param b the second coefficient of the quadric equation
	 * @param c the third coefficient of the quadric equation
	 * @return the lowest positive root or Float.Nan */
	static public float lowestPositiveRoot (float a, float b, float c) {
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

	static public boolean colinear (float x1, float y1, float x2, float y2, float x3, float y3) {
		float dx21 = x2 - x1, dy21 = y2 - y1;
		float dx32 = x3 - x2, dy32 = y3 - y2;
		float det = dx32 * dy21 - dx21 * dy32;
		return Math.abs(det) < MathUtils.FLOAT_ROUNDING_ERROR;
	}

	static public Vector2 triangleCentroid (float x1, float y1, float x2, float y2, float x3, float y3, Vector2 centroid) {
		centroid.x = (x1 + x2 + x3) / 3;
		centroid.y = (y1 + y2 + y3) / 3;
		return centroid;
	}

	/** Returns the circumcenter of the triangle. The input points must not be colinear. */
	static public Vector2 triangleCircumcenter (float x1, float y1, float x2, float y2, float x3, float y3, Vector2 circumcenter) {
		float dx21 = x2 - x1, dy21 = y2 - y1;
		float dx32 = x3 - x2, dy32 = y3 - y2;
		float dx13 = x1 - x3, dy13 = y1 - y3;
		float det = dx32 * dy21 - dx21 * dy32;
		if (Math.abs(det) < MathUtils.FLOAT_ROUNDING_ERROR)
			throw new IllegalArgumentException("Triangle points must not be colinear.");
		det *= 2;
		float sqr1 = x1 * x1 + y1 * y1, sqr2 = x2 * x2 + y2 * y2, sqr3 = x3 * x3 + y3 * y3;
		circumcenter.set((sqr1 * dy32 + sqr2 * dy13 + sqr3 * dy21) / det, -(sqr1 * dx32 + sqr2 * dx13 + sqr3 * dx21) / det);
		return circumcenter;
	}

	static public float triangleCircumradius (float x1, float y1, float x2, float y2, float x3, float y3) {
		float m1, m2, mx1, mx2, my1, my2, x, y;
		if (Math.abs(y2 - y1) < MathUtils.FLOAT_ROUNDING_ERROR) {
			m2 = -(x3 - x2) / (y3 - y2);
			mx2 = (x2 + x3) / 2;
			my2 = (y2 + y3) / 2;
			x = (x2 + x1) / 2;
			y = m2 * (x - mx2) + my2;
		} else if (Math.abs(y3 - y2) < MathUtils.FLOAT_ROUNDING_ERROR) {
			m1 = -(x2 - x1) / (y2 - y1);
			mx1 = (x1 + x2) / 2;
			my1 = (y1 + y2) / 2;
			x = (x3 + x2) / 2;
			y = m1 * (x - mx1) + my1;
		} else {
			m1 = -(x2 - x1) / (y2 - y1);
			m2 = -(x3 - x2) / (y3 - y2);
			mx1 = (x1 + x2) / 2;
			mx2 = (x2 + x3) / 2;
			my1 = (y1 + y2) / 2;
			my2 = (y2 + y3) / 2;
			x = (m1 * mx1 - m2 * mx2 + my2 - my1) / (m1 - m2);
			y = m1 * (x - mx1) + my1;
		}
		float dx = x1 - x, dy = y1 - y;
		return (float)Math.sqrt(dx * dx + dy * dy);
	}

	/** Ratio of circumradius to shortest edge as a measure of triangle quality.
	 * <p>
	 * Gary L. Miller, Dafna Talmor, Shang-Hua Teng, and Noel Walkington. A Delaunay Based Numerical Method for Three Dimensions:
	 * Generation, Formulation, and Partition. */
	static public float triangleQuality (float x1, float y1, float x2, float y2, float x3, float y3) {
		float length1 = (float)Math.sqrt(x1 * x1 + y1 * y1);
		float length2 = (float)Math.sqrt(x2 * x2 + y2 * y2);
		float length3 = (float)Math.sqrt(x3 * x3 + y3 * y3);
		return Math.min(length1, Math.min(length2, length3)) / triangleCircumradius(x1, y1, x2, y2, x3, y3);
	}

	static public float triangleArea (float x1, float y1, float x2, float y2, float x3, float y3) {
		return Math.abs((x1 - x3) * (y2 - y1) - (x1 - x2) * (y3 - y1)) * 0.5f;
	}

	static public Vector2 quadrilateralCentroid (float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4,
		Vector2 centroid) {
		float avgX1 = (x1 + x2 + x3) / 3;
		float avgY1 = (y1 + y2 + y3) / 3;
		float avgX2 = (x1 + x4 + x3) / 3;
		float avgY2 = (y1 + y4 + y3) / 3;
		centroid.x = avgX1 - (avgX1 - avgX2) / 2;
		centroid.y = avgY1 - (avgY1 - avgY2) / 2;
		return centroid;
	}

	/** Returns the centroid for the specified non-self-intersecting polygon. */
	static public Vector2 polygonCentroid (float[] polygon, int offset, int count, Vector2 centroid) {
		if (count < 6) throw new IllegalArgumentException("A polygon must have 3 or more coordinate pairs.");

		float area = 0, x = 0, y = 0;
		int last = offset + count - 2;
		float x1 = polygon[last], y1 = polygon[last + 1];
		for (int i = offset; i <= last; i += 2) {
			float x2 = polygon[i], y2 = polygon[i + 1];
			float a = x1 * y2 - x2 * y1;
			area += a;
			x += (x1 + x2) * a;
			y += (y1 + y2) * a;
			x1 = x2;
			y1 = y2;
		}
		if (area == 0) {
			centroid.x = 0;
			centroid.y = 0;
		} else {
			area *= 0.5f;
			centroid.x = x / (6 * area);
			centroid.y = y / (6 * area);
		}
		return centroid;
	}

	/** Computes the area for a convex polygon. */
	static public float polygonArea (float[] polygon, int offset, int count) {
		float area = 0;
		int last = offset + count - 2;
		float x1 = polygon[last], y1 = polygon[last + 1];
		for (int i = offset; i <= last; i += 2) {
			float x2 = polygon[i], y2 = polygon[i + 1];
			area += x1 * y2 - x2 * y1;
			x1 = x2;
			y1 = y2;
		}
		return area * 0.5f;
	}

	static public void ensureCCW (float[] polygon) {
		ensureCCW(polygon, 0, polygon.length);
	}

	static public void ensureCCW (float[] polygon, int offset, int count) {
		if (!isClockwise(polygon, offset, count)) return;
		int lastX = offset + count - 2;
		for (int i = offset, n = offset + count / 2; i < n; i += 2) {
			int other = lastX - i;
			float x = polygon[i];
			float y = polygon[i + 1];
			polygon[i] = polygon[other];
			polygon[i + 1] = polygon[other + 1];
			polygon[other] = x;
			polygon[other + 1] = y;
		}
	}

	static public boolean isClockwise (float[] polygon, int offset, int count) {
		if (count <= 2) return false;
		float area = 0;
		int last = offset + count - 2;
		float x1 = polygon[last], y1 = polygon[last + 1];
		for (int i = offset; i <= last; i += 2) {
			float x2 = polygon[i], y2 = polygon[i + 1];
			area += x1 * y2 - x2 * y1;
			x1 = x2;
			y1 = y2;
		}
		return area < 0;
	}
}
