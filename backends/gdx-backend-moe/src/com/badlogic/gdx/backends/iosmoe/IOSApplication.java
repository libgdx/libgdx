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

package com.badlogic.gdx.backends.iosmoe;

import apple.foundation.NSDictionary;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import apple.coregraphics.struct.CGRect;
import apple.foundation.NSMutableDictionary;
import apple.NSObject;
import apple.foundation.NSProcessInfo;
import apple.uikit.UIApplication;
import apple.uikit.protocol.UIApplicationDelegate;
import apple.uikit.UIDevice;
import apple.uikit.UIPasteboard;
import apple.uikit.UIScreen;
import apple.uikit.enums.UIUserInterfaceIdiom;
import apple.uikit.UIViewController;
import apple.uikit.UIWindow;
import org.moe.natj.general.NatJ;
import org.moe.natj.general.Pointer;
import org.moe.natj.objc.ann.Selector;

import java.io.File;

public class IOSApplication implements Application {

	public static abstract class Delegate extends NSObject implements UIApplicationDelegate {

		private IOSApplication app;

		protected Delegate (Pointer peer) {
			super(peer);
		}

		@Selector("alloc")
		public static native Delegate alloc ();

		protected abstract IOSApplication createApplication ();

		@Override
		public boolean applicationDidFinishLaunchingWithOptions (UIApplication application, NSDictionary launchOptions) {
			this.app = createApplication();
			return app.didFinishLaunching(application, launchOptions);
		}

		@Override
		public void applicationDidBecomeActive (UIApplication application) {
			app.didBecomeActive(application);
		}

		@Override
		public void applicationWillEnterForeground (UIApplication application) {
			app.willEnterForeground(application);
		}

		@Override
		public void applicationWillResignActive (UIApplication application) {
			app.willResignActive(application);
		}

		@Override
		public void applicationWillTerminate (UIApplication application) {
			app.willTerminate(application);
		}

		@Override
		public UIWindow window () {
			return app.getUIWindow();
		}
	}

	UIApplication uiApp;
	UIWindow uiWindow;
	ApplicationListener listener;
	IOSViewControllerListener viewControllerListener;
	IOSApplicationConfiguration config;
	IOSGraphics graphics;
	IOSAudio audio;
	Files files;
	IOSInput input;
	IOSNet net;
	int logLevel = Application.LOG_DEBUG;
	ApplicationLogger applicationLogger;

	/** The display scale factor (1.0f for normal; 2.0f to use retina coordinates/dimensions). */
	float pixelsPerPoint;

	private IOSScreenBounds lastScreenBounds = null;

	Array<Runnable> runnables = new Array<Runnable>();
	Array<Runnable> executedRunnables = new Array<Runnable>();
	Array<LifecycleListener> lifecycleListeners = new Array<LifecycleListener>();

	public IOSApplication (ApplicationListener listener, IOSApplicationConfiguration config) {
		this.listener = listener;
		this.config = config;
	}

	final boolean didFinishLaunching (UIApplication uiApp, NSDictionary<?, ?> options) {
		setApplicationLogger(new IOSApplicationLogger());
		Gdx.app = this;
		this.uiApp = uiApp;

		// enable or disable screen dimming
		uiApp.setIdleTimerDisabled(config.preventScreenDimming);

		Gdx.app.debug("IOSApplication", "iOS version: " + UIDevice.currentDevice().systemVersion());
		Gdx.app.debug("IOSApplication", "Running in " + (NatJ.is64Bit() ? "64-bit" : "32-bit") + " mode");

		// iOS counts in "points" instead of pixels. Points are logical pixels
		pixelsPerPoint = (float)UIScreen.mainScreen().nativeScale();
		Gdx.app.debug("IOSApplication", "Pixels per point: " + pixelsPerPoint);

		this.uiWindow = UIWindow.alloc().initWithFrame(UIScreen.mainScreen().bounds());
		this.uiWindow.makeKeyAndVisible();
		// uiApp.delegate().setWindow(uiWindow);

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

	protected Files createFiles () {
		return new IOSFiles();
	}

	protected IOSAudio createAudio (IOSApplicationConfiguration config) {
		return new OALIOSAudio(config);
	}

	protected IOSGraphics createGraphics () {
		return new IOSGraphics(this, config, input, config.useGL30);
	}

	protected IOSUIViewController createUIViewController (IOSGraphics graphics) {
		return IOSUIViewController.alloc().init(this, graphics);
	}

	protected IOSInput createInput () {
		return new DefaultIOSInput(this);
	}

	/** Returns device ppi using a best guess approach when device is unknown. Overwrite to customize strategy. */
	protected int guessUnknownPpi () {
		int ppi;
		if (UIDevice.currentDevice().userInterfaceIdiom() == UIUserInterfaceIdiom.Pad)
			ppi = 132 * (int)pixelsPerPoint;
		else
			ppi = 164 * (int)pixelsPerPoint;
		error("IOSApplication", "Device PPI unknown. PPI value has been guessed to " + ppi + " but may be wrong");
		return ppi;
	}

	/** Return the UI view controller of IOSApplication
	 * @return the view controller of IOSApplication */
	public UIViewController getUIViewController () {
		return graphics.viewController;
	}

	/** Return the UI Window of IOSApplication
	 * @return the window */
	public UIWindow getUIWindow () {
		return uiWindow;
	}

	/** @see IOSScreenBounds for detailed explanation
	 * @return logical dimensions of space we draw to, adjusted for device orientation */
	protected IOSScreenBounds computeBounds () {
		CGRect screenBounds = uiWindow.bounds();
		final CGRect statusBarFrame = uiApp.statusBarFrame();
		double statusBarHeight = statusBarFrame.size().height();

		double screenWidth = screenBounds.size().width();
		double screenHeight = screenBounds.size().height();

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

	/** @return area of screen in UIKit points on which libGDX draws, with 0,0 being upper left corner */
	public IOSScreenBounds getScreenBounds () {
		if (lastScreenBounds == null)
			return computeBounds();
		else
			return lastScreenBounds;
	}

	final void didBecomeActive (UIApplication uiApp) {
		Gdx.app.debug("IOSApplication", "resumed");
		audio.didBecomeActive();
		graphics.makeCurrent();
		graphics.resume();
	}

	final void willEnterForeground (UIApplication uiApp) {
		audio.willEnterForeground();
	}

	final void willResignActive (UIApplication uiApp) {
		Gdx.app.debug("IOSApplication", "paused");
		audio.willResignActive();
		graphics.makeCurrent();
		graphics.pause();
		Gdx.gl.glFinish();
	}

	final void willTerminate (UIApplication uiApp) {
		Gdx.app.debug("IOSApplication", "disposed");
		audio.willTerminate();
		graphics.makeCurrent();
		Array<LifecycleListener> listeners = lifecycleListeners;
		synchronized (listeners) {
			for (LifecycleListener listener : listeners) {
				listener.pause();
			}
		}
		listener.dispose();
		Gdx.gl.glFinish();
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return listener;
	}

	@Override
	public Graphics getGraphics () {
		return graphics;
	}

	@Override
	public Audio getAudio () {
		return audio;
	}

	@Override
	public Input getInput () {
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
	public void debug (String tag, String message) {
		if (logLevel >= LOG_DEBUG) getApplicationLogger().debug(tag, message);
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_DEBUG) getApplicationLogger().debug(tag, message, exception);
	}

	@Override
	public void log (String tag, String message) {
		if (logLevel >= LOG_INFO) getApplicationLogger().log(tag, message);
	}

	@Override
	public void log (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_INFO) getApplicationLogger().log(tag, message, exception);
	}

	@Override
	public void error (String tag, String message) {
		if (logLevel >= LOG_ERROR) getApplicationLogger().error(tag, message);
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_ERROR) getApplicationLogger().error(tag, message, exception);
	}

