/*
 * Copyright 2013 Jaroslaw Wisniewski <j.wisniewski@appsisle.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *
 */

package com.badlogic.gdx.backends.android;

import android.content.Context;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.utils.GdxNativesLoader;

/** An implementation of the {@link Application} interface dedicated for android live wallpapers.
 * 
 * Derive from this class. In the {@link AndroidLiveWallpaperService#onCreateApplication} method call the
 * {@link AndroidLiveWallpaperService#initialize(ApplicationListener)} method specifying the configuration for the GLSurfaceView.
 * You can also use {@link AndroidWallpaperListener} along with {@link ApplicationListener} to respond for wallpaper specific
 * events in your app listener:
 * 
 * MyAppListener implements ApplicationListener, AndroidWallpaperListener
 * 
 * Notice: Following methods are not called for live wallpapers: {@link ApplicationListener#pause()}
 * {@link ApplicationListener#dispose()} TODO add callbacks to AndroidWallpaperListener allowing to notify app listener about
 * changed visibility state of live wallpaper but called from main thread, not from GL thread: for example:
 * AndroidWallpaperListener.visibilityChanged(boolean)
 * 
 * //obsoleted: //Notice! //You have to kill all not daemon threads you created in {@link ApplicationListener#pause()} method. //
 * {@link ApplicationListener#dispose()} is never called! //If you leave live non daemon threads, wallpaper service wouldn't be
 * able to close, //this can cause problems with wallpaper lifecycle.
 * 
 * Notice #2! On some devices wallpaper service is not killed immediately after exiting from preview. Service object is destroyed
 * (onDestroy called) but process on which it runs remains alive. When user comes back to wallpaper preview, new wallpaper service
 * object is created, but in the same process. It is important if you plan to use static variables / objects - they will be shared
 * between living instances of wallpaper services'! And depending on your implementation - it can cause problems you were not
 * prepared to.
 * 
 * @author Jaroslaw Wisniewski <j.wisniewski@appsisle.com> */
public abstract class AndroidLiveWallpaperService extends WallpaperService {
	static {
		GdxNativesLoader.load();
	}

	static final String TAG = "WallpaperService";
	static boolean DEBUG = false; // TODO remember to disable this

	// instance of libGDX Application, acts as singleton - one instance per application (per WallpaperService)
	protected volatile AndroidLiveWallpaper app = null; // can be accessed from GL render thread
	protected SurfaceHolder.Callback view = null;

	// current format of surface (one GLSurfaceView is shared between all engines)
	protected int viewFormat;
	protected int viewWidth;
	protected int viewHeight;

	// app is initialized when engines == 1 first time, app is destroyed in WallpaperService.onDestroy, but
// ApplicationListener.dispose is not called for wallpapers
	protected int engines = 0;
	protected int visibleEngines = 0;

	// engine currently associated with app instance, linked engine serves surface handler for GLSurfaceView
	protected volatile AndroidWallpaperEngine linkedEngine = null; // can be accessed from GL render thread by getSurfaceHolder

	protected void setLinkedEngine (AndroidWallpaperEngine linkedEngine) {
		synchronized (sync) {
			this.linkedEngine = linkedEngine;
		}
	}

	// if preview state notified ever
	protected volatile boolean isPreviewNotified = false;

	// the value of last preview state notified to app listener
	protected volatile boolean notifiedPreviewState = false;

	volatile int[] sync = new int[0];

	// volatile ReentrantLock lock = new ReentrantLock();

	// lifecycle methods - the order of calling (flow) is maintained ///////////////

	public AndroidLiveWallpaperService () {
		super();
	}

	/** Service is starting, libGDX application is shutdown now */
	@Override
	public void onCreate () {
		if (DEBUG) Log.d(TAG, " > AndroidLiveWallpaperService - onCreate() " + hashCode());
		Log.i(TAG, "service created");

		super.onCreate();
	}

	/** One of wallpaper engines is starting. Do not override this method, service manages them internally. */
	@Override
	public Engine onCreateEngine () {
		if (DEBUG) Log.d(TAG, " > AndroidLiveWallpaperService - onCreateEngine()");
		Log.i(TAG, "engine created");

		return new AndroidWallpaperEngine();
	}

	/** libGDX application is starting, it occurs after first wallpaper engine had started. Override this method an invoke
	 * {@link AndroidLiveWallpaperService#initialize(ApplicationListener, AndroidApplicationConfiguration)} from there. */
	public void onCreateApplication () {
		if (DEBUG) Log.d(TAG, " > AndroidLiveWallpaperService - onCreateApplication()");
	}

