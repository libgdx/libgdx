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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.tests.utils.GdxTest;

public class AssetManagerTest extends GdxTest implements AssetErrorListener {
	@Override
	public boolean needsGL20 () {
		return false;
	}

	AssetManager manager;
	BitmapFont font;
	SpriteBatch batch;
	int frame = 0;

	public void create () {
		Gdx.app.setLogLevel(Application.LOG_ERROR);
		manager = new AssetManager();
		manager.setErrorListener(this);
// manager.preload("data/animation.png", Texture.class);
// manager.preload("data/badlogic.jpg", Texture.class);
// manager.preload("data/cloudconnected.ogg", Music.class);
// manager.preload("data/shotgun.wav", Sound.class);
		manager.preload("data/pack1.png", Texture.class);
		manager.preload("data/pack", TextureAtlas.class);
		manager.preload("data/verdana39.png", Texture.class);
		manager.preload("data/verdana39.fnt", BitmapFont.class);
		Texture.setAssetManager(manager);
		batch = new SpriteBatch();

		font = new BitmapFont(Gdx.files.internal("data/font.fnt"), false);
	}

	boolean diagnosed = false;

	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		if (manager.update() & !diagnosed) {
			Gdx.app.log("AssetManagerTest", "\n" + manager.getDiagonistics());
			diagnosed = true;
			manager.remove("data/pack");
			manager.remove("data/verdana39.fnt");
			Gdx.app.log("AssetManagerTest", "after disposal\n" + manager.getDiagonistics());
		}
		frame++;
// if(manager.getQueuedAssets() > 0) Gdx.app.log("AssetManagerTest", "frames: " + frame + ", loaded: " + manager.getLoadedAssets()
// + ", queued: " + manager.getQueuedAssets());

		batch.begin();
		if (manager.isLoaded("data/animation.png")) batch.draw(manager.get("data/animation.png", Texture.class), 100, 100);
		if (manager.isLoaded("data/badlogic.jpg")) batch.draw(manager.get("data/badlogic.jpg", Texture.class), 200, 100);
		if (manager.isLoaded("data/pack"))
			batch.draw(manager.get("data/pack", TextureAtlas.class).findRegion("particle-star"), 164, 100);
		if (manager.isLoaded("data/verdana39.fnt"))
			manager.get("data/verdana39.fnt", BitmapFont.class).draw(batch, "This is a test", 100, 200);
		font.draw(batch, "loaded: " + manager.getProgress(), 0, 30);
		batch.end();
	}

	@Override
	public void error (String fileName, Class type, Throwable t) {
		Gdx.app.error("AssetManagerTest", "couldn't load asset '" + fileName + "'", (Exception)t);
	}
}