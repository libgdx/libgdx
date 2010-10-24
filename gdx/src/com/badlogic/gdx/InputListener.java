/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx;

/**
 * An InputListener is used to receive input events from the keyboard and the touch screen (mouse on the desktop). For this it has
 * to be registered with the {@link Input} instance of an {@link Application}. The events might arrive asynchronously in a UI
 * thread, the {@link Input} instance will buffer those events and invoke the methods of the listener in the rendering thread to
 * get rid of the need to synchronize the UI and renderer thread for input. If an InputListener signals that it processed an event
 * by returning true the the event will not be passed to other listeners in the listener chain (multiple InputListeners can
 * register with an Input instance).
 * 
 * @author badlogicgames@gmail.com
 * 
 */
public interface InputListener {
	/**
	 * Called when a key was pressed
	 * @param keycode one of the constants in {@link Input.Keys}
	 * @return whether the input was processed
	 */
	public boolean keyDown (int keycode);

	/**
	 * Called when a key was released
	 * @param keycode one of the constants in {@link Input.Keys}
	 */
	public boolean keyUp (int keycode);

	/**
	 * Called when a key was typed
	 * @param character The character
	 * @return whether the input was processed
	 */
	public boolean keyTyped (char character);

	/**
	 * Called when the screen was touched or the mouse was pressed.
	 * 
	 * @param x The x coordinate, origin is in the upper left corner
	 * @param y The y coordinate, origin is in the upper left corner
	 * @param pointer the pointer for the event. Unsupported yet, used for multitouch later on.
	 * @return whether the input was processed
	 */
	public boolean touchDown (int x, int y, int pointer);

	/**
	 * Called when a finger was lifted or a mouse button was released.
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param pointer the pointer for the event. Unsupported yet, used for multitouch later on.
	 * @return whether the input was processed
	 */
	public boolean touchUp (int x, int y, int pointer);

	/**
	 * Called when a finger or the mouse was dragged.
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param pointer the pointer for the event. Unsupported yet, used for multitouch later on.
	 * @return whether the input was processed
	 */
	public boolean touchDragged (int x, int y, int pointer);
}
