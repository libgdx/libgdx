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
import org.robovm.apple.foundation.NSThread;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIDevice;
import org.robovm.apple.uikit.UIScreen;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.rt.bro.Bro;

/**
 * IOS application that can be embedded to a view as child UI view controller.
 * The controller returned by {@link #getUIViewController()} should be added as
 * child controller. The application must be initialized before shown. Other
 * lifecycle methods must be called accordingly.
 *
 * @author Ondrej Fibich <ondrej.fibich@gmail.com>
 */
public class IOSViewApplication extends BaseIOSApplication {

	UIApplication uiApp;
	UIViewController parentUiViewController;
	IOSGraphics graphics;
	IOSAudio audio;
	Files files;
	IOSInput input;
	IOSNet net;

	/** The display scale factor (1.0f for normal; 2.0f to use retina coordinates/dimensions). */
	float pixelsPerPoint = 1;

	private IOSScreenBounds lastScreenBounds = null;

	public IOSViewApplication (ApplicationListener listener, IOSApplicationConfiguration config) {
		super(listener, config);
	}

	/**
	 * Initializes the application to specified parent view within the the specified application.
	 *
	 * @param uiApp iOS UI application
	 * @param uiParent parent UI view controller for drawing bounds
	 */
	public void init (UIApplication uiApp, UIViewController uiParent) {
		setApplicationLogger(new IOSApplicationLogger());
		Gdx.app = this;
		this.uiApp = uiApp;
		this.parentUiViewController = uiParent;

		// enable or disable screen dimming
		uiApp.setIdleTimerDisabled(config.preventScreenDimming);

		Gdx.app.debug("IOSViewApplication", "iOS version: " + UIDevice.getCurrentDevice().getSystemVersion());
		Gdx.app.debug("IOSViewApplication", "Running in " + (Bro.IS_64BIT ? "64-bit" : "32-bit") + " mode");

		// iOS counts in "points" instead of pixels. Points are logical pixels
		pixelsPerPoint = (float)UIScreen.getMainScreen().getNativeScale();
		Gdx.app.debug("IOSViewApplication", "Pixels per point: " + pixelsPerPoint);

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

		Gdx.app.debug("IOSViewApplication", "created");
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

	@Override
	protected IOSScreenBounds computeBounds () {
		CGRect parentBounds = parentUiViewController.getView().getBounds();

		double parentWidth = parentBounds.getWidth();
		double parentHeight = parentBounds.getHeight();

		final int offsetX = (int)Math.round(parentBounds.getMinX());
		final int offsetY = (int)Math.round(parentBounds.getMinY());

		final int width = (int)Math.round(parentWidth);
		final int height = (int)Math.round(parentHeight);

		final int backBufferWidth = (int)Math.round(parentWidth * pixelsPerPoint);
		final int backBufferHeight = (int)Math.round(parentHeight * pixelsPerPoint);

		debug("IOSViewApplication", "Computed bounds are x=" + offsetX + " y=" + offsetY + " w=" + width + " h=" + height + " bbW= "
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

	public void resume () {
		Gdx.app.debug("IOSViewApplication", "resumed");
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

	public void pause () {
		Gdx.app.debug("IOSViewApplication", "paused");
		graphics.makeCurrent();
		graphics.pause();
		Gdx.gl.glFinish();
	}

	public void dispose () {
		Gdx.app.debug("IOSViewApplication", "disposed");
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
		NSThread.exit();
	}
}
