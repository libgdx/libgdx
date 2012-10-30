/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Modified by Elijah Cornell
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */


package com.badlogic.gdx.backends.android;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.service.wallpaper.WallpaperService.Engine;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxNativesLoader;

/**
 * An implementation of the {@link Application} interface for Android. Create an
 * {@link Activity} that derives from this class. In the
 * {@link Activity#onCreate(Bundle)} method call the
 * {@link #initialize(ApplicationListener, boolean)} method specifying the
 * configuration for the GLSurfaceView.
 * 
 * @author mzechner
 */
public class AndroidLiveWallpaper implements Application {
	static {
		GdxNativesLoader.load();
	}

	protected WallpaperService service;
	private Engine engine;
	protected AndroidGraphicsLiveWallpaper graphics;
	protected AndroidInput input;
	protected AndroidAudio audio;
	protected AndroidFiles files;
	protected AndroidNet net;
	protected ApplicationListener listener;
	protected boolean firstResume = true;
	protected final Array<Runnable> runnables = new Array<Runnable>();
	protected final Array<Runnable> executedRunnables = new Array<Runnable>();
	protected int logLevel = LOG_INFO;

	public AndroidLiveWallpaper(WallpaperService service, Engine engine) {
		this.service = service;
		this.engine = engine;
	}
	
	/** This method has to be called in the {@link Activity#onCreate(Bundle)} method. It sets up all the things necessary to get
	 * input, render via OpenGL and so on. If useGL20IfAvailable is set the AndroidApplication will try to create an OpenGL ES 2.0
	 * context which can then be used via {@link Graphics#getGL20()}. The {@link GL10} and {@link GL11} interfaces should not be
	 * used when OpenGL ES 2.0 is enabled. To query whether enabling OpenGL ES 2.0 was successful use the
	 * {@link Graphics#isGL20Available()} method. Uses a default {@link AndroidApplicationConfiguration}.
	 * 
	 * @param listener the {@link ApplicationListener} implementing the program logic
	 * @param useGL2IfAvailable whether to use OpenGL ES 2.0 if its available. */
	public void initialize (ApplicationListener listener, boolean useGL2IfAvailable) {
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useGL20 = useGL2IfAvailable;
		initialize(listener, config);
	}

	/**
	 * This method has to be called in the {@link Activity#onCreate(Bundle)}
	 * method. It sets up all the things necessary to get input, render via
	 * OpenGL and so on. If useGL20IfAvailable is set the AndroidApplication
	 * will try to create an OpenGL ES 2.0 context which can then be used via
	 * {@link Graphics#getGL20()}. The {@link GL10} and {@link GL11} interfaces
	 * should not be used when OpenGL ES 2.0 is enabled. To query whether
	 * enabling OpenGL ES 2.0 was successful use the
	 * {@link Graphics#isGL20Available()} method. sleepTime specifies the number
	 * of milliseconds to sleep in the touch event handler. This may be used on
	 * <= 1.6 Android devices. Note that it will not solve the CPU usage problem
	 * of the event handler of the Android system. Things will still slow down.
	 * 
	 * @param listener
	 *            the {@link ApplicationListener} implementing the program logic
	 */
	public void initialize(ApplicationListener listener, AndroidApplicationConfiguration config) {
		graphics = new AndroidGraphicsLiveWallpaper(this, config.useGL20, config.resolutionStrategy==null?new FillResolutionStrategy():config.resolutionStrategy);
		input = new AndroidInput(this, this.getService(), null, config);
		audio = new AndroidAudio(this.getService(), config);
		files = new AndroidFiles(this.getService().getAssets());
		this.listener = listener;
		
		Gdx.app = this;
		Gdx.input = this.getInput();
		Gdx.audio = this.getAudio();
		Gdx.files = this.getFiles();
		Gdx.graphics = this.getGraphics();
	}

	public void onPause() {
		graphics.pause();
		if (audio != null) audio.pause();
		input.unregisterSensorListeners();
	}

	public void onResume() {
		Gdx.app = this;
		Gdx.input = this.getInput();
		Gdx.audio = this.getAudio();
		Gdx.files = this.getFiles();
		Gdx.graphics = this.getGraphics();

		((AndroidInput)getInput()).registerSensorListeners();
		
		if (audio != null) audio.resume();		
		if (!firstResume)
			graphics.resume();
		else
			firstResume = false;
	}

	public WallpaperService getService() {
		return service;
	}

	public Engine getEngine() {
		return engine;
	}

	public ApplicationListener getListener() {
		return listener;
	}

	@Override 
	public void postRunnable (Runnable runnable) {
		synchronized(runnables) {
			runnables.add(runnable);
		}
	}
	
	@Override
	public Audio getAudio () {
		return audio;
	}

	@Override
	public Files getFiles () {
		return files;
	}

	@Override
	public Graphics getGraphics () {
		return graphics;
	}

	@Override
	public Input getInput () {
		return input;
	}
	
	@Override
	public Net getNet () {
		return net;
	}

	@Override
	public ApplicationType getType () {
		return ApplicationType.Android;
	}

	@Override
	public int getVersion () {
		return Integer.parseInt(android.os.Build.VERSION.SDK);
	}

	@Override
	public long getJavaHeap () {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	@Override
	public long getNativeHeap () {
		return Debug.getNativeHeapAllocatedSize();
	}

	@Override
	public Preferences getPreferences (String name) {
		return new AndroidPreferences(service.getSharedPreferences(name, Context.MODE_PRIVATE));
	}

	AndroidClipboard clipboard;
	
	@Override
	public Clipboard getClipboard() {
		if (clipboard == null) {
			clipboard = new AndroidClipboard(service);
		}
		return clipboard;
	}
	
	@Override
	public void debug (String tag, String message) {
		if (logLevel >= LOG_DEBUG) {
			Log.d(tag, message);
		}
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_DEBUG) {
			Log.d(tag, message, exception);
		}
	}

	@Override
	public void log (String tag, String message) {
		if (logLevel >= LOG_INFO) Log.i(tag, message);
	}

	@Override
	public void log (String tag, String message, Exception exception) {
		if (logLevel >= LOG_INFO) Log.i(tag, message, exception);
	}

	@Override
	public void error (String tag, String message) {
		if (logLevel >= LOG_ERROR) Log.e(tag, message);
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_ERROR) Log.e(tag, message, exception);
	}

	@Override
	public void setLogLevel (int logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public void exit () {
		// no-op
	}
}
