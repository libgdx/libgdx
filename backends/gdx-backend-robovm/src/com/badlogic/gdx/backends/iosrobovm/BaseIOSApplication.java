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

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Clipboard;
import java.io.File;
import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSMutableDictionary;
import org.robovm.apple.foundation.NSObject;
import org.robovm.apple.foundation.NSProcessInfo;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIDevice;
import org.robovm.apple.uikit.UIPasteboard;
import org.robovm.apple.uikit.UIUserInterfaceIdiom;
import org.robovm.apple.uikit.UIViewController;

/**
 * Abstract iOS application which implements shared code for all IOS
 * applications such as: gdx application initialization, preferences, clipboard,
 * access to bounds, memory statistics, etc.
 *
 * @author Ondřej Fibich <ondrej.fibich@gmail.com>
 */
abstract class BaseIOSApplication extends BaseApplication {

	final ApplicationListener listener;
	final IOSApplicationConfiguration config;

	IOSViewControllerListener viewControllerListener;

	public BaseIOSApplication (ApplicationListener listener, IOSApplicationConfiguration config) {
		this.listener = listener;
		this.config = config;
	}

	protected IOSUIViewController createUIViewController (IOSGraphics graphics) {
		return new IOSUIViewController(this, graphics);
	}

	/** Return the UI application of IOSApplication
	 * @return the iOS UI application */
	protected abstract UIApplication getUIApp ();

	/** Return the UI view controller of IOSApplication
	 * @return the iOS view controller */
	protected abstract UIViewController getUIViewController ();

	@Override
	public ApplicationListener getApplicationListener () {
		return listener;
	}

	@Override
	public abstract IOSGraphics getGraphics ();

	@Override
	public abstract IOSInput getInput ();

	@Override
	public ApplicationType getType () {
		return ApplicationType.iOS;
	}

	@Override
	public int getVersion () {
		return (int) NSProcessInfo.getSharedProcessInfo().getOperatingSystemVersion().getMajorVersion();
	}

	@Override
	public long getJavaHeap () {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	@Override
	public long getNativeHeap () {
		return getJavaHeap();
	}

	/** @see IOSScreenBounds for detailed explanation
	 * @return logical dimensions of space we draw to, adjusted for device orientation */
	protected abstract IOSScreenBounds computeBounds ();

	/** @return area of screen in UIKit points on which libGDX draws, with 0,0 being upper left corner */
	public abstract IOSScreenBounds getScreenBounds ();

	/** @return The display scale factor (1.0f for normal; 2.0f to use retina coordinates/dimensions). */
	public abstract float getPixelsPerPoint ();

	/** Returns device ppi using a best guess approach when device is unknown. Overwrite to customize strategy. */
	protected int guessUnknownPpi () {
		int ppi;
		if (UIDevice.getCurrentDevice().getUserInterfaceIdiom() == UIUserInterfaceIdiom.Pad)
			ppi = 132 * (int) getPixelsPerPoint();
		else
			ppi = 164 * (int) getPixelsPerPoint();
		error("IOSApplication", "Device PPI unknown. PPI value has been guessed to " + ppi + " but may be wrong");
		return ppi;
	}

	@Override
	public Preferences getPreferences (String name) {
		File libraryPath = new File(System.getenv("HOME"), "Library");
		File finalPath = new File(libraryPath, name + ".plist");

		@SuppressWarnings("unchecked")
		NSMutableDictionary<NSString, NSObject> nsDictionary = (NSMutableDictionary<NSString, NSObject>)NSMutableDictionary
			.read(finalPath);

		// if it fails to get an existing dictionary, create a new one.
		if (nsDictionary == null) {
			nsDictionary = new NSMutableDictionary<NSString, NSObject>();
			nsDictionary.write(finalPath, false);
		}
		return new IOSPreferences(nsDictionary, finalPath.getAbsolutePath());
	}

	@Override
	public Clipboard getClipboard () {
		return new Clipboard() {
			@Override
			public void setContents (String content) {
				UIPasteboard.getGeneralPasteboard().setString(content);
			}

			@Override
			public boolean hasContents () {
				if (Foundation.getMajorSystemVersion() >= 10) {
					return UIPasteboard.getGeneralPasteboard().hasStrings();
				}

				String contents = getContents();
				return contents != null && !contents.isEmpty();
			}

			@Override
			public String getContents () {
				return UIPasteboard.getGeneralPasteboard().getString();
			}
		};
	}

	/** Add a listener to handle events from the libgdx root view controller
	 * @param listener The {#link IOSViewControllerListener} to add */
	public void addViewControllerListener (IOSViewControllerListener listener) {
		viewControllerListener = listener;
	}
}
