
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader.BitmapFontParameter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;

public class BitmapFontAtlasRegionTest extends GdxTest {
	private SpriteBatch batch;
	private AssetManager assets;

	private BitmapFont[] fonts;
	private String[] testStrings;

	private static final String FONT_1 = "data/default.fnt";
	private static final String FONT_2 = "data/font.fnt";
	private static final String FONT_3 = "data/verdana39.fnt";
	private static final String ATLAS = "data/atlased-fonts.txt";

	@Override
	public void create () {
		this.batch = new SpriteBatch();
		this.assets = new AssetManager();

		BitmapFontParameter params = new BitmapFontParameter();
		params.atlasName = ATLAS;

		this.assets.load(FONT_1, BitmapFont.class, params);
		this.assets.load(FONT_2, BitmapFont.class, params);
		this.assets.load(FONT_3, BitmapFont.class, params);
		this.assets.finishLoading();

		this.fonts = new BitmapFont[3];
		this.fonts[0] = assets.get(FONT_1);
		this.fonts[1] = assets.get(FONT_2);
		this.fonts[2] = assets.get(FONT_3);

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
		Array<String> loaded = this.assets.getAssetNames();
		
		this.assets.dispose();
		this.batch.dispose();

		String name = ClassReflection.getSimpleName(this.getClass());
		for (int i = 0; i < loaded.size; ++i) {
			String asset = loaded.get(i);
			if (this.assets.isLoaded(asset)) {
				Gdx.app.error(name, asset + " not properly disposed of!");
			} else {
				Gdx.app.log(name, asset + " disposed of OK");
			}
		}
	}
}
