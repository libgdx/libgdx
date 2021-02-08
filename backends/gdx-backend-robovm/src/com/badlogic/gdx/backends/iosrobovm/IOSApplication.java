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
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.backends.iosrobovm.objectal.OALAudioSession;
import com.badlogic.gdx.backends.iosrobovm.objectal.OALIOSAudio;
import com.badlogic.gdx.backends.iosrobovm.objectal.OALSimpleAudio;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIApplicationDelegateAdapter;
import org.robovm.apple.uikit.UIApplicationLaunchOptions;
import org.robovm.apple.uikit.UIDevice;
import org.robovm.apple.uikit.UIScreen;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.apple.uikit.UIWindow;
import org.robovm.rt.bro.Bro;

public class IOSApplication extends BaseIOSApplication {

	public static abstract class Delegate extends UIApplicationDelegateAdapter {
		private IOSApplication app;

		protected abstract IOSApplication createApplication ();

		@Override
		public boolean didFinishLaunching (UIApplication application, UIApplicationLaunchOptions launchOptions) {
			application.addStrongRef(this); // Prevent this from being GCed until the ObjC UIApplication is deallocated
			this.app = createApplication();
			return app.didFinishLaunching(application, launchOptions);
		}

		@Override
		public void didBecomeActive (UIApplication application) {
			app.didBecomeActive(application);
		}

		@Override
		public void willEnterForeground (UIApplication application) {
			app.willEnterForeground(application);
		}

		@Override
		public void willResignActive (UIApplication application) {
			app.willResignActive(application);
		}

		@Override
		public void willTerminate (UIApplication application) {
			app.willTerminate(application);
		}
	}

	UIApplication uiApp;
	UIWindow uiWindow;
	IOSGraphics graphics;
	IOSAudio audio;
	Files files;
	IOSInput input;
	IOSNet net;

	/** The display scale factor (1.0f for normal; 2.0f to use retina coordinates/dimensions). */
	float pixelsPerPoint;

	private IOSScreenBounds lastScreenBounds = null;

	public IOSApplication (ApplicationListener listener, IOSApplicationConfiguration config) {
		super(listener, config);
	}

	final boolean didFinishLaunching (UIApplication uiApp, UIApplicationLaunchOptions options) {
		setApplicationLogger(new IOSApplicationLogger());
		Gdx.app = this;
		this.uiApp = uiApp;

		// enable or disable screen dimming
		uiApp.setIdleTimerDisabled(config.preventScreenDimming);

		Gdx.app.debug("IOSApplication", "iOS version: " + UIDevice.getCurrentDevice().getSystemVersion());
		Gdx.app.debug("IOSApplication", "Running in " + (Bro.IS_64BIT ? "64-bit" : "32-bit") + " mode");

		// iOS counts in "points" instead of pixels. Points are logical pixels
		pixelsPerPoint = (float)UIScreen.getMainScreen().getNativeScale();
		Gdx.app.debug("IOSApplication", "Pixels per point: " + pixelsPerPoint);

		this.uiWindow = new UIWindow(UIScreen.getMainScreen().getBounds());
		this.uiWindow.makeKeyAndVisible();
		uiApp.getDelegate().setWindow(uiWindow);

		// setup libgdx
		this.input = createInput();
		this.graphics = createGraphics();
		Gdx.gl = Gdx.gl20 = graphics.gl20;
		Gdx.gl30 = graphics.gl30;
		this.files = createFiles();
		this.audio = createAudio(config);
		this.net = new IOSNet(this, config);

		Gdx.files = this.files;
		Gdx.graphics = this.graphics;
		Gdx.audio = this.audio;
		Gdx.input = this.input;
		Gdx.net = this.net;

		this.input.setupPeripherals();

		this.uiWindow.setRootViewController(this.graphics.viewController);
		Gdx.app.debug("IOSApplication", "created");
		return true;
	}

	protected Files createFiles() {
		return new IOSFiles();
	}

	protected IOSAudio createAudio (IOSApplicationConfiguration config) {
		return new OALIOSAudio(config);
	}

