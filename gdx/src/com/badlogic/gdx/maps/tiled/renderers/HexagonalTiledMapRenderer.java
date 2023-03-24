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

import static com.badlogic.gdx.graphics.g2d.Batch.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;

public class HexagonalTiledMapRenderer extends BatchTiledMapRenderer {

	/** true for X-Axis, false for Y-Axis */
	private boolean staggerAxisX = true;
	/** true for even StaggerIndex, false for odd */
	private boolean staggerIndexEven = false;
	/** the parameter defining the shape of the hexagon from tiled. more specifically it represents the length of the sides that
	 * are parallel to the stagger axis. e.g. with respect to the stagger axis a value of 0 results in a rhombus shape, while a
	 * value equal to the tile length/height represents a square shape and a value of 0.5 represents a regular hexagon if tile
	 * length equals tile height */
	private float hexSideLength = 0f;

	public HexagonalTiledMapRenderer (TiledMap map) {
		super(map);
		init(map);
	}

	public HexagonalTiledMapRenderer (TiledMap map, float unitScale) {
		super(map, unitScale);
		init(map);
	}

	public HexagonalTiledMapRenderer (TiledMap map, Batch batch) {
		super(map, batch);
		init(map);
	}

	public HexagonalTiledMapRenderer (TiledMap map, float unitScale, Batch batch) {
		super(map, unitScale, batch);
		init(map);
	}

	private void init (TiledMap map) {
		String axis = map.getProperties().get("staggeraxis", String.class);
		if (axis != null) {
			if (axis.equals("x")) {
				staggerAxisX = true;
			} else {
				staggerAxisX = false;
			}
		}

		String index = map.getProperties().get("staggerindex", String.class);
		if (index != null) {
			if (index.equals("even")) {
				staggerIndexEven = true;
			} else {
				staggerIndexEven = false;
			}
		}

		// due to y-axis being different we need to change stagger index in even map height situations as else it would render
		// differently.
		if (!staggerAxisX && map.getProperties().get("height", Integer.class) % 2 == 0) staggerIndexEven = !staggerIndexEven;

		Integer length = map.getProperties().get("hexsidelength", Integer.class);
		if (length != null) {
			hexSideLength = length.intValue();
		} else {
			if (staggerAxisX) {
				length = map.getProperties().get("tilewidth", Integer.class);
				if (length != null) {
					hexSideLength = 0.5f * length.intValue();
				} else {
					TiledMapTileLayer tmtl = (TiledMapTileLayer)map.getLayers().get(0);
					hexSideLength = 0.5f * tmtl.getTileWidth();
				}
			} else {
				length = map.getProperties().get("tileheight", Integer.class);
				if (length != null) {
					hexSideLength = 0.5f * length.intValue();
				} else {
					TiledMapTileLayer tmtl = (TiledMapTileLayer)map.getLayers().get(0);
					hexSideLength = 0.5f * tmtl.getTileHeight();
				}
			}
		}
	}

	@Override
	public void renderTileLayer (TiledMapTileLayer layer) {
		final Color batchColor = batch.getColor();
		final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * layer.getOpacity());

		final int layerWidth = layer.getWidth();
		final int layerHeight = layer.getHeight();

		final float layerTileWidth = layer.getTileWidth() * unitScale;
		final float layerTileHeight = layer.getTileHeight() * unitScale;

		final float layerOffsetX = layer.getRenderOffsetX() * unitScale - viewBounds.x * (layer.getParallaxX() - 1);
		// offset in tiled is y down, so we flip it
		final float layerOffsetY = -layer.getRenderOffsetY() * unitScale - viewBounds.y * (layer.getParallaxY() - 1);

		final float layerHexLength = hexSideLength * unitScale;

