package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Bresenham2;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tests.utils.GdxTest;

public class Bresenham2Test extends GdxTest {
	SpriteBatch batch;
	Texture result;
	
	@Override
	public void create () {
		Pixmap pixmap = new Pixmap(512, 512, Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		
		Bresenham2 bresenham = new Bresenham2();
		for(GridPoint2 point: bresenham.line(0, 0, 512, 512)) pixmap.drawPixel(point.x, point.y);
		for(GridPoint2 point: bresenham.line(512, 0, 0, 512)) pixmap.drawPixel(point.x, point.y);
		for(GridPoint2 point: bresenham.line(0, 0, 512, 256)) pixmap.drawPixel(point.x, point.y);
		for(GridPoint2 point: bresenham.line(512, 0, 0, 256)) pixmap.drawPixel(point.x, point.y);
		
		result = new Texture(pixmap);
		batch = new SpriteBatch();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(result, 0, 0);
		batch.end();
	}
}
