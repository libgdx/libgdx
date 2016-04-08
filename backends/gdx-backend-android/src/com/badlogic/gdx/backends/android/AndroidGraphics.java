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

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20API18;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18;
import com.badlogic.gdx.backends.android.surfaceview.GdxEglConfigChooser;
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureArray;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SnapshotArray;

import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.opengl.GLSurfaceView.Renderer;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager.LayoutParams;

/** An implementation of {@link Graphics} for Android.
 *
 * @author mzechner */
public class AndroidGraphics implements Graphics, Renderer {

	private static final String LOG_TAG = "AndroidGraphics";

	/** When {@link AndroidFragmentApplication#onPause()} or {@link AndroidApplication#onPause()} call
	 * {@link AndroidGraphics#pause()} they <b>MUST</b> enforce continuous rendering. If not, {@link #onDrawFrame(GL10)} will
	 * not be called in the GLThread while {@link #pause()} is sleeping in the Android UI Thread which will cause the
	 * {@link AndroidGraphics#pause} variable never be set to false. As a result, the {@link AndroidGraphics#pause()} method
	 * will kill the current process to avoid ANR */
	static volatile boolean enforceContinuousRendering = false;

	final View view;
	int width;
	int height;
	AndroidApplicationBase app;
	GL20 gl20;
	GL30 gl30;
	EGLContext eglContext;
	GLVersion glVersion;
	String extensions;

	protected long lastFrameTime = System.nanoTime();
// protected float deltaTimeFloat = 0;
	protected long frameStart = System.nanoTime();
	protected long frameId = -1;
	protected int frames = 0;
	protected int fps;
// protected WindowedMean mean = new WindowedMean(5);

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

	protected final AndroidApplicationConfiguration config;
	private BufferFormat bufferFormat = new BufferFormat(5, 6, 5, 0, 16, 0, 0, false);
	private boolean isContinuous = true;

	public AndroidGraphics (AndroidApplicationBase application, AndroidApplicationConfiguration config,
		ResolutionStrategy resolutionStrategy) {
		this(application, config, resolutionStrategy, true);
	}

	public AndroidGraphics (AndroidApplicationBase application, AndroidApplicationConfiguration config,
		ResolutionStrategy resolutionStrategy, boolean focusableView) {
		this.config = config;
		this.app = application;
		view = createGLSurfaceView(application, resolutionStrategy);
		preserveEGLContextOnPause();
		if (focusableView) {
			view.setFocusable(true);
			view.setFocusableInTouchMode(true);
		}
		System.out.println("RedAndroidGraphics_START");
	}

	protected void preserveEGLContextOnPause () {
		int sdkVersion = android.os.Build.VERSION.SDK_INT;
		if ((sdkVersion >= 11 && view instanceof GLSurfaceView20) || view instanceof GLSurfaceView20API18) {
			try {
				view.getClass().getMethod("setPreserveEGLContextOnPause", boolean.class).invoke(view, true);
			} catch (Exception e) {
				Gdx.app.log(LOG_TAG, "Method GLSurfaceView.setPreserveEGLContextOnPause not found");
			}
		}
	}

