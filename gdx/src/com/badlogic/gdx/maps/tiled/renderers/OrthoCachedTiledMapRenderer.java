/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
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

import static com.badlogic.gdx.graphics.g2d.Batch.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

/** Renders ortho tiles by caching geometry on the GPU. How much is cached is controlled by {@link #setOverCache(float)}. When the
 * view reaches the edge of the cached tiles, the cache is rebuilt at the new view position.
 * <p>
 * This class may have poor performance when tiles are often changed dynamically, since the cache must be rebuilt after each
 * change.
 * @author Justin Shapcott
 * @author Nathan Sweet */
public class OrthoCachedTiledMapRenderer implements TiledMapRenderer, Disposable {
	static private final float tolerance = 0.00001f;
	static protected final int NUM_VERTICES = 20;

	protected final TiledMap map;
	protected final SpriteCache spriteCache;

	protected final float[] vertices = new float[20];
	protected boolean blending;

	protected float unitScale;
	protected final Rectangle viewBounds = new Rectangle();
	protected final Rectangle cacheBounds = new Rectangle();

	protected float overCache = 0.50f;
	protected float maxTileWidth, maxTileHeight;
	protected boolean cached;
	protected int count;
	protected boolean canCacheMoreN, canCacheMoreE, canCacheMoreW, canCacheMoreS;

	/** Creates a renderer with a unit scale of 1 and cache size of 2000. */
	public OrthoCachedTiledMapRenderer (TiledMap map) {
		this(map, 1, 2000);
	}

	/** Creates a renderer with a cache size of 2000. */
	public OrthoCachedTiledMapRenderer (TiledMap map, float unitScale) {
		this(map, unitScale, 2000);
	}

	/** @param cacheSize The maximum number of tiles that can be cached. */
	public OrthoCachedTiledMapRenderer (TiledMap map, float unitScale, int cacheSize) {
		this.map = map;
		this.unitScale = unitScale;
		spriteCache = new SpriteCache(cacheSize, true);
	}

	@Override
	public void setView (OrthographicCamera camera) {
		spriteCache.setProjectionMatrix(camera.combined);
		float width = camera.viewportWidth * camera.zoom + maxTileWidth * 2 * unitScale;
		float height = camera.viewportHeight * camera.zoom + maxTileHeight * 2 * unitScale;
		viewBounds.set(camera.position.x - width / 2, camera.position.y - height / 2, width, height);

		if ((canCacheMoreW && viewBounds.x < cacheBounds.x - tolerance) || //
			(canCacheMoreS && viewBounds.y < cacheBounds.y - tolerance) || //
			(canCacheMoreE && viewBounds.x + viewBounds.width > cacheBounds.x + cacheBounds.width + tolerance) || //
			(canCacheMoreN && viewBounds.y + viewBounds.height > cacheBounds.y + cacheBounds.height + tolerance) //
		) cached = false;
	}

	@Override
	public void setView (Matrix4 projection, float x, float y, float width, float height) {
		spriteCache.setProjectionMatrix(projection);
		x -= maxTileWidth * unitScale;
		y -= maxTileHeight * unitScale;
		width += maxTileWidth * 2 * unitScale;
		height += maxTileHeight * 2 * unitScale;
		viewBounds.set(x, y, width, height);

		if ((canCacheMoreW && viewBounds.x < cacheBounds.x - tolerance) || //
			(canCacheMoreS && viewBounds.y < cacheBounds.y - tolerance) || //
			(canCacheMoreE && viewBounds.x + viewBounds.width > cacheBounds.x + cacheBounds.width + tolerance) || //
			(canCacheMoreN && viewBounds.y + viewBounds.height > cacheBounds.y + cacheBounds.height + tolerance) //
		) cached = false;
	}

	@Override
	public void render () {
		if (!cached) {
			cached = true;
			count = 0;
			spriteCache.clear();

			final float extraWidth = viewBounds.width * overCache;
			final float extraHeight = viewBounds.height * overCache;
			cacheBounds.x = viewBounds.x - extraWidth;
			cacheBounds.y = viewBounds.y - extraHeight;
			cacheBounds.width = viewBounds.width + extraWidth * 2;
			cacheBounds.height = viewBounds.height + extraHeight * 2;

			for (MapLayer layer : map.getLayers()) {
				spriteCache.beginCache();
				if (layer instanceof TiledMapTileLayer) {
					renderTileLayer((TiledMapTileLayer)layer);
				} else if (layer instanceof TiledMapImageLayer) {
					renderImageLayer((TiledMapImageLayer)layer);
				}
				spriteCache.endCache();
			}
		}

		if (blending) {
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		}
		spriteCache.begin();
		MapLayers mapLayers = map.getLayers();
		for (int i = 0, j = mapLayers.getCount(); i < j; i++) {
			MapLayer layer = mapLayers.get(i);
			if (layer.isVisible()) {
				spriteCache.draw(i);
				renderObjects(layer);
			}
		}
		spriteCache.end();
		if (blending) Gdx.gl.glDisable(GL20.GL_BLEND);
	}

