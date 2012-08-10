/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

/** Detects the mouse or a finger touch on an actor. The touch must go down over the actor and is considered pressed as long as it
 * is over the actor or within the {@link #setTapSquareSize(float) tap square}. This behavior makes it easier to press buttons on a
 * touch interface when the initial touch happens near the edge of the actor. Any touch (not just the first) will trigger this
 * listener. While pressed, other touch downs are ignored.
 * @author Nathan Sweet */
public class PressedListener extends InputListener {
	private float tapSquareSize = 14, touchDownX = -1, touchDownY = -1;
	private int pressedPointer;
	private int button;
	private boolean pressed, over;

	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
		if (pressed) return false;
		if (pointer == 0 && button != this.button) return false;
		pressed = true;
		pressedPointer = pointer;
		touchDownX = x;
		touchDownY = y;
		return true;
	}

	public void touchDragged (InputEvent event, float x, float y, int pointer) {
		if (pointer != pressedPointer) return;
		pressed = isOver(event.getListenerActor(), x, y);
		if (pressed && pointer == 0 && !Gdx.input.isButtonPressed(button)) pressed = false;
		if (!pressed) {
			// Once outside the tap square, don't use the tap square anymore.
			touchDownX = -1;
			touchDownY = -1;
		}
	}

	public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
		if (pointer == pressedPointer) pressed = false;
	}

	public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
		if (pointer == -1) over = true;
	}

	public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
		if (pointer == -1) over = false;
	}

	/** Returns true if the specified position is over the specified actor or within the tap square. */
	public boolean isOver (Actor actor, float x, float y) {
		Actor hit = actor.hit(x, y);
		if (hit == null || !hit.isDescendant(actor)) {
			if (touchDownX == -1 && touchDownY == -1) return false;
			return Math.abs(x - touchDownX) < tapSquareSize && Math.abs(y - touchDownY) < tapSquareSize;
		}
		return true;
	}

	public boolean isPressed () {
		return pressed;
	}

	public boolean isOver () {
		return over || pressed;
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

	/** Sets the button to listen for, all other buttons are ignored. Default is {@link Buttons#LEFT}. */
	public void setButton (int button) {
		this.button = button;
	}
}
