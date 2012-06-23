
package com.badlogic.gdx.scenes.scene2d.listeners;

import com.badlogic.gdx.scenes.scene2d.ActorEvent;

/** Detects a click on an actor. The touch must go down over the actor and go up over the actor for the click to occur.
 * @author Nathan Sweet */
abstract public class ClickListener extends PressedListener {
	public boolean touchUp (ActorEvent event, float x, float y, int pointer, int button) {
		if (pointer == 0 && isPressed()) clicked(event, x, y);
		return super.touchUp(event, x, y, pointer, button);
	}

	abstract public void clicked (ActorEvent event, float x, float y);
}
