
package com.badlogic.gdx.tools.etc1;

import java.io.File;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Page;
import com.badlogic.gdx.utils.Array;

public class ETC1AtlasCompressorTest implements ApplicationListener {

	private String atlasPath;
	private FileHandle atlasFile;

	public ETC1AtlasCompressorTest (String atlasPath) {
		this.atlasPath = atlasPath;
		this.atlasFile = new FileHandle(atlasPath);
	}

	public static void main (String[] args) throws Exception {

		String applicationHomePathString = System.getProperty("user.dir");

		FileHandle homeFolder = new FileHandle(new File(applicationHomePathString));
		FileHandle spritesFolder = homeFolder.child("sprites");
		FileHandle atlasFolder = homeFolder.child("atlas");

		String outputAtlasFilename = "atlas_test.atlas";
		String pngInputDir = spritesFolder.path();
		String atlasOutputDir = atlasFolder.path();

		prepareTestAtlas(pngInputDir, atlasOutputDir, outputAtlasFilename);

		String atlasFilePathString = atlasFolder.child(outputAtlasFilename).path();

		boolean COMPRESS = true;

		if (COMPRESS) {
			ETC1AtlasCompressorSettings settings = ETC1AtlasCompressor.newCompressionSettings();
			settings.setAtlasFilePathString(atlasFilePathString);
			ETC1AtlasCompressionResult compressionResult = ETC1AtlasCompressor.compress(settings);
			log();
			compressionResult.print();
		}

		log("Showing compressed sprites");
		new LwjglApplication(new ETC1AtlasCompressorTest(atlasFilePathString), "", 768, 768 * 3 / 4);

	}

	private static void prepareTestAtlas (String pngInputDir, String atlasOutputDir, String outputAtlasFilename) {
		log("pngInputDir", pngInputDir);
		log("atlasOutputDir", atlasOutputDir);
		log("outputAtlasFilename", outputAtlasFilename);

		TexturePacker.Settings atlasSettings = new TexturePacker.Settings();
		atlasSettings.debug = true;
		atlasSettings.maxWidth = 512;
		atlasSettings.maxHeight = 256;
		atlasSettings.format = Format.RGBA8888;

		TexturePacker.process(atlasSettings, pngInputDir, atlasOutputDir, outputAtlasFilename);
	}

	private static void log (String tag, Object message) {
		ETC1AtlasCompressor.log(tag, message);
	}

	private static void log (Object message) {
		ETC1AtlasCompressor.log(message);
	}

	private static void log () {
		ETC1AtlasCompressor.log();
	}

	/// -------------------------------------------------------------------------------------------------

	SpriteBatch batch;
	Array<Page> pages;
	private TextureAtlas atlas;
	private Array<Sprite> sprites;

	public void create () {
		batch = new SpriteBatch();

		atlas = new TextureAtlas(this.atlasFile);
		sprites = atlas.createSprites();
		float x = 0;
		for (int i = 0; i < sprites.size; i++) {
			Sprite sprite = sprites.get(i);
			sprite.setX(x);
			x = x + sprite.getWidth() * 0.9f;
		}

	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		int x = 20, y = 20;
		batch.begin();
		for (Sprite sprite : sprites) {
			sprite.draw(batch);
		}
		batch.end();
	}

	public void resize (int width, int height) {
		batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void dispose () {
	}

}
