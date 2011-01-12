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

package com.badlogic.gdx.tiledmappacker;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.tiled.TileSet;
import com.badlogic.gdx.math.Vector2;

/**
 * Contains extra information that can only be calculated after a Tiled Map's tile set images are loaded.
 * @author David Fraska
 * */
public class TileSetLayout {

	public final BufferedImage image;
	private final Vector2[] imageTilePositions;
	public final int numRows, numCols, numTiles;
	public final TileSet tileSet;

	/**
	 * Constructs a Tile Set layout. The tile set image contained in the baseDir should be the original tile set images before
	 * being processed by {@link TiledMapPacker} (the ones actually read by Tiled).
	 * @param tileSet the tile set to process
	 * @param baseDir the directory in which the tile set image is stored
	 * */
	TileSetLayout (TileSet tileSet, FileHandle baseDir) throws IOException {
		this.tileSet = tileSet;
		image = ImageIO.read(baseDir.child(tileSet.imageName).read());

		numRows = (image.getHeight() - 2 * tileSet.margin) / (tileSet.tileHeight + tileSet.spacing);
		numCols = (image.getWidth() - 2 * tileSet.margin) / (tileSet.tileWidth + tileSet.spacing);
		numTiles = numRows * numCols;

		imageTilePositions = new Vector2[numTiles];

		// fill the tile regions
		int x = tileSet.margin, y = tileSet.margin, tile = 0;
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
				imageTilePositions[tile] = new Vector2(x, y);
				tile++;
				x += tileSet.tileWidth + tileSet.spacing;
				if (x >= image.getWidth()) { // end of row
					x = tileSet.margin;
					y += tileSet.tileHeight + tileSet.spacing;
				}
			}
		}
	}

	/** Returns the location of the tile in {@link TileSetLayout#image} */
	public Vector2 getLocation (int tile) {
		return imageTilePositions[tile - tileSet.firstgid];
	}
}
