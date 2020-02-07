/*
 *******************************************************************************
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

import android.content.Context;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.support.wearable.watchface.WatchFaceService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SnapshotArray;

/**
 * An implementation of the {@link com.badlogic.gdx.Application} interface dedicated for WearOS watch face.
 * <p>
 * Drive from these class. In the {@link #onCreateGdxApplication()} method call the {@link #initialize(ApplicationListener)}
 * to setup and start GDX application.
 *
 * @author Chenhe
 */
public abstract class WearOsWatchFaceService extends WatchFaceService implements AndroidServiceApplication {

	static {
		GdxNativesLoader.load();
	}

	private static final String TAG = "GdxWatchFaceService";
	private static final boolean DEBUG = false;

	protected AndroidGraphicsLiveWallpaper graphics;
	protected AndroidInput input;
	protected AndroidAudio audio;
	protected AndroidFiles files;
	protected AndroidNet net;
	protected AndroidClipboard clipboard;
	protected ApplicationListener listener;
	protected Handler handler;
	protected final Array<Runnable> runnables = new Array<Runnable>();
	protected final Array<Runnable> executedRunnables = new Array<Runnable>();
	protected final SnapshotArray<LifecycleListener> lifecycleListeners = new SnapshotArray<LifecycleListener>(LifecycleListener.class);
	protected int logLevel = LOG_INFO;
	protected ApplicationLogger applicationLogger;

	protected SurfaceHolder.Callback view = null;

	private final int[] sync = new int[0];

	protected volatile WearOsWatchFaceEngine linkedEngine = null; // can be accessed from GL render thread by getSurfaceHolder

	@Override
	public SurfaceHolder getSurfaceHolder () {
		if (DEBUG)
			Log.d(TAG, " > WearOsWatchFaceService - getSurfaceHolder()");

		synchronized (sync) {
			if (linkedEngine == null)
				return null;
			else
				return linkedEngine.getSurfaceHolder();
		}
	}

	// lifecycle methods - the order of calling (flow) is maintained ///////////////////////////////////////////////

	/**
	 * Service started, but GDX application hasn't started yet. Do NOT invoke
	 * {@link #initialize(ApplicationListener, AndroidApplicationConfiguration)} here.
	 */
	@Override
	public void onCreate () {
		if (DEBUG)
			Log.d(TAG, " > WearOsWatchFaceService - onCreate() " + hashCode());
		super.onCreate();
	}

	/**
	 * Create a watch face engine. Do NOT override this method, service manages them internally.
	 */
	@Override
	public Engine onCreateEngine () {
		if (DEBUG)
			Log.d(TAG, " > WearOsWatchFaceService - onCreateEngine()");
		return new WearOsWatchFaceEngine();
	}

	/**
	 * GDX application should start now. Override this method to invoke {@link #initialize(ApplicationListener)}
	 * or {@link #initialize(ApplicationListener, AndroidApplicationConfiguration)}.
	 */
	public abstract void onCreateGdxApplication ();

	/**
	 * See {@link #initialize(ApplicationListener, AndroidApplicationConfiguration)}.
	 *
	 * @param listener The {@link ApplicationListener} implementing the program logic.
	 */
	public void initialize (ApplicationListener listener) {
		initialize(listener, new AndroidApplicationConfiguration());
	}

	/**
	 * This method has to be called in the {@link #onCreateGdxApplication()} method. It sets up all the things
	 * necessary to get input, render via OpenGL and so on. You can configure other aspects of the application
	 * with the rest of the fields in the {@link AndroidApplicationConfiguration} instance.
	 *
	 * @param listener The {@link ApplicationListener} implementing the program logic.
	 * @param config   The {@link AndroidApplicationConfiguration}, defining various settings of the application (use accelerometer, etc.).
	 */
	public void initialize (ApplicationListener listener, AndroidApplicationConfiguration config) {
		if (getVersion() < MINIMUM_SDK) {
			throw new GdxRuntimeException("LibGDX requires Android API Level " + MINIMUM_SDK + " or later.");
		}

		setApplicationLogger(new AndroidApplicationLogger());
		graphics = new AndroidGraphicsLiveWallpaper(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy() : config.resolutionStrategy);

		// factory in use, but note: AndroidInputFactory causes exceptions when obfuscated: java.lang.RuntimeException: Couldn't
		// construct AndroidInput, this should never happen, proguard deletes constructor used only by reflection
		input = createInput(this, this, graphics.view, config);

		audio = createAudio(this, config);

		files = new AndroidFiles(getAssets(), this);
		net = new AndroidNet(this, config);
		clipboard = new AndroidClipboard(this);
		this.listener = listener;
		handler = new Handler(Looper.getMainLooper());

		Gdx.app = this;
		Gdx.input = input;
		Gdx.audio = audio;
		Gdx.files = files;
		Gdx.graphics = graphics;
		Gdx.net = net;
	}

