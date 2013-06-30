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
 * @author badlogicgames@gmail.com
 * @author Nicolas Gramlich (Improved performance. Collinear edges are now supported.)
 * @author Eric Spitz
 * @author Thomas ten Cate (Several bugfixes and performance improvements.)
 */
public final class EarClippingTriangulator {

	private static final int CONCAVE = 1;
	private static final int CONVEX_OR_TANGENTIAL = -1;

	private int concaveVertexCount;

	/** Triangulates the given (concave) polygon to a list of triangles. The resulting triangles have clockwise order.
	 * 
	 * @param polygon the polygon
	 * @return the triangles */
	public List<Vector2> computeTriangles (final List<Vector2> polygon) {
		// TODO Check if LinkedList performs better
		final ArrayList<Vector2> triangles = new ArrayList<Vector2>();
		final ArrayList<Vector2> vertices = new ArrayList<Vector2>(polygon.size());
		vertices.addAll(polygon);

		/* Ensure vertices are in clockwise order. */
		if (!areVerticesClockwise(vertices)) {
			Collections.reverse(vertices);
		}

		/*
		 * ESpitz: For the sake of performance, we only need to test for eartips while the polygon has more than three verts. If
		 * there are only three verts left to test, or there were only three verts to begin with, there is no need to continue with
		 * this loop.
		 */
		while (vertices.size() > 3) {
			// TODO Usually(Always?) only the Types of the vertices next to the
			// ear change! --> Improve
			final int vertexTypes[] = this.classifyVertices(vertices);

			int earTipIndex = findEarTip(vertices, vertexTypes);
			cutEarTip(vertices, earTipIndex, triangles);
		}

		/*
		 * ESpitz: If there are only three verts left to test, or there were only three verts to begin with, we have the final
		 * triangle.
		 */
		if (vertices.size() == 3) {
			triangles.addAll(vertices);
		}

		return triangles;
	}

	private static boolean areVerticesClockwise (final ArrayList<Vector2> pVertices) {
		final int vertexCount = pVertices.size();

		float area = 0;
		for (int i = 0; i < vertexCount; i++) {
			final Vector2 p1 = pVertices.get(i);
			final Vector2 p2 = pVertices.get(computeNextIndex(pVertices, i));
			area += p1.x * p2.y - p2.x * p1.y;
		}

		if (area < 0) {
			return true;
		} else {
			return false;
		}
	}

	/** @param pVertices
	 * @return An array of length <code>pVertices.size()</code> filled with either {@link EarClippingTriangulator#CONCAVE} or
	 *         {@link EarClippingTriangulator#CONVEX_OR_TANGENTIAL}. */
	private int[] classifyVertices (final ArrayList<Vector2> pVertices) {
		final int vertexCount = pVertices.size();

		final int[] vertexTypes = new int[vertexCount];
		this.concaveVertexCount = 0;

		for (int index = 0; index < vertexCount; index++) {
			final int previousIndex = computePreviousIndex(pVertices, index);
			final int nextIndex = computeNextIndex(pVertices, index);

			final Vector2 previousVertex = pVertices.get(previousIndex);
			final Vector2 currentVertex = pVertices.get(index);
			final Vector2 nextVertex = pVertices.get(nextIndex);

			if (isTriangleConvex(previousVertex, currentVertex, nextVertex)) {
				vertexTypes[index] = CONVEX_OR_TANGENTIAL;
			} else {
				vertexTypes[index] = CONCAVE;
				this.concaveVertexCount++;
			}
		}

		return vertexTypes;
	}

	private static boolean isTriangleConvex (final Vector2 p1, final Vector2 p2, final Vector2 p3) {
		if (computeSpannedAreaSign(p1, p2, p3) < 0) {
			return false;
		} else {
			return true;
		}
	}

	private static int computeSpannedAreaSign (final Vector2 p1, final Vector2 p2, final Vector2 p3) {
		/*
		 * Espitz: using doubles corrects for very rare cases where we run into floating point imprecision in the area test, causing
		 * the method to return a 0 when it should have returned -1 or 1.
		 */
		double area = 0;

		area += (double)p1.x * (p3.y - p2.y);
		area += (double)p2.x * (p1.y - p3.y);
		area += (double)p3.x * (p2.y - p1.y);

		return (int)Math.signum(area);
	}
	
	private int findEarTip (final ArrayList<Vector2> pVertices, final int[] pVertexTypes) {
		final int vertexCount = pVertices.size();
		for (int index = 0; index < vertexCount; index++) {
			if (isEarTip(pVertices, index, pVertexTypes)) {
				return index;
			}
		}
		return desperatelyFindEarTip(pVertices, pVertexTypes);
	}
	