	/** Look at {@link AndroidLiveWallpaperService#initialize(ApplicationListener, AndroidApplicationConfiguration)}
	 * @param listener */
	public void initialize (ApplicationListener listener) {
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(listener, config);
	}

	/** This method has to be called in the {@link AndroidLiveWallpaperService#onCreateApplication} method. It sets up all the
	 * things necessary to get input, render via OpenGL and so on. You can configure other aspects of the application with the rest
	 * of the fields in the {@link AndroidApplicationConfiguration} instance.
	 * 
	 * @param listener the {@link ApplicationListener} implementing the program logic
	 * @param config the {@link AndroidApplicationConfiguration}, defining various settings of the application (use accelerometer,
	 *           etc.). Do not change contents of this object after passing to this method! */
	public void initialize (ApplicationListener listener, AndroidApplicationConfiguration config) {
		if (DEBUG) Log.d(TAG, " > AndroidLiveWallpaperService - initialize()");

		app.initialize(listener, config);

		if (config.getTouchEventsForLiveWallpaper && Integer.parseInt(android.os.Build.VERSION.SDK) >= 7)
			linkedEngine.setTouchEventsEnabled(true);

		// onResume(); do not call it there
	}

	/** Getter for SurfaceHolder object, surface holder is required to restore gl context in GLSurfaceView */
	public SurfaceHolder getSurfaceHolder () {
		if (DEBUG) Log.d(TAG, " > AndroidLiveWallpaperService - getSurfaceHolder()");

		synchronized (sync) {
			if (linkedEngine == null)
				return null;
			else
				return linkedEngine.getSurfaceHolder();
		}
	}

	// engines live there

	/** Called when the last engine is ending its live, it can occur when: 1. service is dying 2. service is switching from one
	 * engine to another 3. [only my assumption] when wallpaper is not visible and system is going to restore some memory for
	 * foreground processing by disposing not used wallpaper engine We can't destroy app there, because: 1. in won't work - gl
	 * context is disposed right now and after app.onDestroy() app would stuck somewhere in gl thread synchronizing code 2. we
	 * don't know if service create more engines, app is shared between them and should stay initialized waiting for new engines */
	public void onDeepPauseApplication () {
		if (DEBUG) Log.d(TAG, " > AndroidLiveWallpaperService - onDeepPauseApplication()");

		// free native resources consuming runtime memory, note that it can cause some lag when resuming wallpaper
		if (app != null) {
			app.graphics.clearManagedCaches();
		}
	}

	/** Service is dying, and will not be used again. You have to finish execution off all living threads there or short after
	 * there, besides the new wallpaper service wouldn't be able to start. */
	@Override
	public void onDestroy () {
		if (DEBUG) Log.d(TAG, " > AndroidLiveWallpaperService - onDestroy() " + hashCode());
		Log.i(TAG, "service destroyed");

		super.onDestroy(); // can call engine.onSurfaceDestroyed, must be before bellow code:

		if (app != null) {
			app.onDestroy();

			app = null;
			view = null;
		}
	}

	@Override
	protected void finalize () throws Throwable {
		Log.i(TAG, "service finalized");
		super.finalize();
	}

	// end of lifecycle methods ////////////////////////////////////////////////////////

	public AndroidLiveWallpaper getLiveWallpaper () {
		return app;
	}

	public WindowManager getWindowManager () {
		return (WindowManager)getSystemService(Context.WINDOW_SERVICE);
	}

	/** Bridge between surface on which wallpaper is rendered and the wallpaper service. The problem is that there can be a group of
	 * Engines at one time and we must share libGDX application between them.
	 * 
	 * @author libGDX team and Jaroslaw Wisniewski <j.wisniewski@appsisle.com> */
	public class AndroidWallpaperEngine extends Engine {

		protected boolean engineIsVisible = false;

		// destination format of surface when this engine is active (updated in onSurfaceChanged)
		protected int engineFormat;
		protected int engineWidth;
		protected int engineHeight;

		// lifecycle methods - the order of calling (flow) is maintained /////////////////

		public AndroidWallpaperEngine () {
			if (DEBUG) Log.d(TAG, " > AndroidWallpaperEngine() " + hashCode());
		}

		@Override
		public void onCreate (final SurfaceHolder surfaceHolder) {
			if (DEBUG)
				Log.d(TAG, " > AndroidWallpaperEngine - onCreate() " + hashCode() + " running: " + engines + ", linked: "
					+ (linkedEngine == this) + ", thread: " + Thread.currentThread().toString());
			super.onCreate(surfaceHolder);
		}

