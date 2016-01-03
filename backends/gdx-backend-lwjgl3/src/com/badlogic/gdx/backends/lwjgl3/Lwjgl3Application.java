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

package com.badlogic.gdx.backends.lwjgl3;

import static org.lwjgl.glfw.GLFW.GLFW_ALPHA_BITS;
import static org.lwjgl.glfw.GLFW.GLFW_BLUE_BITS;
import static org.lwjgl.glfw.GLFW.GLFW_DEPTH_BITS;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_GREEN_BITS;
import static org.lwjgl.glfw.GLFW.GLFW_RED_BITS;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_STENCIL_BITS;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwExtensionSupported;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;

import java.io.File;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;

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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.LongMap;
import com.badlogic.gdx.utils.ObjectMap;

public class Lwjgl3Application implements Application {
	private final Lwjgl3ApplicationConfiguration config;
	private final Array<Lwjgl3Window> windows = new Array<Lwjgl3Window>();
	private volatile Lwjgl3Window currentWindow;
	private Audio audio;
	private final Files files;
	private final Net net;
	private final ObjectMap<String, Preferences> preferences = new ObjectMap<String, Preferences>();
	private final Lwjgl3Clipboard clipboard;
	private int logLevel = LOG_INFO;
	private volatile boolean running = true;
	private final Array<Runnable> runnables = new Array<Runnable>();
	private final Array<Runnable> executedRunnables = new Array<Runnable>();	
	private final Array<LifecycleListener> lifecycleListeners = new Array<LifecycleListener>();
	@SuppressWarnings("unused")
	private static GLFWErrorCallback errorCallback;

	static void initializeGlfw() {
		if (errorCallback == null) {
			Lwjgl3NativesLoader.load();
			errorCallback = GLFWErrorCallback.createPrint(System.err);
			if (glfwInit() != GLFW_TRUE) {
				throw new GdxRuntimeException("Unable to initialize GLFW");
			}
		}
	}

	public Lwjgl3Application(ApplicationListener listener, Lwjgl3ApplicationConfiguration config) {
		initializeGlfw();
		this.config = Lwjgl3ApplicationConfiguration.copy(config);
		Gdx.app = this;
		if (!config.disableAudio) {
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

		Lwjgl3Window window = createWindow(config, listener);
		windows.add(window);
		loop();
		cleanup();
	}

	private void loop() {
		Array<Lwjgl3Window> closedWindows = new Array<Lwjgl3Window>();
		while (running && windows.size > 0) {
			// FIXME put it on a separate thread
			if (audio instanceof OpenALAudio) {
				((OpenALAudio) audio).update();
			}

			closedWindows.clear();
			for (Lwjgl3Window window : windows) {
				Gdx.graphics = window.getGraphics();
				Gdx.gl = window.getGraphics().getGL30() != null ? window.getGraphics().getGL30()
						: window.getGraphics().getGL20();
				Gdx.gl20 = window.getGraphics().getGL20();
				Gdx.gl30 = window.getGraphics().getGL30();
				Gdx.input = window.getInput();

				currentWindow = window;
				synchronized (lifecycleListeners) {
					window.update(lifecycleListeners);
				}

				if (window.shouldClose()) {
					closedWindows.add(window);
				}
			}

			synchronized (runnables) {
				executedRunnables.clear();
				executedRunnables.addAll(runnables);
				runnables.clear();
			}
			for (Runnable runnable : executedRunnables) {
				runnable.run();
			}

			for (Lwjgl3Window closedWindow : closedWindows) {
				closedWindow.dispose();				
				windows.removeValue(closedWindow, false);
			}
		}
	}

	private void cleanup() {
		for (Lwjgl3Window window : windows) {
			window.dispose();
		}
		if (audio instanceof OpenALAudio) {
			((OpenALAudio) audio).dispose();
		}
		errorCallback.release();
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
		synchronized (runnables) {
			runnables.add(runnable);
		}
	}

	@Override
	public void exit() {
		running = false;
	}

	@Override
	public void addLifecycleListener(LifecycleListener listener) {
		synchronized (lifecycleListeners) {
			lifecycleListeners.add(listener);
		}
	}

	@Override
	public void removeLifecycleListener(LifecycleListener listener) {
		synchronized (lifecycleListeners) {
			lifecycleListeners.add(listener);
		}
	}

	private Lwjgl3Window createWindow(Lwjgl3ApplicationConfiguration config, ApplicationListener listener) {
		long windowHandle = createGlfwWindow(config, 0);
		Lwjgl3Window window = new Lwjgl3Window(windowHandle, listener, config);
		glfwShowWindow(windowHandle);
		return window;
	}

	public static long createGlfwWindow(Lwjgl3ApplicationConfiguration config, long sharedContextWindow) {
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, config.windowResizable ? GLFW_TRUE : GLFW_FALSE);
		
		if(sharedContextWindow == 0) {
			glfwWindowHint(GLFW_RED_BITS, config.r);
			glfwWindowHint(GLFW_GREEN_BITS, config.g);
			glfwWindowHint(GLFW_BLUE_BITS, config.b);
			glfwWindowHint(GLFW_ALPHA_BITS, config.a);
			glfwWindowHint(GLFW_STENCIL_BITS, config.stencil);
			glfwWindowHint(GLFW_DEPTH_BITS, config.depth);
			glfwWindowHint(GLFW_SAMPLES, config.samples);
		}

		long windowHandle = 0;
		
		if(config.fullscreenMode != null) {
			// glfwWindowHint(GLFW.GLFW_REFRESH_RATE, config.fullscreenMode.refreshRate);
			windowHandle = glfwCreateWindow(config.fullscreenMode.width, config.fullscreenMode.height, config.title, config.fullscreenMode.getMonitor(), sharedContextWindow);
		} else {
			windowHandle = glfwCreateWindow(config.windowWidth, config.windowHeight, config.title, 0, sharedContextWindow);			
		}
		if (windowHandle == 0) {
			throw new GdxRuntimeException("Couldn't create window");
		}
		if (config.fullscreenMode != null) {
			if (config.windowX == -1 && config.windowY == -1) {
				GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
				glfwSetWindowPos(windowHandle, vidMode.width() / 2 - config.windowWidth / 2, vidMode.height() / 2 - config.windowHeight / 2);
			} else {
				glfwSetWindowPos(windowHandle, config.windowX, config.windowHeight);
			}
		}
		glfwSwapInterval(config.vSyncEnabled ? 1 : 0);
		glfwMakeContextCurrent(windowHandle);
		GL.createCapabilities();
		
		String version = GL11.glGetString(GL20.GL_VERSION);
		int glMajorVersion = Integer.parseInt("" + version.charAt(0));
		if (glMajorVersion <= 1)
			throw new GdxRuntimeException(
					"OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: " + version);
		if (glMajorVersion == 2 || version.contains("2.1")) {
			if (glfwExtensionSupported("GL_EXT_framebuffer_object") == GLFW_FALSE
					&& glfwExtensionSupported("GL_ARB_framebuffer_object") == GLFW_FALSE) {
				throw new GdxRuntimeException(
						"OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: " + version
								+ ", FBO extension: false");
			}
		}
		for (int i = 0; i < 2; i++) {
			GL11.glClearColor(config.initialBackgroundColor.r, config.initialBackgroundColor.g, config.initialBackgroundColor.b,
					config.initialBackgroundColor.a);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			glfwSwapBuffers(windowHandle);
		}
		Gdx.app.log("Lwjgl3Application", "Created window handle 0x" + Long.toHexString(windowHandle));
		return windowHandle;
	}
}
