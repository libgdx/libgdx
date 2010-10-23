
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.BitmapFont;
import com.badlogic.gdx.graphics.BitmapFontCache;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Sprite;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class BitmapFontTest implements RenderListener {
	private SpriteBatch spriteBatch;
	private BitmapFont font;
	private Sprite logoSprite;
	private Color blue = new Color(0, 0, 1, 0.5f);
	private BitmapFontCache cache1, cache2, cache3, cache4;
	int renderMode;

	public void surfaceCreated () {
		if (spriteBatch != null) return;
		spriteBatch = new SpriteBatch();

		logoSprite = new Sprite(Gdx.graphics.newTexture(Gdx.files.getFileHandle("data/badlogic.jpg", FileType.Internal),
			TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge));
		logoSprite.setColor(1, 1, 1, 0.5f);

		font = new BitmapFont(Gdx.files.getFileHandle("data/verdana39.fnt", FileType.Internal), Gdx.files.getFileHandle(
			"data/verdana39.png", FileType.Internal));

		Gdx.input.addInputListener(new InputAdapter() {
			public boolean touchDown (int x, int y, int pointer) {
				renderMode = (renderMode + 1) % 2;
				return false;
			}
		});

		cache1 = font.cacheText(null, "(cached)", 10, 76, Color.WHITE);

		String text = "Sphinx of black quartz,\njudge my vow.";
		cache2 = font.cacheMultiLineText(null, text, 5, 310, Color.RED, 470, BitmapFont.HAlignment.LEFT);

		text = "How quickly\ndaft jumping zebras vex.";
		cache3 = font.cacheMultiLineText(null, text, 5, 210, blue, 470, BitmapFont.HAlignment.CENTER);

		text = "Kerning: LYA moo";
		cache4 = font.cacheText(null, text, 210, 76, Color.WHITE, 0, text.length() - 3);
	}

	public void surfaceChanged (int width, int height) {
	}

	public void render () {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		logoSprite.draw(spriteBatch);
		switch (renderMode) {
		case 0:
			renderNormal();
			break;
		case 1:
			renderCached();
			break;
		}
		spriteBatch.end();
	}

	private void renderNormal () {
		font.draw(spriteBatch, "(normal)", 10, 76, Color.WHITE);

		String text = "Sphinx of black quartz,\njudge my vow.";
		font.drawMultiLineText(spriteBatch, text, 5, 310, Color.RED);

		text = "How quickly\ndaft jumping zebras vex.";
		font.drawMultiLineText(spriteBatch, text, 5, 210, blue, 470, BitmapFont.HAlignment.CENTER);

		text = "Kerning: LYA moo";
		font.draw(spriteBatch, text, 210, 76, Color.WHITE, 0, text.length() - 3);
	}

	private void renderCached () {
		cache1.draw(spriteBatch);
		cache2.draw(spriteBatch);
		cache3.draw(spriteBatch);
		cache4.draw(spriteBatch);
	}

	public void dispose () {
	}
}
