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

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl.audio.OpenALAudio;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

/** An OpenGL surface fullscreen or in a lightweight window. */
public class LwjglApplication implements Application {
	protected final LwjglGraphics graphics;
	protected OpenALAudio audio;
	protected final LwjglFiles files;
	protected final LwjglInput input;
	protected final LwjglNet net;
	protected final ApplicationListener listener;
	protected Thread mainLoopThread;
	protected boolean running = true;
	protected final Array<Runnable> runnables = new Array<Runnable>();
	protected final Array<Runnable> executedRunnables = new Array<Runnable>();
	protected final Array<LifecycleListener> lifecycleListeners = new Array<LifecycleListener>();
	protected int logLevel = LOG_INFO;
	protected String preferencesdir;

	public LwjglApplication (ApplicationListener listener, String title, int width, int height) {
		this(listener, createConfig(title, width, height));
	}

	public LwjglApplication (ApplicationListener listener) {
		this(listener, null, 640, 480);
	}

	public LwjglApplication (ApplicationListener listener, LwjglApplicationConfiguration config) {
		this(listener, config, new LwjglGraphics(config));
	}

	public LwjglApplication (ApplicationListener listener, Canvas canvas) {
		this(listener, new LwjglApplicationConfiguration(), new LwjglGraphics(canvas));
	}

	public LwjglApplication (ApplicationListener listener, LwjglApplicationConfiguration config, Canvas canvas) {
		this(listener, config, new LwjglGraphics(canvas, config));
	}

	public LwjglApplication (ApplicationListener listener, LwjglApplicationConfiguration config, LwjglGraphics graphics) {
		LwjglNativesLoader.load();		

		if (config.title == null) config.title = listener.getClass().getSimpleName();

		this.graphics = graphics;
		if (!LwjglApplicationConfiguration.disableAudio)
			audio = new OpenALAudio(config.audioDeviceSimultaneousSources, config.audioDeviceBufferCount,
				config.audioDeviceBufferSize);
		files = new LwjglFiles();
		input = new LwjglInput();
		net = new LwjglNet();
		this.listener = listener;
		this.preferencesdir = config.preferencesDirectory;

		Gdx.app = this;
		Gdx.graphics = graphics;
		Gdx.audio = audio;
		Gdx.files = files;
		Gdx.input = input;
		Gdx.net = net;
		initialize();
	}

