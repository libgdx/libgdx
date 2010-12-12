/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.badlogic.gdx.backends.android.surfaceview;

import java.io.Writer;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.opengl.GLSurfaceView.Renderer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * An implementation of SurfaceView that uses the dedicated surface for displaying OpenGL rendering.
 * <p>
 * A GLSurfaceView provides the following features:
 * <p>
 * <ul>
 * <li>Manages a surface, which is a special piece of memory that can be composited into the Android view system.
 * <li>Manages an EGL display, which enables OpenGL to render into a surface.
 * <li>Accepts a user-provided Renderer object that does the actual rendering.
 * <li>Renders on a dedicated thread to decouple rendering performance from the UI thread.
 * <li>Supports both on-demand and continuous rendering.
 * <li>Optionally wraps, traces, and/or error-checks the renderer's OpenGL calls.
 * </ul>
 * 
 * <h3>Using GLSurfaceView</h3>
 * <p>
 * Typically you use GLSurfaceView by subclassing it and overriding one or more of the View system input event methods. If your
 * application does not need to override event methods then GLSurfaceView can be used as-is. For the most part GLSurfaceView
 * behavior is customized by calling "set" methods rather than by subclassing. For example, unlike a regular View, drawing is
 * delegated to a separate Renderer object which is registered with the GLSurfaceView using the {@link #setRenderer(Renderer)}
 * call.
 * <p>
 * <h3>Initializing GLSurfaceView</h3>
 * All you have to do to initialize a GLSurfaceView is call {@link #setRenderer(Renderer)}. However, if desired, you can modify
 * the default behavior of GLSurfaceView by calling one or more of these methods before calling setRenderer:
 * <ul>
 * <li>{@link #setDebugFlags(int)}
 * <li>{@link #setEGLConfigChooser(boolean)}
 * <li>{@link #setEGLConfigChooser(EGLConfigChooser)}
 * <li>{@link #setEGLConfigChooser(int, int, int, int, int, int)}
 * <li>{@link #setGLWrapper(GLWrapper)}
 * </ul>
 * <p>
 * <h4>Choosing an EGL Configuration</h4>
 * A given Android device may support multiple possible types of drawing surfaces. The available surfaces may differ in how may
 * channels of data are present, as well as how many bits are allocated to each channel. Therefore, the first thing GLSurfaceView
 * has to do when starting to render is choose what type of surface to use.
 * <p>
 * By default GLSurfaceView chooses an available surface that's closest to a 16-bit R5G6B5 surface with a 16-bit depth buffer and
 * no stencil. If you would prefer a different surface (for example, if you do not need a depth buffer) you can override the
 * default behavior by calling one of the setEGLConfigChooser methods.
 * <p>
 * <h4>Debug Behavior</h4>
 * You can optionally modify the behavior of GLSurfaceView by calling one or more of the debugging methods
 * {@link #setDebugFlags(int)}, and {@link #setGLWrapper}. These methods may be called before and/or after setRenderer, but
 * typically they are called before setRenderer so that they take effect immediately.
 * <p>
 * <h4>Setting a Renderer</h4>
 * Finally, you must call {@link #setRenderer} to register a {@link Renderer}. The renderer is responsible for doing the actual
 * OpenGL rendering.
 * <p>
 * <h3>Rendering Mode</h3>
 * Once the renderer is set, you can control whether the renderer draws continuously or on-demand by calling
 * {@link #setRenderMode}. The default is continuous rendering.
 * <p>
 * <h3>Activity Life-cycle</h3>
 * A GLSurfaceView must be notified when the activity is paused and resumed. GLSurfaceView clients are required to call
 * {@link #onPause()} when the activity pauses and {@link #onResume()} when the activity resumes. These calls allow GLSurfaceView
 * to pause and resume the rendering thread, and also allow GLSurfaceView to release and recreate the OpenGL display.
 * <p>
 * <h3>Handling events</h3>
 * <p>
 * To handle an event you will typically subclass GLSurfaceView and override the appropriate method, just as you would with any
 * other View. However, when handling the event, you may need to communicate with the Renderer object that's running in the
 * rendering thread. You can do this using any standard Java cross-thread communication mechanism. In addition, one relatively
 * easy way to communicate with your renderer is to call {@link #queueEvent(Runnable)}. For example:
 * 
 * <pre class="prettyprint">
 * class MyGLSurfaceView extends GLSurfaceView {
 * 
 * 	private MyRenderer mMyRenderer;
 * 
 * 	public void start() {
 *         mMyRenderer = ...;
 *         setRenderer(mMyRenderer);
 *     }
 * 
 * 	public boolean onKeyDown (int keyCode, KeyEvent event) {
 * 		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
 * 			queueEvent(new Runnable() {
 * 				// This method will be called on the rendering
 * 				// thread:
 * 				public void run () {
 * 					mMyRenderer.handleDpadCenter();
 * 				}
 * 			});
 * 			return true;
 * 		}
 * 		return super.onKeyDown(keyCode, event);
 * 	}
 * }
 * 
 * </pre>
 * 
 */
public class GLSurfaceViewCupcake extends SurfaceView implements SurfaceHolder.Callback {
	/**
	 * The renderer only renders when the surface is created, or when {@link #requestRender} is called.
	 * 
	 * @see #getRenderMode()
	 * @see #setRenderMode(int)
	 */
	public final static int RENDERMODE_WHEN_DIRTY = 0;
	/**
	 * The renderer is called continuously to re-render the scene.
	 * 
	 * @see #getRenderMode()
	 * @see #setRenderMode(int)
	 * @see #requestRender()
	 */
	public final static int RENDERMODE_CONTINUOUSLY = 1;

	/**
	 * Check glError() after every GL call and throw an exception if glError indicates that an error has occurred. This can be used
	 * to help track down which OpenGL ES call is causing an error.
	 * 
	 * @see #getDebugFlags
	 * @see #setDebugFlags
	 */
	public final static int DEBUG_CHECK_GL_ERROR = 1;

	/**
	 * Log GL calls to the system log at "verbose" level with tag "GLSurfaceView".
	 * 
	 * @see #getDebugFlags
	 * @see #setDebugFlags
	 */
	public final static int DEBUG_LOG_GL_CALLS = 2;

	/**
	 * Standard View constructor. In order to render something, you must call {@link #setRenderer} to register a renderer.
	 */
	public GLSurfaceViewCupcake (Context context) {
		super(context);
		init();
	}

	/**
	 * Standard View constructor. In order to render something, you must call {@link #setRenderer} to register a renderer.
	 */
	public GLSurfaceViewCupcake (Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init () {
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
		mRenderMode = RENDERMODE_CONTINUOUSLY;
	}

	/**
	 * Set the glWrapper. If the glWrapper is not null, its {@link GLWrapper#wrap(GL)} method is called whenever a surface is
	 * created. A GLWrapper can be used to wrap the GL object that's passed to the renderer. Wrapping a GL object enables examining
	 * and modifying the behavior of the GL calls made by the renderer.
	 * <p>
	 * Wrapping is typically used for debugging purposes.
	 * <p>
	 * The default value is null.
	 * @param glWrapper the new GLWrapper
	 */
	public void setGLWrapper (GLWrapper glWrapper) {
		mGLWrapper = glWrapper;
	}

	/**
	 * Set the debug flags to a new value. The value is constructed by OR-together zero or more of the DEBUG_CHECK_* constants. The
	 * debug flags take effect whenever a surface is created. The default value is zero.
	 * @param debugFlags the new debug flags
	 * @see #DEBUG_CHECK_GL_ERROR
	 * @see #DEBUG_LOG_GL_CALLS
	 */
	public void setDebugFlags (int debugFlags) {
		mDebugFlags = debugFlags;
	}

	/**
	 * Get the current value of the debug flags.
	 * @return the current value of the debug flags.
	 */
	public int getDebugFlags () {
		return mDebugFlags;
	}

	/**
	 * Set the renderer associated with this view. Also starts the thread that will call the renderer, which in turn causes the
	 * rendering to start.
	 * <p>
	 * This method should be called once and only once in the life-cycle of a GLSurfaceView.
	 * <p>
	 * The following GLSurfaceView methods can only be called <em>before</em> setRenderer is called:
	 * <ul>
	 * <li>{@link #setEGLConfigChooser(boolean)}
	 * <li>{@link #setEGLConfigChooser(EGLConfigChooser)}
	 * <li>{@link #setEGLConfigChooser(int, int, int, int, int, int)}
	 * </ul>
	 * <p>
	 * The following GLSurfaceView methods can only be called <em>after</em> setRenderer is called:
	 * <ul>
	 * <li>{@link #getRenderMode()}
	 * <li>{@link #onPause()}
	 * <li>{@link #onResume()}
	 * <li>{@link #queueEvent(Runnable)}
	 * <li>{@link #requestRender()}
	 * <li>{@link #setRenderMode(int)}
	 * </ul>
	 * 
	 * @param renderer the renderer to use to perform OpenGL drawing.
	 */
	public void setRenderer (Renderer renderer) {
		if (mRenderer != null) {
			throw new IllegalStateException("setRenderer has already been called for this instance.");
		}

		mRenderer = renderer;
	}

	/**
	 * Install a custom EGLConfigChooser.
	 * <p>
	 * If this method is called, it must be called before {@link #setRenderer(Renderer)} is called.
	 * <p>
	 * If no setEGLConfigChooser method is called, then by default the view will choose a config as close to 16-bit RGB as
	 * possible, with a depth buffer as close to 16 bits as possible.
	 * @param configChooser
	 */
	public void setEGLConfigChooser (EGLConfigChooser configChooser) {
		if (mRenderer != null) {
			throw new IllegalStateException("setRenderer has already been called for this instance.");
		}
		mEGLConfigChooser = configChooser;
	}

	/**
	 * Install a config chooser which will choose a config as close to 16-bit RGB as possible, with or without an optional depth
	 * buffer as close to 16-bits as possible.
	 * <p>
	 * If this method is called, it must be called before {@link #setRenderer(Renderer)} is called.
	 * <p>
	 * If no setEGLConfigChooser method is called, then by default the view will choose a config as close to 16-bit RGB as
	 * possible, with a depth buffer as close to 16 bits as possible.
	 * 
	 * @param needDepth
	 */
	public void setEGLConfigChooser (boolean needDepth) {
		setEGLConfigChooser(new SimpleEGLConfigChooser(needDepth));
	}

	/**
	 * Install a config chooser which will choose a config with at least the specified component sizes, and as close to the
	 * specified component sizes as possible.
	 * <p>
	 * If this method is called, it must be called before {@link #setRenderer(Renderer)} is called.
	 * <p>
	 * If no setEGLConfigChooser method is called, then by default the view will choose a config as close to 16-bit RGB as
	 * possible, with a depth buffer as close to 16 bits as possible.
	 * 
	 */
	public void setEGLConfigChooser (int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
		setEGLConfigChooser(new ComponentSizeChooser(redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize));
	}

	/**
	 * Set the rendering mode. When renderMode is RENDERMODE_CONTINUOUSLY, the renderer is called repeatedly to re-render the
	 * scene. When renderMode is RENDERMODE_WHEN_DIRTY, the renderer only rendered when the surface is created, or when
	 * {@link #requestRender} is called. Defaults to RENDERMODE_CONTINUOUSLY.
	 * <p>
	 * Using RENDERMODE_WHEN_DIRTY can improve battery life and overall system performance by allowing the GPU and CPU to idle when
	 * the view does not need to be updated.
	 * <p>
	 * This method can only be called after {@link #setRenderer(Renderer)}
	 * 
	 * @param renderMode one of the RENDERMODE_X constants
	 * @see #RENDERMODE_CONTINUOUSLY
	 * @see #RENDERMODE_WHEN_DIRTY
	 */
	public void setRenderMode (int renderMode) {
		mRenderMode = renderMode;
		if (mGLThread != null) {
			mGLThread.setRenderMode(renderMode);
		}
	}

	/**
	 * Get the current rendering mode. May be called from any thread. Must not be called before a renderer has been set.
	 * @return the current rendering mode.
	 * @see #RENDERMODE_CONTINUOUSLY
	 * @see #RENDERMODE_WHEN_DIRTY
	 */
	public int getRenderMode () {
		return mRenderMode;
	}

	/**
	 * Request that the renderer render a frame. This method is typically used when the render mode has been set to
	 * {@link #RENDERMODE_WHEN_DIRTY}, so that frames are only rendered on demand. May be called from any thread. Must be called
	 * after onResume() and before onPause().
	 */
	public void requestRender () {
		mGLThread.requestRender();
	}

	/**
	 * This method is part of the SurfaceHolder.Callback interface, and is not normally called or subclassed by clients of
	 * GLSurfaceView.
	 */
	public void surfaceCreated (SurfaceHolder holder) {
		if (mGLThread != null) {
			mGLThread.surfaceCreated();
		}
		mHasSurface = true;
	}

	/**
	 * This method is part of the SurfaceHolder.Callback interface, and is not normally called or subclassed by clients of
	 * GLSurfaceView.
	 */
	public void surfaceDestroyed (SurfaceHolder holder) {
		// Surface will be destroyed when we return
		if (mGLThread != null) {
			mGLThread.surfaceDestroyed();
		}
		mHasSurface = false;
	}

	/**
	 * This method is part of the SurfaceHolder.Callback interface, and is not normally called or subclassed by clients of
	 * GLSurfaceView.
	 */
	public void surfaceChanged (SurfaceHolder holder, int format, int w, int h) {
		if (mGLThread != null) {
			mGLThread.onWindowResize(w, h);
		}
		mSurfaceWidth = w;
		mSurfaceHeight = h;
	}

	/**
	 * Inform the view that the activity is paused. The owner of this view must call this method when the activity is paused.
	 * Calling this method will pause the rendering thread. Must not be called before a renderer has been set.
	 */
	public void onPause () {
		mGLThread.onPause();
		mGLThread.requestExitAndWait();
		mGLThread = null;
	}

	/**
	 * Inform the view that the activity is resumed. The owner of this view must call this method when the activity is resumed.
	 * Calling this method will recreate the OpenGL display and resume the rendering thread. Must not be called before a renderer
	 * has been set.
	 */
	public void onResume () {
		if (mEGLConfigChooser == null) {
			mEGLConfigChooser = new SimpleEGLConfigChooser(true);
		}
		mGLThread = new GLThread(mRenderer);
		mGLThread.start();
		mGLThread.setRenderMode(mRenderMode);
		if (mHasSurface) {
			mGLThread.surfaceCreated();
		}
		if (mSurfaceWidth > 0 && mSurfaceHeight > 0) {
			mGLThread.onWindowResize(mSurfaceWidth, mSurfaceHeight);
		}
		mGLThread.onResume();
	}

	/**
	 * Queue a runnable to be run on the GL rendering thread. This can be used to communicate with the Renderer on the rendering
	 * thread. Must be called after onResume() and before onPause().
	 * @param r the runnable to be run on the GL rendering thread.
	 */
	public void queueEvent (Runnable r) {
		if (mGLThread != null) {
			mGLThread.queueEvent(r);
		}
	}

	// ----------------------------------------------------------------------

	/**
	 * An interface used to wrap a GL interface.
	 * <p>
	 * Typically used for implementing debugging and tracing on top of the default GL interface. You would typically use this by
	 * creating your own class that implemented all the GL methods by delegating to another GL instance. Then you could add your
	 * own behavior before or after calling the delegate. All the GLWrapper would do was instantiate and return the wrapper GL
	 * instance:
	 * 
	 * <pre class="prettyprint">
	 * class MyGLWrapper implements GLWrapper {
	 *     GL wrap(GL gl) {
	 *         return new MyGLImplementation(gl);
	 *     }
	 *     static class MyGLImplementation implements GL,GL10,GL11,... {
	 *         ...
	 *     }
	 * }
	 * </pre>
	 * @see #setGLWrapper(GLWrapper)
	 */
	public interface GLWrapper {
		/**
		 * Wraps a gl interface in another gl interface.
		 * @param gl a GL interface that is to be wrapped.
		 * @return either the input argument or another GL object that wraps the input argument.
		 */
		GL wrap (GL gl);
	}

	private static abstract class BaseConfigChooser implements EGLConfigChooser {
		public BaseConfigChooser (int[] configSpec) {
			mConfigSpec = configSpec;
		}

		public EGLConfig chooseConfig (EGL10 egl, EGLDisplay display) {
			int[] num_config = new int[1];
			egl.eglChooseConfig(display, mConfigSpec, null, 0, num_config);

			int numConfigs = num_config[0];

			if (numConfigs <= 0) {
				throw new IllegalArgumentException("No configs match configSpec");
			}

			EGLConfig[] configs = new EGLConfig[numConfigs];
			egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs, num_config);
			EGLConfig config = chooseConfig(egl, display, configs);
			if (config == null) {
				throw new IllegalArgumentException("No config chosen");
			}
			return config;
		}

		abstract EGLConfig chooseConfig (EGL10 egl, EGLDisplay display, EGLConfig[] configs);

		protected int[] mConfigSpec;
	}

	private static class ComponentSizeChooser extends BaseConfigChooser {
		public ComponentSizeChooser (int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
			super(
				new int[] {EGL10.EGL_RED_SIZE, redSize, EGL10.EGL_GREEN_SIZE, greenSize, EGL10.EGL_BLUE_SIZE, blueSize,
					EGL10.EGL_ALPHA_SIZE, alphaSize, EGL10.EGL_DEPTH_SIZE, depthSize, EGL10.EGL_STENCIL_SIZE, stencilSize,
					EGL10.EGL_NONE});
			mValue = new int[1];
			mRedSize = redSize;
			mGreenSize = greenSize;
			mBlueSize = blueSize;
			mAlphaSize = alphaSize;
			mDepthSize = depthSize;
			mStencilSize = stencilSize;
		}

		@Override public EGLConfig chooseConfig (EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
			EGLConfig closestConfig = null;
			int closestDistance = 1000;
			for (EGLConfig config : configs) {
				int r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
				int g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
				int b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
				int a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);
				int d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
				int s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
				int distance = Math.abs(r - mRedSize) + Math.abs(g - mGreenSize) + Math.abs(b - mBlueSize) + Math.abs(a - mAlphaSize)
					+ Math.abs(d - mDepthSize) + Math.abs(s - mStencilSize);
				if (distance < closestDistance) {
					closestDistance = distance;
					closestConfig = config;
				}
			}
			return closestConfig;
		}

		private int findConfigAttrib (EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {

			if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
				return mValue[0];
			}
			return defaultValue;
		}

		private int[] mValue;
		// Subclasses can adjust these values:
		protected int mRedSize;
		protected int mGreenSize;
		protected int mBlueSize;
		protected int mAlphaSize;
		protected int mDepthSize;
		protected int mStencilSize;
	}

	/**
	 * This class will choose a supported surface as close to RGB565 as possible, with or without a depth buffer.
	 * 
	 */
	private static class SimpleEGLConfigChooser extends ComponentSizeChooser {
		public SimpleEGLConfigChooser (boolean withDepthBuffer) {
			super(4, 4, 4, 0, withDepthBuffer ? 16 : 0, 0);
			// Adjust target values. This way we'll accept a 4444 or
			// 555 buffer if there's no 565 buffer available.
			mRedSize = 5;
			mGreenSize = 6;
			mBlueSize = 5;
		}
	}

	/**
	 * An EGL helper class.
	 */

	private class EglHelper {
		public EglHelper () {

		}

		/**
		 * Initialize EGL for a given configuration spec.
		 */
		public void start () {
			/*
			 * Get an EGL instance
			 */
			mEgl = (EGL10)EGLContext.getEGL();

			/*
			 * Get to the default display.
			 */
			mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

			/*
			 * We can now initialize EGL for that display
			 */
			int[] version = new int[2];
			mEgl.eglInitialize(mEglDisplay, version);
			mEglConfig = mEGLConfigChooser.chooseConfig(mEgl, mEglDisplay);

			/*
			 * Create an OpenGL ES context. This must be done only once, an OpenGL context is a somewhat heavy object.
			 */
			mEglContext = mEgl.eglCreateContext(mEglDisplay, mEglConfig, EGL10.EGL_NO_CONTEXT, null);

			mEglSurface = null;
		}

		/*
		 * React to the creation of a new surface by creating and returning an OpenGL interface that renders to that surface.
		 */
		public GL createSurface (SurfaceHolder holder) {
			/*
			 * The window size has changed, so we need to create a new surface.
			 */
			if (mEglSurface != null) {

				/*
				 * Unbind and destroy the old EGL surface, if there is one.
				 */
				mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
				mEgl.eglDestroySurface(mEglDisplay, mEglSurface);
			}

			/*
			 * Create an EGL surface we can render into.
			 */
			mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, mEglConfig, holder, null);

			/*
			 * Before we can issue GL commands, we need to make sure the context is current and bound to a surface.
			 */
			mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext);

			GL gl = mEglContext.getGL();
			if (mGLWrapper != null) {
				gl = mGLWrapper.wrap(gl);
			}

			/* Debugging disabled */
			/*
			 * if ((mDebugFlags & (DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS))!= 0) { int configFlags = 0; Writer log = null; if
			 * ((mDebugFlags & DEBUG_CHECK_GL_ERROR) != 0) { configFlags |= GLDebugHelper.CONFIG_CHECK_GL_ERROR; } if ((mDebugFlags &
			 * DEBUG_LOG_GL_CALLS) != 0) { log = new LogWriter(); } gl = GLDebugHelper.wrap(gl, configFlags, log); }
			 */
			return gl;
		}

		/**
		 * Display the current render surface.
		 * @return false if the context has been lost.
		 */
		public boolean swap () {
			mEgl.eglSwapBuffers(mEglDisplay, mEglSurface);

			/*
			 * Always check for EGL_CONTEXT_LOST, which means the context and all associated data were lost (For instance because the
			 * device went to sleep). We need to sleep until we get a new surface.
			 */
			return mEgl.eglGetError() != EGL11.EGL_CONTEXT_LOST;
		}

		public void finish () {
			if (mEglSurface != null) {
				mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
				mEgl.eglDestroySurface(mEglDisplay, mEglSurface);
				mEglSurface = null;
			}
			if (mEglContext != null) {
				mEgl.eglDestroyContext(mEglDisplay, mEglContext);
				mEglContext = null;
			}
			if (mEglDisplay != null) {
				mEgl.eglTerminate(mEglDisplay);
				mEglDisplay = null;
			}
		}

		EGL10 mEgl;
		EGLDisplay mEglDisplay;
		EGLSurface mEglSurface;
		EGLConfig mEglConfig;
		EGLContext mEglContext;
	}

	/**
	 * A generic GL Thread. Takes care of initializing EGL and GL. Delegates to a Renderer instance to do the actual drawing. Can
	 * be configured to render continuously or on request.
	 * 
	 */
	class GLThread extends Thread {
		GLThread (Renderer renderer) {
			super();
			mDone = false;
			mWidth = 0;
			mHeight = 0;
			mRequestRender = true;
			mRenderMode = RENDERMODE_CONTINUOUSLY;
			mRenderer = renderer;
			mSizeChanged = true;
			setName("GLThread");
		}

		@Override public void run () {
			/*
			 * When the android framework launches a second instance of an activity, the new instance's onCreate() method may be
			 * called before the first instance returns from onDestroy().
			 * 
			 * This semaphore ensures that only one instance at a time accesses EGL.
			 */
			try {
				try {
					sEglSemaphore.acquire();
				} catch (InterruptedException e) {
					return;
				}
				guardedRun();
			} catch (InterruptedException e) {
				// fall thru and exit normally
			} finally {
				sEglSemaphore.release();
			}
		}

		private void guardedRun () throws InterruptedException {
			mEglHelper = new EglHelper();
			mEglHelper.start();

			GL10 gl = null;
			boolean tellRendererSurfaceCreated = true;
			boolean tellRendererSurfaceChanged = true;

			/*
			 * This is our main activity thread's loop, we go until asked to quit.
			 */
			while (!mDone) {

				/*
				 * Update the asynchronous state (window size)
				 */
				int w, h;
				boolean changed;
				boolean needStart = false;
				synchronized (this) {
					Runnable r;
					while ((r = getEvent()) != null) {
						r.run();
					}
					if (mPaused) {
						mEglHelper.finish();
						needStart = true;
					}
					while (needToWait()) {
						wait();
					}
					if (mDone) {
						break;
					}
					changed = mSizeChanged;
					w = mWidth;
					h = mHeight;
					mSizeChanged = false;
					mRequestRender = false;
				}
				if (needStart) {
					mEglHelper.start();
					tellRendererSurfaceCreated = true;
					changed = true;
				}
				if (changed) {
					gl = (GL10)mEglHelper.createSurface(getHolder());
					tellRendererSurfaceChanged = true;
				}
				if (tellRendererSurfaceCreated) {
					mRenderer.onSurfaceCreated(gl, mEglHelper.mEglConfig);
					tellRendererSurfaceCreated = false;
				}
				if (tellRendererSurfaceChanged) {
					mRenderer.onSurfaceChanged(gl, w, h);
					tellRendererSurfaceChanged = false;
				}
				if ((w > 0) && (h > 0)) {
					/* draw a frame here */
					mRenderer.onDrawFrame(gl);

					/*
					 * Once we're done with GL, we need to call swapBuffers() to instruct the system to display the rendered frame
					 */
					mEglHelper.swap();
				}
			}

			/*
			 * clean-up everything...
			 */
			mEglHelper.finish();
		}

		private boolean needToWait () {
			if (mDone) {
				return false;
			}

			if (mPaused || (!mHasSurface)) {
				return true;
			}

			if ((mWidth > 0) && (mHeight > 0) && (mRequestRender || (mRenderMode == RENDERMODE_CONTINUOUSLY))) {
				return false;
			}

			return true;
		}

		public void setRenderMode (int renderMode) {
			if (!((RENDERMODE_WHEN_DIRTY <= renderMode) && (renderMode <= RENDERMODE_CONTINUOUSLY))) {
				throw new IllegalArgumentException("renderMode");
			}
			synchronized (this) {
				mRenderMode = renderMode;
				if (renderMode == RENDERMODE_CONTINUOUSLY) {
					notify();
				}
			}
		}

		public int getRenderMode () {
			synchronized (this) {
				return mRenderMode;
			}
		}

		public void requestRender () {
			synchronized (this) {
				mRequestRender = true;
				notify();
			}
		}

		public void surfaceCreated () {
			synchronized (this) {
				mHasSurface = true;
				notify();
			}
		}

		public void surfaceDestroyed () {
			synchronized (this) {
				mHasSurface = false;
				notify();
			}
		}

		public void onPause () {
			synchronized (this) {
				mPaused = true;
			}
		}

		public void onResume () {
			synchronized (this) {
				mPaused = false;
				notify();
			}
		}

		public void onWindowResize (int w, int h) {
			synchronized (this) {
				mWidth = w;
				mHeight = h;
				mSizeChanged = true;
				notify();
			}
		}

		public void requestExitAndWait () {
			// don't call this from GLThread thread or it is a guaranteed
			// deadlock!
			synchronized (this) {
				mDone = true;
				notify();
			}
			try {
				join();
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}

		/**
		 * Queue an "event" to be run on the GL rendering thread.
		 * @param r the runnable to be run on the GL rendering thread.
		 */
		public void queueEvent (Runnable r) {
			synchronized (this) {
				mEventQueue.add(r);
			}
		}

		private Runnable getEvent () {
			synchronized (this) {
				if (mEventQueue.size() > 0) {
					return mEventQueue.remove(0);
				}

			}
			return null;
		}

		private boolean mDone;
		private boolean mPaused;
		private boolean mHasSurface;
		private int mWidth;
		private int mHeight;
		private int mRenderMode;
		private boolean mRequestRender;
		private Renderer mRenderer;
		private ArrayList<Runnable> mEventQueue = new ArrayList<Runnable>();
		private EglHelper mEglHelper;
		private boolean mSizeChanged;
	}

	static class LogWriter extends Writer {

		@Override public void close () {
			flushBuilder();
		}

		@Override public void flush () {
			flushBuilder();
		}

		@Override public void write (char[] buf, int offset, int count) {
			for (int i = 0; i < count; i++) {
				char c = buf[offset + i];
				if (c == '\n') {
					flushBuilder();
				} else {
					mBuilder.append(c);
				}
			}
		}

		private void flushBuilder () {
			if (mBuilder.length() > 0) {
				Log.v("GLSurfaceView", mBuilder.toString());
				mBuilder.delete(0, mBuilder.length());
			}
		}

		private StringBuilder mBuilder = new StringBuilder();
	}

	static final Semaphore sEglSemaphore = new Semaphore(1);

	private GLThread mGLThread;
	EGLConfigChooser mEGLConfigChooser;
	GLWrapper mGLWrapper;
	private int mDebugFlags;
	private int mRenderMode;
	private Renderer mRenderer;
	private int mSurfaceWidth;
	private int mSurfaceHeight;
	private boolean mHasSurface;
}
