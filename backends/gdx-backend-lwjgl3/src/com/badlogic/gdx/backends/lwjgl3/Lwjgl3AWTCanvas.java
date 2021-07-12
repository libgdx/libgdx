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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl3.audio.Lwjgl3Audio;
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALLwjgl3Audio;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.PaintEvent;
import java.util.HashMap;
import java.util.Map;

/** An OpenGL surface on an AWT Canvas, allowing OpenGL to be embedded in a Swing application. This uses {@link AWTGLCanvas},
 * which allows multiple LwjglAWTCanvas to be used in a single application. All OpenGL calls are done on the EDT. Note that you
 * may need to call {@link #stop()} or a Swing application may deadlock on System.exit due to how LWJGL and/or Swing deal with
 * shutdown hooks.
 * @author Nathan Sweet */
public class Lwjgl3AWTCanvas implements Application {
	private static boolean glfwInitialized;

	Lwjgl3AWTGraphics graphics;
	Lwjgl3Audio audio;
	Lwjgl3Files files;
	Lwjgl3AWTInput input;
	Lwjgl3Net net;
	final ApplicationListener listener;
	AWTGLCanvas canvas;
	final Array<Runnable> runnables = new Array();
	final Array<Runnable> executedRunnables = new Array();
	final Array<LifecycleListener> lifecycleListeners = new Array<LifecycleListener>();
	boolean running = true;
	int lastWidth;
	int lastHeight;
	int logLevel = LOG_INFO;
	ApplicationLogger applicationLogger;
	Cursor cursor;
	private final Sync sync;
	Lwjgl3ApplicationConfiguration config;

	public Lwjgl3AWTCanvas (ApplicationListener listener) {
		this(listener, null, null);
	}

	public Lwjgl3AWTCanvas (ApplicationListener listener, Lwjgl3AWTCanvas sharedContextCanvas) {
		this(listener, null, sharedContextCanvas);
	}

	public Lwjgl3AWTCanvas (ApplicationListener listener, Lwjgl3ApplicationConfiguration config) {
		this(listener, config, null);
	}

	public Lwjgl3AWTCanvas (ApplicationListener listener, Lwjgl3ApplicationConfiguration config,
		Lwjgl3AWTCanvas sharedContextCanvas) {
		this.listener = listener;
		if (config == null) config = new Lwjgl3ApplicationConfiguration();

		this.config = config;

		Lwjgl3NativesLoader.load();
		setApplicationLogger(new Lwjgl3ApplicationLogger());

		this.sync = new Sync();

		GLData glData = new GLData();

		if (sharedContextCanvas != null) {
			glData.shareContext = sharedContextCanvas.getCanvas();
		}

		// TODO: fill glData from config

		try {
			canvas = new AWTGLCanvas(glData) {
				private final Dimension minSize = new Dimension(0, 0);
				private final NonSystemPaint nonSystemPaint = new NonSystemPaint(this);

				@Override
				public Dimension getMinimumSize () {
					return minSize;
				}

				@Override
				protected void beforeRender () {
					super.beforeRender();
					GL.createCapabilities();
				}

				@Override
				public void update (java.awt.Graphics g) {
					paint(g);
				}

				@Override
				public void paint (java.awt.Graphics g) {
					if (isDisplayable()) {
						render();
					}
				}

				@Override
				public void initGL () {
					create();
				}

				@Override
				public void paintGL () {
// Gdx.gl.glClearColor(1.0f,0,0,0.5f);
// Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
					Gdx.gl.glViewport(0, 0, getWidth(), getHeight());
// Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
// Gdx.gl.glScissor(0,0,getWidth(),getHeight());
					try {
						boolean systemPaint = !(EventQueue.getCurrentEvent() instanceof NonSystemPaint);
						Lwjgl3AWTCanvas.this.render(systemPaint);
						Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(nonSystemPaint);
					} catch (Throwable ex) {
						exception(ex);
					}
					swapBuffers();
				}
			};
		} catch (Throwable ex) {
			exception(ex);
			return;
		}

		canvas.setBackground(new Color(config.initialBackgroundColor.r, config.initialBackgroundColor.g,
			config.initialBackgroundColor.b, config.initialBackgroundColor.a));

		graphics = new Lwjgl3AWTGraphics(this) {
			@Override
			public void setTitle (String title) {
				super.setTitle(title);
				Lwjgl3AWTCanvas.this.setTitle(title);
			}

			@Override
			public boolean setWindowedMode (int width, int height) {
				if (!super.setWindowedMode(width, height)) return false;
				Lwjgl3AWTCanvas.this.setDisplayMode(width, height);
				return true;
			}

			@Override
			public boolean setFullscreenMode (Graphics.DisplayMode displayMode) {
				if (!super.setFullscreenMode(displayMode)) return false;
				Lwjgl3AWTCanvas.this.setDisplayMode(displayMode.width, displayMode.height);
				return true;
			}

			public boolean shouldRender () {
				synchronized (this) {
					boolean rq = requestRendering;
					requestRendering = false;
					return rq || isContinuous;
				}
			}
		};

		if (Gdx.audio instanceof Lwjgl3Audio) {
			// if we have an existing audio, reuse it
			audio = (Lwjgl3Audio)Gdx.audio;
		} else if (!config.disableAudio) {
			try {
				audio = createAudio(config);
			} catch (Throwable t) {
				log("Lwjgl3AWTCanvas", "Couldn't initialize audio, disabling audio", t);
				audio = new MockAudio();
			}
		} else {
			audio = new MockAudio();
		}

		if (Gdx.files == null) files = new Lwjgl3Files();
		if (Gdx.net == null) net = new Lwjgl3Net(config);
		input = new Lwjgl3AWTInput(this);
		setGlobals();
	}

