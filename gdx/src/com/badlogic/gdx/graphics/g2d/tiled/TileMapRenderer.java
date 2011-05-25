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
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;

/**
 * A renderer for Tiled maps backed with a Sprite Cache.
 * @author David Fraska
 * */
public class TileMapRenderer implements Disposable {
	private SpriteCache cache;
	private int normalCacheId[][][], blendedCacheId[][][];

	private TileAtlas atlas;

	private int mapHeightUnits, mapWidthUnits;
	private int tileWidth, tileHeight;
	private float unitsPerTileX, unitsPerTileY;
	private int tilesPerBlockX, tilesPerBlockY;
	private int[] allLayers;

	private IntArray blendedTiles;

	/**
	 * A renderer for static tile maps backed with a Sprite Cache.
	 * 
	 * This constructor is for convenience when loading TiledMaps. The normal Tiled coordinate system is used when placing tiles.
	 * 
	 * A default shader is used if OpenGL ES 2.0 is enabled.
	 * 
	 * The tilesPerBlockX and tilesPerBlockY parameters will need to be adjusted for best performance. Smaller values will cull
	 * more precisely, but result in longer loading times. Larger values result in shorter loading times, but will cull less
	 * precisely.
	 * 
	 * @param map A tile map's tile numbers, in the order [layer][row][column]
	 * @param atlas The tile atlas to be used when drawing the map
	 * @param tilesPerBlockX The width of each block to be drawn, in number of tiles
	 * @param tilesPerBlockY The height of each block to be drawn, in number of tiles
	 */
	public TileMapRenderer (TiledMap map, TileAtlas atlas, int tilesPerBlockX, int tilesPerBlockY) {
		this(map, atlas, tilesPerBlockX, tilesPerBlockY, map.tileWidth, map.tileHeight);
	}

	/**
	 * A renderer for static tile maps backed with a Sprite Cache.
	 * 
	 * This constructor is for convenience when loading TiledMaps.
	 * 
	 * A default shader is used if OpenGL ES 2.0 is enabled.
	 * 
	 * The tilesPerBlockX and tilesPerBlockY parameters will need to be adjusted for best performance. Smaller values will cull
	 * more precisely, but result in longer loading times. Larger values result in shorter loading times, but will cull less
	 * precisely.
	 * 
	 * @param map A tile map's tile numbers, in the order [layer][row][column]
	 * @param atlas The tile atlas to be used when drawing the map
	 * @param tilesPerBlockX The width of each block to be drawn, in number of tiles
	 * @param tilesPerBlockY The height of each block to be drawn, in number of tiles
	 * @param unitsPerTileX The number of units per tile in the x direction
	 * @param unitsPerTileY The number of units per tile in the y direction
	 */
	public TileMapRenderer (TiledMap map, TileAtlas atlas, int tilesPerBlockX, int tilesPerBlockY, float unitsPerTileX,
		float unitsPerTileY) {
		this(map, atlas, tilesPerBlockX, tilesPerBlockY, unitsPerTileX, unitsPerTileY, null);
	}

	/**
	 * A renderer for static tile maps backed with a Sprite Cache.
	 * 
	 * This constructor is for convenience when loading TiledMaps. The normal Tiled coordinate system is used when placing tiles.
	 * 
	 * The tilesPerBlockX and tilesPerBlockY parameters will need to be adjusted for best performance. Smaller values will cull
	 * more precisely, but result in longer loading times. Larger values result in shorter loading times, but will cull less
	 * precisely.
	 * 
	 * @param map A tile map's tile numbers, in the order [layer][row][column]
	 * @param atlas The tile atlas to be used when drawing the map
	 * @param tilesPerBlockX The width of each block to be drawn, in number of tiles
	 * @param tilesPerBlockY The height of each block to be drawn, in number of tiles
	 * @param shader Shader to use for OpenGL ES 2.0, null uses a default shader. Ignored if using OpenGL ES 1.0.
	 */
	public TileMapRenderer (TiledMap map, TileAtlas atlas, int tilesPerBlockX, int tilesPerBlockY, ShaderProgram shader) {
		this(map, atlas, tilesPerBlockX, tilesPerBlockY, map.tileWidth, map.tileHeight, shader);
	}

