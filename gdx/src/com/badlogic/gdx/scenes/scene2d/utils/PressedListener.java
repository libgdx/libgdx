
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ActorEvent;
import com.badlogic.gdx.scenes.scene2d.ActorListener;

/** Detects a click on an actor. The touch must go down over the actor and go up over the actor for the click to occur.
 * @author Nathan Sweet */
public class PressedListener extends ActorListener {
	private float tapSquareSize = 14, touchDownX, touchDownY;
	private boolean pressed;

	public boolean touchDown (ActorEvent event, float x, float y, int pointer, int button) {
		if (pointer > 0) return false;
		touchDownX = x;
		touchDownY = y;
		pressed = true;
		return true;
	}

	public boolean isOver (Actor actor, float x, float y) {
		Actor hit = actor.hit(x, y);
		if (hit == null || !hit.isDescendant(actor)) {
			if (touchDownX == 0 && touchDownY == 0) return false;
			return Math.abs(x - touchDownX) < tapSquareSize && Math.abs(y - touchDownY) < tapSquareSize;
		}
		return true;
	}

	public void touchDragged (ActorEvent event, float x, float y, int pointer) {
		pressed = isOver(event.getContextActor(), x, y);
		if (!pressed) {
			// Once outside the tap square, don't use the tap square anymore.
			touchDownX = 0;
			touchDownY = 0;
		}
	}

	public void touchUp (ActorEvent event, float x, float y, int pointer, int button) {
		if (pointer == 0 && pressed) pressed = false;
	}

	public boolean isPressed () {
		return pressed;
	}

	public void setTapSquareSize (float halfTapSquareSize) {
		tapSquareSize = halfTapSquareSize;
	}

	public float getTapSquareSize () {
		return tapSquareSize;
	}

	public float getTouchDownX () {
		return touchDownX;
	}

	public float getTouchDownY () {
		return touchDownY;
	}
}
