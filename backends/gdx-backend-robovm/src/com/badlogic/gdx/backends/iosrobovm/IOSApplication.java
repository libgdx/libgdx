package com.badlogic.gdx.backends.iosrobovm;

import java.io.File;

import org.robovm.cocoatouch.coregraphics.CGSize;
import org.robovm.cocoatouch.foundation.NSDictionary;
import org.robovm.cocoatouch.foundation.NSMutableDictionary;
import org.robovm.cocoatouch.uikit.UIApplication;
import org.robovm.cocoatouch.uikit.UIApplicationDelegate;
import org.robovm.cocoatouch.uikit.UIDevice;
import org.robovm.cocoatouch.uikit.UIInterfaceOrientation;
import org.robovm.cocoatouch.uikit.UIScreen;
import org.robovm.cocoatouch.uikit.UIUserInterfaceIdiom;
import org.robovm.cocoatouch.uikit.UIViewController;
import org.robovm.cocoatouch.uikit.UIWindow;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;

public class IOSApplication implements Application {
	
	public static abstract class Delegate extends UIApplicationDelegate.Adapter {
		private IOSApplication app;
		
		protected abstract IOSApplication createApplication();

		@Override
		public boolean didFinishLaunching (UIApplication application, NSDictionary launchOptions) {
			application.addStrongRef(this); // Prevent this from being GCed until the ObjC UIApplication is deallocated
			this.app = createApplication();
			return app.didFinishLaunching(application, launchOptions);
		}

		@Override
		public void didBecomeActive (UIApplication application) {
			app.didBecomeActive(application);
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
	ApplicationListener listener;
	IOSApplicationConfiguration config;
	IOSGraphics graphics;
	IOSAudio audio;
	IOSFiles files;
	IOSInput input;
	IOSNet net;
	int logLevel = Application.LOG_DEBUG;

	/** The display scale factor (1.0f for normal; 2.0f to use retina coordinates/dimensions). */
	float displayScaleFactor;

	Array<Runnable> runnables = new Array<Runnable>();
	Array<Runnable> executedRunnables = new Array<Runnable>();
	Array<LifecycleListener> lifecycleListeners = new Array<LifecycleListener>();

	public IOSApplication (ApplicationListener listener, IOSApplicationConfiguration config) {
		this.listener = listener;
		this.config = config;
	}
	
	final boolean didFinishLaunching (UIApplication uiApp, NSDictionary options) {
		Gdx.app = this;
		this.uiApp = uiApp;

		// enable or disable screen dimming
		UIApplication.getSharedApplication().setIdleTimerDisabled(config.preventScreenDimming);

		// fix the scale factor if we have a retina device (NOTE: iOS screen sizes are in "points" not pixels by default!)
		if (UIScreen.getMainScreen().getScale() == 2.0f) {
			// we have a retina device!
			if (UIDevice.getCurrentDevice().getUserInterfaceIdiom() == UIUserInterfaceIdiom.Pad) {
				// it's an iPad!
				displayScaleFactor = config.displayScaleLargeScreenIfRetina * 2.0f;
			} else {
				// it's an iPod or iPhone
				displayScaleFactor = config.displayScaleSmallScreenIfRetina * 2.0f;
			}
		} 
		else {
			// no retina screen: no scaling!
			if (UIDevice.getCurrentDevice().getUserInterfaceIdiom() == UIUserInterfaceIdiom.Pad) {
				// it's an iPad!
				displayScaleFactor = config.displayScaleLargeScreenIfNonRetina;
			} else {
				// it's an iPod or iPhone
				displayScaleFactor = config.displayScaleSmallScreenIfNonRetina;
			}
		}

		GL20 gl20 = new IOSGLES20();
		
		Gdx.gl = gl20;
		Gdx.gl20 = gl20;
		
		// setup libgdx
		this.input = new IOSInput(this);
		this.graphics = new IOSGraphics(getBounds(null), this, input, gl20);
		this.files = new IOSFiles();
		this.audio = new IOSAudio();
		this.net = new IOSNet(this);

		Gdx.files = this.files;
		Gdx.graphics = this.graphics;
		Gdx.audio = this.audio;
		Gdx.input = this.input;
		Gdx.net = this.net;

		this.input.setupPeripherals();

		this.uiWindow = new UIWindow(UIScreen.getMainScreen().getBounds());
		this.uiWindow.setRootViewController(this.graphics.viewController);
		this.uiWindow.makeKeyAndVisible();
		Gdx.app.debug("IOSApplication", "created");
		return true;
	}
	
	/**
	 * Return the UI view controller of IOSApplication
	 * @return the view controller of IOSApplication
	 */
	public UIViewController getUIViewController(){
		return graphics.viewController;
	}

	/** Returns our real display dimension based on screen orientation.
	 * 
	 * @param viewController The view controller.
	 * @return Or real display dimension. */
	CGSize getBounds (UIViewController viewController) {
		// or screen size (always portrait)
		CGSize bounds = UIScreen.getMainScreen().getBounds().size();

		// determine orientation and resulting width + height
		UIInterfaceOrientation orientation = viewController != null 
			? viewController.getInterfaceOrientation() : uiApp.getStatusBarOrientation();
		int width;
		int height;
		switch (orientation) {
		case LandscapeLeft:
		case LandscapeRight:
			height = (int)bounds.width();
			width = (int)bounds.height();
			break;
		default:
			// assume portrait
			width = (int)bounds.width();
			height = (int)bounds.height();
		}

		// update width/height depending on display scaling selected 
		width *= displayScaleFactor;
		height *= displayScaleFactor;

		// log screen dimensions
		Gdx.app.debug("IOSApplication", "View: " + orientation.toString() + " " + width + "x" + height);

		// return resulting view size (based on orientation)
		return new CGSize(width, height);
	}

	final void didBecomeActive (UIApplication uiApp) {
		Gdx.app.debug("IOSApplication", "resumed");
		// workaround for ObjectAL crash problem
		// see: https://groups.google.com/forum/?fromgroups=#!topic/objectal-for-iphone/ubRWltp_i1Q
		//	OALAudioSession.sharedInstance().forceEndInterrupt();
		graphics.makeCurrent();
		graphics.resume();
	}

	final void willResignActive (UIApplication uiApp) {
		Gdx.app.debug("IOSApplication", "paused");
		graphics.makeCurrent();
		graphics.pause();
		Gdx.gl.glFlush();
	}

	final void willTerminate (UIApplication uiApp) {
		Gdx.app.debug("IOSApplication", "disposed");
		graphics.makeCurrent();
		Array<LifecycleListener> listeners = lifecycleListeners;
		synchronized(listeners) {
			for(LifecycleListener listener: listeners) {
				listener.pause();
			}
		}
		listener.dispose();
		Gdx.gl.glFlush();
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
	public void log (String tag, String message) {
		if (logLevel > LOG_NONE) {
			System.out.println("[info] " + tag + ": " + message);
		}
	}

	@Override
	public void log (String tag, String message, Exception exception) {
		if (logLevel > LOG_NONE) {
			System.out.println("[info] " + tag + ": " + message);
			exception.printStackTrace();
		}
	}

	@Override
	public void error (String tag, String message) {
		if (logLevel >= LOG_ERROR) {
			System.out.println("[error] " + tag + ": " + message);
		}
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_ERROR) {
			System.out.println("[error] " + tag + ": " + message);
			exception.printStackTrace();
		}
	}

	@Override
	public void debug (String tag, String message) {
		if (logLevel >= LOG_DEBUG) {
			System.out.println("[debug] " + tag + ": " + message);
		}
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_DEBUG) {
			System.out.println("[error] " + tag + ": " + message);
			exception.printStackTrace();
		}
	}

