/*
 * Copyright 2010 David Fraska (dfraska@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.graphics.g2d.tiled;

import java.util.StringTokenizer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;

/**
 * A renderer for Tiled maps backed with a Sprite Cache.
 * @author David Fraska
 * */
public class TiledMapRenderer {
	private SpriteCache cache;
	private int normalCacheId[][][], blendedCacheId[][][];

	private TileAtlas atlas;

	private int tileWidth, tileHeight;
	private int pixelsPerMapX, pixelsPerMapY;
	private int blocksPerMapY, blocksPerMapX;
	private int tilesPerBlockY, tilesPerBlockX;

	private int overdrawX = 0, overdrawY = 0;

	private IntArray blendedTiles;
	private int[] allLayers;

	/**
	 * A renderer for Tiled maps backed with a Sprite Cache.
	 * 
	 * The blockWidth and blockHeight parameters are used to determine how many tiles are in each block. When
	 * {@link TiledMapRenderer#render(int, int, int, int, int[])} is called, each block will equate one call to
	 * {@link SpriteCache#draw(int)}.
	 * 
	 * For debugging, use {@link TiledMapRenderer#getInitialCol()}, {@link TiledMapRenderer#getInitialRow()},
	 * {@link TiledMapRenderer#getLastCol()}, and {@link TiledMapRenderer#getLastRow()} after calling
	 * {@link TiledMapRenderer#render(int, int, int, int, int[])} to determine how many blocks were drawn.
	 * 
	 * @param map A Tiled map that has been run through the {@link TiledMapPacker}, which will add a few properties to optimize the renderer
	 * @param atlas The tile atlas to be used when drawing the map
	 * @param blockWidth The width of each block to be drawn, in pixels
	 * @param blockHeight The width of each block to be drawn, in pixels
	 */
	public TiledMapRenderer (TiledMap map, TileAtlas atlas, int blockWidth, int blockHeight) {
		this(map, atlas, blockWidth, blockHeight, null);
	}

	/**
	 * A renderer for Tiled maps backed with a Sprite Cache.
	 * 
	 * The blockWidth and blockHeight parameters are used to determine how many tiles are in each block. When
	 * {@link TiledMapRenderer#render(int, int, int, int, int[])} is called, each block will equate one call to
	 * {@link SpriteCache#draw(int)}.
	 * 
	 * For debugging, use {@link TiledMapRenderer#getInitialCol()}, {@link TiledMapRenderer#getInitialRow()},
	 * {@link TiledMapRenderer#getLastCol()}, and {@link TiledMapRenderer#getLastRow()} after calling
	 * {@link TiledMapRenderer#render(int, int, int, int, int[])} to determine how many blocks were drawn.
	 * 
	 * @param map The map to be drawn
	 * @param atlas The tile atlas to be used when drawing the map
	 * @param blockWidth The width of each block to be drawn, in pixels
	 * @param blockHeight The width of each block to be drawn, in pixels
	 * @param shader Shader to use for OpenGL ES 2.0
	 */
	public TiledMapRenderer (TiledMap map, TileAtlas atlas, int blockWidth, int blockHeight, ShaderProgram shader) {
		this.atlas = atlas;
		this.tileWidth = map.tileWidth;
		this.tileHeight = map.tileHeight;

		int i;

		if (!map.orientation.equals("orthogonal")) throw new GdxRuntimeException("Only orthogonal maps supported!");

		// allLayers array simplifies calling render without a layer list
		allLayers = new int[map.layers.size()];
		for (i = 0; i < map.layers.size(); i++) {
			allLayers[i] = i;
		}

		tilesPerBlockX = (int)Math.ceil((float)blockWidth / (float)map.tileWidth);
		tilesPerBlockY = (int)Math.ceil((float)blockHeight / (float)map.tileHeight);

		pixelsPerMapX = map.width * map.tileWidth;
		pixelsPerMapY = map.height * map.tileHeight;

		blocksPerMapX = (int)Math.ceil((float)map.width / (float)tilesPerBlockX);
		blocksPerMapY = (int)Math.ceil((float)map.height / (float)tilesPerBlockY);

		normalCacheId = new int[map.layers.size()][blocksPerMapY][blocksPerMapX];
		blendedCacheId = new int[map.layers.size()][blocksPerMapY][blocksPerMapX];

		// Calculate overdrawing values for when the tiles are larger than the map tile size
		int overdrawXtemp, overdrawYtemp;
		for (i = 0; i < map.tileSets.size(); i++) {
			overdrawXtemp = map.tileSets.get(i).tileWidth - map.tileWidth;
			if (overdrawXtemp > overdrawX) overdrawX = overdrawXtemp;

			overdrawYtemp = map.tileSets.get(i).tileHeight - map.tileHeight;
			if (overdrawYtemp > overdrawY) overdrawY = overdrawYtemp;
		}

		String blendedTilesString = map.properties.get("blended tiles");
		if (blendedTilesString != null) {
			blendedTiles = createFromCSV(blendedTilesString);
		}

		int maxCacheSize = parseIntWithDefault(map.properties.get("tile count"), 0);
		if (maxCacheSize == 0) {
			for (i = 0; i < map.layers.size(); i++) {
				maxCacheSize += map.layers.get(i).height * map.layers.get(i).width;
			}
			Gdx.app.log("TiledMapRenderer", "Warning - map doesn't include a tile count");
		}

		if (shader == null)
			cache = new SpriteCache(maxCacheSize, false);
		else
			cache = new SpriteCache(maxCacheSize, shader, false);

		int row, col;

		for (row = 0; row < blocksPerMapY; row++) {
			for (col = 0; col < blocksPerMapX; col++) {
				for (i = 0; i < map.layers.size(); i++) {
					normalCacheId[i][row][col] = addBlock(map, i, row, col, false);
					blendedCacheId[i][row][col] = addBlock(map, i, row, col, true);
				}
			}
		}
	}

