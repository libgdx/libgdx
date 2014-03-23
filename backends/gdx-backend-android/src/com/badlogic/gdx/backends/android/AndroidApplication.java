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
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/** An implementation of the {@link Application} interface for Android. Create an {@link Activity} that derives from this class. In
 * the {@link Activity#onCreate(Bundle)} method call the {@link #initialize(ApplicationListener)} method specifying the
 * configuration for the GLSurfaceView.
 * 
 * @author mzechner */
public class AndroidApplication extends Activity implements AndroidApplicationBase {
	static {
		GdxNativesLoader.load();
	}

	public static final int MINIMUM_SDK = 8;
	protected AndroidGraphics graphics;
	protected AndroidInput input;
	protected AndroidAudio audio;
	protected AndroidFiles files;
	protected AndroidNet net;
	protected ApplicationListener listener;
	public Handler handler;
	protected boolean firstResume = true;
	protected final Array<Runnable> runnables = new Array<Runnable>();
	protected final Array<Runnable> executedRunnables = new Array<Runnable>();
	protected final Array<LifecycleListener> lifecycleListeners = new Array<LifecycleListener>();
	protected int logLevel = LOG_INFO;
	protected boolean useImmersiveMode = false;
	protected boolean hideStatusBar = false;
	private int wasFocusChanged = -1;
	private boolean isWaitingForAudio = false;

	/** This method has to be called in the {@link Activity#onCreate(Bundle)} method. It sets up all the things necessary to get
	 * input, render via OpenGL and so on. Uses a default {@link AndroidApplicationConfiguration}.
	 * 
	 * @param listener the {@link ApplicationListener} implementing the program logic **/
	public void initialize (ApplicationListener listener) {
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(listener, config);
	}

	/** This method has to be called in the {@link Activity#onCreate(Bundle)} method. It sets up all the things necessary to get
	 * input, render via OpenGL and so on. You can configure other aspects of the application with the rest of the fields in the
	 * {@link AndroidApplicationConfiguration} instance.
	 * 
	 * @param listener the {@link ApplicationListener} implementing the program logic
	 * @param config the {@link AndroidApplicationConfiguration}, defining various settings of the application (use accelerometer,
	 *           etc.). */
	public void initialize (ApplicationListener listener, AndroidApplicationConfiguration config) {
		if (this.getVersion() < MINIMUM_SDK) {
			throw new GdxRuntimeException("LibGDX requires Android API Level " + MINIMUM_SDK + " or later.");
		}
		graphics = new AndroidGraphics(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy()
			: config.resolutionStrategy);
		input = AndroidInputFactory.newAndroidInput(this, this, graphics.view, config);
		audio = new AndroidAudio(this, config);
		this.getFilesDir(); // workaround for Android bug #10515463
		files = new AndroidFiles(this.getAssets(), this.getFilesDir().getAbsolutePath());
		net = new AndroidNet(this);
		this.listener = listener;
		this.handler = new Handler();
		this.useImmersiveMode = config.useImmersiveMode;
		this.hideStatusBar = config.hideStatusBar;

		Gdx.app = this;
		Gdx.input = this.getInput();
		Gdx.audio = this.getAudio();
		Gdx.files = this.getFiles();
		Gdx.graphics = this.getGraphics();
		Gdx.net = this.getNet();

		try {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		} catch (Exception ex) {
			log("AndroidApplication", "Content already displayed, cannot request FEATURE_NO_TITLE", ex);
		}
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		setContentView(graphics.getView(), createLayoutParams());
		createWakeLock(config.useWakelock);
		hideStatusBar(this.hideStatusBar);
		useImmersiveMode(this.useImmersiveMode);
		if (this.useImmersiveMode && getVersion() >= 19) {
			try {
				Class vlistener = Class.forName("com.badlogic.gdx.backends.android.AndroidVisibilityListener");
				Object o = vlistener.newInstance();
				Method method = vlistener.getDeclaredMethod("createListener", AndroidApplicationBase.class);
				method.invoke(o, this);
			} catch (Exception e) {
				log("AndroidApplication", "Failed to create AndroidVisibilityListener", e);
			}
		}
	}

