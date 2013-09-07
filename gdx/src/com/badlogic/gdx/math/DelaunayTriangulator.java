
package com.badlogic.gdx.math;

import com.badlogic.gdx.utils.BooleanArray;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;

/** Delaunay triangulation. Adapted from Paul Bourke's triangulate: http://paulbourke.net/papers/triangulate/
 * @author Nathan Sweet */
public class DelaunayTriangulator {
	static private final float EPSILON = 0.000001f;

	private final IntArray quicksortStack = new IntArray();
	private final IntArray triangles = new IntArray(false, 64);
	private final IntArray edges = new IntArray(32);
	private final BooleanArray complete = new BooleanArray(false, 64);

	/** @see #computeTriangles(float[], int, int, boolean) */
	public IntArray computeTriangles (FloatArray points, boolean sorted) {
		return computeTriangles(points.items, 0, points.size, sorted);
	}

	/** @see #computeTriangles(float[], int, int, boolean) */
	public IntArray computeTriangles (float[] polygon, boolean sorted) {
		return computeTriangles(polygon, 0, polygon.length, sorted);
	}

	/** Triangulates the given point cloud to a list of triangle indices that make up the Delaunay triangulation.
	 * @param points x,y pairs describing points. Duplicate points will result in undefined behavior.
	 * @param sorted If false, the points will be sorted by the x coordinate, which is required by the triangulation algorithm.
	 * @return triples of indexes into the points that describe the triangles in clockwise order. Note the returned array is reused
	 *         for later calls to the same method. */
	public IntArray computeTriangles (float[] points, int offset, int count, boolean sorted) {
		int end = offset + count;

		if (!sorted) quicksortPairs(points, offset, end - 1);

		IntArray triangles = this.triangles;
		triangles.clear();
		triangles.ensureCapacity(count);

		IntArray edges = this.edges;

		BooleanArray complete = this.complete;
		complete.ensureCapacity(count);

		// Determine bounds for super triangle.
		float xmin = points[0], ymin = points[1];
		float xmax = xmin, ymax = ymin;
		for (int i = offset + 2; i < end; i++) {
			if (points[i] < xmin) xmin = points[i];
			if (points[i] > xmax) xmax = points[i];
			i++;
			if (points[i] < ymin) ymin = points[i];
			if (points[i] > ymax) ymax = points[i];
		}
		float dx = xmax - xmin, dy = ymax - ymin;
		float dmax = dx > dy ? dx : dy;
		float xmid = (xmax + xmin) / 2f, ymid = (ymax + ymin) / 2f;

		// Setup the super triangle, which encompasses all points.
		float[] superTriangle = new float[6];
		superTriangle[0] = xmid - 2f * dmax;
		superTriangle[1] = ymid - dmax;
		superTriangle[2] = xmid;
		superTriangle[3] = ymid + 2f * dmax;
		superTriangle[4] = xmid + 2f * dmax;
		superTriangle[5] = ymid - dmax;
		triangles.add(end);
		triangles.add(end + 2);
		triangles.add(end + 4);
		complete.add(false);

		// Include each point one at a time into the existing mesh.
		for (int pointIndex = offset; pointIndex < end; pointIndex += 2) {
			float x = points[pointIndex], y = points[pointIndex + 1];

			// If x,y lies inside the circumcircle of a triangle, the edges are stored and the triangle removed.
			for (int triangleIndex = 0; triangleIndex < triangles.size; triangleIndex += 3) {
				int completeIndex = triangleIndex / 3;
				if (complete.get(completeIndex)) continue;
				int p1 = triangles.get(triangleIndex);
				int p2 = triangles.get(triangleIndex + 1);
				int p3 = triangles.get(triangleIndex + 2);
				float x1, y1, x2, y2, x3, y3;
				if (p1 >= end) {
					x1 = superTriangle[p1 - end];
					y1 = superTriangle[p1 - end + 1];
				} else {
					x1 = points[p1];
					y1 = points[p1 + 1];
				}
				if (p2 >= end) {
					x2 = superTriangle[p2 - end];
					y2 = superTriangle[p2 - end + 1];
				} else {
					x2 = points[p2];
					y2 = points[p2 + 1];
				}
				if (p3 >= end) {
					x3 = superTriangle[p3 - end];
					y3 = superTriangle[p3 - end + 1];
				} else {
					x3 = points[p3];
					y3 = points[p3 + 1];
				}
				float result = circumCircle(x, y, x1, y1, x2, y2, x3, y3);
				if (result == -1) {
					edges.add(p1);
					edges.add(p2);
					edges.add(p2);
					edges.add(p3);
					edges.add(p3);
					edges.add(p1);

					triangles.removeIndex(triangleIndex + 2);
					triangles.removeIndex(triangleIndex + 1);
					triangles.removeIndex(triangleIndex);
					complete.removeIndex(completeIndex);
					triangleIndex -= 3;
				} else if (result < x) //
					complete.set(completeIndex, true);
			}

			for (int i = 0, n = edges.size; i < n; i += 2) {
				// Skip multiple edges. If all triangles are anticlockwise then all interior edges are opposite pointing in direction.
				int p1 = edges.get(i);
				if (p1 == -1) continue;
				int p2 = edges.get(i + 1);
				boolean skip = false;
				for (int ii = i + 2; ii < n; ii += 2) {
					if (p1 == edges.get(ii + 1) && p2 == edges.get(ii)) {
						skip = true;
						edges.set(ii, -1);
					}
				}
				if (skip) continue;

				// Form new triangles for the current point. Edges are arranged in clockwise order.
				triangles.add(p1);
				triangles.add(edges.get(i + 1));
				triangles.add(pointIndex);
				complete.add(false);
			}
			edges.clear();
		}
		complete.clear();

		// Remove triangles with super triangle vertices.
		for (int i = triangles.size - 1; i >= 0; i -= 3) {
			if (triangles.get(i) >= end || triangles.get(i - 1) >= end || triangles.get(i - 2) >= end) {
				triangles.removeIndex(i);
				triangles.removeIndex(i - 1);
				triangles.removeIndex(i - 2);
			}
		}
		return triangles;
	}

