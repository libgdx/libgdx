
package com.badlogic.gdx.backends.lwjgl3;

import static org.lwjgl.glfw.Callbacks.glfwSetCallback;
import static org.lwjgl.glfw.GLFW.*;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.glfw.GLFWWindowRefreshCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

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
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALAudio;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;

/** An OpenGL surface fullscreen or in a lightweight window using GLFW. <br>
 * 
 * Combine it with Lwjgl3WindowController to have multiple windows. <br>
 * 
 * This class has some modification from Jglfw backend to work with LWJGL3
 * 
 * 
 * @author mzechner
 * @author Nathan Sweet
 * @author Natan Guilherme */
public class Lwjgl3Application implements Application {
	String id;
	OpenALAudio audio;
	Lwjgl3Files files;

	GLContext context;
	Lwjgl3Graphics graphics;
	Lwjgl3Input input;
	Lwjgl3Net net;

	private final Array<Runnable> runnables = new Array();
	private final Array<Runnable> executedRunnables = new Array();
	private final Array<LifecycleListener> lifecycleListeners = new Array();
	private final Map<String, Preferences> preferences = new HashMap();
	private int logLevel = LOG_INFO;
	volatile boolean running = true;
	boolean isPaused;
	protected String preferencesdir;
	private int foregroundFPS, backgroundFPS, hiddenFPS;
	private boolean forceExit = false;
	final ApplicationListener listener;
	private boolean disposed;
	private boolean disposed2;

	boolean create = false;

	boolean autoloop;
	int audioDeviceSimultaneousSources;
	int audioDeviceBufferCount;
	int audioDeviceBufferSize;

	boolean init;
	
	int lastWidth;
	int lastHeight;

	public Lwjgl3Application (ApplicationListener listener, Lwjgl3ApplicationConfiguration config) {
		this(listener, config, true);
	}

	/** Autoloop true creates a thread (will block on mac). False is usefull to use it with
	 * {@link Lwjgl3WindowController} */
	public Lwjgl3Application (ApplicationListener listener, final Lwjgl3ApplicationConfiguration config, boolean autoloop) {
		Lwjgl3NativesLoader.load();

		if (glfwInit() != GL11.GL_TRUE) throw new IllegalStateException("Unable to initialize GLFW");

		this.listener = listener;
		this.autoloop = autoloop;
		initCallBacks();
		input = new Lwjgl3Input(Lwjgl3Application.this, true);
		audioDeviceSimultaneousSources = config.audioDeviceSimultaneousSources;
		audioDeviceBufferCount = config.audioDeviceBufferCount;
		audioDeviceBufferSize = config.audioDeviceBufferSize;
		backgroundFPS = config.backgroundFPS;
		hiddenFPS = config.hiddenFPS;
		foregroundFPS = config.foregroundFPS;

		graphics = new Lwjgl3Graphics(Lwjgl3Application.this, config);

		final Runnable appRunnable;
		Runnable mainRunnable;

		appRunnable = new Runnable() {

			public void run () {
				
				glfwMakeContextCurrent(graphics.window);
				context = GLContext.createFromCurrent();
				graphics.initGL();
				glfwShowWindow(graphics.window);
				graphics.show();
				

				while (running) {
					try {
							loop();
					} catch (Exception e) {
						e.printStackTrace();
						running = false;
					}
				}
				disposeListener();
			}
		};

		mainRunnable = new Runnable() {

			@Override
			public void run () {

				Thread thread = null;

				if (init == false) {
					init = true;

					graphics.initWindow();
					
					initStaticVariables();

					input.addCallBacks();
					addCallBacks();

					thread = new Thread(appRunnable);
					thread.start();
				}

				while (running) {
					glfwWaitEvents();
				}
				try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				dispose();
				end();
			}
		};

		if (autoloop) {
			if (Lwjgl3Graphics.isMac) {
				mainRunnable.run();
			} else
				new Thread(mainRunnable).start();
		}
	}

	// MUST HAVE CALLBACK POINTER OR ERROR OCCUR
	GLFWWindowCloseCallback closeCallBack;
	GLFWWindowSizeCallback sizeCallBack;
	GLFWWindowRefreshCallback refreshCallBack;
	GLFWWindowFocusCallback focusCallBack;
	GLFWWindowPosCallback posCallBack;
	GLFWWindowIconifyCallback iconifyCallBack;

