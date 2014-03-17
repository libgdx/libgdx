
package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Event;

/** An EventAction that is complete once it receives X number of events.
 * @author JavadocMD
 * @author Nathan Sweet */
public class CountdownEventAction<T extends Event> extends EventAction<T> {
	int count, current;

	public CountdownEventAction (Class<? extends T> eventClass, int count) {
		super(eventClass);
		this.count = count;
	}

	public boolean handle (T event) {
		current++;
		return current >= count;
	}
}
