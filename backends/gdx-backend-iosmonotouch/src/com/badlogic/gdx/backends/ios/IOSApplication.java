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

import cli.MonoTouch.Foundation.NSDictionary;
import cli.MonoTouch.UIKit.UIApplication;
import cli.MonoTouch.UIKit.UIApplicationDelegate;
import cli.MonoTouch.UIKit.UIDevice;
import cli.MonoTouch.UIKit.UIDeviceOrientation;
import cli.MonoTouch.UIKit.UIInterfaceOrientation;
import cli.MonoTouch.UIKit.UIScreen;
import cli.MonoTouch.UIKit.UIViewController;
import cli.MonoTouch.UIKit.UIWindow;
import cli.System.Console;
import cli.System.Drawing.RectangleF;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Clipboard;

public class IOSApplication extends UIApplicationDelegate implements Application {
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
	boolean firstResume;
	
	/**
	 * Should be called in AppDelegate#FinishedLaunching.
	 * 
	 * @param listener  Our application (aka game) to run.
	 * @param config  The desired iOS configuration.
	 */
	public IOSApplication(ApplicationListener listener, IOSApplicationConfiguration config) {
		this.listener = listener;
		this.config = config; 
		Gdx.app = this;
	}
	
	@Override
	public boolean FinishedLaunching(UIApplication uiApp, NSDictionary options) {
		this.uiApp = uiApp;
			
		// Create: Window -> ViewController-> GameView (controller takes care of rotation)
		this.uiWindow = new UIWindow(UIScreen.get_MainScreen().get_Bounds());	
		UIViewController uiViewController = new UIViewController() {	
			@Override
			public void DidRotate (UIInterfaceOrientation orientation) {
				// get the view size and update graphics
				// FIXME: supporting BOTH (landscape+portrait at same time) is currently not working correctly (needs fix)
				RectangleF bounds = getBounds(this);
				graphics.width = (int)bounds.get_Width();
				graphics.height = (int)bounds.get_Height();
				graphics.MakeCurrent();  // not sure if that's needed?
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
		};
		this.uiWindow.set_RootViewController(uiViewController);

		// setup libgdx
		this.input = new IOSInput(config);
		this.graphics = new IOSGraphics(getBounds(uiViewController), this, input);
		this.files = new IOSFiles();
		this.audio = new IOSAudio();
		
		Gdx.files = this.files;
		Gdx.graphics = this.graphics;
		Gdx.audio = this.audio;
		Gdx.input = this.input;
		
		this.input.setupPeripherals();

		// attach our view to window+controller and make it visible
		uiViewController.set_View(graphics);
		this.graphics.Run();
		this.uiWindow.MakeKeyAndVisible();
		Gdx.app.log("IOSApplication", "created");
		return true;
	}

	/**
	 * Returns our real display dimension based on screen orientation.
	 * 
	 * @param viewController  The view controller.
	 * @return  Or real display dimension.
	 */
	RectangleF getBounds(UIViewController viewController) {
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
		Gdx.app.log("IOSApplication", "View: " + orientation.toString() + " " + width + "x" + height);
		
		// return resulting view size (based on orientation)
		return new RectangleF(0, 0, width, height);
	}
	
	@Override
	public void OnActivated(UIApplication uiApp) {
		Gdx.app.log("IOSApplication", "resumed");
		if(!firstResume) {
			graphics.MakeCurrent();
			listener.resume();
			firstResume = true;
		}
	}

	@Override
	public void OnResignActivation(UIApplication uiApp) {
		Gdx.app.log("IOSApplication", "paused");
		graphics.MakeCurrent();
		listener.pause();
		Gdx.gl.glFlush();
	}

	@Override
	public void WillTerminate(UIApplication uiApp) {
		Gdx.app.log("IOSApplication", "disposed");
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
	public Net getNet() {
		return net;
	}

	@Override
	public void log (String tag, String message) {
		if(logLevel > LOG_NONE) {
			Console.WriteLine("[info] " + tag + ": " + message);
		}
	}

	@Override
	public void log (String tag, String message, Exception exception) {
		if(logLevel > LOG_NONE) {
			Console.WriteLine("[info] " + tag + ": " + message);
			exception.printStackTrace();
		}
	}

	@Override
	public void error (String tag, String message) {
		if(logLevel >= LOG_ERROR) {
			Console.WriteLine("[error] " +  tag + ": " + message);
		}
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		if(logLevel >= LOG_ERROR) {
			Console.WriteLine("[error] " +  tag + ": " + message);
			exception.printStackTrace();
		}
	}

	@Override
	public void debug (String tag, String message) {
		if(logLevel >= LOG_DEBUG) {
			Console.WriteLine("[debug] " +  tag + ": " + message);
		}
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		if(logLevel >= LOG_DEBUG) {
			Console.WriteLine("[error] " +  tag + ": " + message);
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
		return 0;
	}

	@Override
	public long getJavaHeap () {
		return 0;
	}

	@Override
	public long getNativeHeap () {
		return 0;
	}

	@Override
	public Preferences getPreferences (String name) {
		return null;
	}

	@Override
	public void postRunnable (Runnable runnable) {
	}

	@Override
	public void exit () {	
		System.exit(0);
	}

	@Override
	public Clipboard getClipboard() {
		return new Clipboard() {
			@Override
			public void setContents(String content) {
			}
			
			@Override
			public String getContents() {
				return null;
			}
		};
	}
}