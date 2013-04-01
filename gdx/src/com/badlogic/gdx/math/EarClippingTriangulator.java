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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.utils.GdxRuntimeException;

/** A simple implementation of the ear cutting algorithm to triangulate simple polygons without holes.
 * @author seroperson */
public final class EarClippingTriangulator {

	private final static Vector2[] copies = new Vector2[] { new Vector2(), new Vector2(), new Vector2()};
	private final ArrayList<Vector2> source;
	private final ArrayList<Vector2> triangles;

	private EarClippingTriangulator (final int size) {
		source = new ArrayList<Vector2>(size);
		triangles = new ArrayList<Vector2>(size - 2);
	}
	
	public EarClippingTriangulator () {
		this(3);
	}

	public EarClippingTriangulator (final List<Vector2> vert) {
		this(vert.size());
		setVertexList(vert);
	}

	public EarClippingTriangulator (final Vector2[] vert) {
		this(vert.length);
		setVertexList(vert);
	}

	public void setVertexList (final List<Vector2> vert) {
		if (vert == null) throw new IllegalArgumentException("list of vertices cannot be null");
		if (vert.size() < 3) throw new IllegalArgumentException("polygon vertex count < 3");
		source.clear();
		source.addAll(vert);
		if (areVerticesClockwise(source)) Collections.reverse(source);
	}

	public void setVertexList (final Vector2[] vert) {
		if (vert == null) throw new IllegalArgumentException("list of vertices cannot be null");
		if (vert.length < 3) throw new IllegalArgumentException("polygon vertex count < 3");
		source.clear();
		source.addAll(Arrays.asList(vert));
		if (areVerticesClockwise(source)) Collections.reverse(source);

	}

	private static boolean areVerticesClockwise (final List<Vector2> vert) {
		final int vertexCount = vert.size();
		float area = 0;
		for (int i = 0; i < vertexCount - 1; i++) {
			final Vector2 p1 = vert.get(i);
			final Vector2 p2 = vert.get(i + 1);
			area += p1.x * p2.y - p2.x * p1.y;
		}
		final Vector2 p1 = vert.get(vertexCount - 1);
		final Vector2 p2 = vert.get(0);
		area += p1.x * p2.y - p2.x * p1.y;

		return area < 0;
	}
	
	private List<Vector2> basicCompute (final List<Vector2> vert, final CollinearTrackingLevel level) {
		final Vector2[] temparr = new Vector2[3];
		
		if (level.equals(CollinearTrackingLevel.STRONG)) {
			float crsarea;
			for (int i = 0; i < vert.size();) {
				if (i < 0) i = 0;
				fillTempArray(i, temparr, vert);
				crsarea = getCrsArea(temparr);
				if (crsarea == 0) {
					vert.remove(i--);
					if(vert.size() <= 3)
						return null;
					continue;
				}
				i++;
			}
		}

		return basicCompute(vert, level, temparr);
	}

	private List<Vector2> basicCompute (final List<Vector2> vert, final CollinearTrackingLevel level, final Vector2[] temparr) {
		int i = vert.size() - 1;
		int lap = 0;
		float crsarea = 0;
		boolean continuewhile = false;
		final int[] indexs = new int[3];
		
		triangles.clear();
		
		while (vert.size() != 3) {
						
			fillTempArray(i, temparr, indexs, vert);
			crsarea = getCrsArea(temparr);

			if (crsarea < 0f) {
				i--;
				if (i == -1) {
					lap++;
					i = vert.size() - 1;
				}
				if (lap != 0) if (lap > triangles.size() / lap) {
					final int size = triangles.size()/3;
					triangles.clear();
					source.clear();
					throw new GdxRuntimeException("error in triangulation process; Lap: "+lap+"; Triangles: "+size+";");
				}
				continue;
			}

			final int size = vert.size();

			if (indexs[2] - indexs[1] == 1) {
				if (indexs[1] - indexs[0] == 1) {
					for (int ii = 0; ii < size; ii++) {
						if (ii == indexs[0]) {
							ii += 3;
							for (; ii < size; ii++) {
								if (pointInTriangle(temparr[0], temparr[1], temparr[2], vert.get(ii))) {
									continuewhile = true;
									break;
								}
							}
							break;
						}
						if (pointInTriangle(temparr[0], temparr[1], temparr[2], vert.get(ii))) {
							continuewhile = true;
							break;
						}
					}
				} else if (indexs[0] > indexs[1]) for (int ii = 2; ii < size - 1; ii++)
					if (pointInTriangle(temparr[0], temparr[1], temparr[2], vert.get(ii))) {
						continuewhile = true;
						break;

					}
			} else if (indexs[2] < indexs[1]) for (int ii = 1; ii < size - 2; ii++)
				if (pointInTriangle(temparr[0], temparr[1], temparr[2], vert.get(ii))) {
					continuewhile = true;
					break;
				}

			if (continuewhile) {
				i--;
				if (i == -1) {
					lap++;
					i = vert.size() - 1;
				}
				continuewhile = false;
				continue;
			}

			putTempArray(triangles, temparr);

			vert.remove(i);
			i--;
			if (i == -1) {
				lap++;
				i = vert.size() - 1;
			}
		}

		fillTempArray(0, temparr, vert);

		putTempArray(triangles, temparr);

		if (!level.equals(CollinearTrackingLevel.IGNORE)) {
			i = 0;
			while (i < triangles.size()) {
				temparr[0] = triangles.get(i);
				temparr[1] = triangles.get(i + 1);
				temparr[2] = triangles.get(i + 2);
				crsarea = getCrsArea(temparr);
				if (crsarea == 0) {
					triangles.remove(i);
					triangles.remove(i);
					triangles.remove(i);
				}
				i += 3;
			}
		}

		source.clear();
		
		return triangles;
	}

