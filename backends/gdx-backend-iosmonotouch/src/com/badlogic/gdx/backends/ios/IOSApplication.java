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

package com.badlogic.gdx.backends.ios;

import java.util.ArrayList;

import cli.MonoTouch.Foundation.NSBundle;
import cli.MonoTouch.Foundation.NSDictionary;
import cli.MonoTouch.Foundation.NSMutableDictionary;
import cli.MonoTouch.UIKit.UIApplication;
import cli.MonoTouch.UIKit.UIApplicationDelegate;
import cli.MonoTouch.UIKit.UIDevice;
import cli.MonoTouch.UIKit.UIInterfaceOrientation;
import cli.MonoTouch.UIKit.UIScreen;
import cli.MonoTouch.UIKit.UIUserInterfaceIdiom;
import cli.MonoTouch.UIKit.UIViewController;
import cli.MonoTouch.UIKit.UIWindow;
import cli.System.Console;
import cli.System.Environment;
import cli.System.Drawing.RectangleF;
import cli.System.IO.Path;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Clipboard;

public class IOSApplication extends UIApplicationDelegate implements Application {
	
	class IOSUIViewController extends UIViewController {
		@Override
		public void DidRotate (UIInterfaceOrientation orientation) {
			// get the view size and update graphics
			// FIXME: supporting BOTH (landscape+portrait at same time) is currently not working correctly (needs fix)
			// FIXME screen orientation needs to be stored for Input#getNativeOrientation
			RectangleF bounds = getBounds(this);
			graphics.width = (int)bounds.get_Width();
			graphics.height = (int)bounds.get_Height();
			graphics.MakeCurrent(); // not sure if that's needed? badlogic: yes it is, so resize can do OpenGL stuff, not sure if
// it's on the correct thread though
			listener.resize(graphics.width, graphics.height);
		}