	@Override
	public void render (int[] layers) {
		if (!cached) {
			cached = true;
			count = 0;
			spriteCache.clear();

			final float extraWidth = viewBounds.width * overCache;
			final float extraHeight = viewBounds.height * overCache;
			cacheBounds.x = viewBounds.x - extraWidth;
			cacheBounds.y = viewBounds.y - extraHeight;
			cacheBounds.width = viewBounds.width + extraWidth * 2;
			cacheBounds.height = viewBounds.height + extraHeight * 2;

			for (MapLayer layer : map.getLayers()) {
				spriteCache.beginCache();
				if (layer instanceof TiledMapTileLayer) {
					renderTileLayer((TiledMapTileLayer)layer);
				} else if (layer instanceof TiledMapImageLayer) {
					renderImageLayer((TiledMapImageLayer)layer);
				}
				spriteCache.endCache();
			}
		}

		if (blending) {
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		}
		spriteCache.begin();
		MapLayers mapLayers = map.getLayers();
		for (int i : layers) {
			MapLayer layer = mapLayers.get(i);
			if (layer.isVisible()) {
				spriteCache.draw(i);
				renderObjects(layer);
			}
		}
		spriteCache.end();
		if (blending) Gdx.gl.glDisable(GL20.GL_BLEND);
	}

	@Override
	public void renderObjects (MapLayer layer) {
		for (MapObject object : layer.getObjects()) {
			renderObject(object);
		}
	}

	@Override
	public void renderObject (MapObject object) {
	}

