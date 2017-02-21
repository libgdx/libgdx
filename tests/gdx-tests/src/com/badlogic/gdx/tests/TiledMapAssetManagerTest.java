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
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.OrthoCamController;

public class TiledMapAssetManagerTest extends GdxTest {

	private static final String MAP_PROPERTY_NAME = "mapCustomProperty";
	private static final String BOOL_PROPERTY_NAME = "boolCustomProperty";
	private static final String INT_PROPERTY_NAME = "intCustomProperty";
	private static final String FLOAT_PROPERTY_NAME = "floatCustomProperty";

	private static final String TILESET_PROPERTY_NAME = "tilesetCustomProperty";
	private static final String TILE_PROPERTY_NAME = "tileCustomProperty";
	private static final String LAYER_PROPERTY_NAME = "layerCustomProperty";

	private static final String MAP_PROPERTY_VALUE = "mapCustomValue";
	private static final boolean BOOL_PROPERTY_VALUE = true;
	private static final int INT_PROPERTY_VALUE = 5;
	private static final float FLOAT_PROPERTY_VALUE = 1.56f;

	private static final String TILESET_PROPERTY_VALUE = "tilesetCustomValue";
	private static final String TILE_PROPERTY_VALUE = "tileCustomValue";
	private static final String LAYER_PROPERTY_VALUE = "layerCustomValue";

	private TiledMap map;
	private TiledMapRenderer renderer;
	private OrthographicCamera camera;
	private OrthoCamController cameraController;
	private AssetManager assetManager;
	private BitmapFont font;
	private SpriteBatch batch;

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

		assetManager = new AssetManager();
		assetManager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
		assetManager.load("data/maps/tiled/isometric_grass_and_water.tmx", TiledMap.class);
		assetManager.finishLoading();
		map = assetManager.get("data/maps/tiled/isometric_grass_and_water.tmx");
		renderer = new IsometricTiledMapRenderer(map, 1f / 64f);

		String mapCustomValue = map.getProperties().get(MAP_PROPERTY_NAME, String.class);
		Gdx.app.log("TiledMapAssetManagerTest", "Property : " + MAP_PROPERTY_NAME + ", Value : " + mapCustomValue);
		if (!MAP_PROPERTY_VALUE.equals(mapCustomValue)) {
			throw new RuntimeException("Failed to get map properties");
		}

		boolean boolCustomValue = map.getProperties().get(BOOL_PROPERTY_NAME, Boolean.class);
		Gdx.app.log("TiledMapAssetManagerTest", "Property : " + BOOL_PROPERTY_NAME + ", Value : " + boolCustomValue);
		if (boolCustomValue != BOOL_PROPERTY_VALUE) {
			throw new RuntimeException("Failed to get boolean map properties");
		}

		int intCustomValue = map.getProperties().get(INT_PROPERTY_NAME, Integer.class);
		Gdx.app.log("TiledMapAssetManagerTest", "Property : " + INT_PROPERTY_NAME + ", Value : " + intCustomValue);
		if (intCustomValue != INT_PROPERTY_VALUE) {
			throw new RuntimeException("Failed to get int map properties");
		}

		float floatCustomValue = map.getProperties().get(FLOAT_PROPERTY_NAME, Float.class);
		Gdx.app.log("TiledMapAssetManagerTest", "Property : " + FLOAT_PROPERTY_NAME + ", Value : " + floatCustomValue);
		if (floatCustomValue != FLOAT_PROPERTY_VALUE) {
			throw new RuntimeException("Failed to get float map properties");
		}

		TiledMapTileSet tileset = map.getTileSets().getTileSet(0);
		String tilesetCustomValue = tileset.getProperties().get(TILESET_PROPERTY_NAME, String.class);
		if (!TILESET_PROPERTY_VALUE.equals(tilesetCustomValue)) {
			throw new RuntimeException("Failed to get tileset properties");
		}

		TiledMapTile tile = tileset.getTile(1);
		String tileCustomValue = tile.getProperties().get(TILE_PROPERTY_NAME, String.class);
		if (!TILE_PROPERTY_VALUE.equals(tileCustomValue)) {
			throw new RuntimeException("Failed to get tile properties");
		}

		MapLayer layer = map.getLayers().get(0);
		String layerCustomValue = layer.getProperties().get(LAYER_PROPERTY_NAME, String.class);
		if (!LAYER_PROPERTY_VALUE.equals(layerCustomValue)) {
			throw new RuntimeException("Failed to get layer properties");
		}
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(100f / 255f, 100f / 255f, 250f / 255f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		renderer.setView(camera);
		renderer.render();
		batch.begin();
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
		batch.end();
	}
}
