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

package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.iosrobovm.custom.HWMachine;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.utils.Array;

import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSObject;
import org.robovm.apple.glkit.GLKView;
import org.robovm.apple.glkit.GLKViewController;
import org.robovm.apple.glkit.GLKViewControllerDelegate;
import org.robovm.apple.glkit.GLKViewDelegate;
import org.robovm.apple.glkit.GLKViewDrawableColorFormat;
import org.robovm.apple.glkit.GLKViewDrawableDepthFormat;
import org.robovm.apple.glkit.GLKViewDrawableMultisample;
import org.robovm.apple.glkit.GLKViewDrawableStencilFormat;
import org.robovm.apple.opengles.EAGLContext;
import org.robovm.apple.opengles.EAGLRenderingAPI;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIEdgeInsets;
import org.robovm.apple.uikit.UIEvent;
import org.robovm.apple.uikit.UIInterfaceOrientation;
import org.robovm.apple.uikit.UIInterfaceOrientationMask;
import org.robovm.apple.uikit.UIRectEdge;
import org.robovm.apple.uikit.UIView;
import org.robovm.objc.Selector;
import org.robovm.objc.annotation.BindSelector;
import org.robovm.objc.annotation.Method;
import org.robovm.rt.bro.annotation.Callback;
import org.robovm.rt.bro.annotation.Pointer;

public class IOSGraphics extends NSObject implements Graphics, GLKViewDelegate, GLKViewControllerDelegate {

	private static final String tag = "IOSGraphics";

	public static class IOSUIViewController extends GLKViewController {
		final IOSApplication app;
		final IOSGraphics graphics;
		boolean created = false;

		protected IOSUIViewController (IOSApplication app, IOSGraphics graphics) {
			this.app = app;
			this.graphics = graphics;
		}

		@Override
		public void viewWillAppear (boolean arg0) {
			super.viewWillAppear(arg0);
			// start GLKViewController even though we may only draw a single frame
			// (we may be in non-continuous mode)
			setPaused(false);
		}

		@Override
		public void viewDidAppear (boolean animated) {
			super.viewDidAppear(animated);
			if (app.viewControllerListener != null) app.viewControllerListener.viewDidAppear(animated);
		}

		@Override
		public UIInterfaceOrientationMask getSupportedInterfaceOrientations () {
			long mask = 0;
			if (app.config.orientationLandscape) {
				mask |= ((1 << UIInterfaceOrientation.LandscapeLeft.value()) | (1 << UIInterfaceOrientation.LandscapeRight.value()));
			}
			if (app.config.orientationPortrait) {
				mask |= ((1 << UIInterfaceOrientation.Portrait.value()) | (1 << UIInterfaceOrientation.PortraitUpsideDown.value()));
			}
			return new UIInterfaceOrientationMask(mask);
		}

		@Override
		public boolean shouldAutorotate () {
			return true;
		}

		public boolean shouldAutorotateToInterfaceOrientation (UIInterfaceOrientation orientation) {
			// we return "true" if we support the orientation
			switch (orientation) {
			case LandscapeLeft:
			case LandscapeRight:
				return app.config.orientationLandscape;
			default:
				// assume portrait
				return app.config.orientationPortrait;
			}
		}

		@Override
		public UIRectEdge getPreferredScreenEdgesDeferringSystemGestures() {
			return app.config.screenEdgesDeferringSystemGestures;
		}

		@Override
		public void viewDidLayoutSubviews () {
			super.viewDidLayoutSubviews();
			// get the view size and update graphics
			CGRect bounds = app.getBounds();
			graphics.width = (int)bounds.getWidth();
			graphics.height = (int)bounds.getHeight();
			graphics.makeCurrent();
			if (graphics.created) {
				graphics.updateSafeInsets();
				app.listener.resize(graphics.width, graphics.height);
			}
		}

		@Override
		public boolean prefersStatusBarHidden () {
			return !app.config.statusBarVisible;
		}

		@Override
		public boolean prefersHomeIndicatorAutoHidden() {
			return app.config.hideHomeIndicator;
		}

		@Callback
		@BindSelector("shouldAutorotateToInterfaceOrientation:")
		private static boolean shouldAutorotateToInterfaceOrientation (IOSUIViewController self, Selector sel,
			UIInterfaceOrientation orientation) {
			return self.shouldAutorotateToInterfaceOrientation(orientation);
		}
	}

	static class IOSUIView extends GLKView {

		public IOSUIView (CGRect frame, EAGLContext context) {
			super(frame, context);
		}
	}

	IOSApplication app;
	IOSInput input;
	GL20 gl20;
	GL30 gl30;
	int width;
	int height;
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

	volatile boolean appPaused;
	private long frameId = -1;
	private boolean isContinuous = true;
	private boolean isFrameRequested = true;

	IOSApplicationConfiguration config;
	EAGLContext context;
	GLVersion glVersion;
	GLKView view;
	IOSUIViewController viewController;

