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

import com.badlogic.gdx.utils.*;

/** Delaunay triangulation. Adapted from Paul Bourke's triangulate: http://paulbourke.net/papers/triangulate/
 * @author Nathan Sweet
 * @author Berstanio */
public class DelaunayTriangulator {
	static private final int INSIDE = 0;
	static private final int COMPLETE = 1;
	static private final int INCOMPLETE = 2;

	private final IntArray quicksortStack = new IntArray();
	private float[] sortedPoints;
	private final ShortArray triangles = new ShortArray(false, 16);
	private final ShortArray completedTriangles = new ShortArray(false, 16);
	private final ShortArray originalIndices = new ShortArray(false, 0);
	private final IntArray edges = new IntArray();
	private final Vector2 centroid = new Vector2();
	private final ShewchukExactPredicates predicates = new ShewchukExactPredicates();

	/** @see #computeTriangles(float[], int, int, boolean) */
	public ShortArray computeTriangles (FloatArray points, boolean sorted) {
		return computeTriangles(points.items, 0, points.size, sorted);
	}

	/** @see #computeTriangles(float[], int, int, boolean) */
	public ShortArray computeTriangles (float[] polygon, boolean sorted) {
		return computeTriangles(polygon, 0, polygon.length, sorted);
	}

	/** Triangulates the given point cloud to a list of triangle indices that make up the Delaunay triangulation.
	 * @param points x,y pairs describing points. Duplicate points will result in undefined behavior.
	 * @param sorted If false, the points will be sorted by the x coordinate, which is required by the triangulation algorithm. If
	 *           sorting is done the input array is not modified, the returned indices are for the input array, and count*2
	 *           additional working memory is needed.
	 * @return triples of indices into the points that describe the triangles in clockwise order. Note the returned array is reused
	 *         for later calls to the same method. */
	public ShortArray computeTriangles (float[] points, int offset, int count, boolean sorted) {
		if (count > 32767) throw new IllegalArgumentException("count must be <= " + 32767);
		ShortArray completedTriangles = this.completedTriangles;
		completedTriangles.clear();
		if (count < 6) return completedTriangles;
		ShortArray triangles = this.triangles;
		triangles.clear();
		int maxMeshSize = (count + 1) * 3;
		triangles.ensureCapacity(maxMeshSize);
		completedTriangles.ensureCapacity(maxMeshSize);

		if (!sorted) {
			if (sortedPoints == null || sortedPoints.length < count) sortedPoints = new float[count];
			System.arraycopy(points, offset, sortedPoints, 0, count);
			points = sortedPoints;
			offset = 0;
			sort(points, count);
		}

		int end = offset + count;

		IntArray edges = this.edges;
		edges.ensureCapacity(count);

		// Add the super triangle. Vertices are encoded by index only, their coordinates are conceptually at infinity:
		// end + 0 -> v_L (x = -inf), end + 2 -> v_R (x = +inf), end + 4 -> v_T (y = +inf). The super triangle needs to be CW.
		triangles.add((short)end, (short)(end + 4), (short)(end + 2));

		// Include each point one at a time into the existing mesh.
		for (int pointIndex = offset; pointIndex < end; pointIndex += 2) {
			float x = points[pointIndex], y = points[pointIndex + 1];

			// If x,y lies inside the circumcircle of a triangle, the edges are stored and the triangle removed.
			short[] trianglesArray = triangles.items;
			for (int triangleIndex = triangles.size - 1; triangleIndex >= 0; triangleIndex -= 3) {
				int p1 = trianglesArray[triangleIndex - 2];
				int p2 = trianglesArray[triangleIndex - 1];
				int p3 = trianglesArray[triangleIndex];
				switch (circumCircle(points, end, x, y, p1, p2, p3)) {
				case COMPLETE:
					completedTriangles.add((short)p1, (short)p2, (short)p3);
					triangles.removeRange(triangleIndex - 2, triangleIndex);
					break;
				case INSIDE:
					edges.add(p1, p2, p2, p3);
					edges.add(p3, p1);

					triangles.removeRange(triangleIndex - 2, triangleIndex);
					break;
				}
			}

			int[] edgesArray = edges.items;
			for (int i = 0, n = edges.size; i < n; i += 2) {
				// Skip multiple edges. If all triangles have the same winding then all interior edges point in opposite directions.
				int p1 = edgesArray[i];
				if (p1 == -1) continue;
				int p2 = edgesArray[i + 1];
				boolean skip = false;
				for (int ii = i + 2; ii < n; ii += 2) {
					if (p1 == edgesArray[ii + 1] && p2 == edgesArray[ii]) {
						skip = true;
						edgesArray[ii] = -1;
					}
				}
				if (skip) continue;

				// Form new triangles for the current point. Edges are arranged in clockwise order.
				triangles.add((short)p1, (short)p2, (short)pointIndex);
			}
			edges.clear();
		}

		short[] trianglesArray = triangles.items;
		// Copy remaining triangles that are not super. By construction, completedTriangles does not contain any super vertices.
		for (int remaining = 0; remaining < triangles.size; remaining += 3) {
			int p1 = trianglesArray[remaining];
			int p2 = trianglesArray[remaining + 1];
			int p3 = trianglesArray[remaining + 2];

			if (p1 >= end || p2 >= end || p3 >= end) continue;

			completedTriangles.add((short)p1, (short)p2, (short)p3);
		}

		short[] completedTrianglesArray = completedTriangles.items;
		int n = completedTriangles.size;

		// Convert sorted to unsorted indices.
		if (!sorted) {
			short[] originalIndicesArray = originalIndices.items;
			for (int i = 0; i < n; i++)
				completedTrianglesArray[i] = (short)(originalIndicesArray[completedTrianglesArray[i] / 2] * 2);
		}

		// Adjust triangles to start from zero and count by 1, not by vertex x,y coordinate pairs.
		if (offset == 0) {
			for (int i = 0; i < n; i++)
				completedTrianglesArray[i] = (short)(completedTrianglesArray[i] / 2);
		} else {
			for (int i = 0; i < n; i++)
				completedTrianglesArray[i] = (short)((completedTrianglesArray[i] - offset) / 2);
		}

		return completedTriangles;
	}