	public List<Vector2> computeTriangles () {
		return computeTriangles(CollinearTrackingLevel.STRONG);
	}
	
	public List<Vector2> computeTriangles (final CollinearTrackingLevel level) {
		if(source.size() != 0)
			return basicCompute(source, level);
		else
			throw new IllegalArgumentException("list of vertices cannot be empty");
	}
	
	/** Triangulates the given (concave) polygon to a list of triangles. 
	 * @throws GdxRuntimeException if error in triangulation process (usually, it is polygon intersect by itself)
	 * @return the triangles */	
	public List<Vector2> computeTriangles (final List<Vector2> vert) {
		return computeTriangles(vert, CollinearTrackingLevel.STRONG);
	}
	
	public List<Vector2> computeTriangles (final List<Vector2> vert, final CollinearTrackingLevel level) {
		setVertexList(vert);
		return basicCompute(source, level);
	}
	
	public List<Vector2> getTriangulatedPolygon () {
		return triangles;
	}
	
	public Vector2[][] getTriangulatedPolygonAsArray () { 
		Vector2[][] triangles_array = new Vector2[triangles.size()/3][3];
		int i;
		int ii;
		for(i = 0; i < triangles_array.length; i++) { 
			for(ii = 0; ii < 3; ii++)
				triangles_array[i][ii] = triangles.get(i*3+ii);
		}
		return triangles_array;
	}

	private static void putTempArray (final List<Vector2> triangles, final Vector2[] temparr) {
		triangles.add(temparr[0]);
		triangles.add(temparr[1]);
		triangles.add(temparr[2]);
	}

	private static void fillTempArray (final int index, final Vector2[] temparr, final int[] indexarr, final List<Vector2> vert) {
		if (index == 0) {
			indexarr[0] = vert.size() - 1;
			indexarr[1] = index;
			indexarr[2] = index + 1;
			temparr[0] = vert.get(indexarr[0]);
			temparr[1] = vert.get(index);
			temparr[2] = vert.get(index + 1);

		} else if (index == vert.size() - 1) {
			indexarr[0] = index - 1;
			indexarr[1] = index;
			indexarr[2] = 0;
			temparr[0] = vert.get(index - 1);
			temparr[1] = vert.get(index);
			temparr[2] = vert.get(0);
		} else {
			indexarr[0] = index - 1;
			indexarr[1] = index;
			indexarr[2] = index + 1;
			temparr[0] = vert.get(index - 1);
			temparr[1] = vert.get(index);
			temparr[2] = vert.get(index + 1);
		}
	}

	private static void fillTempArray (final int index, final Vector2[] temparr, final List<Vector2> vert) {
		if (index == 0) {
			temparr[0] = vert.get(vert.size() - 1);
			temparr[1] = vert.get(index);
			temparr[2] = vert.get(index + 1);
		} else if (index == vert.size() - 1) {
			temparr[0] = vert.get(index - 1);
			temparr[1] = vert.get(index);
			temparr[2] = vert.get(0);
		} else {
			temparr[0] = vert.get(index - 1);
			temparr[1] = vert.get(index);
			temparr[2] = vert.get(index + 1);
		}
	}

	private static boolean pointInTriangle (final Vector2 f, final Vector2 t, final Vector2 th, final Vector2 p) {
		int c = 0;
		if (calculateIncrement(f, t, p)) c++;
		if (calculateIncrement(t, th, p)) c++;
		if (calculateIncrement(th, f, p)) c++;
		return c == 1;
	}

	private static boolean calculateIncrement (final Vector2 v, final Vector2 V, final Vector2 p) {
		if (((v.y <= p.y && p.y < V.y) || (V.y <= p.y && p.y < v.y)))
			if (p.x < ((V.x - v.x) / (V.y - v.y) * (p.y - v.y) + v.x)) return true;
		return false;
	}

	private static float getCrsArea (final Vector2[] triangle) {
		copies[0].set(triangle[0]);
		copies[1].set(triangle[1]);
		copies[2].set(triangle[2]);
		copies[1].sub(copies[0]);
		copies[2].sub(copies[0]);
		return copies[1].crs(copies[2]) * 0.5f;
	}

	/** Sets a level of handling of collinear vectors for {@link #computeTriangles(CollinearTrackingLevel)} */
	public enum CollinearTrackingLevel {
		STRONG(0), MEDIUM(1), IGNORE(2);

		final int level;

		CollinearTrackingLevel (int l) {
			level = l;
		}

		public int getID () {
			return level;
		}
	}

}
