
package com.badlogic.gdx.backends.iosmoe;

import com.badlogic.gdx.graphics.glutils.HdpiMode;
import apple.foundation.NSSet;
import apple.glkit.GLKViewController;
import apple.uikit.UIDevice;
import apple.uikit.enums.UIInterfaceOrientation;
import apple.uikit.UIPress;
import apple.uikit.UIPressesEvent;
import apple.uikit.enums.UIUserInterfaceIdiom;
import org.moe.natj.general.NatJ;
import org.moe.natj.general.Pointer;
import org.moe.natj.objc.ann.Selector;

public class IOSUIViewController extends GLKViewController {
	private IOSApplication app;
	private IOSGraphics graphics;

	static {
		NatJ.register();
	}

	@Selector("alloc")
	public static native IOSUIViewController alloc ();

	@Selector("init")
	public native IOSUIViewController init ();

	protected IOSUIViewController (Pointer peer) {
		super(peer);
	}

	public IOSUIViewController init (IOSApplication app, IOSGraphics graphics) {
		init();
		this.app = app;
		this.graphics = graphics;
		return this;
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
	public long supportedInterfaceOrientations () {
		long mask = 0;
		if (app.config.orientationLandscape) {
			mask |= (1L << UIInterfaceOrientation.LandscapeLeft) | (1L << UIInterfaceOrientation.LandscapeRight);
		}
		if (app.config.orientationPortrait) {
			mask |= 1L << UIInterfaceOrientation.Portrait;
			if (UIDevice.currentDevice().userInterfaceIdiom() == UIUserInterfaceIdiom.Pad) {
				mask |= 1L << UIInterfaceOrientation.PortraitUpsideDown;
			}
		}
		return mask;
	}

	@Override
	public boolean shouldAutorotate () {
		return true;
	}

	@Override
	public long preferredScreenEdgesDeferringSystemGestures () {
		return app.config.screenEdgesDeferringSystemGestures;
	}

	@Override
	public void viewDidLayoutSubviews () {
		super.viewDidLayoutSubviews();
		// get the view size and update graphics
		final IOSScreenBounds oldBounds = graphics.screenBounds;
		final IOSScreenBounds newBounds = app.computeBounds();
		graphics.screenBounds = newBounds;
		// Layout may happen without bounds changing, don't trigger resize in that case
		if (graphics.created && (newBounds.width != oldBounds.width || newBounds.height != oldBounds.height)) {
			graphics.makeCurrent();
			graphics.updateSafeInsets();
			graphics.gl20.glViewport(0, 0, newBounds.backBufferWidth, newBounds.backBufferHeight);
			if (graphics.config.hdpiMode == HdpiMode.Pixels) {
				app.listener.resize(newBounds.backBufferWidth, newBounds.backBufferHeight);
			} else {
				app.listener.resize(newBounds.width, newBounds.height);
			}
		}

	}

	@Override
	public boolean prefersStatusBarHidden () {
		return !app.config.statusBarVisible;
	}

	@Override
	public boolean prefersHomeIndicatorAutoHidden () {
		return app.config.hideHomeIndicator;
	}

	@Override
	public void pressesBeganWithEvent (NSSet<? extends UIPress> presses, UIPressesEvent event) {
		if (presses == null || presses.count() == 0 || !app.input.onKey(presses.objectEnumerator().nextObject().key(), true)) {
			super.pressesBeganWithEvent(presses, event);
		}
	}

	@Override
	public void pressesEndedWithEvent (NSSet<? extends UIPress> presses, UIPressesEvent event) {
		if (presses == null || presses.count() == 0 || !app.input.onKey(presses.objectEnumerator().nextObject().key(), false)) {
			super.pressesEndedWithEvent(presses, event);
		}
	}
}
