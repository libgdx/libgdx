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

package com.badlogic.gdx.backends.sdl3;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.IntBuffer;

import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.backends.sdl3.Lwjgl3ApplicationConfiguration.GLEmulation;
import com.badlogic.gdx.backends.sdl3.audio.Lwjgl3Audio;
import com.badlogic.gdx.backends.sdl3.audio.OpenALLwjgl3Audio;
import com.badlogic.gdx.graphics.glutils.GLVersion;

import org.lwjgl.opengl.AMDDebugOutput;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.opengl.KHRDebug;
import org.lwjgl.sdl.*;
import org.lwjgl.system.Callback;

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
import com.badlogic.gdx.backends.sdl3.audio.mock.MockAudio;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.badlogic.gdx.utils.Os;

public class Lwjgl3Application implements Lwjgl3ApplicationBase {
	private final Lwjgl3ApplicationConfiguration config;
	final Array<Lwjgl3Window> windows = new Array<Lwjgl3Window>();
	private volatile Lwjgl3Window currentWindow;
	private Lwjgl3Audio audio;
	private final Files files;
	private final Net net;
	private final ObjectMap<String, Preferences> preferences = new ObjectMap<String, Preferences>();
	private final Lwjgl3Clipboard clipboard;
	private int logLevel = LOG_INFO;
	private ApplicationLogger applicationLogger;
	private volatile boolean running = true;
	private final Array<Runnable> runnables = new Array<Runnable>();
	private final Array<Runnable> executedRunnables = new Array<Runnable>();
	private final Array<LifecycleListener> lifecycleListeners = new Array<LifecycleListener>();
	private static boolean initialized = false;
	private static GLVersion glVersion;
	private static Callback glDebugCallback;
	private final Sync sync;
	private Lwjgl3Window mouseHoverWindow = null, inputFocusWindow = null;

	static void initializeSDL () {
		if (!initialized) {
			Lwjgl3NativesLoader.load();
			initialized = true;
			if (!SDLInit.SDL_Init(SDLInit.SDL_INIT_VIDEO)) {
				throw new GdxRuntimeException("Unable to initialize SDL3");
			}
		}
	}

	static void loadANGLE () {
		SDLHints.SDL_SetHint(SDLHints.SDL_HINT_OPENGL_ES_DRIVER, "1");
		try {
			Class angleLoader = Class.forName("com.badlogic.gdx.backends.lwjgl3.angle.ANGLELoader");
			Method load = angleLoader.getMethod("load");
			load.invoke(angleLoader);
		} catch (ClassNotFoundException t) {
			return;
		} catch (Throwable t) {
			throw new GdxRuntimeException("Couldn't load ANGLE.", t);
		}
	}

	static void postLoadANGLE () {
		try {
			Class angleLoader = Class.forName("com.badlogic.gdx.backends.lwjgl3.angle.ANGLELoader");
			Method load = angleLoader.getMethod("postGlfwInit");
			load.invoke(angleLoader);
		} catch (ClassNotFoundException t) {
			return;
		} catch (Throwable t) {
			throw new GdxRuntimeException("Couldn't load ANGLE.", t);
		}
	}

	private Lwjgl3Window findWindow (int id) {
		for (Lwjgl3Window window : windows) {
			if (window.sdlID == id) {
				return window;
			}
		}
		return null;
	}

	public Lwjgl3Application (ApplicationListener listener) {
		this(listener, new Lwjgl3ApplicationConfiguration());
	}