	@Override
	public void setLogLevel (int logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public int getLogLevel () {
		return logLevel;
	}

	@Override
	public void setApplicationLogger (ApplicationLogger applicationLogger) {
		this.applicationLogger = applicationLogger;
	}

	@Override
	public ApplicationLogger getApplicationLogger () {
		return applicationLogger;
	}

	@Override
	public ApplicationType getType () {
		return ApplicationType.iOS;
	}

	@Override
	public int getVersion () {
		return (int)NSProcessInfo.processInfo().operatingSystemVersion().majorVersion();
	}

	@Override
	public long getJavaHeap () {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	@Override
	public long getNativeHeap () {
		return getJavaHeap();
	}

	@Override
	public Preferences getPreferences (String name) {
		File libraryPath = new File(System.getenv("HOME"), "Library");
		File finalPath = new File(libraryPath, name + ".plist");

		NSMutableDictionary<String, Object> nsDictionary = NSMutableDictionary
			.dictionaryWithContentsOfFile(finalPath.getAbsolutePath());

		// if it fails to get an existing dictionary, create a new one.
		if (nsDictionary == null) {
			nsDictionary = (NSMutableDictionary<String, Object>)NSMutableDictionary.alloc().init();
			nsDictionary.writeToFileAtomically(finalPath.getAbsolutePath(), false);
		}
		return new IOSPreferences(nsDictionary, finalPath.getAbsolutePath());
	}

	@Override
	public void postRunnable (Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
			Gdx.graphics.requestRendering();
		}
	}

	public void processRunnables () {
		synchronized (runnables) {
			executedRunnables.clear();
			executedRunnables.addAll(runnables);
			runnables.clear();
		}
		for (int i = 0; i < executedRunnables.size; i++) {
			try {
				executedRunnables.get(i).run();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	@Override
	public void exit () {
		System.exit(0);
	}

	@Override
	public Clipboard getClipboard () {
		return new Clipboard() {
			@Override
			public void setContents (String content) {
				UIPasteboard.generalPasteboard().setString(content);
			}

			@Override
			public boolean hasContents () {
				if (Foundation.getMajorSystemVersion() >= 10) {
					return UIPasteboard.generalPasteboard().hasStrings();
				}

				String contents = getContents();
				return contents != null && !contents.isEmpty();
			}

			@Override
			public String getContents () {
				return UIPasteboard.generalPasteboard().string();
			}
		};
	}

	@Override
	public void addLifecycleListener (LifecycleListener listener) {
		synchronized (lifecycleListeners) {
			lifecycleListeners.add(listener);
		}
	}

	@Override
	public void removeLifecycleListener (LifecycleListener listener) {
		synchronized (lifecycleListeners) {
			lifecycleListeners.removeValue(listener, true);
		}
	}

	/** Add a listener to handle events from the libGDX root view controller
	 * @param listener The {#link IOSViewControllerListener} to add */
	public void addViewControllerListener (IOSViewControllerListener listener) {
		viewControllerListener = listener;
	}
}
