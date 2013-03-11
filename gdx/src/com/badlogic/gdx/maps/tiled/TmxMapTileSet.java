
package com.badlogic.gdx.maps.tiled;

/** @brief Specialized tileset implementation for tiled-based maps, exposes more useful information */
public class TmxMapTileSet extends TiledMapTileSet {
	private final int firstGid;
	private final int tileWidth;
	private final int tileHeight;
	private final int spacing;
	private final int margin;
	private final String imageName;
	private final int imageWidth;
	private final int imageHeight;

	public TmxMapTileSet (String imageName, int imageWidth, int imageHeight, int firstgid, int tileWidth, int tileHeight,
		int margin, int spacing) {

		super();
		this.imageName = imageName;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.firstGid = firstgid;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.spacing = spacing;
		this.margin = margin;
	}

	public int getFirstGid () {
		return firstGid;
	}

	public int getTileWidth () {
		return tileWidth;
	}

	public int getTileHeight () {
		return tileHeight;
	}

	public int getSpacing () {
		return spacing;
	}

	public int getMargin () {
		return margin;
	}

	public String getImageName () {
		return imageName;
	}

	public int getImageWidth () {
		return imageWidth;
	}

	public int getImageHeight () {
		return imageHeight;
	}
}
