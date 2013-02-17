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

import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.android.AndroidWallpaperListener;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.backends.android.surfaceview.GLBaseSurfaceViewLW;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewCupcake;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.utils.GdxNativesLoader;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.provider.LiveFolders;
import android.service.wallpaper.WallpaperService;
import android.service.wallpaper.WallpaperService.Engine;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;


/** 
 * An implementation of the {@link Application} interface dedicated for android live wallpapers.
 * 
 * Derive from this class. In the {@link AndroidLiveWallpaperService#onCreateApplication} method call the {@link AndroidLiveWallpaperService#initialize(ApplicationListener, boolean)} 
 * method specifying the configuration for the GLSurfaceView. You can also use {@link AndroidWallpaperListener} 
 * along with {@link ApplicationListener} to respond for wallpaper specific events in your app listener:
 * 
 * MyAppListener implements ApplicationListener, AndroidWallpaperListener
 * 
 * Notice!
 * You have to kill all not daemon threads you created in {@link ApplicationListener#pause()} method.
 * {@link ApplicationListener#dispose()} is never called!
 * If you leave live non daemon threads, wallpaper service wouldn't be able to close, 
 * this will cause problems with wallpaper lifecycle.
 * 
 * @author libGDX team, enhanced by Jaroslaw Wisniewski <j.wisniewski@appsisle.com>
 */
public abstract class AndroidLiveWallpaperService extends WallpaperService {
	static {
		GdxNativesLoader.load();
	}
	
	static final String TAG = "AndroidLiveWallpaperService";
	static boolean DEBUG	= false;	// TODO remember to disable this

	
	// instance of libGDX Application, acts as singleton - one instance per application (per WallpaperService)
	// at specified time linked to one of available engines
	protected volatile AndroidLiveWallpaper app = null;	// can be accessed from GL render thread
	protected SurfaceHolder.Callback view = null;
	
	// app is initialized when engines == 1 first time, app is destroyed never destroyed! It should be in WallpaperService.onDestroy or Engine.onDestroy, but at this moment gl context is lost, libgdx needs gl context to process 'destroy' method
	protected int engines = 0;
	protected int visibleEngines = 0;
	
	// engine currently associated with app instance, also this engine serves surface handler for GLSurfaceView
	protected volatile AndroidWallpaperEngine linkedEngine = null;		// can be accessed from GL render thread by getSurfaceHolder
	
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
	//volatile ReentrantLock lock = new ReentrantLock();
	
	
	// lifecycle methods - the order of calling (flow) is maintained ///////////////
	
	public AndroidLiveWallpaperService () {
		super();
	}

	
	/**
	 * Service is starting, libGDX application is shutdown now
	 */
	@Override
	public void onCreate () {
		if (DEBUG) Log.d(TAG, " > AndroidLiveWallpaperService - onCreate() " + hashCode());
		Log.i(TAG, "service created");

		super.onCreate();		
	}
	
	
	/**
	 * One of wallpaper engines is starting. 
	 * Do not override this method, service manages them internally.
	 */
	@Override
	public Engine onCreateEngine () {
		if (DEBUG) Log.d(TAG, " > AndroidLiveWallpaperService - onCreateEngine()");
		Log.i(TAG, "engine created");
		
		return new AndroidWallpaperEngine();
	}
	
	
	/**
	 * libGDX application is starting, it occurs after first wallpaper engine had started.
	 * Override this method an invoke {@link AndroidLiveWallpaperService#initialize(ApplicationListener, AndroidApplicationConfiguration)} from there.
	 */
	public void onCreateApplication () {
		if (DEBUG) Log.d(TAG, " > AndroidLiveWallpaperService - onCreateApplication()");
	}
	
	
	/** 
	 * Look at {@link AndroidLiveWallpaperService#initialize(ApplicationListener, AndroidApplicationConfiguration)}}
	 * @param listener
	 * @param useGL2IfAvailable
	 */
	public void initialize (ApplicationListener listener, boolean useGL2IfAvailable) {
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useGL20 = useGL2IfAvailable;
		initialize(listener, config);
	}