	protected FrameLayout.LayoutParams createLayoutParams () {
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
			android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		layoutParams.gravity = Gravity.CENTER;
		return layoutParams;
	}

	protected void createWakeLock (boolean use) {
		if (use) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	protected void hideStatusBar (boolean hide) {
		if (!hide || getVersion() < 11) return;

		View rootView = getWindow().getDecorView();

		try {
			Method m = View.class.getMethod("setSystemUiVisibility", int.class);
			m.invoke(rootView, 0x0);
			m.invoke(rootView, 0x1);
		} catch (Exception e) {
			log("AndroidApplication", "Can't hide status bar", e);
		}
	}

	@Override
	public void onWindowFocusChanged (boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		useImmersiveMode(this.useImmersiveMode);
		hideStatusBar(this.hideStatusBar);
		if (hasFocus) {
			this.wasFocusChanged = 1;
			if (this.isWaitingForAudio) {
				this.audio.resume();
				this.isWaitingForAudio = false;
			}
		} else {
			this.wasFocusChanged = 0;
		}
	}

	@Override
	public void useImmersiveMode (boolean use) {
		if (!use || getVersion() < 19) return;

		View view = getWindow().getDecorView();
		try {
			Method m = View.class.getMethod("setSystemUiVisibility", int.class);
			int code = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
			code ^= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
			code ^= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
			code ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
			code ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			code ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			m.invoke(view, code);
		} catch (Exception e) {
			log("AndroidApplication", "Can't set immersive mode", e);
		}
	}

	/** This method has to be called in the {@link Activity#onCreate(Bundle)} method. It sets up all the things necessary to get
	 * input, render via OpenGL and so on. Uses a default {@link AndroidApplicationConfiguration}.
	 * <p>
	 * Note: you have to add the returned view to your layout!
	 * 
	 * @param listener the {@link ApplicationListener} implementing the program logic
	 * @return the GLSurfaceView of the application */
	public View initializeForView (ApplicationListener listener) {
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		return initializeForView(listener, config);
	}

	/** This method has to be called in the {@link Activity#onCreate(Bundle)} method. It sets up all the things necessary to get
	 * input, render via OpenGL and so on. You can configure other aspects of the application with the rest of the fields in the
	 * {@link AndroidApplicationConfiguration} instance.
	 * <p>
	 * Note: you have to add the returned view to your layout!
	 * 
	 * @param listener the {@link ApplicationListener} implementing the program logic
	 * @param config the {@link AndroidApplicationConfiguration}, defining various settings of the application (use accelerometer,
	 *           etc.).
	 * @return the GLSurfaceView of the application */
	public View initializeForView (ApplicationListener listener, AndroidApplicationConfiguration config) {
		if (this.getVersion() < MINIMUM_SDK) {
			throw new GdxRuntimeException("LibGDX requires Android API Level " + MINIMUM_SDK + " or later.");
		}
		graphics = new AndroidGraphics(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy()
			: config.resolutionStrategy);
		input = AndroidInputFactory.newAndroidInput(this, this, graphics.view, config);
		audio = new AndroidAudio(this, config);
		this.getFilesDir(); // workaround for Android bug #10515463
		files = new AndroidFiles(this.getAssets(), this.getFilesDir().getAbsolutePath());
		net = new AndroidNet(this);
		this.listener = listener;
		this.handler = new Handler();
		this.useImmersiveMode = config.useImmersiveMode;
		this.hideStatusBar = config.hideStatusBar;

		Gdx.app = this;
		Gdx.input = this.getInput();
		Gdx.audio = this.getAudio();
		Gdx.files = this.getFiles();
		Gdx.graphics = this.getGraphics();
		Gdx.net = this.getNet();

		createWakeLock(config.useWakelock);
		hideStatusBar(this.hideStatusBar);
		useImmersiveMode(this.useImmersiveMode);
		if (this.useImmersiveMode && getVersion() >= 19) {
			try {
				Class vlistener = Class.forName("com.badlogic.gdx.backends.android.AndroidVisibilityListener");
				Object o = vlistener.newInstance();
				Method method = vlistener.getDeclaredMethod("createListener", AndroidApplicationBase.class);
				method.invoke(o, this);
			} catch (Exception e) {
				log("AndroidApplication", "Failed to create AndroidVisibilityListener", e);
			}
		}
		return graphics.getView();
	}

	@Override
	protected void onPause () {
		boolean isContinuous = graphics.isContinuousRendering();
		graphics.setContinuousRendering(true);
		graphics.pause();

		input.unregisterSensorListeners();

		int[] realId = input.realId;
		// erase pointer ids. this sucks donkeyballs...
		Arrays.fill(realId, -1);
		boolean[] touched = input.touched;
		// erase touched state. this also sucks donkeyballs...
		Arrays.fill(touched, false);

		if (isFinishing()) {
			graphics.clearManagedCaches();
			graphics.destroy();
		}
		graphics.setContinuousRendering(isContinuous);

		if (graphics != null && graphics.view != null) {
			if (graphics.view instanceof android.opengl.GLSurfaceView) ((android.opengl.GLSurfaceView)graphics.view).onPause();
		}

		super.onPause();
	}

	@Override
	protected void onResume () {
		Gdx.app = this;
		Gdx.input = this.getInput();
		Gdx.audio = this.getAudio();
		Gdx.files = this.getFiles();
		Gdx.graphics = this.getGraphics();
		Gdx.net = this.getNet();

		((AndroidInput)getInput()).registerSensorListeners();

		if (graphics != null && graphics.view != null) {
			if (graphics.view instanceof android.opengl.GLSurfaceView) ((android.opengl.GLSurfaceView)graphics.view).onResume();
		}

		if (!firstResume) {
			graphics.resume();
		} else
			firstResume = false;

		this.isWaitingForAudio = true;
		if (this.wasFocusChanged == 1 || this.wasFocusChanged == -1) {
			this.audio.resume();
			this.isWaitingForAudio = false;
		}
		super.onResume();
	}

	@Override
	protected void onDestroy () {
		super.onDestroy();
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return listener;
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
		return new AndroidPreferences(getSharedPreferences(name, Context.MODE_PRIVATE));
	}

	AndroidClipboard clipboard;

	@Override
	public Clipboard getClipboard () {
		if (clipboard == null) {
			clipboard = new AndroidClipboard(this);
		}
		return clipboard;
	}

	@Override
	public void postRunnable (Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
			Gdx.graphics.requestRendering();
		}
	}

	@Override
	public void onConfigurationChanged (Configuration config) {
		super.onConfigurationChanged(config);
		boolean keyboardAvailable = false;
		if (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) keyboardAvailable = true;
		input.keyboardAvailable = keyboardAvailable;
	}

	@Override
	public void exit () {
		handler.post(new Runnable() {
			@Override
			public void run () {
				AndroidApplication.this.finish();
			}
		});
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
	public Context getContext () {
		return this;
	}

	@Override
	public Array<Runnable> getRunnables () {
		return runnables;
	}

	@Override
	public Array<Runnable> getExecutedRunnables () {
		return executedRunnables;
	}

	@Override
	public Array<LifecycleListener> getLifecycleListeners () {
		return lifecycleListeners;
	}

	@Override
	public boolean isFragment () {
		return false;
	}

	@Override
	public Window getApplicationWindow () {
		return this.getWindow();
	}

	@Override
	public Handler getHandler () {
		return this.handler;
	}
}
