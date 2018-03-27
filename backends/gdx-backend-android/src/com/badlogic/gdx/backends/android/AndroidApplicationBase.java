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

package com.badlogic.gdx.backends.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;

/** Interface that abstracts the Android application class usages, so that libGDX can be used with a fragment (or with any other
 * client code)
 * 
 * @author Bartol Karuza (me@bartolkaruza.com)
 * @author davebaol */
public interface AndroidApplicationBase extends Application {

	static final int MINIMUM_SDK = 9;

	/** The application or activity context
	 * 
	 * @return the {@link Context} */
	Context getContext ();

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

	/** Returns the {@link AndroidInput} object associated with this {@link AndroidApplicationBase}
	 * 
	 * @return the {@link AndroidInput} object */
	@Override
	AndroidInput getInput ();

	/** Returns the {@link LifecycleListener} array associated with this {@link AndroidApplicationBase}
	 * 
	 * @return the array of {@link LifecycleListener}'s */
	SnapshotArray<LifecycleListener> getLifecycleListeners ();

	/** Returns the Window associated with the application
	 * @return The {@link Window} associated with the application */
	Window getApplicationWindow ();

	/** Returns the WindowManager associated with the application
	 * @return The {@link WindowManager} associated with the application */
	WindowManager getWindowManager ();

	/** Activates Android 4.4 KitKat's 'Immersive Mode' feature.
	 * @param b Whether or not to use immersive mode */
	void useImmersiveMode (boolean b);

	/** Returns the Handler object created by the application
	 * @return The {@link Handler} object created by the application */
	Handler getHandler ();
}
