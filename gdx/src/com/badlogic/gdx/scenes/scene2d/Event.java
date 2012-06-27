
package com.badlogic.gdx.scenes.scene2d;

import com.badlogic.gdx.utils.Pool.Poolable;

/** The base class for all events.
 * @see ActorEvent
 * @see Actor#fire(Event) */
public class Event implements Poolable {
	private Stage stage;
	private Actor targetActor; // deepest actor hit
	private Actor currentTarget; // current actor being notified about event
	private boolean capture;
	private boolean bubbles = true; // true means propagate to target's parents
	private boolean handled; // true means the event was handled (the stage will eat the input)
	private boolean stopped; // true means event propagation was stopped
	private boolean cancelled; // true means any action that this event would cause should not happen

	/** Marks this event has being handled. This does not affect event propagation inside scene2d, but causes the {@link Stage}
	 * event methods to return false, which will eat the event so it is not passed on to the application under the stage. */
	public void handle () {
		handled = true;
	}

	/** Marks this event has being cancelled. This {@link #handle() handles} the event and {@link #stop() stops} the event
	 * propagation. It also cancels any default action that would have been taken by the code that fired the event. Eg, if the
	 * event is for a checkbox being checked, cancelling the event could uncheck the checkbox. */
	public void cancel () {
		cancelled = true;
		stopped = true;
		handled = true;
	}

	/** Marks this event has being stopped. This halts event propagation. Any other listeners on the {@link #getCurrentTarget()
	 * current target} are notified, but after that no other listeners are notified. */
	public void stop () {
		stopped = true;
	}

	public void reset () {
		stage = null;
		targetActor = null;
		currentTarget = null;
		capture = false;
		bubbles = true;
		handled = false;
		stopped = false;
		cancelled = false;
	}

	/** Returns the actor that the event originated from. */
	public Actor getTargetActor () {
		return targetActor;
	}

	public void setTargetActor (Actor targetActor) {
		this.targetActor = targetActor;
	}

	/** Returns the actor that this listener is attached to. */
	public Actor getCurrentTarget () {
		return currentTarget;
	}

	public void setCurrentTarget (Actor currentTarget) {
		this.currentTarget = currentTarget;
	}

	public boolean getBubbles () {
		return bubbles;
	}

	/** If true, after the event is fired on the target actor, it will also be fired on each of the parent actors, all the way to
	 * the root. */
	public void setBubbles (boolean bubbles) {
		this.bubbles = bubbles;
	}

	/** {@link #handle()} */
	public boolean isHandled () {
		return handled;
	}

	/** @see #stop() */
	public boolean isStopped () {
		return stopped;
	}

	/** @see #cancel() */
	public boolean isCancelled () {
		return cancelled;
	}

	public void setCapture (boolean capture) {
		this.capture = capture;
	}

	/** If true, the event was fired during the capture phase.
	 * @see Actor#fire(Event) */
	public boolean isCapture () {
		return capture;
	}

	public void setStage (Stage stage) {
		this.stage = stage;
	}

	/** The stage for the actor the event was fired on. */
	public Stage getStage () {
		return stage;
	}
}
