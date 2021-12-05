
package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.graphics.glutils.HdpiMode;
import org.robovm.apple.foundation.NSSet;
import org.robovm.apple.glkit.GLKViewController;
import org.robovm.apple.uikit.UIDevice;
import org.robovm.apple.uikit.UIInterfaceOrientation;
import org.robovm.apple.uikit.UIInterfaceOrientationMask;
import org.robovm.apple.uikit.UIPress;
import org.robovm.apple.uikit.UIPressesEvent;
import org.robovm.apple.uikit.UIRectEdge;
import org.robovm.apple.uikit.UIUserInterfaceIdiom;

public class IOSUIViewController extends GLKViewController {
	final IOSApplication app;
	final IOSGraphics graphics;

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
			mask |= (1L << UIInterfaceOrientation.LandscapeLeft.value()) | (1L << UIInterfaceOrientation.LandscapeRight.value());
		}
		if (app.config.orientationPortrait) {
			mask |= 1L << UIInterfaceOrientation.Portrait.value();
			if (UIDevice.getCurrentDevice().getUserInterfaceIdiom() == UIUserInterfaceIdiom.Pad) {
				mask |= 1L << UIInterfaceOrientation.PortraitUpsideDown.value();
			}
		}
		return new UIInterfaceOrientationMask(mask);
	}

	@Override
	public boolean shouldAutorotate () {
		return true;
	}

	@Override
	public UIRectEdge getPreferredScreenEdgesDeferringSystemGestures () {
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
	public void pressesBegan (NSSet<UIPress> presses, UIPressesEvent event) {
		if (presses == null || presses.isEmpty() || !app.input.onKey(presses.getValues().first().getKey(), true)) {
			super.pressesBegan(presses, event);
		}
	}

	@Override
	public void pressesEnded (NSSet<UIPress> presses, UIPressesEvent event) {
		if (presses == null || presses.isEmpty() || !app.input.onKey(presses.getValues().first().getKey(), false)) {
			super.pressesEnded(presses, event);
		}
	}
}
