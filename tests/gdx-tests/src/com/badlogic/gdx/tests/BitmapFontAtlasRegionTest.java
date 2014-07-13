
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader.BitmapFontParameter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.tests.utils.GdxTest;

public class BitmapFontAtlasRegionTest extends GdxTest {
	private SpriteBatch batch;
	private AssetManager assets;

	private BitmapFont[] fonts;
	private String[] testStrings;

	@Override
	public void create () {
		this.batch = new SpriteBatch();
		this.assets = new AssetManager();

		BitmapFontParameter params = new BitmapFontParameter();
		params.atlasName = "data/atlased-fonts.txt";

		this.assets.load("data/default.fnt", BitmapFont.class, params);
		this.assets.load("data/font.fnt", BitmapFont.class, params);
		this.assets.load("data/verdana39.fnt", BitmapFont.class, params);
		this.assets.finishLoading();

		this.fonts = new BitmapFont[3];
		this.fonts[0] = assets.get("data/default.fnt");
		this.fonts[1] = assets.get("data/font.fnt");
		this.fonts[2] = assets.get("data/verdana39.fnt");
		
		this.fonts[0].setColor(Color.RED);
		this.fonts[1].setColor(Color.BLUE);
		this.fonts[2].setColor(Color.GREEN);
		this.testStrings = new String[] {"I'm loaded from an atlas!", "I, too, am loaded from an atlas", "I'm with stupid ^"};

		Gdx.gl.glClearColor(1, 1, 1, 1);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();

		for (int i = 0; i < fonts.length; ++i) {
			fonts[i].draw(batch, testStrings[i], 16, 16 + 48 * i);
		}

		batch.end();
	}

	@Override
	public void dispose () {
		this.assets.dispose();
		this.batch.dispose();
	}
}
