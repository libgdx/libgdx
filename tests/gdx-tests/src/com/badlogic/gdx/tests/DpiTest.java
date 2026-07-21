
package com.badlogic.gdx.tests;

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
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.draw(batch,
			"Density: " + graphics.getDensity() + "\n" + "PPC-x: " + graphics.getPpcX() + "\n" + "PPC-y: "
				+ graphics.getPpcY() + "\n" + "PPI-x: " + graphics.getPpiX() + "\n" + "PPI-y: " + graphics.getPpiY(),
			0, graphics.getHeight());
		batch.end();
	}

	@Override
	public void resize (int width, int height) {
	}
}
