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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.openal.OpenALAudio;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** An OpenGL surface fullscreen or in a lightweight window. */
public class LwjglApplication implements Application {
	LwjglGraphics graphics;
	OpenALAudio audio;
	LwjglFiles files;
	LwjglInput input;
	final ApplicationListener listener;
	Thread mainLoopThread;
	boolean running = true;
	List<Runnable> runnables = new ArrayList<Runnable>();
	int logLevel = LOG_INFO;

	public LwjglApplication (ApplicationListener listener, String title, int width, int height, boolean useGL2) {
		LwjglNativesLoader.load();

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = title;
		config.width = width;
		config.height = height;
		config.useGL20 = useGL2;
		config.vSyncEnabled = true;
		graphics = new LwjglGraphics(config);
		audio = new OpenALAudio();
		files = new LwjglFiles();
		input = new LwjglInput();
		this.listener = listener;

		Gdx.app = this;
		Gdx.graphics = graphics;
		Gdx.audio = audio;
		Gdx.files = files;
		Gdx.input = input;
		initialize();
	}

	public LwjglApplication (ApplicationListener listener, LwjglApplicationConfiguration config) {
		LwjglNativesLoader.load();

		graphics = new LwjglGraphics(config);
		audio = new OpenALAudio();
		files = new LwjglFiles();
		input = new LwjglInput();
		this.listener = listener;

		Gdx.app = this;
		Gdx.graphics = graphics;
		Gdx.audio = audio;
		Gdx.files = files;
		Gdx.input = input;
		initialize();

	}

	public LwjglApplication (ApplicationListener listener, boolean useGL2, Canvas canvas) {
		LwjglNativesLoader.load();

		graphics = new LwjglGraphics(canvas, useGL2);
		audio = new OpenALAudio();
		files = new LwjglFiles();
		input = new LwjglInput();
		this.listener = listener;

		Gdx.app = this;
		Gdx.graphics = graphics;
		Gdx.audio = audio;
		Gdx.files = files;
		Gdx.input = input;
		initialize();
	}
	
	public LwjglApplication (ApplicationListener listener, LwjglApplicationConfiguration config, Canvas canvas) {
		LwjglNativesLoader.load();

		graphics = new LwjglGraphics(canvas, config);
		audio = new OpenALAudio();
		files = new LwjglFiles();
		input = new LwjglInput();
		this.listener = listener;

		Gdx.app = this;
		Gdx.graphics = graphics;
		Gdx.audio = audio;
		Gdx.files = files;
		Gdx.input = input;
		initialize();
	}

	private void initialize () {
		mainLoopThread = new Thread("LWJGL Application") {
			public void run () {
				graphics.setVSync(graphics.config.vSyncEnabled);
				LwjglApplication.this.mainLoop();
			}
		};
		mainLoopThread.start();
	}

	void mainLoop () {
		try {
			graphics.setupDisplay();
		} catch (LWJGLException e) {
			throw new GdxRuntimeException(e);
		}

		listener.create();
		listener.resize(graphics.getWidth(), graphics.getHeight());
		graphics.resize = false;

		int lastWidth = graphics.getWidth();
		int lastHeight = graphics.getHeight();

		graphics.lastTime = System.nanoTime();
		while (running) {
			if (Display.isCloseRequested()) {
				exit();
			}

			graphics.updateTime();
			if (graphics.resize) {
				graphics.resize = false;
				listener.resize(graphics.getWidth(), graphics.getHeight());
			}
			synchronized (runnables) {
				for (int i = 0; i < runnables.size(); i++) {
					try {
						runnables.get(i).run();
					}
					catch(Throwable t) {
						t.printStackTrace();
					}
				}
				runnables.clear();
			}
			input.update();

			if (graphics.canvas != null) {
				int width = graphics.canvas.getWidth();
				int height = graphics.canvas.getHeight();
				if (lastWidth != width || lastHeight != height) {
					lastWidth = width;
					lastHeight = height;
					listener.resize(lastWidth, lastHeight);
				}
			}

			input.processEvents();
			listener.render();
			audio.update();
			Display.update();
			if (graphics.vsync && graphics.config.useCPUSynch) {
				Display.sync(60);
			}
		}

		listener.pause();
		listener.dispose();
		Display.destroy();
		audio.dispose();
		if (graphics.config.forceExit) System.exit(-1);
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

	Map<String, Preferences> preferences = new HashMap<String, Preferences>();

	@Override
	public Preferences getPreferences (String name) {
		if (preferences.containsKey(name)) {
			return preferences.get(name);
		} else {
			Preferences prefs = new LwjglPreferences(name);
			preferences.put(name, prefs);
			return prefs;
		}
	}

	@Override
	public void postRunnable (Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
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

	public void log (String tag, String message) {
		if (logLevel >= LOG_INFO) {
			System.out.println(tag + ": " + message);
		}
	}

	@Override
	public void log (String tag, String message, Exception exception) {
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
	public void exit () {
		postRunnable(new Runnable() {
			@Override
			public void run () {
				running = false;
			}
		});
	}
}
