
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ActorEvent;
import com.badlogic.gdx.scenes.scene2d.ActorListener;

/** Detects a click on an actor. The touch must go down over the actor and go up over the actor for the click to occur.
 * @author Nathan Sweet */
public class PressedListener extends ActorListener {
	private boolean pressed;

	public boolean isPressed () {
		return pressed;
	}

	public boolean touchDown (ActorEvent event, float x, float y, int pointer, int button) {
		if (pointer > 0) return false;
		pressed = true;
		return true;
	}

	public void exit (ActorEvent event, float x, float y, int pointer, Actor toActor) {
		if (pointer == 0) pressed = false;
	}

	public boolean touchUp (ActorEvent event, float x, float y, int pointer, int button) {
		if (pointer == 0 && pressed) {
			pressed = false;
			return true;
		}
		return false;
	}
}
