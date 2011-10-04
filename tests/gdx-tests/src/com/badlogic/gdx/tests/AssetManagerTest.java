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
package com.badlogic.gdx.tests;

import java.nio.IntBuffer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.TileMapRendererLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver.Resolution;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.tiled.TileMapRenderer;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.BufferUtils;

public class AssetManagerTest extends GdxTest implements AssetErrorListener {
	@Override
	public boolean needsGL20 () {
		return true;
	}

	AssetManager manager;
	BitmapFont font;
	SpriteBatch batch;
	int frame = 0;
	int reloads = 0;

	public void create () {
		Gdx.app.setLogLevel(Application.LOG_ERROR);
		
		Resolution[] resolutions = { new Resolution(320, 480, ".320480"),
			 								  new Resolution(480, 800, ".480800"),
			 								  new Resolution(480, 856, ".480854") };
		ResolutionFileResolver resolver = new ResolutionFileResolver(new InternalFileHandleResolver(), resolutions);
		manager = new AssetManager();
		manager.setLoader(Texture.class, new TextureLoader(resolver));
		manager.setErrorListener(this);
		load();
		Texture.setAssetManager(manager);
		batch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("data/font.fnt"), false);
	}

	boolean diagnosed = false;
	
	private void load() {
		manager.load("data/animation.png", Texture.class);
		manager.load("data/pack1.png", Texture.class);
		manager.load("data/pack", TextureAtlas.class);
		manager.load("data/verdana39.png", Texture.class);
		manager.load("data/verdana39.fnt", BitmapFont.class);
		manager.load("data/test.etc1", Texture.class);
		manager.load("data/tiledmap/tilemap csv.tmx", TileMapRenderer.class, new TileMapRendererLoader.TileMapParameter("data/tiledmap/", 8, 8));
	}
	
	private void unload() {
		manager.unload("data/animation.png");
		manager.unload("data/pack1.png");
		manager.unload("data/pack");
		manager.unload("data/verdana39.png");
		manager.unload("data/verdana39.fnt");
		manager.unload("data/test.etc1");
		manager.unload("data/tiledmap/tilemap csv.tmx");
	}
	
	private void invalidateTexture(Texture texture) {
		IntBuffer buffer = BufferUtils.newIntBuffer(1);
		buffer.put(0, texture.getTextureObjectHandle());
		Gdx.gl.glDeleteTextures(1, buffer);
	}
	
	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		boolean result = manager.update();
		if (result & !diagnosed) {
			Gdx.app.log("AssetManagerTest", "\n" + manager.getDiagnostics() + "\n" + Texture.getManagedStatus());
			diagnosed = false;
//			invalidateTexture(manager.get("data/animation.png", Texture.class));
//			invalidateTexture(manager.get("data/pack1.png", Texture.class));
//			invalidateTexture(manager.get("data/verdana39.png", Texture.class));
//			invalidateTexture(manager.get("data/test.etc1", Texture.class));
//			Texture.invalidateAllTextures(Gdx.app);
//			unload();
//			load();
//			manager.finishLoading();
//			Gdx.app.log("AssetManagerTest", "after disposal\n" + manager.getDiagonistics());
			reloads++;
		}
		frame++;

		batch.begin();
		if (manager.isLoaded("data/test.etc1")) batch.draw(manager.get("data/test.etc1", Texture.class), 0, 0);
		if (manager.isLoaded("data/animation.png")) batch.draw(manager.get("data/animation.png", Texture.class), 100, 100);
		if (manager.isLoaded("data/verdana39.png")) batch.draw(manager.get("data/verdana39.png", Texture.class), 300, 100);
		if (manager.isLoaded("data/pack")) batch.draw(manager.get("data/pack", TextureAtlas.class).findRegion("particle-star"), 164, 100);
		if (manager.isLoaded("data/verdana39.fnt")) manager.get("data/verdana39.fnt", BitmapFont.class).draw(batch, "This is a test", 100, 200);
		if (manager.isLoaded("data/tiledmap/tilemap csv.tmx")) manager.get("data/tiledmap/tilemap csv.tmx", TileMapRenderer.class).render();
		font.draw(batch, "loaded: " + manager.getProgress() + ", reloads: " + reloads, 0, 30);
		batch.end();
		
		if(Gdx.input.justTouched()) {
			Texture.invalidateAllTextures(Gdx.app);
			diagnosed = false;
		}
	}

	@Override
	public void error (String fileName, Class type, Throwable t) {
		Gdx.app.error("AssetManagerTest", "couldn't load asset '" + fileName + "'", (Exception)t);
	}
}