
package com.badlogic.gdx.tests.extensions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader.FreeTypeFontLoaderParameter;
import com.badlogic.gdx.tests.utils.GdxTest;

public class FreeTypeFontLoaderTest extends GdxTest {
	AssetManager manager;
	SpriteBatch batch;

	@Override
	public void create () {
		manager = new AssetManager();

		// set the loaders for the generator and the fonts themselves
		FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

		// load to fonts via the generator (implicitely done by the FreetypeFontLoader).
		// Note: you MUST specify a FreetypeFontGenerator defining the ttf font file name and the size
		// of the font to be generated. The names of the fonts are arbitrary and are not pointing
		// to a file on disk (but must end with the font's file format '.ttf')!
		FreeTypeFontLoaderParameter size1Params = new FreeTypeFontLoaderParameter();
		size1Params.fontFileName = "data/arial.ttf";
		size1Params.fontParameters.size = 10;
		manager.load("size10.ttf", BitmapFont.class, size1Params);

		FreeTypeFontLoaderParameter size2Params = new FreeTypeFontLoaderParameter();
		size2Params.fontFileName = "data/arial.ttf";
		size2Params.fontParameters.size = 20;
		manager.load("size20.ttf", BitmapFont.class, size2Params);

		// we also load a "normal" font generated via Hiero
		manager.load("data/default.fnt", BitmapFont.class);

		batch = new SpriteBatch();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		if (manager.update() && manager.isLoaded("size10.ttf")) {
			batch.begin();
			manager.get("size10.ttf", BitmapFont.class).draw(batch, "First font!", 20, 20);
			manager.get("size20.ttf", BitmapFont.class).draw(batch, "Second font!", 20, 50);
			manager.get("data/default.fnt", BitmapFont.class).draw(batch, "Default font!", 20, 100);
			batch.end();
		}

		if (Gdx.input.justTouched() && manager.isLoaded("size10.ttf")) {
			// unload all the things and check if they really get disposed properly
			manager.unload("size10.ttf");
			manager.finishLoading();
			if (manager.isLoaded("size10.ttf")) throw new RuntimeException("broken");
			if (!manager.isLoaded("size20.ttf")) throw new RuntimeException("broken");

			manager.unload("size20.ttf");
			manager.finishLoading();
			if (manager.isLoaded("size10.ttf")) throw new RuntimeException("broken");
			if (manager.isLoaded("size20.ttf")) throw new RuntimeException("broken");
		}
	}

	@Override
	public void dispose () {
		manager.dispose();
		batch.dispose();
	}
}
