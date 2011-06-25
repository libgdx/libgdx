package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.tests.utils.GdxTest;

public class IntegerBitmapFontTest extends GdxTest {

	@Override public boolean needsGL20 () {
		return false;
	}

	BitmapFont font;
	BitmapFontCache singleLineCacheNonInteger;
	BitmapFontCache multiLineCacheNonInteger;
	BitmapFontCache singleLineCache;
	BitmapFontCache multiLineCache;
	SpriteBatch batch;
	
	public void create() {
		font = new BitmapFont();
		singleLineCache = new BitmapFontCache(font, true);
		multiLineCache = new BitmapFontCache(font, true);
		singleLineCacheNonInteger = new BitmapFontCache(font, false);
		multiLineCacheNonInteger = new BitmapFontCache(font, false);
		batch = new SpriteBatch();
		fillCaches();
	}
	
	public void render() {		
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.setUseIntegerPositions(false);
		font.setColor(1, 0, 0, 1);
		singleLineCacheNonInteger.draw(batch);
		multiLineCacheNonInteger.draw(batch);
		drawTexts();
		font.setUseIntegerPositions(true);
		font.setColor(1, 1, 1, 1);
		singleLineCache.draw(batch);
		multiLineCache.draw(batch);
		drawTexts();
		batch.end();
	}
	
	private void fillCaches() {
		String text = "This is a TEST\nxahsdhwekjhasd23§$%$%/%&";
		singleLineCache.setColor(0, 0, 1, 1);
		singleLineCache.setText(text, 10.2f, 30.5f);
		multiLineCache.setColor(0, 0, 1, 1);
		multiLineCache.setMultiLineText(text, 10.5f, 180.5f, 200, HAlignment.CENTER);
		singleLineCacheNonInteger.setColor(0, 1, 0, 1);
		singleLineCacheNonInteger.setText(text, 10.2f, 30.5f);
		multiLineCacheNonInteger.setColor(0, 1, 0, 1);
		multiLineCacheNonInteger.setMultiLineText(text, 10.5f, 180.5f, 200, HAlignment.CENTER);
	}
	
	private void drawTexts() {
		String text = "This is a TEST\nxahsdhwekjhasd23§$%$%/%&";
		font.draw(batch, text, 10.2f, 30.5f);		
		font.drawMultiLine(batch, text, 10.5f, 120.5f);
		font.drawMultiLine(batch, text, 10.5f, 180.5f, 200, HAlignment.CENTER);
	}
}
