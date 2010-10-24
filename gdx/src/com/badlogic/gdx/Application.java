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
 * <p>
 * An <code>Application<code> is the main class of your project. It manages the different
 * aspects of your application, namely {@link Graphics}, {@link Audio}, {@link Input}
 * and {@link Files}.
 * </p>
 * 
 * <p>
 * An application can either be a desktop application ({@link JoglApplication}), an applet ({@link AppletApplication} or and
 * Android application ({@link AndroidApplication}). Each application class has it's own startup and initialization methods.
 * Please refer to their documentation for more information.
 * </p>
 * 
 * <p>
 * {@link Graphics} offers you various methods to output visuals to the screen. This is achieved via OpenGL ES 1.0, 1.1 or 2.0
 * depending on what's available an the platform. On the desktop the features of OpenGL ES 2.0 are emulated via Jogl. On Android
 * the functionality of the Java OpenGL ES bindings is used. The Graphics object allows you to register a {@link RenderListener}
 * which will be called each time a new frame has to be drawn. This <code>RenderListener<code> usually 
 * houses you application logic.
 * </p>
 * 
 * <p>
 * {@link Audio} offers you various methods to output and record sound and music. This is achieved via the Java Sound API on the
 * desktop. On Android the Android media framework is used.
 * </p>
 * 
 * <p>
 * {@link Input} offers you various methods to poll user input from the keyboard, touch screen, mouse and accelerometer.
 * Additionally you can register an {@link InputListener} which allows for event based input processing. The
 * <code>InputListener</code> will be called in the rendering thread which will also call your <code>RenderListener</code>. This
 * way you don't have to care about thread synchronization when you process input events.
 * </p>
 * 
 * <p>
 * {@link Files} offers you various methods to access internal and external files. An internal file is a file that is stored near
 * your application. On Android internal file are equivalent to assets. On the desktop the root directory of your application is
 * where internal files will be looked up. Internal files are read only. External files are resources you create in your
 * application and write to an external storage. On Android external files reside on the SD-card, on the desktop external files
 * are written to a users home directory. If you know what you do you can also specify absolute file names. This is not portable,
 * so take great care when using this feature.
 * </p>
 * 
 * <p>
 * Additionally an <code>Application</code> allows to set a {@link ApplicationListener} which will be invoked when the
 * <code>Application</code> is paused, resumed or closing. This can be used to save any state that needs saving. Note that the
 * <code>ApplicationListener<code> will not be called in the rendering thread. 
 * 
 * <p>
 * Generally you will have two projects for your application. The first one will be the 
 * desktop project which houses all your application code as well as a {@link JoglApplication}. 
 * The second project will be the Android project which only contains an Activity derived from {@link AndroidApplication}.
 * In both applications you register the same RenderListener instance which will get your application
 * going on both systems. In this way both the desktop and Android version of your
 * application share the same code. They only differ in setting up the Application instance.
 * </p>
 * 
 * <p>
 * The <code>Application</code> also has a set of methods that you can use to query specific information such as the operating
 * system the application is currently running on and so forth. This allows you to have operating system dependent code paths. It
 * is however not recommended to use this facilities.
 * </p>
 * 
 * <p>
 * The <code>Application</code> also has a simple logging method which will print to standard out on the desktop and to logcat on
 * Android.
 * </p>
 * 
 * @author mzechner
 * 
 */
public interface Application {
	/**
	 * Enumeration of possible {@link Application} types
	 * @author mzechner
	 * 
	 */
	public enum ApplicationType {
		Android, Desktop, Applet
	}

	/**
	 * @return the {@link Graphics} instance
	 */
	public Graphics getGraphics ();

	/**
	 * @return the {@link Audio} instance
	 */
	public Audio getAudio ();

	/**
	 * @return the {@link Input} instance
	 */
	public Input getInput ();

	/**
	 * @return the {@link Files} instance
	 */
	public Files getFiles ();

	/**
	 * Logs a message to the console or logcat
	 */
	public void log (String tag, String message);

	/**
	 * @return what {@link ApplicationType} this application has, e.g. Android or Desktop
	 */
	public ApplicationType getType ();

	/**
	 * @return the Android API level on Android or 0 on the desktop.
	 */
	public int getVersion ();

	/**
	 * Sets the {@link ApplicationListener} that is called when the {@link Application} is paused, resumed or closing.
	 * 
	 * @param listener the ApplicationListener
	 */
	public void setApplicationListener (ApplicationListener listener);
}
