package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.viewport.MultiAspectViewport;

public class ViewportTestMultiAspectRatio extends GdxTest {
	
	MultiAspectViewport viewport;

   float minWidth;
   float minHeight;
   float maxWidth;
   float maxHeight;

   SpriteBatch spriteBatch;
   ShapeRenderer shaper;
   BitmapFont font;
   
   Sprite minimumAreaSprite;
   Sprite maximumAreaSprite;
   Sprite floatingButtonSprite;

	@Override
	public void create () {
		float minWidth = 1200;
		float minHeight = 720;
		float maxWidth = 1280;
		float maxHeight = 900;
		
		viewport = new MultiAspectViewport();
		viewport.setup(minWidth, minHeight, maxWidth, maxHeight, 4f / 3f, 16f / 9f);
		spriteBatch = new SpriteBatch();
		shaper = new ShapeRenderer();
		font = new BitmapFont();
		font.setColor(Color.BLACK);
		font.scale(4f);

		Pixmap pixmap = new Pixmap(64, 64, Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fillRectangle(0, 0, 64, 64);

		minimumAreaSprite = new Sprite(new Texture(pixmap));
		minimumAreaSprite.setPosition(-minWidth / 2, -minHeight / 2);
		minimumAreaSprite.setSize(minWidth, minHeight);
		minimumAreaSprite.setColor(0f, 1f, 0f, 1f);

		maximumAreaSprite = new Sprite(new Texture(pixmap));
		maximumAreaSprite.setPosition(-maxWidth / 2, -maxHeight / 2);
		maximumAreaSprite.setSize(maxWidth, maxHeight);
		maximumAreaSprite.setColor(1f, 1f, 0f, 1f);
		
		floatingButtonSprite = new Sprite(new Texture(pixmap));
		floatingButtonSprite.setPosition(viewport.getWorldWidth() * 0.5f - 80, viewport.getWorldHeight() * 0.5f - 80);
		floatingButtonSprite.setSize(64, 64);
		floatingButtonSprite.setColor(1f, 1f, 1f, 1f);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1f, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
		shaper.setProjectionMatrix(viewport.getCamera().combined);
		spriteBatch.begin();
		shaper.begin(ShapeType.Line);
		shaper.setColor(Color.BLACK);
		maximumAreaSprite.draw(spriteBatch);
		minimumAreaSprite.draw(spriteBatch);
		floatingButtonSprite.draw(spriteBatch);
		String tmp = String.format("%1$sx%2$s", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		font.draw(spriteBatch, tmp, -font.getBounds(tmp).width / 2, 0);  
		shaper.rect(viewport.getWorldWidth() / -2 + 5, viewport.getWorldHeight() / -2 + 5,
			viewport.getWorldWidth() - 10, viewport.getWorldHeight() - 10);

		spriteBatch.end();
		shaper.end();
	}

	@Override
	public void resize (int width, int height) {
		viewport.update(width, height, false);
		floatingButtonSprite.setPosition(viewport.getWorldWidth() * 0.5f - 80, viewport.getWorldHeight() * 0.5f - 80);
	}

	@Override
	public void dispose () {
		spriteBatch.dispose();
		shaper.dispose();
		font.dispose();
	}
	
}
