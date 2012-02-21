package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Jpeg;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;

/**
 * Test for the gdx-image extension. Usually you'd just use
 * Pixmap directly. gdx-image can be used if more robust jpeg
 * loading is necessary, e.g. if the images come from the web.
 *  
 * @author mzechner
 *
 */
public class JpegTest extends GdxTest {
	Texture texture;
	SpriteBatch batch;
	
	@Override
	public void create () {
		texture = new Texture(Jpeg.read(Gdx.files.internal("data/43kJgl.jpg")));
		batch = new SpriteBatch();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(texture, 0, 0);
		batch.end();
	}

	@Override
	public boolean needsGL20 () {
		return true;
	}	
}