	protected void setDisplayMode (int width, int height) {
	}

	protected void setTitle (String title) {
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return listener;
	}

	public AWTGLCanvas getCanvas () {
		return canvas;
	}

	@Override
	public Audio getAudio () {
		return Gdx.audio;
	}

	@Override
	public Files getFiles () {
		return files;
	}

	@Override
	public Graphics getGraphics () {
		return graphics;
	}

	@Override
	public Input getInput () {
		return input;
	}

	@Override
	public Net getNet () {
		return net;
	}

	@Override
	public ApplicationType getType () {
		return ApplicationType.Desktop;
	}

	@Override
	public int getVersion () {
		return 0;
	}

	void setGlobals () {
		Gdx.graphics = graphics;
		Gdx.gl30 = graphics.getGL30();
		Gdx.gl20 = Gdx.gl30 != null ? Gdx.gl30 : graphics.getGL20();
		Gdx.gl = Gdx.gl30 != null ? Gdx.gl30 : Gdx.gl20;

		Gdx.app = this;
		if (audio != null) Gdx.audio = audio;
		if (files != null) Gdx.files = files;
		if (net != null) Gdx.net = net;
		Gdx.input = input;
	}

	void create () {
		try {
			setGlobals();
			if (!glfwInitialized) {
				// We need to initialize GLFW in the rendering thread
				glfwInitialized = GLFW.glfwInit();
				if (!glfwInitialized) {
					throw new GdxRuntimeException("Unable to initialize GLFW");
				}
			}

			graphics.initiateGL();
// canvas.setVSyncEnabled(graphics.config.vSyncEnabled);
			listener.create();
			lastWidth = Math.max(1, graphics.getWidth());
			lastHeight = Math.max(1, graphics.getHeight());
			listener.resize(lastWidth, lastHeight);
			start();
		} catch (Throwable ex) {
			stopped();
			exception(ex);
		}
	}