	@Override
	public void onDestroy () {
		if (DEBUG)
			Log.d(TAG, " > WearOsWatchFaceService - onDestroy() " + hashCode());
		super.onDestroy();
	}

	// end of lifecycle methods ////////////////////////////////////////////////////////////////////////////////////

	public class WearOsWatchFaceEngine extends Engine {

		protected boolean engineIsVisible = false;

		// destination format of surface when this engine is active (updated in onSurfaceChanged)
		protected int format;
		protected int width;
		protected int height;

		// lifecycle methods - the order of calling (flow) is maintained //////////////////////////////////////////

		public WearOsWatchFaceEngine () {
			if (DEBUG)
				Log.d(TAG, " > Engine() " + hashCode());
		}

		@Override
		public void onCreate (SurfaceHolder holder) {
			if (DEBUG)
				Log.d(TAG, " > Engine - onCreate() " + hashCode() + ", thread: " + Thread.currentThread().toString());
			super.onCreate(holder);
		}

		@Override
		public void onSurfaceCreated (SurfaceHolder holder) {
			if (DEBUG)
				Log.d(TAG, " > Engine - onSurfaceCreated() " + hashCode());
			super.onSurfaceCreated(holder);
			linkedEngine = this;

			format = 0; // must be initialized with zeroes
			width = 0;
			height = 0;

			onCreateGdxApplication();
			if (graphics == null)
				throw new Error("You must override 'onCreateGdxApplication' method and call 'initialize' from its body.");

			view = (SurfaceHolder.Callback)graphics.view;
			// We maintain its events manually to prevent unnecessary onSurfaceChanged() call.
			getSurfaceHolder().removeCallback(view);

			view.surfaceCreated(holder);
			if (!Gdx.graphics.isContinuousRendering()) {
				Gdx.graphics.requestRendering();
			}
		}

		/**
		 * This is called immediately after any structural changes (format or size) have been madeto the surface.
		 * We should at this point update the imagery in the surface. This method is always called at least once,
		 * after {@link #onSurfaceCreated(SurfaceHolder)}.
		 *
		 * @param holder The SurfaceHolder whose surface has changed.
		 * @param format The new PixelFormat of the surface.
		 * @param width  The new width of the surface.
		 * @param height The new height of the surface.
		 * @see <a href="https://developer.android.com/reference/android/view/SurfaceHolder.Callback.html#surfacechanged">Android docs</a>
		 */
		@Override
		public void onSurfaceChanged (SurfaceHolder holder, int format, int width, int height) {
			if (DEBUG)
				Log.d(TAG, " > Engine - onSurfaceChanged() surface valid: " + getSurfaceHolder().getSurface().isValid());
			super.onSurfaceChanged(holder, format, width, height);

			notifySurfaceChanged(format, width, height, true);
		}

		/**
		 * Notifies GLSurfaceView about changed surface format.
		 *
		 * @param forceUpdate if false, surface view will be notified only if currently contains expired information
		 */
		private void notifySurfaceChanged (final int format, final int width, final int height, boolean forceUpdate) {
			if (!forceUpdate && format == this.format && width == this.width && height == this.height) {
				// skip if didn't changed
				if (DEBUG)
					Log.d(TAG, " > surface is current, skipping surfaceChanged event");
			} else {
				// update engine desired surface format
				this.format = format;
				this.width = width;
				this.height = height;
				view.surfaceChanged(getSurfaceHolder(), this.format, this.width, this.height);
			}
		}

