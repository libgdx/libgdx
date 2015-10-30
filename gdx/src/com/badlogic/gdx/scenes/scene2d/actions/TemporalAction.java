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

package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Interpolation.SplineInterpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.utils.Pool;

/** Base class for actions that transition over time using the percent complete.
 * @author Nathan Sweet */
abstract public class TemporalAction extends Action {
	private float duration, time;
	private Interpolation interpolation;
	private SplineInterpolation splineInterpolation; // To avoid repeated casts. Non-null if and only if there are starting speeds
																		// and interpolation is a spline interpolation.
	private float startSpeed0, startSpeed1, startSpeed2, startSpeed3;
	private int startSpeedsUsed;
	private boolean reverse, began, complete;

	public TemporalAction () {
	}

	public TemporalAction (float duration) {
		this.duration = duration;
	}

	public TemporalAction (float duration, Interpolation interpolation) {
		this.duration = duration;
		this.interpolation = interpolation;
	}

	public boolean act (float delta) {
		if (complete) return true;
		Pool pool = getPool();
		setPool(null); // Ensure this action can't be returned to the pool while executing.
		try {
			if (!began) {
				begin();
				began = true;
			}
			time += delta;
			complete = time >= duration;
			if (complete) {
				update(reverse ? 0f : 1f);
			} else if (splineInterpolation == null) {
				float percent = time / duration;
				if (interpolation != null) percent = interpolation.apply(percent);
				update(reverse ? 1 - percent : percent);
			} else {
				float percent = time / duration;
				float percent0 = splineInterpolation.applyWithSpeed(startSpeed0, percent);
				if (startSpeedsUsed == 1) {
					update(reverse ? 1 - percent0 : percent0);
				} else {
					float percent1 = 0f, percent2 = 0f, percent3 = 0f;
					switch (startSpeedsUsed) {
					case 4:
						percent3 = splineInterpolation.applyWithSpeed(startSpeed3, percent);
					case 3:
						percent2 = splineInterpolation.applyWithSpeed(startSpeed2, percent);
					case 2:
						percent1 = splineInterpolation.applyWithSpeed(startSpeed1, percent);
					}
					if (reverse)
						updateIndependently(1 - percent0, 1 - percent1, 1 - percent2, 1 - percent3);
					else
						updateIndependently(percent0, percent1, percent2, percent3);
				}
			}
			if (complete) end();
			return complete;
		} finally {
			setPool(pool);
		}
	}

	/** Called the first time {@link #act(float)} is called. This is a good place to query the {@link #actor actor's} starting
	 * state. */
	protected void begin () {
	}

	/** Called the last time {@link #act(float)} is called. */
	protected void end () {
	}

	/** Called each frame.
	 * @param percent The percentage of completion for this action, growing from 0 to 1 over the duration. If
	 *           {@link #setReverse(boolean) reversed}, this will shrink from 1 to 0. */
	abstract protected void update (float percent);

	/** Called each frame if the interpolation affects more than one value (such as a translation, scale, or color) and the
	 * starting speeds of the interpolation have been customized. Any excess percents are ignored. This only needs to be overridden
	 * by actions that affect more than one value.
	 * @param percent0 The percentage of completion for the first channel, such as X, width, or red.
	 * @param percent1 The percentage of completion for the second channel, such as Y, height, or green.
	 * @param percent2 The percentage of completion for the third channel, such as blue.
	 * @param percent3 The percentage of completion for the fourth channel, such as alpha. */
	protected void updateIndependently (float percent0, float percent1, float percent2, float percent3) {
	};

	/** Skips to the end of the transition. */
	public void finish () {
		time = duration;
	}

	public void restart () {
		time = 0;
		began = false;
		complete = false;
	}

	public void reset () {
		super.reset();
		reverse = false;
		interpolation = null;
		splineInterpolation = null;
		startSpeedsUsed = 0;
	}

	/** Gets the transition time so far. */
	public float getTime () {
		return time;
	}

	/** Sets the transition time so far. */
	public void setTime (float time) {
		this.time = time;
	}

	public float getDuration () {
		return duration;
	}

	/** Sets the length of the transition in seconds. */
	public void setDuration (float duration) {
		this.duration = duration;
	}

	public Interpolation getInterpolation () {
		return interpolation;
	}

	public void setInterpolation (Interpolation interpolation) {
		this.interpolation = interpolation;
	}

	public void setInterpolation (SplineInterpolation interpolation) {
		this.interpolation = interpolation;
		if (startSpeedsUsed > 0) splineInterpolation = (SplineInterpolation)interpolation;
	}

	/** Start speeds are used only if the interpolation is a {@linkplain SplineInterpolation}. They are expressed in terms of unit
	 * change over unit duration. Subclasses can expose a more intuitive method. To prepare the input for this method, you may use:
	 * <p>
	 * {@code speed = worldSpeed * duration / (totalChange)}
	 * @param numberUsed The number of start speeds to be used, which is the same as the number of values this Action impacts. For
	 *           example, an Action that affects position impacts two elements, X and Y. Excess inputs will be ignored. Maximum of
	 *           4. */
	protected void setStartSpeed (int numberUsed, float startSpeed0, float startSpeed1, float startSpeed2, float startSpeed3) {
		this.startSpeed0 = startSpeed0;
		this.startSpeed1 = startSpeed1;
		this.startSpeed2 = startSpeed2;
		this.startSpeed3 = startSpeed3;
		startSpeedsUsed = numberUsed;
		if (interpolation instanceof SplineInterpolation) splineInterpolation = (SplineInterpolation)interpolation;
	}

	protected void clearStartSpeeds () {
		startSpeedsUsed = 0;
		splineInterpolation = null;
	}

	protected float getSpeed0 () {
		if (splineInterpolation != null) return splineInterpolation.speed(startSpeed0, 0, time / duration);
		if (interpolation != null) return interpolation.speed(0, 0, time / duration);
		return 1f;
	}

	protected float getSpeed1 () {
		if (splineInterpolation != null) return splineInterpolation.speed(startSpeed1, 0, time / duration);
		if (interpolation != null) return interpolation.speed(0, 0, time / duration);
		return 1f;
	}

	protected float getSpeed2 () {
		if (splineInterpolation != null) return splineInterpolation.speed(startSpeed2, 0, time / duration);
		if (interpolation != null) return interpolation.speed(0, 0, time / duration);
		return 1f;
	}

	protected float getSpeed3 () {
		if (splineInterpolation != null) return splineInterpolation.speed(startSpeed3, 0, time / duration);
		if (interpolation != null) return interpolation.speed(0, 0, time / duration);
		return 1f;
	}

	public boolean isReverse () {
		return reverse;
	}

	/** When true, the action's progress will go from 100% to 0%. */
	public void setReverse (boolean reverse) {
		this.reverse = reverse;
	}

	public interface MoveAction {
		public float getWorldSpeedX ();

		public float getWorldSpeedY ();
	}

	public interface ScaleAction {
		public float getWorldSpeedX ();

		public float getWorldSpeedY ();
	}

	public interface SizeAction {
		public float getWorldSpeedWidth ();

		public float getWorldSpeedHeight ();
	}

	public interface RotateAction {
		public float getWorldSpeed ();
	}
}
