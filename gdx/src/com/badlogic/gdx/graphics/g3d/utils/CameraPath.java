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

package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector3;

/** Camera path for transitioning camera along a curve in a given duration 
 * @author florianbaethge (evident) */
public class CameraPath {

	/** control points for the path */
	Vector3[] keys;
	private boolean continuous;
	private float approxLength;
	private float avg_speed;
	CatmullRomSpline<Vector3> spline;

	private boolean running = false;
	private int loopingsRemaining;
	private boolean constSpeed;
	private float t = 0;
	private float timePassed = 0;
	private float duration;

	private Vector3 tmp = new Vector3(Vector3.Y);
	private Vector3 tmp2 = new Vector3(Vector3.Y);

	public CameraPath () {
		spline = new CatmullRomSpline<Vector3>();
	}

	public CameraPath (final Vector3[] keyPoints, final boolean continuous) {
		spline = new CatmullRomSpline<Vector3>();
		set(keyPoints, continuous);
	}

	private void set (final Vector3[] keyPoints, final boolean continuous) {
		this.keys = keyPoints;
		this.continuous = continuous;

		spline.set(keys, continuous);

		// approximate curve length and speed
		approxLength = spline.approxLength(500);
	}

	/**
	 * Advances the parameters of the path and updates the current position on the
	 * path when the CameraPath is currently running
	 * @param delta the delta time since rendering the last frame
	 */
	public void update (float delta) {
		if (running) {
			timePassed += delta;

			if (timePassed >= duration) { // end reached, reduce loopings and stop if necessary
				loopingsRemaining--;
				if (loopingsRemaining == 0) {
					this.running = false;
					return;
				}
				timePassed = 0;

			}

			if (constSpeed) { // calculate new t parameter based on chosen velocity type
				t += delta * avg_speed / tmp.len();
				if (t >= 1f) t -= 1f;
				spline.derivativeAt(tmp, t);
			} else {
				t = timePassed / duration;
			}
		}
	}

	/**
	 * @return the current position on the path
	 */
	public Vector3 getValue () {
		spline.valueAt(tmp2, t);
		return tmp2;
	}

	/**
	 * Starts the path and sets its state to running
	 * @param duration The time in seconds, the camera animation should take
	 * @param loopings The amount of loopings this path should transform. Setting it to a negative value (-1) will result in infinite motion
	 * @param constSpeed if set to true, the camera will move along this path with a constant speed rather than the speed given by its parametrization. Might result in a more smooth animation in some cases
	 */
	public void start (float duration, int loopings, boolean constSpeed) {
		this.duration = duration;
		this.loopingsRemaining = loopings;
		this.constSpeed = constSpeed;
		this.t = 0;
		this.timePassed = 0;

		avg_speed = approxLength / (keys.length * duration);

		this.running = true;
	}

	/**
	 * stops the path and resets the position to the starting position
	 */
	public void stop () {
		this.running = false;
		this.timePassed = 0;
		this.t = 0;
	}

	/**
	 * pauses the camera path, but leaves it at its current state
	 */
	public void pause () {
		this.running = false;
	}

	/**
	 * resumes a paused camera path
	 */
	public void resume () {
		this.running = true;
	}
}
