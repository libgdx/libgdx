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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.AtlasTmxMapLoader;
import com.badlogic.gdx.maps.tiled.AtlasTmxMapLoader.AtlasTiledMapLoaderParameters;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.OrthoCamController;

public class TiledMapAtlasAssetManagerTest extends GdxTest {

	private TiledMap map;
	private TiledMapRenderer renderer;
	private OrthographicCamera camera;
	private OrthoCamController cameraController;
	private AssetManager assetManager;
	private BitmapFont font;
	private SpriteBatch batch;
	String errorMessage;
	private String fileName = "data/maps/tiled-atlas-processed/test.tmx";

	@Override
	public void create () {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, (w / h) * 10, 10);
		camera.zoom = 2;
		camera.update();

		cameraController = new OrthoCamController(camera);
		Gdx.input.setInputProcessor(cameraController);

		font = new BitmapFont();
		batch = new SpriteBatch();

		AtlasTiledMapLoaderParameters params = new AtlasTiledMapLoaderParameters();
		params.forceTextureFilters = true;
		params.textureMinFilter = TextureFilter.Linear;
		params.textureMagFilter = TextureFilter.Linear;

		assetManager = new AssetManager();
		assetManager.setErrorListener(new AssetErrorListener() {
			@Override
			public void error (AssetDescriptor asset, Throwable throwable) {
				errorMessage = throwable.getMessage();
			}
		});

		assetManager.setLoader(TiledMap.class, new AtlasTmxMapLoader(new InternalFileHandleResolver()));
		assetManager.load(fileName, TiledMap.class);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(100f / 255f, 100f / 255f, 250f / 255f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		assetManager.update();
		if (renderer == null && assetManager.isLoaded(fileName)) {
			map = assetManager.get(fileName);
			renderer = new OrthogonalTiledMapRenderer(map, 1f / 32f);
		} else if (renderer != null) {
			renderer.setView(camera);
			renderer.render();
		}
		batch.begin();
		if (errorMessage != null) font.draw(batch, "ERROR (OK if running in GWT): " + errorMessage, 10, 50);
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
		batch.end();
	}
}