	/** Returns INSIDE if point xp,yp is inside the circumcircle of the triangle (p1,p2,p3). Returns COMPLETE if xp is to the right
	 * of the entire circumcircle. Otherwise returns INCOMPLETE. Note: a point on the circumcircle edge is considered outside.
	 * <p>
	 * Indices >= end refer to ghost super-triangle vertices at infinity. The number of ghost vertices in the triple selects the
	 * test:
	 * <ul>
	 * <li>0 ghosts: standard in-circle test.
	 * <li>1 ghost: half-plane test on the line through the two real vertices, inside when p is on the ghost's side.
	 * <li>2 ghosts: half-plane test on a line through the real vertex, inside when p is on the ghosts' side.
	 * <li>3 ghosts: always INSIDE (the root super-triangle).
	 * </ul>
	 */
	private int circumCircle (float[] points, int end, double xp, double yp, int p1, int p2, int p3) {
		boolean s1 = p1 >= end, s2 = p2 >= end, s3 = p3 >= end;
		int superCount = (s1 ? 1 : 0) + (s2 ? 1 : 0) + (s3 ? 1 : 0);

		// No ghost vertices -> normal circumcircle calc
		if (superCount == 0) {
			return circumCircleReal(xp, yp, points[p1], points[p1 + 1], points[p2], points[p2 + 1], points[p3], points[p3 + 1]);
		}

		// All ghost vertices -> trivially inside
		if (superCount == 3) return INSIDE;

		// One ghost vertex -> form half-space of both real vertices and check, if p is on the same side as ghost vertex
		if (superCount == 1) {
			int superIdx, realA, realB;
			if (s1) {
				superIdx = p1;
				realA = p2;
				realB = p3;
			} else if (s2) {
				superIdx = p2;
				realA = p1;
				realB = p3;
			} else {
				superIdx = p3;
				realA = p1;
				realB = p2;
			}

			double ax = points[realA], ay = points[realA + 1];
			double bx = points[realB], by = points[realB + 1];

			// Exact-sign orientation of p against the line (a, b): sign(orient) == sign(cross(b - a, p - a)).
			double orient = predicates.orient2d(ax, ay, bx, by, xp, yp);

			// Get direction vector of ghost vertex
			double dx, dy;
			int kind = superIdx - end; // 0 = v_L, 2 = v_R, 4 = v_T
			if (kind == 0) { // v_L
				dx = -1;
				dy = 0;
			} else if (kind == 2) { // v_R
				dx = +1;
				dy = 0;
			} else { // v_T
				dx = 0;
				dy = 1;
			}

			// cross(b - a, ghost dir): sign tells whether the ghost vertex is to the left or right of a->b.
			// Exactly representable: bx-ax and by-ay are exact in double and dx,dy are 0/+-1.
			double factor = (bx - ax) * dy - (by - ay) * dx;

			// p is collinear with edge a->b. As the ghost vertex moves to infinity the circumcircle flattens
			// onto the segment a-b, so p is inside only if it lies strictly between a and b. The ghost
			// direction doesn't matter here.
			if (orient == 0) {
				boolean between = Math.abs(bx - ax) >= Math.abs(by - ay) ? (ax < bx ? xp > ax && xp < bx : xp > bx && xp < ax)
					: (ay < by ? yp > ay && yp < by : yp > by && yp < ay);
				return between ? INSIDE : INCOMPLETE;
			}

			// Ghost direction is parallel to edge a-b, so a, b, and the ghost are collinear and the triangle
			// is degenerate. The half-plane test breaks down here, so fall back on which ghost this is:
			// v_L and v_R lie below the points on a horizontal edge, so inside means below the edge.
			// v_T on a vertical edge has no consistent side. Safe to skip since v_L or v_R on the same edge
			// will catch p.
			if (factor == 0) {
				if (kind == 4) return INCOMPLETE; // v_T: vertical edge, no consistent half-plane
				return yp < ay ? INSIDE : INCOMPLETE; // v_L / v_R: inside if strictly below the horizontal edge
			}

			// p is inside if it is on the same side of a->b as the ghost direction
			return (orient < 0) == (factor < 0) ? INSIDE : INCOMPLETE;
		}

		// Two ghost vertices -> form half-space of both ghost vertices and check, if p is between real vert. and the half-space
		// Could also be written as a same-side-as-ghost half-plane test like the 1-ghost case (constructing a line through q with
		// direction from the ghost-pair limit),
		// but the direct limit forms below are simpler and avoid a cross product.
		int qIdx, kindA, kindB;
		if (!s1) {
			qIdx = p1;
			kindA = p2 - end;
			kindB = p3 - end;
		} else if (!s2) {
			qIdx = p2;
			kindA = p1 - end;
			kindB = p3 - end;
		} else {
			qIdx = p3;
			kindA = p1 - end;
			kindB = p2 - end;
		}

		double qx = points[qIdx], qy = points[qIdx + 1];
		// kindA/kindB with 0 = v_L, 2 = v_R, 4 = v_T. Unordered, so encode as sum
		// kindA + kindB with 2 = {v_L, v_R}, 4 = {v_L, v_T}, 6 = {v_R, v_T}
		int kindSum = kindA + kindB;
		if (kindSum == 2) { // {v_L, v_R}
			// {v_L, v_R} is a straight horizontal line through q and p needs to be below the line, so if p.y < q.y, it lives inside
			// the circumcircle
			return yp < qy ? INSIDE : INCOMPLETE;
		}
		if (kindSum == 4) {
			// {v_L, v_T} is a line y = x through q and p needs to be above the line, so if p.y - q.y > p.x - q.x, it lives inside
			// the circumcircle
			return (yp - qy) > (xp - qx) ? INSIDE : INCOMPLETE;
		}
		// {v_R, v_T} is a line y = -x through q and p needs to be above the line, so if p.y - q.y > q.x - p.x, it lives inside the
		// circumcircle
		return (yp - qy) > (qx - xp) ? INSIDE : INCOMPLETE;
	}

