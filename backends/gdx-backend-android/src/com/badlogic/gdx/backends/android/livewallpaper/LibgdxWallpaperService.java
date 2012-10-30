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

package com.badlogic.gdx.backends.android.livewallpaper;

import android.app.WallpaperManager;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import com.badlogic.gdx.backends.android.AndroidLiveWallpaper;
import com.badlogic.gdx.backends.android.AndroidGraphicsLiveWallpaper;
import com.badlogic.gdx.backends.android.surfaceview.GLBaseSurfaceViewLW;

public abstract class LibgdxWallpaperService extends WallpaperService {
	private final String TAG = "GDX-LW-Service";
	private LibgdxWallpaperEngine previousEngine;
	private static boolean DEBUG = false;

	public LibgdxWallpaperService () {
		super();
	}

	@Override
	public void onCreate () {
		if (DEBUG) Log.d(TAG, " > LibdgxWallpaperService - onCreate()");
		super.onCreate();
	}

	@Override
	abstract public Engine onCreateEngine ();

	@Override
	public void onDestroy () {
		if (DEBUG) Log.d(TAG, " > LibdgxWallpaperService - onDestroy()");
		super.onDestroy();
	}

	public abstract class LibgdxWallpaperEngine extends Engine {
		protected AndroidLiveWallpaper app;
		protected LibgdxWallpaperListener wallpaperListener;
		protected GLBaseSurfaceViewLW view;

		abstract protected void initialize (AndroidLiveWallpaper app);

		public LibgdxWallpaperEngine (final LibgdxWallpaperService libgdxWallpaperService) {
			super();

			if (DEBUG) Log.d(TAG, " > MyEngine() " + hashCode());
			app = new AndroidLiveWallpaper(libgdxWallpaperService, this);
			initialize(app);
			view = ((AndroidGraphicsLiveWallpaper)app.getGraphics()).getView();
		}

		public void setWallpaperListener (LibgdxWallpaperListener wallpaperListener) {
			this.wallpaperListener = wallpaperListener;
		}

		@Override
		public Bundle onCommand (final String pAction, final int pX, final int pY, final int pZ, final Bundle pExtras,
			final boolean pResultRequested) {

			if (DEBUG)
				Log.d(TAG, " > onCommand(" + pAction + " " + pX + " " + pY + " " + pZ + " " + pExtras + " " + pResultRequested + ")");

			if (pAction.equals(WallpaperManager.COMMAND_TAP)) {
				// FIXME
				//((AndroidInput)app.getInput()).onTap(pX, pY);
			} else if (pAction.equals(WallpaperManager.COMMAND_DROP)) {
				// FIXME
				//((AndroidInputLiveWallpaper)app.getInput()).onDrop(pX, pY);
			}
			return super.onCommand(pAction, pX, pY, pZ, pExtras, pResultRequested);
		}

		@Override
		public void onCreate (final SurfaceHolder surfaceHolder) {
			if (DEBUG) Log.d(TAG, " > onCreate() " + hashCode());
			super.onCreate(surfaceHolder);
			if (previousEngine != null) {
				previousEngine.view.onPause();
			}
			previousEngine = this;
			wallpaperListener.setIsPreview(this.isPreview());
		}

		@Override
		public void onDestroy () {
			if (DEBUG) Log.d(TAG, " > onDestroy() " + hashCode());
			view.onDestroy();
			super.onDestroy();
		}

		public void onPause () {
			if (DEBUG) Log.d(TAG, " > onPause() " + hashCode());
			app.onPause();
			view.onPause();
		}

		public void onResume () {
			if (DEBUG) Log.d(TAG, " > onResume() " + hashCode());
			app.onResume();
			view.onResume();
		}

		@Override
		public void onSurfaceChanged (final SurfaceHolder holder, final int format, final int width, final int height) {
			if (DEBUG) Log.d(TAG, " > onSurfaceChanged() " + isPreview() + " " + hashCode());
			super.onSurfaceChanged(holder, format, width, height);
		}

		@Override
		public void onSurfaceCreated (final SurfaceHolder holder) {
			if (DEBUG) Log.d(TAG, " > onSurfaceCreated() " + hashCode());
			super.onSurfaceCreated(holder);
		}

		@Override
		public void onSurfaceDestroyed (final SurfaceHolder holder) {
			if (DEBUG) Log.d(TAG, " > onSurfaceDestroyed() " + hashCode());
			super.onSurfaceDestroyed(holder);
		}

		@Override
		public void onVisibilityChanged (final boolean visible) {
			if (DEBUG) Log.d(TAG, " > onVisibilityChanged(" + visible + ") " + hashCode());
			if (visible) {
				onResume();
			} else {
				onPause();
			}

			super.onVisibilityChanged(visible);
		}

		@Override
		public void onOffsetsChanged (float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset,
			int yPixelOffset) {

			if (DEBUG)
				Log.d(TAG, " > onVisibilityChanged(" + xOffset + " " + yOffset + " " + xOffsetStep + " " + yOffsetStep + " "
					+ xPixelOffset + " " + yPixelOffset + ") " + hashCode());

			wallpaperListener.offsetChange(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
		}
	}
}