	/** 
	 * This method has to be called in the {@link AndroidLiveWallpaperService#onCreateApplication} method. It sets up all the things necessary to get
	 * input, render via OpenGL and so on. If config.useGL20 is set the AndroidApplication will try to create an OpenGL ES 2.0
	 * context which can then be used via {@link Graphics#getGL20()}. The {@link GL10} and {@link GL11} interfaces should not be
	 * used when OpenGL ES 2.0 is enabled. To query whether enabling OpenGL ES 2.0 was successful use the
	 * {@link Graphics#isGL20Available()} method. You can configure other aspects of the application with the rest of the fields in
	 * the {@link AndroidApplicationConfiguration} instance.
	 * 
	 * @param listener the {@link ApplicationListener} implementing the program logic
	 * @param config the {@link AndroidApplicationConfiguration}, defining various settings of the application (use accelerometer,
	 *           etc.). Do not change contents of this object after passing to this method!
	 */
	public void initialize (ApplicationListener listener, AndroidApplicationConfiguration config) {
		if (DEBUG) Log.d(TAG, " > AndroidLiveWallpaperService - initialize()");
		
		app.initialize(listener, config);
		//view = ((AndroidGraphicsLiveWallpaper)app.getGraphics()).getView();
		
		if (config.getTouchEventsForLiveWallpaper && Integer.parseInt(android.os.Build.VERSION.SDK) < 9)
			linkedEngine.setTouchEventsEnabled(true);
		
		//onResume(); do not call it there
	}
	
	
	/**
	 * Getter for SurfaceHolder object, surface holder is required to restore gl context in GLSurfaceView
	 */
	public SurfaceHolder getSurfaceHolder() {
		if (DEBUG) Log.d(TAG, " > AndroidLiveWallpaperService - getSurfaceHolder()");
		
		synchronized (sync) {
			if (linkedEngine == null)
				return null;
			else 
				return linkedEngine.getSurfaceHolder();
		}
	}
	
	
	// engines live there
	
	
	/**
	 * Called when the last engine is ending its live, it can occurs when:
	 * 1. service is dying
	 * 2. service is switching from one engine to another
	 * 3. [only my assumption] when wallpaper is not visible and system is going to restore some memory 
	 * 	for foreground process by disposing not used wallpaper engine
	 * We can't destroy app there, because:
	 * 1. in won't work - gl context is disposed right now and after app.onDestroy() app would stuck somewhere in gl thread synchronizing code
	 * 2. we don't know if service create more engines, app is shared between them and should stay initialized waiting for new engines
	 */
	public void onDeepPauseApplication () {
		if (DEBUG) Log.d(TAG, " > AndroidLiveWallpaperService - onDeepPauseApplication()");
		
		// free native resources consuming runtime memory, note that it can cause some lag when resuming wallpaper
		if (app != null) {
			app.graphics.clearManagedCaches();
		}
	}
	
	
	/**
	 * Service is dying, and will not be used again.
	 * You have to finish execution off all living threads there or short after there, 
	 * besides the new wallpaper service wouldn't be able to start.
	 */
	@Override
	public void onDestroy () {
		if (DEBUG) Log.d(TAG, " > AndroidLiveWallpaperService - onDestroy() " + hashCode());
		Log.i(TAG, "service destroyed");
		
		super.onDestroy();	// can call engine.onSurfaceDestroyed, must be before bellow code:
		
		if (app != null)
		{
			// it is too late to call app.onDestroy (gl context is dead)
			//app.onDestroy();
			
			// so we do what we can..
			if (app.graphics != null)
			{
				// not necessary - already called in onDeepPauseApplication
				// app.graphics.clearManagedCaches();
				
				// kill the GLThread managed by GLSurfaceView (only for GLSurfaceView because GLSurffaceViewCupcake stops thread in onPause events - and it is not so easy and safe for GLSurfaceView)
				if (app.graphics.view != null && (app.graphics.view instanceof GLSurfaceView))
				{
					GLSurfaceView glSurfaceView = (GLSurfaceView)app.graphics.view;
					try {
						Method method = null;
						for (Method m : glSurfaceView.getClass().getMethods()) 
						{
							if (m.getName().equals("onDestroy"))	// implemented in AndroidGraphicsLiveWallpaper, redirects to onDetachedFromWindow - which stops GLThread by calling mGLThread.requestExitAndWait()
							{
								method = m;
								break;
							}
						}
						
						if (method != null)
						{
							method.invoke(glSurfaceView);
							if (DEBUG) Log.d(TAG, " > AndroidLiveWallpaperService - onDestroy() stopped GLThread managed by GLSurfaceView");
						}
						else
							throw new Exception("method not found!");
					} 
					catch (Throwable t)
					{
						// error while scheduling exit of GLThread, GLThread will remain live and wallpaper service wouldn't be able to shutdown completely
						Log.e(TAG, "failed to destroy GLSurfaceView's thread! GLSurfaceView.onDetachedFromWindow impl changed since API lvl 16!");
						t.printStackTrace();
					}
				}
			}
			
			if (app.audio != null)
			{
				// dispose audio and free native resources, mandatory sinde app.onDispose is never called in live wallpaper
				app.audio.dispose();
			}
			
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
	
	
	
	public AndroidLiveWallpaper getLiveWallpaper() {
		return app;
	}
	
	
	public WindowManager getWindowManager() {
		return (WindowManager)getSystemService(Context.WINDOW_SERVICE);
	}
	
	
	/**
	 * Bridge between surface on which wallpaper is rendered and the wallpaper service. 
	 * The problem is that there can be a group of Engines at one time and we must share libGDX application between them.
	 * 
	 * @author libGDX team and Jaroslaw Wisniewski <j.wisniewski@appsisle.com>
	 *
	 */
	public class AndroidWallpaperEngine extends Engine {

		protected boolean isVisible = false;
		protected Runnable waitingSurfaceChangedEvent = null;
		
		
		// lifecycle methods - the order of calling (flow) is maintained /////////////////
		
		public AndroidWallpaperEngine () {
			if (DEBUG) Log.d(TAG, " > AndroidWallpaperEngine() " + hashCode());
		}

		
		@Override
		public void onCreate (final SurfaceHolder surfaceHolder) {
			if (DEBUG) Log.d(TAG, " > AndroidWallpaperEngine - onCreate() " + hashCode() + " running: " + engines + ", linked: " + (linkedEngine == this) + ", thread: " + Thread.currentThread().toString());
			super.onCreate(surfaceHolder);
		}
		
		
		/**
		 * Called before surface holder callbacks (ex for GLSurfaceView)!
		 * This is called immediately after the surface is first created. Implementations of this should start 
		 * up whatever rendering code they desire. Note that only one thread can ever draw into a Surface, 
		 * so you should not draw into the Surface here if your normal rendering will be in another thread.
		 */
		@Override
		public void onSurfaceCreated (final SurfaceHolder holder) {
			engines ++;
			setLinkedEngine(this);
			
			if (DEBUG) Log.d(TAG, " > AndroidWallpaperEngine - onSurfaceCreated() " + hashCode() + ", running: " + engines + ", linked: " + (linkedEngine == this));
			Log.i(TAG, "engine surface created");
			
			super.onSurfaceCreated(holder);
	
			if (engines == 1) {
				// safeguard: recover attributes that could suffered by unexpected surfaceDestroy event
				visibleEngines = 0;
			}
			
			if (engines == 1 && app == null) {
				app = new AndroidLiveWallpaper(AndroidLiveWallpaperService.this);
				
				onCreateApplication();
				if (app.graphics == null)
					throw new Error("You must override 'AndroidLiveWallpaperService.onCreateApplication' method and call 'initialize' from its body.");
				
				view = (SurfaceHolder.Callback)app.graphics.view;
				this.getSurfaceHolder().removeCallback(view);	// we are going to call this events manually
			}
			
			if (engines == 1)
				view.surfaceCreated(holder);
			else {
				view.surfaceDestroyed(holder);	// force gl surface reload, new instance will be created on current surface holder
				view.surfaceCreated(holder);
			}
			
			if (waitingSurfaceChangedEvent != null)
				waitingSurfaceChangedEvent.run();
			
			notifyPreviewState();
			notifyOffsetsChanged();
		}
		
		
		/**
		 * This is called immediately after any structural changes (format or size) have been made to the surface. 
		 * You should at this point update the imagery in the surface. This method is always called at least once, 
		 * after surfaceCreated(SurfaceHolder).
		 */
		@Override
		public void onSurfaceChanged (final SurfaceHolder holder, final int format, final int width, final int height) {
			if (DEBUG) Log.d(TAG, " > AndroidWallpaperEngine - onSurfaceChanged() isPreview: " + isPreview() + ", " + hashCode() + ", running: " + engines + ", linked: " + (linkedEngine == this) + ", sufcace valid: " + getSurfaceHolder().getSurface().isValid());
			Log.i(TAG, "engine surface changed");
			
			super.onSurfaceChanged(holder, format, width, height);
			
			if (linkedEngine == this)
				view.surfaceChanged(holder, format, width, height);
			else {
				// save new surface holder format and call surfaceChanged when this engine will be linked again
				waitingSurfaceChangedEvent = new Runnable() {
					@Override
					public void run () {
						if (DEBUG) Log.d(TAG, " > AndroidWallpaperEngine - onSurfaceChanged() calling delayed..");
						onSurfaceChanged(holder, format, width, height);
					}
				};
			}
			
			// it shouldn't be required there (as I studied original Android WallpaperService impl)
			//notifyPreviewState();
		}


		/**
		 * Called to inform you of the wallpaper becoming visible or hidden. It is very important that 
		 * a wallpaper only use CPU while it is visible..
		 */
		@Override
		public void onVisibilityChanged (final boolean visible) {
			if (DEBUG) Log.d(TAG, " > AndroidWallpaperEngine - onVisibilityChanged(" + visible + ") " + hashCode()  + ", sufcace valid: " + getSurfaceHolder().getSurface().isValid());
			super.onVisibilityChanged(visible);

			// Android WallpaperService sends fake visibility changed events sometimes to force some buggy live wallpapers to shut down when they aren't visible, it can cause problems in current implementation and it is not nessesary - wallpaper is pausing correctly
			if (visible != isVisible())
			{
				if (DEBUG) Log.d(TAG, " > AndroidWallpaperEngine - onVisibilityChanged() fake visibilityChanged event! Android WallpaperService likes do that!");
				return;
			}

			notifyVisibilityChanged(visible);
		}

		
		private void notifyVisibilityChanged(final boolean visible)
		{
			if (this.isVisible != visible) {
				this.isVisible = visible;
				
				if (this.isVisible)
					onResume();
				else
					onPause();
			}
		}
		
		
		public void onResume () {
			visibleEngines ++;
			if (DEBUG) Log.d(TAG, " > AndroidWallpaperEngine - onResume() " + hashCode() + ", running: " + engines + ", linked: " + (linkedEngine == this) + ", visible: " + visibleEngines);
			Log.i(TAG, "engine resumed");
			
			if (linkedEngine != null) {
				if (linkedEngine != this) {
					setLinkedEngine(this);
					view.surfaceDestroyed(getSurfaceHolder());	// force gl surface reload, new instance will be created on current surface holder
					view.surfaceCreated(getSurfaceHolder());
				}
				
				if (visibleEngines == 1)
					app.onResume();
	
				if (waitingSurfaceChangedEvent != null)
					waitingSurfaceChangedEvent.run();
	
				notifyPreviewState();
				notifyOffsetsChanged();
			}
		}
		
		
		public void onPause () {
			visibleEngines --;
			if (DEBUG) Log.d(TAG, " > AndroidWallpaperEngine - onPause() " + hashCode() + ", running: " + engines + ", linked: " + (linkedEngine == this) + ", visible: " + visibleEngines);
			Log.i(TAG, "engine pausde");
			
			if (linkedEngine != null) {
				if (visibleEngines == 0)
					app.onPause();
	
				//if (linkedEngine == this)
				//	app.graphics.view.surfaceDestroyed(getSurfaceHolder());
			}
			
			if (DEBUG) Log.d(TAG, " > AndroidWallpaperEngine - onPause() done!");
		}

		
		/**
		 * Called after surface holder callbacks (ex for GLSurfaceView)!
		 * This is called immediately before a surface is being destroyed. After returning from this call, 
		 * you should no longer try to access this surface. If you have a rendering thread that directly 
		 * accesses the surface, you must ensure that thread is no longer touching the Surface before 
		 * returning from this function.
		 * 
		 * Attention!
		 * In some cases GL context may be shutdown right now! and SurfaceHolder.Surface.isVaild = false
		 */
		@Override
		public void onSurfaceDestroyed (final SurfaceHolder holder) {
			engines --;
			if (DEBUG) Log.d(TAG, " > AndroidWallpaperEngine - onSurfaceDestroyed() " + hashCode()  + ", running: " + engines + " ,linked: " + (linkedEngine == this) + ", isVisible: " + isVisible);
			Log.i(TAG, "engine surface destroyed");
			
			// application can be in resumed state at this moment if app surface had been lost just after it was created (wallpaper selected too fast from preview mode etc)
			// it is too late probably - calling on pause causes deadlock
			//notifyVisibilityChanged(false);
			
			// it is too late to call app.onDispose, just free native resources
			if (engines == 0)
				onDeepPauseApplication();
			
			// free surface if it belongs to this engine and if it was initialized
			if (linkedEngine == this && view != null)
				view.surfaceDestroyed(holder);
			//else
			//	save surface destroyed state for further processing
		
			waitingSurfaceChangedEvent = null;
			
			// safeguard for other engine callbacks
			if (engines == 0)
				linkedEngine = null;
			
			super.onSurfaceDestroyed(holder);
		}
		
		
		@Override
		public void onDestroy () {
			super.onDestroy();
		}

		// end of lifecycle methods ////////////////////////////////////////////////////////
		
		
		// input
		
		@Override
		public Bundle onCommand (final String pAction, final int pX, final int pY, final int pZ, final Bundle pExtras,
			final boolean pResultRequested) {
			if (DEBUG) Log.d(TAG, " > AndroidWallpaperEngine - onCommand(" + pAction + " " + pX + " " + pY + " " + pZ + " " + pExtras + " " + pResultRequested + ")" + ", linked: " + (linkedEngine == this));

			// FIXME
			/*if (linkedEngine == this) {
				if (pAction.equals(WallpaperManager.COMMAND_TAP)) {
						linkedApp.input.onTap(pX, pY);
				} else if (pAction.equals(WallpaperManager.COMMAND_DROP)) {
						linkedApp.input.onDrop(pX, pY);
				}
			}*/
			
			return super.onCommand(pAction, pX, pY, pZ, pExtras, pResultRequested);
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
		public void onOffsetsChanged (final float xOffset, final float yOffset, final float xOffsetStep, final float yOffsetStep, final int xPixelOffset,
			final int yPixelOffset) {

			// it spawns too frequent on some devices - its annoying!
			//if (DEBUG)
			//	Log.d(TAG, " > AndroidWallpaperEngine - onOffsetChanged(" + xOffset + " " + yOffset + " " + xOffsetStep + " "
			//		+ yOffsetStep + " " + xPixelOffset + " " + yPixelOffset + ") " + hashCode() + ", linkedApp: " + (linkedApp != null));

			this.offsetsConsumed = false;
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.xOffsetStep = xOffsetStep;
			this.yOffsetStep = yOffsetStep;
			this.xPixelOffset = xPixelOffset;
			this.yPixelOffset = yPixelOffset;
			
			// can fail if linkedApp == null, so we repeat it in Engine.onResume
			notifyOffsetsChanged();
			
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
		}
		
		
		protected void notifyOffsetsChanged()
		{
			if (linkedEngine == this && app.listener instanceof AndroidWallpaperListener) {
				if (!offsetsConsumed) {		// no need for more sophisticated synchronization - offsetsChanged can be called multiple times and with various patterns on various devices - user application must be prepared for that
					offsetsConsumed = true;
					
					app.postRunnable(new Runnable() {
						@Override
						public void run () {
							boolean isCurrent = false;
							synchronized (sync) {
								isCurrent = (linkedEngine == AndroidWallpaperEngine.this);		// without this app can crash when fast switching between engines (tested!)
							}
							if (isCurrent)
								((AndroidWallpaperListener)app.listener).offsetChange(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);	
						}
					});
				}
			}
		}
		
		
		protected void notifyPreviewState()
		{
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
						
						if (shouldNotify)
						{
							AndroidLiveWallpaper currentApp = app;		// without this app can crash when fast switching between engines (tested!)
							if (currentApp != null)
								((AndroidWallpaperListener)currentApp.listener).previewStateChange(currentPreviewState);
						}
					}
				});
			}
		}
	}	
}
