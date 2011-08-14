
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;

public class OnscreenKeyboardTest extends GdxTest implements InputProcessor {

	@Override
	public boolean needsGL20 () {
		return false;
	}

	BitmapFont font;
	String text;
	SpriteBatch batch;

	public void create () {
		batch = new SpriteBatch();
		font = new BitmapFont();
		text = "";
		Gdx.input.setInputProcessor(this);
		Gdx.input.setOnscreenKeyboardVisible(true);
	}

	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.draw(batch, "input: " + text, 0, Gdx.graphics.getHeight());
		batch.end();

		if (Gdx.input.justTouched()) Gdx.input.setOnscreenKeyboardVisible(true);
	}

	@Override
	public boolean keyDown (int keycode) {

		return false;
	}

	@Override
	public boolean keyUp (int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped (char character) {
		if (character == '\b' && text.length() >= 1) {
			text = text.substring(0, text.length() - 1);
		} else if (character == '\n') {
			Gdx.input.setOnscreenKeyboardVisible(false);
		} else {
			text += character;
		}
		return false;
	}

	@Override
	public boolean touchDown (int x, int y, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchMoved (int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled (int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}
