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

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20API18;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18;
import com.badlogic.gdx.backends.android.surfaceview.GdxEglConfigChooser;
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.WindowedMean;

import java.lang.reflect.Method;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

/** An implementation of {@link Graphics} for Android.
 * 
 * @author mzechner */
public final class AndroidGraphicsLiveWallpaper implements Graphics, Renderer {

	// jw: changed
	// final GLBaseSurfaceViewLW view;
	final View view;

	int width;
	int height;
	AndroidLiveWallpaper app;

	protected GL20 gl20;
	protected GL30 gl30;
	protected GLU glu;
	protected EGLContext eglContext;
	protected String extensions;

	protected long lastFrameTime = System.nanoTime();
	protected float deltaTime = 0;
	protected long frameStart = System.nanoTime();
	protected int frames = 0;
	protected int fps;
	protected WindowedMean mean = new WindowedMean(5);

	volatile boolean created = false;
	volatile boolean running = false;
	volatile boolean pause = false;
	volatile boolean resume = false;
	volatile boolean destroy = false;

	protected float ppiX = 0;
	protected float ppiY = 0;
	protected float ppcX = 0;
	protected float ppcY = 0;
	protected float density = 1;

	private final AndroidApplicationConfiguration config;
	private BufferFormat bufferFormat = new BufferFormat(5, 6, 5, 0, 16, 0, 0, false);
	protected boolean isContinuous = true;

	public AndroidGraphicsLiveWallpaper (AndroidLiveWallpaper app, AndroidApplicationConfiguration config,
		ResolutionStrategy resolutionStrategy) {
		this.config = config;
		this.app = app;
		view = createGLSurfaceView(app.service, resolutionStrategy);
		setPreserveContext(view);
	}

	// jw: it will be called only after current GLSurfaceViewLW family of methods
	// will be replaced by subclass of original GLSurfaceView, i'm working on it:)
	// <- ok it is in use now
	private void setPreserveContext (Object view) {
		int sdkVersion = android.os.Build.VERSION.SDK_INT;
		if ((sdkVersion >= 11 && view instanceof GLSurfaceView) || view instanceof GLSurfaceViewAPI18) {
			try {
				Method method = null;
				for (Method m : view.getClass().getMethods()) {
					if (m.getName().equals("setPreserveEGLContextOnPause")) {
						method = m;
						break;
					}
				}
				if (method != null) {
					method.invoke(view, true);
				}
			} catch (Exception e) {
			}
		}
	}

	// jw: I replaced GL..SurfaceViewLW classes with them original counterparts, if it will work
	// on known devices, on opengl 1.0 and 2.0, and all possible SDK versions.. You can remove
	// GL..SurfaceViewLW family of classes completely (there is no use for them).

	// -> specific for live wallpapers
	// jw: synchronized access to current wallpaper surface holder
	SurfaceHolder getSurfaceHolder () {
		synchronized (app.service.sync) {
			return app.service.getSurfaceHolder();
		}
	}

	// <- specific for live wallpapers

