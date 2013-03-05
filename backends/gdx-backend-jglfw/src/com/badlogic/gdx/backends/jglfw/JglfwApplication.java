
package com.badlogic.gdx.backends.jglfw;

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

import java.util.HashMap;
import java.util.Map;

/** An OpenGL surface fullscreen or in a lightweight window.
 * @author mzechner
 * @author Nathan Sweet */
public class JglfwApplication implements Application {
	final JglfwApplicationConfiguration config;
	final JglfwGraphics graphics;
	final JglfwFiles files;
	final JglfwInput input;
	final JglfwNet net;
	final ApplicationListener listener;
	final Array<Runnable> runnables = new Array();
	final Array<Runnable> executedRunnables = new Array();
	final Array<LifecycleListener> lifecycleListeners = new Array();
	final Map<String, Preferences> preferences = new HashMap();
	final JglfwClipboard clipboard = new JglfwClipboard();
	boolean running = true;
	int logLevel = LOG_INFO;

	public JglfwApplication (ApplicationListener listener, String title, int width, int height, boolean useGL2) {
		this(listener, createConfig(title, width, height, useGL2));
	}

	static private JglfwApplicationConfiguration createConfig (String title, int width, int height, boolean useGL2) {
		JglfwApplicationConfiguration config = new JglfwApplicationConfiguration();
		config.title = title;
		config.width = width;
		config.height = height;
		config.useGL20 = useGL2;
		config.vSync = true;
		return config;
	}

	public JglfwApplication (ApplicationListener listener) {
		this(listener, new JglfwApplicationConfiguration());
	}

	public JglfwApplication (ApplicationListener listener, JglfwApplicationConfiguration config) {
		this.listener = listener;
		this.config = config;

		GdxNativesLoader.load();
		if (!glfwInit()) throw new GdxRuntimeException("Unable to initialize GLFW.");

		Gdx.app = this;
		Gdx.graphics = graphics = new JglfwGraphics(config);
		Gdx.files = files = new JglfwFiles();
		Gdx.input = input = new JglfwInput(graphics);
		Gdx.net = net = new JglfwNet();

		mainLoop();
	}

	private void mainLoop () {
		listener.create();
		listener.resize(graphics.getWidth(), graphics.getHeight());
		graphics.resize = false;

		int lastWidth = graphics.getWidth();
		int lastHeight = graphics.getHeight();

		graphics.lastTime = System.nanoTime();
		while (running) {
			if (glfwWindowShouldClose(graphics.window)) exit();

			graphics.config.x = glfwGetWindowX(graphics.window);
			graphics.config.y = glfwGetWindowY(graphics.window);
			int width = glfwGetWindowWidth(graphics.window);
			int height = glfwGetWindowHeight(graphics.window);
			if (graphics.resize || width != graphics.config.width || height != graphics.config.height) {
				graphics.resize = false;
				Gdx.gl.glViewport(0, 0, width, height);
				graphics.config.width = width;
				graphics.config.height = height;
				if (listener != null) listener.resize(width, height);
				graphics.requestRendering();
			}

			synchronized (runnables) {
				executedRunnables.clear();
				executedRunnables.addAll(runnables);
				runnables.clear();
			}

			boolean shouldRender = false;
			for (int i = 0; i < executedRunnables.size; i++) {
				shouldRender = true;
				executedRunnables.get(i).run(); // calls out to random app code that could do anything ...
			}

			// If one of the runnables set running to false, for example after an exit().
			if (!running) break;

			glfwPollEvents();
			shouldRender |= graphics.shouldRender();

			// If input processing set running to false.
			if (!running) break;

			if (shouldRender) {
				graphics.updateTime();
				listener.render();
				glfwSwapBuffers(graphics.window);
			} else {
				// Avoid burning CPU when not rendering.
				try {
					Thread.sleep(16);
				} catch (InterruptedException ignored) {
				}
			}
		}

		Array<LifecycleListener> listeners = lifecycleListeners;
		synchronized (listeners) {
			for (LifecycleListener listener : listeners) {
				listener.pause();
				listener.dispose();
			}
		}
		listener.pause();
		listener.dispose();
		glfwDestroyWindow(graphics.window);
		if (graphics.config.forceExit) System.exit(-1);
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
		return preferences.get(name);
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
		postRunnable(new Runnable() {

			public void run () {
				running = false;
			}
		});
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
}
