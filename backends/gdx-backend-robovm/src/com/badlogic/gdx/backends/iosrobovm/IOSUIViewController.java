
package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.input.NativeInputConfiguration;
import org.robovm.apple.coregraphics.CGPoint;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.coregraphics.CGSize;
import org.robovm.apple.foundation.*;
import org.robovm.apple.glkit.GLKViewController;
import org.robovm.apple.uikit.*;
import org.robovm.objc.Selector;
import org.robovm.objc.annotation.Method;

public class IOSUIViewController extends GLKViewController {
	final IOSApplication app;
	final IOSGraphics graphics;

	protected IOSUIViewController (IOSApplication app, IOSGraphics graphics) {
		this.app = app;
		this.graphics = graphics;
	}

	@Override
	public void viewWillAppear (boolean animated) {
		super.viewWillAppear(animated);
		// start GLKViewController even though we may only draw a single frame
		// (we may be in non-continuous mode)
		setPaused(false);
		injectKeyboardNotification();
	}

	protected Input.KeyboardHeightObserver observer;

	@Method(selector = "keyboardWillHide")
	public void keyboardWillHide (NSNotification notification) {
		UIView view = graphics.input.getActiveKeyboardTextField();
		if (view == null) {
			if (observer != null) {
				observer.onKeyboardHide();
				observer.onKeyboardHeightChanged(0);
			}

		} else {
			// On iPad a keyboard can close because it 1. the close button was pressed and 2. a password auto-fill was requested
			// We can differentiate them, if in postRunnable the view is still first responder. If it is -> password auto-fill and we
			// ignore that the keyboard closed
			// If not, dispatch to observer and request close
			Gdx.app.postRunnable( () -> {
				UIView active = graphics.input.getActiveKeyboardTextField();
				if (view != active) {
					// Our field was closed meanwhile (own closeTextInputField / setOnscreenKeyboardVisible(false)).
					// Don't touch `view` — it may be disposed. If it was replaced by a new field, its
					// keyboardWillShow already fired, so swallow the stale hide.
					if (active == null && observer != null) {
						observer.onKeyboardHide();
						observer.onKeyboardHeightChanged(0);
					}
					return;
				}

				if (view.isFirstResponder()) return;

				if (observer != null) {
					observer.onKeyboardHide();
					observer.onKeyboardHeightChanged(0);
				}

				if (graphics.input.isTextInputFieldOpened()) graphics.input.closeTextInputField(false);
			});
		}
	}

	@Method(selector = "keyboardWillShow")
	public void keyboardWillShow (NSNotification notification) {
		NativeInputConfiguration configuration = graphics.input.getNativeInputConfiguration();
		CGRect screenRect = UIScreen.getMainScreen().getBounds();
		double screenHeight = screenRect.getSize().getHeight();
		double heightScale = Gdx.graphics.getHeight() / screenHeight;

		NSDictionary<NSString, ?> userInfo = (NSDictionary<NSString, ?>)notification.getUserInfo();
		CGRect keyboardFrameScreen = ((NSValue)userInfo.get(UIKeyboardAnimation.Keys.FrameEnd())).rectValue();

		UIView textField = graphics.input.getActiveKeyboardTextField();
		if (textField == null || !textField.isFirstResponder() || textField.isHidden()) {
			if (observer != null) {
				int kbHeight = (int)(keyboardFrameScreen.getSize().getHeight() * heightScale);
				observer.onKeyboardShow(kbHeight);
				observer.onKeyboardHeightChanged(kbHeight);
			}
			return;
		}

		// I haven't found any docs on when keyboardWillShow constructs a implicit animation, so iOS 15 should be fine
		if (Foundation.getMajorSystemVersion() <= 15) {
			double duration;
			long curve;
			curve = ((NSNumber)userInfo.get(UIKeyboardAnimation.Keys.AnimationCurve())).longValue();
			duration = ((NSNumber)userInfo.get(UIKeyboardAnimation.Keys.AnimationDuration())).doubleValue();

			UIView.beginAnimations(null, null);
			UIView.setAnimationDurationInSeconds(duration);
			UIView.setAnimationCurve(UIViewAnimationCurve.valueOf(curve));
		}

		float insetFraction = configuration != null ? configuration.getHorizontalInsetFraction() : 0;
		double fallbackInset = getView().getBounds().getSize().getWidth() * insetFraction;

		UIView accessoryView = textField.getInputAccessoryView();
		if (Foundation.getMajorSystemVersion() >= 26 && accessoryView != null) {
			CGRect keyboardFrameInView = textField.convertRectToView(keyboardFrameScreen, null);
			double extraRightShift = getView().getSafeAreaInsets().getRight() > 0 ? 0 : fallbackInset;
			CGRect accessoryFrame = new CGRect(
				new CGPoint(keyboardFrameInView.getSize().getWidth() / 2 - accessoryView.getBounds().getSize().getHeight() / 2 - 2
					- extraRightShift, -3),
				new CGSize(getView().getBounds().getSize().getWidth(), accessoryView.getBounds().getSize().getHeight()));
			accessoryView.setFrame(accessoryFrame);
		}

		CGRect newFrame = textField.getFrame();
		if (observer != null) {
			int kbHeight = (int)((keyboardFrameScreen.getSize().getHeight() + newFrame.getSize().getHeight()) * heightScale);
			if (Foundation.getMajorSystemVersion() >= 26 && accessoryView != null) {
				kbHeight -= (int)(accessoryView.getBounds().getSize().getHeight() * heightScale);
			}
			observer.onKeyboardShow(kbHeight);
			observer.onKeyboardHeightChanged(kbHeight);
		}

		CGRect keyboardFrameInTextField = textField.convertRectToView(keyboardFrameScreen, null);
		double keyboardHeight = keyboardFrameInTextField.getSize().getHeight();
		double specialRightInset = 0;
		if (Foundation.getMajorSystemVersion() >= 26 && accessoryView != null) {
			keyboardHeight -= accessoryView.getBounds().getSize().getHeight();
			specialRightInset = accessoryView.getBounds().getSize().getHeight() + 3;
		}

		double leftInset = getView().getSafeAreaInsets().getLeft() > 0 ? getView().getSafeAreaInsets().getLeft() : fallbackInset;
		double rightInset = (getView().getSafeAreaInsets().getRight() > 0 ? getView().getSafeAreaInsets().getRight()
			: fallbackInset) + specialRightInset;
		newFrame.setOrigin(
			new CGPoint(leftInset, getView().getBounds().getSize().getHeight() - keyboardHeight - newFrame.getSize().getHeight()));
		newFrame.setSize(
			new CGSize(getView().getBounds().getSize().getWidth() - leftInset - rightInset, newFrame.getSize().getHeight()));
		textField.setFrame(newFrame);

		if (Foundation.getMajorSystemVersion() <= 15) {
			UIView.commitAnimations();
		}
	}

	public void injectKeyboardNotification () {
		NSNotificationCenter.getDefaultCenter().addObserver(this, Selector.register("keyboardWillShow"),
			UIWindow.KeyboardWillShowNotification(), null);
		NSNotificationCenter.getDefaultCenter().addObserver(this, Selector.register("keyboardWillHide"),
			UIWindow.KeyboardWillHideNotification(), null);

	}

	@Override
	public void viewDidAppear (boolean animated) {
		super.viewDidAppear(animated);
		getView().setContentScaleFactor(UIScreen.getMainScreen().getNativeScale());
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
		if (newBounds.width != oldBounds.width || newBounds.height != oldBounds.height) {
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
