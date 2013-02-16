package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.maps.MapLayer;

/**
 * @brief Layer for a TiledMap 
 */
public class TiledMapTileLayer extends MapLayer {

	private int width;
	private int height;
	
	private float tileWidth;
	private float tileHeight;
	
	private Cell[][] cells;
	
	/**
	 * @return layer's witdth in tiles
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * @return layer's height in tiles
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * @return tiles' width in pixels
	 */
	public float getTileWidth() {
		return tileWidth;
	}
	
	/**
	 * @return tiles' height in pixels
	 */
	public float getTileHeight() {
		return tileHeight;
	}
	
	/**
	 * Creates TiledMap layer
	 * 
	 * @param width layer width in tiles
	 * @param height layer height in tiles
	 * @param tileWidth tile width in pixels
	 * @param tileHeight tile height in pixels 
	 */
	public TiledMapTileLayer(int width, int height, int tileWidth, int tileHeight) {
		super();
		this.width = width;
		this.height = height;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.cells = new Cell[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				cells[x][y] = new Cell();
			}
		}
	}
	
	/**
	 * @param x
	 * @param y 
	 * @return cell at (x, y)
	 */
	public Cell getCell(int x, int y) {
		return cells[x][y];
	}
	
	/**
	 * Sets the tile which should be used for the cell in the given location.
	 * 
	 * @param x
	 * @param y
	 * @param tile
	 */
	public void setCell(int x, int y, TiledMapTile tile) {
		cells[x][y].setTile(tile);
	}
	
	/**
	 * @brief represents a slot in a TiledLayer: TiledMapTile, flip and rotation properties.
	 */
	public class Cell {
		
		private TiledMapTile tile;
		
		private boolean flipHorizontally;
		
		private boolean flipVertically;
		
		private int rotation;
		
		/**
		 * @return The tile currently assigned to this cell.
		 */
		public TiledMapTile getTile() {
			return tile;
		}
		
		/**
		 * Sets the tile to be used for this cell.
		 * 
		 * @param tile
		 */
		public void setTile(TiledMapTile tile) {
			this.tile = tile;
		}

		/**
		 * @return Whether the tile should be flipped horizontally.
		 */		
		public boolean getFlipHorizontally() {
			return flipHorizontally;
		}
		
		/**
		 * Sets whether to flip the tile horizontally.
		 * 
		 * @param flipHorizontally
		 */
		public void setFlipHorizontally(boolean flipHorizontally) {
			this.flipHorizontally = flipHorizontally;
		}
		
		/**
		 * @return Whether the tile should be flipped vertically.
		 */
		public boolean getFlipVertically() {
			return flipVertically;
		}
		
		/**
		 * Sets whether to flip the tile vertically.
		 * 
		 * @param flipVertically
		 */
		public void setFlipVertically(boolean flipVertically) {
			this.flipVertically = flipVertically;
		}
		
		/**
		 * @return The rotation of this cell, in degrees.
		 */
		public int getRotation() {
			return rotation;
		}
		
		/**
		 * Sets the rotation of this cell, in degrees.
		 * 
		 * @param rotation
		 */
		public void setRotation(int rotation) {
			this.rotation = rotation;
		}
		
		public static final int ROTATE_0 = 0;
		public static final int ROTATE_90 = 1;
		public static final int ROTATE_180 = 2;
		public static final int ROTATE_270 = 3;
		
	}
	
}