		/** Called before surface holder callbacks (ex for GLSurfaceView)! This is called immediately after the surface is first
		 * created. Implementations of this should start up whatever rendering code they desire. Note that only one thread can ever
		 * draw into a Surface, so you should not draw into the Surface here if your normal rendering will be in another thread. */
		@Override
		public void onSurfaceCreated (final SurfaceHolder holder) {
			engines++;
			setLinkedEngine(this);

			if (DEBUG)
				Log.d(TAG, " > AndroidWallpaperEngine - onSurfaceCreated() " + hashCode() + ", running: " + engines + ", linked: "
					+ (linkedEngine == this));
			Log.i(TAG, "engine surface created");

			super.onSurfaceCreated(holder);

			if (engines == 1) {
				// safeguard: recover attributes that could suffered by unexpected surfaceDestroy event
				visibleEngines = 0;
			}

			if (engines == 1 && app == null) {
				viewFormat = 0; // must be initialized with zeroes
				viewWidth = 0;
				viewHeight = 0;

				app = new AndroidLiveWallpaper(AndroidLiveWallpaperService.this);

				onCreateApplication();
				if (app.graphics == null)
					throw new Error(
						"You must override 'AndroidLiveWallpaperService.onCreateApplication' method and call 'initialize' from its body.");
			}

			view = (SurfaceHolder.Callback)app.graphics.view;
			this.getSurfaceHolder().removeCallback(view); // we are going to call this events manually

			// inherit format from shared surface view
			engineFormat = viewFormat;
			engineWidth = viewWidth;
			engineHeight = viewHeight;

			if (engines == 1) {
				view.surfaceCreated(holder);
			} else {
				// this combination of methods is described in AndroidWallpaperEngine.onResume
				view.surfaceDestroyed(holder);
				notifySurfaceChanged(engineFormat, engineWidth, engineHeight, false);
				view.surfaceCreated(holder);
			}

			notifyPreviewState();
			notifyOffsetsChanged();
			if (!Gdx.graphics.isContinuousRendering()) {
				Gdx.graphics.requestRendering();
			}
		}

		/** This is called immediately after any structural changes (format or size) have been made to the surface. You should at
		 * this point update the imagery in the surface. This method is always called at least once, after
		 * surfaceCreated(SurfaceHolder). */
		@Override
		public void onSurfaceChanged (final SurfaceHolder holder, final int format, final int width, final int height) {
			if (DEBUG)
				Log.d(TAG, " > AndroidWallpaperEngine - onSurfaceChanged() isPreview: " + isPreview() + ", " + hashCode()
					+ ", running: " + engines + ", linked: " + (linkedEngine == this) + ", sufcace valid: "
					+ getSurfaceHolder().getSurface().isValid());
			Log.i(TAG, "engine surface changed");

			super.onSurfaceChanged(holder, format, width, height);

			notifySurfaceChanged(format, width, height, true);

			// it shouldn't be required there (as I understand android.service.wallpaper.WallpaperService impl)
			// notifyPreviewState();
		}

		/** Notifies shared GLSurfaceView about changed surface format.
		 * @param format
		 * @param width
		 * @param height
		 * @param forceUpdate if false, surface view will be notified only if currently contains expired information */
		private void notifySurfaceChanged (final int format, final int width, final int height, boolean forceUpdate) {
			if (!forceUpdate && format == viewFormat && width == viewWidth && height == viewHeight) {
				// skip if didn't changed
				if (DEBUG) Log.d(TAG, " > surface is current, skipping surfaceChanged event");
			} else {
				// update engine desired surface format
				engineFormat = format;
				engineWidth = width;
				engineHeight = height;

				// update surface view if engine is linked with it already
				if (linkedEngine == this) {
					viewFormat = engineFormat;
					viewWidth = engineWidth;
					viewHeight = engineHeight;
					view.surfaceChanged(this.getSurfaceHolder(), viewFormat, viewWidth, viewHeight);
				} else {
					if (DEBUG) Log.d(TAG, " > engine is not active, skipping surfaceChanged event");
				}
			}
		}

		/** Called to inform you of the wallpaper becoming visible or hidden. It is very important that a wallpaper only use CPU
		 * while it is visible.. */
		@Override
		public void onVisibilityChanged (final boolean visible) {
			boolean reportedVisible = isVisible();

			if (DEBUG)
				Log.d(TAG, " > AndroidWallpaperEngine - onVisibilityChanged(paramVisible: " + visible + " reportedVisible: "
					+ reportedVisible + ") " + hashCode() + ", sufcace valid: " + getSurfaceHolder().getSurface().isValid());
			super.onVisibilityChanged(visible);

			// Android WallpaperService sends fake visibility changed events to force some buggy live wallpapers to shut down after
// onSurfaceChanged when they aren't visible, it can cause problems in current implementation and it is not necessary
			if (reportedVisible == false && visible == true) {
				if (DEBUG) Log.d(TAG, " > fake visibilityChanged event! Android WallpaperService likes do that!");
				return;
			}

			notifyVisibilityChanged(visible);
		}

