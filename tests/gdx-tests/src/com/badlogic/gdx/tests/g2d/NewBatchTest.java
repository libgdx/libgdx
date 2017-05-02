package com.badlogic.gdx.tests.g2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.newspritebatch.NewBatch;
import com.badlogic.gdx.graphics.g2d.newspritebatch.NewSpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;

public class NewBatchTest extends GdxTest {
	NewBatch batch;
	Texture tex;

	@Override
	public void create () {		
		batch = new NewSpriteBatch();
		tex = new Texture("data/badlogic.jpg");
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.begin();
		batch.quad().texture(tex).position(100, 100).origin(128, 128).rotation(45).end();			
		batch.end();
	}
}
