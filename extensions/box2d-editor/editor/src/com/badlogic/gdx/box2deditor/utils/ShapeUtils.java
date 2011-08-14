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
package com.badlogic.gdx.box2deditor.utils;

import com.badlogic.gdx.math.Vector2;

/**
 *
 * @author Aurelien Ribon (aurelien.ribon@gmail.com)
 */
public class ShapeUtils {
	public static float getPolygonSignedArea(Vector2[] points) {
		if (points.length < 3)
			return 0;

		float sum = 0;
		for (int i = 0; i < points.length; i++) {
			Vector2 p1 = points[i];
			Vector2 p2 = i != points.length-1 ? points[i+1] : points[0];
			sum += (p1.x * p2.y) - (p1.y * p2.x);
		}
		return 0.5f * sum;
	}

	public static float getPolygonArea(Vector2[] points) {
		return Math.abs(getPolygonSignedArea(points));
	}

	public static boolean isPolygonCCW(Vector2[] points) {
		return getPolygonSignedArea(points) > 0;
	}
}