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
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;

public class HexagonalTiledMapRenderer extends BatchTiledMapRenderer {

	public HexagonalTiledMapRenderer (TiledMap map) {
		super(map);
	}

	public HexagonalTiledMapRenderer (TiledMap map, float unitScale) {
		super(map, unitScale);
	}

	public HexagonalTiledMapRenderer (TiledMap map, Batch batch) {
		super(map, batch);
	}

	public HexagonalTiledMapRenderer (TiledMap map, float unitScale, Batch batch) {
		super(map, unitScale, batch);
	}

	@Override
	public void renderTileLayer (TiledMapTileLayer layer) {
		final Color batchColor = batch.getColor();
		final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * layer.getOpacity());

		final int layerWidth = layer.getWidth();
		final int layerHeight = layer.getHeight();

		final float layerTileWidth = layer.getTileWidth() * unitScale;
		final float layerTileHeight = layer.getTileHeight() * unitScale;

		final float layerTileWidth25 = layerTileWidth * 0.25f;
		final float layerTileWidth75 = layerTileWidth * 0.75f;

		final float layerTileHeight50 = layerTileHeight * 0.50f;
		final float layerTileHeight150 = layerTileHeight * 1.50f;

		final int col1 = Math.max(0, (int)(((viewBounds.x - layerTileWidth25) / layerTileWidth75)));
		final int col2 = Math.min(layerWidth, (int)((viewBounds.x + viewBounds.width + layerTileWidth75) / layerTileWidth75));

		final int row1 = Math.max(0, (int)((viewBounds.y / layerTileHeight150)));
		final int row2 = Math.min(layerHeight, (int)((viewBounds.y + viewBounds.height + layerTileHeight150) / layerTileHeight));

		final float[] vertices = this.vertices;

		for (int row = row1; row < row2; row++) {
			for (int col = col1; col < col2; col++) {
				float x = layerTileWidth75 * col;
				float y = (col % 2 == 1 ? 0 : layerTileHeight50) + (layerTileHeight * row);

				final TiledMapTileLayer.Cell cell = layer.getCell(col, row);
				if (cell == null) {
					x += layerTileWidth;
					continue;
				}
				final TiledMapTile tile = cell.getTile();
				if (tile != null) {
					if (tile instanceof AnimatedTiledMapTile) continue;

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
						break;
					}
					batch.draw(region.getTexture(), vertices, 0, 20);
				}
			}
		}
	}
}