		private void notifyVisibilityChanged (final boolean visible) {
			if (this.engineIsVisible != visible) {
				this.engineIsVisible = visible;

				if (this.engineIsVisible)
					onResume();
				else
					onPause();
			} else {
				if (DEBUG) Log.d(TAG, " > visible state is current, skipping visibilityChanged event!");
			}
		}

		public void onResume () {
			visibleEngines++;
			if (DEBUG)
				Log.d(TAG, " > AndroidWallpaperEngine - onResume() " + hashCode() + ", running: " + engines + ", linked: "
					+ (linkedEngine == this) + ", visible: " + visibleEngines);
			Log.i(TAG, "engine resumed");

			if (linkedEngine != null) {
				if (linkedEngine != this) {
					setLinkedEngine(this);

					// disconnect surface view from previous window
					view.surfaceDestroyed(this.getSurfaceHolder()); // force gl surface reload, new instance will be created on current
// surface holder

					// resize surface to match window associated with current engine
					notifySurfaceChanged(engineFormat, engineWidth, engineHeight, false);

					// connect surface view to current engine
					view.surfaceCreated(this.getSurfaceHolder());
				} else {
					// update if surface changed when engine wasn't active
					notifySurfaceChanged(engineFormat, engineWidth, engineHeight, false);
				}

				if (visibleEngines == 1) app.onResume();

				notifyPreviewState();
				notifyOffsetsChanged();
				if (!Gdx.graphics.isContinuousRendering()) {
					Gdx.graphics.requestRendering();
				}
			}
		}

		public void onPause () {
			visibleEngines--;
			if (DEBUG)
				Log.d(TAG, " > AndroidWallpaperEngine - onPause() " + hashCode() + ", running: " + engines + ", linked: "
					+ (linkedEngine == this) + ", visible: " + visibleEngines);
			Log.i(TAG, "engine paused");

			// this shouldn't never happen, but if it will.. live wallpaper will not be stopped when device will pause and lwp will
// drain battery.. shortly!
			if (visibleEngines >= engines) {
				Log.e(AndroidLiveWallpaperService.TAG, "wallpaper lifecycle error, counted too many visible engines! repairing..");
				visibleEngines = Math.max(engines - 1, 0);
			}

			if (linkedEngine != null) {
				if (visibleEngines == 0) app.onPause();
			}

			if (DEBUG) Log.d(TAG, " > AndroidWallpaperEngine - onPause() done!");
		}

		/** Called after surface holder callbacks (ex for GLSurfaceView)! This is called immediately before a surface is being
		 * destroyed. After returning from this call, you should no longer try to access this surface. If you have a rendering
		 * thread that directly accesses the surface, you must ensure that thread is no longer touching the Surface before returning
		 * from this function.
		 * 
		 * Attention! In some cases GL context may be shutdown right now! and SurfaceHolder.Surface.isVaild = false */
		@Override
		public void onSurfaceDestroyed (final SurfaceHolder holder) {
			engines--;
			if (DEBUG)
				Log.d(TAG, " > AndroidWallpaperEngine - onSurfaceDestroyed() " + hashCode() + ", running: " + engines + " ,linked: "
					+ (linkedEngine == this) + ", isVisible: " + engineIsVisible);
			Log.i(TAG, "engine surface destroyed");

			// application can be in resumed state at this moment if app surface had been lost just after it was created (wallpaper
// selected too fast from preview mode etc)
			// it is too late probably - calling on pause causes deadlock
			// notifyVisibilityChanged(false);

			// it is too late to call app.onDispose, just free native resources
			if (engines == 0) onDeepPauseApplication();

			// free surface if it belongs to this engine and if it was initialized
			if (linkedEngine == this && view != null) view.surfaceDestroyed(holder);

			// waitingSurfaceChangedEvent = null;
			engineFormat = 0;
			engineWidth = 0;
			engineHeight = 0;

			// safeguard for other engine callbacks
			if (engines == 0) linkedEngine = null;

			super.onSurfaceDestroyed(holder);
		}

		@Override
		public void onDestroy () {
			super.onDestroy();
		}

		// end of lifecycle methods ////////////////////////////////////////////////////////

		// input
		
		boolean iconDropConsumed = true;
		int xIconDrop, yIconDrop;