	private static LwjglApplicationConfiguration createConfig (String title, int width, int height) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = title;
		config.width = width;
		config.height = height;
		config.vSyncEnabled = true;
		return config;
	}

	private void initialize () {
		mainLoopThread = new Thread("LWJGL Application") {
			@Override
			public void run () {
				graphics.setVSync(graphics.config.vSyncEnabled);
				try {
					LwjglApplication.this.mainLoop();
				} catch (Throwable t) {
					if (audio != null) audio.dispose();
					if (t instanceof RuntimeException)
						throw (RuntimeException)t;
					else
						throw new GdxRuntimeException(t);
				}
			}
		};
		mainLoopThread.start();
	}

	void mainLoop () {
		Array<LifecycleListener> lifecycleListeners = this.lifecycleListeners;

		try {
			graphics.setupDisplay();
		} catch (LWJGLException e) {
			throw new GdxRuntimeException(e);
		}

		listener.create();
		graphics.resize = true;

		int lastWidth = graphics.getWidth();
		int lastHeight = graphics.getHeight();

		graphics.lastTime = System.nanoTime();
		boolean wasActive = true;
		while (running) {
			Display.processMessages();
			if (Display.isCloseRequested()) exit();

			boolean isActive = Display.isActive();
			if (wasActive && !isActive) { // if it's just recently minimized from active state
				wasActive = false;
				synchronized (lifecycleListeners) {
					for (LifecycleListener listener : lifecycleListeners)
						listener.pause();
				}
				listener.pause();
			}
			if (!wasActive && isActive) { // if it's just recently focused from minimized state
				wasActive = true;
				listener.resume();
				synchronized (lifecycleListeners) {
					for (LifecycleListener listener : lifecycleListeners)
						listener.resume();
				}
			}

			boolean shouldRender = false;

			if (graphics.canvas != null) {
				int width = graphics.canvas.getWidth();
				int height = graphics.canvas.getHeight();
				if (lastWidth != width || lastHeight != height) {
					lastWidth = width;
					lastHeight = height;
					Gdx.gl.glViewport(0, 0, lastWidth, lastHeight);
					listener.resize(lastWidth, lastHeight);
					shouldRender = true;
				}
			} else {
				graphics.config.x = Display.getX();
				graphics.config.y = Display.getY();
				if (graphics.resize || Display.wasResized() || Display.getWidth() != graphics.config.width
					|| Display.getHeight() != graphics.config.height) {
					graphics.resize = false;
					Gdx.gl.glViewport(0, 0, Display.getWidth(), Display.getHeight());
					graphics.config.width = Display.getWidth();
					graphics.config.height = Display.getHeight();
					if (listener != null) listener.resize(Display.getWidth(), Display.getHeight());
					graphics.requestRendering();
				}
			}

			if (executeRunnables()) shouldRender = true;

			// If one of the runnables set running to false, for example after an exit().
			if (!running) break;

			input.update();
			shouldRender |= graphics.shouldRender();
			input.processEvents();
			if (audio != null) audio.update();

			if (!isActive && graphics.config.backgroundFPS == -1) shouldRender = false;
			int frameRate = isActive ? graphics.config.foregroundFPS : graphics.config.backgroundFPS;
			if (shouldRender) {
				graphics.updateTime();
				graphics.frameId++;
				listener.render();
				Display.update(false);
			} else {
				// Sleeps to avoid wasting CPU in an empty loop.
				if (frameRate == -1) frameRate = 10;
				if (frameRate == 0) frameRate = graphics.config.backgroundFPS;
				if (frameRate == 0) frameRate = 30;
			}
			if (frameRate > 0) Display.sync(frameRate);
		}

		synchronized (lifecycleListeners) {
			for (LifecycleListener listener : lifecycleListeners) {
				listener.pause();
				listener.dispose();
			}
		}
		listener.pause();
		listener.dispose();
		Display.destroy();
		if (audio != null) audio.dispose();
		if (graphics.config.forceExit) System.exit(-1);
	}

	public boolean executeRunnables () {
		synchronized (runnables) {
			executedRunnables.addAll(runnables);
			runnables.clear();
		}
		if (executedRunnables.size == 0) return false;
		for (int i = 0; i < executedRunnables.size; i++)
			executedRunnables.get(i).run();
		executedRunnables.clear();
		return true;
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return listener;
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
	public LwjglGraphics getGraphics () {
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

	public void stop () {
		running = false;
		try {
			mainLoopThread.join();
		} catch (Exception ex) {
		}
	}

	@Override
	public long getJavaHeap () {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	@Override
	public long getNativeHeap () {
		return getJavaHeap();
	}

	ObjectMap<String, Preferences> preferences = new ObjectMap<String, Preferences>();

	@Override
	public Preferences getPreferences (String name) {
		if (preferences.containsKey(name)) {
			return preferences.get(name);
		} else {
			Preferences prefs = new LwjglPreferences(name, this.preferencesdir);
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
			Gdx.graphics.requestRendering();
		}
	}

	@Override
	public void debug (String tag, String message) {
		if (logLevel >= LOG_DEBUG) {
			System.out.println(tag + ": " + message);
		}
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_DEBUG) {
			System.out.println(tag + ": " + message);
			exception.printStackTrace(System.out);
		}
	}

	@Override
	public void log (String tag, String message) {
		if (logLevel >= LOG_INFO) {
			System.out.println(tag + ": " + message);
		}
	}

	@Override
	public void log (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_INFO) {
			System.out.println(tag + ": " + message);
			exception.printStackTrace(System.out);
		}
	}

	@Override
	public void error (String tag, String message) {
		if (logLevel >= LOG_ERROR) {
			System.err.println(tag + ": " + message);
		}
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		if (logLevel >= LOG_ERROR) {
			System.err.println(tag + ": " + message);
			exception.printStackTrace(System.err);
		}
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
	public void exit () {
		postRunnable(new Runnable() {
			@Override
			public void run () {
				running = false;
			}
		});
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
}
