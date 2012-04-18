package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.tests.utils.GdxTest;

/**
 * Tests alpha blending with all ShapeRenderer shapes.
 * @author mzechner
 *
 */
public class ShapeRendererAlphaTest extends GdxTest {
	ShapeRenderer renderer;

	@Override
	public void create () {
		renderer = new ShapeRenderer();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		renderer.begin(ShapeType.Rectangle);
		renderer.setColor(1, 0, 0, 0.5f);
		renderer.rect(0, 0, 100, 200);
		renderer.end();
		
		renderer.begin(ShapeType.FilledRectangle);
		renderer.setColor(0, 1, 0, 0.5f);
		renderer.filledRect(200, 0, 100, 100);
		renderer.end();
		
		renderer.begin(ShapeType.Circle);
		renderer.setColor(0, 1, 0, 0.5f);
		renderer.circle(400, 50, 50);
		renderer.end();
		
		renderer.begin(ShapeType.FilledCircle);
		renderer.setColor(1, 0, 1, 0.5f);
		renderer.filledCircle(500, 50, 50);
		renderer.end();
	}

	@Override
	public void dispose () {
		renderer.dispose();
	}
}
