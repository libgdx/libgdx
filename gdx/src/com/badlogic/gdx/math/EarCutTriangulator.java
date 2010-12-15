/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
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
 * A simple implementation of the ear cutting algorithm to triangulate simple polygons without holes. For more information see
 * http://cgm.cs.mcgill.ca/~godfried/teaching/cg-projects/97/Ian/algorithm2.html
 * @author badlogicgames@gmail.com
 * 
 */
public final class EarCutTriangulator {
	/**
	 * Triangulates the list of points and returns an array of {@link Vector3} triples that each form a single triangle.
	 * 
	 * @param polygon The polygon to triangulate
	 * @return The list of triangle vertices.
	 */
	public List<Vector2> triangulate (List<Vector2> polygon) {
		List<Vector2> triangles = new ArrayList<Vector2>();
		List<Vector2> tmp = new ArrayList<Vector2>(polygon.size());
		tmp.addAll(polygon);
		polygon = tmp;

		if (polygon.size() == 3) {
			triangles.addAll(polygon);
			return triangles;
		}

		while (polygon.size() >= 3) {
			int ptType[] = classifyPoints(polygon);

			for (int i = 0; i < polygon.size(); i++) {
				float x1 = polygon.get(i == 0 ? polygon.size() - 1 : i - 1).x;
				float y1 = polygon.get(i == 0 ? polygon.size() - 1 : i - 1).y;
				float x2 = polygon.get(i).x;
				float y2 = polygon.get(i).y;
				float x3 = polygon.get(i == polygon.size() - 1 ? 0 : i + 1).x;
				float y3 = polygon.get(i == polygon.size() - 1 ? 0 : i + 1).y;

				if (ear(polygon, ptType, x1, y1, x2, y2, x3, y3)) {

					cutEar(polygon, triangles, i);
					updatePolygon(polygon, i);
					break;
				}
			}
		}

// if( polygon.size() == 3 )
// {
// triangles.add( polygon.get(0) );
// triangles.add( polygon.get(1) );
// triangles.add( polygon.get(2) );
// }

		return triangles;
	}

