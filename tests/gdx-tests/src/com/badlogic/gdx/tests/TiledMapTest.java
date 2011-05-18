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

import java.util.StringTokenizer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.tiled.TileAtlas;
import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader;
import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
import com.badlogic.gdx.graphics.g2d.tiled.TileMapRenderer;
import com.badlogic.gdx.graphics.g2d.tiled.TiledObject;
import com.badlogic.gdx.graphics.g2d.tiled.TiledObjectGroup;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.OrthoCamController;
import com.badlogic.gdx.utils.IntArray;

/**
 * @author David Fraska
 * */
public class TiledMapTest extends GdxTest {

	private static final boolean automove = false;

	private static final int SCREEN_WIDTH = 480;
	private static final int SCREEN_HEIGHT = 320;

	private static final int[] layersList = {2,3};

	SpriteBatch spriteBatch;
	BitmapFont font;

	OrthographicCamera cam;
	OrthoCamController camController;
	Vector3 camDirection = new Vector3(1, 1, 0);
	Vector2 maxCamPosition = new Vector2(0, 0);

	TileMapRenderer tileMapRenderer;
	TiledMap map;
	TileAtlas atlas;

	long startTime = System.nanoTime();
	Vector3 tmp = new Vector3();

	@Override public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		cam.update();

		if (automove) {
			updateCameraPosition();
		}

		tileMapRenderer.getProjectionMatrix().set(cam.combined);
		tmp.set(0, 0, 0);
		cam.unproject(tmp);

		tileMapRenderer.render(tmp.x, tmp.y, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());//, layersList);

		spriteBatch.begin();
		font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, 20);
		font.draw(spriteBatch, "InitialCol, LastCol: " + tileMapRenderer.getInitialCol() + "," + tileMapRenderer.getLastCol(), 20,
			40);
		font.draw(spriteBatch, "InitialRow, LastRow: " + tileMapRenderer.getInitialRow() + "," + tileMapRenderer.getLastRow(), 20,
			60);
		font.draw(spriteBatch, "Location: " + (int)tmp.x + "," + (map.height * map.tileHeight - (int)tmp.y), 20, 80);
		spriteBatch.end();
	}

	private void updateCameraPosition () {
		cam.position.add(camDirection.tmp().mul(Gdx.graphics.getDeltaTime()).mul(60));

		if (cam.position.x < 0) {
			cam.position.x = 0;
			camDirection.x = 1;
		}
		if (cam.position.x > maxCamPosition.x) {
			cam.position.x = maxCamPosition.x;
			camDirection.x = -1;
		}
		if (cam.position.y < 0) {
			cam.position.y = 0;
			camDirection.y = 1;
		}
		if (cam.position.y > maxCamPosition.y) {
			cam.position.y = maxCamPosition.y;
			camDirection.y = -1;
		}
	}

	@Override public void create () {
		int i;
		long startTime, endTime;
		font = new BitmapFont();
		font.setColor(Color.RED);

		spriteBatch = new SpriteBatch();

		final String path = "data/tiledmap/";
		final String mapname = "tilemap csv";

		FileHandle mapHandle = Gdx.files.internal(path + mapname + ".tmx");
		FileHandle baseDir = Gdx.files.internal(path);

		startTime = System.currentTimeMillis();
		map = TiledLoader.createMap(mapHandle);
		endTime = System.currentTimeMillis();
		System.out.println("Loaded map in " + (endTime - startTime) + "mS");

		atlas = new TileAtlas(map, baseDir);

		int blockWidth = 10;
		int blockHeight = 12;

		startTime = System.currentTimeMillis();

		tileMapRenderer = new TileMapRenderer(map, atlas, blockWidth, blockHeight);
		endTime = System.currentTimeMillis();
		System.out.println("Created cache in " + (endTime - startTime) + "mS");

		for (TiledObjectGroup group : map.objectGroups) {
			for (TiledObject object : group.objects) {
				// TODO: Draw sprites where objects occur
				System.out.println("Object " + object.name + " x,y = " + object.x + "," + object.y + " width,height = "
					+ object.width + "," + object.height);
			}
		}

		cam = new OrthographicCamera(SCREEN_WIDTH, SCREEN_HEIGHT);
		cam.position.set(map.width * map.tileWidth / 2, map.height * map.tileHeight / 2, 0);
		camController = new OrthoCamController(cam);
		Gdx.input.setInputProcessor(camController);

		float maxX = map.width * map.tileWidth;
		float maxY = map.height * map.tileHeight;
		maxCamPosition.set(maxX, maxY);
	}

	@Override public boolean needsGL20 () {
		return false;
	}
}
