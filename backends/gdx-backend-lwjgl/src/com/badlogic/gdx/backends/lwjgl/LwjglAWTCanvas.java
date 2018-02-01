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

package com.badlogic.gdx.backends.lwjgl;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.PaintEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import com.badlogic.gdx.ApplicationLogger;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.AWTGLCanvas;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.PixelFormat;

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
import com.badlogic.gdx.backends.lwjgl.audio.OpenALAudio;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;

/** An OpenGL surface on an AWT Canvas, allowing OpenGL to be embedded in a Swing application. This uses {@link AWTGLCanvas},
 * which allows multiple LwjglAWTCanvas to be used in a single application. All OpenGL calls are done on the EDT. Note that you
 * may need to call {@link #stop()} or a Swing application may deadlock on System.exit due to how LWJGL and/or Swing deal with
 * shutdown hooks.
 * @author Nathan Sweet */
public class LwjglAWTCanvas implements Application {
	static int instanceCount;

	LwjglGraphics graphics;
	OpenALAudio audio;
	LwjglFiles files;
	LwjglAWTInput input;
	LwjglNet net;
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
	final String logTag = "LwjglAWTCanvas";
	Cursor cursor;

	public LwjglAWTCanvas (ApplicationListener listener) {
		this(listener, null, null);
	}

	public LwjglAWTCanvas (ApplicationListener listener, LwjglAWTCanvas sharedContextCanvas) {
		this(listener, null, sharedContextCanvas);
	}

	public LwjglAWTCanvas (ApplicationListener listener, LwjglApplicationConfiguration config) {
		this(listener, config, null);
	}

	public LwjglAWTCanvas (ApplicationListener listener, LwjglApplicationConfiguration config,
		LwjglAWTCanvas sharedContextCanvas) {
		this.listener = listener;
		if (config == null) config = new LwjglApplicationConfiguration();

		LwjglNativesLoader.load();
		setApplicationLogger(new LwjglApplicationLogger());
		instanceCount++;

		AWTGLCanvas sharedDrawable = sharedContextCanvas != null ? sharedContextCanvas.canvas : null;
		try {
			canvas = new AWTGLCanvas(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice(), new PixelFormat(),
				sharedDrawable) {
				private final Dimension minSize = new Dimension(0, 0);
				private final NonSystemPaint nonSystemPaint = new NonSystemPaint(this);

				@Override
				public Dimension getMinimumSize () {
					return minSize;
				}

				@Override
				public void initGL () {
					create();
				}

				@Override
				public void paintGL () {
					try {
						boolean systemPaint = !(EventQueue.getCurrentEvent() instanceof NonSystemPaint);
						render(systemPaint);
						Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(nonSystemPaint);
					} catch (Throwable ex) {
						exception(ex);
					}
				}
			};
		} catch (Throwable ex) {
			exception(ex);
			return;
		}

		canvas.setBackground(new Color(config.initialBackgroundColor.r, config.initialBackgroundColor.g,
			config.initialBackgroundColor.b, config.initialBackgroundColor.a));

		graphics = new LwjglGraphics(canvas, config) {
			@Override
			public void setTitle (String title) {
				super.setTitle(title);
				LwjglAWTCanvas.this.setTitle(title);
			}

			@Override
			public boolean setWindowedMode (int width, int height) {
				if (!super.setWindowedMode(width, height)) return false;
				LwjglAWTCanvas.this.setDisplayMode(width, height);
				return true;
			}

			@Override
			public boolean setFullscreenMode (DisplayMode displayMode) {
				if (!super.setFullscreenMode(displayMode)) return false;
				LwjglAWTCanvas.this.setDisplayMode(displayMode.width, displayMode.height);
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

		if (!LwjglApplicationConfiguration.disableAudio && Gdx.audio == null) audio = new OpenALAudio();
		if (Gdx.files == null) files = new LwjglFiles();
		if (Gdx.net == null) net = new LwjglNet();
		input = new LwjglAWTInput(this);
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

	public Canvas getCanvas () {
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
		Gdx.app = this;
		if (audio != null) Gdx.audio = audio;
		if (files != null) Gdx.files = files;
		if (net != null) Gdx.net = net;
		Gdx.graphics = graphics;
		Gdx.input = input;
	}

	void create () {
		try {
			setGlobals();
			graphics.initiateGL();
			canvas.setVSyncEnabled(graphics.config.vSyncEnabled);
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

	void render (boolean shouldRender) throws LWJGLException {
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
			graphics.updateTime();
			graphics.frameId++;
			listener.render();
			canvas.swapBuffers();
		}

		Display.sync(getFrameRate() * instanceCount);
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
		int frameRate = isActive() ? graphics.config.foregroundFPS : graphics.config.backgroundFPS;
		if (frameRate == -1) frameRate = 10;
		if (frameRate == 0) frameRate = graphics.config.backgroundFPS;
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
		running = false;
		setGlobals();
		Array<LifecycleListener> listeners = lifecycleListeners;

		// To allow destroying of OpenGL textures during disposal.
		if (canvas.isDisplayable()) {
			makeCurrent();
		} else {
			error(logTag, "OpenGL context destroyed before application listener has had a chance to dispose of textures.");
		}

		synchronized (listeners) {
			for (LifecycleListener listener : listeners) {
				listener.pause();
				listener.dispose();
			}
		}
		listener.pause();
		listener.dispose();

		Gdx.app = null;

		Gdx.graphics = null;

		if (audio != null) {
			audio.dispose();
			Gdx.audio = null;
		}

		if (files != null) Gdx.files = null;

		if (net != null) Gdx.net = null;

		instanceCount--;

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
			Preferences prefs = new LwjglPreferences(name, ".prefs/");
			preferences.put(name, prefs);
			return prefs;
		}
	}

	@Override
	public Clipboard getClipboard () {
		return new LwjglClipboard();
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

	/** Make the canvas' context current. It is highly recommended that the context is only made current inside the AWT thread (for
	 * example in an overridden paintGL()). */
	public void makeCurrent () {
		try {
			canvas.makeCurrent();
			setGlobals();
		} catch (Throwable ex) {
			exception(ex);
		}
	}

	/** Test whether the canvas' context is current. */
	public boolean isCurrent () {
		try {
			return canvas.isCurrent();
		} catch (Throwable ex) {
			exception(ex);
			return false;
		}
	}

	/** @param cursor May be null. */
	public void setCursor (Cursor cursor) {
		this.cursor = cursor;
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