	@Override
	public void setLogLevel (int logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public ApplicationType getType () {
		return ApplicationType.iOS;
	}

	@Override
	public int getVersion () {
		// FIXME return iOS version
		return 0;
	}

	@Override
	public long getJavaHeap () {
		// FIXME check what mono offers
		return 0;
	}

	@Override
	public long getNativeHeap () {
		// FIXME check what mono offers
		return 0;
	}

	@Override
	public Preferences getPreferences (String name) {
		File libraryPath = new File(System.getenv("HOME"), "Library");
		String finalPath = new File(libraryPath, name + ".plist").getAbsolutePath();
		
		Gdx.app.debug("IOSApplication", "Loading NSDictionary from file " + finalPath);
		NSMutableDictionary nsDictionary = NSMutableDictionary.fromFile(finalPath);

		// if it fails to get an existing dictionary, create a new one.
		if (nsDictionary == null) {
			Gdx.app.debug("IOSApplication", "NSDictionary not found, creating a new one");
			nsDictionary = new NSMutableDictionary();
			boolean fileWritten = nsDictionary.writeToFile(finalPath, false);
			if (fileWritten)
				Gdx.app.debug("IOSApplication", "NSDictionary file written");
			else 
				Gdx.app.debug("IOSApplication", "Failed to write NSDictionary to file " + finalPath);
		}
		return new IOSPreferences(nsDictionary, finalPath);
	}

	@Override
	public void postRunnable (Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
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
		// FIXME implement clipboard
		return new Clipboard() {
			@Override
			public void setContents (String content) {
			}

			@Override
			public String getContents () {
				return null;
			}
		};
	}
	
	@Override
	public void addLifecycleListener (LifecycleListener listener) {
		synchronized(lifecycleListeners) {
			lifecycleListeners.add(listener);
		}
	}

	@Override
	public void removeLifecycleListener (LifecycleListener listener) {
		synchronized(lifecycleListeners) {
			lifecycleListeners.removeValue(listener, true);
		}		
	}
}