	protected View createGLSurfaceView (AndroidApplicationBase application, final ResolutionStrategy resolutionStrategy) {
		if (!checkGL20()) throw new GdxRuntimeException("Libgdx requires OpenGL ES 2.0");

		EGLConfigChooser configChooser = getEglConfigChooser();
		int sdkVersion = android.os.Build.VERSION.SDK_INT;
		if (sdkVersion <= 10 && config.useGLSurfaceView20API18) {
			GLSurfaceView20API18 view = new GLSurfaceView20API18(application.getContext(), resolutionStrategy);
			if (configChooser != null)
				view.setEGLConfigChooser(configChooser);
			else
				view.setEGLConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil);
			view.setRenderer(this);
			return view;
		} else {
			GLSurfaceView20 view = new GLSurfaceView20(application.getContext(), resolutionStrategy, config.useGL30 ? 3 : 2);
			if (configChooser != null)
				view.setEGLConfigChooser(configChooser);
			else
				view.setEGLConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil);
			view.setRenderer(this);
			return view;
		}
	}

	public void onPauseGLSurfaceView () {
		if (view != null) {
			if (view instanceof GLSurfaceViewAPI18) ((GLSurfaceViewAPI18)view).onPause();
			if (view instanceof GLSurfaceView) ((GLSurfaceView)view).onPause();
		}
	}

	public void onResumeGLSurfaceView () {
		if (view != null) {
			if (view instanceof GLSurfaceViewAPI18) ((GLSurfaceViewAPI18)view).onResume();
			if (view instanceof GLSurfaceView) ((GLSurfaceView)view).onResume();
		}
	}

	protected EGLConfigChooser getEglConfigChooser () {
		return new GdxEglConfigChooser(config.r, config.g, config.b, config.a, config.depth, config.stencil, config.numSamples);
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

	@Override
	public int getBackBufferWidth () {
		return width;
	}

	@Override
	public int getBackBufferHeight () {
		return height;
	}

	/** This instantiates the GL10, GL11 and GL20 instances. Includes the check for certain devices that pretend to support GL11
	 * but fuck up vertex buffer objects. This includes the pixelflinger which segfaults when buffers are deleted as well as the
	 * Motorola CLIQ and the Samsung Behold II.
	 *
	 * @param gl */
	private void setupGL (javax.microedition.khronos.opengles.GL10 gl) {
		String versionString = gl.glGetString(GL10.GL_VERSION);
		String vendorString = gl.glGetString(GL10.GL_VENDOR);
		String rendererString = gl.glGetString(GL10.GL_RENDERER);
		glVersion = new GLVersion(Application.ApplicationType.Android, versionString, vendorString, rendererString);
		if (config.useGL30 && glVersion.getMajorVersion() > 2) {
			if (gl30 != null) return;
			gl20 = gl30 = new AndroidGL30();

			Gdx.gl = gl30;
			Gdx.gl20 = gl30;
			Gdx.gl30 = gl30;
		} else {
			if (gl20 != null) return;
			gl20 = new AndroidGL20();

			Gdx.gl = gl20;
			Gdx.gl20 = gl20;
		}

		Gdx.app.log(LOG_TAG, "OGL renderer: " + gl.glGetString(GL10.GL_RENDERER));
		Gdx.app.log(LOG_TAG, "OGL vendor: " + gl.glGetString(GL10.GL_VENDOR));
		Gdx.app.log(LOG_TAG, "OGL version: " + gl.glGetString(GL10.GL_VERSION));
		Gdx.app.log(LOG_TAG, "OGL extensions: " + gl.glGetString(GL10.GL_EXTENSIONS));
	}

	@Override
	public void onSurfaceChanged (javax.microedition.khronos.opengles.GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;
		updatePpi();
		gl.glViewport(0, 0, this.width, this.height);
		if (created == false) {
			app.getApplicationListener().create();
			created = true;
			synchronized (this) {
				running = true;
			}
		}
		app.getApplicationListener().resize(width, height);
	}

	@Override
	public void onSurfaceCreated (javax.microedition.khronos.opengles.GL10 gl, EGLConfig config) {
		eglContext = ((EGL10)EGLContext.getEGL()).eglGetCurrentContext();
		setupGL(gl);
		logConfig(config);
		updatePpi();

		Mesh.invalidateAllMeshes(app);
		Texture.invalidateAllTextures(app);
		Cubemap.invalidateAllCubemaps(app);
		TextureArray.invalidateAllTextureArrays(app);
		ShaderProgram.invalidateAllShaderPrograms(app);
		FrameBuffer.invalidateAllFrameBuffers(app);

		logManagedCachesStatus();

		Display display = app.getWindowManager().getDefaultDisplay();
		this.width = display.getWidth();
		this.height = display.getHeight();
// this.mean = new WindowedMean(5);
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

		Gdx.app.log(LOG_TAG, "framebuffer: (" + r + ", " + g + ", " + b + ", " + a + ")");
		Gdx.app.log(LOG_TAG, "depthbuffer: (" + d + ")");
		Gdx.app.log(LOG_TAG, "stencilbuffer: (" + s + ")");
		Gdx.app.log(LOG_TAG, "samples: (" + samples + ")");
		Gdx.app.log(LOG_TAG, "coverage sampling: (" + coverageSample + ")");

		bufferFormat = new BufferFormat(r, g, b, a, d, s, samples, coverageSample);
	}

	int[] value = new int[1];

	private int getAttrib (EGL10 egl, EGLDisplay display, EGLConfig config, int attrib, int defValue) {
		if (egl.eglGetConfigAttrib(display, config, attrib, value)) {
			return value[0];
		}
		return defValue;
	}

	final Object lock = new Object();

	private long systemNanoTime;

	private long timeDeltaLong;

	void resume () {
		synchronized (lock) {
			running = true;
			resume = true;
		}
	}

	void pause () {
		synchronized (lock) {
			if (!running) return;
			running = false;
			pause = true;
			while (pause) {
				try {
					// TODO: fix deadlock race condition with quick resume/pause.
					// Temporary workaround:
					// Android ANR time is 5 seconds, so wait up to 4 seconds before assuming
					// deadlock and killing process. This can easily be triggered by opening the
					// Recent Apps list and then double-tapping the Recent Apps button with
					// ~500ms between taps.
					lock.wait(4000);
					if (pause) {
						// pause will never go false if onDrawFrame is never called by the GLThread
						// when entering this method, we MUST enforce continuous rendering
						Gdx.app.error(LOG_TAG, "waiting for pause synchronization took too long; assuming deadlock and killing");
						android.os.Process.killProcess(android.os.Process.myPid());
					}
				} catch (InterruptedException ignored) {
					Gdx.app.log(LOG_TAG, "waiting for pause synchronization failed!");
				}
			}
		}
	}

	void destroy () {
		synchronized (lock) {
			running = false;
			destroy = true;

			while (destroy) {
				try {
					lock.wait();
				} catch (InterruptedException ex) {
					Gdx.app.log(LOG_TAG, "waiting for destroy synchronization failed!");
				}
			}
		}
	}

	boolean lrunning_onDrawFrame = false;
	boolean lpause_onDrawFrame = false;
	boolean ldestroy_onDrawFrame = false;
	boolean lresume_onDrawFrame = false;

	int i_onDrawFrame_A;
	int i_onDrawFrame_B;
	int i_onDrawFrame_C;
	int i_onDrawFrame_D;

	private ApplicationListener appListener;

	@Override
	public final void onDrawFrame (final javax.microedition.khronos.opengles.GL10 gl) {
		this.appListener = this.app.getApplicationListener();
		this.systemNanoTime = System.nanoTime();
		this.timeDeltaLong = this.systemNanoTime - this.lastFrameTime;
		this.lastFrameTime = this.systemNanoTime;

		// After pause deltaTime can have somewhat huge value that destabilizes the mean, so let's cut it off
		if (!this.resume) {
		} else {
			this.timeDeltaLong = 0L;
		}

		this.lrunning_onDrawFrame = false;
		this.lpause_onDrawFrame = false;
		this.ldestroy_onDrawFrame = false;
		this.lresume_onDrawFrame = false;

		synchronized (this.lock) {
			this.lrunning_onDrawFrame = this.running;
			this.lpause_onDrawFrame = this.pause;
			this.ldestroy_onDrawFrame = this.destroy;
			this.lresume_onDrawFrame = this.resume;

			if (this.resume) {
				this.resume = false;
			}

			if (this.pause) {
				this.pause = false;
				this.lock.notifyAll();
			}

			if (this.destroy) {
				this.destroy = false;
				this.lock.notifyAll();
			}
		}

		if (this.lresume_onDrawFrame) {
			final SnapshotArray<LifecycleListener> lifecycleListeners = app.getLifecycleListeners();
			synchronized (lifecycleListeners) {
				final LifecycleListener[] listeners = lifecycleListeners.begin();
				for (this.i_onDrawFrame_A = 0; this.i_onDrawFrame_A < lifecycleListeners.size; this.i_onDrawFrame_A++) {
					listeners[i_onDrawFrame_A].resume();
				}
				lifecycleListeners.end();
			}
			this.appListener.resume();
			Gdx.app.log(LOG_TAG, "resumed");
		}

		if (this.lrunning_onDrawFrame) {
			synchronized (this.app.getRunnables()) {
				this.app.getExecutedRunnables().clear();
				this.app.getExecutedRunnables().addAll(this.app.getRunnables());
				this.app.getRunnables().clear();
			}

			for (this.i_onDrawFrame_B = 0; this.i_onDrawFrame_B < this.app.getExecutedRunnables().size; this.i_onDrawFrame_B++) {
				try {
					this.app.getExecutedRunnables().get(this.i_onDrawFrame_B).run();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			this.app.getInput().processEvents();
			this.frameId++;
			this.appListener.render();
		}

		if (this.lpause_onDrawFrame) {
			final SnapshotArray<LifecycleListener> lifecycleListeners = this.app.getLifecycleListeners();
			synchronized (lifecycleListeners) {
				final LifecycleListener[] listeners = lifecycleListeners.begin();
				for (this.i_onDrawFrame_C = 0; this.i_onDrawFrame_C < lifecycleListeners.size; this.i_onDrawFrame_C++) {
					listeners[this.i_onDrawFrame_C].pause();
				}
			}
			this.appListener.pause();
			Gdx.app.log(LOG_TAG, "paused");
		}

		if (this.ldestroy_onDrawFrame) {
			final SnapshotArray<LifecycleListener> lifecycleListeners = this.app.getLifecycleListeners();
			synchronized (lifecycleListeners) {
				final LifecycleListener[] listeners = lifecycleListeners.begin();
				for (this.i_onDrawFrame_D = 0; this.i_onDrawFrame_D < lifecycleListeners.size; this.i_onDrawFrame_D++) {
					listeners[this.i_onDrawFrame_D].dispose();
				}
			}
			this.appListener.dispose();
			Gdx.app.log(LOG_TAG, "destroyed");
		}

		if (this.systemNanoTime - this.frameStart > 1000000000L) {
			this.fps = this.frames;
			this.frames = 0;
			this.frameStart = this.systemNanoTime;
		}
		this.frames++;
	}

	@Override
	public long getFrameId () {
		return frameId;
	}

	/** {@inheritDoc} */
	@Override
	public float getDeltaTime () {
// return mean.getMean() == 0 ? deltaTimeFloat : mean.getMean();
		return this.timeDeltaLong / 1000000000.0f;
	}

	@Override
	public float getRawDeltaTime () {
		return this.timeDeltaLong / 1000000000.0f;
	}

	/** {@inheritDoc} */
	@Override
	public GraphicsType getType () {
		return GraphicsType.AndroidGL;
	}

	/** {@inheritDoc} */
	@Override
	public GLVersion getGLVersion () {
		return glVersion;
	}

	/** {@inheritDoc} */
	@Override
	public int getFramesPerSecond () {
		return fps;
	}

	public void clearManagedCaches () {
		Mesh.clearAllMeshes(app);
		Texture.clearAllTextures(app);
		Cubemap.clearAllCubemaps(app);
		TextureArray.clearAllTextureArrays(app);
		ShaderProgram.clearAllShaderPrograms(app);
		FrameBuffer.clearAllFrameBuffers(app);

		logManagedCachesStatus();
	}

	protected void logManagedCachesStatus () {
		Gdx.app.log(LOG_TAG, Mesh.getManagedStatus());
		Gdx.app.log(LOG_TAG, Texture.getManagedStatus());
		Gdx.app.log(LOG_TAG, Cubemap.getManagedStatus());
		Gdx.app.log(LOG_TAG, ShaderProgram.getManagedStatus());
		Gdx.app.log(LOG_TAG, FrameBuffer.getManagedStatus());
	}

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
	public boolean setFullscreenMode (DisplayMode displayMode) {
		return false;
	}

	@Override
	public Monitor getPrimaryMonitor () {
		return new AndroidMonitor(0, 0, "Primary Monitor");
	}

	@Override
	public Monitor getMonitor () {
		return getPrimaryMonitor();
	}

	@Override
	public Monitor[] getMonitors () {
		return new Monitor[] {getPrimaryMonitor()};
	}

	@Override
	public DisplayMode[] getDisplayModes (Monitor monitor) {
		return getDisplayModes();
	}

	@Override
	public DisplayMode getDisplayMode (Monitor monitor) {
		return getDisplayMode();
	}

	@Override
	public DisplayMode[] getDisplayModes () {
		return new DisplayMode[] {getDisplayMode()};
	}

	@Override
	public boolean setWindowedMode (int width, int height) {
		return false;
	}

	@Override
	public void setTitle (String title) {

	}

	@Override
	public void setUndecorated (boolean undecorated) {
		final int mask = (undecorated) ? 1 : 0;
		app.getApplicationWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, mask);
	}

	@Override
	public void setResizable (boolean resizable) {

	}

	@Override
	public DisplayMode getDisplayMode () {
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
			// ignore setContinuousRendering(false) while pausing
			this.isContinuous = enforceContinuousRendering || isContinuous;
			int renderMode = this.isContinuous ? GLSurfaceView.RENDERMODE_CONTINUOUSLY : GLSurfaceView.RENDERMODE_WHEN_DIRTY;
			if (view instanceof GLSurfaceViewAPI18) ((GLSurfaceViewAPI18)view).setRenderMode(renderMode);
			if (view instanceof GLSurfaceView) ((GLSurfaceView)view).setRenderMode(renderMode);
// mean.clear();
		}
	}

	@Override
	public boolean isContinuousRendering () {
		return isContinuous;
	}

	@Override
	public void requestRendering () {
		if (view != null) {
			if (view instanceof GLSurfaceViewAPI18) ((GLSurfaceViewAPI18)view).requestRender();
			if (view instanceof GLSurfaceView) ((GLSurfaceView)view).requestRender();
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

	@Override
	public Cursor newCursor (Pixmap pixmap, int xHotspot, int yHotspot) {
		return null;
	}

	@Override
	public void setCursor (Cursor cursor) {
	}

	@Override
	public void setSystemCursor (SystemCursor systemCursor) {
	}

	private class AndroidDisplayMode extends DisplayMode {
		protected AndroidDisplayMode (int width, int height, int refreshRate, int bitsPerPixel) {
			super(width, height, refreshRate, bitsPerPixel);
		}
	}

	private class AndroidMonitor extends Monitor {
		public AndroidMonitor (int virtualX, int virtualY, String name) {
			super(virtualX, virtualY, name);
		}
	}
}
