package com.badlogic.gdx.backends.ios;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

public class IOSApplication implements Application {
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
		System.out.println( message);
	}

	@Override
	public void log (String tag, String message, Exception exception) {
		
	}

	@Override
	public void error (String tag, String message) {
		
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		
	}

	@Override
	public void debug (String tag, String message) {
		
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		
	}

	@Override
	public void setLogLevel (int logLevel) {
		
	}

	@Override
	public ApplicationType getType () {
		return null;
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
