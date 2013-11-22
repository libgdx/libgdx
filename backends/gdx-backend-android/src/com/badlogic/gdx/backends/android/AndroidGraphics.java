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
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.opengl.GLSurfaceView.Renderer;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewCupcake;
import com.badlogic.gdx.backends.android.surfaceview.GdxEglConfigChooser;
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.utils.Array;

import java.lang.reflect.Method;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/** An implementation of {@link Graphics} for Android.
 * 
 * @author mzechner */
public final class AndroidGraphics implements Graphics, Renderer {
	final View view;
	int width;
	int height;
	AndroidApplication app;
	GLCommon gl;
	GL10 gl10;
	GL11 gl11;
	GL20 gl20;
	EGLContext eglContext;
	String extensions;

	private long lastFrameTime = System.nanoTime();
	private float deltaTime = 0;
	private long frameStart = System.nanoTime();
	private int frames = 0;
	private int fps;
	private WindowedMean mean = new WindowedMean(5);

	volatile boolean created = false;
	volatile boolean running = false;
	volatile boolean pause = false;
	volatile boolean resume = false;
	volatile boolean destroy = false;

	private float ppiX = 0;
	private float ppiY = 0;
	private float ppcX = 0;
	private float ppcY = 0;
	private float density = 1;

	private final AndroidApplicationConfiguration config;
	private BufferFormat bufferFormat = new BufferFormat(5, 6, 5, 0, 16, 0, 0, false);
	private boolean isContinuous = true;

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

