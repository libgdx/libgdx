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

package com.badlogic.gdx.backends.iosmoe;

import org.moe.natj.general.NatJ;
import org.moe.natj.general.Pointer;
import org.moe.natj.general.ann.ByValue;
import org.moe.natj.objc.ann.Selector;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.iosmoe.custom.HWMachine;
import com.badlogic.gdx.backends.iosrobovm.IOSGLES20;
import com.badlogic.gdx.backends.iosrobovm.IOSGLES30;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.utils.Array;

import apple.NSObject;
import apple.coregraphics.struct.CGPoint;
import apple.coregraphics.struct.CGRect;
import apple.coregraphics.struct.CGSize;
import apple.glkit.GLKView;
import apple.glkit.GLKViewController;
import apple.glkit.enums.GLKViewDrawableColorFormat;
import apple.glkit.enums.GLKViewDrawableDepthFormat;
import apple.glkit.enums.GLKViewDrawableMultisample;
import apple.glkit.enums.GLKViewDrawableStencilFormat;
import apple.glkit.protocol.GLKViewControllerDelegate;
import apple.glkit.protocol.GLKViewDelegate;
import apple.opengles.EAGLContext;
import apple.opengles.enums.EAGLRenderingAPI;

public class IOSGraphics extends NSObject implements Graphics, GLKViewDelegate, GLKViewControllerDelegate {

	private static final String tag = "IOSGraphics";

	static {
		NatJ.register();
	}

	IOSApplication app;
	IOSInput input;
	GL20 gl20;
	GL30 gl30;
	int width;
	int height;
	long lastFrameTime;
	float deltaTime;
	long framesStart;
	int frames;
	int fps;
	BufferFormat bufferFormat;
	String extensions;

	private float ppiX = 0;
	private float ppiY = 0;
	private float ppcX = 0;
	private float ppcY = 0;
	private float density = 1;

	volatile boolean appPaused;
	private long frameId = -1;
	private boolean isContinuous = true;
	private boolean isFrameRequested = true;

	IOSApplicationConfiguration config;
	EAGLContext context;
	GLVersion glVersion;
	IOSGLKView view;
	IOSUIViewController viewController;

	@Selector("alloc")
	public static native IOSGraphics alloc ();

	@Selector("init")
	public native IOSGraphics init ();

	protected IOSGraphics (Pointer peer) {
		super(peer);
	}

	public IOSGraphics init (float scale, IOSApplication app, IOSApplicationConfiguration config, IOSInput input,
		boolean useGLES30, IOSGLKView view) {
		this.view = view;

		init();
		this.config = config;

		final CGRect bounds = app.getBounds();
		// setup view and OpenGL
		width = (int)bounds.size().width();
		height = (int)bounds.size().height();

		if (useGLES30) {
			context = EAGLContext.alloc().initWithAPI(EAGLRenderingAPI.GLES3);
			;
			if (context != null)
				gl20 = gl30 = new IOSGLES30();
			else
				Gdx.app.log("IOGraphics", "OpenGL ES 3.0 not supported, falling back on 2.0");
		}
		if (context == null) {
			context = EAGLContext.alloc().initWithAPI(EAGLRenderingAPI.GLES2);
			gl20 = new IOSGLES20();
			gl30 = null;
		}

		view.setContext(context);
		view.setGraphics(this);

		view.setDelegate(this);
		view.setDrawableColorFormat(config.colorFormat);
		view.setDrawableDepthFormat(config.depthFormat);
		view.setDrawableStencilFormat(config.stencilFormat);
		view.setDrawableMultisample(config.multisample);
		view.setMultipleTouchEnabled(true);

		viewController = IOSUIViewController.alloc().init(app, this);
		viewController.setView(view);
		viewController.setDelegate(this);
		viewController.setPreferredFramesPerSecond(config.preferredFramesPerSecond);

		this.app = app;
		this.input = input;

		int r = 0, g = 0, b = 0, a = 0, depth = 0, stencil = 0, samples = 0;
		if (config.colorFormat == GLKViewDrawableColorFormat.RGB565) {
			r = 5;
			g = 6;
			b = 5;
			a = 0;
		} else {
			r = g = b = a = 8;
		}
		if (config.depthFormat == GLKViewDrawableDepthFormat.Format16) {
			depth = 16;
		} else if (config.depthFormat == GLKViewDrawableDepthFormat.Format24) {
			depth = 24;
		} else {
			depth = 0;
		}
		if (config.stencilFormat == GLKViewDrawableStencilFormat.Format8) {
			stencil = 8;
		}
		if (config.multisample == GLKViewDrawableMultisample.Multisample4X) {
			samples = 4;
		}
		bufferFormat = new BufferFormat(r, g, b, a, depth, stencil, samples, false);

		String machineString = HWMachine.getMachineString();
		IOSDevice device = IOSDevice.getDevice(machineString);
		if (device == null) app.error(tag, "Machine ID: " + machineString + " not found, please report to LibGDX");
		int ppi = device != null ? device.ppi : 163;
		density = device != null ? device.ppi / 160f : scale;
		ppiX = ppi;
		ppiY = ppi;
		ppcX = ppiX / 2.54f;
		ppcY = ppiY / 2.54f;
		app.debug(tag, "Display: ppi=" + ppi + ", density=" + density);

		// time + FPS
		lastFrameTime = System.nanoTime();
		framesStart = lastFrameTime;

		appPaused = false;
		return this;
	}

