/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
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

package com.badlogic.gdx.tiledmappacker;

import java.io.File;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.AtlasTmjMapLoader;
import com.badlogic.gdx.maps.tiled.AtlasTmxMapLoader;
import com.badlogic.gdx.maps.tiled.BaseTiledMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Renders and, optionally, deletes maps processed by TiledMapPackerTest. Run TiledMapPackerTest before running this */
public class TiledMapPackerTestRender extends ApplicationAdapter {

	// --WARNING!--
	// Please do not edit the MAP_PATH. This deletes the folder recursively and could be very dangerous. The default location is:
	// MAP_PATH = "data/maps/tiled-atlas-processed/deleteMe/";

	private final boolean DELETE_DELETEME_FOLDER_ON_EXIT = false; // read warning before setting to true
	private final static String MAP_PATH = "data/maps/tiled-atlas-processed/deleteMe/";

	/** Choose which processed map you want to load. DEFAULT_TMX_MAP: The original default test map.
	 *
	 * DEFAULT_TMJ_MAP: The original default test map in the .tmj format.
	 *
	 * DEFAULT_TMX_IMGLAYER_MAP: A Test map which also loads image layers.
	 *
	 * DEFAULT_TMX_IMGLAYERS_COLLECTION_TILESET: The DEFAULT_TMX_IMGLAYER_MAP but also uses tileset made up of a collection of
	 * images as well as a normal tilesheet tileset.
	 *
	 * DEFAULT_TMJ_IMGLAYER_WITH_PROPS_MAP: A Test Map in the .tmj format, with an image layer and also supports custom class via a
	 * project file.
	 *
	 * *NOTE the DEFAULT_TMJ_IMGLAYER_WITH_PROPS_MAP map will only show up in deleteMe folder if the TiledMapPackerTest is run with
	 * TestType testType = TestType.DefaultUsageWithProjectFile; */

	private final TestMapType TEST_MAP_TYPE = TestMapType.DEFAULT_TMX_MAP;

	/** Project file won't exist in the deleteMe folder. We must use the one in tiled-atlas-processed folder that will always be
	 * there */
	private final static String PROJECT_FILE_PATH = "data/maps/tiled-atlas-processed/tiled-prop-test.tiled-project";

	private final boolean CENTER_CAM = true;
	private final float WORLD_WIDTH = 16;
	private final float WORLD_HEIGHT = 8;
	private final float PIXELS_PER_METER = 32;
	private final float UNIT_SCALE = 1f / PIXELS_PER_METER;
	private BaseTiledMapLoader.Parameters params;
	private AtlasTmxMapLoader atlasTmxMapLoader;
	private BaseTiledMapLoader.Parameters paramsTmj;
	private AtlasTmjMapLoader atlasTmjMapLoader;
	private TiledMap map;
	private Viewport viewport;
	private OrthogonalTiledMapRenderer mapRenderer;
	private OrthographicCamera cam;

	public enum TestMapType {
		DEFAULT_TMX_MAP, DEFAULT_TMJ_MAP, DEFAULT_TMX_IMGLAYER_MAP, DEFAULT_TMJ_IMGLAYER_WITH_PROPS_MAP, DEFAULT_TMX_IMGLAYERS_COLLECTION_TILESET;
	}

	@Override
	public void create () {
		String mapLocation = "";
		switch (TEST_MAP_TYPE) {
		case DEFAULT_TMX_MAP:
			atlasTmxMapLoader = new AtlasTmxMapLoader(new InternalFileHandleResolver());
			params = new BaseTiledMapLoader.Parameters();
			params.generateMipMaps = false;
			params.convertObjectToTileSpace = false;
			params.flipY = true;
			params.projectFilePath = "";

			mapLocation = MAP_PATH + "test.tmx";
			map = atlasTmxMapLoader.load(mapLocation, params);
			break;
		case DEFAULT_TMJ_MAP:
			atlasTmjMapLoader = new AtlasTmjMapLoader(new InternalFileHandleResolver());
			paramsTmj = new BaseTiledMapLoader.Parameters();
			paramsTmj.generateMipMaps = false;
			paramsTmj.convertObjectToTileSpace = false;
			paramsTmj.flipY = true;
			paramsTmj.projectFilePath = "";

			mapLocation = MAP_PATH + "test.tmj";
			map = atlasTmjMapLoader.load(mapLocation, paramsTmj);
			break;
		case DEFAULT_TMX_IMGLAYER_MAP:
			atlasTmxMapLoader = new AtlasTmxMapLoader(new InternalFileHandleResolver());
			params = new BaseTiledMapLoader.Parameters();
			params.generateMipMaps = false;
			params.convertObjectToTileSpace = false;
			params.flipY = true;
			params.projectFilePath = "";

			mapLocation = MAP_PATH + "test_w_imglayers.tmx";
			map = atlasTmxMapLoader.load(mapLocation, params);
			break;
		case DEFAULT_TMX_IMGLAYERS_COLLECTION_TILESET:
			atlasTmxMapLoader = new AtlasTmxMapLoader(new InternalFileHandleResolver());
			params = new BaseTiledMapLoader.Parameters();
			params.generateMipMaps = false;
			params.convertObjectToTileSpace = false;
			params.flipY = true;
			params.projectFilePath = "";

			mapLocation = MAP_PATH + "test_w_imglayers_coi.tmx";
			map = atlasTmxMapLoader.load(mapLocation, params);
			break;
		case DEFAULT_TMJ_IMGLAYER_WITH_PROPS_MAP:
			atlasTmjMapLoader = new AtlasTmjMapLoader(new InternalFileHandleResolver());
			paramsTmj = new BaseTiledMapLoader.Parameters();
			paramsTmj.generateMipMaps = false;
			paramsTmj.convertObjectToTileSpace = false;
			paramsTmj.flipY = true;
			paramsTmj.projectFilePath = PROJECT_FILE_PATH;

			mapLocation = MAP_PATH + "test_w_imglayer_props.tmj";
			map = atlasTmjMapLoader.load(mapLocation, paramsTmj);
			break;
		}

		viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
		cam = (OrthographicCamera)viewport.getCamera();

		mapRenderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		viewport.apply();
		mapRenderer.setView(cam);
		mapRenderer.render();

		if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			if (DELETE_DELETEME_FOLDER_ON_EXIT) {
				FileHandle deleteMeHandle = Gdx.files.local(MAP_PATH);
				deleteMeHandle.deleteDirectory();
			}

			dispose();
			Gdx.app.exit();
		}
	}

	@Override
	public void resize (int width, int height) {
		viewport.update(width, height, CENTER_CAM);
	}

	@Override
	public void dispose () {
		map.dispose();
	}

	public static void main (String[] args) throws Exception {
		File file = new File(MAP_PATH);
		if (!file.exists()) {
			System.out.println("Please run TiledMapPackerTest.");
			return;
		}
		new LwjglApplication(new TiledMapPackerTestRender(), "", 640, 480);
	}
}
