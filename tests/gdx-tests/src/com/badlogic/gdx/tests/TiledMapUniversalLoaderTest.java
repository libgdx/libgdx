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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.*;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.OrthoCamController;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;

public class TiledMapUniversalLoaderTest extends GdxTest {
	private final static String MAP_TMX = "data/maps/tiled-imagelayer/iso.tmx";
	private final static String MAP_TMX_2 = "data/maps/tiled-tint-opacity/ortho_w_img_layer.tmx";
	private final static String MAP_TMJ = "data/maps/tiled-json/hex_x.tmj";
	private final static String MAP_ATLAS_TMX = "data/maps/tiled-atlas-processed/test_w_imglayers_coi.tmx";
	private final static String MAP_ATLAS_TMJ = "data/maps/tiled-atlas-processed/test_w_imglayer_props.tmj";
	private final static String MAP_TMJ_2 = "data/maps/tiled-json/ortho2.tmj";
	private final static String MAP_TMX_3 = "data/maps/tiled/super-koalio/level1.tmx";

	private TiledMap map;
	private TiledMapRenderer renderer;
	private OrthographicCamera camera;
	private OrthoCamController cameraController;
	private AssetManager assetManager;
	private BitmapFont font;
	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;
	private int mapType = 0;

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
		shapeRenderer = new ShapeRenderer();

		assetManager = new AssetManager();
		assetManager.setLoader(TiledMap.class, new TiledMapLoader(new InternalFileHandleResolver()));

		assetManager.load(MAP_TMX, TiledMap.class);
		assetManager.load(MAP_TMJ, TiledMap.class);

		TmxMapLoader.Parameters mapTmxParams = new TmxMapLoader.Parameters();
		mapTmxParams.textureMinFilter = Texture.TextureFilter.Linear;
		mapTmxParams.textureMagFilter = Texture.TextureFilter.Linear;
		assetManager.load(MAP_TMX_2, TiledMap.class, mapTmxParams);

		assetManager.load(MAP_ATLAS_TMX, TiledMap.class);

		BaseTiledMapLoader.Parameters mapAtlasTmjParams = new BaseTiledMapLoader.Parameters();
		mapAtlasTmjParams.projectFilePath = "data/maps/tiled-atlas-processed/tiled-prop-test.tiled-project";
		assetManager.load(MAP_ATLAS_TMJ, TiledMap.class, mapAtlasTmjParams);

		assetManager.load(MAP_TMJ_2, TiledMap.class);

		/** This test is just to show off that passing in parameters build from the BaseTiledMapLoader.Parameters would work. Note
		 * that you can not pass in Parameters of a different subclass map loader type. e.g. No TmxMapLoader.Parameters into a .tmj
		 * map. */
		BaseTiledMapLoader.Parameters mapTmx3Params = new BaseTiledMapLoader.Parameters();
		mapTmx3Params.textureMinFilter = Texture.TextureFilter.Linear;
		mapTmx3Params.textureMagFilter = Texture.TextureFilter.Linear;
		assetManager.load(MAP_TMX_3, TiledMap.class, mapTmx3Params);

		assetManager.finishLoading();

		map = assetManager.get(MAP_TMX);
		renderer = new IsometricTiledMapRenderer(map, 1f / 64f);
	}

	@Override
	public void render () {
		if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
			if (mapType != 0) {
				if (renderer instanceof Disposable) ((Disposable)renderer).dispose();
				mapType = 0;
				map = assetManager.get(MAP_TMX);
				renderer = new IsometricTiledMapRenderer(map, 1f / 64f);
			}
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
			if (mapType != 1) {
				if (renderer instanceof Disposable) ((Disposable)renderer).dispose();
				mapType = 1;
				map = assetManager.get(MAP_TMX_2);
				renderer = new OrthogonalTiledMapRenderer(map, 1f / 32f);
			}
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
			if (mapType != 2) {
				if (renderer instanceof Disposable) ((Disposable)renderer).dispose();
				mapType = 2;
				map = assetManager.get(MAP_TMJ);
				renderer = new HexagonalTiledMapRenderer(map, 1f / 64f);
			}
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
			if (mapType != 3) {
				if (renderer instanceof Disposable) ((Disposable)renderer).dispose();
				mapType = 3;
				map = assetManager.get(MAP_ATLAS_TMX);
				renderer = new OrthogonalTiledMapRenderer(map, 1f / 32f);
			}
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
			if (mapType != 4) {
				if (renderer instanceof Disposable) ((Disposable)renderer).dispose();
				mapType = 4;
				map = assetManager.get(MAP_ATLAS_TMJ);
				renderer = new OrthogonalTiledMapRenderer(map, 1f / 16f);
			}
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) {
			if (mapType != 5) {
				if (renderer instanceof Disposable) ((Disposable)renderer).dispose();
				mapType = 5;
				map = assetManager.get(MAP_TMJ_2);
				renderer = new OrthogonalTiledMapRenderer(map, 1f / 32f);
			}
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)) {
			if (mapType != 6) {
				if (renderer instanceof Disposable) ((Disposable)renderer).dispose();
				mapType = 6;
				map = assetManager.get(MAP_TMX_3);
				renderer = new OrthogonalTiledMapRenderer(map, 1f / 32f);
			}
		}

		ScreenUtils.clear(100f / 255f, 100f / 255f, 250f / 255f, 1f);
		camera.update();

		// add margin to view bounds so it is easy to see any issues with clipping, calculated same way as
		// BatchTiledMapRenderer#setView (OrthographicCamera)
		final float margin = 3;
		final float width = camera.viewportWidth * camera.zoom - margin * 2;
		final float height = camera.viewportHeight * camera.zoom - margin * 2;
		final float w = width * Math.abs(camera.up.y) + height * Math.abs(camera.up.x);
		final float h = height * Math.abs(camera.up.y) + width * Math.abs(camera.up.x);
		final float x = camera.position.x - w / 2;
		final float y = camera.position.y - h / 2;
		renderer.setView(camera.combined, x, y, w, h);
		renderer.render();

		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.rect(x, y, w, h);
		shapeRenderer.end();

		batch.begin();
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
		font.draw(batch, "Switch type with 1-7", Gdx.graphics.getHeight() - 100, 50);
		font.draw(batch, renderer.getClass().getSimpleName(), Gdx.graphics.getHeight() - 100, 20);
		batch.end();
	}
}
