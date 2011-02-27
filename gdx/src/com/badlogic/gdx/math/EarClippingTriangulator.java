/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com), Nicolas Gramlich
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple implementation of the ear cutting algorithm to triangulate simple
 * polygons without holes. For more information:
 * http://cgm.cs.mcgill.ca/~godfried/teaching/cg-projects/97/Ian/algorithm2.html
 * http://www.geometrictools.com/Documentation/TriangulationByEarClipping.pdf
 * 
 * @author badlogicgames@gmail.com
 * @author Nicolas Gramlich (Improved performance. Collinear edges are now supported.)
 */
public final class EarClippingTriangulator {

	private static final int CONCAVE = 1;
	private static final int CONVEX = -1;

	private int concaveVertexCount;

	/**
	 * Triangulates the given (concave) polygon to a list of triangles. The
	 * resulting triangles have clockwise order. 
	 * @param polygon the polygon
	 * @return the triangles
	 */
	public List<Vector2> computeTriangles(final List<Vector2> polygon) {
		// TODO Check if LinkedList performs better
		final ArrayList<Vector2> triangles = new ArrayList<Vector2>();
		final ArrayList<Vector2> vertices = new ArrayList<Vector2>(polygon.size());
		vertices.addAll(polygon);

		if(vertices.size() == 3) {
			triangles.addAll(vertices);
			return triangles;
		}

		while(vertices.size() >= 3) {
			// TODO Usually(Always?) only the Types of the vertices next to the ear change! --> Improve
			final int vertexTypes[] = this.classifyVertices(vertices);

			final int vertexCount = vertices.size();
			for(int index = 0; index < vertexCount; index++) {
				if(this.isEarTip(vertices, index, vertexTypes)) {
					this.cutEarTip(vertices, index, triangles);
					break;
				}
			}
		}

		return triangles;
	}
	
