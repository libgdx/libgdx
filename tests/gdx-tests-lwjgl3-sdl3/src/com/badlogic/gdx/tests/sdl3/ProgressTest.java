
package com.badlogic.gdx.tests.sdl3;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.sdl3.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class ProgressTest extends ApplicationAdapter {

	private SpriteBatch batch;
	private Texture texture;
	private Lwjgl3Window.ProgressState[] states = new Lwjgl3Window.ProgressState[] {Lwjgl3Window.ProgressState.Normal,
		Lwjgl3Window.ProgressState.Indeterminate, Lwjgl3Window.ProgressState.Error, Lwjgl3Window.ProgressState.None};
	private int index = 0;

	@Override
	public void create () {
		System.out.println(Gdx.graphics.getGLVersion().getRendererString());
		batch = new SpriteBatch();
		texture = new Texture("data/badlogic.jpg");
	}

	@Override
	public void render () {
		ScreenUtils.clear(1, 0, 0, 1);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.begin();
		batch.draw(texture, Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY() - 1);
		batch.end();

		if (Gdx.input.isTouched()) {
			Lwjgl3Window window = ((Lwjgl3Graphics)Gdx.graphics).getWindow();
			window.setProgressState(states[index]);
			window.setProgress(Gdx.input.getX() / ((float)Gdx.graphics.getWidth()));
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			index = (index + 1) % states.length;
		}
	}

	public static void main (String[] argv) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Progress test");
		config.useVsync(true);
		config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);
		new Lwjgl3Application(new ProgressTest(), config);
	}
}