		@Override
		public boolean ShouldAutorotateToInterfaceOrientation (UIInterfaceOrientation orientation) {
			// we return "true" if we support the orientation
			switch (orientation.Value) {
			case UIInterfaceOrientation.LandscapeLeft:
			case UIInterfaceOrientation.LandscapeRight:
				return config.orientationLandscape;
			default:
				// assume portrait
				return config.orientationPortrait;
			}
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

	ArrayList<Runnable> runnables = new ArrayList<Runnable>();
	ArrayList<Runnable> executedRunnables = new ArrayList<Runnable>();

	/** Should be called in AppDelegate#FinishedLaunching.
	 * 
	 * @param listener Our application (aka game) to run.
	 * @param config The desired iOS configuration. */
	public IOSApplication (ApplicationListener listener, IOSApplicationConfiguration config) {
		this.listener = listener;
		this.config = config;
		Gdx.app = this;
	}

	@Override
	public boolean FinishedLaunching (UIApplication uiApp, NSDictionary options) {
		this.uiApp = uiApp;

		// enable or disable screen dimming
		UIApplication.get_SharedApplication().set_IdleTimerDisabled(config.preventScreenDimming);

		// fix the scale factor if we have a retina device (NOTE: iOS screen sizes are in "points" not pixels by default!)
		if (UIScreen.get_MainScreen().get_Scale() == 2.0f) {
			// we have a retina device!
			if (UIDevice.get_CurrentDevice().get_UserInterfaceIdiom().Value == UIUserInterfaceIdiom.Pad) {
				// it's an iPad!
				displayScaleFactor = config.displayScaleLargeScreenIfRetina * 2.0f;
			} else {
				// it's an iPod or iPhone
				displayScaleFactor = config.displayScaleSmallScreenIfRetina * 2.0f;
			}
		} 
		else {
			// no retina screen: no scaling!
			if (UIDevice.get_CurrentDevice().get_UserInterfaceIdiom().Value == UIUserInterfaceIdiom.Pad) {
				// it's an iPad!
				displayScaleFactor = config.displayScaleLargeScreenIfNonRetina;
			} else {
				// it's an iPod or iPhone
				displayScaleFactor = config.displayScaleSmallScreenIfNonRetina;
			}
		}

		// Create: Window -> ViewController-> GameView (controller takes care of rotation)
		this.uiWindow = new UIWindow(UIScreen.get_MainScreen().get_Bounds());
		UIViewController uiViewController = new IOSUIViewController();
		this.uiWindow.set_RootViewController(uiViewController);

		GL20 gl20 = config.useMonotouchOpenTK ? new IOSMonotouchGLES20() : new IOSGLES20();
		
		Gdx.gl = gl20;
		Gdx.gl20 = gl20;
		
		// setup libgdx
		this.input = new IOSInput(this);
		this.graphics = new IOSGraphics(getBounds(uiViewController), this, input, gl20);
		this.files = new IOSFiles();
		this.audio = new IOSAudio(config.useObjectAL);
		this.net = new IOSNet(this);

		Gdx.files = this.files;
		Gdx.graphics = this.graphics;
		Gdx.audio = this.audio;
		Gdx.input = this.input;
		Gdx.net = this.net;

		this.input.setupPeripherals();

		// attach our view to window+controller and make it visible
		uiViewController.set_View(graphics);
		this.graphics.Run();
		this.uiWindow.MakeKeyAndVisible();
		Gdx.app.debug("IOSApplication", "created");
		return true;
	}

	/** Returns our real display dimension based on screen orientation.
	 * 
	 * @param viewController The view controller.
	 * @return Or real display dimension. */
	RectangleF getBounds (UIViewController viewController) {
		// or screen size (always portrait)
		RectangleF bounds = UIScreen.get_MainScreen().get_Bounds();

		// determine orientation and resulting width + height
		UIInterfaceOrientation orientation = viewController.get_InterfaceOrientation();
		int width;
		int height;
		switch (orientation.Value) {
		case UIInterfaceOrientation.LandscapeLeft:
		case UIInterfaceOrientation.LandscapeRight:
			height = (int)bounds.get_Width();
			width = (int)bounds.get_Height();
			break;
		default:
			// assume portrait
			width = (int)bounds.get_Width();
			height = (int)bounds.get_Height();
		}

		// update width/height depending on display scaling selected 
		width *= displayScaleFactor;
		height *= displayScaleFactor;

		// log screen dimensions
		Gdx.app.debug("IOSApplication", "View: " + orientation.toString() + " " + width + "x" + height);

		// return resulting view size (based on orientation)
		return new RectangleF(0, 0, width, height);
	}

	@Override
	public void OnActivated (UIApplication uiApp) {
		Gdx.app.debug("IOSApplication", "resumed");
		graphics.MakeCurrent();
		graphics.resume();
	}

	@Override
	public void OnResignActivation (UIApplication uiApp) {
		Gdx.app.debug("IOSApplication", "paused");
		graphics.MakeCurrent();
		graphics.pause();
		Gdx.gl.glFlush();
	}

	@Override
	public void WillTerminate (UIApplication uiApp) {
		Gdx.app.debug("IOSApplication", "disposed");
		graphics.MakeCurrent();
		listener.dispose();
		Gdx.gl.glFlush();
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
			Console.WriteLine("[info] " + tag + ": " + message);
		}
	}

	@Override
	public void log (String tag, String message, Exception exception) {
		if (logLevel > LOG_NONE) {
			Console.WriteLine("[info] " + tag + ": " + message);
			exception.printStackTrace();
		}
	}

	@Override
	public void error (String tag, String message) {
		if (logLevel >= LOG_ERROR) {
			Console.WriteLine("[error] " + tag + ": " + message);
		}
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_ERROR) {
			Console.WriteLine("[error] " + tag + ": " + message);
			exception.printStackTrace();
		}
	}

	@Override
	public void debug (String tag, String message) {
		if (logLevel >= LOG_DEBUG) {
			Console.WriteLine("[debug] " + tag + ": " + message);
		}
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_DEBUG) {
			Console.WriteLine("[error] " + tag + ": " + message);
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
		String applicationPath = Environment.GetFolderPath (Environment.SpecialFolder.wrap(Environment.SpecialFolder.MyDocuments));
		String libraryPath = Path.Combine(applicationPath, "..", "Library");
		String finalPath = Path.Combine(libraryPath, name + ".plist");
		
		Gdx.app.debug("IOSApplication", "Loading NSDictionary from file " + finalPath);
		NSMutableDictionary nsDictionary = NSMutableDictionary.FromFile(finalPath);

		// if it fails to get an existing dictionary, create a new one.
		if (nsDictionary == null) {
			Gdx.app.debug("IOSApplication", "NSDictionary not found, creating a new one");
			nsDictionary = new NSMutableDictionary();
			boolean fileWritten = nsDictionary.WriteToFile(finalPath, false);
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
		for (int i = 0; i < executedRunnables.size(); i++) {
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
}
