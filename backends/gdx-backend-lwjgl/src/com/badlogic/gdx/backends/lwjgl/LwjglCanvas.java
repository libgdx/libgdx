
package com.badlogic.gdx.backends.lwjgl;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.EventQueue;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * An OpenGL surface on an AWT Canvas, allowing OpenGL to be embedded in a Swing application. All OpenGL calls are done on the
 * EDT. This is slightly less efficient then a dedicated thread, but greatly simplifies synchronization.
 * @author Nathan Sweet <misc@n4te.com>
 */
public class LwjglCanvas implements Application {
	final LwjglGraphics graphics;
	final LwjglAudio audio;
	final LwjglFiles files;
	final LwjglInput input;
	final ApplicationListener listener;
	final Canvas canvas;
	boolean running = true;

	public LwjglCanvas (ApplicationListener listener, boolean useGL2) {
		LwjglNativesLoader.load();

		canvas = new Canvas() {
			private final Dimension minSize = new Dimension(0, 0);

			public final void addNotify () {
				super.addNotify();
				EventQueue.invokeLater(new Runnable() {
					public void run () {
						start();
					}
				});
			}

			public final void removeNotify () {
				stop();
				super.removeNotify();
			}

			public Dimension getMinimumSize () {
				return minSize;
			}
		};
		canvas.setIgnoreRepaint(true);

		graphics = new LwjglGraphics(canvas, useGL2);
		audio = new LwjglAudio();
		files = new LwjglFiles();
		input = new LwjglInput();
		this.listener = listener;

		Gdx.app = this;
		Gdx.graphics = graphics;
		Gdx.audio = audio;
		Gdx.files = files;
		Gdx.input = input;
	}

	public Canvas getCanvas () {
		return canvas;
	}

	@Override public Audio getAudio () {
		return audio;
	}

	@Override public Files getFiles () {
		return files;
	}

	@Override public Graphics getGraphics () {
		return graphics;
	}

	@Override public Input getInput () {
		return input;
	}

	@Override public ApplicationType getType () {
		return ApplicationType.Desktop;
	}

	@Override public int getVersion () {
		return 0;
	}

	@Override public void log (String tag, String message) {
		System.out.println(tag + ": " + message);
	}

	void start () {
		try {
			graphics.setupDisplay();
		} catch (LWJGLException e) {
			throw new GdxRuntimeException(e);
		}

		Keyboard.enableRepeatEvents(true);

		listener.create();
		listener.resize(graphics.getWidth(), graphics.getHeight());

		final Runnable runnable = new Runnable() {
			int lastWidth = Math.max(1, graphics.getWidth());
			int lastHeight = Math.max(1, graphics.getHeight());

			public void run () {
				if (!running) return;
				graphics.updateTime();
				input.update();

				int width = Math.max(1, graphics.getWidth());
				int height = Math.max(1, graphics.getHeight());
				if (lastWidth != width || lastHeight != height) {
					lastWidth = width;
					lastHeight = height;
					listener.resize(width, height);
				}

				listener.render();
				input.processEvents(null);
				Display.update();
				Display.sync(60);
			}
		};

		new Thread("LWJGL Canvas") {
			public void run () {
				while (running && !Display.isCloseRequested()) {
					try {
						EventQueue.invokeAndWait(runnable);
					} catch (Exception ex) {
						throw new GdxRuntimeException(ex);
					}
				}
			}
		}.start();
	}

	public void stop () {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				if (!running) return;
				running = false;
				listener.pause();
				listener.dispose();
				Display.destroy();
			}
		});
	}
}
