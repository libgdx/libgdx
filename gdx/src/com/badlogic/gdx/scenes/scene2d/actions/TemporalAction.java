
package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;

/** Base class for actions that transition over time. 
 * @author Nathan Sweet */
abstract public class TemporalAction extends Action {
	private float duration, time;
	private Interpolation interpolation;
	private boolean reverse, complete;

	public boolean act (float delta) {
		if (complete) return true;
		if (time == 0) initialize();
		time += delta;
		complete = time >= duration;
		float percent;
		if (complete)
			percent = 1;
		else {
			percent = time / duration;
			if (interpolation != null) percent = interpolation.apply(percent);
		}
		if (reverse) percent = 1 - percent;
		update(percent);
		return complete;
	}

	/** Called the first time {@link #act(float)} is called. This is a good place to query the {@link #actor actor's} starting
	 * state. */
	abstract protected void initialize ();

	/** Called each frame.
	 * @param percent The percentage of completion for this action, growing from 0 to 1 over the duration. If
	 *           {@link #setReverse(boolean) reversed}, this will shrink from 1 to 0. */
	abstract protected void update (float percent);

	/** Skips to the end of the transition. */
	public void finish () {
		time = duration;
	}

	public void restart () {
		time = 0;
		complete = false;
	}

	public void reset () {
		super.reset();
		reverse = false;
		interpolation = null;
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

	public boolean isReverse () {
		return reverse;
	}

	public void setReverse (boolean reverse) {
		this.reverse = reverse;
	}
}
