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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.backends.lwjgl.audio.LwjglAudio;
import org.lwjgl.opengl.AWTGLCanvas;
import org.lwjgl.opengl.Display;

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
import com.badlogic.gdx.backends.lwjgl.audio.OpenALLwjglAudio;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.SharedLibraryLoader;

import java.awt.Canvas;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.geom.AffineTransform;

/** An OpenGL surface on an AWT Canvas, allowing OpenGL to be embedded in a Swing application. This uses
 * {@link Display#setParent(Canvas)}, which is preferred over {@link AWTGLCanvas} but is limited to a single LwjglCanvas in an
 * application. All OpenGL calls are done on the EDT. Note that you may need to call {@link #stop()} or a Swing application may
 * deadlock on System.exit due to how LWJGL and/or Swing deal with shutdown hooks.
 * @author Nathan Sweet */
public class LwjglCanvas implements LwjglApplicationBase {
	static boolean isWindows = System.getProperty("os.name").contains("Windows");

	LwjglGraphics graphics;
	LwjglAudio audio;
	LwjglFiles files;
	LwjglInput input;
	LwjglNet net;
	ApplicationListener listener;
	Canvas canvas;
	final Array runnables = new Array();
	final Array executedRunnables = new Array();
	final Array<LifecycleListener> lifecycleListeners = new Array();
	boolean running = true;
	int logLevel = LOG_INFO;
	ApplicationLogger applicationLogger;
	Cursor cursor;
	float scaleX, scaleY;
	boolean postedRunnableStacktraces;
	final Map<String, Preferences> preferences = new HashMap();

