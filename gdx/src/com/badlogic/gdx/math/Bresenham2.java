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

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/** Returns a list of points at integer coordinates for a line on a 2D grid, using the Bresenham algorithm.
 * <p>
 * 
 * Instances of this class own the returned array of points and the points themselves to avoid garbage collection as much as
 * possible. Calling any of the methods will result in the reuse of the previously returned array and vectors, expect
 * @author badlogic */
public class Bresenham2 {
	private final Array<GridPoint2> points = new Array<GridPoint2>();
	private final Pool<GridPoint2> pool = new Pool<GridPoint2>() {
		@Override
		protected GridPoint2 newObject () {
			return new GridPoint2();
		}
	};

	/** Returns a list of {@link GridPoint2} instances along the given line, at integer coordinates.
	 * @param start the start of the line
	 * @param end the end of the line
	 * @return the list of points on the line at integer coordinates */
	public Array<GridPoint2> line (GridPoint2 start, GridPoint2 end) {
		return line(start.x, start.y, end.x, end.y);
	}

	/** Returns a list of {@link GridPoint2} instances along the given line, at integer coordinates.
	 * @param startX the start x coordinate of the line
	 * @param startY the start y coordinate of the line
	 * @param endX the end x coordinate of the line
	 * @param endY the end y coordinate of the line
	 * @return the list of points on the line at integer coordinates */
	public Array<GridPoint2> line (int startX, int startY, int endX, int endY) {
		pool.freeAll(points);
		points.clear();
		return line(startX, startY, endX, endY, pool, points);
	}

	/** Returns a list of {@link GridPoint2} instances along the given line, at integer coordinates.
	 * @param startX the start x coordinate of the line
	 * @param startY the start y coordinate of the line
	 * @param endX the end x coordinate of the line
	 * @param endY the end y coordinate of the line
	 * @param pool the pool from which GridPoint2 instances are fetched
	 * @param output the output array, will be cleared in this method
	 * @return the list of points on the line at integer coordinates */
	public Array<GridPoint2> line (int startX, int startY, int endX, int endY, Pool<GridPoint2> pool, Array<GridPoint2> output) {

		int w = endX - startX;
		int h = endY - startY;
		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
		if (w < 0) {
			dx1 = -1;
			dx2 = -1;
		} else if (w > 0) {
			dx1 = 1;
			dx2 = 1;
		}
		if (h < 0)
			dy1 = -1;
		else if (h > 0) dy1 = 1;
		int longest = Math.abs(w);
		int shortest = Math.abs(h);
		if (longest <= shortest) {
			longest = Math.abs(h);
			shortest = Math.abs(w);
			if (h < 0)
				dy2 = -1;
			else if (h > 0) dy2 = 1;
			dx2 = 0;
		}
		int numerator = longest >> 1;
		for (int i = 0; i <= longest; i++) {
			GridPoint2 point = pool.obtain();
			point.set(startX, startY);
			output.add(point);
			numerator += shortest;
			if (numerator > longest) {
				numerator -= longest;
				startX += dx1;
				startY += dy1;
			} else {
				startX += dx2;
				startY += dy2;
			}
		}
		return output;
	}
}