	private static boolean areVerticesClockwise(final ArrayList<Vector2> pVertices) {
		final int vertexCount = pVertices.size();

		float area = 0;
		for(int i = 0; i < vertexCount; i++) {
			final Vector2 p1 = pVertices.get(i);
			final Vector2 p2 = pVertices.get(EarClippingTriangulator.computeNextIndex(pVertices, i));
			area += p1.x * p2.y - p2.x * p1.y;
		}

		if(area < 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @param pVertices
	 * @return An array of length <code>pVertices.size()</code> filled with either {@link EarClippingTriangulator#CONCAVE} or
	 * {@link EarClippingTriangulator#CONVEX}.
	 */
	private int[] classifyVertices(final ArrayList<Vector2> pVertices) {
		final int vertexCount = pVertices.size();

		final int[] vertexTypes = new int[vertexCount];
		this.concaveVertexCount = 0;

		/* Ensure vertices are in clockwise order. */
		if(!EarClippingTriangulator.areVerticesClockwise(pVertices)) {
			Collections.reverse(pVertices);
		}

		for(int index = 0; index < vertexCount; index++) {
			final int previousIndex = EarClippingTriangulator.computePreviousIndex(pVertices, index);
			final int nextIndex = EarClippingTriangulator.computeNextIndex(pVertices, index);

			final Vector2 previousVertex = pVertices.get(previousIndex);
			final Vector2 currentVertex = pVertices.get(index);
			final Vector2 nextVertex = pVertices.get(nextIndex);

			if(EarClippingTriangulator.isTriangleConvex(previousVertex.x, previousVertex.y, currentVertex.x, currentVertex.y, nextVertex.x, nextVertex.y)) {
				vertexTypes[index] = CONVEX;
			} else {
				vertexTypes[index] = CONCAVE;
				this.concaveVertexCount++;
			}
		}

		return vertexTypes;
	}

	private static boolean isTriangleConvex(final float pX1, final float pY1, final float pX2, final float pY2, final float pX3, final float pY3) {
		if(EarClippingTriangulator.computeSpannedAreaSign(pX1, pY1, pX2, pY2, pX3, pY3) < 0) {
			return false;
		} else {
			return true;
		}
	}

	private static int computeSpannedAreaSign(final float pX1, final float pY1, final float pX2, final float pY2, final float pX3, final float pY3) {
		float area = 0;

		area += pX1 * (pY3 - pY2);
		area += pX2 * (pY1 - pY3);
		area += pX3 * (pY2 - pY1);

		return (int)Math.signum(area);
	}

	/**
	 * @return <code>true</code> when the Triangles contains one or more vertices, <code>false</code> otherwise.
	 */
	private static boolean isAnyVertexInTriangle(final ArrayList<Vector2> pVertices, final int[] pVertexTypes, final float pX1, final float pY1, final float pX2, final float pY2, final float pX3, final float pY3) {
		int i = 0;

		final int vertexCount = pVertices.size();
		while(i < vertexCount - 1) {
			if((pVertexTypes[i] == CONCAVE)) {
				final Vector2 currentVertex = pVertices.get(i);

				final float currentVertexX = currentVertex.x;
				final float currentVertexY = currentVertex.y;

				/* TODO The following condition fails for perpendicular, axis aligned triangles! 
				 * Removing it doesn't seem to cause problems. 
				 * Maybe it was an optimization?
				 * Maybe it tried to handle collinear pieces ? */
//				if(((currentVertexX != pX1) && (currentVertexY != pY1)) || ((currentVertexX != pX2) && (currentVertexY != pY2)) || ((currentVertexX != pX3) && (currentVertexY != pY3))) {
					final int areaSign1 = EarClippingTriangulator.computeSpannedAreaSign(pX1, pY1, pX2, pY2, currentVertexX, currentVertexY);
					final int areaSign2 = EarClippingTriangulator.computeSpannedAreaSign(pX2, pY2, pX3, pY3, currentVertexX, currentVertexY);
					final int areaSign3 = EarClippingTriangulator.computeSpannedAreaSign(pX3, pY3, pX1, pY1, currentVertexX, currentVertexY);

					if(areaSign1 > 0 && areaSign2 > 0 && areaSign3 > 0) {
						return true;
					} else if(areaSign1 <= 0 && areaSign2 <= 0 && areaSign3 <= 0) {
						return true;
					}
//				}
			}
			i++;
		}
		return false;
	}

	private boolean isEarTip(final ArrayList<Vector2> pVertices, final int pEarTipIndex, final int[] pVertexTypes) {
		if(this.concaveVertexCount != 0) {
			final Vector2 previousVertex = pVertices.get(EarClippingTriangulator.computePreviousIndex(pVertices, pEarTipIndex));
			final Vector2 currentVertex = pVertices.get(pEarTipIndex);
			final Vector2 nextVertex = pVertices.get(EarClippingTriangulator.computeNextIndex(pVertices, pEarTipIndex));

			if(EarClippingTriangulator.isAnyVertexInTriangle(pVertices, pVertexTypes, previousVertex.x, previousVertex.y, currentVertex.x, currentVertex.y, nextVertex.x, nextVertex.y)) {
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}

	private void cutEarTip(final ArrayList<Vector2> pVertices, final int pEarTipIndex, final ArrayList<Vector2> pTriangles) {
		final int previousIndex = EarClippingTriangulator.computePreviousIndex(pVertices, pEarTipIndex);
		final int nextIndex = EarClippingTriangulator.computeNextIndex(pVertices, pEarTipIndex);

		if(!EarClippingTriangulator.isCollinear(pVertices, previousIndex, pEarTipIndex, nextIndex)) {
			pTriangles.add(new Vector2(pVertices.get(previousIndex)));
			pTriangles.add(new Vector2(pVertices.get(pEarTipIndex)));
			pTriangles.add(new Vector2(pVertices.get(nextIndex)));
		}

		pVertices.remove(pEarTipIndex);
		if(pVertices.size() >= 3) {
			EarClippingTriangulator.removeCollinearNeighborEarsAfterRemovingEarTip(pVertices, pEarTipIndex);
		}
	}

	private static void removeCollinearNeighborEarsAfterRemovingEarTip(final ArrayList<Vector2> pVertices, final int pEarTipCutIndex) {
		final int collinearityCheckNextIndex = pEarTipCutIndex % pVertices.size();
		int collinearCheckPreviousIndex = EarClippingTriangulator.computePreviousIndex(pVertices, collinearityCheckNextIndex);

		if(EarClippingTriangulator.isCollinear(pVertices, collinearityCheckNextIndex)) {
			pVertices.remove(collinearityCheckNextIndex);

			if(pVertices.size() > 3) {
				/* Update */
				collinearCheckPreviousIndex = EarClippingTriangulator.computePreviousIndex(pVertices, collinearityCheckNextIndex);
				if(EarClippingTriangulator.isCollinear(pVertices, collinearCheckPreviousIndex)){
					pVertices.remove(collinearCheckPreviousIndex);
				}
			}
		} else if(EarClippingTriangulator.isCollinear(pVertices, collinearCheckPreviousIndex)){
			pVertices.remove(collinearCheckPreviousIndex);
		}
	}

	private static boolean isCollinear(final ArrayList<Vector2> pVertices, final int pIndex) {
		final int previousIndex = EarClippingTriangulator.computePreviousIndex(pVertices, pIndex);
		final int nextIndex = EarClippingTriangulator.computeNextIndex(pVertices, pIndex);

		return EarClippingTriangulator.isCollinear(pVertices, previousIndex, pIndex, nextIndex);
	}

	private static boolean isCollinear(final ArrayList<Vector2> pVertices, final int pPreviousIndex, final int pIndex, final int pNextIndex) {
		final Vector2 previousVertex = pVertices.get(pPreviousIndex);
		final Vector2 vertex = pVertices.get(pIndex);
		final Vector2 nextVertex = pVertices.get(pNextIndex);

		return EarClippingTriangulator.computeSpannedAreaSign(previousVertex.x, previousVertex.y, vertex.x, vertex.y, nextVertex.x, nextVertex.y) == 0;
	}

	private static int computePreviousIndex(final List<Vector2> pVertices, final int pIndex) {
		return pIndex == 0 ? pVertices.size() - 1 : pIndex - 1;
	}

	private static int computeNextIndex(final List<Vector2> pVertices, final int pIndex) {
		return pIndex == pVertices.size() - 1 ? 0 : pIndex + 1;
	}
}