	private static IntArray createFromCSV (String values) {
		IntArray list = new IntArray(false, (values.length() + 1) / 2);
		StringTokenizer st = new StringTokenizer(values, ",");
		while (st.hasMoreTokens()) {
			list.add(Integer.parseInt(st.nextToken()));
		}
		list.shrink();
		return list;
	}

	private int addBlock (TiledMap map, int layerNum, int blockRow, int blockCol, boolean blended) {
		int tile;
		AtlasRegion region;
		cache.beginCache();

		TiledLayer layer = map.layers.get(layerNum);

		int firstCol = blockCol * tilesPerBlockX;
		int firstRow = blockRow * tilesPerBlockY;
		int lastCol = Math.min(firstCol + tilesPerBlockX, map.width);
		int lastRow = Math.min(firstRow + tilesPerBlockY, map.height);

		int row, col;

		for (row = firstRow; row < lastRow; row++) {
			for (col = firstCol; col < lastCol; col++) {
				tile = layer.tile[row][col];
				if (tile != 0) {
					if (blended == blendedTiles.contains(tile)) {
						region = atlas.getRegion(tile);
						cache.add(region, col * map.tileWidth + region.offsetX, (map.height - row) * map.tileHeight
							- region.packedHeight - region.offsetY);
					}
				}
			}
		}

		return cache.endCache();
	}

	/**
	 * Renders the entire map. Use this function only on very small maps or for debugging purposes.
	 */
	public void render () {
		render(0, 0, pixelsPerMapX, pixelsPerMapY);
	}

	/**
	 * Renders all layers between the given Tiled world coordinates. This is the same as calling
	 * {@link TiledMapRenderer#render(int, int, int, int, int[])} with all layers in the layers list.
	 */
	public void render (int x, int y, int width, int height) {
		render(x, y, width, height, allLayers);
	}

	private int initialRow, initialCol, currentRow, currentCol, lastRow, lastCol, currentLayer;

