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

import java.lang.reflect.Method;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.opengl.GLSurfaceView.Renderer;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewCupcake;
import com.badlogic.gdx.backends.android.surfaceview.GdxEglConfigChooser;
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.GLU;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.WindowedMean;

/** An implementation of {@link Graphics} for Android.
 * 
 * @author mzechner */
public final class AndroidGraphics extends AndroidGraphicsBase implements Graphics, Renderer {
	final View view;

	AndroidApplication app;

	private final AndroidApplicationConfiguration config;

	public AndroidGraphics (AndroidApplication activity, AndroidApplicationConfiguration config,
		ResolutionStrategy resolutionStrategy) {
		this.config = config;
		view = createGLSurfaceView(activity, config.useGL20, resolutionStrategy);
		setPreserveContext(view);
		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		this.app = activity;
	}

	private void setPreserveContext (View view) {
		int sdkVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
		if (sdkVersion >= 11 && view instanceof GLSurfaceView20) {
			try {
				Method method = null;
				for (Method m : view.getClass().getMethods()) {
					if (m.getName().equals("setPreserveEGLContextOnPause")) {
						method = m;
						break;
					}
				}
				if (method != null) {
					method.invoke((GLSurfaceView20)view, true);
				}
			} catch (Exception e) {
			}
		}
	}

