/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Modified by Elijah Cornell
 * 2013.01 Modified by Jaroslaw Wisniewski <j.wisniewski@appsisle.com>
 * 2014.04 Modified by davebaol
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

import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20API18;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18;
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** A subclass of {@link AndroidGraphics} specialized for live wallpaper applications.
 * 
 * @author mzechner */
public final class AndroidGraphicsLiveWallpaper extends AndroidGraphics {

	public AndroidGraphicsLiveWallpaper (AndroidLiveWallpaper lwp, AndroidApplicationConfiguration config,
		ResolutionStrategy resolutionStrategy) {
		super(lwp, config, resolutionStrategy, false);
	}

	// jw: I replaced GL..SurfaceViewLW classes with their original counterparts, if it will work
	// on known devices, on opengl 1.0 and 2.0, and all possible SDK versions.. You can remove
	// GL..SurfaceViewLW family of classes completely (there is no use for them).

	// -> specific for live wallpapers
	// jw: synchronized access to current wallpaper surface holder
	SurfaceHolder getSurfaceHolder () {
		synchronized (((AndroidLiveWallpaper)app).service.sync) {
			return ((AndroidLiveWallpaper)app).service.getSurfaceHolder();
		}
	}

	// <- specific for live wallpapers

	// Grabbed from AndroidGraphics superclass and modified to override
	// getHolder in created GLSurfaceView and GLSurfaceViewAPI18 instances
	@Override
	protected View createGLSurfaceView (AndroidApplicationBase application, final ResolutionStrategy resolutionStrategy) {
		if (!checkGL20()) throw new GdxRuntimeException("Libgdx requires OpenGL ES 2.0");

		EGLConfigChooser configChooser = getEglConfigChooser();
		int sdkVersion = android.os.Build.VERSION.SDK_INT;
		if (sdkVersion <= 10 && config.useGLSurfaceView20API18) {
			GLSurfaceView20API18 view = new GLSurfaceView20API18(application.getContext(), resolutionStrategy) {
				@Override
				public SurfaceHolder getHolder () {
					return getSurfaceHolder();
				}

				// This method is invoked via reflection by AndroidLiveWallpaper.onDestroy() 
				public void onDestroy () {
					onDetachedFromWindow(); // calls GLSurfaceView.mGLThread.requestExitAndWait();
				}
			};
			if (configChooser != null)
				view.setEGLConfigChooser(configChooser);
			else
				view.setEGLConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil);
			view.setRenderer(this);
			return view;
		}
		else {
			GLSurfaceView20 view = new GLSurfaceView20(application.getContext(), resolutionStrategy) {
				@Override
				public SurfaceHolder getHolder () {
					return getSurfaceHolder();
				}
	
				// This method is invoked via reflection by AndroidLiveWallpaper.onDestroy() 
				public void onDestroy () {
					onDetachedFromWindow(); // calls GLSurfaceView.mGLThread.requestExitAndWait();
				}
			};
	
			if (configChooser != null)
				view.setEGLConfigChooser(configChooser);
			else
				view.setEGLConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil);
			view.setRenderer(this);
			return view;
		}
	}

	// kill the GLThread managed by GLSurfaceView (only for GLSurfaceView because GLSurffaceViewCupcake stops thread in
	// onPause events - which is not as easy and safe for GLSurfaceView)
	public void onDestroyGLSurfaceView () {
		if (view != null) {
			if (view instanceof GLSurfaceView || view instanceof GLSurfaceViewAPI18) {
				try {
					// onDestroy redirects to onDetachedFromWindow - which stops GLThread by calling mGLThread.requestExitAndWait()
					view.getClass().getMethod("onDestroy").invoke(view);
					if (AndroidLiveWallpaperService.DEBUG)
						Log.d(AndroidLiveWallpaperService.TAG,
							" > AndroidLiveWallpaper - onDestroy() stopped GLThread managed by GLSurfaceView");
				} catch (Throwable t) {
					// error while scheduling exit of GLThread, GLThread will remain live and wallpaper service
					// wouldn't be able to shutdown completely
					Log.e(AndroidLiveWallpaperService.TAG,
						"failed to destroy GLSurfaceView's thread! GLSurfaceView.onDetachedFromWindow impl changed since API lvl 16!");
					t.printStackTrace();
				}
			}
		}
	}

	@Override
	void resume () {
		synchronized (synch) {
			running = true;
			resume = true;

			// by jw: added synchronization, there was nothing before
			while (resume) {
				try {
					synch.wait();
				} catch (InterruptedException ignored) {
					Gdx.app.log("AndroidGraphics", "waiting for resume synchronization failed!");
				}
			}
		}
	}

	@Override
	public void onDrawFrame (javax.microedition.khronos.opengles.GL10 gl) {
		long time = System.nanoTime();
		deltaTime = (time - lastFrameTime) / 1000000000.0f;
		lastFrameTime = time;

		// After pause deltaTime can have somewhat huge value that destabilizes the mean, so let's cut it off
		if (!resume) {
			mean.addValue(deltaTime);
		} else {
			deltaTime = 0;
		}

		boolean lrunning = false;
		boolean lpause = false;
		boolean ldestroy = false;
		boolean lresume = false;

		synchronized (synch) {
			lrunning = running;
			lpause = pause;
			ldestroy = destroy;
			lresume = resume;

			if (resume) {
				resume = false;
				// by jw: originally was not synchronized
				synch.notifyAll();
			}

			if (pause) {
				pause = false;
				synch.notifyAll();
			}

			if (destroy) {
				destroy = false;
				synch.notifyAll();
			}
		}

		if (lresume) {
			// ((AndroidAudio)app.getAudio()).resume(); // jw: moved to AndroidLiveWallpaper.onResume
			app.getApplicationListener().resume();
			Gdx.app.log("AndroidGraphics", "resumed");
		}

		// HACK: added null check to handle set wallpaper from preview null
		// error in renderer
		// jw: this hack is not working always, renderer ends with error for some devices - because of uninitialized gl context
		// jw: now it shouldn't be necessary - after wallpaper backend refactoring:)
		if (lrunning) {

			// jw: changed
			synchronized (app.getRunnables()) {
				app.getExecutedRunnables().clear();
				app.getExecutedRunnables().addAll(app.getRunnables());
				app.getRunnables().clear();

				for (int i = 0; i < app.getExecutedRunnables().size; i++) {
					try {
						app.getExecutedRunnables().get(i).run();
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			}
			/*
			 * synchronized (app.runnables) { for (int i = 0; i < app.runnables.size; i++) { app.runnables.get(i).run(); }
			 * app.runnables.clear(); }
			 */

			app.getInput().processEvents();
			frameId++;
			app.getApplicationListener().render();
		}

		// jw: never called on lvp, why? see description in AndroidLiveWallpaper.onPause
		if (lpause) {
			app.getApplicationListener().pause();
			// ((AndroidAudio)app.getAudio()).pause(); jw: moved to AndroidLiveWallpaper.onPause
			Gdx.app.log("AndroidGraphics", "paused");
		}

		// jw: never called on lwp, why? see description in AndroidLiveWallpaper.onPause
		if (ldestroy) {
			app.getApplicationListener().dispose();
			// ((AndroidAudio)app.getAudio()).dispose(); jw: moved to AndroidLiveWallpaper.onDestroy
			Gdx.app.log("AndroidGraphics", "destroyed");
		}

		if (time - frameStart > 1000000000) {
			fps = frames;
			frames = 0;
			frameStart = time;
		}
		frames++;
	}

	@Override
	protected void logManagedCachesStatus() {
		// to prevent creating too many string buffers in live wallpapers
		if (AndroidLiveWallpaperService.DEBUG) {
			super.logManagedCachesStatus();
		}
	}
}
