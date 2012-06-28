
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ActorEvent;
import com.badlogic.gdx.scenes.scene2d.ActorListener;

/** Detects the mouse or a finger touch on an actor. The touch must go down over the actor and is considered pressed as long as it
 * is over the actor or within the {@link #setTapSquareSize(float) tap square}.
 * @author Nathan Sweet */
public class PressedListener extends ActorListener {
	private float tapSquareSize = 14, touchDownX = -1, touchDownY = -1;
	private boolean pressed;
	private int button;

	public boolean touchDown (ActorEvent event, float x, float y, int pointer, int button) {
		if (pointer > 0 || button != this.button) return false;
		touchDownX = x;
		touchDownY = y;
		pressed = true;
		return true;
	}

	/** Returns true if the specified position is over the specified actor or one of its decsendants. */
	public boolean isOver (Actor actor, float x, float y) {
		Actor hit = actor.hit(x, y);
		if (hit == null || !hit.isDescendant(actor)) {
			if (touchDownX == -1 && touchDownY == -1) return false;
			return Math.abs(x - touchDownX) < tapSquareSize && Math.abs(y - touchDownY) < tapSquareSize;
		}
		return true;
	}

	public void touchDragged (ActorEvent event, float x, float y, int pointer) {
		pressed = Gdx.input.isButtonPressed(button) && isOver(event.getListenerActor(), x, y);
		if (!pressed) {
			// Once outside the tap square, don't use the tap square anymore.
			touchDownX = -1;
			touchDownY = -1;
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

	public int getButton () {
		return button;
	}

	public void setButton (int button) {
		this.button = button;
	}
}
