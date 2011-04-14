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

import java.util.HashSet;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;

/**
 * Contains an atlas of tiles by tile id for use with {@link TileMapRenderer}
 * @author David Fraska
 */
public class TileAtlas implements Disposable {
	private IntMap<AtlasRegion> regionsMap;
	private final HashSet<Texture> textures = new HashSet<Texture>(1);

	/**
	 * Creates a TileAtlas for use with {@link TileMapRenderer}. Run the map through TiledMapPacker to create the files required.
	 * @param map The tiled map
	 * @param inputDir The directory containing all the files created by TiledMapPacker
	 * */
	public TileAtlas (TiledMap map, FileHandle inputDir) {
		// TODO: Create a constructor that doesn't take a tmx map, 
		regionsMap = new IntMap<AtlasRegion>();
		TextureAtlas textureAtlas;
		List<AtlasRegion> atlasRegions;
		int j;
		TileSet set;

		for (int i = 0; i < map.tileSets.size(); i++) {
			set = map.tileSets.get(i);
			textureAtlas = new TextureAtlas(inputDir.child(removeExtension(set.imageName) + " packfile"), inputDir, false);
			atlasRegions = (List<AtlasRegion>)textureAtlas.findRegions(removeExtension(set.imageName));

			for (j = 0; j < atlasRegions.size(); j++) {
				regionsMap.put(atlasRegions.get(j).index + set.firstgid, atlasRegions.get(j));
				if (!textures.contains(atlasRegions.get(j).getTexture())) textures.add(atlasRegions.get(j).getTexture());
			}
		}
	}

	/**
	 * Gets an {@link AtlasRegion} for a tile id
	 * @param id tile id
	 * @return the {@link AtlasRegion}
	 * */
	public AtlasRegion getRegion (int id) {
		return regionsMap.get(id);
	}

	/**
	 * Releases all resources associated with this TileAtlas instance. This releases all the textures backing all AtlasRegions,
	 * which should no longer be used after calling dispose.
	 */
	public void dispose () {
		for (Texture texture : textures)
			texture.dispose();
		textures.clear();
	}

	private static String removeExtension (String s) {

		String separator = System.getProperty("file.separator");
		String filename;

		// Remove the path up to the filename.
		int lastSeparatorIndex = s.lastIndexOf(separator);
		if (lastSeparatorIndex == -1) {
			filename = s;
		} else {
			filename = s.substring(lastSeparatorIndex + 1);
		}

		// Remove the extension.
		int extensionIndex = filename.lastIndexOf(".");
		if (extensionIndex == -1) return filename;

		return filename.substring(0, extensionIndex);
	}
}