	public IOSGraphics init (float scale, IOSApplication app, IOSApplicationConfiguration config, IOSInput input,
		boolean useGLES30) {
		CGRect bounds = app.getBounds();
		IOSGLKView view = IOSGLKView.alloc()
			.init(new CGRect(new CGPoint(0, 0), new CGSize(bounds.size().width(), bounds.size().height())));
		return init(scale, app, config, input, useGLES30, view);
	}

	public void resume () {
		if (!appPaused) return;
		appPaused = false;

		Array<LifecycleListener> listeners = app.lifecycleListeners;
		synchronized (listeners) {
			for (LifecycleListener listener : listeners) {
				listener.resume();
			}
		}
		app.listener.resume();
	}

	public void pause () {
		if (appPaused) return;
		appPaused = true;

		Array<LifecycleListener> listeners = app.lifecycleListeners;
		synchronized (listeners) {
			for (LifecycleListener listener : listeners) {
				listener.pause();
			}
		}
		app.listener.pause();
	}

	boolean created = false;

	@Override
	public void glkViewDrawInRect (GLKView view, @ByValue CGRect rect) {
		makeCurrent();
		// massive hack, GLKView resets the viewport on each draw call, so IOSGLES20
		// stores the last known viewport and we reset it here...
		gl20.glViewport(IOSGLES20.x, IOSGLES20.y, IOSGLES20.width, IOSGLES20.height);

		if (!created) {
			gl20.glViewport(0, 0, width, height);

			String versionString = gl20.glGetString(GL20.GL_VERSION);
			String vendorString = gl20.glGetString(GL20.GL_VENDOR);
			String rendererString = gl20.glGetString(GL20.GL_RENDERER);
			glVersion = new GLVersion(Application.ApplicationType.iOS, versionString, vendorString, rendererString);

			app.listener.create();
			app.listener.resize(width, height);
			created = true;
		}
		if (appPaused) {
			return;
		}

		long time = System.nanoTime();
		deltaTime = (time - lastFrameTime) / 1000000000.0f;
		lastFrameTime = time;

		frames++;
		if (time - framesStart >= 1000000000l) {
			framesStart = time;
			fps = frames;
			frames = 0;
		}

		input.processEvents();
		frameId++;
		app.listener.render();
	}

	void makeCurrent () {
		EAGLContext.setCurrentContext(context);
	}

