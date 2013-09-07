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

import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;

/** A simple implementation of the ear cutting algorithm to triangulate simple polygons without holes. For more information:
 * <ul>
 * <li><a href="http://cgm.cs.mcgill.ca/~godfried/teaching/cg-projects/97/Ian/algorithm2.html">http://cgm.cs.mcgill.ca/~godfried/
 * teaching/cg-projects/97/Ian/algorithm2.html</a></li>
 * <li><a
 * href="http://www.geometrictools.com/Documentation/TriangulationByEarClipping.pdf">http://www.geometrictools.com/Documentation
 * /TriangulationByEarClipping.pdf</a></li>
 * </ul>
 * If the input polygon is not simple (self-intersects), there will be output but it is of unspecified quality (garbage in,
 * garbage out).
 * @author badlogicgames@gmail.com
 * @author Nicolas Gramlich (optimizations, collinear edge support)
 * @author Eric Spitz
 * @author Thomas ten Cate (bugfixes, optimizations)
 * @author Nathan Sweet (rewrite, no allocation, optimizations) */
public class EarClippingTriangulator {
	static private final int CONCAVE = -1;
	static private final int TANGENTIAL = 0;
	static private final int CONVEX = 1;

	private final FloatArray verticesArray = new FloatArray();
	private float[] vertices;
	private int vertexCount;
	private final IntArray vertexTypes = new IntArray();
	private final FloatArray triangles = new FloatArray();

	/** @see #computeTriangles(float[], int, int) */
	public FloatArray computeTriangles (FloatArray polygon) {
		setVertices(polygon.items, 0, polygon.size);
		return computeTriangles();
	}

	/** @see #computeTriangles(float[], int, int) */
	public FloatArray computeTriangles (float[] polygon) {
		setVertices(polygon, 0, polygon.length);
		return computeTriangles();
	}

	/** Triangulates the given (convex or concave) simple polygon to a list of triangle vertices.
	 * @param polygon pairs describing vertices of the polygon, in either clockwise or counterclockwise order.
	 * @return triples of pairs describing triangle vertices in clockwise order. Note the returned array is reused for later calls
	 *         to the same method. */
	public FloatArray computeTriangles (float[] polygon, int offset, int count) {
		setVertices(polygon, offset, count);
		return computeTriangles();
	}

	private void setVertices (float[] polygon, int offset, int count) {
		FloatArray verticesArray = this.verticesArray;
		if (areVerticesClockwise(polygon, 0, polygon.length)) {
			verticesArray.addAll(polygon, 0, polygon.length);
			return;
		}
		// Copy reversed.
		verticesArray.ensureCapacity(count);
		verticesArray.size = count;
		float[] vertices = verticesArray.items;
		for (int v = count - 2, p = offset, n = offset + count; p < n; v -= 2, p += 2) {
			vertices[v] = polygon[p];
			vertices[v + 1] = polygon[p + 1];
		}
	}

	private FloatArray computeTriangles () {
		FloatArray verticesArray = this.verticesArray;
		float[] vertices = this.vertices = verticesArray.items;

		IntArray vertexTypes = this.vertexTypes;
		vertexCount = verticesArray.size / 2;
		vertexTypes.ensureCapacity(vertexCount);
		for (int i = 0, n = vertexCount; i < n; ++i)
			vertexTypes.add(classifyVertex(i));

		FloatArray triangles = this.triangles;
		triangles.clear();
		// A polygon with n vertices has a triangulation of n-2 triangles.
		triangles.ensureCapacity(Math.max(0, vertexCount - 2) * 3 * 2);

		while (vertexCount > 3) {
			int earTipIndex = findEarTip();
			cutEarTip(earTipIndex);

			// The type of the two vertices adjacent to the clipped vertex may have changed.
			int previousIndex = previousIndex(earTipIndex);
			int nextIndex = earTipIndex == vertexCount ? 0 : earTipIndex;
			vertexTypes.set(previousIndex, classifyVertex(previousIndex));
			vertexTypes.set(nextIndex, classifyVertex(nextIndex));
		}

		if (vertexCount == 3) {
			triangles.add(vertices[0]);
			triangles.add(vertices[1]);
			triangles.add(vertices[2]);
			triangles.add(vertices[3]);
			triangles.add(vertices[4]);
			triangles.add(vertices[5]);
		}

		verticesArray.clear();
		vertexTypes.clear();
		return triangles;
	}

	/** @return {@link #CONCAVE}, {@link #TANGENTIAL} or {@link #CONVEX} */
	private int classifyVertex (int index) {
		float[] vertices = this.vertices;
		int previousIndex = previousIndex(index) * 2;
		float previousX = vertices[previousIndex];
		float previousY = vertices[previousIndex + 1];
		float currentX = vertices[index * 2];
		float currentY = vertices[index * 2 + 1];
		int nextIndex = nextIndex(index) * 2;
		float nextX = vertices[nextIndex];
		float nextY = vertices[nextIndex + 1];
		return computeSpannedAreaSign(previousX, previousY, currentX, currentY, nextX, nextY);
	}

