
package com.badlogic.gdx.backends.jglfw;

import static com.badlogic.gdx.utils.SharedLibraryLoader.*;
import static com.badlogic.jglfw.Glfw.*;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.jglfw.GlfwCallbackAdapter;
import com.badlogic.jglfw.GlfwCallbacks;

import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Map;

/** An OpenGL surface fullscreen or in a lightweight window using GLFW.
 * @author mzechner
 * @author Nathan Sweet */
public class JglfwApplication implements Application {
	JglfwGraphics graphics;
	JglfwFiles files;
	JglfwInput input;
	JglfwNet net;
	final ApplicationListener listener;
	private final Array<Runnable> runnables = new Array();
	private final Array<Runnable> executedRunnables = new Array();
	private final Array<LifecycleListener> lifecycleListeners = new Array();
	private final Map<String, Preferences> preferences = new HashMap();
	private final JglfwClipboard clipboard = new JglfwClipboard();
	private final GlfwCallbacks callbacks = new GlfwCallbacks();
	private boolean forceExit;
	private boolean runOnEDT;
	volatile boolean running = true;
	private int logLevel = LOG_INFO;

	public JglfwApplication (ApplicationListener listener) {
		this(listener, listener.getClass().getSimpleName(), 640, 480, false);
	}

	public JglfwApplication (ApplicationListener listener, String title, int width, int height, boolean useGL2) {
		this(listener, createConfig(title, width, height, useGL2));
	}

	static private JglfwApplicationConfiguration createConfig (String title, int width, int height, boolean useGL2) {
		JglfwApplicationConfiguration config = new JglfwApplicationConfiguration();
		config.title = title;
		config.width = width;
		config.height = height;
		config.useGL20 = useGL2;
		return config;
	}

	public JglfwApplication (final ApplicationListener listener, final JglfwApplicationConfiguration config) {
		this.listener = listener;

		Runnable runnable = new Runnable() {
			public void run () {
				try {
					initialize(config);
				} catch (Throwable ex) {
					exception(ex);
				}
			}
		};
		if (config.runOnEDT)
			EventQueue.invokeLater(runnable);
		else
			new Thread(runnable, "MainLoop").start();
	}

	/** Called when an uncaught exception happens in the game loop. Default implementation prints the exception and calls
	 * System.exit(0). */
	protected void exception (Throwable ex) {
		ex.printStackTrace();
		System.exit(0);
	}

	void initialize (JglfwApplicationConfiguration config) {
		forceExit = config.forceExit;
		runOnEDT = config.runOnEDT;

		final Thread glThread = Thread.currentThread();

		GdxNativesLoader.load();

		boolean inputCallbacksOnAppKitThread = isMac;
		if (inputCallbacksOnAppKitThread) java.awt.Toolkit.getDefaultToolkit(); // Ensure AWT is initialized before GLFW.

		if (!glfwInit()) throw new GdxRuntimeException("Unable to initialize GLFW.");

		Gdx.app = this;
		Gdx.graphics = graphics = new JglfwGraphics(config);
		Gdx.files = files = new JglfwFiles();
		Gdx.input = input = new JglfwInput(this, inputCallbacksOnAppKitThread);
		Gdx.net = net = new JglfwNet();

		callbacks.add(new GlfwCallbackAdapter() {
			public void windowSize (long window, final int width, final int height) {
				Runnable runnable = new Runnable() {
					public void run () {
						graphics.sizeChanged(width, height);
					}
				};
				if (Thread.currentThread() != glThread)
					postRunnable(runnable);
				else
					runnable.run();
			}

			public void windowPos (long window, final int x, final int y) {
				Runnable runnable = new Runnable() {
					public void run () {
						graphics.positionChanged(x, y);
					}
				};
				if (Thread.currentThread() != glThread)
					postRunnable(runnable);
				else
					runnable.run();
			}

			public void windowRefresh (long window) {
				if (Thread.currentThread() == glThread) render();
			}

			public void error (int error, String description) {
				throw new GdxRuntimeException("GLFW error " + error + ": " + description);
			}
		});
		glfwSetCallback(callbacks);

		start();
	}

	/** Starts the game loop after the application internals have been initialized. */
	protected void start () {
		listener.create();
		listener.resize(graphics.getWidth(), graphics.getHeight());

		if (runOnEDT) {
			new Runnable() {
				public void run () {
					frame();
					if (running)
						EventQueue.invokeLater(this);
					else
						end();
				}
			}.run();
		} else {
			while (running)
				frame();
			end();
		}
	}

	void frame () {
		if (glfwWindowShouldClose(graphics.window)) {
			exit();
			return;
		}

		synchronized (runnables) {
			executedRunnables.clear();
			executedRunnables.addAll(runnables);
			runnables.clear();
		}
		if (executedRunnables.size > 0) {
			for (int i = 0; i < executedRunnables.size; i++)
				executedRunnables.get(i).run();
			if (!running) return;
			graphics.requestRendering();
		}

		input.update();

		if (graphics.shouldRender())
			render();
		else {
			try {
				Thread.sleep(16); // Avoid wasting CPU when not rendering.
			} catch (InterruptedException ignored) {
			}
		}
	}

	void render () {
		graphics.frameStart();
		listener.render();
		glfwSwapBuffers(graphics.window);
	}

	void end () {
		synchronized (lifecycleListeners) {
			for (LifecycleListener listener : lifecycleListeners) {
				listener.pause();
				listener.dispose();
			}
		}
		listener.pause();
		listener.dispose();
		glfwTerminate();
		if (forceExit) System.exit(-1);
	}

	public ApplicationListener getApplicationListener () {
		return listener;
	}

	public JglfwGraphics getGraphics () {
		return graphics;
	}

	public Audio getAudio () {
		return null;
	}

	public JglfwInput getInput () {
		return input;
	}

	public JglfwFiles getFiles () {
		return files;
	}

	public JglfwNet getNet () {
		return net;
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
			Preferences prefs = new JglfwPreferences(name);
			preferences.put(name, prefs);
			return prefs;
		}
	}

	public Clipboard getClipboard () {
		return clipboard;
	}

	public void postRunnable (Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
			graphics.requestRendering();
		}
	}

	public void exit () {
		running = false;
	}

	public void setLogLevel (int logLevel) {
		this.logLevel = logLevel;
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

	public void log (String tag, String message, Exception exception) {
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

	public GlfwCallbacks getCallbacks () {
		return callbacks;
	}
}