	public TileMapRenderer (TiledMap map, TileAtlas atlas, int tilesPerBlockX, int tilesPerBlockY, float unitsPerTileX,
		float unitsPerTileY, ShaderProgram shader) {
		int[][][] tileMap = new int[map.layers.size()][][];
		for (int i = 0; i < map.layers.size(); i++) {
			tileMap[i] = map.layers.get(i).tiles;
		}

		for (int i = 0; i < map.tileSets.size(); i++) {
			if (map.tileSets.get(i).tileHeight - map.tileHeight > overdrawY * unitsPerTileY)
				overdrawY = (map.tileSets.get(i).tileHeight - map.tileHeight) / unitsPerTileY;
			if (map.tileSets.get(i).tileWidth - map.tileWidth > overdrawX * unitsPerTileX)
				overdrawX = (map.tileSets.get(i).tileWidth - map.tileWidth) / unitsPerTileX;
		}

		String blendedTiles = map.properties.get("blended tiles");
		IntArray blendedTilesArray;

		if (blendedTiles != null) {
			blendedTilesArray = createFromCSV(blendedTiles);
		} else {
			blendedTilesArray = new IntArray(0);
		}

		init(tileMap, atlas, map.tileWidth, map.tileHeight, unitsPerTileX, unitsPerTileY, blendedTilesArray, tilesPerBlockX,
			tilesPerBlockY, shader);
	}

	/**
	 * A renderer for static tile maps backed with a Sprite Cache.
	 * 
	 * The tilesPerBlockX and tilesPerBlockY parameters will need to be adjusted for best performance. Smaller values will cull
	 * more precisely, but result in longer loading times. Larger values result in shorter loading times, but will cull less
	 * precisely.
	 * 
	 * A default shader is used if OpenGL ES 2.0 is enabled.
	 * 
	 * @param map A tile map's tile numbers, in the order [layer][row][column]
	 * @param atlas The tile atlas to be used when drawing the map
	 * @param tileWidth The width of the tiles, in pixels
	 * @param tileHeight The height of the tiles, in pixels
	 * @param unitsPerTileX The number of units per tile in the x direction
	 * @param unitsPerTileY The number of units per tile in the y direction
	 * @param blendedTiles Array containing tile numbers that require blending
	 * @param tilesPerBlockX The width of each block to be drawn, in number of tiles
	 * @param tilesPerBlockY The height of each block to be drawn, in number of tiles
	 */
	public TileMapRenderer (int[][][] map, TileAtlas atlas, int tileWidth, int tileHeight, float unitsPerTileX,
		float unitsPerTileY, IntArray blendedTiles, int tilesPerBlockX, int tilesPerBlockY) {
		init(map, atlas, tileWidth, tileHeight, unitsPerTileX, unitsPerTileY, blendedTiles, tilesPerBlockX, tilesPerBlockY, null);
	}

	/**
	 * A renderer for static tile maps backed with a Sprite Cache.
	 * 
	 * The tilesPerBlockX and tilesPerBlockY parameters will need to be adjusted for best performance. Smaller values will cull
	 * more precisely, but result in longer loading times. Larger values result in shorter loading times, but will cull less
	 * precisely.
	 * 
	 * @param map A tile map's tile numbers, in the order [layer][row][column]
	 * @param atlas The tile atlas to be used when drawing the map
	 * @param tileWidth The width of the tiles, in pixels
	 * @param tileHeight The height of the tiles, in pixels
	 * @param unitsPerTileX The number of units per tile in the x direction
	 * @param unitsPerTileY The number of units per tile in the y direction
	 * @param blendedTiles Array containing tile numbers that require blending
	 * @param tilesPerBlockX The width of each block to be drawn, in number of tiles
	 * @param tilesPerBlockY The height of each block to be drawn, in number of tiles
	 * @param shader Shader to use for OpenGL ES 2.0, null uses a default shader. Ignored if using OpenGL ES 1.0.
	 */
	public TileMapRenderer (int[][][] map, TileAtlas atlas, int tileWidth, int tileHeight, float unitsPerTileX,
		float unitsPerTileY, IntArray blendedTiles, int tilesPerBlockX, int tilesPerBlockY, ShaderProgram shader) {
		init(map, atlas, tileWidth, tileHeight, unitsPerTileX, unitsPerTileY, blendedTiles, tilesPerBlockX, tilesPerBlockY, shader);
	}

