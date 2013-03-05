
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
import com.badlogic.jglfw.GlfwCallbackAdapter;
import com.badlogic.jglfw.GlfwCallbacks;

import java.util.HashMap;
import java.util.Map;

/** An OpenGL surface fullscreen or in a lightweight window.
 * @author mzechner
 * @author Nathan Sweet */
public class JglfwApplication implements Application {
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
	final GlfwCallbacks callbacks = new GlfwCallbacks();
	final boolean forceExit;
	boolean running = true;
	int logLevel = LOG_INFO;

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
		config.vSync = true;
		return config;
	}

	public JglfwApplication (final ApplicationListener listener, JglfwApplicationConfiguration config) {
		this.listener = listener;

		forceExit = config.forceExit;

		GdxNativesLoader.load();
		if (!glfwInit()) throw new GdxRuntimeException("Unable to initialize GLFW.");

		Gdx.app = this;
		Gdx.graphics = graphics = new JglfwGraphics(config);
		Gdx.files = files = new JglfwFiles();
		Gdx.input = input = new JglfwInput(this);
		Gdx.net = net = new JglfwNet();

		glfwSetCallback(callbacks);
		callbacks.add(new GlfwCallbackAdapter() {
			public void windowSize (long window, int width, int height) {
				Gdx.gl.glViewport(0, 0, width, height);
				if (listener != null) listener.resize(width, height);
				graphics.requestRendering();
			}

			public void windowRefresh (long window) {
				renderFrame();
			}

			public void windowPos (long window, int x, int y) {
			}

			public void windowIconify (long window, boolean iconified) {
			}

			public void windowFocus (long window, boolean focused) {
			}

			public boolean windowClose (long window) {
				return true;
			}

			public void monitor (long monitor, boolean connected) {
			}

			public void error (int error, String description) {
				throw new GdxRuntimeException("GLFW error " + error + ": " + description);
			}
		});
	}

	public void start () {
		listener.create();
		listener.resize(graphics.getWidth(), graphics.getHeight());

		graphics.lastTime = System.nanoTime();
		while (running) {
			if (glfwWindowShouldClose(graphics.window)) exit();

			synchronized (runnables) {
				executedRunnables.clear();
				executedRunnables.addAll(runnables);
				runnables.clear();
			}
			if (executedRunnables.size > 0) {
				for (int i = 0; i < executedRunnables.size; i++)
					executedRunnables.get(i).run();
				if (!running) break;
				graphics.requestRendering();
			}

			input.update();
			if (!running) break;

			if (graphics.shouldRender())
				renderFrame();
			else {
				try {
					Thread.sleep(16); // Avoid wasting CPU when not rendering.
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
		if (forceExit) System.exit(-1);
	}

	void renderFrame () {
		graphics.updateTime();
		listener.render();
		glfwSwapBuffers(graphics.window);
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

	public GlfwCallbacks getCallbacks () {
		return callbacks;
	}
}
