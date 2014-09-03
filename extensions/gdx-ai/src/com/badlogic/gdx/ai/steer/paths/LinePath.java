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

package com.badlogic.gdx.ai.steer.paths;

import com.badlogic.gdx.ai.steer.behaviors.FollowPath.Path;
import com.badlogic.gdx.ai.steer.paths.LinePath.LinePathParam;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.utils.Array;

/** A path for path following behaviors that is made up of a series of waypoints. Each waypoint is connected to the successor with
 * a segment.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol
 * @author Daniel Holderbaum */
public class LinePath<T extends Vector<T>> implements Path<T, LinePathParam> {

	private Array<Segment<T>> segments;
	private boolean isOpen;
	private float pathLength;
	private T nearestPointOnCurrentSegment;
	private T nearestPointOnPath;
	private T tmp1;
	private T tmp2;
	private T tmp3;

	/** Creates a closed {@code LinePath} for the specified {@code waypoints}.
	 * @param waypoints the points making up the path
	 * @throws IllegalArgumentException if {@code waypoints} is {@code null} or empty. */
	public LinePath (Array<T> waypoints) {
		this(waypoints, false);
	}

	/** Creates a {@code LinePath} for the specified {@code waypoints}.
	 * @param waypoints the points making up the path
	 * @param isOpen a flag indicating whether the path is open or not
	 * @throws IllegalArgumentException if {@code waypoints} is {@code null} or empty. */
	public LinePath (Array<T> waypoints, boolean isOpen) {
		this.isOpen = isOpen;
		createPath(waypoints);
		nearestPointOnCurrentSegment = waypoints.first().cpy();
		nearestPointOnPath = waypoints.first().cpy();
		tmp1 = waypoints.first().cpy();
		tmp2 = waypoints.first().cpy();
		tmp3 = waypoints.first().cpy();
	}

	@Override
	public boolean isOpen () {
		return isOpen;
	}

	@Override
	public float getLength () {
		return pathLength;
	}

	@Override
	public T getStartPoint () {
		return segments.first().begin;
	}

	@Override
	public T getEndPoint () {
		return segments.peek().end;
	}

	/** Returns the square distance of the nearest point on line segment {@code a-b}, from point {@code c}. Also, the {@code out}
	 * vector is assigned to the nearest point.
	 * @param out the output vector that contains the nearest point on return
	 * @param a the start point of the line segment
	 * @param b the end point of the line segment
	 * @param c the point to calculate the distance from */
	public float calculatePointSegmentSquareDistance (T out, T a, T b, T c) {
		tmp1.set(a);
		tmp2.set(b);
		tmp3.set(c);

		T ab = tmp2.sub(a);
		float t = (tmp3.sub(a)).dot(ab) / ab.len2();
		t = MathUtils.clamp(t, 0, 1);
		out.set(tmp1.add(ab.scl(t)));

		tmp1.set(out);
		T distance = tmp1.sub(c);
		return distance.len2();
	}

	@Override
	public LinePathParam createParam () {
		return new LinePathParam();
	}

	// We pass the last parameter value to the path in order to calculate the current
	// parameter value. This is essential to avoid nasty problems when lines are close together.
	// We should limit the algorithm to only considering areas of the path close to the previous
	// parameter value. The character is unlikely to have moved far, after all.
	// This technique, assuming the new value is close to the old one, is called coherence, and it is a
	// feature of many geometric algorithms.
	// TODO: Currently coherence is not implemented.
	@Override
	public float calculateDistance (T agentCurrPos, LinePathParam parameter) {
		// Find the nearest segment
		float smallestDistance2 = Float.POSITIVE_INFINITY;
		Segment<T> nearestSegment = null;
		for (int i = 0; i < segments.size; i++) {
			Segment<T> segment = segments.get(i);
			float distance2 = calculatePointSegmentSquareDistance(nearestPointOnCurrentSegment, segment.begin, segment.end,
				agentCurrPos);

			// first point
			if (distance2 < smallestDistance2) {
				nearestPointOnPath.set(nearestPointOnCurrentSegment);
				smallestDistance2 = distance2;
				nearestSegment = segment;
				parameter.segmentIndex = i;
			}
		}

		// Distance from path start
		float lengthOnPath = nearestSegment.cumulativeLength - nearestPointOnPath.dst(nearestSegment.end);

		parameter.setDistance(lengthOnPath);

		return lengthOnPath;
	}

	@Override
	public void calculateTargetPosition (T out, LinePathParam param, float targetDistance) {
		if (isOpen) {
			// Open path support
			if (targetDistance < 0) {
				// Clamp target distance to the min
				targetDistance = 0;
			} else if (targetDistance > pathLength) {
				// Clamp target distance to the max
				targetDistance = pathLength;
			}
		} else {
			// Closed path support
			if (targetDistance < 0) {
				// Backwards
				targetDistance = pathLength + (targetDistance % pathLength);
			} else if (targetDistance > pathLength) {
				// Forward
				targetDistance = targetDistance % pathLength;
			}
		}

		// Walk through lines to see on which line we are
		Segment<T> desiredSegment = null;
		for (int i = 0; i < segments.size; i++) {
			Segment<T> segment = segments.get(i);
			if (segment.cumulativeLength >= targetDistance) {
				desiredSegment = segment;
				break;
			}
		}

		// begin-------targetPos-------end
		float distance = desiredSegment.cumulativeLength - targetDistance;

		out.set(desiredSegment.begin).sub(desiredSegment.end).scl(distance / desiredSegment.length).add(desiredSegment.end);
	}

	/** Sets up this {@link Path} using the given way points.
	 * @param waypoints The way points of this path. 
	 * @throws IllegalArgumentException if {@code waypoints} is {@code null} or empty. */
	public void createPath (Array<T> waypoints) {
		if (waypoints == null || waypoints.size == 0) throw new IllegalArgumentException("waypoints cannot be null or empty");

		segments = new Array<Segment<T>>(waypoints.size);
		pathLength = 0;
		T curr = waypoints.first();
		T prev = null;
		for (int i = 1; i <= waypoints.size; i++) {
			prev = curr;
			if (i < waypoints.size)
				curr = waypoints.get(i);
			else if (isOpen)
				break; // keep the path open
			else
				curr = waypoints.first(); // close the path
			Segment<T> segment = new Segment<T>(prev, curr);
			pathLength += segment.length;
			segment.cumulativeLength = pathLength;
			segments.add(segment);
		}
	}

	public Array<Segment<T>> getSegments () {
		return segments;
	}

	public static class LinePathParam implements Param {
		int segmentIndex;
		float distance;

		@Override
		public float getDistance () {
			return distance;
		}

		@Override
		public void setDistance (float distance) {
			this.distance = distance;
		}

	}

	public static class Segment<T extends Vector<T>> {
		T begin;
		T end;
		float length;
		float cumulativeLength;

		Segment (T begin, T end) {
			this.begin = begin;
			this.end = end;
			this.length = begin.dst(end);
		}

		public T getBegin () {
			return begin;
		}

		public T getEnd () {
			return end;
		}

		public float getLength () {
			return length;
		}

		public float getCumulativeLength () {
			return cumulativeLength;
		}
	}
}