	/** In-circle test for a triangle of three real vertices. */
	private int circumCircleReal (double xp, double yp, double x1, double y1, double x2, double y2, double x3, double y3) {
		if (predicates.incircle(x1, y1, x2, y2, x3, y3, xp, yp) < 0) return INSIDE;

		// Not inside. Sweep-line shortcut: the triangle is done once the sweep x passes the rightmost
		// point of its circumcircle. Only need the circumcentre x and squared radius for that, computed
		// relative to vertex 3 for numerical stability. Performance only, a missed COMPLETE just costs
		// speed. Collinear triples have det == 0 and never complete.
		double acx = x1 - x3, acy = y1 - y3, bcx = x2 - x3, bcy = y2 - y3;
		double det = acx * bcy - bcx * acy;
		if (det == 0) return INCOMPLETE;
		double a = acx * acx + acy * acy, b = bcx * bcx + bcy * bcy;
		double ux = (bcy * a - acy * b) / (2 * det);
		double uy = (acx * b - bcx * a) / (2 * det);
		double dxp = xp - (x3 + ux);
		return dxp > 0 && dxp * dxp > ux * ux + uy * uy ? COMPLETE : INCOMPLETE;
	}

	/** Sorts x,y pairs of values by the x value.
	 * @param count Number of indices, must be even. */
	private void sort (float[] values, int count) {
		int pointCount = count / 2;
		originalIndices.clear();
		originalIndices.ensureCapacity(pointCount);
		short[] originalIndicesArray = originalIndices.items;
		for (short i = 0; i < pointCount; i++)
			originalIndicesArray[i] = i;

		int lower = 0;
		int upper = count - 1;
		IntArray stack = quicksortStack;
		stack.add(lower, upper - 1);
		while (stack.size > 0) {
			upper = stack.pop();
			lower = stack.pop();
			if (upper <= lower) continue;
			int i = quicksortPartition(values, lower, upper, originalIndicesArray);
			if (i - lower > upper - i) stack.add(lower, i - 2);
			stack.add(i + 2, upper);
			if (upper - i >= i - lower) stack.add(lower, i - 2);
		}
	}