		@Override
		public void onVisibilityChanged (boolean visible) {
			boolean reportedVisible = isVisible();
			if (DEBUG)
				Log.d(TAG, " > Engine - onVisibilityChanged(paramVisible: " + visible + " reportedVisible: " + reportedVisible + ") " + hashCode() + ", sufcace valid: " + getSurfaceHolder().getSurface().isValid());
			super.onVisibilityChanged(visible);
			// Android WallpaperService sends fake visibility changed events for some reason but not necessary here.
			// Whether surface has changed or not, engine will keep pause if it is invisible.
			// See https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android10-c2f2-release/core/java/android/service/wallpaper/WallpaperService.java#1005
			if (!reportedVisible && visible) {
				if (DEBUG)
					Log.d(TAG, " > fake visibilityChanged event!");
				return;
			}

			notifyVisibilityChanged(visible);
		}

		private void notifyVisibilityChanged (final boolean visible) {
			if (engineIsVisible != visible) {
				engineIsVisible = visible;

				if (engineIsVisible)
					onResume();
				else
					onPause();
			} else {
				if (DEBUG)
					Log.d(TAG, " > visible state is current, skipping visibilityChanged event!");
			}
		}

		@Override
		public void onSurfaceDestroyed (SurfaceHolder holder) {
			if (DEBUG)
				Log.d(TAG, " > Engine - onSurfaceDestroyed() " + hashCode() + ", isVisible: " + engineIsVisible);

			graphics.clearManagedCaches();

			// Resume the SurfaceView to trigger onDrawFrame() one more time.
			// If not, AndroidGraphics.synch will never be notified which cause AndroidGraphics.destroy() never complete.
			graphics.onResumeGLSurfaceView();
			graphics.destroy();
			// Now destroy() method should has completed, let's pause the SurfaceView.
			graphics.onPauseGLSurfaceView();

			graphics.onDestroyGLSurfaceView();
			view.surfaceDestroyed(holder);

			format = 0;
			width = 0;
			height = 0;

			audio.dispose();
			super.onSurfaceDestroyed(holder);
		}

		@Override
		public void onDestroy () {
			if (DEBUG)
				Log.d(TAG, " > Engine - onDestroy() " + hashCode());
			super.onDestroy();
		}

		// end of lifecycle methods ////////////////////////////////////////////////////////////////////////////////

		/**
		 * Called when watch face comes to visible in {@link #onVisibilityChanged(boolean)}.
		 */
		private void onResume () {
			if (DEBUG)
				Log.d(TAG, " > Engine - onResume() " + hashCode());

			// update if surface changed when engine wasn't active
			notifySurfaceChanged(format, width, height, false);

			Gdx.app = WearOsWatchFaceService.this;
			Gdx.input = getInput();
			Gdx.audio = getAudio();
			Gdx.files = getFiles();
			Gdx.graphics = getGraphics();
			Gdx.net = getNet();

			input.onResume();
			audio.resume();
			if (graphics != null) {
				graphics.onResumeGLSurfaceView();
				graphics.resume();
			}

			if (!Gdx.graphics.isContinuousRendering()) {
				Gdx.graphics.requestRendering();
			}
		}

		/**
		 * Called when watch face is no longer visible in {@link #onVisibilityChanged(boolean)}.
		 */
		private void onPause () {
			if (DEBUG)
				Log.d(TAG, " > Engine - onPause() " + hashCode());

			// Not like LiveWallpaper, watch face only available on some recent WearOS device.
			// So it should not have the bug which described in AndroidLiveWallpaper.onPause().
			graphics.pause();
			input.onPause();
			audio.pause();
			graphics.onPauseGLSurfaceView();
		}

