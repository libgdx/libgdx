
package com.badlogic.gdx.tests.lwjgl3;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;

public class MultiWindowCursorTest {
	/** A wrapper for an ApplicationListener that assigns a random cursor to each window on creation. Cursors are either
	 * system-defined or custom, loaded from images. */
	static class WindowWithCursorListener implements ApplicationListener {
		ApplicationListener listener;

		/** Constructs a WindowWithCursorListener wrapping the specified listener.
		 *
		 * @param listener the ApplicationListener to wrap */
		WindowWithCursorListener (ApplicationListener listener) {
			this.listener = listener;
		}

		/** Initializes the window and assigns a random cursor (system or custom). */
		@Override
		public void create () {
			listener.create();
			if (MathUtils.randomBoolean()) {
				Cursor.SystemCursor[] systemCursors = Cursor.SystemCursor.values();
				Cursor.SystemCursor systemCursor = systemCursors[MathUtils.random(systemCursors.length - 1)];
				Gdx.graphics.setSystemCursor(systemCursor);
			} else {
				Pixmap pixmap;
				if (MathUtils.randomBoolean()) {
					pixmap = new Pixmap(Gdx.files.internal("data/particle-star.png"));
				} else {
					pixmap = new Pixmap(Gdx.files.internal("data/ps-bobargb8888-32x32.png"));
				}
				Cursor cursor = Gdx.graphics.newCursor(pixmap, pixmap.getWidth() / 2, pixmap.getHeight() / 2);
				Gdx.graphics.setCursor(cursor);
				pixmap.dispose();
			}
		}

		/** Called when the window is resized.
		 *
		 * @param width the new width of the window
		 * @param height the new height of the window */
		@Override
		public void resize (int width, int height) {
			listener.resize(width, height);
		}

		@Override
		public void render () {
			listener.render();
		}

		@Override
		public void pause () {
			listener.pause();
		}

		@Override
		public void resume () {
			listener.resume();
		}

		@Override
		public void dispose () {
			listener.dispose();
		}
	}

	/** Main window class for the multi-window test. Creates child windows with the custom WindowWithCursorListener. */
	public static class MainWindow extends MultiWindowTest.MainWindow {
		/** Creates a child window with a custom WindowWithCursorListener.
		 *
		 * @param clazz the class of the child window to be created
		 * @return the ApplicationListener for the new child window */
		@Override
		public ApplicationListener createChildWindowClass (Class clazz) {
			return new WindowWithCursorListener(super.createChildWindowClass(clazz));
		}
	}

	/** Configures and launches the main application window for the multi-window cursor test.
	 *
	 * @param argv command-line arguments (not used) */
	public static void main (String[] argv) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Multi-window test with cursors");
		new Lwjgl3Application(new MainWindow(), config);
	}
}
