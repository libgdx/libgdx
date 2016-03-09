/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tools.etc1;

import java.io.File;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;

public class ETC1AtlasCompressorTest implements ApplicationListener {

	private String regularAtlasPath;
	private FileHandle regularAtlasPathFile;

	private String etc1AtlasPath;
	private FileHandle etc1AtlasPathFile;

	public ETC1AtlasCompressorTest (String regularAtlasPath, String etc1AtlasPath) {
		this.regularAtlasPath = regularAtlasPath;
		this.regularAtlasPathFile = new FileHandle(regularAtlasPath);

		this.etc1AtlasPath = etc1AtlasPath;
		this.etc1AtlasPathFile = new FileHandle(etc1AtlasPath);
	}

	public static void main (String[] args) throws Exception {

		String applicationHomePathString = System.getProperty("user.dir");

		FileHandle homeFolder = new FileHandle(new File(applicationHomePathString));
		FileHandle spritesFolder = homeFolder.child("sprites");
		FileHandle regularAtlasFolder = homeFolder.child("atlas");
		FileHandle etc1AtlasFolder = homeFolder.child("atlas-etc1");

		String outputAtlasFilename = "atlas_test.atlas";
		String pngInputDir = spritesFolder.path();
		String regularAtlasOutputDir = regularAtlasFolder.path();
		String etc1AtlasOutputDir = etc1AtlasFolder.path();

		prepareTestAtlas(pngInputDir, regularAtlasOutputDir, outputAtlasFilename);
		prepareTestAtlas(pngInputDir, etc1AtlasOutputDir, outputAtlasFilename);

		String regularAtlasFilePathString = regularAtlasFolder.child(outputAtlasFilename).path();
		String etc1AtlasFilePathString = etc1AtlasFolder.child(outputAtlasFilename).path();

		boolean COMPRESS = true;

		if (COMPRESS) {
			ETC1AtlasCompressorSettings settings = ETC1AtlasCompressor.newCompressionSettings();
			settings.setAtlasFilePathString(etc1AtlasFilePathString);
			Color fuxia = new com.badlogic.gdx.graphics.Color(1f, 0f, 1f, 1f);
			settings.setTransparentColor(fuxia);
			log();
			ETC1AtlasCompressionResult compressionResult = ETC1AtlasCompressor.compress(settings);
			log();
			compressionResult.print();
		}

		log("Showing compressed sprites");
		new LwjglApplication(new ETC1AtlasCompressorTest(regularAtlasFilePathString, etc1AtlasFilePathString), "", 1024, 768);

	}

	private static void prepareTestAtlas (String pngInputDir, String atlasOutputDir, String outputAtlasFilename) {
		log("pngInputDir", pngInputDir);
		log("atlasOutputDir", atlasOutputDir);
		log("outputAtlasFilename", outputAtlasFilename);

		TexturePacker.Settings atlasSettings = new TexturePacker.Settings();
		atlasSettings.debug = !true;
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

	private TextureAtlas regularAtlas;
	private Array<Sprite> regularSprites;

	private TextureAtlas etc1Atlas;
	private Array<Sprite> etc1Sprites;

	public void create () {
		batch = new SpriteBatch();

		regularAtlas = new TextureAtlas(this.regularAtlasPathFile);
		regularSprites = regularAtlas.createSprites();

		etc1Atlas = new TextureAtlas(this.etc1AtlasPathFile);
		etc1Sprites = etc1Atlas.createSprites();
		float x = 10;
		float y = 10;
		for (int i = 0; i < regularSprites.size; i++) {
			Sprite sprite = regularSprites.get(i);
			sprite.setX(x);
			sprite.setY(10);
			x = x + sprite.getWidth() * 0.9f;
			y = Math.max(y, sprite.getHeight());
		}
		x = 10;
		for (int i = 0; i < etc1Sprites.size; i++) {
			Sprite sprite = etc1Sprites.get(i);
			sprite.setX(x);
			sprite.setY(y * 1.1f);
			x = x + sprite.getWidth() * 0.9f;
		}

	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		int x = 20, y = 20;
		batch.begin();
		for (Sprite sprite : regularSprites) {
			sprite.draw(batch);
		}
		for (Sprite sprite : etc1Sprites) {
			sprite.draw(batch);
		}
		batch.end();
	}

	public void resize (int width, int height) {
		float m = 0.6f;
		batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth() * m, Gdx.graphics.getHeight() * m));
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