	/*
	 * polygonClockwise: Returns true if user inputted polygon in clockwise order, false if counterclockwise. The Law of Cosines is
	 * used to determine the angle.
	 */
	public boolean polygonClockwise (List<Vector2> polygon) {
		float area = 0;
		for (int i = 0; i < polygon.size(); i++) {
			Vector2 p1 = polygon.get(i);
			Vector2 p2 = polygon.get(i == polygon.size() - 1 ? 0 : i + 1);
			area += p1.x * p2.y - p2.x * p1.y;
		}

		if (area < 0)
			return true;
		else
			return false;

// float aa, bb, cc, b, c, theta;
// float convex_turn;
// float convex_sum = 0;
//
// for (int i = 0; i < polygon.size() - 2; i++) {
// aa = ((polygon.get(i+2).getX() - polygon.get(i).getX()) * (polygon.get(i+2).getX() - polygon.get(i).getX())) +
// ((-polygon.get(i+2).getY() + polygon.get(i).getY()) * (-polygon.get(i+2).getY() + polygon.get(i).getY()));
//
// bb = ((polygon.get(i+1).getX() - polygon.get(i).getX()) * (polygon.get(i+1).getX() - polygon.get(i).getX()) +
// ((-polygon.get(i+1).getY() + polygon.get(i).getY()) * (-polygon.get(i+1).getY() + polygon.get(i).getY())));
//
// cc = ((polygon.get(i+2).getX() - polygon.get(i+1).getX()) *
// (polygon.get(i+2).getX() - polygon.get(i+1).getX())) +
// ((-polygon.get(i+2).getY() + polygon.get(i+1).getY()) *
// (-polygon.get(i+2).getY() + polygon.get(i+1).getY()));
//
// b = (float)Math.sqrt(bb);
// c = (float)Math.sqrt(cc);
// theta = (float)Math.acos((bb + cc - aa) / (2 * b * c));
//
// if (convex(polygon.get(i).getX(), polygon.get(i).getY(),
// polygon.get(i+1).getX(), polygon.get(i+1).getY(),
// polygon.get(i+2).getX(), polygon.get(i+2).getY())) {
// convex_turn = (float)Math.PI - theta;
// convex_sum += convex_turn;
// }
// else {
// convex_sum -= Math.PI - theta;
// }
// }
// aa = ((polygon.get(1).getX() - polygon.get(polygon.size()-1).getX()) *
// (polygon.get(1).getX() - polygon.get(polygon.size()-1).getX())) +
// ((-polygon.get(1).getY() + polygon.get(polygon.size()-1).getY()) *
// (-polygon.get(1).getY() + polygon.get(polygon.size()-1).getY()));
//
// bb = ((polygon.get(0).getX() - polygon.get(polygon.size()-1).getX()) *
// (polygon.get(0).getX() - polygon.get(polygon.size()-1).getX())) +
// ((-polygon.get(0).getY() + polygon.get(polygon.size()-1).getY()) *
// (-polygon.get(0).getY() + polygon.get(polygon.size()-1).getY()));
//
// cc = ((polygon.get(1).getX() - polygon.get(0).getX()) *
// (polygon.get(1).getX() - polygon.get(0).getX())) +
// ((-polygon.get(1).getY() + polygon.get(0).getY()) *
// (-polygon.get(1).getY() + polygon.get(0).getY()));
//
// b = (float)Math.sqrt(bb);
// c = (float)Math.sqrt(cc);
// theta = (float)Math.acos((bb + cc - aa) / (2 * b * c));
//
// if (convex(polygon.get(polygon.size()-1).getX(), polygon.get(polygon.size()-1).getY(),
// polygon.get(0).getX(), polygon.get(0).getY(),
// polygon.get(1).getX(), polygon.get(1).getY())) {
// convex_turn = (float)Math.PI - theta;
// convex_sum += convex_turn;
// }
// else {
// convex_sum -= Math.PI - theta;
// }
//
// if (convex_sum >= (2 * 3.14159))
// return true;
// else
// return false;
	}

	/*
	 * classifyPoints: Classifies points as "convex" or "concave". Convex points are represented as a "1" in the ptType array;
	 * concave points are represented as a "-1" in the array.
	 */
	int concaveCount = 0;

	int[] classifyPoints (List<Vector2> polygon) {
		int[] ptType = new int[polygon.size()];
		concaveCount = 0;

		/*
		 * Before cutting any ears, we must determine if the polygon was inputted in clockwise order or not, since the algorithm for
		 * cutting ears assumes that the polygon's points are in clockwise order. If the points are in counterclockwise order, they
		 * are simply reversed in the array.
		 */
		if (!polygonClockwise(polygon)) {
			Collections.reverse(polygon);
		}

		for (int i = 0; i < polygon.size(); i++) {
			if (i == 0) {
				if (convex(polygon.get(polygon.size() - 1).x, polygon.get(polygon.size() - 1).y, polygon.get(i).x, polygon.get(i).y,
					polygon.get(i + 1).x, polygon.get(i + 1).y)) {
					ptType[i] = 1; /* point is convex */
				} else {
					ptType[i] = -1; /* point is concave */
					concaveCount++;
				}
			} else if (i == polygon.size() - 1) {
				if (convex(polygon.get(i - 1).x, polygon.get(i - 1).y, polygon.get(i).x, polygon.get(i).y, polygon.get(0).x,
					polygon.get(0).y)) {
					ptType[i] = 1; /* point is convex */
				} else {
					ptType[i] = -1; /* point is concave */
					concaveCount++;
				}
			} else { /* i > 0 */
				if (convex(polygon.get(i - 1).x, polygon.get(i - 1).y, polygon.get(i).x, polygon.get(i).y, polygon.get(i + 1).x,
					polygon.get(i + 1).y)) {
					ptType[i] = 1; /* point is convex */
				} else {
					ptType[i] = -1; /* point is concave */
					concaveCount++;
				}
			}
		}

		return ptType;
	}

