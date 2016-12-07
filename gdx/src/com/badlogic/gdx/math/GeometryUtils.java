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

import com.badlogic.gdx.utils.Pool.Poolable;

/** @author Nathan Sweet */
public final class GeometryUtils {
	static private final Vector2 tmp1 = new Vector2(), tmp2 = new Vector2(), tmp3 = new Vector2();
	static private final Vector3 vec = new Vector3();
	
	/** caches the values for a triangle for faster barycentric calculations  */
	public static class BaryCache{		
		float v0X, v0Y, v0Z;
		float v1X, v1Y, v1Z;
		float d00, d01, d11;
		float x1, y1,z1;
		float invDenom;
		public BaryCache(){		
		}
		public BaryCache(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3){
			set(x1, y1, z1, x2, y2, z2, x3, y3, z3);
		}
		public BaryCache(BaryCache cache){
			set(cache);
		}
		public BaryCache set(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3){
			v0X = x2-x1;
			v0Y = y2-y1;
			v0Z = z2-z1;					
			v1X = x3-x1;
			v1Y = y3-y1;
			v1Z = z3-z1;
			d00 = Vector3.dot(v0X, v0Y, v0Z, v0X, v0Y, v0Z);
			d01 = Vector3.dot(v0X, v0Y, v0Z, v1X, v1Y, v1Z);
			d11 = Vector3.dot(v1X, v1Y, v1Z, v1X, v1Y, v1Z);			
			invDenom = 1f / (d00 * d11 - d01 * d01);	
			return this;
		}
		public BaryCache set(BaryCache cache){
			v0X = cache.v0X;
			v0Y = cache.v0Y;
			v0Z = cache.v0Z;				
			v1X = cache.v1X;
			v1Y = cache.v1Y;
			v1Z = cache.v1Z;
			d00 = cache.d00;
			d01 = cache.d01;
			d11 = cache.d11;			
			invDenom = cache.invDenom;
			return this;
		}
	}
	
	static public boolean barycoordInsideTriangle(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x, float y, float z){
		Vector3 vec = toBarycoord(x1, y1, z1, x2, y2, z2, x3, y3, z3, x, y, z, GeometryUtils.vec);
		return vec.x >= 0 && vec.y >= 0 && vec.z >= 0;
	}
	
	static public boolean barycoordInsideTriangle(Vector3 p1, Vector3 p2, Vector3 p3, Vector3 p){
		return barycoordInsideTriangle(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, p3.x, p3.y, p3.z, p.x, p.y, p.z);
	}
	
	/**
	 * Calculates the barycentric coordinates of a 3d triangle as described in Realtime Collision Detection by Christen Ericson on pages 46-48 (http://realtimecollisiondetection.net/)
	 * @param p the point to be checked
	 * @return barycentricOut
	 */
	static public Vector3 toBarycoord(Vector3 p1, Vector3 p2, Vector3 p3, Vector3 p, Vector3 barycentricOut){
		return toBarycoord(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, p3.x, p3.y, p3.z, p.x, p.y, p.z, barycentricOut);
	}

	/**
	 * Calculates the barycentric coordinates of a 3d triangle as described in Realtime Collision Detection by Christen Ericson on pages 46-48 (http://realtimecollisiondetection.net/)
	 * @return barycentricOut
	 */
	static public Vector3 toBarycoord(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x, float y, float z, Vector3 barycentricOut){
		float v0X = x2-x1;
		float v0Y = y2-y1;
		float v0Z = z2-z1;
				
		float v1X = x3-x1;
		float v1Y = y3-y1;
		float v1Z = z3-z1;
				
		float v2X = x-x1;
		float v2Y = y-y1;
		float v2Z = z-z1;
				
		float d00 = Vector3.dot(v0X, v0Y, v0Z, v0X, v0Y, v0Z);
		float d01 = Vector3.dot(v0X, v0Y, v0Z, v1X, v1Y, v1Z);
		float d11 = Vector3.dot(v1X, v1Y, v1Z, v1X, v1Y, v1Z);
		float d20 = Vector3.dot(v2X, v2Y, v2Z, v0X, v0Y, v0Z);
		float d21 = Vector3.dot(v2X, v2Y, v2Z, v1X, v1Y, v1Z);
			    
		float invDenom = 1f / (d00 * d11 - d01 * d01);
		barycentricOut.x = (d11 * d20 - d01 * d21) * invDenom;
		barycentricOut.y = (d00 * d21 - d01 * d20) *  invDenom;
		barycentricOut.z = 1.0f - barycentricOut.x - barycentricOut.y;
		return barycentricOut;
	}
	
	static public boolean barycoordInsideTriangle(BaryCache cache, float x, float y, float z){
		Vector3 vec = toBarycoord(cache, x, y, z, GeometryUtils.vec);
		return vec.x >= 0 && vec.y >= 0 && vec.z >= 0;
	}
	
	static public boolean barycoordInsideTriangle(BaryCache cache, Vector3 p){
		return barycoordInsideTriangle(cache, p.x, p.y, p.z);
	}
	
	/**
	 * Use a {@link BaryCache} for calculations with increased speed.
	 * @param cache 
	 * @param x coordinate to be checked
	 * @param y coordinate to be checked
	 * @param z coordinate to be checked
	 * @return barycentricOut
	 */
	static public Vector3 toBarycoord(BaryCache cache, float x, float y, float z, Vector3 barycentricOut){
		float v2X = x-cache.x1;
		float v2Y = y-cache.y1;
		float v2Z = z-cache.z1;
		
		float d20 = Vector3.dot(v2X, v2Y, v2Z, cache.v0X, cache.v0Y, cache.v0Z);
		float d21 = Vector3.dot(v2X, v2Y, v2Z, cache.v1X, cache.v1Y, cache.v1Z);
		
		barycentricOut.x = (cache.d11 * d20 - cache.d01 * d21) * cache.invDenom;
		barycentricOut.y = (cache.d00 * d21 - cache.d01 * d20) * cache.invDenom;
		barycentricOut.z = 1.0f - barycentricOut.x - barycentricOut.y;
		return barycentricOut;
	}

	/** Computes the barycentric coordinates v,w for the specified point in the 2d triangle.
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
		tmp1.set(b).sub(a);
		tmp2.set(c).sub(a);
		tmp3.set(p).sub(a);
		float invDenom = 1f / (tmp1.x * tmp2.y - tmp2.x * tmp1.y);	
		barycentricOut.x = (tmp3.x * tmp2.y - tmp2.x * tmp3.y) * invDenom;
		barycentricOut.y = (tmp1.x * tmp3.y - tmp3.x * tmp1.y) * invDenom;
		return barycentricOut;
	}

	/** Returns true if the barycentric coordinates are inside the 2d triangle. */
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
			if (x2 < offset) x2 += offset;
			int y2 = (i + 3) % n;
			if (y2 < offset) y2 += offset;
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
