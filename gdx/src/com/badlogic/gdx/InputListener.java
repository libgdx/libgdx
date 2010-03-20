/**
 *  This file is part of Libgdx by Mario Zechner (badlogicgames@gmail.com)
 *
 *  Libgdx is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Libgdx is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx;


/**
 * An InputListener is used to receive input events from the keyboard and the touch screen (mouse on the desktop). For this
 * it has to be registered with the {@link Input} instance of an {@link Application}. The
 * events might arrive asynchronously in a UI thread, the {@link Input} instance will buffer those events
 * and invoke the methods of the listener in the rendering thread to get rid of the need to synchronize
 * the UI and renderer thread for input. If an InputListener signals that it processed an event by 
 * returning true the event will not be passed to other listeners in the listener chain (multiple
 * InputListeners can register with an Input instance). Note that
 * mouse and touch screen are equivalent here.
 * 
 * @author badlogicgames@gmail.com
 *
 */
public interface InputListener 
{	
	/**
	 * Called when a key was pressed
	 * @param keycode one of the constants in {@link Input.Keys}
	 * @return whether the input was processed
	 */
	public boolean keyDown( int keycode );
	
	/**
	 * Called when a key was released
	 * @param keycode  one of the constants in {@link Input.Keys}
	 */
	public boolean keyUp( int keycode );
	
	/**
	 * Called when a key was typed
	 * @param character The character
	 * @return whether the input was processed
	 */
	public boolean keyTyped( char character );
	
	/**
	 * Called when the screen was touched or the mouse was pressed. 
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param pointer the pointer for the event. Unsupported yet, used for multitouch later on.
	 * @return whether the input was processed
	 */
	public boolean touchDown( int x, int y, int pointer );
	
	/**
	 * Called when a finger was lifted or the mouse button was released. 
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param pointer the pointer for the event. Unsupported yet, used for multitouch later on.
	 * @return whether the input was processed
	 */
	public boolean touchUp( int x, int y, int pointer );	
	
	/**
	 * Called when a finger or the mouse was dragged. 
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param pointer the pointer for the event. Unsupported yet, used for multitouch later on.
	 * @return whether the input was processed
	 */
	public boolean touchDragged( int x,  int y, int pointer );
}
