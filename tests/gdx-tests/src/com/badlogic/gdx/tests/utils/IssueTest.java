package com.badlogic.gdx.tests.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Placeholder class that's wired up with all backends so i can quickly test out
 * issues...
 * @author badlogic
 *
 */
public class IssueTest extends GdxTest {
	SpriteBatch batch;
	Texture img;
	Texture img2;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("data/issue/bark.png");
		img2 = new Texture("data/issue/leaf.png");
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.draw(img2, 512, 0);
		batch.end();
	}
}
