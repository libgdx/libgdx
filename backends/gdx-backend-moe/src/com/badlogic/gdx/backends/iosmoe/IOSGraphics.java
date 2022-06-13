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

import apple.coregraphics.struct.CGPoint;
import apple.coregraphics.struct.CGSize;
import apple.foundation.NSSet;
import com.badlogic.gdx.AbstractGraphics;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.iosrobovm.IOSGLES20;
import com.badlogic.gdx.backends.iosrobovm.IOSGLES30;
import com.badlogic.gdx.backends.iosmoe.custom.HWMachine;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.utils.Array;
import apple.coregraphics.struct.CGRect;
import apple.NSObject;
import apple.glkit.GLKView;
import apple.glkit.GLKViewController;
import apple.glkit.protocol.GLKViewControllerDelegate;
import apple.glkit.protocol.GLKViewDelegate;
import apple.glkit.enums.GLKViewDrawableColorFormat;
import apple.glkit.enums.GLKViewDrawableDepthFormat;
import apple.glkit.enums.GLKViewDrawableMultisample;
import apple.glkit.enums.GLKViewDrawableStencilFormat;
import apple.opengles.EAGLContext;
import apple.opengles.enums.EAGLRenderingAPI;
import apple.uikit.struct.UIEdgeInsets;
import apple.uikit.UIEvent;
import org.moe.natj.general.Pointer;
import org.moe.natj.objc.ObjCRuntime;
import org.moe.natj.objc.ann.Selector;

public class IOSGraphics extends AbstractGraphics {

	private static final String tag = "IOSGraphics";

	IOSApplication app;
	IOSInput input;
	GL20 gl20;
	GL30 gl30;
	IOSScreenBounds screenBounds;
	int safeInsetLeft, safeInsetTop, safeInsetBottom, safeInsetRight;
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

	volatile boolean resume = false;
	volatile boolean appPaused;
	private long frameId = -1;
	private boolean isContinuous = true;
	private boolean isFrameRequested = true;

	IOSApplicationConfiguration config;
	EAGLContext context;
	GLVersion glVersion;
	IOSGLKView view;
	IOSUIViewController viewController;

