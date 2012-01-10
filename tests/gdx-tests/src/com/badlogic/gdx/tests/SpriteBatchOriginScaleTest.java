package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.tests.utils.GdxTest;

/**
 * Test for issue http://code.google.com/p/libgdx/issues/detail?id=493
 * @author mzechner
 *
 */
public class SpriteBatchOriginScaleTest extends GdxTest {
	SpriteBatch batch;
	TextureRegion region;
	ShapeRenderer renderer;
	
	@Override
	public void create() {
		region = new TextureRegion(new Texture("data/badlogicsmall.jpg"));
		batch = new SpriteBatch();
		renderer = new ShapeRenderer();
		renderer.setProjectionMatrix(batch.getProjectionMatrix());
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		renderer.begin(ShapeType.Line);
		renderer.setColor(1, 1, 1, 1);
		renderer.line(0, 100, Gdx.graphics.getWidth(), 100);
		renderer.line(100, 0, 100, Gdx.graphics.getHeight());
		renderer.end();
		
		batch.begin();
		batch.draw(region, 100, 100, 0, 0, 32, 32, 2, 2, 20);
		batch.end();
	}
	
}
