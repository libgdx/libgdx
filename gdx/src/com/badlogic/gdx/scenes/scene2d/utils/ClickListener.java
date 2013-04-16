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
import com.badlogic.gdx.utils.TimeUtils;

/** Detects mouse over, mouse or finger touch presses, and clicks on an actor. A touch must go down over the actor and is
 * considered pressed as long as it is over the actor or within the {@link #setTapSquareSize(float) tap square}. This behavior
 * makes it easier to press buttons on a touch interface when the initial touch happens near the edge of the actor. Double clicks
 * can be detected using {@link #getTapCount()}. Any touch (not just the first) will trigger this listener. While pressed, other
 * touch downs are ignored.
 * @author Nathan Sweet */
public class ClickListener extends InputListener {
	private float tapSquareSize = 14, touchDownX = -1, touchDownY = -1;
	private int pressedPointer = -1;
	private int button;
	private boolean pressed, over, cancelled;
	private long tapCountInterval = (long)(0.4f * 1000000000l);
	private int tapCount;
	private long lastTapTime;

	public ClickListener () {
	}

	public ClickListener (int button) {
		this.button = button;
	}

	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
		if (pressed) return false;
		if (pointer == 0 && this.button != -1 && button != this.button) return false;
		pressed = true;
		pressedPointer = pointer;
		touchDownX = x;
		touchDownY = y;
		return true;
	}

	public void touchDragged (InputEvent event, float x, float y, int pointer) {
		if (pointer != pressedPointer || cancelled) return;
		pressed = isOver(event.getListenerActor(), x, y);
		if (pressed && pointer == 0 && button != -1 && !Gdx.input.isButtonPressed(button)) pressed = false;
		if (!pressed) {
			// Once outside the tap square, don't use the tap square anymore.
			invalidateTapSquare();
		}
	}

	public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
		if (pointer == pressedPointer) {
			if (!cancelled) {
				over = isOver(event.getListenerActor(), x, y);
				if (over && pointer == 0 && this.button != -1 && button != this.button) over = false;
				if (over) {
					long time = TimeUtils.nanoTime();
					if (time - lastTapTime > tapCountInterval) tapCount = 0;
					tapCount++;
					lastTapTime = time;
					clicked(event, x, y);
				}
			}
			pressed = false;
			pressedPointer = -1;
			cancelled = false;
		}
	}

	public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
		if (pointer == -1 && !cancelled) over = true;
	}

	public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
		if (pointer == -1 && !cancelled) over = false;
	}

	/** If a touch down is being monitored, the drag and touch up events are ignored until the next touch up. */
	public void cancel () {
		if (pressedPointer == -1) return;
		cancelled = true;
		over = false;
		pressed = false;
	}

	public void clicked (InputEvent event, float x, float y) {
	}

	public void dragStart (InputEvent event, float x, float y, int pointer) {
	}

	public void drag (InputEvent event, float x, float y, int pointer) {
	}

	public void dragStop (InputEvent event, float x, float y, int pointer) {
	}

	/** Returns true if the specified position is over the specified actor or within the tap square. */
	public boolean isOver (Actor actor, float x, float y) {
		Actor hit = actor.hit(x, y, true);
		if (hit == null || !hit.isDescendantOf(actor)) return inTapSquare(x, y);
		return true;
	}

	public boolean inTapSquare (float x, float y) {
		if (touchDownX == -1 && touchDownY == -1) return false;
		return Math.abs(x - touchDownX) < tapSquareSize && Math.abs(y - touchDownY) < tapSquareSize;
	}

	/** The tap square will not longer be used for the current touch. */
	public void invalidateTapSquare () {
		touchDownX = -1;
		touchDownY = -1;
	}

	/** Returns true if a touch is over the actor or within the tap square. */
	public boolean isPressed () {
		return pressed;
	}

	/** Returns true if the mouse or touch is over the actor or pressed and within the tap square. */
	public boolean isOver () {
		return over || pressed;
	}

	public void setTapSquareSize (float halfTapSquareSize) {
		tapSquareSize = halfTapSquareSize;
	}

	public float getTapSquareSize () {
		return tapSquareSize;
	}

	/** @param tapCountInterval time in seconds that must pass for two touch down/up sequences to be detected as consecutive taps. */
	public void setTapCountInterval (float tapCountInterval) {
		this.tapCountInterval = (long)(tapCountInterval * 1000000000l);
	}

	/** Returns the number of taps within the tap count interval for the most recent click event. */
	public int getTapCount () {
		return tapCount;
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

	/** Sets the button to listen for, all other buttons are ignored. Default is {@link Buttons#LEFT}. Use -1 for any button. */
	public void setButton (int button) {
		this.button = button;
	}
}
