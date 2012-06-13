package com.badlogic.gdx.backends.ios;

import cli.MonoTouch.UIKit.UIApplication;
import cli.MonoTouch.UIKit.UIApplicationDelegate;
import cli.MonoTouch.UIKit.UIScreen;
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
import com.badlogic.gdx.Preferences;

public class IOSApplication implements Application {
	final UIApplication uiApp;
	final UIWindow uiWindow;
	final ApplicationListener listener;
	final IOSGraphics graphics;
	final IOSAudio audio;
	final IOSFiles files;
	final IOSInput input;
	int logLevel = Application.LOG_DEBUG;
	
	/**
	 * Should be called in AppDelegate#FinishedLaunching
	 * @param uiApp
	 */
	public IOSApplication(UIApplication uiApp, ApplicationListener listener) {
		this.uiApp = uiApp;
		this.listener = listener;
		
		Gdx.app = this;
		
		RectangleF bounds = UIScreen.get_MainScreen().get_Bounds();
		this.graphics = new IOSGraphics(bounds, this);
		this.files = new IOSFiles();
		this.audio = new IOSAudio();
		this.input = new IOSInput();
		
		Gdx.files = this.files;
		Gdx.graphics = this.graphics;
		Gdx.audio = this.audio;
		Gdx.input = this.input;
		
		this.uiWindow = new UIWindow(bounds);
		this.uiWindow.Add(graphics);
		this.graphics.Run();
		this.uiWindow.MakeKeyAndVisible();
	}
	
	@Override
	public Graphics getGraphics () {
		return null;
	}

	@Override
	public Audio getAudio () {
		return null;
	}

	@Override
	public Input getInput () {
		return null;
	}

	@Override
	public Files getFiles () {
		return null;
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
	}
}