	/**
	 * Initializer, used to avoid a "Constructor call must be the first statement in a constructor" syntax error when creating a
	 * map from a TiledMap
	 * */
	private void init (int[][][] map, TileAtlas atlas, int tileWidth, int tileHeight, float unitsPerTileX, float unitsPerTileY,
		IntArray blendedTiles, int tilesPerBlockX, int tilesPerBlockY, ShaderProgram shader) {
		this.atlas = atlas;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.unitsPerTileX = unitsPerTileX;
		this.unitsPerTileY = unitsPerTileY;

		this.blendedTiles = blendedTiles;
		this.tilesPerBlockX = tilesPerBlockX;
		this.tilesPerBlockY = tilesPerBlockY;

		int layer, row, col;

		allLayers = new int[map.length];

		// Calculate maximum cache size and map height in pixels, fill allLayers array
		int maxCacheSize = 0;
		int maxHeight = 0;
		int maxWidth = 0;
		for (layer = 0; layer < map.length; layer++) {
			allLayers[layer] = layer;
			if (map[layer].length > maxHeight) maxHeight = map[layer].length;
			for (row = 0; row < map[layer].length; row++) {
				if (map[layer][row].length > maxWidth) maxWidth = map[layer][row].length;
				for (col = 0; col < map[layer][row].length; col++)
					if (map[layer][row][col] != 0) maxCacheSize++;
			}
		}
		mapHeightUnits = (int)(maxHeight * unitsPerTileY);
		mapWidthUnits = (int)(maxWidth * unitsPerTileX);

		if (shader == null)
			cache = new SpriteCache(maxCacheSize, false);
		else
			cache = new SpriteCache(maxCacheSize, shader, false);

		normalCacheId = new int[map.length][][];
		blendedCacheId = new int[map.length][][];
		for (layer = 0; layer < map.length; layer++) {
			normalCacheId[layer] = new int[(int)MathUtils.ceil((float)map[layer].length / tilesPerBlockY)][];
			blendedCacheId[layer] = new int[(int)MathUtils.ceil((float)map[layer].length / tilesPerBlockY)][];
			for (row = 0; row < normalCacheId[layer].length; row++) {
				normalCacheId[layer][row] = new int[(int)MathUtils.ceil((float)map[layer][row].length / tilesPerBlockX)];
				blendedCacheId[layer][row] = new int[(int)MathUtils.ceil((float)map[layer][row].length / tilesPerBlockX)];
				for (col = 0; col < normalCacheId[layer][row].length; col++) {
					normalCacheId[layer][row][col] = addBlock(map[layer], layer, row, col, false);
					blendedCacheId[layer][row][col] = addBlock(map[layer], layer, row, col, true);
				}
			}
		}
	}

	private int addBlock (int[][] layer, int layerNum, int blockRow, int blockCol, boolean blended) {
		int tile;
		AtlasRegion region;
		cache.beginCache();

		int firstCol = blockCol * tilesPerBlockX;
		int firstRow = blockRow * tilesPerBlockY;
		int lastCol = firstCol + tilesPerBlockX;
		int lastRow = firstRow + tilesPerBlockY;

		int row, col;
		float x, y;

		for (row = firstRow; row < lastRow && row < layer.length; row++) {
			for (col = firstCol; col < lastCol && col < layer[row].length; col++) {
				tile = layer[row][col];
				if (tile != 0) {
					if (blended == blendedTiles.contains(tile)) {
						region = atlas.getRegion(tile);
						if (region != null) {
							y = ((layer.length - row) - (region.packedHeight + region.offsetY) / tileHeight) * unitsPerTileY;// + unitsPerTileY*layer.length;
							x = (col + region.offsetX / tileWidth) * unitsPerTileX;
							cache.add(region, x, y, unitsPerTileX, unitsPerTileY);
						}
					}
				}
			}
		}

		return cache.endCache();
	}

	/**
	 * Renders the entire map. Use this function only on very small maps or for debugging purposes. The size of the map is based on
	 * the first layer and the first row's size.
	 */
	public void render () {
		render(0, 0, (int)(getLayerWidthInBlocks(0, 0) * tilesPerBlockX * unitsPerTileX), (int)(getLayerHeightInBlocks(0)
			* tilesPerBlockX * unitsPerTileY));
	}

	/**
	 * Renders all layers between the given bounding box in map units. This is the same as calling
	 * {@link TileMapRenderer#render(float, float, int, int, int[])} with all layers in the layers list.
	 */
	public void render (float x, float y, float width, float height) {
		render(x, y, width, height, allLayers);
	}

	/**
	 * Renders specific layers between the given a camera. The x and y coordinates of the camera 
	 * @param cam The camera to use
	 */
	public void render (Camera cam) {
		render(cam, allLayers);
	}

	Vector3 tmp = new Vector3();
	/**
	 * Renders specific layers between the given a camera.
	 * @param cam The camera to use
	 * @param layers The list of layers to draw, 0 being the lowest layer. You will get an IndexOutOfBoundsException if a layer
	 *           number is too high.
	 */
	public void render (Camera cam, int[] layers) {
		getProjectionMatrix().set(cam.combined);
		tmp.set(0, 0, 0);
		cam.unproject(tmp);
		render(tmp.x, tmp.y, cam.viewportWidth, cam.viewportHeight, layers);
	}

	/**
	 * Sets the amount of overdraw in the X direction (in units). Use this if an actual tile width is greater than the tileWidth
	 * specified in the constructor. Use the value actual_tile_width - tileWidth (from the constructor).
	 */
	public float overdrawX;

