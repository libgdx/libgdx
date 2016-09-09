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
		float x = 0, y = 0;

		float signedArea = 0;
		int i = offset;
		for (int n = offset + count - 2; i < n; i += 2) {
			float x0 = polygon[i];
			float y0 = polygon[i + 1];
			float x1 = polygon[i + 2];
			float y1 = polygon[i + 3];
			float a = x0 * y1 - x1 * y0;
			signedArea += a;
			x += (x0 + x1) * a;
			y += (y0 + y1) * a;
		}

		float x0 = polygon[i];
		float y0 = polygon[i + 1];
		float x1 = polygon[offset];
		float y1 = polygon[offset + 1];
		float a = x0 * y1 - x1 * y0;
		signedArea += a;
		x += (x0 + x1) * a;
		y += (y0 + y1) * a;

		if (signedArea == 0) {
			centroid.x = 0;
			centroid.y = 0;
		} else {
			signedArea *= 0.5f;
			centroid.x = x / (6 * signedArea);
			centroid.y = y / (6 * signedArea);
		}
		return centroid;
	}

	/** Computes the area for a convex polygon. */
	static public float polygonArea (float[] polygon, int offset, int count) {
		float area = 0;
		for (int i = offset, n = offset + count; i < n; i += 2) {
			int x1 = i;
			int y1 = i + 1;
			int x2 = (i + 2) % n;
			if(x2 < offset)
				x2 += offset;
			int y2 = (i + 3) % n;
			if(y2 < offset)
				y2 += offset;
			area += polygon[x1] * polygon[y2];
			area -= polygon[x2] * polygon[y1];
		}
		area *= 0.5f;
		return area;
	}

	static public void ensureCCW (float[] polygon) {
		if (!areVerticesClockwise(polygon, 0, polygon.length)) return;
		int lastX = polygon.length - 2;
		for (int i = 0, n = polygon.length / 2; i < n; i += 2) {
			int other = lastX - i;
			float x = polygon[i];
			float y = polygon[i + 1];
			polygon[i] = polygon[other];
			polygon[i + 1] = polygon[other + 1];
			polygon[other] = x;
			polygon[other + 1] = y;
		}
	}

	static private boolean areVerticesClockwise (float[] polygon, int offset, int count) {
		if (count <= 2) return false;
		float area = 0, p1x, p1y, p2x, p2y;
		for (int i = offset, n = offset + count - 3; i < n; i += 2) {
			p1x = polygon[i];
			p1y = polygon[i + 1];
			p2x = polygon[i + 2];
			p2y = polygon[i + 3];
			area += p1x * p2y - p2x * p1y;
		}
		p1x = polygon[count - 2];
		p1y = polygon[count - 1];
		p2x = polygon[0];
		p2y = polygon[1];
		return area + p1x * p2y - p2x * p1y < 0;
	}
}
