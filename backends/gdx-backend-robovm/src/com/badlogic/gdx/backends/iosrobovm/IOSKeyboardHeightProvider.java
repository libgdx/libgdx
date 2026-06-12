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

import com.badlogic.gdx.Gdx;

import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.NSDictionary;
import org.robovm.apple.foundation.NSNotification;
import org.robovm.apple.foundation.NSNotificationCenter;
import org.robovm.apple.foundation.NSNumber;
import org.robovm.apple.foundation.NSObject;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.foundation.NSValue;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIInterfaceOrientation;
import org.robovm.apple.uikit.UIKeyboardAnimation;
import org.robovm.apple.uikit.UIView;
import org.robovm.apple.uikit.UIViewAnimationOptions;
import org.robovm.apple.uikit.UIWindow;
import org.robovm.objc.Selector;
import org.robovm.objc.annotation.Method;

/** Pushes keyboard show/hide events to an {@link IOSKeyboardObserver}. Owns the notification registration, the notification
 * parsing and the keyboard-matched animation: the observer is invoked one frame after the notification (via postRunnable) inside
 * a UIView animation block using the keyboard's own duration and curve, so any UIView property writes the observer performs
 * animate alongside the keyboard. */
public class IOSKeyboardHeightProvider extends NSObject {

	/** The observer that will be notified when the keyboard height changes. */
	public interface IOSKeyboardObserver {
		/** Pushed on the frame after the keyboard notification, inside a UIView animation block matching the keyboard's own
		 * transition.
		 * @param opened whether an on-screen keyboard is on screen
		 * @param height keyboard height in screen points; 0 when closed */
		void onKeyboardHeightChanged (boolean opened, double height);
	}

	private IOSKeyboardObserver observer;
	private boolean started;

	/** The cached height (screen points) the keyboard last had in landscape orientation */
	private double keyboardLandscapeHeight;
	/** The cached height (screen points) the keyboard last had in portrait orientation */
	private double keyboardPortraitHeight;

	/** The cached opened state of the keyboard */
	private boolean cachedOpened;
	/** The cached height of the keyboard */
	private double cachedHeight;
	/** The cached interface orientation of the app */
	private UIInterfaceOrientation cachedOrientation;

	/** Starts observing keyboard notifications. Idempotent. */
	public void start () {
		if (started) return;
		started = true;
		// We do this, to not dispatch changes that are not changes on first run
		cachedOrientation = getInterfaceOrientation();
		NSNotificationCenter.getDefaultCenter().addObserver(this, Selector.register("keyboardWillShow"),
			UIWindow.KeyboardWillShowNotification(), null);
		NSNotificationCenter.getDefaultCenter().addObserver(this, Selector.register("keyboardWillHide"),
			UIWindow.KeyboardWillHideNotification(), null);
	}

	/** Stops observing keyboard notifications. The default application never calls this; it exists for API symmetry with the
	 * Android backend and custom lifecycles. */
	public void close () {
		if (!started) return;
		started = false;
		NSNotificationCenter.getDefaultCenter().removeObserver(this);
	}

	public void setKeyboardHeightObserver (IOSKeyboardObserver observer) {
		this.observer = observer;
	}

	@Method(selector = "keyboardWillShow")
	public void keyboardWillShow (NSNotification notification) {
		NSDictionary<NSString, ?> userInfo = (NSDictionary<NSString, ?>)notification.getUserInfo();
		CGRect keyboardFrameEnd = ((NSValue)userInfo.get(UIKeyboardAnimation.Keys.FrameEnd())).rectValue();
		double height = keyboardFrameEnd.getSize().getHeight();

		UIInterfaceOrientation orientation = getInterfaceOrientation();
		if (orientation == UIInterfaceOrientation.LandscapeLeft || orientation == UIInterfaceOrientation.LandscapeRight) {
			keyboardLandscapeHeight = height;
		} else {
			keyboardPortraitHeight = height;
		}

		dispatch(notification, true, height);
	}

	@Method(selector = "keyboardWillHide")
	public void keyboardWillHide (NSNotification notification) {
		dispatch(notification, false, 0);
	}

	/** @return the cached height (screen points) the keyboard last had in landscape orientation */
	public double getKeyboardLandscapeHeight () {
		return keyboardLandscapeHeight;
	}

	/** @return the cached height (screen points) the keyboard last had in portrait orientation */
	public double getKeyboardPortraitHeight () {
		return keyboardPortraitHeight;
	}

	private UIInterfaceOrientation getInterfaceOrientation () {
		return UIApplication.getSharedApplication().getStatusBarOrientation();
	}

	protected void dispatch (NSNotification notification, boolean opened, double height) {
		// Don't dispatch what isn't a change. iOS re-fires keyboard notifications liberally (focus churn, repeated
		// hides); orientation is part of the key so a rotation with an unchanged keyboard height still goes through
		// (the field layout depends on it)
		UIInterfaceOrientation orientation = getInterfaceOrientation();
		if (opened == cachedOpened && height == cachedHeight && orientation == cachedOrientation) return;
		cachedOpened = opened;
		cachedHeight = height;
		cachedOrientation = orientation;

		NSDictionary<NSString, ?> userInfo = (NSDictionary<NSString, ?>)notification.getUserInfo();
		double duration = ((NSNumber)userInfo.get(UIKeyboardAnimation.Keys.AnimationDuration())).doubleValue();
		long curve = ((NSNumber)userInfo.get(UIKeyboardAnimation.Keys.AnimationCurve())).longValue();

		Gdx.app.postRunnable( () -> {
			if (observer == null) return;
			// The keyboard reports a private animation curve (7) that UIViewAnimationCurve can't represent: shifting the
			// raw value into the curve bits of UIViewAnimationOptions passes it through to UIKit unchanged
			UIView.animate(duration, 0, new UIViewAnimationOptions(curve << 16),
				() -> observer.onKeyboardHeightChanged(opened, height), null);
		});
	}
}
