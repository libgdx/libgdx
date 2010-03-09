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
package com.badlogic.gdx.backends;

import com.badlogic.gdx.Application;

/**
 * An InputListener is used to receive input events from an {@link Application}. For this
 * it has to be registered with the Application via {@link Application.addInputListener()}. These
 * events might arrive asynchronously in a UI thread, the {@link Application} will buffer those events
 * and invoke the methods of the listener in the rendering thread to get rid of the need to synchronize
 * the UI and renderer thread for input. If an input listener signals that it processed an event by 
 * returning true the event will not be passed to other listeners in the listener chain (multiple
 * InputListeners can register with an Application). Note that
 * mouse and touch screen are equivalent here.
 * 
 * @author badlogicgames@gmail.com
 *
 */
public interface InputListener 
{
	/** left mouse button **/
	public final static int MOUSE_BUTTON1 = 1;
	/** right mouse button **/
	public final static int MOUSE_BUTTON2 = 2;
	/** middle mouse button **/
	public final static int MOUSE_BUTTON3 = 3;
	
	/**
	 * Called when a key was pressed
	 * @param keycode The key code
	 * @return wheter the input was processed
	 */
	public boolean keyDown( int keycode );
	
	/**
	 * Called when a key was released
	 * @param keycode The key code
	 */
	public boolean keyUp( int keycode );
	
	/**
	 * Called when a key was typed
	 * @param character The character
	 * @return wheter the input was processed
	 */
	public boolean keyTyped( char character );
	
	/**
	 * Called when a mouse button was pressed. Can be a 
	 * combination of {@link InputListener.MOUSE_BUTTON1} to 3.
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param buttons the pressed buttons
	 * @return wheter the input was processed
	 */
	public boolean mouseDown( int x, int y, int button, int pointer );
	
	/**
	 * Called when a mouse button was released. Can be a 
	 * combination of {@link InputListener.MOUSE_BUTTON1} to 3.
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param buttons the released buttons
	 * @return wheter the input was processed
	 */
	public boolean mouseUp( int x, int y, int button, int pointer );
	
	/**
	 * Called when the mouse moved. 
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param buttons the pressed buttons
	 * @return wheter the input was processed
	 */
	public boolean mouseMoved( int x, int y, int buttons, int pointer );
	
	/**
	 * Called when the mouse was dragged. 
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param buttons the pressed buttons
	 * @return wheter the input was processed
	 */
	public boolean mouseDragged( int x,  int y, int buttons, int pointer );
}