					@Override
					public InputConnection onCreateInputConnection (EditorInfo outAttrs) {
						BaseInputConnection connection = new BaseInputConnection(this, false) {
							@Override
							public boolean deleteSurroundingText (int beforeLength, int afterLength) {
								int sdkVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
								if (sdkVersion >= 16) {
									/* In Jelly Bean, they don't send key events for delete.
									 *  Instead, they send beforeLength = 1, afterLength = 0.
									 *  So, we'll just simulate what it used to do. */
									if (beforeLength == 1 && afterLength == 0) {
										sendDownUpKeyEventForBackwardCompatibility(KeyEvent.KEYCODE_DEL);
										return true;
									}
								}
								return super.deleteSurroundingText(beforeLength, afterLength);
							}
							private void sendDownUpKeyEventForBackwardCompatibility (final int code) {
								final long eventTime = SystemClock.uptimeMillis();
								super.sendKeyEvent(new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, code, 0, 0,
									KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
								super.sendKeyEvent(new KeyEvent(SystemClock.uptimeMillis(), eventTime, KeyEvent.ACTION_UP, code, 0, 0,
									KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
							}
						};
						return connection;
					}

				};
				if (configChooser != null)
					view.setEGLConfigChooser(configChooser);
				else
					view.setEGLConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil);
				view.setRenderer(this);
				return view;
			} else {
				if (config.useGLSurfaceViewAPI18) {
					GLSurfaceViewAPI18 view = new GLSurfaceViewAPI18(activity, resolutionStrategy);
					if (configChooser != null)
						view.setEGLConfigChooser(configChooser);
					else
						view.setEGLConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil);
					view.setRenderer(this);
					return view;
				}
				else {
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

	private boolean checkGL20 () {
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
	public GL10 getGL10 () {
		return gl10;
	}

	/** {@inheritDoc} */
	@Override
	public GL11 getGL11 () {
		return gl11;
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

	/** {@inheritDoc} */
	@Override
	public boolean isGL11Available () {
		return gl11 != null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isGL20Available () {
		return gl20 != null;
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

		Gdx.gl = this.gl;
		Gdx.gl10 = gl10;
		Gdx.gl11 = gl11;
		Gdx.gl20 = gl20;

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

		Gdx.app.log("AndroidGraphics", "framebuffer: (" + r + ", " + g + ", " + b + ", " + a + ")");
		Gdx.app.log("AndroidGraphics", "depthbuffer: (" + d + ")");
		Gdx.app.log("AndroidGraphics", "stencilbuffer: (" + s + ")");
		Gdx.app.log("AndroidGraphics", "samples: (" + samples + ")");
		Gdx.app.log("AndroidGraphics", "coverage sampling: (" + coverageSample + ")");

		bufferFormat = new BufferFormat(r, g, b, a, d, s, samples, coverageSample);
	}

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
		}
	}

	void pause () {
		synchronized (synch) {
			if (!running) return;
			running = false;
			pause = true;
			while (pause) {
				try {
					// TODO: fix deadlock race condition with quick resume/pause.
					// Temporary workaround:
					// Android ANR time is 5 seconds, so wait up to 4 seconds before assuming
					// deadlock and killing process. This can easily be triggered by openning the
					// Recent Apps list and then double-tapping the Recent Apps button with
					// ~500ms between taps.
					synch.wait(4000);
					if (pause) {
						Gdx.app.error("AndroidGraphics", "waiting for pause synchronization took too "
						                                 + "long; assuming deadlock and killing");
						android.os.Process.killProcess(android.os.Process.myPid());
					}
				} catch (InterruptedException ignored) {
					Gdx.app.log("AndroidGraphics", "waiting for pause synchronization failed!");
				}
			}
		}
	}

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
			Array<LifecycleListener> listeners = ((AndroidApplication)app).lifecycleListeners;
			synchronized(listeners) {
				for(LifecycleListener listener: listeners) {
					listener.resume();
				}
			}
			app.listener.resume();
			Gdx.app.log("AndroidGraphics", "resumed");
		}

		if (lrunning) {
			synchronized (app.runnables) {
				app.executedRunnables.clear();
				app.executedRunnables.addAll(app.runnables);
				app.runnables.clear();
			}

			for (int i = 0; i < app.executedRunnables.size; i++) {
				try {
					app.executedRunnables.get(i).run();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			app.input.processEvents();
			app.listener.render();
		}

		if (lpause) {
			Array<LifecycleListener> listeners = ((AndroidApplication)app).lifecycleListeners;
			synchronized(listeners) {
				for(LifecycleListener listener: listeners) {
					listener.pause();
				}
			}
			app.listener.pause();
			((AndroidApplication)app).audio.pause();
			Gdx.app.log("AndroidGraphics", "paused");
		}

		if (ldestroy) {
			Array<LifecycleListener> listeners = ((AndroidApplication)app).lifecycleListeners;
			synchronized(listeners) {
				for(LifecycleListener listener: listeners) {
					listener.dispose();
				}
			}
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

		Gdx.app.log("AndroidGraphics", Mesh.getManagedStatus());
		Gdx.app.log("AndroidGraphics", Texture.getManagedStatus());
		Gdx.app.log("AndroidGraphics", ShaderProgram.getManagedStatus());
		Gdx.app.log("AndroidGraphics", FrameBuffer.getManagedStatus());
	}

	public View getView () {
		return view;
	}

	/** {@inheritDoc} */
	@Override
	public GLCommon getGLCommon () {
		return gl;
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
			int renderMode = isContinuous ? GLSurfaceView.RENDERMODE_CONTINUOUSLY : GLSurfaceView.RENDERMODE_WHEN_DIRTY;
			if (view instanceof GLSurfaceViewCupcake) ((GLSurfaceViewCupcake)view).setRenderMode(renderMode);
			if (view instanceof GLSurfaceViewAPI18) ((GLSurfaceViewAPI18)view).setRenderMode(renderMode);
			if (view instanceof GLSurfaceView) ((GLSurfaceView)view).setRenderMode(renderMode);
			mean.clear();
		}
	}

	public boolean isContinuousRendering () {
		return isContinuous;
	}

	@Override
	public void requestRendering () {
		if (view != null) {
			if (view instanceof GLSurfaceViewCupcake) ((GLSurfaceViewCupcake)view).requestRender();
			if (view instanceof GLSurfaceViewAPI18) ((GLSurfaceViewAPI18)view).requestRender();
			if (view instanceof GLSurfaceView) ((GLSurfaceView)view).requestRender();
		}
	}

	@Override
	public boolean isFullscreen () {
		return true;
	}
}