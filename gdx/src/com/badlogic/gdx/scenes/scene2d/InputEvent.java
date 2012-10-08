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

package com.badlogic.gdx.scenes.scene2d;

import com.badlogic.gdx.Input.Buttons;

/** Event for actor input: touch, mouse, keyboard, and scroll.
 * @see InputListener */
public class InputEvent extends Event {
	private Type type;
	private float stageX, stageY;
	private int pointer, button, keyCode, scrollAmount;
	private char character;
	private Actor relatedActor;

	public void reset () {
		super.reset();
		relatedActor = null;
		button = -1;
	}

	/** The stage x coordinate where the event occured. Valid for: touchDown, touchDragged, touchUp, mouseMoved, enter, and exit. */
	public float getStageX () {
		return stageX;
	}

	public void setStageX (float stageX) {
		this.stageX = stageX;
	}

	/** The stage x coordinate where the event occured. Valid for: touchDown, touchDragged, touchUp, mouseMoved, enter, and exit. */
	public float getStageY () {
		return stageY;
	}

	public void setStageY (float stageY) {
		this.stageY = stageY;
	}

	/** The type of input event. */
	public Type getType () {
		return type;
	}

	public void setType (Type type) {
		this.type = type;
	}

	/** The pointer index for the event. The first touch is index 0, second touch is index 1, etc. Always -1 on desktop. Valid for:
	 * touchDown, touchDragged, touchUp, enter, and exit. */
	public int getPointer () {
		return pointer;
	}

	public void setPointer (int pointer) {
		this.pointer = pointer;
	}

	/** The index for the mouse button pressed. Always 0 on Android. Valid for: touchDown and touchUp.
	 * @see Buttons */
	public int getButton () {
		return button;
	}

	public void setButton (int button) {
		this.button = button;
	}

	/** The key code of the key that was pressed. Valid for: keyDown and keyUp. */
	public int getKeyCode () {
		return keyCode;
	}

	public void setKeyCode (int keyCode) {
		this.keyCode = keyCode;
	}

	/** The character for the key that was type. Valid for: keyTyped. */
	public char getCharacter () {
		return character;
	}

	public void setCharacter (char character) {
		this.character = character;
	}

	/** The amount the mouse was scrolled. Valid for: scrolled. */
	public int getScrollAmount () {
		return scrollAmount;
	}

	public void setScrollAmount (int scrollAmount) {
		this.scrollAmount = scrollAmount;
	}

	/** The actor related to the event. Valid for: enter and exit. For enter, this is the actor being exited, or null. For exit,
	 * this is the actor being entered, or null. */
	public Actor getRelatedActor () {
		return relatedActor;
	}

	public void setRelatedActor (Actor relatedActor) {
		this.relatedActor = relatedActor;
	}

	public String toString () {
		return type.toString();
	}

	static public enum Type {
		touchDown, touchUp, touchDragged, mouseMoved, enter, exit, scrolled, keyDown, keyUp, keyTyped
	}
}
