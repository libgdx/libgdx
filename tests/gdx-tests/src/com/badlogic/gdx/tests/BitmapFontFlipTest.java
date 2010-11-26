
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.graphics.BitmapFont;
import com.badlogic.gdx.graphics.BitmapFontCache;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Sprite;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tests.utils.GdxTest;

public class BitmapFontFlipTest extends GdxTest {
	private SpriteBatch spriteBatch;
	private BitmapFont font;
	private Sprite logoSprite;
	private Color red = new Color(1, 0, 0, 0.5f);
	private BitmapFontCache cache1, cache2, cache3, cache4, cache5;
	int renderMode;
	private float alpha;
	private InputProcessor inputProcessor;

	@Override
	public void create () {
		spriteBatch = new SpriteBatch();
		spriteBatch.setProjectionMatrix(new Matrix4().setToOrtho(0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1));

		logoSprite = new Sprite(Gdx.graphics.newTexture(Gdx.files.getFileHandle("data/badlogic.jpg", FileType.Internal),
			TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge));
		logoSprite.flip(false, true);
		logoSprite.setPosition(0, 320 - 256);
		logoSprite.setColor(1, 1, 1, 0.5f);

		font = new BitmapFont(Gdx.files.getFileHandle("data/verdana39.fnt", FileType.Internal), Gdx.files.getFileHandle(
			"data/verdana39.png", FileType.Internal), true);

		inputProcessor = new InputAdapter() {
			public boolean touchDown (int x, int y, int pointer) {
				renderMode = (renderMode + 1) % 2;
				return false;
			}
		};
		Gdx.input.setInputProcessor(inputProcessor);

		cache1 = new BitmapFontCache(font);
		cache2 = new BitmapFontCache(font);
		cache3 = new BitmapFontCache(font);
		cache4 = new BitmapFontCache(font);
		cache5 = new BitmapFontCache(font);

		cache1.setText("(cached)", 10, 320 - 66, Color.WHITE);

		String text = "Sphinx of black quartz,\njudge my vow.";
		cache2.setMultiLineText(text, 5, 320 - 300, Color.RED);

		text = "How quickly\ndaft jumping zebras vex.";
		cache3.setMultiLineText(text, 5, 320 - 200, Color.BLUE, 470, BitmapFont.HAlignment.CENTER);

		text = "Kerning: LYA moo";
		cache4.setText(text, 210, 320 - 66, Color.WHITE, 0, text.length() - 3);

		text = "Forsaking monastic tradition, twelve jovial friars gave\nup their vocation for a questionable existence on the flying trapeze.";
		cache5.setWrappedText(text, 0, 320 - 300, red, 480, HAlignment.CENTER);
	}

	@Override
	public void render () {
		alpha = (alpha + Gdx.graphics.getDeltaTime() * 0.1f) % 1;

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
		String text = "Forsaking monastic tradition, twelve jovial friars gave\nup their vocation for a questionable existence on the flying trapeze.";
		red.a = alpha;
		font.drawWrapped(spriteBatch, text, 0, 320 - 300, red, 480, HAlignment.CENTER);

		font.draw(spriteBatch, "(normal)", 10, 320 - 66, Color.WHITE);

		if (alpha > 0.6f) return;

		text = "Sphinx of black quartz,\njudge my vow.";
		font.drawMultiLine(spriteBatch, text, 5, 320 - 300, Color.RED);

		text = "How quickly\ndaft jumping zebras vex.";
		font.drawMultiLine(spriteBatch, text, 5, 320 - 200, Color.BLUE, 470, BitmapFont.HAlignment.RIGHT);

		text = "Kerning: LYA moo";
		font.draw(spriteBatch, text, 210, 320 - 66, Color.WHITE, 0, text.length() - 3);
	}

	private void renderCached () {
		red.a = alpha;
		cache5.setColor(red);
		cache5.draw(spriteBatch);

		cache1.draw(spriteBatch);

		if (alpha > 0.6f) return;

		cache2.draw(spriteBatch);
		cache3.draw(spriteBatch);
		cache4.draw(spriteBatch);
	}

	public boolean needsGL20 () {
		return false;
	}
}
