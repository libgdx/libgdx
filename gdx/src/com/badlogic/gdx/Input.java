package com.badlogic.gdx;

/**
 * Interface to the input facilities. This allows to poll the state
 * of the keyboard, touch screen and accelerometer. On the desktop
 * the touch screen is replaced by mouse input, the accelerometer is
 * of course not available. Additionally one can register an {@link 
 * InputListener} with this module. The InputListener will then be
 * called each time a key is pressed or released or a touch event occured.
 * The InputListener will be called in the rendering thread of the 
 * graphics module to which a RenderListener is probably attached. This 
 * means that one does not have to take precautions to guarantee thread
 * safety. One can safely call graphics methods from within the InputListener
 * callbacks. One or more InputListeners can be registered with the module.
 * The events will then get passed to the InputListeners in the order they
 * have been registered with the module. If an InputListeners signals that
 * it consumed the event the InputListeners down the chain will not be invoked.
 * 
 * @author mzechner
 *
 */
public interface Input 
{
	/**
	 * Callback interface for {@link Application.getTextInput()}
	 * 
	 * @author badlogicgames@gmail.com
	 *
	 */
	public interface TextInputListener
	{
		public void input( String text );
	}
	
	/**
	 * Keys. 
	 * 
	 * @author badlogicgames@gmail.com
	 *
	 */
	public enum Keys
	{
		Left,
		Right,
		Up,
		Down,
		Shift,
		Control,
		Space,
		Any
	}
	
	/**
	 * Adds an {@link InputListener}. The order InputListeners
	 * are added is the same as the order in which they are called in case of an event. If an input
	 * listener signals that it processed the event the event is not passed to
	 * the other listeners in the chain.
	 * 
	 * @param listener the listener
	 */
	public void addInputListener( InputListener listener );
	
	/**
	 * Removes the {@link InputListener}.
	 * @param listener the listener
	 */
	public void removeInputListener( InputListener listener );
	
	/** 
	 * @return whether an accelerometer is available
	 */
	public boolean isAccelerometerAvailable( );	
	
	/** 
	 * @return The value of the accelerometer on its x-axis. ranges between [-10,10].
	 */
	public float getAccelerometerX( );
	
	/** 
	 * @return The value of the accelerometer on its y-axis. ranges between [-10,10].
	 */
	public float getAccelerometerY( );
	
	/** 
	 * @return The value of the accelerometer on its y-axis. ranges between [-10,10].
	 */
	public float getAccelerometerZ( );
	
	/**
	 * @return the last touch x coordinate in screen coordinates. The screen origin is the top left corner.
	 */
	public int getX( );
	
	/**
	 * @return the last touch y coordinate in screen coordinates. The screen origin is the top left corner.
	 */
	public int getY( );
	
	/**
	 * @return whether the screen is currently touched.
	 */
	public boolean isTouched( );
	
	/**
	 * Returns whether the key is pressed.
	 * 
	 * @param key The key.
	 * @return True or false.
	 */
	public boolean isKeyPressed( Keys key );
	
	/**
	 * System dependent method to input a string of text. A dialog
	 * box will be created with the given title and the given text
	 * as a message for the user. Once the dialog has been closed
	 * the provided {@link TextInputListener} will be called in the
	 * rendering thread.
	 * 
	 * @param listener The TextInputListener.
	 * @param title The title of the text input dialog.
	 * @param text The message presented to the user.
	 */
	public void getTextInput( TextInputListener listener, String title, String text );
}