	@Override
	public void glkViewControllerUpdate (GLKViewController glkViewController) {
		makeCurrent();
		app.processRunnables();
		// pause the GLKViewController render loop if we are no longer continuous
		// and if we haven't requested a frame in the last loop iteration
		if (!isContinuous && !isFrameRequested) {
			viewController.setPaused(true);
		}
		isFrameRequested = false;
	}

	@Override
	public void glkViewControllerWillPause (GLKViewController controller, boolean pause) {

	}

	@Override
	public GL20 getGL20 () {
		return gl20;
	}

	@Override
	public void setGL20 (GL20 gl20) {
		this.gl20 = gl20;
		if (gl30 == null) {
			Gdx.gl = gl20;
			Gdx.gl20 = gl20;
		}
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
	public void setGL30 (GL30 gl30) {
		this.gl30 = gl30;
		if (gl30 != null) {
			this.gl20 = gl30;

			Gdx.gl = gl20;
			Gdx.gl20 = gl20;
			Gdx.gl30 = gl30;
		}
	}

	@Override
	public int getWidth () {
		return width;
	}

	@Override
	public int getHeight () {
		return height;
	}

	@Override
	public int getBackBufferWidth () {
		return width;
	}

	@Override
	public int getBackBufferHeight () {
		return height;
	}

	@Override
	public float getDeltaTime () {
		return deltaTime;
	}

	@Override
	public float getRawDeltaTime () {
		return deltaTime;
	}

	@Override
	public int getFramesPerSecond () {
		return fps;
	}

	@Override
	public GraphicsType getType () {
		return GraphicsType.iOSGL;
	}

	@Override
	public GLVersion getGLVersion () {
		return glVersion;
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
	public DisplayMode[] getDisplayModes () {
		return new DisplayMode[] {getDisplayMode()};
	}

	@Override
	public DisplayMode getDisplayMode () {
		return new IOSDisplayMode(getWidth(), getHeight(), config.preferredFramesPerSecond,
			bufferFormat.r + bufferFormat.g + bufferFormat.b + bufferFormat.a);
	}

	@Override
	public Monitor getPrimaryMonitor () {
		return new IOSMonitor(0, 0, "Primary Monitor");
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
	public boolean setFullscreenMode (DisplayMode displayMode) {
		return false;
	}

	@Override
	public boolean setWindowedMode (int width, int height) {
		return false;
	}

	@Override
	public void setTitle (String title) {
	}

	@Override
	public void setVSync (boolean vsync) {
	}

	@Override
	public BufferFormat getBufferFormat () {
		return bufferFormat;
	}

	@Override
	public boolean supportsExtension (String extension) {
		if (extensions == null) extensions = Gdx.gl.glGetString(GL20.GL_EXTENSIONS);
		return extensions.contains(extension);
	}

	@Override
	public void setContinuousRendering (boolean isContinuous) {
		if (isContinuous != this.isContinuous) {
			this.isContinuous = isContinuous;
			// start the GLKViewController if we go from non-continuous -> continuous
			if (isContinuous) viewController.setPaused(false);
		}
	}

	@Override
	public boolean isContinuousRendering () {
		return isContinuous;
	}

	@Override
	public void requestRendering () {
		isFrameRequested = true;
		// start the GLKViewController if we are in non-continuous mode
		// (we should already be started in continuous mode)
		if (!isContinuous) viewController.setPaused(false);
	}

	@Override
	public boolean isFullscreen () {
		return true;
	}

	@Override
	public long getFrameId () {
		return frameId;
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

	@Override
	public void setResizable (boolean resizable) {
	}

	@Override
	public void setUndecorated (boolean undecorated) {
	}

	private class IOSDisplayMode extends DisplayMode {
		protected IOSDisplayMode (int width, int height, int refreshRate, int bitsPerPixel) {
			super(width, height, refreshRate, bitsPerPixel);
		}
	}

	private class IOSMonitor extends Monitor {
		protected IOSMonitor (int virtualX, int virtualY, String name) {
			super(virtualX, virtualY, name);
		}
	}
}