		@Override
		public Bundle onCommand (final String pAction, final int pX, final int pY, final int pZ, final Bundle pExtras,
			final boolean pResultRequested) {
			if (DEBUG)
				Log.d(TAG, " > AndroidWallpaperEngine - onCommand(" + pAction + " " + pX + " " + pY + " " + pZ + " " + pExtras + " "
					+ pResultRequested + ")" + ", linked: " + (linkedEngine == this));
			
			if (pAction.equals("android.home.drop")){
				iconDropConsumed = false;
				xIconDrop = pX;
				yIconDrop = pY;
				notifyIconDropped();
			}

			return super.onCommand(pAction, pX, pY, pZ, pExtras, pResultRequested);
		}
		
		protected void notifyIconDropped () {
			if (linkedEngine == this && app.listener instanceof AndroidWallpaperListener) {
				if (!iconDropConsumed) { // same type of synchronization as in notifyOffsetsChanged()
					iconDropConsumed = true;

					app.postRunnable(new Runnable() {
						@Override
						public void run () {
							boolean isCurrent = false;
							synchronized (sync) {
								isCurrent = (linkedEngine == AndroidWallpaperEngine.this);
							}
							if (isCurrent)
								((AndroidWallpaperListener)app.listener).iconDropped(xIconDrop, yIconDrop);
						}
					});
				}
			}
		}

		@Override
		public void onTouchEvent (MotionEvent event) {
			if (linkedEngine == this) {
				app.input.onTouch(null, event);
			}
		}

		// offsets from last onOffsetsChanged
		boolean offsetsConsumed = true;
		float xOffset = 0.0f;
		float yOffset = 0.0f;
		float xOffsetStep = 0.0f;
		float yOffsetStep = 0.0f;
		int xPixelOffset = 0;
		int yPixelOffset = 0;

		@Override
		public void onOffsetsChanged (final float xOffset, final float yOffset, final float xOffsetStep, final float yOffsetStep,
			final int xPixelOffset, final int yPixelOffset) {

			// it spawns too frequent on some devices - its annoying!
			// if (DEBUG)
			// Log.d(TAG, " > AndroidWallpaperEngine - onOffsetChanged(" + xOffset + " " + yOffset + " " + xOffsetStep + " "
			// + yOffsetStep + " " + xPixelOffset + " " + yPixelOffset + ") " + hashCode() + ", linkedApp: " + (linkedApp != null));

			this.offsetsConsumed = false;
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.xOffsetStep = xOffsetStep;
			this.yOffsetStep = yOffsetStep;
			this.xPixelOffset = xPixelOffset;
			this.yPixelOffset = yPixelOffset;

			// can fail if linkedApp == null, so we repeat it in Engine.onResume
			notifyOffsetsChanged();
			if (!Gdx.graphics.isContinuousRendering()) {
				Gdx.graphics.requestRendering();
			}

			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
		}

		protected void notifyOffsetsChanged () {
			if (linkedEngine == this && app.listener instanceof AndroidWallpaperListener) {
				if (!offsetsConsumed) { // no need for more sophisticated synchronization - offsetsChanged can be called multiple
// times and with various patterns on various devices - user application must be prepared for that
					offsetsConsumed = true;

					app.postRunnable(new Runnable() {
						@Override
						public void run () {
							boolean isCurrent = false;
							synchronized (sync) {
								isCurrent = (linkedEngine == AndroidWallpaperEngine.this); // without this app can crash when fast
// switching between engines (tested!)
							}
							if (isCurrent)
								((AndroidWallpaperListener)app.listener).offsetChange(xOffset, yOffset, xOffsetStep, yOffsetStep,
									xPixelOffset, yPixelOffset);
						}
					});
				}
			}
		}

		protected void notifyPreviewState () {
			// notify preview state to app listener
			if (linkedEngine == this && app.listener instanceof AndroidWallpaperListener) {
				final boolean currentPreviewState = linkedEngine.isPreview();
				app.postRunnable(new Runnable() {
					@Override
					public void run () {
						boolean shouldNotify = false;
						synchronized (sync) {
							if (!isPreviewNotified || notifiedPreviewState != currentPreviewState) {
								notifiedPreviewState = currentPreviewState;
								isPreviewNotified = true;
								shouldNotify = true;
							}
						}

						if (shouldNotify) {
							AndroidLiveWallpaper currentApp = app; // without this app can crash when fast switching between engines
// (tested!)
							if (currentApp != null)
								((AndroidWallpaperListener)currentApp.listener).previewStateChange(currentPreviewState);
						}
					}
				});
			}
		}
		
	}
}
