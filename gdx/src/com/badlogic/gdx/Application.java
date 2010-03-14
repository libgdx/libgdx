package com.badlogic.gdx;

/**
 * <p>
 * An application is the main class of your project. It manages the different
 * aspects of your application, namely {@link Graphics}, {@link Audio}, {@link Input}
 * and {@link Files}.
 * </p> 
 * 
 * <p>
 * An application can either be a desktop application ({@link DesktopApplication}} or
 * an Android application ({@link AndroidApplication}). Refer to the documentation
 * of those classes to see how to setup each of of them.
 * </p> 
 * 
 * <p>
 * {@link Graphics} offers you various methods to output visuals to the screen. This is
 * achieved via OpenGL ES 2.0. On the desktop the features of OpenGL ES 2.0 are
 * emulated via Jogl. On Android the functionality of the Java OpenGL ES bindings
 * is used. The Graphics object allows you to register a {link RenderListener} which will
 * be called each time a new frame has to be drawn. This RenderListener usually 
 * houses you application logic.
 * </p>
 * 
 * <p>
 * {@link Audio} offers you various methods to output sound and music. This is achieved
 * via Java Sound on the desktop. On Android the Android media framework is used.
 * </p>
 * 
 * <p>
 * {@link Input} offers you various methods to poll user input form the keyboard, touch screen,
 * mouse and accelerometer. Additionally you can register an {@link InputListener}
 * which allows for event based input processing. The InputListener will be called
 * in the rendering thread which will also call your RenderListener. This way you
 * don't have to care about thread synchronization when you process input events.
 * </p>
 * 
 * <p>
 * {@link Files} offers you various methods to access internal and external files.
 * An internal file is a file that is stored near your application. On Android internal
 * file are equivalent to assets. On the desktop the root directory of your application
 * is where internal files will be looked up. Internal files are read only. External
 * files are resources you create in your application and write to an external storage.
 * On Android external files reside on the SD-card, on the desktop external files
 * are written to a users home directory.
 * </p> 
 * 
 * <p>
 * Additionally an Application allows to set a {@link ApplicationListener} which will be invoked
 * when the Application is paused, resumed or closing. This can be used to save any state that needs saving.
 * Note that the ApplicationListener will not be called in the rendering thread. 
 * 
 * <p>
 * Generally you will have two projects for your application. The first one will be the 
 * desktop project which houses all your application code. The second one will be the
 * Android project which only contains an Activity derived from {link AndroidApplication}.
 * In this activity you register a RenderListener instance which is the same you use
 * in your desktop project. In this way both the desktop and Android version of your
 * application share the same code. They only differ in setting up the Application instance.
 * </p>
 * 
 * @author mzechner
 *
 */
public interface Application 
{
	/**
	 * @return the {@link Graphics} instance
	 */
	public Graphics getGraphics( );
	
	/**
	 * @return the {@link Audio} instance
	 */
	public Audio getAudio( );
	
	/**
	 * @return the {@link Input} instance
	 */
	public Input getInput( );
	
	/**
	 * @return the {@link Files} instance
	 */
	public Files getFiles( );
	
	/**
	 * Logs a message to the console or logcat
	 */
	public void log( String tag, String message );
	
	/**
	 * Sets the {@link ApplicationListener} that is called
	 * when the {@link Application} is paused, resumed or closing.
	 * 
	 * @param listener the ApplicationListener 
	 */
	public void setApplicationListener( ApplicationListener listener );
}