	public Lwjgl3Application (ApplicationListener listener, Lwjgl3ApplicationConfiguration config) {
		if (config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20) loadANGLE();
		initializeSDL();
		setApplicationLogger(new Lwjgl3ApplicationLogger());

		this.config = config = Lwjgl3ApplicationConfiguration.copy(config);
		if (config.title == null) config.title = listener.getClass().getSimpleName();

		Gdx.app = this;
		if (!config.disableAudio) {
			try {
				this.audio = createAudio(config);
			} catch (Throwable t) {
				log("Lwjgl3Application", "Couldn't initialize audio, disabling audio", t);
				this.audio = new MockAudio();
			}
		} else {
			this.audio = new MockAudio();
		}
		Gdx.audio = audio;
		this.files = Gdx.files = createFiles();
		this.net = Gdx.net = new Lwjgl3Net(config);
		this.clipboard = new Lwjgl3Clipboard();

		this.sync = new Sync();

		Lwjgl3Window window = createWindow(config, listener, null);
		if (config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20) postLoadANGLE();
		windows.add(window);

		SDLEvents.SDL_SetEventFilter(new SDL_EventFilterI() {
			@Override
			public boolean invoke (long ignored, long eventAddress) {
				SDL_Event event = SDL_Event.create(eventAddress);
				if (event.type() == SDLEvents.SDL_EVENT_WINDOW_EXPOSED) {
					findWindow(event.window().windowID()).requestRendering();
					return false;
				}
				return true;
			}
		}, 0);

		try {
			loop();
			cleanupWindows();
		} catch (Throwable t) {
			if (t instanceof RuntimeException)
				throw (RuntimeException)t;
			else
				throw new GdxRuntimeException(t);
		} finally {
			cleanup();
		}
	}

