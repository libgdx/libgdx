package com.badlogic.gdx.backends.iosrobovm;

import org.robovm.cocoatouch.coregraphics.CGPoint;
import org.robovm.cocoatouch.coregraphics.CGRect;
import org.robovm.cocoatouch.coregraphics.CGSize;
import org.robovm.cocoatouch.foundation.NSObject;
import org.robovm.cocoatouch.foundation.NSSet;
import org.robovm.cocoatouch.glkit.GLKView;
import org.robovm.cocoatouch.glkit.GLKViewController;
import org.robovm.cocoatouch.glkit.GLKViewControllerDelegate;
import org.robovm.cocoatouch.glkit.GLKViewDelegate;
import org.robovm.cocoatouch.glkit.GLKViewDrawableColorFormat;
import org.robovm.cocoatouch.opengles.EAGLContext;
import org.robovm.cocoatouch.opengles.EAGLRenderingAPI;
import org.robovm.cocoatouch.uikit.UIDevice;
import org.robovm.cocoatouch.uikit.UIEvent;
import org.robovm.cocoatouch.uikit.UIInterfaceOrientation;
import org.robovm.cocoatouch.uikit.UIScreen;
import org.robovm.cocoatouch.uikit.UIUserInterfaceIdiom;
import org.robovm.objc.Selector;
import org.robovm.objc.annotation.BindSelector;
import org.robovm.rt.bro.annotation.Callback;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.utils.Array;

