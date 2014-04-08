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

import java.lang.reflect.Method;
import java.util.Arrays;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

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
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** An implementation of the {@link Application} interface to be used with an AndroidLiveWallpaperService. Not directly
 * constructable, instead the {@link AndroidLiveWallpaperService} will create this class internally.
 * 
 * @author mzechner */
public class AndroidLiveWallpaper implements Application {
	static {
		GdxNativesLoader.load();
	}
	
	public static final int MINIMUM_SDK = 8;
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

	public AndroidLiveWallpaper (AndroidLiveWallpaperService service) {
		this.service = service;
	}

	public void initialize (ApplicationListener listener, AndroidApplicationConfiguration config) {
		if (this.getVersion() < MINIMUM_SDK) {
			throw new GdxRuntimeException("LibGDX requires Android API Level " + MINIMUM_SDK + " or later.");
		}
		graphics = new AndroidGraphicsLiveWallpaper(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy()
			: config.resolutionStrategy);

		// factory in use, but note: AndroidInputFactory causes exceptions when obfuscated: java.lang.RuntimeException: Couldn't
		// construct AndroidInput, this should never happen, proguard deletes constructor used only by reflection
		input = AndroidInputFactory.newAndroidInput(this, this.getService(), graphics.view, config);
		// input = new AndroidInput(this, this.getService(), null, config);

		audio = new AndroidAudio(this.getService(), config);

		// added initialization of android local storage: /data/data/<app package>/files/
		this.getService().getFilesDir(); // workaround for Android bug #10515463
		files = new AndroidFiles(this.getService().getAssets(), this.getService().getFilesDir().getAbsolutePath());

		this.listener = listener;

		Gdx.app = this;
		Gdx.input = input;
		Gdx.audio = audio;
		Gdx.files = files;
		Gdx.graphics = graphics;
	}

	public void onPause () {
		if (AndroidLiveWallpaperService.DEBUG) Log.d(AndroidLiveWallpaperService.TAG, " > AndroidLiveWallpaper - onPause()");

		// IMPORTANT!
		// jw: graphics.pause is never called, graphics.pause works on most devices but not on all..
		// for example on Samsung Galaxy Tab (GT-P6800) on android 4.0.4 invoking graphics.pause causes "Fatal Signal 11"
		// near mEglHelper.swap() in GLSurfaceView while processing next onPause event.
		// See related issue:
		// http://code.google.com/p/libgdx/issues/detail?id=541
		// the problem with graphics.pause occurs while using OpenGL 2.0 and original GLSurfaceView while rotating device in lwp
// preview
		// in my opinion it is a bug of android not libgdx, even example Cubic live wallpaper from
		// Android SDK crashes on affected devices.......... and on some configurations of android emulator too.
		//
		// My wallpaper was rejected on Samsung Apps because of this issue, so I decided to disable graphics.pause..
		// also I moved audio lifecycle methods from AndroidGraphicsLiveWallpaper into this class

		// graphics.pause();
		// if (AndroidLiveWallpaperService.DEBUG) Log.d(AndroidLiveWallpaperService.TAG,
// " > AndroidLiveWallpaper - onPause() application paused!");
		audio.pause();

		input.unregisterSensorListeners();

		int[] realId = input.realId;
		// erase pointer ids. this sucks donkeyballs...
		Arrays.fill(realId, -1);
		boolean[] touched = input.touched;
		// erase touched state. this also sucks donkeyballs...
		Arrays.fill(touched, false);

		if (graphics != null && graphics.view != null) {
			if (graphics.view instanceof GLSurfaceViewAPI18)
				((GLSurfaceViewAPI18)graphics.view).onPause();
			else if (graphics.view instanceof GLSurfaceView)
				((GLSurfaceView)graphics.view).onPause();
			else
				throw new RuntimeException("unimplemented");
		}

		if (AndroidLiveWallpaperService.DEBUG) Log.d(AndroidLiveWallpaperService.TAG, " > AndroidLiveWallpaper - onPause() done!");
	}