	private int desperatelyFindEarTip (final ArrayList<Vector2> pVertices, final int[] pVertexTypes) {
		// Desperate mode: if no vertex is an ear tip, we are dealing with a degenerate polygon (e.g. nearly collinear).
		// Note that the input was not necessarily degenerate, but we could have made it so by clipping some valid ears.
		
		// Idea taken from Martin Held, "FIST: Fast industrial-strength triangulation of polygons", Algorithmica (1998),
		// http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.115.291
		
		// Return a convex vertex if one exists
		final int vertexCount = pVertices.size();
		for (int index = 0; index < vertexCount; index++) {
			if (pVertexTypes[index] == CONVEX_OR_TANGENTIAL) {
				return index;
			}
		}
		
		// If all vertices are concave, just return the first one
		return 0;
	}

	private boolean isEarTip (final ArrayList<Vector2> pVertices, final int pEarTipIndex, final int[] pVertexTypes) {
		if (pVertexTypes[pEarTipIndex] != CONVEX_OR_TANGENTIAL) {
			return false;
		}
		if (this.concaveVertexCount == 0 ) {
			return true;
		}
		final int previousIndex = computePreviousIndex(pVertices, pEarTipIndex);
		final int nextIndex = computeNextIndex(pVertices, pEarTipIndex);
		final Vector2 p1 = pVertices.get(previousIndex);
		final Vector2 p2 = pVertices.get(pEarTipIndex);
		final Vector2 p3 = pVertices.get(nextIndex);
		
		final int vertexCount = pVertices.size();
		// Check if any point is inside the triangle formed by previous, current and next vertices.
		// Only consider vertices that are not part of this triangle, or else we'll always find one inside.
		for (int i = computeNextIndex(pVertices, nextIndex); i != previousIndex; i = computeNextIndex(pVertices, i)) {
			if ((pVertexTypes[i] == CONCAVE)) {
				final Vector2 v = pVertices.get(i);

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

	private void cutEarTip (final ArrayList<Vector2> pVertices, final int pEarTipIndex, final ArrayList<Vector2> pTriangles) {
		final int previousIndex = computePreviousIndex(pVertices, pEarTipIndex);
		final int nextIndex = computeNextIndex(pVertices, pEarTipIndex);

		if (!isCollinear(pVertices, previousIndex, pEarTipIndex, nextIndex)) {
			pTriangles.add(new Vector2(pVertices.get(previousIndex)));
			pTriangles.add(new Vector2(pVertices.get(pEarTipIndex)));
			pTriangles.add(new Vector2(pVertices.get(nextIndex)));
		}

		pVertices.remove(pEarTipIndex);
		if (pVertices.size() >= 3) {
			removeCollinearNeighborEarsAfterRemovingEarTip(pVertices, pEarTipIndex);
		}
	}

	private static void removeCollinearNeighborEarsAfterRemovingEarTip (final ArrayList<Vector2> pVertices,
		final int pEarTipCutIndex) {
		final int collinearityCheckNextIndex = pEarTipCutIndex % pVertices.size();
		int collinearCheckPreviousIndex = computePreviousIndex(pVertices, collinearityCheckNextIndex);

		if (isCollinear(pVertices, collinearityCheckNextIndex)) {
			pVertices.remove(collinearityCheckNextIndex);

			if (pVertices.size() > 3) {
				/* Update */
				collinearCheckPreviousIndex = computePreviousIndex(pVertices, collinearityCheckNextIndex);
				if (isCollinear(pVertices, collinearCheckPreviousIndex)) {
					pVertices.remove(collinearCheckPreviousIndex);
				}
			}
		} else if (isCollinear(pVertices, collinearCheckPreviousIndex)) {
			pVertices.remove(collinearCheckPreviousIndex);
		}
	}

	private static boolean isCollinear (final ArrayList<Vector2> pVertices, final int pIndex) {
		final int previousIndex = computePreviousIndex(pVertices, pIndex);
		final int nextIndex = computeNextIndex(pVertices, pIndex);

		return isCollinear(pVertices, previousIndex, pIndex, nextIndex);
	}

	private static boolean isCollinear (final ArrayList<Vector2> pVertices, final int pPreviousIndex, final int pIndex,
		final int pNextIndex) {
		final Vector2 previousVertex = pVertices.get(pPreviousIndex);
		final Vector2 vertex = pVertices.get(pIndex);
		final Vector2 nextVertex = pVertices.get(pNextIndex);

		return computeSpannedAreaSign(previousVertex, vertex, nextVertex) == 0;
	}

	private static int computePreviousIndex (final List<Vector2> pVertices, final int pIndex) {
		return pIndex == 0 ? pVertices.size() - 1 : pIndex - 1;
	}

	private static int computeNextIndex (final List<Vector2> pVertices, final int pIndex) {
		return pIndex == pVertices.size() - 1 ? 0 : pIndex + 1;
	}
}