	private int quicksortPartition (final float[] values, int lower, int upper, short[] originalIndices) {
		float value = values[lower];
		int up = upper;
		int down = lower + 2;
		float tempValue;
		short tempIndex;
		while (down < up) {
			while (down < up && values[down] <= value)
				down = down + 2;
			while (values[up] > value)
				up = up - 2;
			if (down < up) {
				tempValue = values[down];
				values[down] = values[up];
				values[up] = tempValue;

				tempValue = values[down + 1];
				values[down + 1] = values[up + 1];
				values[up + 1] = tempValue;

				tempIndex = originalIndices[down / 2];
				originalIndices[down / 2] = originalIndices[up / 2];
				originalIndices[up / 2] = tempIndex;
			}
		}
		if (value > values[up]) {
			values[lower] = values[up];
			values[up] = value;

			tempValue = values[lower + 1];
			values[lower + 1] = values[up + 1];
			values[up + 1] = tempValue;

			tempIndex = originalIndices[lower / 2];
			originalIndices[lower / 2] = originalIndices[up / 2];
			originalIndices[up / 2] = tempIndex;
		}
		return up;
	}

	/** Removes all triangles with a centroid outside the specified hull, which may be concave. Note some triangulations may have
	 * triangles whose centroid is inside the hull but a portion is outside. */
	public void trim (ShortArray triangles, float[] points, float[] hull, int offset, int count) {
		short[] trianglesArray = triangles.items;
		for (int i = triangles.size - 1; i >= 0; i -= 3) {
			int p1 = trianglesArray[i - 2] * 2;
			int p2 = trianglesArray[i - 1] * 2;
			int p3 = trianglesArray[i] * 2;
			GeometryUtils.triangleCentroid(points[p1], points[p1 + 1], points[p2], points[p2 + 1], points[p3], points[p3 + 1],
				centroid);
			if (!Intersector.isPointInPolygon(hull, offset, count, centroid.x, centroid.y)) {
				triangles.removeIndex(i);
				triangles.removeIndex(i - 1);
				triangles.removeIndex(i - 2);
			}
		}
	}
}
