
package com.badlogic.gdx.backends.lwjgl;

import java.awt.Canvas;

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
 * An OpenGL surface fullscreen or in a lightweight window.
 */
public class LwjglApplication implements Application {
	LwjglGraphics graphics;
	LwjglAudio audio;
	LwjglFiles files;
	LwjglInput input;
	final ApplicationListener listener;
	Thread mainLoopThread;
	boolean running = true;

	public LwjglApplication (ApplicationListener listener, String title, int width, int height, boolean useGL2) {
		graphics = new LwjglGraphics(title, width, height, useGL2);
		audio = new LwjglAudio();
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
		initialize();
	}

	private void initialize () {
		LwjglNativesLoader.load();
		mainLoopThread = new Thread("LWJGL Application") {
			@SuppressWarnings("synthetic-access") public void run () {
				LwjglApplication.this.mainLoop();
			}
		};
		mainLoopThread.start();
	}

	private void mainLoop () {
		try {
			graphics.setupDisplay();
		} catch (LWJGLException e) {
			throw new GdxRuntimeException(e);
		}

		Keyboard.enableRepeatEvents(true);
		listener.create();
		listener.resize(graphics.getWidth(), graphics.getHeight());

		int lastWidth = graphics.getWidth();
		int lastHeight = graphics.getHeight();

		graphics.lastTime = System.nanoTime();
		while (running && !Display.isCloseRequested()) {
			graphics.updateTime();
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

			((LwjglInput)Gdx.input).processEvents();
			listener.render();
			Display.update();
			Display.sync(60);
		}

		listener.pause();
		listener.dispose();
		Display.destroy();
		audio.dispose();
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

	public void stop () {
		running = false;
		try {
			mainLoopThread.join();
		} catch (Exception ex) {
		}
	}
	
	@Override
	public long getJavaHeap() {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	@Override
	public long getNativeHeap() {
		return getJavaHeap();
	}
}
