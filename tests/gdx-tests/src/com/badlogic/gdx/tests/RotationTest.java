package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;

public class RotationTest extends GdxTest {

	@Override public boolean needsGL20 () {
		return false;
	}
	
	Texture texture;
	SpriteBatch batch;
	
	
	@Override public void create() {
		texture = new Texture(Gdx.files.internal("data/black_marked_0.png"));
		batch = new SpriteBatch();
	}
	
	@Override public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(texture, 100.5f, 100.5f);
		batch.end();
	}
}
