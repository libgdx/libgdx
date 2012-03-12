package com.badlogic.gdx.tests.gwt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.tests.utils.GdxTest;

public class GwtInputTest extends GdxTest {
	ShapeRenderer renderer;
	
	@Override
	public void create () {
		renderer = new ShapeRenderer(4);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		renderer.begin(ShapeType.FilledRectangle);
		renderer.filledRect(Gdx.input.getX(), Gdx.input.getY(), 30, 30);
		renderer.end();
	}

	@Override
	public boolean needsGL20 () {
		return true;
	}
}