	// Grabbed from original AndroidGraphics class, with modifications:
	// + overrided getHolder in created GLSurfaceView and GLSurfaceViewAPI18 instances
	// + Activity changed to Context (as it should be in AndroidGraphics I think;p)
	private View createGLSurfaceView (Context context, final ResolutionStrategy resolutionStrategy) {
		EGLConfigChooser configChooser = getEglConfigChooser();

		if (!checkGL20()) throw new RuntimeException("Libgdx requires OpenGL ES 2.0");

		int sdkVersion = android.os.Build.VERSION.SDK_INT;
		if (sdkVersion <= 10 && config.useGLSurfaceView20API18) {
			GLSurfaceView20API18 view = new GLSurfaceView20API18(context, resolutionStrategy) {
				// -> specific for live wallpapers
				@Override
				public SurfaceHolder getHolder () {
					return getSurfaceHolder();
				}

				// Warning suppressed because the method is invoked via reflection
				// by AndroidLiveWallpaper.onDestroy() 
				@SuppressWarnings("unused")
				public void onDestroy () {
					onDetachedFromWindow(); // calls GLSurfaceView.mGLThread.requestExitAndWait();
				}
				// <- specific for live wallpapers
			};
			if (configChooser != null)
				view.setEGLConfigChooser(configChooser);
			else
				view.setEGLConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil);
			view.setRenderer(this);
			return view;
		}
		else {
			GLSurfaceView20 view = new GLSurfaceView20(context, resolutionStrategy) {
				// -> specific for live wallpapers
				@Override
				public SurfaceHolder getHolder () {
					return getSurfaceHolder();
				}
	
				// Warning suppressed because the method is invoked via reflection
				// by AndroidLiveWallpaper.onDestroy() 
				@SuppressWarnings("unused")
				public void onDestroy () {
					onDetachedFromWindow(); // calls GLSurfaceView.mGLThread.requestExitAndWait();
				}
				// <- specific for live wallpapers
			};
	
			if (configChooser != null)
				view.setEGLConfigChooser(configChooser);
			else
				view.setEGLConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil);
			view.setRenderer(this);
			return view;
		}
	}

	// jw: changed, method replaced with implementation from original AndroidGraphics
	private EGLConfigChooser getEglConfigChooser () {
		return new GdxEglConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil, config.numSamples);
	}

	private void updatePpi () {
		DisplayMetrics metrics = new DisplayMetrics();

		// jw: changed
		app.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		// final Display display = ((WindowManager)app.getService().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		// display.getMetrics(metrics);

		ppiX = metrics.xdpi;
		ppiY = metrics.ydpi;
		ppcX = metrics.xdpi / 2.54f;
		ppcY = metrics.ydpi / 2.54f;
		density = metrics.density;
	}

	protected boolean checkGL20 () {
		EGL10 egl = (EGL10)EGLContext.getEGL();
		EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

		int[] version = new int[2];
		egl.eglInitialize(display, version);

		int EGL_OPENGL_ES2_BIT = 4;
		int[] configAttribs = {EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4, EGL10.EGL_RENDERABLE_TYPE,
			EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE};

		EGLConfig[] configs = new EGLConfig[10];
		int[] num_config = new int[1];
		egl.eglChooseConfig(display, configAttribs, configs, 10, num_config);
		egl.eglTerminate(display);
		return num_config[0] > 0;
	}

	/** {@inheritDoc} */
	@Override
	public GL20 getGL20 () {
		return gl20;
	}

	/** {@inheritDoc} */
	@Override
	public int getHeight () {
		return height;
	}

	/** {@inheritDoc} */
	@Override
	public int getWidth () {
		return width;
	}

	/** This instantiates the GL10, GL11 and GL20 instances. Includes the check for certain devices that pretend to support GL11 but
	 * fuck up vertex buffer objects. This includes the pixelflinger which segfaults when buffers are deleted as well as the
	 * Motorola CLIQ and the Samsung Behold II.
	 * 
	 * @param gl */
	private void setupGL (javax.microedition.khronos.opengles.GL10 gl) {
		if (gl20 != null) return;

		gl20 = new AndroidGL20();

		Gdx.gl = gl20;
		Gdx.gl20 = gl20;
	}

	@Override
	public void onSurfaceChanged (javax.microedition.khronos.opengles.GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;
		updatePpi();
		gl.glViewport(0, 0, this.width, this.height);

		// jw: moved from onSurfaceCreated (as in AndroidGraphics class)
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
		eglContext = ((EGL10)EGLContext.getEGL()).eglGetCurrentContext(); // jw: added
		setupGL(gl);
		logConfig(config);
		updatePpi();

		Mesh.invalidateAllMeshes(app);
		Texture.invalidateAllTextures(app);
		ShaderProgram.invalidateAllShaderPrograms(app);
		FrameBuffer.invalidateAllFrameBuffers(app);

		if (AndroidLiveWallpaperService.DEBUG) { // to prevent creating too many string buffers in live wallpapers
			Gdx.app.debug("AndroidGraphics", Mesh.getManagedStatus());
			Gdx.app.debug("AndroidGraphics", Texture.getManagedStatus());
			Gdx.app.debug("AndroidGraphics", ShaderProgram.getManagedStatus());
			Gdx.app.debug("AndroidGraphics", FrameBuffer.getManagedStatus());
		}

		Display display = app.getWindowManager().getDefaultDisplay();
		this.width = display.getWidth();
		this.height = display.getHeight();
		mean = new WindowedMean(5);
		this.lastFrameTime = System.nanoTime();

		gl.glViewport(0, 0, this.width, this.height);

		// jw: moved to onSurfaceChanged (as in AndroidGraphics class)
		/*
		 * if (created == false) { app.getListener().create(); created = true; synchronized (this) { running = true; } }
		 */
	}

	private void logConfig (EGLConfig config) {
		EGL10 egl = (EGL10)EGLContext.getEGL();
		EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
		int r = getAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
		int g = getAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
		int b = getAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
		int a = getAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);
		int d = getAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
		int s = getAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
		int samples = Math.max(getAttrib(egl, display, config, EGL10.EGL_SAMPLES, 0),
			getAttrib(egl, display, config, GdxEglConfigChooser.EGL_COVERAGE_SAMPLES_NV, 0));
		boolean coverageSample = getAttrib(egl, display, config, GdxEglConfigChooser.EGL_COVERAGE_SAMPLES_NV, 0) != 0;

		// print configuration just one time (on some devices gl context is recreated every time when device is locked / unlocked -