	void initCallBacks () {
		closeCallBack = new GLFWWindowCloseCallback() {
			@Override
			public void invoke (long window) {
					exit();
			}
		};

		sizeCallBack = new GLFWWindowSizeCallback() {
			@Override
			public void invoke (long window, int width, int height) {
					graphics.sizeChanged(width, height);
			}
		};

		refreshCallBack = new GLFWWindowRefreshCallback() {

			@Override
			public void invoke (long window) {
//				Lwjgl3WindowController.toRefresh = false; // simple sync logic to refresh window when there is a refresh call
//				synchronized (Lwjgl3WindowController.SYNC) {
//					loop();
//					Lwjgl3WindowController.toRefresh = true;
//				}
			}
		};

		focusCallBack = new GLFWWindowFocusCallback() {
			@Override
			public void invoke (long window, int focused) {
				boolean focus = focused == GL11.GL_TRUE ? true : false;
				if (focus) Lwjgl3WindowController.currentWindow = window;
				graphics.foreground = focus;
			}
		};

		posCallBack = new GLFWWindowPosCallback() {
			@Override
			public void invoke (long window, int xpos, int ypos) {
				graphics.positionChanged(xpos, ypos);
			}
		};

		iconifyCallBack = new GLFWWindowIconifyCallback() {
			@Override
			public void invoke (long window, int iconified) {
				graphics.minimized = iconified == GL11.GL_TRUE ? true : false;
			}
		};
	}

	void initStaticVariables () {
		if (Gdx.files == null) {
			files = new Lwjgl3Files();
			Gdx.files = files;
		}
		if (Gdx.net == null) {
			net = new Lwjgl3Net();
			Gdx.net = net;
		}
		if (Gdx.audio == null) {
			audio = new OpenALAudio(audioDeviceSimultaneousSources, audioDeviceBufferCount, audioDeviceBufferSize);
			Gdx.audio = audio;
		}
	}

	void addCallBacks () {
		glfwSetCallback(graphics.window, closeCallBack);
		glfwSetCallback(graphics.window, sizeCallBack);
		glfwSetCallback(graphics.window, refreshCallBack);
		glfwSetCallback(graphics.window, focusCallBack);
		glfwSetCallback(graphics.window, posCallBack);
		glfwSetCallback(graphics.window, iconifyCallBack);
	}

	public void removeCallBacks () {
		closeCallBack.release();
		sizeCallBack.release();
		refreshCallBack.release();
		focusCallBack.release();
		posCallBack.release();
		iconifyCallBack.release();
	}

	void loop () {
		if (!running) return;

		setGlobals();

		if (create == false) {
			create = true;
			Lwjgl3Application.this.listener.create();
		}
		
		int width = Math.max(1, graphics.getWidth());
		int height = Math.max(1, graphics.getHeight());
		if (lastWidth != width || lastHeight != height) {
			lastWidth = width;
			lastHeight = height;
			Gdx.gl.glViewport(0, 0, lastWidth, lastHeight);
			listener.resize(width, height);
		}
		
		boolean shouldRender = false;

		if (executeRunnables()) shouldRender = true;

		if (!running) return;

		if (audio != null) audio.update();

		shouldRender |= graphics.shouldRender();

		long frameStartTime = System.nanoTime();
		int targetFPS = (graphics.isHidden() || graphics.isMinimized()) ? hiddenFPS : //
			(graphics.isForeground() ? foregroundFPS : backgroundFPS);

		if (targetFPS == -1) { // Rendering is paused.
			if (!isPaused) listener.pause();
			isPaused = true;
		} else {
			if (isPaused) listener.resume();
			isPaused = false;
			if (shouldRender)
				render(frameStartTime);
			else
				targetFPS = backgroundFPS;
		}

		input.update(); // becuase of the order of the glfwPollEvents() and input.update() logic. It needs to be after render
// becuase Gdx.##.justTouched() method wont work well.

		if (autoloop) {
			if (targetFPS != 0) {
				if (targetFPS == -1)
					sleep(100);
				else
					Sync.sync(targetFPS);
			}
		}
	}

	static void sleep (int millis) {
		try {
			if (millis > 0) Thread.sleep(millis);
		} catch (InterruptedException ignored) {
		}
	}

	void render (long time) {
		graphics.frameStart(time);
		listener.render();
		glfwSwapBuffers(graphics.window);
	}

