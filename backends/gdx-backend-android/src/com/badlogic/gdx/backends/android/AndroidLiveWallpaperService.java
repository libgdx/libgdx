/*
 * Copyright 2010 Elijah Cornell
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

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.android.surfaceview.GLBaseSurfaceViewLW;

import android.app.WallpaperManager;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.service.wallpaper.WallpaperService.Engine;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public abstract class AndroidLiveWallpaperService extends WallpaperService {

	private static final String TAG = "AndroidLiveWallpaperService";
	private static final String ENGINE_TAG = "AndroidWallpaperEngine";

	public static boolean DEBUG = false;
	protected static volatile int runningEngines = 0;

	public static interface PreviewListener {

		void setIsPreview (boolean preview);

	}

	public AndroidLiveWallpaperService () {
		super();
	}

	@Override
	public void onCreate () {
		if (DEBUG) Log.d(TAG, " - onCreate()");
		super.onCreate();
	}

	@Override
	public Engine onCreateEngine () {
		if (DEBUG) Log.d(TAG, " - onCreateEngine()");
		ApplicationListener listener = createListener();
		Engine engine = new AndroidWallpaperEngine(listener, createConfig());
		if (listener instanceof PreviewListener) ((PreviewListener)listener).setIsPreview(engine.isPreview());
		return engine;
	}

	/** @return a new {@link ApplicationListener} that implements the live wallpaper */
	public abstract ApplicationListener createListener ();

	/** @return a new {@link AndroidApplicationConfiguration} that specifies the config to be used for the live wall paper */
	public abstract AndroidApplicationConfiguration createConfig ();

	/** Called when the live wallpaper's offset changed. This method will be called on the rendering thread.
	 * @param xOffset
	 * @param yOffset
	 * @param xOffsetStep
	 * @param yOffsetStep
	 * @param xPixelOffset
	 * @param yPixelOffset */
	public abstract void offsetChange (ApplicationListener listener, float xOffset, float yOffset, float xOffsetStep,
		float yOffsetStep, int xPixelOffset, int yPixelOffset);

	@Override
	public void onDestroy () {
		if (DEBUG) Log.d(TAG, " - onDestroy()");
		super.onDestroy();
	}

	public class AndroidWallpaperEngine extends Engine {
		protected final AndroidLiveWallpaper app;
		protected final ApplicationListener listener;
		protected GLBaseSurfaceViewLW view;

		public AndroidWallpaperEngine (ApplicationListener listener, AndroidApplicationConfiguration config) {
			if (AndroidLiveWallpaperService.DEBUG) Log.d(ENGINE_TAG, hashCode() + " - AndroidWallpaperEngine()");
			this.app = new AndroidLiveWallpaper(AndroidLiveWallpaperService.this, this);
			this.app.initialize(listener, config);
			this.listener = listener;
			this.view = ((AndroidGraphicsLiveWallpaper)app.getGraphics()).getView();

			if (config.getTouchEventsForLiveWallpaper && Integer.parseInt(android.os.Build.VERSION.SDK) < 9)
				this.setTouchEventsEnabled(true);
		}

		@Override
		public Bundle onCommand (final String pAction, final int pX, final int pY, final int pZ, final Bundle pExtras,
			final boolean pResultRequested) {

			if (AndroidLiveWallpaperService.DEBUG)
				Log.d(ENGINE_TAG, hashCode() + " - onCommand(" + pAction + " " + pX + " " + pY + " " + pZ + " " + pExtras + " "
					+ pResultRequested + ")");

			// FIXME
			// if (pAction.equals(WallpaperManager.COMMAND_TAP)) {
			// app.input.onTap(pX, pY);
			// } else if (pAction.equals(WallpaperManager.COMMAND_DROP)) {
			// app.input.onDrop(pX, pY);
			// }
			return super.onCommand(pAction, pX, pY, pZ, pExtras, pResultRequested);
		}

		@Override
		public void onCreate (final SurfaceHolder surfaceHolder) {
			runningEngines++;
			if (AndroidLiveWallpaperService.DEBUG) Log.d(ENGINE_TAG, hashCode() + " - onCreate(), running: " + runningEngines);
			super.onCreate(surfaceHolder);

			// wallpaperListener.setIsPreview(this.isPreview());
		}

		@Override
		public void onDestroy () {
			runningEngines--;
			if (AndroidLiveWallpaperService.DEBUG) Log.d(ENGINE_TAG, hashCode() + " - onDestroy(), running: " + runningEngines);
			view.onDestroy();
			// as the graphics.onDrawFrame() is not being called anymore then the listener is never disposed(), so we
			// have to call it here, don't like the solution but graphics has no API to call the dispose() logic inside the
			// onDrawFrame().
			listener.dispose();
			app.onDestroy();
			super.onDestroy();
		}

		public void onPause () {
			if (AndroidLiveWallpaperService.DEBUG) Log.d(ENGINE_TAG, hashCode() + " - onPause(), running: " + runningEngines);
			app.onPause();
			view.onPause();
		}

		public void onResume () {
			if (AndroidLiveWallpaperService.DEBUG) Log.d(ENGINE_TAG, hashCode() + " - onResume(), running: " + runningEngines);
			app.onResume();
			view.onResume();
		}

		@Override
		public void onSurfaceChanged (final SurfaceHolder holder, final int format, final int width, final int height) {
			if (AndroidLiveWallpaperService.DEBUG)
				Log.d(ENGINE_TAG, hashCode() + " - onSurfaceChanged() " + isPreview() + ", running: " + runningEngines);
			super.onSurfaceChanged(holder, format, width, height);
		}

		@Override
		public void onSurfaceCreated (final SurfaceHolder holder) {
			if (AndroidLiveWallpaperService.DEBUG)
				Log.d(ENGINE_TAG, hashCode() + " - onSurfaceCreated(), running: " + runningEngines);
			super.onSurfaceCreated(holder);
		}

		@Override
		public void onSurfaceDestroyed (final SurfaceHolder holder) {
			if (AndroidLiveWallpaperService.DEBUG)
				Log.d(ENGINE_TAG, hashCode() + " - onSurfaceDestroyed(), running: " + runningEngines);
			super.onSurfaceDestroyed(holder);
		}

		@Override
		public void onVisibilityChanged (final boolean visible) {
			if (AndroidLiveWallpaperService.DEBUG) Log.d(ENGINE_TAG, hashCode() + " - onVisibilityChanged(" + visible + ")");
			if (visible) {
				onResume();
			} else {
				onPause();
			}

			super.onVisibilityChanged(visible);
		}

		@Override
		public void onTouchEvent (MotionEvent event) {
			app.input.onTouch(null, event);
		}

		@Override
		public void onOffsetsChanged (final float xOffset, final float yOffset, final float xOffsetStep, final float yOffsetStep,
			final int xPixelOffset, final int yPixelOffset) {

			// this log is really annoying.
			// if (AndroidLiveWallpaperService.DEBUG)
			// Log.d(AndroidLiveWallpaperService.this.TAG, hashCode() + " - onOffsetChanged(" + xOffset + " " + yOffset + " " + xOffsetStep +
			// " "
			// + yOffsetStep + " " + xPixelOffset + " " + yPixelOffset + ")");

			app.postRunnable(new Runnable() {
				@Override
				public void run () {
					AndroidLiveWallpaperService.this.offsetChange(listener, xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset,
						yPixelOffset);
				}
			});
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
		}
	}
}