// FIXME add GL 1.x support by ripping Android's classes
public class IOSGraphics extends NSObject implements Graphics, GLKViewDelegate,
		GLKViewControllerDelegate {

	private static final String tag = "IOSGraphics";

	static class IOSUIViewController extends GLKViewController {
		final IOSApplication app;
		final IOSGraphics graphics;
		boolean created = false;

		IOSUIViewController(IOSApplication app, IOSGraphics graphics) {
			this.app = app;
			this.graphics = graphics;
		}

		@Override
		public void didRotate(UIInterfaceOrientation orientation) {
			super.didRotate(orientation);
			// get the view size and update graphics
			// FIXME: supporting BOTH (landscape+portrait at same time) is
			// currently not working correctly (needs fix)
			// FIXME screen orientation needs to be stored for
			// Input#getNativeOrientation
			CGSize bounds = app.getBounds(this);
			graphics.width = (int) bounds.width();
			graphics.height = (int) bounds.height();
			graphics.makeCurrent();
			app.listener.resize(graphics.width, graphics.height);
		}

		public boolean shouldAutorotateToInterfaceOrientation(
				UIInterfaceOrientation orientation) {
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
		private static boolean shouldAutorotateToInterfaceOrientation(
				IOSUIViewController self, Selector sel,
				UIInterfaceOrientation orientation) {
			return self.shouldAutorotateToInterfaceOrientation(orientation);
		}
	}

	static class IOSUIView extends GLKView {

		public IOSUIView(CGRect frame, EAGLContext context) {
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
	boolean wasPaused;

	EAGLContext context;
	GLKView view;
	IOSUIViewController viewController;

	public IOSGraphics(CGSize bounds, IOSApplication app, IOSInput input, GL20 gl20) {
		// setup view and OpenGL
		width = (int) bounds.width();
		height = (int) bounds.height();
		app.debug(tag, bounds.width() + "x" + bounds.height() + ", " + UIScreen.getMainScreen().getScale());
		this.gl20 = gl20;

		context = new EAGLContext(EAGLRenderingAPI.OpenGLES2);

		view = new GLKView(new CGRect(new CGPoint(0, 0), bounds), context) {
			@Override
			public void touchesBegan(NSSet touches, UIEvent event) {
				super.touchesBegan(touches, event);
				IOSGraphics.this.input.touchDown(touches, event);
			}

			@Override
			public void touchesCancelled(NSSet touches, UIEvent event) {
				super.touchesCancelled(touches, event);
				IOSGraphics.this.input.touchUp(touches, event);
			}

			@Override
			public void touchesEnded(NSSet touches, UIEvent event) {
				super.touchesEnded(touches, event);
				IOSGraphics.this.input.touchUp(touches, event);
			}

			@Override
			public void touchesMoved(NSSet touches, UIEvent event) {
				super.touchesMoved(touches, event);
				IOSGraphics.this.input.touchMoved(touches, event);
			}

			@Override
			public void draw(CGRect rect) {
				IOSGraphics.this.draw(this, rect);
			}

		};
		view.setDelegate(this);
		view.setDrawableColorFormat(GLKViewDrawableColorFormat.RGB565);

		viewController = new IOSUIViewController(app, this);
		viewController.setView(view);
		viewController.setDelegate(this);
		viewController.setPreferredFramesPerSecond(60);

		this.app = app;
		this.input = input;

		// FIXME fix this if we add rgba/depth/stencil flags to
		// IOSApplicationConfiguration
		bufferFormat = new BufferFormat(5, 6, 5, 0, 16, 0, 0, false);
		this.gl20 = gl20;

		// determine display density and PPI (PPI values via Wikipedia!)
		density = 1f;

		// if ((UIScreen.getMainScreen().respondsToSelector(new
		// Selector("scale")))) {
		float scale = UIScreen.getMainScreen().getScale();
		app.debug(tag, "Calculating density, UIScreen.mainScreen.scale: " + scale);
		if (scale == 2f) density = 2f;

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
		ppcY = ppcY / 2.54f;
		app.debug(tag, "Display: ppi=" + ppi + ", density=" + density);

		// time + FPS
		lastFrameTime = System.nanoTime();
		framesStart = lastFrameTime;

		paused = false;
		wasPaused = true;
	}

	public void resume() {
		paused = false;
	}

	public void pause() {
		paused = true;
	}

	boolean created = false;
	@Override
	public void draw(GLKView view, CGRect rect) {
		if(!created) {
			app.graphics.makeCurrent();
			app.listener.create();
			app.listener.resize(width, height);
			created = true;
		}
		if (paused) {
			if (!wasPaused) {
				Array<LifecycleListener> listeners = app.lifecycleListeners;
				synchronized (listeners) {
					for (LifecycleListener listener : listeners) {
						listener.pause();
					}
				}
				app.listener.pause();
				wasPaused = true;
			}
			return;
		} else {
			if (wasPaused) {
				Array<LifecycleListener> listeners = app.lifecycleListeners;
				synchronized (listeners) {
					for (LifecycleListener listener : listeners) {
						listener.resume();
					}
				}
				app.listener.resume();
				wasPaused = false;
			}
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

		makeCurrent();
		input.processEvents();
		app.listener.render();
	}

	void makeCurrent() {
		EAGLContext.setCurrentContext(context);
	}

	@Override
	public void update(GLKViewController controller) {
		makeCurrent();
		app.processRunnables();
	}

	@Override
	public void willPause(GLKViewController controller, boolean pause) {
		if (pause) {
			pause();
		} else {
			resume();
		}
	}

	@Override
	public boolean isGL11Available() {
		return false;
	}

	@Override
	public boolean isGL20Available() {
		return true;
	}

	@Override
	public GLCommon getGLCommon() {
		return gl20;
	}

	@Override
	public GL10 getGL10() {
		return null;
	}

	@Override
	public GL11 getGL11() {
		return null;
	}

	@Override
	public GL20 getGL20() {
		return gl20;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public float getDeltaTime() {
		return deltaTime;
	}

	@Override
	public float getRawDeltaTime() {
		return deltaTime;
	}

	@Override
	public int getFramesPerSecond() {
		return fps;
	}

	@Override
	public GraphicsType getType() {
		return GraphicsType.iOSGL;
	}

	@Override
	public float getPpiX() {
		return ppiX;
	}

	@Override
	public float getPpiY() {
		return ppiY;
	}

	@Override
	public float getPpcX() {
		return ppcX;
	}

	@Override
	public float getPpcY() {
		return ppcY;
	}

	/**
	 * Returns the display density.
	 * 
	 * @return 1.0f for non-retina devices, 2.0f for retina devices.
	 */
	@Override
	public float getDensity() {
		return density;
	}

	@Override
	public boolean supportsDisplayModeChange() {
		return false;
	}

	@Override
	public DisplayMode[] getDisplayModes() {
		return new DisplayMode[] { getDesktopDisplayMode() };
	}

	@Override
	public DisplayMode getDesktopDisplayMode() {
		return new IOSDisplayMode(getWidth(), getHeight(), 60, 0);
	}

	private static class IOSDisplayMode extends DisplayMode {
		protected IOSDisplayMode(int width, int height, int refreshRate,
				int bitsPerPixel) {
			super(width, height, refreshRate, bitsPerPixel);
		}
	}

	@Override
	public boolean setDisplayMode(DisplayMode displayMode) {
		return false;
	}

	@Override
	public boolean setDisplayMode(int width, int height, boolean fullscreen) {
		return false;
	}

	@Override
	public void setTitle(String title) {
	}

	@Override
	public void setVSync(boolean vsync) {
	}

	@Override
	public BufferFormat getBufferFormat() {
		return bufferFormat;
	}

	@Override
	public boolean supportsExtension(String extension) {
		if (extensions == null)
			extensions = Gdx.gl.glGetString(GL10.GL_EXTENSIONS);
		return extensions.contains(extension);
	}

	@Override
	public void setContinuousRendering(boolean isContinuous) {
		// FIXME implement this if possible
	}

	@Override
	public boolean isContinuousRendering() {
		// FIXME implement this if possible
		return true;
	}

	@Override
	public void requestRendering() {
		// FIXME implement this if possible
	}

	@Override
	public boolean isFullscreen() {
		return true;
	}
}