// every time when screen turns on and off)
		if (!configLogged) {

			if (gl20 != null) {
				Gdx.app.log("AndroidGraphics", "OGL renderer: " + gl20.glGetString(GL10.GL_RENDERER));
				Gdx.app.log("AndroidGraphics", "OGL vendor: " + gl20.glGetString(GL10.GL_VENDOR));
				Gdx.app.log("AndroidGraphics", "OGL version: " + gl20.glGetString(GL10.GL_VERSION));
				Gdx.app.log("AndroidGraphics", "OGL extensions: " + gl20.glGetString(GL10.GL_EXTENSIONS));
				configLogged = true;
			}

			Gdx.app.log("AndroidGraphics", "framebuffer: (" + r + ", " + g + ", " + b + ", " + a + ")");
			Gdx.app.log("AndroidGraphics", "depthbuffer: (" + d + ")");
			Gdx.app.log("AndroidGraphics", "stencilbuffer: (" + s + ")");
			Gdx.app.log("AndroidGraphics", "samples: (" + samples + ")");
			Gdx.app.log("AndroidGraphics", "coverage sampling: (" + coverageSample + ")");
		}

		bufferFormat = new BufferFormat(r, g, b, a, d, s, samples, coverageSample);
	}

	boolean configLogged = false;

	int[] value = new int[1];

	private int getAttrib (EGL10 egl, EGLDisplay display, EGLConfig config, int attrib, int defValue) {
		if (egl.eglGetConfigAttrib(display, config, attrib, value)) {
			return value[0];
		}
		return defValue;
	}

	Object synch = new Object();

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

	// jw: never called on lvp, why? see description in AndroidLiveWallpaper.onPause
	void pause () {
		synchronized (synch) {
			if (!running) return;
			running = false;
			pause = true;

			while (pause) {
				try {
					synch.wait();
				} catch (InterruptedException ignored) {
					Gdx.app.log("AndroidGraphics", "waiting for pause synchronization failed!");
				}
			}
		}
	}

	// jw: never called on lvp
	void destroy () {
		synchronized (synch) {
			running = false;
			destroy = true;

			while (destroy) {
				try {
					synch.wait();
				} catch (InterruptedException ex) {
					Gdx.app.log("AndroidGraphics", "waiting for destroy synchronization failed!");
				}
			}
		}
	}

	@Override
	public void onDrawFrame (javax.microedition.khronos.opengles.GL10 gl) {
		long time = System.nanoTime();
		deltaTime = (time - lastFrameTime) / 1000000000.0f;
		lastFrameTime = time;

		// jw: after pause deltaTime can have somewhat huge value and it destabilize mean, so I propose to just cut it of
		if (!resume) {
			mean.addValue(deltaTime);
		} else {
			deltaTime = 0;
		}
		// mean.addValue(deltaTime);

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
			app.listener.resume();
			Gdx.app.log("AndroidGraphics", "resumed");
		}

		// HACK: added null check to handle set wallpaper from preview null
		// error in renderer
		// jw: this hack is not working always, renderer ends with error for some devices - because of uninitialized gl context
		// jw: now it shouldn't be necessary - after wallpaper backend refactoring:)
		if (lrunning && (Gdx.graphics.getGL20() != null)) {

			// jw: changed
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
			/*
			 * synchronized (app.runnables) { for (int i = 0; i < app.runnables.size; i++) { app.runnables.get(i).run(); }
			 * app.runnables.clear(); }
			 */

			app.input.processEvents();
			app.listener.render();
		}

		// jw: never called on lvp, why? see description in AndroidLiveWallpaper.onPause
		if (lpause) {
			app.listener.pause();
			// ((AndroidAudio)app.getAudio()).pause(); jw: moved to AndroidLiveWallpaper.onPause
			Gdx.app.log("AndroidGraphics", "paused");
		}

		// jw: never called on lwp, why? see description in AndroidLiveWallpaper.onPause
		if (ldestroy) {
			app.listener.dispose();
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

	/** {@inheritDoc} */
	@Override
	public float getDeltaTime () {
		return mean.getMean() == 0 ? deltaTime : mean.getMean();
	}

	@Override
	public float getRawDeltaTime () {
		return deltaTime;
	}

	/** {@inheritDoc} */
	@Override
	public GraphicsType getType () {
		return GraphicsType.AndroidGL;
	}

	/** {@inheritDoc} */
	@Override
	public int getFramesPerSecond () {
		return fps;
	}

	public void clearManagedCaches () {
		Mesh.clearAllMeshes(app);
		Texture.clearAllTextures(app);
		ShaderProgram.clearAllShaderPrograms(app);
		FrameBuffer.clearAllFrameBuffers(app);

		if (AndroidLiveWallpaperService.DEBUG) { // to prevent creating too many string buffers in live wallpapers
			Gdx.app.debug("AndroidGraphics", Mesh.getManagedStatus());
			Gdx.app.debug("AndroidGraphics", Texture.getManagedStatus());
			Gdx.app.debug("AndroidGraphics", ShaderProgram.getManagedStatus());
			Gdx.app.debug("AndroidGraphics", FrameBuffer.getManagedStatus());
		}
	}

	// jw: changed this
	// public GLBaseSurfaceViewLW getView () {
	public View getView () {
		return view;
	}

	@Override
	public float getPpiX () {
		return ppiX;
	}

	@Override
	public float getPpiY () {
		return ppiY;
	}

	@Override
	public float getPpcX () {
		return ppcX;
	}

	@Override
	public float getPpcY () {
		return ppcY;
	}

	@Override
	public float getDensity () {
		return density;
	}

	@Override
	public boolean supportsDisplayModeChange () {
		return false;
	}

	@Override
	public boolean setDisplayMode (DisplayMode displayMode) {
		return false;
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
	public void setTitle (String title) {

	}

	private class AndroidDisplayMode extends DisplayMode {
		protected AndroidDisplayMode (int width, int height, int refreshRate, int bitsPerPixel) {
			super(width, height, refreshRate, bitsPerPixel);
		}
	}

	@Override
	public DisplayMode getDesktopDisplayMode () {
		DisplayMetrics metrics = new DisplayMetrics();
		app.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return new AndroidDisplayMode(metrics.widthPixels, metrics.heightPixels, 0, 0);
	}

	@Override
	public BufferFormat getBufferFormat () {
		return bufferFormat;
	}

	@Override
	public void setVSync (boolean vsync) {
	}

	@Override
	public boolean supportsExtension (String extension) {
		if (extensions == null) extensions = Gdx.gl.glGetString(GL10.GL_EXTENSIONS);
		return extensions.contains(extension);
	}

	@Override
	public void setContinuousRendering (boolean isContinuous) {
		if (view != null) {
			this.isContinuous = isContinuous;
			// jw: changed
			// view.setRenderMode(renderMode);
			if (view instanceof GLSurfaceViewAPI18) {
				int renderMode = isContinuous ? GLSurfaceViewAPI18.RENDERMODE_CONTINUOUSLY : GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY;
				((GLSurfaceViewAPI18)view).setRenderMode(renderMode);
			}
			else if (view instanceof GLSurfaceView) {
				int renderMode = isContinuous ? GLSurfaceView.RENDERMODE_CONTINUOUSLY : GLSurfaceView.RENDERMODE_WHEN_DIRTY;
				((GLSurfaceView)view).setRenderMode(renderMode);
			}
			else
				throw new RuntimeException("unimplemented");
			mean.clear();
		}
	}

	public boolean isContinuousRendering () {
		return isContinuous;
	}

	@Override
	public void requestRendering () {
		if (view != null) {
			// jw: changed
			// view.requestRender();
			if (view instanceof GLSurfaceViewAPI18)
				((GLSurfaceViewAPI18)view).requestRender();
			else if (view instanceof GLSurfaceView)
				((GLSurfaceView)view).requestRender();
			else
				throw new RuntimeException("unimplemented");
		}
	}

	@Override
	public boolean isFullscreen () {
		return true;
	}

	@Override
	public boolean isGL30Available () {
		return gl30 != null;
	}

	@Override
	public GL30 getGL30 () {
		return gl30;
	}
}
