package com.badlogic.gdx;

/**
 * An input listener is used to receive input events from a {@link Application}. These
 * events might arrive asynchronously so take care of synchronization between threads. If an
 * input listener signals that it processed an event by returning true the event will not be
 * passed to other listeners in a listener chain.
 * 
 * @author mzechner
 *
 */
public interface InputListener 
{
	public final static int MOUSE_BUTTON1 = 1;
	public final static int MOUSE_BUTTON2 = 2;
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
