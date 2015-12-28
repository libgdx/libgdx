package com.badlogic.gdx.backends.lwjgl3;

import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwInit;

import java.io.File;

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
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALAudio;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.LongMap;
import com.badlogic.gdx.utils.ObjectMap;

public class Lwjgl3Application implements Application {
	private final Lwjgl3ApplicationConfiguration config;
	private final LongMap<Lwjgl3Window> windows = new LongMap<Lwjgl3Window>();
	private Lwjgl3Window currentWindow;
	private Audio audio;
	private final Files files;
	private final Net net;
	private final Lwjgl3Clipboard clipboard;
	private int logLevel = LOG_INFO;

	public Lwjgl3Application(ApplicationListener listener, Lwjgl3ApplicationConfiguration config) {
		Lwjgl3NativesLoader.load();
		this.config = config;
		if (!Lwjgl3ApplicationConfiguration.disableAudio) {
			try {
				this.audio = Gdx.audio = new OpenALAudio(config.audioDeviceSimultaneousSources,
						config.audioDeviceBufferCount, config.audioDeviceBufferSize);
			} catch (Throwable t) {
				log("Lwjgl3Application", "Couldn't initialize audio, disabling audio", t);
				this.audio = Gdx.audio = new MockAudio();
			}
		} else {
			this.audio = Gdx.audio = new MockAudio();
		}
		this.files = Gdx.files = new Lwjgl3Files();
		this.net = Gdx.net = new Lwjgl3Net();
		this.clipboard = new Lwjgl3Clipboard();

		if (glfwInit() != GLFW_TRUE) {
			throw new GdxRuntimeException("Unable to initialize GLFW");
		}
		
		createWindow(listener);
		loop();
	}
	
	private void createWindow(ApplicationListener listener) {
		
	}

	private void loop() {

	}

	@Override
	public ApplicationListener getApplicationListener() {
		return currentWindow.getListener();
	}

	@Override
	public Graphics getGraphics() {
		return currentWindow.getGraphics();
	}

	@Override
	public Audio getAudio() {
		return audio;
	}

	@Override
	public Input getInput() {
		return currentWindow.getInput();
	}

	@Override
	public Files getFiles() {
		return files;
	}

	@Override
	public Net getNet() {
		return net;
	}

	@Override
	public void debug(String tag, String message) {
		if (logLevel >= LOG_DEBUG) {
			System.out.println(tag + ": " + message);
		}
	}

	@Override
	public void debug(String tag, String message, Throwable exception) {
		if (logLevel >= LOG_DEBUG) {
			System.out.println(tag + ": " + message);
			exception.printStackTrace(System.out);
		}
	}

	@Override
	public void log(String tag, String message) {
		if (logLevel >= LOG_INFO) {
			System.out.println(tag + ": " + message);
		}
	}

	@Override
	public void log(String tag, String message, Throwable exception) {
		if (logLevel >= LOG_INFO) {
			System.out.println(tag + ": " + message);
			exception.printStackTrace(System.out);
		}
	}

	@Override
	public void error(String tag, String message) {
		if (logLevel >= LOG_ERROR) {
			System.err.println(tag + ": " + message);
		}
	}

	@Override
	public void error(String tag, String message, Throwable exception) {
		if (logLevel >= LOG_ERROR) {
			System.err.println(tag + ": " + message);
			exception.printStackTrace(System.err);
		}
	}

	@Override
	public void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public int getLogLevel() {
		return logLevel;
	}

	@Override
	public ApplicationType getType() {
		return ApplicationType.Desktop;
	}

	@Override
	public int getVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getJavaHeap() {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	@Override
	public long getNativeHeap() {
		return getJavaHeap();
	}

	ObjectMap<String, Preferences> preferences = new ObjectMap<String, Preferences>();

	@Override
	public Preferences getPreferences(String name) {
		if (preferences.containsKey(name)) {
			return preferences.get(name);
		} else {
			Preferences prefs = new Lwjgl3Preferences(
					new Lwjgl3FileHandle(new File(config.preferencesDirectory, name), config.preferencesFileType));
			preferences.put(name, prefs);
			return prefs;
		}
	}

	@Override
	public Clipboard getClipboard() {
		return clipboard;
	}

	@Override
	public void postRunnable(Runnable runnable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addLifecycleListener(LifecycleListener listener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void removeLifecycleListener(LifecycleListener listener) {
		// TODO Auto-generated method stub
	}
}