	public void onResume () {
		Gdx.app = this;
		Gdx.input = input;
		Gdx.audio = audio;
		Gdx.files = files;
		Gdx.graphics = graphics;

		input.registerSensorListeners();

		// FIXME restore conditional execution if lifecycle errors will occur when GLSurfaceView used.
		// GLSurfaceView is guaranteed to work with this condition on, but GLSurfaceViewCupcake requires it off,
		// so I disabled it.
		// if (!firstResume) // mentioned condition
		if (graphics != null && graphics.view != null) {
			if (graphics.view instanceof GLSurfaceViewAPI18)
				((GLSurfaceViewAPI18)graphics.view).onResume();
			else if (graphics.view instanceof GLSurfaceView)
				((GLSurfaceView)graphics.view).onResume();
			else
				throw new RuntimeException("unimplemented");
		}

		if (!firstResume) {
			audio.resume();
			graphics.resume();
		} else
			firstResume = false;
	}

	public void onDestroy () {

		// it is too late to call graphics.destroy - it needs live gl GLThread and gl context, otherwise it will cause of deadlock
		// if (graphics != null) {
		// graphics.clearManagedCaches();
		// graphics.destroy();
		// }

		// so we do what we can..
		if (graphics != null) {
			// not necessary - already called in AndroidLiveWallpaperService.onDeepPauseApplication
			// app.graphics.clearManagedCaches();

			// kill the GLThread managed by GLSurfaceView (only for GLSurfaceView because GLSurffaceViewCupcake stops thread in
// onPause events - which is not as easy and safe for GLSurfaceView)
			if (graphics.view != null &&
					(graphics.view instanceof GLSurfaceView || graphics.view instanceof GLSurfaceViewAPI18)) {
				View glSurfaceView = graphics.view;
				try {
					Method method = null;
					for (Method m : glSurfaceView.getClass().getMethods()) {
						if (m.getName().equals("onDestroy")) // implemented in AndroidGraphicsLiveWallpaper, redirects to
// onDetachedFromWindow - which stops GLThread by calling mGLThread.requestExitAndWait()
						{
							method = m;
							break;
						}
					}

					if (method != null) {
						method.invoke(glSurfaceView);
						if (AndroidLiveWallpaperService.DEBUG)
							Log.d(AndroidLiveWallpaperService.TAG,
								" > AndroidLiveWallpaper - onDestroy() stopped GLThread managed by GLSurfaceView");
					} else
						throw new Exception("method not found!");
				} catch (Throwable t) {
					// error while scheduling exit of GLThread, GLThread will remain live and wallpaper service wouldn't be able to
// shutdown completely
					Log.e(AndroidLiveWallpaperService.TAG,
						"failed to destroy GLSurfaceView's thread! GLSurfaceView.onDetachedFromWindow impl changed since API lvl 16!");
					t.printStackTrace();
				}
			}
		}

		if (audio != null) {
			// dispose audio and free native resources, mandatory since graphics.pause is never called in live wallpaper
			audio.dispose();
		}
	}

	public WindowManager getWindowManager () {
		return service.getWindowManager();
	}

	public AndroidLiveWallpaperService getService () {
		return service;
	}

	public ApplicationListener getListener () {
		return listener;
	}

	@Override
	public void postRunnable (Runnable runnable) {
		synchronized (runnables) {
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
		return android.os.Build.VERSION.SDK_INT;
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
	public Clipboard getClipboard () {
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
	public void log (String tag, String message, Throwable exception) {
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
	public int getLogLevel () {
		return logLevel;
	}

	@Override
	public void exit () {
		// no-op
	}

	@Override
	public void addLifecycleListener (LifecycleListener listener) {
		synchronized (lifecycleListeners) {
			lifecycleListeners.add(listener);
		}
	}

	@Override
	public void removeLifecycleListener (LifecycleListener listener) {
		synchronized (lifecycleListeners) {
			lifecycleListeners.removeValue(listener, true);
		}
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return listener;
	}
}