	protected IOSGraphics createGraphics() {
		 return new IOSGraphics(this, config, input, config.useGL30);
	}

	protected IOSInput createInput() {
		 return new DefaultIOSInput(this);
	}

	@Override
	protected UIApplication getUIApp () {
		return uiApp;
	}

	@Override
	public UIViewController getUIViewController () {
		return graphics.viewController;
	}

	/** Return the UI Window of IOSApplication
	 * @return the window */
	public UIWindow getUIWindow () {
		return uiWindow;
	}

	@Override
	protected IOSScreenBounds computeBounds () {
		CGRect screenBounds = uiWindow.getBounds();
		final CGRect statusBarFrame = uiApp.getStatusBarFrame();
		double statusBarHeight = statusBarFrame.getHeight();

		double screenWidth = screenBounds.getWidth();
		double screenHeight = screenBounds.getHeight();

		if (statusBarHeight != 0.0) {
			debug("IOSApplication", "Status bar is visible (height = " + statusBarHeight + ")");
			screenHeight -= statusBarHeight;
		} else {
			debug("IOSApplication", "Status bar is not visible");
		}
		final int offsetX = 0;
		final int offsetY = (int)Math.round(statusBarHeight);

		final int width = (int)Math.round(screenWidth);
		final int height = (int)Math.round(screenHeight);

		final int backBufferWidth = (int)Math.round(screenWidth * pixelsPerPoint);
		final int backBufferHeight = (int)Math.round(screenHeight * pixelsPerPoint);

		debug("IOSApplication", "Computed bounds are x=" + offsetX + " y=" + offsetY + " w=" + width + " h=" + height + " bbW= "
					+ backBufferWidth + " bbH= " + backBufferHeight);

		return lastScreenBounds = new IOSScreenBounds(offsetX, offsetY, width, height, backBufferWidth, backBufferHeight);
	}

	@Override
	public IOSScreenBounds getScreenBounds () {
		if (lastScreenBounds == null)
			return computeBounds();
		else
			return lastScreenBounds;
	}

	@Override
	public float getPixelsPerPoint () {
		return pixelsPerPoint;
	}

	final void didBecomeActive (UIApplication uiApp) {
		Gdx.app.debug("IOSApplication", "resumed");
		// workaround for ObjectAL crash problem
		// see: https://groups.google.com/forum/?fromgroups=#!topic/objectal-for-iphone/ubRWltp_i1Q
		OALAudioSession audioSession = OALAudioSession.sharedInstance();
		if (audioSession != null) {
			audioSession.forceEndInterruption();
		}
		if (config.allowIpod) {
			OALSimpleAudio audio = OALSimpleAudio.sharedInstance();
			if (audio != null) {
				audio.setUseHardwareIfAvailable(false);
			}
		}
		graphics.makeCurrent();
		graphics.resume();
	}

	final void willEnterForeground (UIApplication uiApp) {
		// workaround for ObjectAL crash problem
		// see: https://groups.google.com/forum/?fromgroups=#!topic/objectal-for-iphone/ubRWltp_i1Q
		OALAudioSession audioSession = OALAudioSession.sharedInstance();
		if (audioSession != null) {
			audioSession.forceEndInterruption();
		}
	}

	final void willResignActive (UIApplication uiApp) {
		Gdx.app.debug("IOSApplication", "paused");
		graphics.makeCurrent();
		graphics.pause();
		Gdx.gl.glFinish();
	}

	final void willTerminate (UIApplication uiApp) {
		Gdx.app.debug("IOSApplication", "disposed");
		graphics.makeCurrent();
		pauseLifecycleListeners();
		listener.dispose();
		Gdx.gl.glFinish();
	}

	@Override
	public IOSGraphics getGraphics () {
		return graphics;
	}

	@Override
	public Audio getAudio () {
		return audio;
	}

	@Override
	public IOSInput getInput () {
		return input;
	}

	@Override
	public Files getFiles () {
		return files;
	}

	@Override
	public Net getNet () {
		return net;
	}

	@Override
	public void exit () {
		System.exit(0);
	}
}
