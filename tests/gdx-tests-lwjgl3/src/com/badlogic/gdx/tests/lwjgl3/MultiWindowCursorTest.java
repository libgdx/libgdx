package com.badlogic.gdx.tests.lwjgl3;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;

public class MultiWindowCursorTest {

	static class WindowWithCursorListener implements ApplicationListener {
		ApplicationListener listener;

		WindowWithCursorListener(ApplicationListener listener) {
			this.listener = listener;
		}

		@Override
		public void create() {
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

		@Override
		public void resize(int width, int height) {
			listener.resize(width, height);
		}

		@Override
		public void render() {
			listener.render();
		}

		@Override
		public void pause() {
			listener.pause();
		}

		@Override
		public void resume() {
			listener.resume();
		}

		@Override
		public void dispose() {
			listener.dispose();
		}
	}

	public static class MainWindow extends MultiWindowTest.MainWindow {
		@Override
		public ApplicationListener createChildWindowClass(Class clazz) {
			return new WindowWithCursorListener(super.createChildWindowClass(clazz));
		}
	}

	public static void main(String[] argv) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Multi-window test with cursors");
		new Lwjgl3Application(new MainWindow(), config);
	}
}
