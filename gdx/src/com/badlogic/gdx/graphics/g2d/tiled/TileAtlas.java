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

import java.util.HashSet;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;

/**
 * Contains an atlas of tiles by tile id for use with {@link TiledMapRenderer}
 * @author David Fraska
 */
public class TileAtlas implements Disposable {
	private IntMap<AtlasRegion> regionsMap;
	private final HashSet<Texture> textures = new HashSet<Texture>(1);

	/**
	 * Creates a TileAtlas for use with {@link TiledMapRenderer}. Run the map through TiledMapPacker to create the files
	 * required.
	 * @param map The tiled map
	 * @param packFile The pack file created by TiledMapPacker
	 * @param imagesDir The directory that has the images created by TiledMapPacker
	 * */
	public TileAtlas (TiledMap map, FileHandle packFile, FileHandle imagesDir) {
		TextureAtlas textureAtlas = new TextureAtlas(packFile, imagesDir, false);
		List<AtlasRegion> atlasRegions = (List<AtlasRegion>)textureAtlas.findRegions(map.tmxFile.nameWithoutExtension());
		regionsMap = new IntMap<AtlasRegion>(atlasRegions.size());
		for (int i = 0; i < atlasRegions.size(); i++) {
			regionsMap.put(atlasRegions.get(i).index, atlasRegions.get(i));
			if (!textures.contains(atlasRegions.get(i).getTexture())) textures.add(atlasRegions.get(i).getTexture());
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
	 * 
	 * Note: This function will only dispose of textures that were added by TiledMapPacker and are included in the map specified in
	 * the constructor.
	 */
	public void dispose () {
		for (Texture texture : textures)
			texture.dispose();
		textures.clear();
	}
}
