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

import java.io.File;

import org.moe.natj.general.Pointer;
import org.moe.natj.objc.ann.Selector;

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
import com.badlogic.gdx.backends.iosmoe.objectal.OALAudioSession;
import com.badlogic.gdx.backends.iosmoe.objectal.OALSimpleAudio;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;

import apple.NSObject;
import apple.coregraphics.struct.CGPoint;
import apple.coregraphics.struct.CGRect;
import apple.coregraphics.struct.CGSize;
import apple.foundation.NSDictionary;
import apple.foundation.NSMutableDictionary;
import apple.foundation.NSThread;
import apple.uikit.UIApplication;
import apple.uikit.UIDevice;
import apple.uikit.UIPasteboard;
import apple.uikit.UIScreen;
import apple.uikit.UIViewController;
import apple.uikit.UIWindow;
import apple.uikit.enums.UIInterfaceOrientation;
import apple.uikit.enums.UIUserInterfaceIdiom;
import apple.uikit.protocol.UIApplicationDelegate;

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
		public boolean applicationDidFinishLaunchingWithOptions (UIApplication application, NSDictionary<?, ?> launchOptions) {
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
	IOSFiles files;
	IOSInput input;
	IOSNet net;
	int logLevel = Application.LOG_DEBUG;
	ApplicationLogger applicationLogger;

	/** The display scale factor (1.0f for normal; 2.0f to use retina coordinates/dimensions). */
	float displayScaleFactor;

	protected CGRect lastScreenBounds = null;

	Array<Runnable> runnables = new Array<Runnable>();
	Array<Runnable> executedRunnables = new Array<Runnable>();
	Array<LifecycleListener> lifecycleListeners = new Array<LifecycleListener>();

	public IOSApplication (ApplicationListener listener, IOSApplicationConfiguration config) {
		this.listener = listener;
		this.config = config;
	}

	final boolean didFinishLaunching (UIApplication uiApp, NSDictionary<?, ?> launchOptions) {
		this.uiApp = uiApp;

		init();

		this.uiWindow = UIWindow.alloc().initWithFrame(UIScreen.mainScreen().bounds());
		this.uiWindow.setRootViewController(this.graphics.viewController);
		this.uiWindow.makeKeyAndVisible();
		Gdx.app.debug("IOSApplication", "created");
		return true;
	}

	protected void init () {
		setApplicationLogger(new IOSApplicationLogger());
		Gdx.app = this;

		// enable or disable screen dimming
		UIApplication.sharedApplication().setIdleTimerDisabled(config.preventScreenDimming);

		Gdx.app.debug("IOSApplication", "iOS version: " + UIDevice.currentDevice().systemVersion());
		// fix the scale factor if we have a retina device (NOTE: iOS screen sizes are in "points" not pixels by default!)

		float scale = (float)(getIosVersion() >= 8 ? UIScreen.mainScreen().nativeScale() : UIScreen.mainScreen().nativeScale());
		if (scale >= 2.0f) {
			Gdx.app.debug("IOSApplication", "scale: " + scale);
			if (UIDevice.currentDevice().userInterfaceIdiom() == UIUserInterfaceIdiom.Pad) {
				// it's an iPad!
				displayScaleFactor = config.displayScaleLargeScreenIfRetina * scale;
			} else {
				// it's an iPod or iPhone
				displayScaleFactor = config.displayScaleSmallScreenIfRetina * scale;
			}
		} else {
			// no retina screen: no scaling!
			if (UIDevice.currentDevice().userInterfaceIdiom() == UIUserInterfaceIdiom.Pad) {
				// it's an iPad!
				displayScaleFactor = config.displayScaleLargeScreenIfNonRetina;
			} else {
				// it's an iPod or iPhone
				displayScaleFactor = config.displayScaleSmallScreenIfNonRetina;
			}
		}

		// setup libgdx
		this.input = createInput();
		this.graphics = createGraphics(scale);
		Gdx.gl = Gdx.gl20 = graphics.gl20;
		Gdx.gl30 = graphics.gl30;
		this.files = new IOSFiles();
		this.audio = new IOSAudio(config);
		this.net = new IOSNet(this);

		Gdx.files = this.files;
		Gdx.graphics = this.graphics;
		Gdx.audio = this.audio;
		Gdx.input = this.input;
		Gdx.net = this.net;

		this.input.setupPeripherals();
	}

	protected IOSGraphics createGraphics (float scale) {
		return IOSGraphics.alloc().init(scale, this, config, input, config.useGL30);
	}

	protected IOSInput createInput() {
		return new IOSInput(this);
	}

	private int getIosVersion () {
		String systemVersion = UIDevice.currentDevice().systemVersion();
		int version = Integer.parseInt(systemVersion.split("\\.")[0]);
		return version;
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

	/** GL View spans whole screen, that is, even under the status bar. iOS can also rotate the screen, which is not handled
	 * consistently over iOS versions. This method returns, in pixels, rectangle in which libGDX draws.
	 *
	 * @return dimensions of space we draw to, adjusted for device orientation */
	protected CGRect getBounds () {
		final CGRect screenBounds = getOriginalBounds();
		final long statusBarOrientation = getStatusBarOrientation();

		double screenWidth = screenBounds.size().width();
		double screenHeight = screenBounds.size().height();

		// Make sure that the orientation is consistent with ratios. Should be, but may not be on older iOS versions
		if (statusBarOrientation == UIInterfaceOrientation.LandscapeLeft
			|| statusBarOrientation == UIInterfaceOrientation.LandscapeRight) {
			if (screenHeight > screenWidth) {
				debug("IOSApplication", "Switching reported width and height (w=" + screenWidth + " h=" + screenHeight + ")");
				double tmp = screenHeight;
				// noinspection SuspiciousNameCombination
				screenHeight = screenWidth;
				screenWidth = tmp;
			}
		}

		// update width/height depending on display scaling selected
		screenWidth *= displayScaleFactor;
		screenHeight *= displayScaleFactor;

		double statusBarHeight = getStatusBarHeight(screenHeight);

		debug("IOSApplication", "Total computed bounds are w=" + screenWidth + " h=" + screenHeight);

		return lastScreenBounds = new CGRect(new CGPoint(0, statusBarHeight), new CGSize(screenWidth, screenHeight));
	}

	protected CGRect getCachedBounds () {
		if (lastScreenBounds == null)
			return getBounds();
		else
			return lastScreenBounds;
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
		return Integer.parseInt(UIDevice.currentDevice().systemVersion().split("\\.")[0]);
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
		String path = libraryPath + "/" + name + ".plist";

		NSMutableDictionary<String, Object> nsDictionary = NSMutableDictionary.dictionaryWithContentsOfFile(path);

		// if it fails to get an existing dictionary, create a new one.
		if (nsDictionary == null) {
			nsDictionary = (NSMutableDictionary<String, Object>)NSMutableDictionary.alloc().init();
			nsDictionary.writeToFileAtomically(path, false);
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
		NSThread.exit();
	}

	@Override
	public Clipboard getClipboard () {
		return new Clipboard() {
			@Override
			public void setContents (String content) {
				UIPasteboard.generalPasteboard().setString(content);
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

	/** Add a listener to handle events from the libgdx root view controller
	 * @param listener The {#link IOSViewControllerListener} to add */
	public void addViewControllerListener (IOSViewControllerListener listener) {
		viewControllerListener = listener;
	}

	protected CGRect getOriginalBounds () {
		return UIScreen.mainScreen().bounds();
	}

	protected double getStatusBarHeight (double screenHeight) {
		final CGRect statusBarFrame = uiApp.statusBarFrame();
		double statusBarHeight = Math.min(statusBarFrame.size().width(), statusBarFrame.size().height());
		if (statusBarHeight != 0.0) {
			debug("IOSApplication", "Status bar is visible (height = " + statusBarHeight + ")");
			statusBarHeight *= displayScaleFactor;
			screenHeight -= statusBarHeight;
		} else {
			debug("IOSApplication", "Status bar is not visible");
		}

		return statusBarHeight;
	}

	protected long getStatusBarOrientation () {
		return uiApp.statusBarOrientation();
	}
}