	public boolean executeRunnables () {
		synchronized (runnables) {
			for (int i = runnables.size - 1; i >= 0; i--)
				executedRunnables.addAll(runnables.get(i));
			runnables.clear();
		}
		if (executedRunnables.size == 0) return false;
		for (int i = executedRunnables.size - 1; i >= 0; i--)
			executedRunnables.removeIndex(i).run();
		return true;
	}

	void setGlobals () {
		long window = glfwGetCurrentContext();

		if(window != graphics.window)
			glfwMakeContextCurrent(graphics.window); // for every call needs to make sure its context is set
//		if(GL.getCurrent() != context)
//			GL.setCurrent(context);

		if (audio != null && Gdx.audio == null) Gdx.audio = audio;
		if (files != null && Gdx.files == null) Gdx.files = files;
		if (net != null && Gdx.net == null) Gdx.net = net;

		// every window must set these 3 globals
		Gdx.app = this;
		Gdx.graphics = graphics;
		Gdx.input = input;
	}

	public ApplicationListener getApplicationListener () {
		return listener;
	}

	public Graphics getGraphics () {
		return graphics;
	}

	public Audio getAudio () {
		return audio;
	}

	public Input getInput () {
		return input;
	}

	public Files getFiles () {
		return files;
	}

	public Net getNet () {
		return net;
	}

	public void setLogLevel (int logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public int getLogLevel () {
		return logLevel;
	}

	public void debug (String tag, String message) {
		if (logLevel >= LOG_DEBUG) {
			System.out.println(tag + ": " + message);
		}
	}

	public void debug (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_DEBUG) {
			System.out.println(tag + ": " + message);
			exception.printStackTrace(System.out);
		}
	}

	public void log (String tag, String message) {
		if (logLevel >= LOG_INFO) {
			System.out.println(tag + ": " + message);
		}
	}

	public void log (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_INFO) {
			System.out.println(tag + ": " + message);
			exception.printStackTrace(System.out);
		}
	}

	public void error (String tag, String message) {
		if (logLevel >= LOG_ERROR) {
			System.err.println(tag + ": " + message);
		}
	}

	public void error (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_ERROR) {
			System.err.println(tag + ": " + message);
			exception.printStackTrace(System.err);
		}
	}

	public ApplicationType getType () {
		return ApplicationType.Desktop;
	}

	public int getVersion () {
		return 0;
	}

	public long getJavaHeap () {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	public long getNativeHeap () {
		return getJavaHeap();
	}

	public Preferences getPreferences (String name) {
		if (preferences.containsKey(name))
			return preferences.get(name);
		else {
			Preferences prefs = new Lwjgl3Preferences(name, this.preferencesdir);
			preferences.put(name, prefs);
			return prefs;
		}
	}

	public Clipboard getClipboard () {
		// TODO Auto-generated method stub
		return null;
	}

	public void postRunnable (Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
			graphics.requestRendering();
		}
	}
	
	/**
	 * Call this on listener window
	 */
	public void disposeListener()
	{
		if(disposed2)
			return;
		disposed2 = true;
		
		synchronized (lifecycleListeners) {
			for (LifecycleListener listener : lifecycleListeners) {
				listener.pause();
				listener.dispose();
			}
		}
		listener.pause();
		listener.dispose();
	}
	
	
	/**
	 * Call this on GLFW main thread.
	 */
	public void dispose () {
		if (disposed) return;
		exit();

		if (audio != null) {
			audio.dispose();

			if (Gdx.audio == audio) Gdx.audio = null;

			audio = null;
		}

		if (input != null) {
			input.removeCallBacks();
			if (input == Gdx.input) Gdx.input = null;
			input = null;
		}

		if (net != null) {
			if (net == Gdx.net) Gdx.net = null;
			net = null;
		}

		// Release calback
		removeCallBacks();
		glfwDestroyWindow(graphics.window);

		if (graphics != null) {
			if (graphics == Gdx.graphics) Gdx.graphics = null;

			graphics = null;
		}
	}

	void end () // used for this application thread only
	{
		glfwTerminate();
		if (forceExit) System.exit(-1);
	}

	public void exit () {
		running = false;
	}

	public void addLifecycleListener (LifecycleListener listener) {
		synchronized (lifecycleListeners) {
			lifecycleListeners.add(listener);
		}
	}

	public void removeLifecycleListener (LifecycleListener listener) {
		synchronized (lifecycleListeners) {
			lifecycleListeners.removeValue(listener, true);
		}
	}

	public String getId () {
		return id;
	}

	/** Get GLFW window so you can use it for GLFW commands. */
	public long getWindow () {
		return graphics.window;
	}

}