	void render (boolean shouldRender) {
		if (!running) return;

		setGlobals();
		canvas.setCursor(cursor);

		int width = Math.max(1, graphics.getWidth());
		int height = Math.max(1, graphics.getHeight());
		if (lastWidth != width || lastHeight != height) {
			lastWidth = width;
			lastHeight = height;
			Gdx.gl.glViewport(0, 0, lastWidth, lastHeight);
			resize(width, height);
			listener.resize(width, height);
			shouldRender = true;
		}

		if (executeRunnables()) shouldRender = true;

		// If one of the runnables set running to false, for example after an exit().
		if (!running) return;

		shouldRender |= graphics.shouldRender();
		input.processEvents();
		if (audio != null) audio.update();

		if (shouldRender) {
			graphics.update();
			listener.render();
			canvas.swapBuffers();
		}

		sync.sync(getFrameRate());
	}

	public boolean executeRunnables () {
		synchronized (runnables) {
			for (int i = runnables.size - 1; i >= 0; i--)
				executedRunnables.addAll(runnables.get(i));
			runnables.clear();
		}
		if (executedRunnables.size == 0) return false;
		do
			executedRunnables.pop().run();
		while (executedRunnables.size > 0);
		return true;
	}

	protected int getFrameRate () {
		int frameRate = config.foregroundFPS;// isActive() ? config.foregroundFPS : config.backgroundFPS;
		if (frameRate == -1) frameRate = 10;
// if (frameRate == 0) frameRate = config.backgroundFPS;
		if (frameRate == 0) frameRate = 30;
		return frameRate;
	}

	/** Returns true when the frame containing the canvas is the foreground window. */
	public boolean isActive () {
		Component root = SwingUtilities.getRoot(canvas);
		return root instanceof Frame ? ((Frame)root).isActive() : true;
	}

	/** Called after {@link ApplicationListener} create and resize, but before the game loop iteration. */
	protected void start () {
	}

	/** Called when the canvas size changes. */
	protected void resize (int width, int height) {
	}

	/** Called when the game loop has stopped. */
	protected void stopped () {
	}

	public void stop () {
		if (!running) return;
		if (!SwingUtilities.isEventDispatchThread()) {
			// This method must be called on the EDT
			SwingUtilities.invokeLater(this::stop);
			return;
		}
		running = false;
		Array<LifecycleListener> listeners = lifecycleListeners;

		// To allow destroying of OpenGL textures during disposal.
		canvas.runInContext( () -> {
			synchronized (listeners) {
				for (LifecycleListener listener : listeners) {
					listener.pause();
					listener.dispose();
				}
			}
			listener.pause();
			listener.dispose();
		});

		Gdx.app = null;

		Gdx.graphics = null;

		if (audio != null) {
			audio.dispose();
			Gdx.audio = null;
		}

		if (files != null) Gdx.files = null;

		if (net != null) Gdx.net = null;

		stopped();
	}

	@Override
	public long getJavaHeap () {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	@Override
	public long getNativeHeap () {
		return getJavaHeap();
	}

	Map<String, Preferences> preferences = new HashMap<String, Preferences>();

	@Override
	public Preferences getPreferences (String name) {
		if (preferences.containsKey(name)) {
			return preferences.get(name);
		} else {
			Preferences prefs = new Lwjgl3Preferences(name, ".prefs/");
			preferences.put(name, prefs);
			return prefs;
		}
	}

	@Override
	public Clipboard getClipboard () {
		return new Lwjgl3Clipboard();
	}

	@Override
	public void postRunnable (Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
		}
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
	public void exit () {
		postRunnable(new Runnable() {
			@Override
			public void run () {
				stop();
				System.exit(-1);
			}
		});
	}

	/** @param cursor May be null. */
	public void setCursor (Cursor cursor) {
		this.cursor = cursor;
	}

	public Lwjgl3ApplicationConfiguration getConfig () {
		return config;
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

	public Lwjgl3Audio createAudio (Lwjgl3ApplicationConfiguration config) {
		return new OpenALLwjgl3Audio(config.audioDeviceSimultaneousSources, config.audioDeviceBufferCount,
			config.audioDeviceBufferSize);
	}

	protected void exception (Throwable ex) {
		ex.printStackTrace();
		stop();
	}

	static public class NonSystemPaint extends PaintEvent {
		public NonSystemPaint (AWTGLCanvas canvas) {
			super(canvas, UPDATE, new Rectangle(0, 0, 99999, 99999));
		}
	}
}