		if (staggerAxisX) {
			final float tileWidthLowerCorner = (layerTileWidth - layerHexLength) / 2;
			final float tileWidthUpperCorner = (layerTileWidth + layerHexLength) / 2;
			final float layerTileHeight50 = layerTileHeight * 0.50f;

			final int row1 = Math.max(0, (int)((viewBounds.y - layerTileHeight50 - layerOffsetX) / layerTileHeight));
			final int row2 = Math.min(layerHeight,
				(int)((viewBounds.y + viewBounds.height + layerTileHeight - layerOffsetX) / layerTileHeight));

			final int col1 = Math.max(0, (int)(((viewBounds.x - tileWidthLowerCorner - layerOffsetY) / tileWidthUpperCorner)));
			final int col2 = Math.min(layerWidth,
				(int)((viewBounds.x + viewBounds.width + tileWidthUpperCorner - layerOffsetY) / tileWidthUpperCorner));

			// depending on the stagger index either draw all even before the odd or vice versa
			final int colA = (staggerIndexEven == (col1 % 2 == 0)) ? col1 + 1 : col1;
			final int colB = (staggerIndexEven == (col1 % 2 == 0)) ? col1 : col1 + 1;

			for (int row = row2 - 1; row >= row1; row--) {
				for (int col = colA; col < col2; col += 2) {
					renderCell(layer.getCell(col, row), tileWidthUpperCorner * col + layerOffsetX,
						layerTileHeight50 + (layerTileHeight * row) + layerOffsetY, color);
				}
				for (int col = colB; col < col2; col += 2) {
					renderCell(layer.getCell(col, row), tileWidthUpperCorner * col + layerOffsetX,
						layerTileHeight * row + layerOffsetY, color);
				}
			}
		} else {
			final float tileHeightLowerCorner = (layerTileHeight - layerHexLength) / 2;
			final float tileHeightUpperCorner = (layerTileHeight + layerHexLength) / 2;
			final float layerTileWidth50 = layerTileWidth * 0.50f;

			final int row1 = Math.max(0, (int)(((viewBounds.y - tileHeightLowerCorner - layerOffsetX) / tileHeightUpperCorner)));
			final int row2 = Math.min(layerHeight,
				(int)((viewBounds.y + viewBounds.height + tileHeightUpperCorner - layerOffsetX) / tileHeightUpperCorner));

			final int col1 = Math.max(0, (int)(((viewBounds.x - layerTileWidth50 - layerOffsetY) / layerTileWidth)));
			final int col2 = Math.min(layerWidth,
				(int)((viewBounds.x + viewBounds.width + layerTileWidth - layerOffsetY) / layerTileWidth));

			float shiftX = 0;
			for (int row = row2 - 1; row >= row1; row--) {
				// depending on the stagger index either shift for even or uneven indexes
				if ((row % 2 == 0) == staggerIndexEven)
					shiftX = layerTileWidth50;
				else
					shiftX = 0;
				for (int col = col1; col < col2; col++) {
					renderCell(layer.getCell(col, row), layerTileWidth * col + shiftX + layerOffsetX,
						tileHeightUpperCorner * row + layerOffsetY, color);
				}
			}
		}
	}

	/** render a single cell */
	private void renderCell (final TiledMapTileLayer.Cell cell, final float x, final float y, final float color) {
		if (cell != null) {
			final TiledMapTile tile = cell.getTile();
			if (tile != null) {
				if (tile instanceof AnimatedTiledMapTile) return;

				final boolean flipX = cell.getFlipHorizontally();
				final boolean flipY = cell.getFlipVertically();
				final int rotations = cell.getRotation();

				TextureRegion region = tile.getTextureRegion();

				float x1 = x + tile.getOffsetX() * unitScale;
				float y1 = y + tile.getOffsetY() * unitScale;
				float x2 = x1 + region.getRegionWidth() * unitScale;
				float y2 = y1 + region.getRegionHeight() * unitScale;

				float u1 = region.getU();
				float v1 = region.getV2();
				float u2 = region.getU2();
				float v2 = region.getV();

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
				if (rotations == 2) {
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
				}
				batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
			}
		}
	}

	@Override
	public void renderImageLayer (TiledMapImageLayer layer) {
		final Color batchColor = batch.getColor();
		final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * layer.getOpacity());

		final float[] vertices = this.vertices;

		TextureRegion region = layer.getTextureRegion();

		if (region == null) {
			return;
		}

		int tileHeight = getMap().getProperties().get("tileheight", Integer.class);
		int mapHeight = getMap().getProperties().get("height", Integer.class);
		float layerHexLength = hexSideLength;
		// Map height if it were tiles
		float totalHeightPixels = (mapHeight * tileHeight) * unitScale;
		// To determine size of Hex map height we use (mapHeight * tileHeight(3/4)) + (layerHexLength * 0.5f)
		float hexMapHeightPixels = ((mapHeight * tileHeight * (3f / 4f)) + (layerHexLength * 0.5f)) * unitScale;

		float imageLayerYOffset = 0;
		float layerTileHeight = tileHeight * unitScale;
		float halfTileHeight = layerTileHeight * 0.5f;

		if (staggerAxisX) {
			/** If X axis staggered, must offset imagelayer y position by adding half of tileHeight to match position */
			imageLayerYOffset = halfTileHeight;
		} else {
			/** ImageLayer's y position seems to be placed at an offset determined the total height if this were a normal tile map
			 * minus the height as calculated for a hexmap. We get this number and use it to counter offset our Y. Then we will have
			 * our imagelayer matching its position in Tiled. */
			imageLayerYOffset = -(totalHeightPixels - hexMapHeightPixels);
		}

		final float x = layer.getX();
		final float y = layer.getY();
		final float x1 = x * unitScale - viewBounds.x * (layer.getParallaxX() - 1);
		final float y1 = y * unitScale - viewBounds.y * (layer.getParallaxY() - 1) + imageLayerYOffset;
		final float x2 = x1 + region.getRegionWidth() * unitScale;
		final float y2 = y1 + region.getRegionHeight() * unitScale;

		imageBounds.set(x1, y1, x2 - x1, y2 - y1);

		if (!layer.isRepeatX() && !layer.isRepeatY()) {
			if (viewBounds.contains(imageBounds) || viewBounds.overlaps(imageBounds)) {
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

				batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
			}
		} else {

			// Determine number of times to repeat image across X and Y, + 4 for padding to avoid pop in/out
			int repeatX = layer.isRepeatX() ? (int)Math.ceil((viewBounds.width / imageBounds.width) + 4) : 0;
			int repeatY = layer.isRepeatY() ? (int)Math.ceil((viewBounds.height / imageBounds.height) + 4) : 0;

			// Calculate the offset of the first image to align with the camera
			float startX = viewBounds.x;
			float startY = viewBounds.y;
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

					repeatedImageBounds.set(rx1, ry1, rx2 - rx1, ry2 - ry1);

					if (viewBounds.contains(repeatedImageBounds) || viewBounds.overlaps(repeatedImageBounds)) {
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

						batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
					}
				}
			}
		}
	}

}
