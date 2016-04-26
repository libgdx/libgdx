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

import java.io.File;

import com.badlogic.gdx.graphics.glutils.GLVersion;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.SharedLibraryLoader;

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
	private static GLFWErrorCallback errorCallback;
	private static GLVersion glVersion;

	static void initializeGlfw() {
		if (errorCallback == null) {
			Lwjgl3NativesLoader.load();
			errorCallback = GLFWErrorCallback.createPrint(System.err);
			GLFW.glfwSetErrorCallback(errorCallback);
			if (GLFW.glfwInit() != GLFW.GLFW_TRUE) {
				throw new GdxRuntimeException("Unable to initialize GLFW");
			}
		}
	}

	public Lwjgl3Application(ApplicationListener listener, Lwjgl3ApplicationConfiguration config) {
		initializeGlfw();
		this.config = Lwjgl3ApplicationConfiguration.copy(config);
		if (this.config.title == null) this.config.title = listener.getClass().getSimpleName();
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

		Lwjgl3Window window = createWindow(config, listener, 0);
		windows.add(window);
		try {
			loop();
		} catch(Throwable t) {
			if (t instanceof RuntimeException)
				throw (RuntimeException) t;
			else
				throw new GdxRuntimeException(t);
		} finally {
			cleanup();
		}
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
				Gdx.gl30 = window.getGraphics().getGL30();
				Gdx.gl20 = Gdx.gl30 != null ? Gdx.gl30 : window.getGraphics().getGL20();
				Gdx.gl = Gdx.gl30 != null ? Gdx.gl30 : Gdx.gl20;
				Gdx.input = window.getInput();

				GLFW.glfwMakeContextCurrent(window.getWindowHandle());
				currentWindow = window;
				synchronized (lifecycleListeners) {
					window.update(lifecycleListeners);
				}				
				if (window.shouldClose()) {
					closedWindows.add(window);
				}				
			}
			GLFW.glfwPollEvents();

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
		Lwjgl3Cursor.disposeSystemCursors();
		if (audio instanceof OpenALAudio) {
			((OpenALAudio) audio).dispose();
		}
		errorCallback.release();
		GLFW.glfwTerminate();
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
	
	/**
	 * Creates a new {@link Lwjgl3Window} using the provided listener and {@link Lwjgl3WindowConfiguration}.
	 */
	public Lwjgl3Window newWindow(ApplicationListener listener, Lwjgl3WindowConfiguration config) {
		Lwjgl3ApplicationConfiguration appConfig = Lwjgl3ApplicationConfiguration.copy(this.config);
		appConfig.setWindowedMode(config.windowWidth, config.windowHeight);
		appConfig.setWindowPosition(config.windowX, config.windowY);
		appConfig.setResizable(config.windowResizable);
		appConfig.setDecorated(config.windowDecorated);
		appConfig.setWindowListener(config.windowListener);
		appConfig.setFullscreenMode(config.fullscreenMode);
		appConfig.setTitle(config.title);
		appConfig.setInitialBackgroundColor(config.initialBackgroundColor);		
		Lwjgl3Window window = createWindow(appConfig, listener, windows.get(0).getWindowHandle());
		windows.add(window);
		return window;
	}

	private Lwjgl3Window createWindow(Lwjgl3ApplicationConfiguration config, ApplicationListener listener, long sharedContext) {
		long windowHandle = createGlfwWindow(config, sharedContext);
		Lwjgl3Window window = new Lwjgl3Window(windowHandle, listener, config);
		window.setVisible(true);
		return window;
	}

	static long createGlfwWindow(Lwjgl3ApplicationConfiguration config, long sharedContextWindow) {
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, config.windowResizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
		
		if(sharedContextWindow == 0) {
			GLFW.glfwWindowHint(GLFW.GLFW_RED_BITS, config.r);
			GLFW.glfwWindowHint(GLFW.GLFW_GREEN_BITS, config.g);
			GLFW.glfwWindowHint(GLFW.GLFW_BLUE_BITS, config.b);
			GLFW.glfwWindowHint(GLFW.GLFW_ALPHA_BITS, config.a);
			GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, config.stencil);
			GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, config.depth);
			GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, config.samples);
		}

		if (config.useGL30) {
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, config.gles30ContextMajorVersion);
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, config.gles30ContextMinorVersion);
			if (SharedLibraryLoader.isMac) {
				// hints mandatory on OS X for GL 3.2+ context creation, but fail on Windows if the
				// WGL_ARB_create_context extension is not available
				// see: http://www.glfw.org/docs/latest/compat.html
				GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
				GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
			}
		}

		long windowHandle = 0;
		
		if(config.fullscreenMode != null) {
			// glfwWindowHint(GLFW.GLFW_REFRESH_RATE, config.fullscreenMode.refreshRate);
			windowHandle = GLFW.glfwCreateWindow(config.fullscreenMode.width, config.fullscreenMode.height, config.title, config.fullscreenMode.getMonitor(), sharedContextWindow);
		} else {
			GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, config.windowDecorated? GLFW.GLFW_TRUE: GLFW.GLFW_FALSE);
			windowHandle = GLFW.glfwCreateWindow(config.windowWidth, config.windowHeight, config.title, 0, sharedContextWindow);			
		}
		if (windowHandle == 0) {
			throw new GdxRuntimeException("Couldn't create window");
		}
		if (config.fullscreenMode == null) {
			if (config.windowX == -1 && config.windowY == -1) {
				GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
				GLFW.glfwSetWindowPos(windowHandle, vidMode.width() / 2 - config.windowWidth / 2, vidMode.height() / 2 - config.windowHeight / 2);
			} else {
				GLFW.glfwSetWindowPos(windowHandle, config.windowX, config.windowY);
			}
		}
		GLFW.glfwMakeContextCurrent(windowHandle);
		GLFW.glfwSwapInterval(config.vSyncEnabled ? 1 : 0);
		GL.createCapabilities();

		initiateGL();
		if (!glVersion.isVersionEqualToOrHigher(2, 0))
			throw new GdxRuntimeException("OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: "
					+ GL11.glGetString(GL11.GL_VERSION) + "\n" + glVersion.getDebugVersionString());

		if (!supportsFBO()) {
			throw new GdxRuntimeException("OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: "
					+ GL11.glGetString(GL11.GL_VERSION) + ", FBO extension: false\n" + glVersion.getDebugVersionString());
		}

		for (int i = 0; i < 2; i++) {
			GL11.glClearColor(config.initialBackgroundColor.r, config.initialBackgroundColor.g, config.initialBackgroundColor.b,
					config.initialBackgroundColor.a);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			GLFW.glfwSwapBuffers(windowHandle);
		}
		return windowHandle;
	}

	private static void initiateGL () {
		String versionString = GL11.glGetString(GL11.GL_VERSION);
		String vendorString = GL11.glGetString(GL11.GL_VENDOR);
		String rendererString = GL11.glGetString(GL11.GL_RENDERER);
		glVersion = new GLVersion(Application.ApplicationType.Desktop, versionString, vendorString, rendererString);
	}

	private static boolean supportsFBO () {
		// FBO is in core since OpenGL 3.0, see https://www.opengl.org/wiki/Framebuffer_Object
		return glVersion.isVersionEqualToOrHigher(3, 0) || GLFW.glfwExtensionSupported("GL_EXT_framebuffer_object") == GLFW.GLFW_TRUE
				|| GLFW.glfwExtensionSupported("GL_ARB_framebuffer_object") == GLFW.GLFW_TRUE;
	}

}