	/** Returns -1 if point xp,yp is inside the circumcircle made up of the points x1,y1, x2,y2, x3,y3. Otherwise returns the x
	 * coordinate of the circumcircle center plus the radius. Note: a point on the edge is inside the circumcircle. */
	float circumCircle (float xp, float yp, float x1, float y1, float x2, float y2, float x3, float y3) {
		// Check for coincident points.
		if (Math.abs(y1 - y2) < EPSILON && Math.abs(y2 - y3) < EPSILON) return Float.MAX_VALUE;

		float xc, yc;
		if (Math.abs(y2 - y1) < EPSILON) {
			float m2 = -(x3 - x2) / (y3 - y2);
			float mx2 = (x2 + x3) / 2f;
			float my2 = (y2 + y3) / 2f;
			xc = (x2 + x1) / 2f;
			yc = m2 * (xc - mx2) + my2;
		} else if (Math.abs(y3 - y2) < EPSILON) {
			float m1 = -(x2 - x1) / (y2 - y1);
			float mx1 = (x1 + x2) / 2f;
			float my1 = (y1 + y2) / 2f;
			xc = (x3 + x2) / 2f;
			yc = m1 * (xc - mx1) + my1;
		} else {
			float m1 = -(x2 - x1) / (y2 - y1);
			float m2 = -(x3 - x2) / (y3 - y2);
			float mx1 = (x1 + x2) / 2f;
			float mx2 = (x2 + x3) / 2f;
			float my1 = (y1 + y2) / 2f;
			float my2 = (y2 + y3) / 2f;
			xc = (m1 * mx1 - m2 * mx2 + my2 - my1) / (m1 - m2);
			yc = m1 * (xc - mx1) + my1;
		}

		float dx = x2 - xc;
		float dy = y2 - yc;
		float rsqr = dx * dx + dy * dy;

		dx = xp - xc;
		dy = yp - yc;
		return dx * dx + dy * dy - rsqr <= EPSILON ? -1 : (xc + (float)Math.sqrt(rsqr));
	}

	/** Sorts x,y pairs of values by the x value.
	 * @param lower Start x index.
	 * @param upper End x index. */
	private void quicksortPairs (float[] values, int lower, int upper) {
		IntArray stack = quicksortStack;
		stack.add(lower);
		stack.add(upper - 1);
		while (stack.size > 0) {
			upper = stack.pop();
			lower = stack.pop();
			if (upper <= lower) continue;
			int i = quicksortPartition(values, lower, upper);
			if (i - lower > upper - i) {
				stack.add(lower);
				stack.add(i - 2);
			}
			stack.add(i + 2);
			stack.add(upper);
			if (upper - i >= i - lower) {
				stack.add(lower);
				stack.add(i - 2);
			}
		}
	}

	private int quicksortPartition (final float[] values, int lower, int upper) {
		float value = values[lower];
		int up = upper;
		int down = lower;
		float temp;
		while (down < up) {
			while (values[down] <= value && down < up)
				down = down + 2;
			while (values[up] > value)
				up = up - 2;
			if (down < up) {
				temp = values[down];
				values[down] = values[up];
				values[up] = temp;

				temp = values[down + 1];
				values[down + 1] = values[up + 1];
				values[up + 1] = temp;
			}
		}
		values[lower] = values[up];
		values[up] = value;

		temp = values[lower + 1];
		values[lower + 1] = values[up + 1];
		values[up + 1] = temp;
		return up;
	}
}
