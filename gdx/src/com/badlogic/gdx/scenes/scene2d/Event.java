
package com.badlogic.gdx.scenes.scene2d;

import com.badlogic.gdx.utils.Pool.Poolable;

public class Event implements Poolable {
	Stage stage;
	Actor target; // deepest actor hit
	private Actor currentTarget; // current actor being notified about event
	boolean capture;
	private boolean bubbles = true; // true means propagate to target's parents
	private boolean handled; // true means the event was handled (the stage will eat the input)
	private boolean stopped; // true means event propagation was stopped
	private boolean cancelled; // true means any action that this event would cause should not happen

	public void handled () {
		handled = true;
	}

	public void cancel () {
		cancelled = true;
		stopped = true;
		handled = true;
	}

	public void stop () {
		stopped = true;
	}

	public void reset () {
		target = null;
		currentTarget = null;
		handled = false;
		stopped = false;
		cancelled = false;
		bubbles = true;
	}

	public void setTarget (Actor target) {
		this.target = target;
	}

	public Actor getTarget () {
		return target;
	}

	public Actor getCurrentTarget () {
		return currentTarget;
	}

	public void setCurrentTarget (Actor currentTarget) {
		this.currentTarget = currentTarget;
	}

	public boolean getBubbles () {
		return bubbles;
	}

	public void setBubbles (boolean bubbles) {
		this.bubbles = bubbles;
	}

	public boolean isHandled () {
		return handled;
	}

	public boolean isStopped () {
		return stopped;
	}

	public boolean isCancelled () {
		return cancelled;
	}

	public boolean isCapture () {
		return capture;
	}

	public Stage getStage () {
		return stage;
	}
}
