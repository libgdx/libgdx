
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

abstract public class ChangeListener implements EventListener {
	public boolean handle (Event event) {
		if (!(event instanceof ChangeEvent)) return false;
		changed((ChangeEvent)event, event.getTargetActor());
		return false;
	}

	abstract public void changed (ChangeEvent event, Actor actor);

	static public class ChangeEvent extends Event {
	}
}
