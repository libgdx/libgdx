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

import org.robovm.apple.coregraphics.CGPoint;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.coregraphics.CGSize;
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
import org.robovm.apple.uikit.UIDevice;
import org.robovm.apple.uikit.UIEvent;
import org.robovm.apple.uikit.UIInterfaceOrientation;
import org.robovm.apple.uikit.UIInterfaceOrientationMask;
import org.robovm.apple.uikit.UIScreen;
import org.robovm.apple.uikit.UIUserInterfaceIdiom;
import org.robovm.objc.Selector;
import org.robovm.objc.annotation.BindSelector;
import org.robovm.objc.annotation.Method;
import org.robovm.rt.bro.annotation.Callback;
import org.robovm.rt.bro.annotation.Pointer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.Array;

public class IOSGraphics extends NSObject implements Graphics, GLKViewDelegate, GLKViewControllerDelegate {

	private static final String tag = "IOSGraphics";

	static class IOSUIViewController extends GLKViewController {
		final IOSApplication app;
		final IOSGraphics graphics;
		boolean created = false;

		IOSUIViewController (IOSApplication app, IOSGraphics graphics) {
			this.app = app;
			this.graphics = graphics;
		}

		@Override
		public void didRotate (UIInterfaceOrientation orientation) {
			super.didRotate(orientation);
			// get the view size and update graphics
			// FIXME: supporting BOTH (landscape+portrait at same time) is
			// currently not working correctly (needs fix)
			// FIXME screen orientation needs to be stored for
			// Input#getNativeOrientation
			CGSize bounds = app.getBounds(this);
			graphics.width = (int)bounds.width();
			graphics.height = (int)bounds.height();
			graphics.makeCurrent();
			app.listener.resize(graphics.width, graphics.height);
		}

		@Override
		public UIInterfaceOrientationMask getSupportedInterfaceOrientations() {
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
		public boolean shouldAutorotate() {
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

	volatile boolean paused;

	IOSApplicationConfiguration config;
	EAGLContext context;
	GLKView view;
	IOSUIViewController viewController;

	public IOSGraphics (CGSize bounds, IOSApplication app, IOSApplicationConfiguration config, IOSInput input, GL20 gl20) {
		this.config = config;
		// setup view and OpenGL
		width = (int)bounds.width();
		height = (int)bounds.height();
		app.debug(tag, bounds.width() + "x" + bounds.height() + ", " + UIScreen.getMainScreen().getScale());
		this.gl20 = gl20;

		context = new EAGLContext(EAGLRenderingAPI.OpenGLES2);

		view = new GLKView(new CGRect(new CGPoint(0, 0), bounds), context) {
			@Method(selector = "touchesBegan:withEvent:")
			public void touchesBegan (@Pointer long touches, UIEvent event) {
				IOSGraphics.this.input.touchDown(touches, event);
			}

			@Method(selector = "touchesCancelled:withEvent:")
			public void touchesCancelled (@Pointer long touches, UIEvent event) {
				IOSGraphics.this.input.touchUp(touches, event);
			}

			@Method(selector = "touchesEnded:withEvent:")
			public void touchesEnded (@Pointer long touches, UIEvent event) {
				IOSGraphics.this.input.touchUp(touches, event);
			}

			@Method(selector = "touchesMoved:withEvent:")
			public void touchesMoved (@Pointer long touches, UIEvent event) {
				IOSGraphics.this.input.touchMoved(touches, event);
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

		viewController = new IOSUIViewController(app, this);
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
		this.gl20 = gl20;

		// determine display density and PPI (PPI values via Wikipedia!)
		density = 1f;

		// if ((UIScreen.getMainScreen().respondsToSelector(new
		// Selector("scale")))) {
		double scale = UIScreen.getMainScreen().getScale();
		app.debug(tag, "Calculating density, UIScreen.mainScreen.scale: " + scale);
		if (scale == 2) density = 2f;

		int ppi;
		if (UIDevice.getCurrentDevice().getUserInterfaceIdiom() == UIUserInterfaceIdiom.Pad) {
			// iPad
			ppi = Math.round(density * 132);
		} else {
			// iPhone or iPodTouch
			ppi = Math.round(density * 163);
		}
		ppiX = ppi;
		ppiY = ppi;
		ppcX = ppiX / 2.54f;
		ppcY = ppiY / 2.54f;
		app.debug(tag, "Display: ppi=" + ppi + ", density=" + density);

		// time + FPS
		lastFrameTime = System.nanoTime();
		framesStart = lastFrameTime;

		paused = false;
	}

	public void resume () {
		if (!paused) return;
		paused = false;

		Array<LifecycleListener> listeners = app.lifecycleListeners;
		synchronized (listeners) {
			for (LifecycleListener listener : listeners) {
				listener.resume();
			}
		}
		app.listener.resume();
	}

	public void pause () {
		if (paused) return;
		paused = true;

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
			app.listener.create();
			app.listener.resize(width, height);
			created = true;
		}
		if (paused) {
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
		app.listener.render();
	}

	void makeCurrent () {
		EAGLContext.setCurrentContext(context);
	}

	@Override
	public void update (GLKViewController controller) {
		makeCurrent();
		app.processRunnables();
	}

	@Override
	public void willPause (GLKViewController controller, boolean pause) {
		if (pause) {
			if (paused) return;
			pause();
		} else {
			if (!paused) return;
			resume();
		}
	}

	@Override
	public GL20 getGL20 () {
		return gl20;
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

	/** Returns the display density.
	 * 
	 * @return 1.0f for non-retina devices, 2.0f for retina devices. */
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
		return new DisplayMode[] {getDesktopDisplayMode()};
	}

	@Override
	public DisplayMode getDesktopDisplayMode () {
		return new IOSDisplayMode(getWidth(), getHeight(), config.preferredFramesPerSecond, bufferFormat.r + bufferFormat.g
			+ bufferFormat.b + bufferFormat.a);
	}

	private class IOSDisplayMode extends DisplayMode {
		protected IOSDisplayMode (int width, int height, int refreshRate, int bitsPerPixel) {
			super(width, height, refreshRate, bitsPerPixel);
		}
	}

	@Override
	public boolean setDisplayMode (DisplayMode displayMode) {
		return false;
	}

	@Override
	public boolean setDisplayMode (int width, int height, boolean fullscreen) {
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
		// FIXME implement this if possible
	}

	@Override
	public boolean isContinuousRendering () {
		// FIXME implement this if possible
		return true;
	}

	@Override
	public void requestRendering () {
		// FIXME implement this if possible
	}

	@Override
	public boolean isFullscreen () {
		return true;
	}

	@Override
	public boolean isGL30Available () {
		return false;
	}

	@Override
	public GL30 getGL30 () {
		return null;
	}
}