	public IOSGraphics (IOSApplication app, IOSApplicationConfiguration config, IOSInput input, boolean useGLES30) {
		this.config = config;

		// setup view and OpenGL
		screenBounds = app.computeBounds();

		if (useGLES30) {
			context = EAGLContext.alloc().initWithAPI(EAGLRenderingAPI.GLES3);
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

		IOSViewDelegate viewDelegate = IOSViewDelegate.alloc().init();
		// TODO: 13.06.2022 Solve better
		viewDelegate.setIosGraphics(this);
		view = IOSGLKView.alloc().init(new CGRect(new CGPoint(0, 0), new CGSize(screenBounds.width, screenBounds.height)), context);
		view.setGraphics(this);
		view.setDelegate(viewDelegate);
		view.setDrawableColorFormat(config.colorFormat);
		view.setDrawableDepthFormat(config.depthFormat);
		view.setDrawableStencilFormat(config.stencilFormat);
		view.setDrawableMultisample(config.multisample);
		view.setMultipleTouchEnabled(true);

		viewController = app.createUIViewController(this);
		viewController.setView(view);
		viewController.setDelegate(viewDelegate);
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
		IOSDevice device = config.knownDevices.get(machineString);
		if (device == null) app.error(tag, "Machine ID: " + machineString + " not found, please report to libGDX");
		int ppi = device != null ? device.ppi : app.guessUnknownPpi();
		density = ppi / 160f;
		ppiX = ppi;
		ppiY = ppi;
		ppcX = ppiX / 2.54f;
		ppcY = ppiY / 2.54f;
		app.debug(tag, "Display: ppi=" + ppi + ", density=" + density);

		// time + FPS
		lastFrameTime = System.nanoTime();
		framesStart = lastFrameTime;

		appPaused = false;
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
		resume = true;
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

	public void draw (GLKView view, CGRect rect) {
		makeCurrent();
		// massive hack, GLKView resets the viewport on each draw call, so IOSGLES20
		// stores the last known viewport and we reset it here...
		gl20.glViewport(IOSGLES20.x, IOSGLES20.y, IOSGLES20.width, IOSGLES20.height);

		if (!created) {
			// OpenGL glViewport() function expects backbuffer coordinates instead of logical coordinates
			gl20.glViewport(0, 0, screenBounds.backBufferWidth, screenBounds.backBufferHeight);

			String versionString = gl20.glGetString(GL20.GL_VERSION);
			String vendorString = gl20.glGetString(GL20.GL_VENDOR);
			String rendererString = gl20.glGetString(GL20.GL_RENDERER);
			glVersion = new GLVersion(Application.ApplicationType.iOS, versionString, vendorString, rendererString);

			updateSafeInsets();
			app.listener.create();
			app.listener.resize(getWidth(), getHeight());
			created = true;
		}
		if (appPaused) {
			return;
		}

		long time = System.nanoTime();
		if (!resume) {
			deltaTime = (time - lastFrameTime) / 1000000000.0f;
		} else {
			resume = false;
			deltaTime = 0;
		}
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

	public void update (GLKViewController controller) {
		makeCurrent();
		app.processRunnables();
		// pause the GLKViewController render loop if we are no longer continuous
		// and if we haven't requested a frame in the last loop iteration
		if (!isContinuous && !isFrameRequested) {
			viewController.setPaused(true);
		}
		isFrameRequested = false;
	}

	public void willPause (GLKViewController controller, boolean pause) {
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
		if (config.hdpiMode == HdpiMode.Pixels) {
			return getBackBufferWidth();
		} else {
			return screenBounds.width;
		}
	}

	@Override
	public int getHeight () {
		if (config.hdpiMode == HdpiMode.Pixels) {
			return getBackBufferHeight();
		} else {
			return screenBounds.height;
		}
	}

	@Override
	public int getBackBufferWidth () {
		return screenBounds.backBufferWidth;
	}

	@Override
	public int getBackBufferHeight () {
		return screenBounds.backBufferHeight;
	}

	@Override
	public float getBackBufferScale () {
		return app.pixelsPerPoint;
	}

	@Override
	public float getDeltaTime () {
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

	protected void updateSafeInsets () {
		safeInsetTop = 0;
		safeInsetLeft = 0;
		safeInsetRight = 0;
		safeInsetBottom = 0;

		if (Foundation.getMajorSystemVersion() >= 11) {
			UIEdgeInsets edgeInsets = viewController.view().safeAreaInsets();
			safeInsetTop = (int)edgeInsets.top();
			safeInsetLeft = (int)edgeInsets.left();
			safeInsetRight = (int)edgeInsets.right();
			safeInsetBottom = (int)edgeInsets.bottom();
			if (config.hdpiMode == HdpiMode.Pixels) {
				safeInsetTop *= app.pixelsPerPoint;
				safeInsetLeft *= app.pixelsPerPoint;
				safeInsetRight *= app.pixelsPerPoint;
				safeInsetBottom *= app.pixelsPerPoint;
			}
		}
	}

	@Override
	public int getSafeInsetLeft () {
		return safeInsetLeft;
	}

	@Override
	public int getSafeInsetTop () {
		return safeInsetTop;
	}

	@Override
	public int getSafeInsetBottom () {
		return safeInsetBottom;
	}

	@Override
	public int getSafeInsetRight () {
		return safeInsetRight;
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
	public void setUndecorated (boolean undecorated) {
	}

	@Override
	public void setResizable (boolean resizable) {
	}

	@Override
	public void setVSync (boolean vsync) {
	}

	/** Sets the preferred framerate for the application. Default is 60. Is not generally advised to be used on mobile platforms.
	 *
	 * @param fps the preferred fps */
	@Override
	public void setForegroundFPS (int fps) {
		viewController.setPreferredFramesPerSecond(fps);
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

	private static class IOSViewDelegate extends NSObject implements GLKViewDelegate, GLKViewControllerDelegate {
		protected IOSViewDelegate(Pointer peer) {
			super(peer);
		}

		private IOSGraphics iosGraphics;

		@Selector("alloc")
		public static native IOSViewDelegate alloc();

		@Selector("init")
		public native IOSViewDelegate init();

		public void setIosGraphics(IOSGraphics iosGraphics) {
			this.iosGraphics = iosGraphics;
		}

		@Override
		public void glkViewControllerUpdate (GLKViewController controller) {
			iosGraphics.update(controller);
		}

		@Override
		public void glkViewControllerWillPause (GLKViewController controller, boolean pause) {
			iosGraphics.willPause(controller, pause);
		}

		@Override
		public void glkViewDrawInRect (GLKView view, CGRect rect) {
			iosGraphics.draw(view, rect);
		}
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
