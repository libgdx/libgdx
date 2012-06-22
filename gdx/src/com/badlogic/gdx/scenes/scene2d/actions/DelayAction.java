
package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;

/** Delays execution of an action or inserts a pause in a {@link SequenceAction}. 
 * @author Nathan Sweet */
public class DelayAction extends DelegateAction {
	private float duration, time;

	public boolean act (float delta) {
		if (time < duration) {
			time += delta;
			if (time < duration) return false;
			delta = time - duration;
		}
		if (action == null) return true;
		return action.act(delta);
	}

	/** Causes the delay to be complete. */
	public void finish () {
		time = duration;
	}

	public void restart () {
		super.restart();
		time = 0;
	}

	/** Gets the time spent waiting for the delay. */
	public float getTime () {
		return time;
	}

	/** Sets the time spent waiting for the delay. */
	public void setTime (float time) {
		this.time = time;
	}

	public float getDuration () {
		return duration;
	}

	/** Sets the length of the delay in seconds. */
	public void setDuration (float duration) {
		this.duration = duration;
	}
}