	private View createGLSurfaceView (Activity activity, boolean useGL2, final ResolutionStrategy resolutionStrategy) {
		EGLConfigChooser configChooser = getEglConfigChooser();

		if (useGL2 && checkGL20()) {
			GLSurfaceView20 view = new GLSurfaceView20(activity, resolutionStrategy);
			if (configChooser != null)
				view.setEGLConfigChooser(configChooser);
			else
				view.setEGLConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil);
			view.setRenderer(this);
			return view;
		} else {
			config.useGL20 = false;
			configChooser = getEglConfigChooser();
			int sdkVersion = Integer.parseInt(android.os.Build.VERSION.SDK);

			if (sdkVersion >= 11) {
				GLSurfaceView view = new GLSurfaceView(activity) {
					@Override
					protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
						ResolutionStrategy.MeasuredDimension measures = resolutionStrategy.calcMeasures(widthMeasureSpec,
							heightMeasureSpec);
						setMeasuredDimension(measures.width, measures.height);
					}
				};
				if (configChooser != null)
					view.setEGLConfigChooser(configChooser);
				else
					view.setEGLConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil);
				view.setRenderer(this);
				return view;
			} else {
				GLSurfaceViewCupcake view = new GLSurfaceViewCupcake(activity, resolutionStrategy);
				if (configChooser != null)
					view.setEGLConfigChooser(configChooser);
				else
					view.setEGLConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil);
				view.setRenderer(this);
				return view;
			}
		}
	}

	private EGLConfigChooser getEglConfigChooser () {
		return new GdxEglConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil, config.numSamples,
			config.useGL20);
	}

	private void updatePpi () {
		DisplayMetrics metrics = new DisplayMetrics();
		app.getWindowManager().getDefaultDisplay().getMetrics(metrics);

		ppiX = metrics.xdpi;
		ppiY = metrics.ydpi;
		ppcX = metrics.xdpi / 2.54f;
		ppcY = metrics.ydpi / 2.54f;
		density = metrics.density;
	}

	private static boolean isPowerOfTwo (int value) {
		return ((value != 0) && (value & (value - 1)) == 0);
	}

	/** This instantiates the GL10, GL11 and GL20 instances. Includes the check for certain devices that pretend to support GL11 but
	 * fuck up vertex buffer objects. This includes the pixelflinger which segfaults when buffers are deleted as well as the
	 * Motorola CLIQ and the Samsung Behold II.
	 * 
	 * @param gl */
	private void setupGL (javax.microedition.khronos.opengles.GL10 gl) {
		if (gl10 != null || gl20 != null) return;

		if (view instanceof GLSurfaceView20) {
			gl20 = new AndroidGL20();
			this.gl = gl20;
		} else {
			gl10 = new AndroidGL10(gl);
			this.gl = gl10;
			if (gl instanceof javax.microedition.khronos.opengles.GL11) {
				String renderer = gl.glGetString(GL10.GL_RENDERER);
				if (renderer != null) { // silly GT-I7500
					if (!renderer.toLowerCase().contains("pixelflinger")
						&& !(android.os.Build.MODEL.equals("MB200") || android.os.Build.MODEL.equals("MB220") || android.os.Build.MODEL
							.contains("Behold"))) {
						gl11 = new AndroidGL11((javax.microedition.khronos.opengles.GL11)gl);
						gl10 = gl11;
					}
				}
			}
		}

		this.glu = new AndroidGLU();

		Gdx.gl = this.gl;
		Gdx.gl10 = gl10;
		Gdx.gl11 = gl11;
		Gdx.gl20 = gl20;
		Gdx.glu = glu;

		Gdx.app.log("AndroidGraphics", "OGL renderer: " + gl.glGetString(GL10.GL_RENDERER));
		Gdx.app.log("AndroidGraphics", "OGL vendor: " + gl.glGetString(GL10.GL_VENDOR));
		Gdx.app.log("AndroidGraphics", "OGL version: " + gl.glGetString(GL10.GL_VERSION));
		Gdx.app.log("AndroidGraphics", "OGL extensions: " + gl.glGetString(GL10.GL_EXTENSIONS));
	}

	@Override
	public void onSurfaceChanged (javax.microedition.khronos.opengles.GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;
		updatePpi();
		gl.glViewport(0, 0, this.width, this.height);
		if (created == false) {
			app.listener.create();
			created = true;
			synchronized (this) {
				running = true;
			}
		}
		app.listener.resize(width, height);
	}

	@Override
	public void onSurfaceCreated (javax.microedition.khronos.opengles.GL10 gl, EGLConfig config) {
		eglContext = ((EGL10)EGLContext.getEGL()).eglGetCurrentContext();
		setupGL(gl);
		logConfig(config);
		updatePpi();

		Mesh.invalidateAllMeshes(app);
		Texture.invalidateAllTextures(app);
		ShaderProgram.invalidateAllShaderPrograms(app);
		FrameBuffer.invalidateAllFrameBuffers(app);

		Gdx.app.log("AndroidGraphics", Mesh.getManagedStatus());
		Gdx.app.log("AndroidGraphics", Texture.getManagedStatus());
		Gdx.app.log("AndroidGraphics", ShaderProgram.getManagedStatus());
		Gdx.app.log("AndroidGraphics", FrameBuffer.getManagedStatus());

		Display display = app.getWindowManager().getDefaultDisplay();
		this.width = display.getWidth();
		this.height = display.getHeight();
		mean = new WindowedMean(5);
		this.lastFrameTime = System.nanoTime();

		gl.glViewport(0, 0, this.width, this.height);
	}

	@Override
	public void onDrawFrame (javax.microedition.khronos.opengles.GL10 gl) {
		long time = System.nanoTime();
		deltaTime = (time - lastFrameTime) / 1000000000.0f;
		lastFrameTime = time;
		mean.addValue(deltaTime);

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
			((AndroidApplication)app).audio.resume();
			app.listener.resume();
			Gdx.app.log("AndroidGraphics", "resumed");
		}

		if (lrunning) {
			synchronized (app.runnables) {
				app.executedRunnables.clear();
				app.executedRunnables.addAll(app.runnables);
				app.runnables.clear();

				for (int i = 0; i < app.executedRunnables.size; i++) {
					try {
						app.executedRunnables.get(i).run();
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			}
			app.input.processEvents();
			app.listener.render();
		}

		if (lpause) {
			app.listener.pause();
			((AndroidApplication)app).audio.pause();
			Gdx.app.log("AndroidGraphics", "paused");
		}

		if (ldestroy) {
			app.listener.dispose();
			((AndroidApplication)app).audio.dispose();
			((AndroidApplication)app).audio = null;
			Gdx.app.log("AndroidGraphics", "destroyed");
		}

		if (time - frameStart > 1000000000) {
			fps = frames;
			frames = 0;
			frameStart = time;
		}
		frames++;
	}

	protected void clearManagedCaches () {
		Mesh.clearAllMeshes(app);
		Texture.clearAllTextures(app);
		ShaderProgram.clearAllShaderPrograms(app);
		FrameBuffer.clearAllFrameBuffers(app);

		Gdx.app.log("AndroidGraphics", Mesh.getManagedStatus());
		Gdx.app.log("AndroidGraphics", Texture.getManagedStatus());
		Gdx.app.log("AndroidGraphics", ShaderProgram.getManagedStatus());
		Gdx.app.log("AndroidGraphics", FrameBuffer.getManagedStatus());
	}

	public View getView () {
		return view;
	}
	
	@Override
	public DisplayMode[] getDisplayModes () {
		return new DisplayMode[] {getDesktopDisplayMode()};
	}

	@Override
	public boolean setDisplayMode (int width, int height, boolean fullscreen) {
		return false;
	}

	@Override
	public DisplayMode getDesktopDisplayMode () {
		DisplayMetrics metrics = new DisplayMetrics();
		app.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return new AndroidDisplayMode(metrics.widthPixels, metrics.heightPixels, 0, 0);
	}

	@Override
	public void setContinuousRendering (boolean isContinuous) {
		if (view != null) {
			this.isContinuous = isContinuous;
			int renderMode = isContinuous ? GLSurfaceView.RENDERMODE_CONTINUOUSLY : GLSurfaceView.RENDERMODE_WHEN_DIRTY;
			if (view instanceof GLSurfaceViewCupcake) ((GLSurfaceViewCupcake)view).setRenderMode(renderMode);
			if (view instanceof GLSurfaceView) ((GLSurfaceView)view).setRenderMode(renderMode);
			mean.clear();
		}
	}

	@Override
	public void requestRendering () {
		if (view != null) {
			if (view instanceof GLSurfaceViewCupcake) ((GLSurfaceViewCupcake)view).requestRender();
			if (view instanceof GLSurfaceView) ((GLSurfaceView)view).requestRender();
		}
	}

	@Override
	public boolean isFullscreen () {
		return true;
	}
}
