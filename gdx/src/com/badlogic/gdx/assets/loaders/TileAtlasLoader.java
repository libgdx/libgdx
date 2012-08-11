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

package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.tiled.TileAtlas;
import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
import com.badlogic.gdx.utils.Array;

/** {@link AssetLoader} for {@link TileAtlas} instances.
 * @author mzechner */
public class TileAtlasLoader extends AsynchronousAssetLoader<TileAtlas, TileAtlasLoader.TileAtlasParameter> {

	/** Mandatory {@link AssetLoaderParameters} for loading a {@link TileAtlas}
	 * @author mzechner */
	public static class TileAtlasParameter extends AssetLoaderParameters<TileAtlas> {
		/** the filename of the {@link TiledMap} **/
		public String tileMapFile;
		/** the directory containing all the images **/
		public String inputDirectory;
	}

	public TileAtlasLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, TileAtlasParameter parameter) {
		if (parameter == null) throw new IllegalArgumentException("Missing TileAtlasParameter: " + fileName);

	}

	@Override
	public TileAtlas loadSync (AssetManager manager, String fileName, TileAtlasParameter parameter) {
		if (parameter == null) throw new IllegalArgumentException("Missing TileAtlasParameter: " + fileName);

		return null;
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, TileAtlasParameter parameter) {
		if (parameter == null) throw new IllegalArgumentException("Missing TileAtlasParameter: " + fileName);

		Array<AssetDescriptor> deps = new Array<AssetDescriptor>();
		deps.add(new AssetDescriptor(parameter.tileMapFile, TiledMap.class));
		return deps;
	}
}
