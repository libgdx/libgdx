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

package com.badlogic.gdx;

import com.badlogic.gdx.Input.Buttons;

/** An InputProcessor is used to receive input events from the keyboard and the touch screen (mouse on the desktop). For this it
 * has to be registered with the {@link Input#setInputProcessor(InputProcessor)} method. It will be called each frame before the
 * call to {@link ApplicationListener#render()}. The methods return a* boolean in case you want to write a multiplexing
 * InputProcessor that has a chain of child processors that signal whether they processed the event. The {@link InputMultiplexer}
 * offers you exactly this functionality.
 * 
 * @author badlogicgames@gmail.com */
public interface InputProcessor {
	/** Called when a key was pressed
	 * 
	 * @param keycode one of the constants in {@link Input.Keys}
	 * @return whether the input was processed */
	public boolean keyDown (int keycode);

	/** Called when a key was released
	 * 
	 * @param keycode one of the constants in {@link Input.Keys}
	 * @return whether the input was processed */
	public boolean keyUp (int keycode);

	/** Called when a key was typed
	 * 
	 * @param character The character
	 * @return whether the input was processed */
	public boolean keyTyped (char character);

	/** Called when the screen was touched or a mouse button was pressed. The button parameter will be {@link Buttons#LEFT} on
	 * Android.
	 * 
	 * @param x The x coordinate, origin is in the upper left corner
	 * @param y The y coordinate, origin is in the upper left corner
	 * @param pointer the pointer for the event.
	 * @param button the button
	 * @return whether the input was processed */
	public boolean touchDown (int x, int y, int pointer, int button);

	/** Called when a finger was lifted or a mouse button was released. The button parameter will be {@link Buttons#LEFT} on
	 * Android.
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param pointer the pointer for the event.
	 * @param button the button
	 * @return whether the input was processed */
	public boolean touchUp (int x, int y, int pointer, int button);

	/** Called when a finger or the mouse was dragged.
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param pointer the pointer for the event.
	 * @return whether the input was processed */
	public boolean touchDragged (int x, int y, int pointer);

	/** Called when the mouse was moved without any buttons being pressed. Will not be called on Android.
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @return whether the input was processed */
	public boolean touchMoved (int x, int y);

	/** Called when the mouse wheel was scrolled. Will not be called on Android.
	 * @param amount the scroll amount, -1 or 1 depending on the direction the wheel was scrolled.
	 * @return whether the input was processed. */
	public boolean scrolled (int amount);
}
