
package com.badlogic.gdx.backends.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.Window;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.utils.Array;

/** Interface that abstracts the Android application class usages, so that libGDX can be used with a fragment (or with any other
 * client code)
 * 
 * @author Bartol Karuza (me@bartolkaruza.com) */
public interface AndroidApplicationBase extends Application {

	/** The application or activity context
	 * 
	 * @return The {@link Context} */
	Context getContext ();

	/** The provided application listener
	 * 
	 * @return the application listener */
	ApplicationListener getApplicationListener ();

	/** A set of usable runnables
	 * 
	 * @return the {@link Runnable} array */
	Array<Runnable> getRunnables ();

	/** The currently executed runnables
	 * 
	 * @return the {@link Runnable} array */
	Array<Runnable> getExecutedRunnables ();

	/** Method signifies an intent of the caller to execute some action on the UI Thread.
	 * 
	 * @param runnable The runnable to be executed */
	void runOnUiThread (Runnable runnable);

	/** Method signifies an intent to start an activity, may be the default method of the {@link Activity} class
	 * 
	 * @param intent The {@link Intent} for starting an activity */
	void startActivity (Intent intent);

	/** Returns the {@link Audio} object associated with this {@link AndroidApplicationBase}
	 * 
	 * @return the {@link Audio} object */
	Audio getAudio ();

	/** Returns the {@link LifecycleListener} array associated with this {@link AndroidApplicationBase}
	 * 
	 * @return the array of {@link LifecycleListener}'s */
	Array<LifecycleListener> getLifecycleListeners ();

	/** Returns if this application is a fragment
	 * @return true if the application is a fragment, otherwise false */
	boolean isFragment ();

	/** Returns the Window associated with the application
	 * @return The {@link Window} associated with the application */
	Window getApplicationWindow ();

	/** Activates Android 4.4 KitKat's 'Immersive Mode' feature.
	 * @param b Whether or not to use immersive mode */
	void useImmersiveMode (boolean b);

	/** Returns the Handler object created by the application
	 * @return The {@link Handler} object created by the application */
	Handler getHandler ();
}