	protected void loop () {
		Array<Lwjgl3Window> closedWindows = new Array<Lwjgl3Window>();
		SDL_Event event = SDL_Event.calloc();
		while (running && windows.size > 0) {
			// FIXME put it on a separate thread
			audio.update();

			boolean haveWindowsRendered = false;
			closedWindows.clear();
			int targetFramerate = -2;
			for (Lwjgl3Window window : windows) {
				if (currentWindow != window) {
					window.makeCurrent();
					currentWindow = window;
				}
				if (targetFramerate == -2) targetFramerate = window.getConfig().foregroundFPS;
				synchronized (lifecycleListeners) {
					haveWindowsRendered |= window.update();
				}
				if (window.shouldClose()) {
					closedWindows.add(window);
				}
			}
			while (SDLEvents.SDL_PollEvent(event)) {
				Lwjgl3Window window = null;
				if (event.type() >= SDLEvents.SDL_EVENT_WINDOW_FIRST && event.type() <= SDLEvents.SDL_EVENT_WINDOW_LAST) {
					window = findWindow(event.window().windowID());
				} else if (event.type() == SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN
					|| event.type() == SDLEvents.SDL_EVENT_MOUSE_BUTTON_UP) {
					window = findWindow(event.button().windowID());
				} else if (event.type() == SDLEvents.SDL_EVENT_MOUSE_WHEEL) {
					window = findWindow(event.wheel().windowID());
				} else if (event.type() == SDLEvents.SDL_EVENT_MOUSE_MOTION) {
					window = findWindow(event.motion().windowID());
				} else if (event.type() == SDLEvents.SDL_EVENT_KEY_DOWN || event.type() == SDLEvents.SDL_EVENT_KEY_UP) {
					window = findWindow(event.key().windowID());
				} else if (event.type() == SDLEvents.SDL_EVENT_TEXT_INPUT) {
					window = findWindow(event.text().windowID());
				} else if (event.type() == SDLEvents.SDL_EVENT_DROP_BEGIN || event.type() == SDLEvents.SDL_EVENT_DROP_FILE
					|| event.type() == SDLEvents.SDL_EVENT_DROP_COMPLETE) {
					window = findWindow(event.text().windowID());
				}
				if (window != null) {
					switch (event.type()) {
					case SDLEvents.SDL_EVENT_WINDOW_RESIZED:
					case SDLEvents.SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED:
						window.getGraphics().resizeCallback(window.getWindowHandle());
						window.refreshCallback();
						break;
					case SDLEvents.SDL_EVENT_WINDOW_MINIMIZED:
						window.iconifyCallback(true);
						break;
					case SDLEvents.SDL_EVENT_WINDOW_MAXIMIZED:
						window.maximizeCallback(true);
						break;
					case SDLEvents.SDL_EVENT_WINDOW_RESTORED:
						if (window.iconified) {
							window.iconifyCallback(false);
						} else {
							window.maximizeCallback(false);
						}
						break;
					case SDLEvents.SDL_EVENT_MOUSE_MOTION:
						window.getInput().cursorPosCallback(window.getWindowHandle(), event.motion().x(), event.motion().y());
						break;
					case SDLEvents.SDL_EVENT_MOUSE_WHEEL:
						window.getInput().scrollCallback(window.getWindowHandle(), event.wheel().x(), event.wheel().y());
						break;
					case SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN:
					case SDLEvents.SDL_EVENT_MOUSE_BUTTON_UP:
						window.getInput().mouseButtonCallback(window.getWindowHandle(), event.button().button(), event.button().down());
						break;
					case SDLEvents.SDL_EVENT_TEXT_INPUT:
						window.getInput().charCallback(window.getWindowHandle(), event.text().textString().codePointAt(0));
						break;
					case SDLEvents.SDL_EVENT_KEY_DOWN:
					case SDLEvents.SDL_EVENT_KEY_UP:
						window.getInput().keyCallback(window.getWindowHandle(), event.key().key(), event.key().scancode(),
							event.key().mod(), event.key().repeat(), event.key().down());
						break;
					case SDLEvents.SDL_EVENT_WINDOW_MOUSE_ENTER:
						mouseHoverWindow = window;
						mouseHoverWindow.isMouseInside = true;
						break;
					case SDLEvents.SDL_EVENT_WINDOW_MOUSE_LEAVE:
						if (mouseHoverWindow != null) {
							mouseHoverWindow.isMouseInside = false;
						}
						mouseHoverWindow = null;
						break;
					case SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED:
						if (inputFocusWindow != null) {
							SDLKeyboard.SDL_StopTextInput(inputFocusWindow.getWindowHandle());
						}
						inputFocusWindow = window;
						SDLKeyboard.SDL_StartTextInput(inputFocusWindow.getWindowHandle());
						window.focusCallback(event.type() == SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED);
						break;
					case SDLEvents.SDL_EVENT_WINDOW_FOCUS_LOST:
						if (inputFocusWindow != null) {
							SDLKeyboard.SDL_StopTextInput(inputFocusWindow.getWindowHandle());
						}
						inputFocusWindow = null;
						window.focusCallback(event.type() == SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED);
						break;
					case SDLEvents.SDL_EVENT_DROP_BEGIN:
						window.dropClear();
						break;
					case SDLEvents.SDL_EVENT_DROP_COMPLETE:
						window.dropCallback();
						break;
					case SDLEvents.SDL_EVENT_DROP_FILE:
						window.dropFile(event.drop().dataString());
						break;
					case SDLEvents.SDL_EVENT_WINDOW_CLOSE_REQUESTED:
						window.closeWindow();
						break;
					}
					if (event.type() == SDLEvents.SDL_EVENT_QUIT) {
						running = false;
					}
				}
			}

			boolean shouldRequestRendering;
			synchronized (runnables) {
				shouldRequestRendering = runnables.size > 0;
				executedRunnables.clear();
				executedRunnables.addAll(runnables);
				runnables.clear();
			}
			for (Runnable runnable : executedRunnables) {
				runnable.run();
			}
			if (shouldRequestRendering) {
				// Must follow Runnables execution so changes done by Runnables are reflected
				// in the following render.
				for (Lwjgl3Window window : windows) {
					if (!window.getGraphics().isContinuousRendering()) window.requestRendering();
				}
			}

			for (Lwjgl3Window closedWindow : closedWindows) {
				if (windows.size == 1) {
					// Lifecycle listener methods have to be called before ApplicationListener methods. The
					// application will be disposed when _all_ windows have been disposed, which is the case,
					// when there is only 1 window left, which is in the process of being disposed.
					for (int i = lifecycleListeners.size - 1; i >= 0; i--) {
						LifecycleListener l = lifecycleListeners.get(i);
						l.pause();
						l.dispose();
					}
					lifecycleListeners.clear();
				}
				closedWindow.dispose();

				windows.removeValue(closedWindow, false);
			}

			if (!haveWindowsRendered) {
				// Sleep a few milliseconds in case no rendering was requested
				// with continuous rendering disabled.
				try {
					Thread.sleep(1000 / config.idleFPS);
				} catch (InterruptedException e) {
					// ignore
				}
			} else if (targetFramerate > 0) {
				sync.sync(targetFramerate); // sleep as needed to meet the target framerate
			}
		}
		event.free();
	}