	/**
	 * Sets the amount of overdraw in the Y direction (in units). Use this if an actual tile height is greater than the tileHeight
	 * specified in the constructor. Use the value actual_tile_height - tileHeight (from the constructor).
	 */
	public float overdrawY;

	private int initialRow, initialCol, currentRow, currentCol, lastRow, lastCol, currentLayer;

	/**
	 * Renders specific layers between the given bounding box in map units.
	 * @param x The x coordinate to start drawing
	 * @param y the y coordinate to start drawing
	 * @param width the width of the tiles to draw
	 * @param height the width of the tiles to draw
	 * @param layers The list of layers to draw, 0 being the lowest layer. You will get an IndexOutOfBoundsException if a layer
	 *           number is too high.
	 */
	public void render (float x, float y, float width, float height, int[] layers) {
		lastRow = (int)((mapHeightUnits - (y - height + overdrawY)) / (tilesPerBlockY * unitsPerTileY));
		initialRow = (int)((mapHeightUnits - (y - overdrawY)) / (tilesPerBlockY * unitsPerTileY));
		initialRow = (initialRow > 0) ? initialRow : 0; // Clamp initial Row > 0

		initialCol = (int)((x - overdrawX) / (tilesPerBlockX * unitsPerTileX));
		initialCol = (initialCol > 0) ? initialCol : 0; // Clamp initial Col > 0
		lastCol = (int)((x + width + overdrawX) / (tilesPerBlockX * unitsPerTileX));

		Gdx.gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		cache.begin();
		for (currentLayer = 0; currentLayer < layers.length; currentLayer++) {
			for (currentRow = initialRow; currentRow <= lastRow && currentRow < getLayerHeightInBlocks(currentLayer); currentRow++) {
				for (currentCol = initialCol; currentCol <= lastCol && currentCol < getLayerWidthInBlocks(currentLayer, currentRow); currentCol++) {
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

	private int getLayerWidthInBlocks (int layer, int row) {
		if (normalCacheId == null) return 0;
		if (normalCacheId[layer] == null) return 0;
		if (normalCacheId[layer][row] == null) return 0;
		return normalCacheId[layer][row].length;
	}

	private int getLayerHeightInBlocks (int layer) {
		if (normalCacheId == null) return 0;
		if (normalCacheId[layer] == null) return 0;
		return normalCacheId[layer].length;
	}

	public Matrix4 getProjectionMatrix () {
		return cache.getProjectionMatrix();
	}

	public Matrix4 getTransformMatrix () {
		return cache.getTransformMatrix();
	}

	/**
	 * Computes the Tiled Map row given a Y coordinate in units
	 * @param worldY the Y coordinate in units
	 * */
	public int getRow (int worldY) {
		return (int)(worldY / unitsPerTileY);
	}

	/**
	 * Computes the Tiled Map column given an X coordinate in units
	 * @param worldX the X coordinate in units
	 * */
	public int getCol (int worldX) {
		return (int)(worldX / unitsPerTileX);
	}

	/**
	 * Returns the initial drawn block row, for debugging purposes. Use this along with {@link TileMapRenderer#getLastRow()} to
	 * compute the number of rows drawn in the last call to {@link TileMapRenderer#render(float, float, int, int, int[])}.
	 * */
	public int getInitialRow () {
		return initialRow;
	}

	/**
	 * Returns the initial drawn block column, for debugging purposes. Use this along with {@link TileMapRenderer#getLastCol()} to
	 * compute the number of columns drawn in the last call to {@link TileMapRenderer#render(float, float, int, int, int[])}.
	 * */
	public int getInitialCol () {
		return initialCol;
	}

	/**
	 * Returns the final drawn block row, for debugging purposes. Use this along with {@link TileMapRenderer#getInitialRow()} to
	 * compute the number of rows drawn in the last call to {@link TileMapRenderer#render(float, float, int, int, int[])}.
	 * */
	public int getLastRow () {
		return lastRow;
	}

	/**
	 * Returns the final drawn block column, for debugging purposes. Use this along with {@link TileMapRenderer#getInitialCol()} to
	 * compute the number of columns drawn in the last call to {@link TileMapRenderer#render(float, float, int, int, int[])}.
	 * */
	public int getLastCol () {
		return lastCol;
	}

	public float getUnitsPerTileX () {
		return unitsPerTileX;
	}

	public float getUnitsPerTileY () {
		return unitsPerTileY;
	}

	public int getMapHeightUnits () {
		return mapHeightUnits;
	}

	public int getMapWidthUnits () {
		return mapWidthUnits;
	}

	private static int parseIntWithDefault (String string, int defaultValue) {
		if (string == null) return defaultValue;
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Releases all resources held by this TiledMapRenderer.
	 * */
	public void dispose () {
		cache.dispose();
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
}