	/**
	 * Renders specific layers between the given Tiled world coordinates.
	 * @param layers The list of layers to draw, 0 being the lowest layer. You will get an IndexOutOfBoundsException if a layer
	 *           number is too high.
	 */
	public void render (int x, int y, int width, int height, int[] layers) {
		if (x > pixelsPerMapX || y > pixelsPerMapY) return;
		initialRow = (y - overdrawY) / (tilesPerBlockY * tileHeight);
		initialRow = (initialRow > 0) ? initialRow : 0; // Clamp initial Row > 0
		initialCol = (x - overdrawX) / (tilesPerBlockX * tileWidth);
		initialCol = (initialCol > 0) ? initialCol : 0; // Clamp initial Col > 0
		lastRow = (y + height + overdrawY) / (tilesPerBlockY * tileHeight);
		lastRow = (lastRow < blocksPerMapY) ? lastRow : blocksPerMapY - 1; // Clamp last Row < blocksPerMapY
		lastCol = (x + width + overdrawX) / (tilesPerBlockX * tileWidth);
		lastCol = (lastCol < blocksPerMapX) ? lastCol : blocksPerMapX - 1; // Clamp last Col < blocksPerMapX

		Gdx.gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		cache.begin();
		for (currentRow = initialRow; currentRow <= lastRow; currentRow++) {
			for (currentCol = initialCol; currentCol <= lastCol; currentCol++) {
				for (currentLayer = 0; currentLayer < layers.length; currentLayer++) {
					Gdx.gl.glDisable(GL10.GL_BLEND);
					cache.draw(normalCacheId[layers[currentLayer]][currentRow][currentCol]);
					Gdx.gl.glEnable(GL10.GL_BLEND);
					cache.draw(blendedCacheId[layers[currentLayer]][currentRow][currentCol]);
				}
			}
		}
		cache.end();
		Gdx.gl.glDisable(GL10.GL_BLEND);
	}

	/**
	 * Returns the initial drawn block row, for debugging purposes. Use this along with {@link TiledMapRenderer#getLastRow()} to
	 * compute the number of rows drawn in the last call to {@link TiledMapRenderer#render(int, int, int, int, int[])}.
	 * */
	public int getInitialRow () {
		return initialRow;
	}

	/**
	 * Returns the initial drawn block column, for debugging purposes. Use this along with {@link TiledMapRenderer#getLastCol()} to
	 * compute the number of columns drawn in the last call to {@link TiledMapRenderer#render(int, int, int, int, int[])}.
	 * */
	public int getInitialCol () {
		return initialCol;
	}

	/**
	 * Returns the final drawn block row, for debugging purposes. Use this along with {@link TiledMapRenderer#getInitialRow()} to
	 * compute the number of rows drawn in the last call to {@link TiledMapRenderer#render(int, int, int, int, int[])}.
	 * */
	public int getLastRow () {
		return lastRow;
	}

	/**
	 * Returns the final drawn block column, for debugging purposes. Use this along with {@link TiledMapRenderer#getInitialCol()}
	 * to compute the number of columns drawn in the last call to {@link TiledMapRenderer#render(int, int, int, int, int[])}.
	 * */
	public int getLastCol () {
		return lastCol;
	}

	public Matrix4 getProjectionMatrix () {
		return cache.getProjectionMatrix();
	}

	public Matrix4 getTransformMatrix () {
		return cache.getTransformMatrix();
	}

	public int getMapHeightPixels () {
		return pixelsPerMapY;
	}

	public int getMapWidthPixels () {
		return pixelsPerMapX;
	}

	/**
	 * Computes the Tiled Map row given a Y coordinate in pixels
	 * @param worldY the Y coordinate in pixels
	 * */
	int getRow (int worldY) {
		if (worldY < 0) return 0;
		if (worldY > pixelsPerMapY) return tileHeight - 1;
		return worldY / tileHeight;
	}
	
	/**
	 * Computes the Tiled Map column given an X coordinate in pixels
	 * @param worldX the X coordinate in pixels
	 * */
	int getCol (int worldX) {
		if (worldX < 0) return 0;
		if (worldX > pixelsPerMapX) return tileWidth - 1;
		return worldX / tileWidth;
	}

	private static int parseIntWithDefault (String string, int defaultValue) {
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Releases all resources held by this TiledMapRenderer.
	 * */
	void dispose () {
		cache.dispose();
	}
}