	@Override
	public void renderTileLayer (TiledMapTileLayer layer) {
		final float color = Color.toFloatBits(1, 1, 1, layer.getOpacity());

		final int layerWidth = layer.getWidth();
		final int layerHeight = layer.getHeight();

		final float layerTileWidth = layer.getTileWidth() * unitScale;
		final float layerTileHeight = layer.getTileHeight() * unitScale;

		final float layerOffsetX = layer.getRenderOffsetX() * unitScale - viewBounds.x * (layer.getParallaxX() - 1);
		// offset in tiled is y down, so we flip it
		final float layerOffsetY = -layer.getRenderOffsetY() * unitScale - viewBounds.y * (layer.getParallaxY() - 1);

		final int col1 = Math.max(0, (int)((cacheBounds.x - layerOffsetX) / layerTileWidth));
		final int col2 = Math.min(layerWidth,
			(int)((cacheBounds.x + cacheBounds.width + layerTileWidth - layerOffsetX) / layerTileWidth));

		final int row1 = Math.max(0, (int)((cacheBounds.y - layerOffsetY) / layerTileHeight));
		final int row2 = Math.min(layerHeight,
			(int)((cacheBounds.y + cacheBounds.height + layerTileHeight - layerOffsetY) / layerTileHeight));

		canCacheMoreN = row2 < layerHeight;
		canCacheMoreE = col2 < layerWidth;
		canCacheMoreW = col1 > 0;
		canCacheMoreS = row1 > 0;

		float[] vertices = this.vertices;
		for (int row = row2; row >= row1; row--) {
			for (int col = col1; col < col2; col++) {
				final TiledMapTileLayer.Cell cell = layer.getCell(col, row);
				if (cell == null) continue;

				final TiledMapTile tile = cell.getTile();
				if (tile == null) continue;

				count++;
				final boolean flipX = cell.getFlipHorizontally();
				final boolean flipY = cell.getFlipVertically();
				final int rotations = cell.getRotation();

				final TextureRegion region = tile.getTextureRegion();
				final Texture texture = region.getTexture();

				final float x1 = col * layerTileWidth + tile.getOffsetX() * unitScale + layerOffsetX;
				final float y1 = row * layerTileHeight + tile.getOffsetY() * unitScale + layerOffsetY;
				final float x2 = x1 + region.getRegionWidth() * unitScale;
				final float y2 = y1 + region.getRegionHeight() * unitScale;

				final float adjustX = 0.5f / texture.getWidth();
				final float adjustY = 0.5f / texture.getHeight();
				final float u1 = region.getU() + adjustX;
				final float v1 = region.getV2() - adjustY;
				final float u2 = region.getU2() - adjustX;
				final float v2 = region.getV() + adjustY;

				vertices[X1] = x1;
				vertices[Y1] = y1;
				vertices[C1] = color;
				vertices[U1] = u1;
				vertices[V1] = v1;

				vertices[X2] = x1;
				vertices[Y2] = y2;
				vertices[C2] = color;
				vertices[U2] = u1;
				vertices[V2] = v2;

				vertices[X3] = x2;
				vertices[Y3] = y2;
				vertices[C3] = color;
				vertices[U3] = u2;
				vertices[V3] = v2;

				vertices[X4] = x2;
				vertices[Y4] = y1;
				vertices[C4] = color;
				vertices[U4] = u2;
				vertices[V4] = v1;

				if (flipX) {
					float temp = vertices[U1];
					vertices[U1] = vertices[U3];
					vertices[U3] = temp;
					temp = vertices[U2];
					vertices[U2] = vertices[U4];
					vertices[U4] = temp;
				}
				if (flipY) {
					float temp = vertices[V1];
					vertices[V1] = vertices[V3];
					vertices[V3] = temp;
					temp = vertices[V2];
					vertices[V2] = vertices[V4];
					vertices[V4] = temp;
				}
				if (rotations != 0) {
					switch (rotations) {
					case Cell.ROTATE_90: {
						float tempV = vertices[V1];
						vertices[V1] = vertices[V2];
						vertices[V2] = vertices[V3];
						vertices[V3] = vertices[V4];
						vertices[V4] = tempV;

						float tempU = vertices[U1];
						vertices[U1] = vertices[U2];
						vertices[U2] = vertices[U3];
						vertices[U3] = vertices[U4];
						vertices[U4] = tempU;
						break;
					}
					case Cell.ROTATE_180: {
						float tempU = vertices[U1];
						vertices[U1] = vertices[U3];
						vertices[U3] = tempU;
						tempU = vertices[U2];
						vertices[U2] = vertices[U4];
						vertices[U4] = tempU;
						float tempV = vertices[V1];
						vertices[V1] = vertices[V3];
						vertices[V3] = tempV;
						tempV = vertices[V2];
						vertices[V2] = vertices[V4];
						vertices[V4] = tempV;
						break;
					}
					case Cell.ROTATE_270: {
						float tempV = vertices[V1];
						vertices[V1] = vertices[V4];
						vertices[V4] = vertices[V3];
						vertices[V3] = vertices[V2];
						vertices[V2] = tempV;

						float tempU = vertices[U1];
						vertices[U1] = vertices[U4];
						vertices[U4] = vertices[U3];
						vertices[U3] = vertices[U2];
						vertices[U2] = tempU;
						break;
					}
					}
				}
				spriteCache.add(texture, vertices, 0, NUM_VERTICES);
			}
		}
	}

	protected Rectangle imageBounds = new Rectangle();

