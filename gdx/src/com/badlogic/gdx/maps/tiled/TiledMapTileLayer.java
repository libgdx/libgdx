package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.maps.MapLayer;

public class TiledMapTileLayer extends MapLayer {

	private int width;
	private int height;
	
	private float tileWidth;
	private float tileHeight;
	
	private Cell[][] cells;
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public float getTileWidth() {
		return tileWidth;
	}
	
	public float getTileHeight() {
		return tileHeight;
	}
	
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
	
	public Cell getCell(int x, int y) {
		return cells[x][y];
	}
	public void setCell(int x, int y, TiledMapTile tile) {
		cells[x][y].setTile(tile);
	}
	
	public class Cell {
		
		private TiledMapTile tile;
		
		private boolean flipHorizontally;
		
		private boolean flipVertically;
		
		private boolean flipDiagonally;
		
		public TiledMapTile getTile() {
			return tile;
		}
		
		public void setTile(TiledMapTile tile) {
			this.tile = tile;
		}
		
		public boolean getFlipHorizontally() {
			return flipHorizontally;
		}
		
		public void setFlipHorizontally(boolean flipHorizontally) {
			this.flipHorizontally = flipHorizontally;
		}
		
		public boolean getFlipVertically() {
			return flipVertically;
		}
		
		public void setFlipVertically(boolean flipVertically) {
			this.flipVertically = flipVertically;
		}
		
		public boolean getFlipDiagonally() {
			return flipDiagonally;
		}
		
		public void setFlipDiagonally(boolean flipDiagonally) {
			this.flipDiagonally = flipDiagonally;
		}
		
	}
	
}
