
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.BitmapFont;
import com.badlogic.gdx.graphics.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.BitmapFontCache;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Sprite;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.tests.utils.GdxTest;

/**
 * Shows how to align single line, wrapped, and multi line text within a rectangle.
 */
public class BitmapFontAlignmentTest extends GdxTest {
	private SpriteBatch spriteBatch;
	private BitmapFont font;
	private BitmapFontCache cache;
	private Sprite logoSprite;
	int renderMode;

	@Override public void create () {
		spriteBatch = new SpriteBatch();

		logoSprite = new Sprite(Gdx.graphics.newTexture(Gdx.files.getFileHandle("data/badlogic.jpg", FileType.Internal),
			TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge));
		logoSprite.setColor(1, 1, 1, 0.6f);
		logoSprite.setBounds(50, 100, 400, 100);

		font = new BitmapFont(Gdx.files.getFileHandle("data/verdana39.fnt", FileType.Internal), Gdx.files.getFileHandle(
			"data/verdana39.png", FileType.Internal), false);
		cache = new BitmapFontCache(font);

		Gdx.input.setInputProcessor(new InputAdapter() {
			public boolean touchDown (int x, int y, int pointer) {
				renderMode = (renderMode + 1) % 6;
				return false;
			}
		});
	}

	@Override public void render () {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		logoSprite.draw(spriteBatch);
		switch (renderMode) {
		case 0:
			renderSingleLine();
			break;
		case 1:
			renderSingleLineCached();
			break;
		case 2:
			renderWrapped();
			break;
		case 3:
			renderWrappedCached();
			break;
		case 4:
			renderMultiLine();
			break;
		case 5:
			renderMultiLineCached();
			break;
		}
		spriteBatch.end();
	}

	private void renderSingleLine () {
		String text = "Single Line";
		float x = logoSprite.getX();
		float y = logoSprite.getY();
		float width = logoSprite.getWidth();
		float height = logoSprite.getHeight();

		TextBounds bounds = font.getBounds(text);
		x += width / 2 - bounds.width / 2;
		y += height / 2 + bounds.height / 2;

		font.draw(spriteBatch, text, x, y, Color.WHITE);
	}

	private void renderSingleLineCached () {
		String text = "Single Line Cached";
		float x = logoSprite.getX();
		float y = logoSprite.getY();
		float width = logoSprite.getWidth();
		float height = logoSprite.getHeight();

		// Obviously you wouldn't set the cache text every frame in real code.
		cache.setMultiLineText(text, 0, 0, Color.WHITE);

		x += width / 2 - cache.getWidth() / 2;
		y += height / 2 + cache.getHeight() / 2;
		cache.setPosition(x, y);

		cache.draw(spriteBatch);
	}

	private void renderWrapped () {
		String text = "Wrapped Wrapped Wrapped Wrapped";
		float x = logoSprite.getX();
		float y = logoSprite.getY();
		float width = logoSprite.getWidth();
		float height = logoSprite.getHeight();

		TextBounds bounds = font.getWrappedBounds(text, width);
		x += width / 2 - bounds.width / 2;
		y += height / 2 + bounds.height / 2;

		font.drawWrapped(spriteBatch, text, x, y, Color.WHITE, width);

		// Note that wrapped text can be aligned:
		// font.drawWrapped(spriteBatch, text, x, y, Color.WHITE, width, HAlignment.CENTER);
	}

	private void renderWrappedCached () {
		String text = "Wrapped Cached Wrapped Cached";
		float x = logoSprite.getX();
		float y = logoSprite.getY();
		float width = logoSprite.getWidth();
		float height = logoSprite.getHeight();

		// Obviously you wouldn't set the cache text every frame in real code.
		cache.setWrappedText(text, 0, 0, Color.WHITE, width);

		// Note that wrapped text can be aligned:
		// cache.setWrappedText(text, 0, 0, Color.WHITE, width, HAlignment.CENTER);

		x += width / 2 - cache.getWidth() / 2;
		y += height / 2 + cache.getHeight() / 2;
		cache.setPosition(x, y);

		cache.draw(spriteBatch);
	}

	private void renderMultiLine () {
		String text = "Multi\nLine";
		float x = logoSprite.getX();
		float y = logoSprite.getY();
		float width = logoSprite.getWidth();
		float height = logoSprite.getHeight();

		TextBounds bounds = font.getMultiLineBounds(text);
		x += width / 2 - bounds.width / 2;
		y += height / 2 + bounds.height / 2;

		font.drawMultiLine(spriteBatch, text, x, y, Color.WHITE);

		// Note that multi line text can be aligned:
		// font.drawMultiLine(spriteBatch, text, x, y, Color.WHITE, width, HAlignment.CENTER);
	}

	private void renderMultiLineCached () {
		String text = "Multi Line\nCached";
		int lines = 2;
		float x = logoSprite.getX();
		float y = logoSprite.getY();
		float width = logoSprite.getWidth();
		float height = logoSprite.getHeight();

		// Obviously you wouldn't set the cache text every frame in real code.
		cache.setMultiLineText(text, 0, 0, Color.WHITE);

		// Note that multi line text can be aligned:
		// cache.setMultiLineText(text, 0, 0, Color.WHITE, width, HAlignment.CENTER);

		x += width / 2 - cache.getWidth() / 2;
		y += height / 2 + cache.getHeight() / 2;
		cache.setPosition(x, y);

		cache.draw(spriteBatch);
	}

	public boolean needsGL20 () {
		return false;
	}
}
