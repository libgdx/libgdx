/*******************************************************************************
 * Copyright 2013 See AUTHORS file.
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

package com.badlogic.gdx.maps.tiled.renderers;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

public abstract class BatchTiledMapRenderer implements TiledMapRenderer, Disposable {
	protected TiledMap map;

	protected float unitScale;

	protected Batch spriteBatch;

	protected Rectangle viewBounds;

	protected boolean ownsSpriteBatch;

	protected float vertices[] = new float[20];

	public TiledMap getMap () {
		return map;
	}

	public void setMap (TiledMap map) {
		this.map = map;
	}

	public float getUnitScale () {
		return unitScale;
	}

	public Batch getSpriteBatch () {
		return spriteBatch;
	}

	public Rectangle getViewBounds () {
		return viewBounds;
	}

	public BatchTiledMapRenderer (TiledMap map) {
		this(map, 1.0f);
	}

	public BatchTiledMapRenderer (TiledMap map, float unitScale) {
		this.map = map;
		this.unitScale = unitScale;
		this.viewBounds = new Rectangle();
		this.spriteBatch = new SpriteBatch();
		this.ownsSpriteBatch = true;
	}

	public BatchTiledMapRenderer (TiledMap map, Batch batch) {
		this(map, 1.0f, batch);
	}

	public BatchTiledMapRenderer (TiledMap map, float unitScale, Batch batch) {
		this.map = map;
		this.unitScale = unitScale;
		this.viewBounds = new Rectangle();
		this.spriteBatch = batch;
		this.ownsSpriteBatch = false;
	}

	@Override
	public void setView (OrthographicCamera camera) {
		spriteBatch.setProjectionMatrix(camera.combined);
		float width = camera.viewportWidth * camera.zoom;
		float height = camera.viewportHeight * camera.zoom;
		viewBounds.set(camera.position.x - width / 2, camera.position.y - height / 2, width, height);
	}

	@Override
	public void setView (Matrix4 projection, float x, float y, float width, float height) {
		spriteBatch.setProjectionMatrix(projection);
		viewBounds.set(x, y, width, height);
	}

	@Override
	public void render () {
		beginRender();
		for (MapLayer layer : map.getLayers()) {
			if (layer.isVisible()) {
				if (layer instanceof TiledMapTileLayer) {
					renderTileLayer((TiledMapTileLayer)layer);
				} else {
					for (MapObject object : layer.getObjects()) {
						renderObject(object);
					}
				}
			}
		}
		endRender();
	}

	@Override
	public void render (int[] layers) {
		beginRender();
		for (int layerIdx : layers) {
			MapLayer layer = map.getLayers().get(layerIdx);
			if (layer.isVisible()) {
				if (layer instanceof TiledMapTileLayer) {
					renderTileLayer((TiledMapTileLayer)layer);
				} else {
					for (MapObject object : layer.getObjects()) {
						renderObject(object);
					}
				}
			}
		}
		endRender();
	}

	/** Called before the rendering of all layers starts. */
	protected void beginRender () {
		AnimatedTiledMapTile.updateAnimationBaseTime();
		spriteBatch.begin();
	}

	/** Called after the rendering of all layers ended. */
	protected void endRender () {
		spriteBatch.end();
	}

	@Override
	public void dispose () {
		if (ownsSpriteBatch) {
			spriteBatch.dispose();
		}
	}
}