	public LwjglCanvas (ApplicationListener listener) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		initialize(listener, config);
	}

	public LwjglCanvas (ApplicationListener listener, LwjglApplicationConfiguration config) {
		initialize(listener, config);
	}

	private void initialize (ApplicationListener listener, LwjglApplicationConfiguration config) {
		LwjglNativesLoader.load();
		setApplicationLogger(new LwjglApplicationLogger());
		canvas = new Canvas() {
			private final Dimension minSize = new Dimension(1, 1);

			public final void addNotify () {
				super.addNotify();

				AffineTransform transform = getGraphicsConfiguration().getDefaultTransform();
				scaleX = (float)transform.getScaleX();
				scaleY = (float)transform.getScaleY();

				if (SharedLibraryLoader.isMac) {
					EventQueue.invokeLater(new Runnable() {
						public void run () {
							create();
						}
					});
				} else
					create();
			}

			public final void removeNotify () {
				stop();
				super.removeNotify();
			}

			public Dimension getMinimumSize () {
				return minSize;
			}

			public int getWidth () {
				return Math.round(super.getWidth() * scaleX);
			}

			public int getHeight () {
				return Math.round(super.getHeight() * scaleY);
			}
		};
		canvas.setSize(1, 1);
		canvas.setIgnoreRepaint(true);

		graphics = new LwjglGraphics(canvas, config) {
			public void setTitle (String title) {
				super.setTitle(title);
				LwjglCanvas.this.setTitle(title);
			}

			public boolean setWindowedMode (int width, int height, boolean fullscreen) {
				if (!super.setWindowedMode(width, height)) return false;
				if (!fullscreen) LwjglCanvas.this.setDisplayMode(width, height);
				return true;
			}

			public boolean setFullscreenMode (DisplayMode displayMode) {
				if (!super.setFullscreenMode(displayMode)) return false;
				LwjglCanvas.this.setDisplayMode(displayMode.width, displayMode.height);
				return true;
			}
		};
		graphics.setVSync(config.vSyncEnabled);
		if (!LwjglApplicationConfiguration.disableAudio) audio = createAudio(config);
		files = new LwjglFiles();
		input = createInput(config);
		net = new LwjglNet(config);
		this.listener = listener;

		Gdx.app = this;
		Gdx.graphics = graphics;
		Gdx.audio = audio;
		Gdx.files = files;
		Gdx.input = input;
		Gdx.net = net;
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
		return audio;
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

	void create () {
		try {
			graphics.setupDisplay();

			listener.create();
			listener.resize(Math.max(1, graphics.getWidth()), Math.max(1, graphics.getHeight()));

			start();
		} catch (Exception ex) {
			try {
				Display.destroy();
				if (audio != null) audio.dispose();
			} catch (Throwable ignored) {
			}
			stopped();
			exception(ex);
			return;
		}

		EventQueue.invokeLater(new Runnable() {
			int lastWidth = Math.max(1, graphics.getWidth());
			int lastHeight = Math.max(1, graphics.getHeight());

			public void run () {
				if (!running || Display.isCloseRequested()) {
					running = false;
					stopped();
					return;
				}
				try {
					Display.processMessages();
					if (cursor != null || !isWindows) canvas.setCursor(cursor);

					boolean shouldRender = checkResize();

					if (executeRunnables()) shouldRender = true;

					// If one of the runnables set running to false, for example after an exit().
					if (!running) return;

					input.update();
					shouldRender |= graphics.shouldRender();
					input.processEvents();
					if (audio != null) audio.update();

					if (checkResize()) shouldRender = true;

					if (shouldRender) {
						graphics.updateTime();
						graphics.frameId++;
						listener.render();
						Display.update(false);
					}

					Display.sync(getFrameRate());
				} catch (Throwable ex) {
					exception(ex);
				}
				EventQueue.invokeLater(this);
			}

			private boolean checkResize () {
				Container parent = canvas.getParent();
				if (parent != null && (canvas.getWidth() != parent.getWidth() || canvas.getHeight() != parent.getHeight()))
					canvas.setSize(parent.getWidth(), parent.getHeight());

				int width = Math.max(1, graphics.getWidth());
				int height = Math.max(1, graphics.getHeight());
				if (lastWidth == width && lastHeight == height) return false;
				lastWidth = width;
				lastHeight = height;

				Display.setLocation(0, 0);
				Gdx.gl.glViewport(0, 0, width, height);
				resize(width, height);
				listener.resize(width, height);
				return true;
			}
		});
	}

	public boolean executeRunnables () {
		synchronized (runnables) {
			for (int i = runnables.size - 1; i >= 0; i--)
				executedRunnables.add(runnables.get(i));
			runnables.clear();
		}
		if (executedRunnables.size == 0) return false;
		do {
			Runnable runnable = (Runnable)executedRunnables.pop();
			Throwable caller = (Throwable)executedRunnables.pop();
			try {
				runnable.run();
			} catch (Throwable ex) {
				postedException(ex, caller);
			}
		} while (executedRunnables.size > 0);
		return true;
	}

	protected void postedException (Throwable ex, @Null Throwable caller) {
		if (caller == null) throw new RuntimeException(ex);
		StringWriter buffer = new StringWriter(1024);
		caller.printStackTrace(new PrintWriter(buffer));
		throw new RuntimeException("Posted: " + buffer, ex);
	}

	protected void exception (Throwable ex) {
		ex.printStackTrace();
		stop();
	}

	protected int getFrameRate () {
		int frameRate = Display.isActive() ? graphics.config.foregroundFPS : graphics.config.backgroundFPS;
		if (frameRate == -1) frameRate = 10;
		if (frameRate == 0) frameRate = graphics.config.backgroundFPS;
		if (frameRate == 0) frameRate = 30;
		return frameRate;
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

	/** Called after dispose is complete. */
	protected void disposed () {
	}

	public void stop () {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				if (!running) return;
				running = false;
				Array<LifecycleListener> listeners = lifecycleListeners;
				synchronized (listeners) {
					for (LifecycleListener listener : listeners) {
						listener.pause();
						listener.dispose();
					}
				}
				listener.pause();
				listener.dispose();
				try {
					Display.destroy();
					if (audio != null) audio.dispose();
				} catch (Throwable ignored) {
				}
				disposed();
			}
		});
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
			runnables.add(postedRunnableStacktraces ? new Throwable() : null);
			graphics.requestRendering();
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
				LwjglCanvas.this.listener.pause();
				LwjglCanvas.this.listener.dispose();
				if (audio != null) audio.dispose();
				System.exit(-1);
			}
		});
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

	/** When true, {@link #postRunnable(Runnable)} keeps the stacktrace (which is an allocation) so it can be included if the
	 * runnable later throws an exception. Default is false. */
	public void setPostedRunnableStacktraces (boolean postedRunnableStacktraces) {
		this.postedRunnableStacktraces = postedRunnableStacktraces;
	}

	@Override
	public LwjglAudio createAudio (LwjglApplicationConfiguration config) {
		return new OpenALLwjglAudio();
	}

	@Override
	public LwjglInput createInput (LwjglApplicationConfiguration config) {
		return new DefaultLwjglInput();
	}
}
