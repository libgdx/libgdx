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
package com.badlogic.gdx.box2deditor.earclipping;

import com.badlogic.gdx.math.Vector2;

/**
 * Original code from EwJordan (http://www.ewjordan.com/earClip/)
 * @author Aurelien Ribon (aurelien.ribon@gmail.com)
 */
public class Clipper {
	public static Vector2[][] polygonize(Vector2[] points) {
		int vNum = points.length;
		float[] xv = new float[vNum];
		float[] yv = new float[vNum];

		for (int i = 0; i < vNum; i++) {
			xv[i] = points[i].x;
			yv[i] = points[i].y;
		}

		Triangle[] tempTriangles = triangulatePolygon(xv, yv, vNum);
		Polygon[] tempPolygons = polygonizeTriangles(tempTriangles);

		if (tempPolygons == null)
			return null;

		Vector2[][] polygons = new Vector2[tempPolygons.length][];
		for (int i = 0; i < tempPolygons.length; i++) {
			polygons[i] = new Vector2[tempPolygons[i].nVertices];
			for (int ii = 0; ii < tempPolygons[i].nVertices; ii++)
				polygons[i][ii] = new Vector2(tempPolygons[i].x[ii], tempPolygons[i].y[ii]);
		}

		polygons = updateForBox2D(polygons);
		return polygons;
	}

	private static Vector2[][] updateForBox2D(Vector2[][] polygons) {
		for (int i = 0; i < polygons.length; i++) {
			Vector2[] poly = polygons[i];
			if (poly.length > 8) {
				int limit = poly.length < 15 ? poly.length / 2 + 1 : 8;
				Vector2[] newPoly1 = new Vector2[limit];
				Vector2[] newPoly2 = new Vector2[poly.length - limit + 2];
				System.arraycopy(poly, 0, newPoly1, 0, limit);
				System.arraycopy(poly, limit - 1, newPoly2, 0, poly.length - limit + 1);
				newPoly2[newPoly2.length - 1] = poly[0].cpy();

				Vector2[][] newPolys = new Vector2[polygons.length + 1][];
				if (i > 0) {
					System.arraycopy(polygons, 0, newPolys, 0, i);
				}
				if (i < polygons.length - 1) {
					System.arraycopy(polygons, i + 1, newPolys, i + 2, polygons.length - i - 1);
				}
				newPolys[i] = newPoly1;
				newPolys[i + 1] = newPoly2;
				polygons = newPolys;

				i -= 1;
			}
		}
		return polygons;
	}

	// -------------------------------------------------------------------------

	private static Triangle[] triangulatePolygon(float[] xv, float[] yv, int vNum) {
		if (vNum < 3)
			return null;

		Triangle[] buffer = new Triangle[vNum];
		int bufferSize = 0;
		float[] xrem = new float[vNum];
		float[] yrem = new float[vNum];
		for (int i = 0; i < vNum; ++i) {
			xrem[i] = xv[i];
			yrem[i] = yv[i];
		}

		while (vNum > 3) {
			int earIndex = -1;
			for (int i = 0; i < vNum; ++i) {
				if (isEar(i, xrem, yrem)) {
					earIndex = i;
					break;
				}
			}

			if (earIndex == -1)
				return null;

			--vNum;
			float[] newx = new float[vNum];
			float[] newy = new float[vNum];
			int currDest = 0;
			for (int i = 0; i < vNum; ++i) {
				if (currDest == earIndex) {
					++currDest;
				}
				newx[i] = xrem[currDest];
				newy[i] = yrem[currDest];
				++currDest;
			}

			int under = (earIndex == 0) ? (xrem.length - 1) : (earIndex - 1);
			int over = (earIndex == xrem.length - 1) ? 0 : (earIndex + 1);

			Triangle toAdd = new Triangle(xrem[earIndex], yrem[earIndex], xrem[over], yrem[over], xrem[under], yrem[under]);
			buffer[bufferSize] = toAdd;
			++bufferSize;

			xrem = newx;
			yrem = newy;
		}
		Triangle toAdd = new Triangle(xrem[1], yrem[1], xrem[2], yrem[2], xrem[0], yrem[0]);
		buffer[bufferSize] = toAdd;
		++bufferSize;

		Triangle[] res = new Triangle[bufferSize];
		System.arraycopy(buffer, 0, res, 0, bufferSize);
		return res;
	}

	private static Polygon[] polygonizeTriangles(Triangle[] triangulated) {
		Polygon[] polys;
		int polyIndex = 0;

		if (triangulated == null)
			return null;

		polys = new Polygon[triangulated.length];
		boolean[] covered = new boolean[triangulated.length];
		for (int i = 0; i < triangulated.length; i++)
			covered[i] = false;

		boolean notDone = true;

		while (notDone) {
			int currTri = -1;
			for (int i = 0; i < triangulated.length; i++) {
				if (!covered[i]) {
					currTri = i;
					break;
				}
			}

			if (currTri == -1) {
				notDone = false;
			} else {
				Polygon poly = new Polygon(triangulated[currTri]);
				covered[currTri] = true;
				for (int i = 0; i < triangulated.length; i++) {
					if (covered[i])
						continue;

					Polygon newP = poly.add(triangulated[i]);
					if (newP == null)
						continue;

					if (newP.isConvex()) {
						poly = newP;
						covered[i] = true;
					}
				}
				polys[polyIndex] = poly;
				polyIndex++;
			}
		}

		Polygon[] ret = new Polygon[polyIndex];
		System.arraycopy(polys, 0, ret, 0, polyIndex);
		return ret;
	}

	private static boolean isEar(int i, float[] xv, float[] yv) {
		float dx0, dy0, dx1, dy1;
		dx0 = dy0 = dx1 = dy1 = 0;

		if (i >= xv.length || i < 0 || xv.length < 3)
			return false;

		int upper = i + 1;
		int lower = i - 1;

		if (i == 0) {
			dx0 = xv[0] - xv[xv.length - 1];
			dy0 = yv[0] - yv[yv.length - 1];
			dx1 = xv[1] - xv[0];
			dy1 = yv[1] - yv[0];
			lower = xv.length - 1;
		} else if (i == xv.length - 1) {
			dx0 = xv[i] - xv[i - 1];
			dy0 = yv[i] - yv[i - 1];
			dx1 = xv[0] - xv[i];
			dy1 = yv[0] - yv[i];
			upper = 0;
		} else {
			dx0 = xv[i] - xv[i - 1];
			dy0 = yv[i] - yv[i - 1];
			dx1 = xv[i + 1] - xv[i];
			dy1 = yv[i + 1] - yv[i];
		}

		float cross = dx0 * dy1 - dx1 * dy0;
		if (cross > 0)
			return false;

		Triangle myTri = new Triangle(xv[i], yv[i], xv[upper], yv[upper], xv[lower], yv[lower]);
		for (int j = 0; j < xv.length; ++j) {
			if (j == i || j == lower || j == upper)
				continue;
			if (myTri.isInside(xv[j], yv[j]))
				return false;
		}

		return true;
	}
}