	protected void cleanupWindows () {
		synchronized (lifecycleListeners) {
			for (LifecycleListener lifecycleListener : lifecycleListeners) {
				lifecycleListener.pause();
				lifecycleListener.dispose();
			}
		}
		for (Lwjgl3Window window : windows) {
			window.dispose();
		}
		windows.clear();
	}

	protected void cleanup () {
		Lwjgl3Cursor.disposeSystemCursors();
		audio.dispose();
		if (glDebugCallback != null) {
			glDebugCallback.free();
			glDebugCallback = null;
		}
		SDLInit.SDL_Quit();
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return currentWindow.getListener();
	}

	@Override
	public Graphics getGraphics () {
		return currentWindow.getGraphics();
	}

	@Override
	public Audio getAudio () {
		return audio;
	}

	@Override
	public Input getInput () {
		return currentWindow.getInput();
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
		return ApplicationType.Desktop;
	}

	@Override
	public int getVersion () {
		return 0;
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
	public Clipboard getClipboard () {
		return clipboard;
	}

	@Override
	public void postRunnable (Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
		}
	}

	@Override
	public void exit () {
		running = false;
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

	@Override
	public Lwjgl3Audio createAudio (Lwjgl3ApplicationConfiguration config) {
		return new OpenALLwjgl3Audio(config.audioDeviceSimultaneousSources, config.audioDeviceBufferCount,
			config.audioDeviceBufferSize);
	}

	@Override
	public Lwjgl3Input createInput (Lwjgl3Window window) {
		return new DefaultLwjgl3Input(window);
	}

	protected Files createFiles () {
		return new Lwjgl3Files();
	}

	/** Creates a new {@link Lwjgl3Window} using the provided listener and {@link Lwjgl3WindowConfiguration}.
	 *
	 * This function only just instantiates a {@link Lwjgl3Window} and returns immediately. The actual window creation is postponed
	 * with {@link Application#postRunnable(Runnable)} until after all existing windows are updated. */
	public Lwjgl3Window newWindow (ApplicationListener listener, Lwjgl3WindowConfiguration config) {
		Lwjgl3ApplicationConfiguration appConfig = Lwjgl3ApplicationConfiguration.copy(this.config);
		appConfig.setWindowConfiguration(config);
		if (appConfig.title == null) appConfig.title = listener.getClass().getSimpleName();
		return createWindow(appConfig, listener, windows.get(0));
	}

	private Lwjgl3Window createWindow (final Lwjgl3ApplicationConfiguration config, ApplicationListener listener,
		final Lwjgl3Window sharedContext) {
		final Lwjgl3Window window = new Lwjgl3Window(listener, lifecycleListeners, config, this);
		long currentWindow = sharedContext != null ? SDLVideo.SDL_GL_GetCurrentWindow() : 0;
		long currentContext = sharedContext != null ? SDLVideo.SDL_GL_GetCurrentContext() : 0;
		createWindow(window, config, sharedContext);
		// No need for postRunnable
		if (currentContext != 0) {
			windows.add(window);
			SDLVideo.SDL_GL_MakeCurrent(currentWindow, currentContext);
		}
		return window;
	}

	void createWindow (Lwjgl3Window window, Lwjgl3ApplicationConfiguration config, Lwjgl3Window sharedContext) {
		long[] windowData = createSDLWindow(config, sharedContext);
		long windowHandle = windowData[0];
		long glContext = windowData[1];
		window.create(windowHandle, glContext);
		window.setVisible(config.initialVisible);
		window.autoIconify = config.autoIconify;

		for (int i = 0; i < 2; i++) {
			window.getGraphics().gl20.glClearColor(config.initialBackgroundColor.r, config.initialBackgroundColor.g,
				config.initialBackgroundColor.b, config.initialBackgroundColor.a);
			window.getGraphics().gl20.glClear(GL11.GL_COLOR_BUFFER_BIT);
			SDLVideo.SDL_GL_SwapWindow(windowHandle);
		}

		if (currentWindow != null) {
			currentWindow.makeCurrent();
		}
	}

	static long[] createSDLWindow (Lwjgl3ApplicationConfiguration config, Lwjgl3Window sharedContextWindow) {

		SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_RED_SIZE, config.r);
		SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_GREEN_SIZE, config.g);
		SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_BLUE_SIZE, config.b);
		SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_ALPHA_SIZE, config.a);
		SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_STENCIL_SIZE, config.stencil);
		SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_DEPTH_SIZE, config.depth);
		SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_MULTISAMPLESAMPLES, config.samples);
		SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_DOUBLEBUFFER, 1);
		SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_SHARE_WITH_CURRENT_CONTEXT, 1);

		int glContextFlags = 0;

		if (config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.GL30
			|| config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.GL31
			|| config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.GL32) {
			SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_MAJOR_VERSION, config.gles30ContextMajorVersion);
			SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_MINOR_VERSION, config.gles30ContextMinorVersion);
			if (SharedLibraryLoader.os == Os.MacOsX) {
				// hints mandatory on OS X for GL 3.2+ context creation, but fail on Windows if the
				// WGL_ARB_create_context extension is not available
				// see: http://www.glfw.org/docs/latest/compat.html
				glContextFlags |= SDLVideo.SDL_GL_CONTEXT_FORWARD_COMPATIBLE_FLAG;
				SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_PROFILE_MASK, SDLVideo.SDL_GL_CONTEXT_PROFILE_CORE);
			}
		} else {
			if (config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20) {
				SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_EGL_PLATFORM, 1);
				SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_PROFILE_MASK, SDLVideo.SDL_GL_CONTEXT_PROFILE_ES);
				SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_MAJOR_VERSION, 2);
				SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_MINOR_VERSION, 0);
			}
		}

		if (config.debug) {
			glContextFlags |= SDLVideo.SDL_GL_CONTEXT_DEBUG_FLAG;
		}

		if (glContextFlags != 0) {
			SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_FLAGS, glContextFlags);
		}

		int props = SDLProperties.SDL_CreateProperties();
		SDLProperties.SDL_SetBooleanProperty(props, SDLVideo.SDL_PROP_WINDOW_CREATE_BORDERLESS_BOOLEAN, !config.windowDecorated);
		SDLProperties.SDL_SetBooleanProperty(props, SDLVideo.SDL_PROP_WINDOW_CREATE_RESIZABLE_BOOLEAN, config.windowResizable);
		SDLProperties.SDL_SetBooleanProperty(props, SDLVideo.SDL_PROP_WINDOW_CREATE_MAXIMIZED_BOOLEAN, config.windowMaximized);
		SDLProperties.SDL_SetBooleanProperty(props, SDLVideo.SDL_PROP_WINDOW_CREATE_TRANSPARENT_BOOLEAN,
			config.transparentFramebuffer);
		SDLProperties.SDL_SetBooleanProperty(props, SDLVideo.SDL_PROP_WINDOW_CREATE_HIDDEN_BOOLEAN, true);
		SDLProperties.SDL_SetBooleanProperty(props, SDLVideo.SDL_PROP_WINDOW_CREATE_OPENGL_BOOLEAN, true);
		SDLProperties.SDL_SetBooleanProperty(props, SDLVideo.SDL_PROP_WINDOW_CREATE_HIGH_PIXEL_DENSITY_BOOLEAN, true);

		SDLProperties.SDL_SetNumberProperty(props, SDLVideo.SDL_PROP_WINDOW_CREATE_WIDTH_NUMBER, config.windowWidth);
		SDLProperties.SDL_SetNumberProperty(props, SDLVideo.SDL_PROP_WINDOW_CREATE_HEIGHT_NUMBER, config.windowHeight);

		SDLProperties.SDL_SetStringProperty(props, SDLVideo.SDL_PROP_WINDOW_CREATE_TITLE_STRING, config.title);

		if (config.fullscreenMode == null) {
			int windowX = config.windowX;
			int windowY = config.windowY;
			if (config.windowX == -1 && config.windowY == -1) { // i.e., center the window
				int windowWidth = Math.max(config.windowWidth, config.windowMinWidth);
				int windowHeight = Math.max(config.windowHeight, config.windowMinHeight);
				if (config.windowMaxWidth > -1) windowWidth = Math.min(windowWidth, config.windowMaxWidth);
				if (config.windowMaxHeight > -1) windowHeight = Math.min(windowHeight, config.windowMaxHeight);

				int monitorHandle = SDLVideo.SDL_GetPrimaryDisplay();
				if (config.windowMaximized && config.maximizedMonitor != null) {
					monitorHandle = config.maximizedMonitor.monitorHandle;
				}

				// If the primary monitor is unavailable, use (0, 0) as a fallback
				if (monitorHandle == 0) {
					windowX = 0;
					windowY = 0;
				} else {
					GridPoint2 newPos = Lwjgl3ApplicationConfiguration.calculateCenteredWindowPosition(
						Lwjgl3ApplicationConfiguration.toLwjgl3Monitor(monitorHandle), windowWidth, windowHeight);
					windowX = newPos.x;
					windowY = newPos.y;
				}
			}
			SDLProperties.SDL_SetNumberProperty(props, SDLVideo.SDL_PROP_WINDOW_CREATE_X_NUMBER, windowX);
			SDLProperties.SDL_SetNumberProperty(props, SDLVideo.SDL_PROP_WINDOW_CREATE_Y_NUMBER, windowY);
		}

		long windowHandle = SDLVideo.SDL_CreateWindowWithProperties(props);
		if (windowHandle == 0) {
			Lwjgl3ApplicationConfiguration.errorStream.println(SDLError.SDL_GetError());
			throw new GdxRuntimeException("Couldn't create window");
		}
		SDLProperties.SDL_DestroyProperties(props);
		if (config.fullscreenMode != null) {
			SDLVideo.SDL_SetWindowFullscreen(windowHandle, true);
			SDLVideo.SDL_SetWindowFullscreenMode(windowHandle, config.fullscreenMode.displayMode);
		}
		if (sharedContextWindow != null) { // share context
			SDLVideo.SDL_GL_MakeCurrent(sharedContextWindow.getWindowHandle(), sharedContextWindow.glContext);
		}
		long glContext = SDLVideo.SDL_GL_CreateContext(windowHandle);
		if (glContext == 0) {
			Lwjgl3ApplicationConfiguration.errorStream.println(SDLError.SDL_GetError());
			throw new GdxRuntimeException("Couldn't create GL Context");
		}

		Lwjgl3Window.setSizeLimits(windowHandle, config.windowMinWidth, config.windowMinHeight, config.windowMaxWidth,
			config.windowMaxHeight);
		if (config.windowIconPaths != null) {
			Lwjgl3Window.setIcon(windowHandle, config.windowIconPaths, config.windowIconFileType);
		}
		SDLVideo.SDL_GL_MakeCurrent(windowHandle, glContext);
		SDLVideo.SDL_GL_SetSwapInterval(config.vSyncEnabled ? 1 : 0);
		if (config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20) {
			try {
				Class gles = Class.forName("org.lwjgl.opengles.GLES");
				gles.getMethod("createCapabilities").invoke(gles);
			} catch (Throwable e) {
				throw new GdxRuntimeException("Couldn't initialize GLES", e);
			}
		} else {
			GL.createCapabilities();
		}

		initiateGL(config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20);
		if (!glVersion.isVersionEqualToOrHigher(2, 0))
			throw new GdxRuntimeException("OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: "
				+ glVersion.getVersionString() + "\n" + glVersion.getDebugVersionString());

		if (config.glEmulation != Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20 && !supportsFBO()) {
			throw new GdxRuntimeException("OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: "
				+ glVersion.getVersionString() + ", FBO extension: false\n" + glVersion.getDebugVersionString());
		}

		if (config.debug) {
			if (config.glEmulation == GLEmulation.ANGLE_GLES20) {
				throw new IllegalStateException(
					"ANGLE currently can't be used with with Lwjgl3ApplicationConfiguration#enableGLDebugOutput");
			}
			glDebugCallback = GLUtil.setupDebugMessageCallback(config.debugStream);
			setGLDebugMessageControl(GLDebugMessageSeverity.NOTIFICATION, false);
		}

		return new long[] {windowHandle, glContext};
	}

	private static void initiateGL (boolean useGLES20) {
		if (!useGLES20) {
			String versionString = GL11.glGetString(GL11.GL_VERSION);
			String vendorString = GL11.glGetString(GL11.GL_VENDOR);
			String rendererString = GL11.glGetString(GL11.GL_RENDERER);
			glVersion = new GLVersion(Application.ApplicationType.Desktop, versionString, vendorString, rendererString);
		} else {
			try {
				Class gles = Class.forName("org.lwjgl.opengles.GLES20");
				Method getString = gles.getMethod("glGetString", int.class);
				String versionString = (String)getString.invoke(gles, GL11.GL_VERSION);
				String vendorString = (String)getString.invoke(gles, GL11.GL_VENDOR);
				String rendererString = (String)getString.invoke(gles, GL11.GL_RENDERER);
				glVersion = new GLVersion(Application.ApplicationType.Desktop, versionString, vendorString, rendererString);
			} catch (Throwable e) {
				throw new GdxRuntimeException("Couldn't get GLES version string.", e);
			}
		}
	}

	private static boolean supportsFBO () {
		// FBO is in core since OpenGL 3.0, see https://www.opengl.org/wiki/Framebuffer_Object
		return glVersion.isVersionEqualToOrHigher(3, 0) || SDLVideo.SDL_GL_ExtensionSupported("GL_EXT_framebuffer_object")
			|| SDLVideo.SDL_GL_ExtensionSupported("GL_ARB_framebuffer_object");
	}

	public enum GLDebugMessageSeverity {
		HIGH(GL43.GL_DEBUG_SEVERITY_HIGH, KHRDebug.GL_DEBUG_SEVERITY_HIGH, ARBDebugOutput.GL_DEBUG_SEVERITY_HIGH_ARB,
			AMDDebugOutput.GL_DEBUG_SEVERITY_HIGH_AMD), MEDIUM(GL43.GL_DEBUG_SEVERITY_MEDIUM, KHRDebug.GL_DEBUG_SEVERITY_MEDIUM,
				ARBDebugOutput.GL_DEBUG_SEVERITY_MEDIUM_ARB, AMDDebugOutput.GL_DEBUG_SEVERITY_MEDIUM_AMD), LOW(
					GL43.GL_DEBUG_SEVERITY_LOW, KHRDebug.GL_DEBUG_SEVERITY_LOW, ARBDebugOutput.GL_DEBUG_SEVERITY_LOW_ARB,
					AMDDebugOutput.GL_DEBUG_SEVERITY_LOW_AMD), NOTIFICATION(GL43.GL_DEBUG_SEVERITY_NOTIFICATION,
						KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION, -1, -1);

		final int gl43, khr, arb, amd;

		GLDebugMessageSeverity (int gl43, int khr, int arb, int amd) {
			this.gl43 = gl43;
			this.khr = khr;
			this.arb = arb;
			this.amd = amd;
		}
	}

	/** Enables or disables GL debug messages for the specified severity level. Returns false if the severity level could not be
	 * set (e.g. the NOTIFICATION level is not supported by the ARB and AMD extensions).
	 *
	 * See {@link Lwjgl3ApplicationConfiguration#enableGLDebugOutput(boolean, PrintStream)} */
	public static boolean setGLDebugMessageControl (GLDebugMessageSeverity severity, boolean enabled) {
		GLCapabilities caps = GL.getCapabilities();
		final int GL_DONT_CARE = 0x1100; // not defined anywhere yet

		if (caps.OpenGL43) {
			GL43.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, severity.gl43, (IntBuffer)null, enabled);
			return true;
		}

		if (caps.GL_KHR_debug) {
			KHRDebug.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, severity.khr, (IntBuffer)null, enabled);
			return true;
		}

		if (caps.GL_ARB_debug_output && severity.arb != -1) {
			ARBDebugOutput.glDebugMessageControlARB(GL_DONT_CARE, GL_DONT_CARE, severity.arb, (IntBuffer)null, enabled);
			return true;
		}

		if (caps.GL_AMD_debug_output && severity.amd != -1) {
			AMDDebugOutput.glDebugMessageEnableAMD(GL_DONT_CARE, severity.amd, (IntBuffer)null, enabled);
			return true;
		}

		return false;
	}

}
