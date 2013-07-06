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
import java.util.Collections;
import java.util.List;

/** A simple implementation of the ear cutting algorithm to triangulate simple polygons without holes. For more information:
 * <ul>
 * <li><a href="http://cgm.cs.mcgill.ca/~godfried/teaching/cg-projects/97/Ian/algorithm2.html">http://cgm.cs.mcgill.ca/~godfried/teaching/cg-projects/97/Ian/algorithm2.html</a></li>
 * <li><a href="http://www.geometrictools.com/Documentation/TriangulationByEarClipping.pdf">http://www.geometrictools.com/Documentation/TriangulationByEarClipping.pdf</a></li>
 * </ul>
 *
 * If the input polygon is not simple (i.e. has self-intersections), there will be output but it is of unspecified quality
 * (garbage in, garbage out).
 *
 * @author badlogicgames@gmail.com
 * @author Nicolas Gramlich (Improved performance. Collinear edges are now supported.)
 * @author Eric Spitz
 * @author Thomas ten Cate (Several bugfixes and performance improvements.) */
public final class EarClippingTriangulator {

	private static final int CONCAVE = -1;
	private static final int TANGENTIAL = 0;
	private static final int CONVEX = 1;

	private List<Vector2> vertices;
	private int vertexCount;
	private int[] vertexTypes;
	private List<Vector2> triangles;

	/** Triangulates the given (convex or concave) polygon to a list of triangles.
	 *
	 * @param polygon a list of points describing a simple polygon, in either clockwise or counterclockwise order
	 * @return the triangles, as triples of points in clockwise order */
	public List<Vector2> computeTriangles (final List<Vector2> polygon) {
		// TODO Check if LinkedList performs better
		vertices = new ArrayList<Vector2>(polygon.size());
		vertices.addAll(polygon);
		vertexCount = vertices.size();

		/* Ensure vertices are in clockwise order. */
		if (!areVerticesClockwise()) {
			Collections.reverse(vertices);
		}

		vertexTypes = new int[vertexCount];
		for (int i = 0; i < vertexCount; ++i) {
			vertexTypes[i] = classifyVertex(i);
		}

		// A polygon with n vertices has a triangulation of n-2 triangles
		triangles = new ArrayList<Vector2>(3 * Math.max(0, vertexCount - 2));

		/*
		 * ESpitz: For the sake of performance, we only need to test for eartips while the polygon has more than three verts. If
		 * there are only three verts left to test, or there were only three verts to begin with, there is no need to continue with
		 * this loop.
		 */
		while (vertexCount > 3) {
			int earTipIndex = findEarTip();
			cutEarTip(earTipIndex);

			// Only the type of the two vertices adjacent to the clipped vertex can have changed,
			// so no need to reclassify all of them.
			int previousIndex = computePreviousIndex(earTipIndex);
			int nextIndex = earTipIndex == vertexCount ? 0 : earTipIndex;
			vertexTypes[previousIndex] = classifyVertex(previousIndex);
			vertexTypes[nextIndex] = classifyVertex(nextIndex);
		}

		/*
		 * ESpitz: If there are only three verts left to test, or there were only three verts to begin with, we have the final
		 * triangle.
		 */
		if (vertexCount == 3) {
			triangles.addAll(vertices);
		}

		List<Vector2> result = triangles;
		vertices = null;
		triangles = null;
		vertexTypes = null;
		return result;
	}

	private boolean areVerticesClockwise () {
		float area = 0;
		for (int i = 0; i < vertexCount; i++) {
			final Vector2 p1 = vertices.get(i);
			final Vector2 p2 = vertices.get(computeNextIndex(i));
			area += p1.x * p2.y - p2.x * p1.y;
		}
		return area < 0;
	}