	/*
	 * convex: returns true if point (x2, y2) is convex
	 */
	boolean convex (float x1, float y1, float x2, float y2, float x3, float y3) {
		if (area(x1, y1, x2, y2, x3, y3) < 0)
			return false;
		else
			return true;
	}

	/*
	 * area: determines area of triangle formed by three points
	 */
	float area (float x1, float y1, float x2, float y2, float x3, float y3) {
		float areaSum = 0;

		areaSum += x1 * (y3 - y2);
		areaSum += x2 * (y1 - y3);
		areaSum += x3 * (y2 - y1);

		/*
		 * for actual area, we need to multiple areaSum * 0.5, but we are only interested in the sign of the area (+/-)
		 */

		return areaSum;
	}

	/*
	 * triangleContainsPoints: returns true if the triangle formed by three points contains another point
	 */
	boolean triangleContainsPoint (List<Vector2> polygon, int[] ptType, float x1, float y1, float x2, float y2, float x3, float y3) {
		int i = 0;
		float area1, area2, area3;
		boolean noPointInTriangle = true;

		while ((i < polygon.size() - 1) && (noPointInTriangle)) {
			if ((ptType[i] == -1) /* point is concave */
				&& (((polygon.get(i).x != x1) && (polygon.get(i).y != y1)) || ((polygon.get(i).x != x2) && (polygon.get(i).y != y2)) || ((polygon
					.get(i).x != x3) && (polygon.get(i).y != y3)))) {

				area1 = area(x1, y1, x2, y2, polygon.get(i).x, polygon.get(i).y);
				area2 = area(x2, y2, x3, y3, polygon.get(i).x, polygon.get(i).y);
				area3 = area(x3, y3, x1, y1, polygon.get(i).x, polygon.get(i).y);

				if (area1 > 0) if ((area2 > 0) && (area3 > 0)) noPointInTriangle = false;
				if (area1 <= 0) if ((area2 <= 0) && (area3 <= 0)) noPointInTriangle = false;
			}
			i++;
		}
		return !noPointInTriangle;
	}

	/*
	 * ear: returns true if the point (x2, y2) is an ear, false otherwise
	 */
	boolean ear (List<Vector2> polygon, int[] ptType, float x1, float y1, float x2, float y2, float x3, float y3) {
		if (concaveCount != 0)
			if (triangleContainsPoint(polygon, ptType, x1, y1, x2, y2, x3, y3))
				return false;
			else
				return true;
		else
			return true;
	}

	/*
	 * cutEar: creates triangle that represents ear for graphics purposes
	 */
	void cutEar (List<Vector2> polygon, List<Vector2> triangles, int index) {
		if (index == 0) {
			triangles.add(new Vector2(polygon.get(polygon.size() - 1)));
			triangles.add(new Vector2(polygon.get(index)));
			triangles.add(new Vector2(polygon.get(index + 1)));
		} else if ((index > 0) && (index < polygon.size() - 1)) {
			triangles.add(new Vector2(polygon.get(index - 1)));
			triangles.add(new Vector2(polygon.get(index)));
			triangles.add(new Vector2(polygon.get(index + 1)));
		} else if (index == polygon.size() - 1) {
			triangles.add(new Vector2(polygon.get(index - 1)));
			triangles.add(new Vector2(polygon.get(index)));
			triangles.add(new Vector2(polygon.get(0)));
		}
	}

	/*
	 * updatePolygon: creates new polygon without the ear that was cut
	 */
	void updatePolygon (List<Vector2> polygon, int index) {
		polygon.remove(index);
	}

}
