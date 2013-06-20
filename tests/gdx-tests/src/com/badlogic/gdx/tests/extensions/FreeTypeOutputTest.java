package com.badlogic.gdx.tests.extensions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;

public class FreeTypeOutputTest extends GdxTest {

	SpriteBatch batch;
	OrthographicCamera camera;
	
	@Override
	public void create () {
		camera = new OrthographicCamera();
		batch = new SpriteBatch();
		
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		batch.end();
	}

	@Override
	public boolean needsGL20 () {
		return true;
	}
	

	@Override
	public void dispose () {
		super.dispose();
		batch.dispose();
	}
}
