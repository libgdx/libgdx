
package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.utils.reflect.ClassReflection;

/** Adds a listener to the actor for a specific event type and does not complete until {@link #handle(Event)} returns true.
 * @author JavadocMD
 * @author Nathan Sweet */
abstract public class EventAction<T extends Event> extends Action {
	final Class<? extends T> eventClass;
	boolean result, active;

	private final EventListener listener = new EventListener() {
		public boolean handle (Event event) {
			if (!active || !ClassReflection.isInstance(eventClass, event)) return false;
			result = EventAction.this.handle((T)event);
			return result;
		}
	};

	public EventAction (Class<? extends T> eventClass) {
		this.eventClass = eventClass;
	}

	public void restart () {
		result = false;
		active = false;
	}

	public void setTarget (Actor newTarget) {
		if (target != null) target.removeListener(listener);
		super.setTarget(newTarget);
		if (newTarget != null) newTarget.addListener(listener);
	}

	/** Called when the specific type of event occurs on the actor.
	 * @return true if the event should be considered {@link Event#handle() handled} and this EventAction considered complete. */
	abstract public boolean handle (T event);

	public boolean act (float delta) {
		active = true;
		return result;
	}

	public boolean isActive () {
		return active;
	}

	public void setActive (boolean active) {
		this.active = active;
	}
}
