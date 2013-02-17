/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Modified by Elijah Cornell
 * 2013.01 Modified by Jaroslaw Wisniewski <j.wisniewski@appsisle.com>
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
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.service.wallpaper.WallpaperService.Engine;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.backends.android.surfaceview.GLBaseSurfaceViewLW;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20LW;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewCupcake;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxNativesLoader;

/**
 * An implementation of the {@link Application} interface to be used with an
 * AndroidLiveWallpaperService. Not directly constructable, instead the
 * {@link AndroidLiveWallpaperService} will create this class internally.
 * 
 * @author mzechner
 */
public class AndroidLiveWallpaper implements Application {
	static {
		GdxNativesLoader.load();
	}
	
	protected AndroidLiveWallpaperService service;

	protected AndroidGraphicsLiveWallpaper graphics;
	protected AndroidInput input;
	protected AndroidAudio audio;
	protected AndroidFiles files;
	protected AndroidNet net;
	protected ApplicationListener listener;
	protected boolean firstResume = true;
	protected final Array<Runnable> runnables = new Array<Runnable>();
	protected final Array<Runnable> executedRunnables = new Array<Runnable>();
	protected final Array<LifecycleListener> lifecycleListeners = new Array<LifecycleListener>();
	protected int logLevel = LOG_INFO;

	public AndroidLiveWallpaper(AndroidLiveWallpaperService service) {
		this.service = service;
	}
	
	public void initialize(ApplicationListener listener, AndroidApplicationConfiguration config) {
		graphics = new AndroidGraphicsLiveWallpaper(this, config, config.resolutionStrategy==null?new FillResolutionStrategy():config.resolutionStrategy);
		//input = new AndroidInput(this, this.getService(), null, config);
		input = AndroidInputFactory.newAndroidInput(this, this.getService(), graphics.view, config);
		audio = new AndroidAudio(this.getService(), config);
		
		// jw: added initialization of android local storage: /data/data/<app package>/files/
		files = new AndroidFiles(this.getService().getAssets(), this.getService().getFilesDir().getAbsolutePath());
		
		this.listener = listener;
		
		Gdx.app = this;
		Gdx.input = this.getInput();
		Gdx.audio = this.getAudio();
		Gdx.files = this.getFiles();
		Gdx.graphics = this.getGraphics();
	}

	public void onPause() {
		if (AndroidLiveWallpaperService.DEBUG) Log.d(AndroidLiveWallpaperService.TAG, " > AndroidLiveWallpaper - onPause()");

		graphics.pause();
		
		if (AndroidLiveWallpaperService.DEBUG) Log.d(AndroidLiveWallpaperService.TAG, " > AndroidLiveWallpaper - onPause() graphics paused!");

		//if (audio != null) audio.pause();	// jw: moved to AndroidGraphicsLiveWallpaper.onFrameRender
		input.unregisterSensorListeners();
		
		// jw: moved from AndroidApplication
		if (graphics != null && graphics.view != null) {
			if (graphics.view instanceof GLSurfaceViewCupcake) ((GLSurfaceViewCupcake)graphics.view).onPause();
			else if (graphics.view instanceof android.opengl.GLSurfaceView) ((android.opengl.GLSurfaceView)graphics.view).onPause();
			else throw new RuntimeException("unimplemented");
		}
		
		if (AndroidLiveWallpaperService.DEBUG) Log.d(AndroidLiveWallpaperService.TAG, " > AndroidLiveWallpaper - onPause() done!");
	}

	public void onResume() {
		Gdx.app = this;
		Gdx.input = this.getInput();
		Gdx.audio = this.getAudio();
		Gdx.files = this.getFiles();
		Gdx.graphics = this.getGraphics();

		((AndroidInput)getInput()).registerSensorListeners();
		
		//if (audio != null) audio.resume();	// jw: moved to AndroidGraphicsLiveWallpaper.onFrameRender
		if (!firstResume)
		{
			// jw: moved from AndroidApplication class
			if (graphics != null && graphics.view != null) {
				if (graphics.view instanceof GLSurfaceViewCupcake) ((GLSurfaceViewCupcake)graphics.view).onResume();
				else if (graphics.view instanceof android.opengl.GLSurfaceView) ((android.opengl.GLSurfaceView)graphics.view).onResume();
				else throw new RuntimeException("unimplemented");
			}

			
			graphics.resume();
		}
		else
			firstResume = false;
	}
	
	public void onDestroy() {

		// jw: do not call this method after onPaused had finished - it needs live gl GLThread and gl context, otherwise it will cause of deadlock
		if (graphics != null) {
			graphics.clearManagedCaches();
			graphics.destroy();
		}
		
		/*
		// jw: my earlier solution there, but it was never called, it should be called when GLThread is alive (in app.onPause -> but in wallpaper it is not clear if onPause invocation is the last one before shutdown)
		//graphics.clearManagedCaches();
		//graphics.destroy();
		
		// jw: new solution:
		if (graphics != null && graphics.view != null)
		{
			// do not invoke it there it was already invoked in AndroidLiveWallpaperService.onDestroy or onDeepPauseApplication
			//graphics.clearManagedCaches();
			
			//graphics.destroy();	// will block application because GLBaseSurfaceViewLW.GLThread is stopped/paused already
			//graphics.requestRendering();  // it doesn't revive GLThread
			
			// note: graphics.destroy needs living GLThread to process destroying
			graphics.view.onDestroy();
		}
		*/
	}

	public WindowManager getWindowManager() {
		return service.getWindowManager();
	}
	
	public AndroidLiveWallpaperService getService() {
		return service;
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
	
	@Override
	public void addLifecycleListener (LifecycleListener listener) {
		synchronized(lifecycleListeners) {
			lifecycleListeners.add(listener);
		}
	}

	@Override
	public void removeLifecycleListener (LifecycleListener listener) {
		synchronized(lifecycleListeners) {
			lifecycleListeners.removeValue(listener, true);
		}		
	}
}
