
package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Event;

import org.jetbrains.annotations.NotNull;

/** An EventAction that is complete once it receives X number of events.
 * @author JavadocMD
 * @author Nathan Sweet */
public class CountdownEventAction<T extends Event> extends EventAction<T> {
	int count, current;

	public CountdownEventAction (@NotNull Class<? extends T> eventClass, int count) {
		super(eventClass);
		this.count = count;
	}

	public boolean handle (@NotNull T event) {
		current++;
		return current >= count;
	}
}