	public IOSGraphics (float scale, IOSApplication app, IOSApplicationConfiguration config, IOSInput input, boolean useGLES30) {
		this.config = config;

		final CGRect bounds = app.getBounds();
		// setup view and OpenGL
		width = (int)bounds.getWidth();
		height = (int)bounds.getHeight();

		if (useGLES30) {
			context = new EAGLContext(EAGLRenderingAPI.OpenGLES3);
			if (context != null)
				gl20 = gl30 = new IOSGLES30();
			else
				Gdx.app.log("IOGraphics", "OpenGL ES 3.0 not supported, falling back on 2.0");
		}
		if (context == null) {
			context = new EAGLContext(EAGLRenderingAPI.OpenGLES2);
			gl20 = new IOSGLES20();
			gl30 = null;
		}

		view = new GLKView(new CGRect(0, 0, bounds.getWidth(), bounds.getHeight()), context) {
			@Method(selector = "touchesBegan:withEvent:")
			public void touchesBegan (@Pointer long touches, UIEvent event) {
				IOSGraphics.this.input.onTouch(touches);
			}

			@Method(selector = "touchesCancelled:withEvent:")
			public void touchesCancelled (@Pointer long touches, UIEvent event) {
				IOSGraphics.this.input.onTouch(touches);
			}

			@Method(selector = "touchesEnded:withEvent:")
			public void touchesEnded (@Pointer long touches, UIEvent event) {
				IOSGraphics.this.input.onTouch(touches);
			}

			@Method(selector = "touchesMoved:withEvent:")
			public void touchesMoved (@Pointer long touches, UIEvent event) {
				IOSGraphics.this.input.onTouch(touches);
			}

			@Override
			public void draw (CGRect rect) {
				IOSGraphics.this.draw(this, rect);
			}

		};
		view.setDelegate(this);
		view.setDrawableColorFormat(config.colorFormat);
		view.setDrawableDepthFormat(config.depthFormat);
		view.setDrawableStencilFormat(config.stencilFormat);
		view.setDrawableMultisample(config.multisample);
		view.setMultipleTouchEnabled(true);

		viewController = app.createUIViewController(this);
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
		if (config.depthFormat == GLKViewDrawableDepthFormat._16) {
			depth = 16;
		} else if (config.depthFormat == GLKViewDrawableDepthFormat._24) {
			depth = 24;
		} else {
			depth = 0;
		}
		if (config.stencilFormat == GLKViewDrawableStencilFormat._8) {
			stencil = 8;
		}
		if (config.multisample == GLKViewDrawableMultisample._4X) {
			samples = 4;
		}
		bufferFormat = new BufferFormat(r, g, b, a, depth, stencil, samples, false);

		String machineString = HWMachine.getMachineString();
		IOSDevice device = config.knownDevices.get(machineString);
		if (device == null) app.error(tag, "Machine ID: " + machineString + " not found, please report to LibGDX");
		int ppi = device != null ? device.ppi : 163;
		density = device != null ? device.ppi/160f : scale;
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
	public void draw (GLKView view, CGRect rect) {
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

			updateSafeInsets();
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

	@Override
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
		return width;
	}

	@Override
	public int getHeight () {
		return height;
	}

	@Override
	public int getBackBufferWidth() {
		return width;
	}

	@Override
	public int getBackBufferHeight() {
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
		return new IOSDisplayMode(getWidth(), getHeight(), config.preferredFramesPerSecond, bufferFormat.r + bufferFormat.g
			+ bufferFormat.b + bufferFormat.a);
	}

	@Override
	public Monitor getPrimaryMonitor() {
		return new IOSMonitor(0, 0, "Primary Monitor");
	}

	@Override
	public Monitor getMonitor() {
		return getPrimaryMonitor();
	}

	@Override
	public Monitor[] getMonitors() {
		return new Monitor[] { getPrimaryMonitor() };
	}

	@Override
	public DisplayMode[] getDisplayModes(Monitor monitor) {
		return getDisplayModes();
	}

	@Override
	public DisplayMode getDisplayMode(Monitor monitor) {
		return getDisplayMode();
	}

	protected void updateSafeInsets() {
		safeInsetTop = 0;
		safeInsetLeft = 0;
		safeInsetRight = 0;
		safeInsetBottom = 0;

		if (Foundation.getMajorSystemVersion() >= 11) {
			UIView view = UIApplication.getSharedApplication().getKeyWindow().getRootViewController().getView();
			UIEdgeInsets edgeInsets = view.getSafeAreaInsets();

			double top = edgeInsets.getTop() * view.getContentScaleFactor();
			double bottom = edgeInsets.getBottom() * view.getContentScaleFactor();
			double left = edgeInsets.getLeft() * view.getContentScaleFactor();
			double right = edgeInsets.getRight() * view.getContentScaleFactor();

			safeInsetTop = (int) top;
			safeInsetLeft = (int) left;
			safeInsetRight = (int) right;
			safeInsetBottom = (int) bottom;
		}
	}

	@Override
	public int getSafeInsetLeft() {
		return safeInsetLeft;
	}

	@Override
	public int getSafeInsetTop() {
		return safeInsetTop;
	}

	@Override
	public int getSafeInsetBottom() {
		return safeInsetBottom;
	}

	@Override
	public int getSafeInsetRight() {
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
	public void setUndecorated(boolean undecorated) {
	}

	@Override
	public void setResizable(boolean resizable) {
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

	private class IOSDisplayMode extends DisplayMode {
		protected IOSDisplayMode (int width, int height, int refreshRate, int bitsPerPixel) {
			super(width, height, refreshRate, bitsPerPixel);
		}
	}

	private class IOSMonitor extends Monitor {
		protected IOSMonitor(int virtualX, int virtualY, String name) {
			super(virtualX, virtualY, name);
		}
	}
}