	private int findEarTip () {
		for (int i = 0, n = vertexCount; i < n; i++)
			if (isEarTip(i)) return i;

		// Desperate mode: if no vertex is an ear tip, we are dealing with a degenerate polygon (e.g. nearly collinear).
		// Note that the input was not necessarily degenerate, but we could have made it so by clipping some valid ears.

		// Idea taken from Martin Held, "FIST: Fast industrial-strength triangulation of polygons", Algorithmica (1998),
		// http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.115.291

		// Return a convex or tangential vertex if one exists.
		IntArray vertexTypes = this.vertexTypes;
		for (int i = 0, n = vertexCount; i < n; i++)
			if (vertexTypes.get(i) != CONCAVE) return i;
		return 0; // If all vertices are concave, just return the first one.
	}

	private boolean isEarTip (int earTipIndex) {
		IntArray vertexTypes = this.vertexTypes;
		if (vertexTypes.get(earTipIndex) == CONCAVE) return false;

		int previousIndex = previousIndex(earTipIndex);
		int nextIndex = nextIndex(earTipIndex);
		float[] vertices = this.vertices;
		float p1x = vertices[previousIndex * 2];
		float p1y = vertices[previousIndex * 2 + 1];
		float p2x = vertices[earTipIndex * 2];
		float p2y = vertices[earTipIndex * 2 + 1];
		float p3x = vertices[nextIndex * 2];
		float p3y = vertices[nextIndex * 2 + 1];

		// Check if any point is inside the triangle formed by previous, current and next vertices.
		// Only consider vertices that are not part of this triangle, or else we'll always find one inside.
		for (int i = nextIndex(nextIndex); i != previousIndex; i = nextIndex(i)) {
			// Concave vertices can obviously be inside the candidate ear, but so can tangential vertices
			// if they coincide with one of the triangle's vertices.
			if (vertexTypes.get(i) != CONVEX) {
				float vx = vertices[i * 2];
				float vy = vertices[i * 2 + 1];
				// Because the polygon has clockwise winding order, the area sign will be positive if the point is strictly inside.
				// It will be 0 on the edge, which we want to include as well.
				if (computeSpannedAreaSign(p1x, p1y, p2x, p2y, vx, vy) >= 0) {
					if (computeSpannedAreaSign(p2x, p2y, p3x, p3y, vx, vy) >= 0) {
						if (computeSpannedAreaSign(p3x, p3y, p1x, p1y, vx, vy) >= 0) return false;
					}
				}
			}
		}
		return true;
	}

	private void cutEarTip (int earTipIndex) {
		float[] vertices = this.vertices;
		FloatArray triangles = this.triangles;

		int previousIndex = previousIndex(earTipIndex) * 2;
		int nextIndex = nextIndex(earTipIndex) * 2;

		vertexTypes.removeIndex(earTipIndex);
		vertexCount--;

		triangles.add(vertices[previousIndex]);
		triangles.add(vertices[previousIndex + 1]);
		earTipIndex *= 2;
		triangles.add(vertices[earTipIndex]);
		triangles.add(vertices[earTipIndex + 1]);
		triangles.add(vertices[nextIndex]);
		triangles.add(vertices[nextIndex + 1]);

		// Remove both indexes with a single copy.
		verticesArray.size -= 2;
		System.arraycopy(vertices, earTipIndex + 2, vertices, earTipIndex, verticesArray.size - earTipIndex);
	}

	private int previousIndex (int index) {
		return (index == 0 ? vertexCount : index) - 1;
	}

	private int nextIndex (int index) {
		return (index + 1) % vertexCount;
	}

	static private boolean areVerticesClockwise (float[] vertices, int offset, int count) {
		if (count <= 2) return false;
		float area = 0, p1x, p1y, p2x, p2y;
		for (int i = offset, n = offset + count - 3; i < n; i += 2) {
			p1x = vertices[i];
			p1y = vertices[i + 1];
			p2x = vertices[i + 2];
			p2y = vertices[i + 3];
			area += p1x * p2y - p2x * p1y;
		}
		p1x = vertices[count - 2];
		p1y = vertices[count - 1];
		p2x = vertices[0];
		p2y = vertices[1];
		return area + p1x * p2y - p2x * p1y < 0;
	}

	static private int computeSpannedAreaSign (float p1x, float p1y, float p2x, float p2y, float p3x, float p3y) {
		float area = p1x * (p3y - p2y);
		area += p2x * (p1y - p3y);
		area += p3x * (p2y - p1y);
		return (int)Math.signum(area);
	}
}