	@Override
	public void renderImageLayer (TiledMapImageLayer layer) {
		final float color = Color.toFloatBits(1.0f, 1.0f, 1.0f, layer.getOpacity());
		final float[] vertices = this.vertices;

		TextureRegion region = layer.getTextureRegion();

		if (region == null) {
			return;
		}

		final float x = layer.getX();
		final float y = layer.getY();
		final float x1 = x * unitScale - viewBounds.x * (layer.getParallaxX() - 1);
		final float y1 = y * unitScale - viewBounds.y * (layer.getParallaxY() - 1);
		final float x2 = x1 + region.getRegionWidth() * unitScale;
		final float y2 = y1 + region.getRegionHeight() * unitScale;

		imageBounds.set(x1, y1, x2 - x1, y2 - y1);
		if (!layer.isRepeatX() && !layer.isRepeatY()) {

			final float u1 = region.getU();
			final float v1 = region.getV2();
			final float u2 = region.getU2();
			final float v2 = region.getV();

			vertices[X1] = x1;
			vertices[Y1] = y1;
			vertices[C1] = color;
			vertices[U1] = u1;
			vertices[V1] = v1;

			vertices[X2] = x1;
			vertices[Y2] = y2;
			vertices[C2] = color;
			vertices[U2] = u1;
			vertices[V2] = v2;

			vertices[X3] = x2;
			vertices[Y3] = y2;
			vertices[C3] = color;
			vertices[U3] = u2;
			vertices[V3] = v2;

			vertices[X4] = x2;
			vertices[Y4] = y1;
			vertices[C4] = color;
			vertices[U4] = u2;
			vertices[V4] = v1;

			spriteCache.add(region.getTexture(), vertices, 0, NUM_VERTICES);

		} else {

			// Determine number of times to repeat image across X and Y, + 4 for padding to avoid pop in/out
			int repeatX = layer.isRepeatX() ? (int)Math.ceil((cacheBounds.width / imageBounds.width) + 4) : 0;
			int repeatY = layer.isRepeatY() ? (int)Math.ceil((cacheBounds.height / imageBounds.height) + 4) : 0;

			// Calculate the offset of the first image to align with the camera
			float startX = cacheBounds.x;
			float startY = cacheBounds.y;
			startX = startX - (startX % imageBounds.width);
			startY = startY - (startY % imageBounds.height);

			for (int i = 0; i <= repeatX; i++) {
				for (int j = 0; j <= repeatY; j++) {
					float rx1 = x1;
					float ry1 = y1;
					float rx2 = x2;
					float ry2 = y2;

					// Use (i -2)/(j-2) to begin placing our repeating images outside the camera.
					// In case the image is offset, we must negate this using + (x1% imageBounds.width)
					// It's a way to get the remainder of how many images would fit between its starting position and 0
					if (layer.isRepeatX()) {
						rx1 = startX + ((i - 2) * imageBounds.width) + (x1 % imageBounds.width);
						rx2 = rx1 + imageBounds.width;
					}

					if (layer.isRepeatY()) {
						ry1 = startY + ((j - 2) * imageBounds.height) + (y1 % imageBounds.height);
						ry2 = ry1 + imageBounds.height;
					}

					final float ru1 = region.getU();
					final float rv1 = region.getV2();
					final float ru2 = region.getU2();
					final float rv2 = region.getV();

					vertices[X1] = rx1;
					vertices[Y1] = ry1;
					vertices[C1] = color;
					vertices[U1] = ru1;
					vertices[V1] = rv1;

					vertices[X2] = rx1;
					vertices[Y2] = ry2;
					vertices[C2] = color;
					vertices[U2] = ru1;
					vertices[V2] = rv2;

					vertices[X3] = rx2;
					vertices[Y3] = ry2;
					vertices[C3] = color;
					vertices[U3] = ru2;
					vertices[V3] = rv2;

					vertices[X4] = rx2;
					vertices[Y4] = ry1;
					vertices[C4] = color;
					vertices[U4] = ru2;
					vertices[V4] = rv1;

					spriteCache.add(region.getTexture(), vertices, 0, NUM_VERTICES);
				}
			}
		}
	}

	/** Causes the cache to be rebuilt the next time it is rendered. */
	public void invalidateCache () {
		cached = false;
	}

	/** Returns true if tiles are currently cached. */
	public boolean isCached () {
		return cached;
	}

	/** Sets the percentage of the view that is cached in each direction. Default is 0.5.
	 * <p>
	 * Eg, 0.75 will cache 75% of the width of the view to the left and right of the view, and 75% of the height of the view above
	 * and below the view. */
	public void setOverCache (float overCache) {
		this.overCache = overCache;
	}

	/** Expands the view size in each direction, ensuring that tiles of this size or smaller are never culled from the visible
	 * portion of the view. Default is 0,0.
	 * <p>
	 * The amount of tiles cached is computed using <code>(view size + max tile size) * overCache</code>, meaning the max tile size
	 * increases the amount cached and possibly {@link #setOverCache(float)} can be reduced.
	 * <p>
	 * If the view size and {@link #setOverCache(float)} are configured so the size of the cached tiles is always larger than the
	 * largest tile size, this setting is not needed. */
	public void setMaxTileSize (float maxPixelWidth, float maxPixelHeight) {
		this.maxTileWidth = maxPixelWidth;
		this.maxTileHeight = maxPixelHeight;
	}

	public void setBlending (boolean blending) {
		this.blending = blending;
	}

	public SpriteCache getSpriteCache () {
		return spriteCache;
	}

	@Override
	public void dispose () {
		spriteCache.dispose();
	}
}
