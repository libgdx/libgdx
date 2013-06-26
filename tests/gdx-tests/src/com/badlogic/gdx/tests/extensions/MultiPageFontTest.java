package com.badlogic.gdx.tests.extensions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.tests.utils.GdxTest;

public class MultiPageFontTest extends GdxTest {

	SpriteBatch batch;
	OrthographicCamera camera;
	BitmapFont font, smallFont;
	BitmapFontCache cache;
	
	BitmapFont ttfFont;
	
	@Override
	public void create () {
		camera = new OrthographicCamera();
		batch = new SpriteBatch();
		smallFont = new BitmapFont();
		font = new BitmapFont(Gdx.files.internal("data/multipagefont.fnt"), false);
		
		cache = new BitmapFontCache(font);
		
		FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("data/arial.ttf"));
		ttfFont = gen.generateFont(146);
		gen.dispose();
		
		System.out.println(ttfFont.getRegions().length);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		//qpg characters are part of texture 1
		//RPN characters are part of texture 2
		String str = "bcdqpg\nRPNqpgRPNbcd";
		int start = 4;
		int end = str.length()-1;
		
		batch.renderCalls = 0;
		
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		//font.draw(batch, "Test Red RPN", 5, Gdx.graphics.getHeight()-5);
		//font.draw(batch, str, 5, Gdx.graphics.getHeight()-5, start, end);
		
		cache.clear();
		cache.setMultiLineText(str,5, Gdx.graphics.getHeight()-25);
		cache.setColor(Color.RED, 1, str.length()-4);
		cache.setColor(Color.PINK, 2, 5);
		cache.draw(batch, start, end);
		
		batch.end();
		
		int r = batch.renderCalls;
		
		batch.begin();
		smallFont.draw(batch, "Render Calls for above string: "+r, 20, Gdx.graphics.getHeight()-100);
		smallFont.drawMultiLine(batch, "Texture pages used for big TTF font: "+ttfFont.getRegions().length
							+"\nMax Generated Texture Size: "+FreeTypeFontGenerator.maxTextureSize, 20, smallFont.getLineHeight()*2+10);
		
		batch.end();
		

		batch.begin();
		ttfFont.draw(batch, "Big text", 5, 200);
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
