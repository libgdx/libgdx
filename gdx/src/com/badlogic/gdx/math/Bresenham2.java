package com.badlogic.gdx.math;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/**
 * Returns a list of {@link Vector2} instances at integer coordinates for
 * a line in 2D, using the Bresenham algorithm.<p>
 * 
 * Instances of this class own the returned array of vectors and the vectors
 * themselves to avoid garbage collection as much as possible. Calling
 * {@link #line(Vector2, Vector2)} or {@link #line(float, float, float, float)} will
 * result in the reuse of the previously returned array and vectors. 
 * @author badlogic
 *
 */
public class Bresenham2 {
	private final Array<Vector2> points = new Array<Vector2>();
	private final Pool<Vector2> pool = new Pool<Vector2>() {
		@Override
		protected Vector2 newObject () {
			return new Vector2();
		}
	};
	
	/**
	 * Returns a list of {@link Vector2} instances along the given line, at integer coordinates.
	 * The input coordinates are cast to integers using a floor operation.
	 * @param start the start of the line
	 * @param end the end of the line
	 * @return the list of points on the line at integer coordinates
	 */
	public Array<Vector2> line(Vector2 start, Vector2 end) {
		return line(start.x, start.y, end.y, end.y);
	}
	
	/**
	 * Returns a list of {@link Vector2} instances along the given line, at integer coordinates.
	 * The input coordinates are cast to integers using a floor operation.
	 * @param startX the start x coordinate of the line
	 * @param startY the start y coordinate of the line
	 * @param endX the end x coordinate of the line
	 * @param endY the end y coordinate of the line
	 * @return the list of points on the line at integer coordinates
	 */
	public Array<Vector2> line(float startX, float startY, float endX, float endY) {
		pool.freeAll(points);
		points.clear();
		line(startX, startY, endX, endY, pool, points);
		return points;
	}
	
	/**
	 * Returns a list of {@link Vector2} instances along the given line, at integer coordinates.
	 * The input coordinates are cast to integers using a floor operation.
	 * @param startX the start x coordinate of the line
	 * @param startY the start y coordinate of the line
	 * @param endX the end x coordinate of the line
	 * @param endY the end y coordinate of the line
	 * @param pool the pool from which Vector2 instances are fetched
	 * @param output the output array, will be cleared in this method
	 * @return the list of points on the line at integer coordinates
	 */
	public Array<Vector2> line(float startX, float startY, float endX, float endY, Pool<Vector2> pool, Array<Vector2> output) {
		int x1 = (int)startX;
		int y1 = (int)startY;
		int x2 = (int)endX;
		int y2 = (int)endY;
		
		int w = x2 - x1;
		int h = y2 - y1;
		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
		if(w < 0) {
			dx1 = -1;
			dx2 = -1;
		} else if(w > 0) {
			dx1 = 1;
			dx2 = 1;
		}
		if(h < 0) dy1 = -1; else if(h > 0) dy1 = 1;
		int longest = Math.abs(w);
		int shortest = Math.abs(h);
		if(longest <= shortest) {
			longest = Math.abs(h);
			shortest = Math.abs(w);
			if(h < 0) dy2 = -1; else if(h > 0) dy2 = 1;
			dx2 = 0;
		}
		int numerator = longest >> 1;
		for(int i = 0; i <= longest; i++) {
			Vector2 point = pool.obtain();
			point.set(x1, y1);
			output.add(point);
			numerator += shortest;
			if(numerator > longest) {
				numerator -= longest;
				x1 += dx1;
				y1 += dy1;
			} else {
				x1 += dx2;
				y1 += dy2;
			}
		}
		return output;
	}
}
