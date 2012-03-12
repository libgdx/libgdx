package com.badlogic.gdx.tests.gwt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.tests.utils.GdxTest;

public class GwtInputTest extends GdxTest {
	ShapeRenderer renderer;

	@Override
	public void create () {
		renderer = new ShapeRenderer();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		renderer.begin(ShapeType.FilledRectangle);
		if(Gdx.input.isTouched()) renderer.setColor(Color.RED);
		else renderer.setColor(Color.GREEN);
		renderer.filledRect(Gdx.input.getX() - 15, Gdx.graphics.getHeight() - Gdx.input.getY() - 15, 30, 30);
		renderer.end();
	}

	@Override
	public boolean needsGL20 () {
		return true;
	}
}
