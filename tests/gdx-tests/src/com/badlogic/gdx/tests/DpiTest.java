package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;

public class DpiTest extends GdxTest {
	BitmapFont font;
	SpriteBatch batch;
	
	@Override
	public void create () {
		font = new BitmapFont();
		batch = new SpriteBatch();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.draw(batch, 
			"Density: " + Gdx.graphics.getDensity() + "\n" +
			"PPC-x: " + Gdx.graphics.getPpcX() + "\n" + 
			"PPC-y: " + Gdx.graphics.getPpcY() + "\n" +
			"PPI-x: " + Gdx.graphics.getPpiX() + "\n" +
			"PPI-y: " + Gdx.graphics.getPpiY()
			, 0, Gdx.graphics.getHeight());
		batch.end();
	}

	@Override
	public void resize (int width, int height) {
	}
}
