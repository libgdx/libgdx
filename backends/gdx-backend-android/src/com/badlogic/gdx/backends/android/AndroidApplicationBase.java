package com.badlogic.gdx.backends.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;

/**
 * Interface that abstracts the Android application class usages, so that libGDX can be used with a fragment (or with any other client code) 
 * 
 * @author Bartol Karuza (me@bartolkaruza.com)
 *
 */
public interface AndroidApplicationBase extends Application {
	
	/**
	 * The application or activity context
	 * 
	 * @return The {@link Context}
	 * 
	 */
	Context getContext();
	
	/**
	 * The provided application listener
	 * 
	 * @return the application listener
	 */
	ApplicationListener getApplicationListener();
	
	/**
	 * A set of usable runnables
	 * 
	 * @return the {@link Runnable} array
	 */
	Array<Runnable> getRunnables();
	
	/**
	 * The currently executed runnables
	 *
	 * @return the {@link Runnable} array 
	 */
	Array<Runnable> getExecutedRunnables();
	
	/**
	 * Method signifies an intent of the caller to execute some action on the UI Thread.
	 * 
	 * @param runnable The runnable to be executed
	 */
	void runOnUiThread(Runnable runnable);
	
	/**
	 * Method signifies an intent to start an activity, may be the default method of 
	 * the {@link Activity} class
	 *  
	 * @param intent The {@link Intent} for starting an activity
	 */
	void startActivity(Intent intent);

}