	/** @return one of {@link #CONCAVE}, {@link #TANGENTIAL} or {@link #CONVEX} */
	private int classifyVertex (int index) {
		final Vector2 previousVertex = vertices.get(computePreviousIndex(index));
		final Vector2 currentVertex = vertices.get(index);
		final Vector2 nextVertex = vertices.get(computeNextIndex(index));

		return computeSpannedAreaSign(previousVertex, currentVertex, nextVertex);
	}

	private static int computeSpannedAreaSign (final Vector2 p1, final Vector2 p2, final Vector2 p3) {
		float area = 0;
		area += p1.x * (p3.y - p2.y);
		area += p2.x * (p1.y - p3.y);
		area += p3.x * (p2.y - p1.y);
		return (int)Math.signum(area);
	}

	private int findEarTip () {
		for (int index = 0; index < vertexCount; index++) {
			if (isEarTip(index)) {
				return index;
			}
		}
		return desperatelyFindEarTip();
	}

	private int desperatelyFindEarTip () {
		// Desperate mode: if no vertex is an ear tip, we are dealing with a degenerate polygon (e.g. nearly collinear).
		// Note that the input was not necessarily degenerate, but we could have made it so by clipping some valid ears.

		// Idea taken from Martin Held, "FIST: Fast industrial-strength triangulation of polygons", Algorithmica (1998),
		// http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.115.291

		// Return a convex or tangential vertex if one exists
		for (int index = 0; index < vertexCount; index++) {
			if (vertexTypes[index] != CONCAVE) {
				return index;
			}
		}

		// If all vertices are concave, just return the first one
		return 0;
	}

	private boolean isEarTip (final int pEarTipIndex) {
		if (vertexTypes[pEarTipIndex] == CONCAVE) {
			return false;
		}

		final int previousIndex = computePreviousIndex(pEarTipIndex);
		final int nextIndex = computeNextIndex(pEarTipIndex);
		final Vector2 p1 = vertices.get(previousIndex);
		final Vector2 p2 = vertices.get(pEarTipIndex);
		final Vector2 p3 = vertices.get(nextIndex);

		// Check if any point is inside the triangle formed by previous, current and next vertices.
		// Only consider vertices that are not part of this triangle, or else we'll always find one inside.
		for (int i = computeNextIndex(nextIndex); i != previousIndex; i = computeNextIndex(i)) {
			// Concave vertices can obviously be inside the candidate ear, but so can tangential vertices
			// if they coincide with one of the triangle's vertices.
			if (vertexTypes[i] != CONVEX) {
				final Vector2 v = vertices.get(i);

				final int areaSign1 = computeSpannedAreaSign(p1, p2, v);
				final int areaSign2 = computeSpannedAreaSign(p2, p3, v);
				final int areaSign3 = computeSpannedAreaSign(p3, p1, v);

				// Because the polygon has clockwise winding order, the area sign will be positive if the point is strictly inside.
				// It will be 0 on the edge, which we want to include as well, because incorrect results can happen if we don't
				// (http://code.google.com/p/libgdx/issues/detail?id=815).
				if (areaSign1 >= 0 && areaSign2 >= 0 && areaSign3 >= 0) {
					return false;
				}
			}
		}
		return true;
	}

	private void cutEarTip (final int pEarTipIndex) {
		final int previousIndex = computePreviousIndex(pEarTipIndex);
		final int nextIndex = computeNextIndex(pEarTipIndex);

		triangles.add(new Vector2(vertices.get(previousIndex)));
		triangles.add(new Vector2(vertices.get(pEarTipIndex)));
		triangles.add(new Vector2(vertices.get(nextIndex)));

		vertices.remove(pEarTipIndex);
		System.arraycopy(vertexTypes, pEarTipIndex + 1, vertexTypes, pEarTipIndex, vertexCount - pEarTipIndex - 1);
		vertexCount--;
	}

	private int computePreviousIndex (final int pIndex) {
		return pIndex == 0 ? vertexCount - 1 : pIndex - 1;
	}

	private int computeNextIndex (final int pIndex) {
		return pIndex == vertexCount - 1 ? 0 : pIndex + 1;
	}
}
