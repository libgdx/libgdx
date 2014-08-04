
package com.badlogic.gdx.ai.steer.paths;

import com.badlogic.gdx.math.Vector2;

/** @author davebaol */
public class LinePath2D extends LinePath<Vector2> {

	public LinePath2D (Vector2[] waypoints) {
		super(waypoints);
	}

	@Override
	public float calculatePointSegmentSquareDistance (Vector2 out, Vector2 a, Vector2 b, Vector2 c) {

		float ax = a.x;
		float ay = a.y;
		float bx = b.x;
		float by = b.y;
		float cx = c.x;
		float cy = c.y;

		float r_numerator = (cx - ax) * (bx - ax) + (cy - ay) * (by - ay);
		float r_denominator = (bx - ax) * (bx - ax) + (by - ay) * (by - ay);
		float r = r_numerator / r_denominator;

		if (r >= 0 && r <= 1) {
			float px = ax + r * (bx - ax);
			float py = ay + r * (by - ay);
			out.x = px;
			out.y = py;
			float s = ((ay - cy) * (bx - ax) - (ax - cx) * (by - ay)) / r_denominator;
			return s * s * r_denominator;
		}

		float squareDist1 = (cx - ax) * (cx - ax) + (cy - ay) * (cy - ay);
		float squareDist2 = (cx - bx) * (cx - bx) + (cy - by) * (cy - by);

		if (squareDist1 < squareDist2) {
			out.x = ax;
			out.y = ay;
			return squareDist1;
		}

		out.x = bx;
		out.y = by;
		return squareDist2;
	}

}
