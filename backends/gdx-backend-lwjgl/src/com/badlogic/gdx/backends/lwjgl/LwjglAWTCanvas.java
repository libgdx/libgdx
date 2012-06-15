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
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.AWTGLCanvas;
import org.lwjgl.opengl.PixelFormat;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.openal.OpenALAudio;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** An OpenGL surface on an AWT Canvas, allowing OpenGL to be embedded in a Swing application. All OpenGL calls are done on the
 * EDT. This is slightly less efficient then a dedicated thread, but greatly simplifies synchronization. Note that you may need to
 * call {@link #stop()} or a Swing application may deadlock on System.exit due to how LWJGL and/or Swing deal with shutdown hooks.
 * @author Nathan Sweet */
public class LwjglAWTCanvas implements Application {
	final LwjglGraphics graphics;
	final OpenALAudio audio;
	final LwjglFiles files;
	final LwjglAWTInput input;
	final ApplicationListener listener;
	final AWTGLCanvas canvas;
	final List<Runnable> runnables = new ArrayList<Runnable>();
	boolean running = true;
	int lastWidth;
	int lastHeight;
	int logLevel = LOG_INFO;

	public LwjglAWTCanvas (ApplicationListener listener, boolean useGL2) {
		this(listener, useGL2, null);
	}

	public LwjglAWTCanvas (ApplicationListener listener, boolean useGL2,
			LwjglAWTCanvas sharedContextCanvas) {
		LwjglNativesLoader.load();

		AWTGLCanvas sharedDrawable = sharedContextCanvas != null ?
			sharedContextCanvas.canvas : null;
		try {
			canvas = new AWTGLCanvas(
				GraphicsEnvironment.getLocalGraphicsEnvironment()
					.getDefaultScreenDevice(),
					new PixelFormat(),
					sharedDrawable) {
				private final Dimension minSize = new Dimension(0, 0);

				public Dimension getMinimumSize () {
					return minSize;
				}
			
				@Override
				public void initGL () {
					start();
				}

				@Override
				public void paintGL () {
					try {
						LwjglAWTCanvas.this.render();
						swapBuffers();
						repaint();
					} catch (LWJGLException ex) {
						throw new GdxRuntimeException(ex);
					}
				}
			};
		} catch (LWJGLException ex) {
			throw new GdxRuntimeException(ex);
		}

		graphics = new LwjglGraphics(canvas, useGL2) {
			public void setTitle (String title) {
				super.setTitle(title);
				LwjglAWTCanvas.this.setTitle(title);
			}

			public boolean setDisplayMode (int width, int height, boolean fullscreen) {
				if (!super.setDisplayMode(width, height, fullscreen)) return false;
				if (!fullscreen) LwjglAWTCanvas.this.setDisplayMode(width, height);
				return true;
			}

			public boolean setDisplayMode (DisplayMode displayMode) {
				if (!super.setDisplayMode(displayMode)) return false;
				LwjglAWTCanvas.this.setDisplayMode(displayMode.width, displayMode.height);
				return true;
			}
		};
		if (Gdx.audio == null) {
			audio = new OpenALAudio();
			Gdx.audio = audio;
		} else {
			audio = null;
		}
		if(Gdx.files == null) {
			files = new LwjglFiles();
			Gdx.files = files;
		} else {
			files = null;
		}
		input = new LwjglAWTInput(canvas);
		this.listener = listener;

		setGlobals();
	}

	protected void setDisplayMode (int width, int height) {
	}

	protected void setTitle (String title) {
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
	public ApplicationType getType () {
		return ApplicationType.Desktop;
	}

	@Override
	public int getVersion () {
		return 0;
	}
	
	void setGlobals() {
		Gdx.app = this;
		if(audio != null) Gdx.audio = audio;
		if(files != null) Gdx.files = files;
		Gdx.graphics = graphics;
		Gdx.input = input;
	}

	void start () {
		try {
			setGlobals();
			graphics.initiateGLInstances();
			listener.create();
			lastWidth = Math.max(1, graphics.getWidth());
			lastHeight = Math.max(1, graphics.getHeight());
			listener.resize(lastWidth, lastHeight);
		} catch (Exception ex) {
			stopped();
			throw new GdxRuntimeException(ex);
		}
	}

	void render () {
		setGlobals();
		canvas.setCursor(null);
		graphics.updateTime();
		synchronized (runnables) {
			for (int i = 0; i < runnables.size(); i++) {
				try {
					runnables.get(i).run();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			runnables.clear();
		}

		int width = Math.max(1, graphics.getWidth());
		int height = Math.max(1, graphics.getHeight());
		if (lastWidth != width || lastHeight != height) {
			lastWidth = width;
			lastHeight = height;
			Gdx.gl.glViewport(0, 0, lastWidth, lastHeight);
			resize(width, height);
			listener.resize(width, height);
		}
		input.processEvents();
		listener.render();
		if (audio != null) {
			audio.update();
		}
	}

	protected void resize (int width, int height) {
	}

	protected void stopped () {
	}

	public void stop () {
		if (!running) return;
		running = false;
		setGlobals();
		listener.pause();
		listener.dispose();
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
				setGlobals();
				LwjglAWTCanvas.this.listener.pause();
				LwjglAWTCanvas.this.listener.dispose();
				System.exit(-1);
			}
		});
	}
}