		@Override
		public void onAmbientModeChanged (final boolean inAmbientMode) {
			if (DEBUG)
				Log.d(TAG, " > Engine - onAmbientModeChanged(paramVisible: " + inAmbientMode + ") " + hashCode() + ", surface valid: " + getSurfaceHolder().getSurface().isValid());
			if (listener instanceof WearOsWatchFaceListener) {
				postRunnable(new Runnable() {
					@Override
					public void run () {
						((WearOsWatchFaceListener)listener).onAmbientModeChanged(isInAmbientMode());
					}
				});
			}
			if (inAmbientMode) {
				// According to the Android documents, we must stop continuous rendering in ambient mode to save battery.
				// In this case rendering only needed when onTimeTick() called. Otherwise power consumption is unacceptable.
				// See https://developer.android.com/training/wearables/watch-faces/drawing#TimeTick.
				Gdx.graphics.setContinuousRendering(false);
				if (DEBUG)
					Log.d(TAG, " > Engine - enter ambient mode, ContinuousRendering: " + Gdx.graphics.isContinuousRendering());
				Gdx.graphics.requestRendering();
			} else {
				Gdx.graphics.setContinuousRendering(true);
				if (DEBUG)
					Log.d(TAG, " > Engine - exit ambient mode, ContinuousRendering: " + Gdx.graphics.isContinuousRendering());
			}
			super.onAmbientModeChanged(inAmbientMode);
		}

		@Override
		public void onTimeTick () {
			super.onTimeTick();
			if (DEBUG)
				Log.d(TAG, " > Engine - onTimeTick()" + hashCode() + ", inAmbientMode: " + isInAmbientMode() + ", continuousRendering: " + Gdx.graphics.isContinuousRendering());
			// Only in ambient mode we need request rendering, or it will be continuously rendering mode.
			if (isInAmbientMode()) {
				// Just in case to rescue battery
				if (Gdx.graphics.isContinuousRendering()) {
					Log.w(TAG, " > Engine: should not continuously rendering, correct it.");
					Gdx.graphics.setContinuousRendering(false);
				}
				Gdx.graphics.requestRendering();
			}
		}

		@SuppressWarnings("deprecation")
		@Override
		public void onTouchEvent (MotionEvent event) {
			super.onTouchEvent(event);
			input.onTouch(null, event);
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
	public void runOnUiThread (Runnable runnable) {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			handler.post(runnable);
		} else {
			runnable.run();
		}
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return listener;
	}

	@Override
	public Graphics getGraphics () {
		return graphics;
	}

	@Override
	public Audio getAudio () {
		return audio;
	}

	@Override
	public AndroidInput getInput () {
		return input;
	}

	@Override
	public Files getFiles () {
		return files;
	}

	@Override
	public Net getNet () {
		return net;
	}

	@Override
	public void log (String tag, String message) {
		if (logLevel >= LOG_INFO)
			applicationLogger.log(tag, message);
	}

	@Override
	public void log (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_INFO)
			applicationLogger.log(tag, message, exception);
	}

	@Override
	public void error (String tag, String message) {
		if (logLevel >= LOG_ERROR)
			applicationLogger.error(tag, message);
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_ERROR)
			applicationLogger.error(tag, message, exception);
	}

	@Override
	public void debug (String tag, String message) {
		if (logLevel >= LOG_ERROR)
			applicationLogger.debug(tag, message);
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_ERROR)
			applicationLogger.debug(tag, message, exception);
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
	public void setApplicationLogger (ApplicationLogger applicationLogger) {
		this.applicationLogger = applicationLogger;
	}

	@Override
	public ApplicationLogger getApplicationLogger () {
		return applicationLogger;
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

	@Override
	public Clipboard getClipboard () {
		return clipboard;
	}

	@Override
	public void postRunnable (Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
		}
	}

	@Override
	public void exit () {
		// no-op
		// Since watch face service is managed by system, we can't exit forwardly.
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
	public SnapshotArray<LifecycleListener> getLifecycleListeners () {
		return lifecycleListeners;
	}

	@Override
	public Window getApplicationWindow () {
		throw new UnsupportedOperationException();
	}

	@Override
	public WindowManager getWindowManager () {
		return (WindowManager)getSystemService(Context.WINDOW_SERVICE);
	}

	@Override
	public void useImmersiveMode (boolean b) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Handler getHandler () {
		return handler;
	}

	@Override
	public boolean isDebug () {
		return DEBUG;
	}

	@Override
	public AndroidAudio createAudio (Context context, AndroidApplicationConfiguration config) {
		return new DefaultAndroidAudio(context, config);
	}

	@Override
	public AndroidInput createInput (Application activity, Context context, Object view, AndroidApplicationConfiguration config) {
		return new DefaultAndroidInput(this, this, graphics.view, config);
	}
}
