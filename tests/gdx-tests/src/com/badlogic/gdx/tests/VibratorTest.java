package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;

public class VibratorTest extends GdxTest {

	@Override public boolean needsGL20 () {
		return false;
	}

	SpriteBatch batch;
	BitmapFont font;
	
	@Override
	public void create() {
		batch = new SpriteBatch();
		font = new BitmapFont();
	}
	
	@Override 
	public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.draw(batch, "Touch screen to vibrate", 100, 100);
		batch.end();
		
		if(Gdx.input.justTouched())
			Gdx.input.vibrate(100);		
	}
}
