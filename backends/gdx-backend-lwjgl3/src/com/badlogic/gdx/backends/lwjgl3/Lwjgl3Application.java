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
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;

import java.io.File;

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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.LongMap;
import com.badlogic.gdx.utils.ObjectMap;

public class Lwjgl3Application implements Application {
	private final Lwjgl3ApplicationConfiguration config;
	private final LongMap<Lwjgl3Window> windows = new LongMap<Lwjgl3Window>();
	private volatile Lwjgl3Window currentWindow;
	private Audio audio;
	private final Files files;
	private final Net net;
	private final ObjectMap<String, Preferences> preferences = new ObjectMap<String, Preferences>();
	private final Lwjgl3Clipboard clipboard;
	private int logLevel = LOG_INFO;
	private volatile boolean running = true;
	private final Array<Runnable> runnables = new Array<Runnable>();
	private final Array<LifecycleListener> lifecycleListeners = new Array<LifecycleListener>();
	private GLFWErrorCallback errorCallback;

	public Lwjgl3Application(ApplicationListener listener, Lwjgl3ApplicationConfiguration config) {
		Lwjgl3NativesLoader.load();
		this.config = config;
		Gdx.app = this;
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
		errorCallback = GLFWErrorCallback.createPrint(System.err);

		Lwjgl3Window window = createWindow(listener);
		windows.put(window.getWindowHandle(), window);
		loop();
	}

	private Lwjgl3Window createWindow(ApplicationListener listener) {
		long windowHandle = createGlfwWindow(config.resizable, config.r, config.g, config.b, config.a, config.stencil,
				config.depth, config.samples, config.width, config.height, config.title, config.fullscreen, config.x,
				config.y, config.vSyncEnabled, config.initialBackgroundColor, 0);
		Lwjgl3Window window = new Lwjgl3Window(windowHandle, listener, config);
		glfwShowWindow(window.getWindowHandle());
		return window;
	}

	private void loop() {
		while (running && windows.size > 0) {
			Array<Lwjgl3Window> closedWindows = new Array<Lwjgl3Window>();
			for (Lwjgl3Window window : windows.values()) {
				Gdx.graphics = window.getGraphics();
				Gdx.gl = window.getGraphics().getGL30() != null ? window.getGraphics().getGL30()
						: window.getGraphics().getGL20();
				Gdx.gl20 = window.getGraphics().getGL20();
				Gdx.gl30 = window.getGraphics().getGL30();
				Gdx.input = window.getInput();

				currentWindow = window;
				synchronized(lifecycleListeners) {
					window.update(lifecycleListeners);
				}

				if (window.shouldClose()) {
					closedWindows.add(window);
				}
			}
			
			synchronized(runnables) {
				for(Runnable runnable: runnables) {
					runnable.run();
				}
				runnables.clear();
			}

			for (Lwjgl3Window closedWindow : closedWindows) {
				closedWindow.dispose();
				windows.remove(closedWindow.getWindowHandle());
			}						
		}

		for (Lwjgl3Window window : windows.values()) {
			window.dispose();
		}
		if (audio instanceof OpenALAudio) {
			((OpenALAudio) audio).dispose();
		}
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
		synchronized(runnables) {
			runnables.add(runnable);
		}
	}

	@Override
	public void exit() {
		running = false;
	}

	@Override
	public void addLifecycleListener(LifecycleListener listener) {
		synchronized(lifecycleListeners) {
			lifecycleListeners.add(listener);
		}
	}

	@Override
	public void removeLifecycleListener(LifecycleListener listener) {
		synchronized(lifecycleListeners) {
			lifecycleListeners.add(listener);
		}
	}

	public static long createGlfwWindow(boolean resizable, int r, int g, int b, int a, int stencil, int depth,
			int samples, int width, int height, String title, boolean fullscreen, int x, int y, boolean vSyncEnabled,
			Color initialBackgroundColor, long sharedContextWindow) {
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);
		glfwWindowHint(GLFW_RED_BITS, r);
		glfwWindowHint(GLFW_GREEN_BITS, g);
		glfwWindowHint(GLFW_BLUE_BITS, b);
		glfwWindowHint(GLFW_ALPHA_BITS, a);
		glfwWindowHint(GLFW_STENCIL_BITS, stencil);
		glfwWindowHint(GLFW_DEPTH_BITS, depth);
		glfwWindowHint(GLFW_SAMPLES, samples);

		long windowHandle = glfwCreateWindow(width, height, title, fullscreen ? glfwGetPrimaryMonitor() : 0,
				sharedContextWindow);
		if (windowHandle == 0) {
			throw new GdxRuntimeException("Couldn't create window");
		}

		if (x == -1 && y == -1) {
			GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
			glfwSetWindowPos(windowHandle, vidMode.width() / 2 - width / 2, vidMode.height() / 2 - height / 2);
		} else {
			glfwSetWindowPos(windowHandle, x, y);
		}

		if (title != null) {
			glfwSetWindowTitle(windowHandle, title);
		}
		glfwSwapInterval(vSyncEnabled ? 1 : 0);
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
			GL11.glClearColor(initialBackgroundColor.r, initialBackgroundColor.g, initialBackgroundColor.b,
					initialBackgroundColor.a);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			glfwSwapBuffers(windowHandle);
		}		
		return windowHandle;
	}
}
