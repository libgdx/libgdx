/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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

package com.badlogic.gdx.graphics.g2d.tiled;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

/** Contains an atlas of tiles by tile id for use with {@link TileMapRenderer} It does not need to be loaded with packed files.
 * Just plain textures.
 * @author Tomas Lazaro */
public class SimpleTileAtlas extends TileAtlas {

	/** Creates a TileAtlas for use with {@link TileMapRenderer}.
	 * @param map The tiled map
	 * @param inputDir The directory containing all needed textures in the map */
	public SimpleTileAtlas (TiledMap map, FileHandle inputDir) {
		for (TileSet set : map.tileSets) {
			Pixmap pixmap = new Pixmap(inputDir.child(set.imageName));

			int originalWidth = pixmap.getWidth();
			int originalHeight = pixmap.getHeight();

			if (!MathUtils.isPowerOfTwo(originalWidth) || !MathUtils.isPowerOfTwo(originalHeight)) {
				final int width = MathUtils.nextPowerOfTwo(originalWidth);
				final int height = MathUtils.nextPowerOfTwo(originalHeight);

				Pixmap potPixmap = new Pixmap(width, height, pixmap.getFormat());
				potPixmap.drawPixmap(pixmap, 0, 0, 0, 0, width, height);
				pixmap.dispose();
				pixmap = potPixmap;
			}
			Texture texture = new Texture(pixmap);
			pixmap.dispose();
			textures.add(texture);

			int idx = 0;
			TextureRegion[][] regions = split(texture, originalWidth, originalHeight, map.tileWidth, map.tileHeight, set.spacing,
				set.margin);
			for (int y = 0; y < regions[0].length; y++) {
				for (int x = 0; x < regions.length; x++) {
					regionsMap.put(idx++ + set.firstgid, regions[x][y]);
				}
			}
		}
	}

	private static TextureRegion[][] split (Texture texture, int totalWidth, int totalHeight, int width, int height, int spacing,
		int margin) {
		// TODO add padding support
		int xSlices = (totalWidth - margin) / (width + spacing);
		int ySlices = (totalHeight - margin) / (height + spacing);

		TextureRegion[][] res = new TextureRegion[xSlices][ySlices];
		for (int x = 0; x < xSlices; x++) {
			for (int y = 0; y < ySlices; y++) {
				res[x][y] = new TextureRegion(texture, margin + x * (width + spacing), margin + y * (height + spacing), width, height);
			}
		}
		return res;
	}
}
