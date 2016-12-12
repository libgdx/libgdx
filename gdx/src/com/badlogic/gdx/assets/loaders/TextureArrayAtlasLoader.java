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
import com.badlogic.gdx.assets.loaders.TextureArrayLoader.TextureArrayParameter;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader.TextureAtlasParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.TextureArray;
import com.badlogic.gdx.graphics.g2d.TextureArrayAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Page;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;

/** {@link AssetLoader} to load {@link TextureArrayAtlas} instances, available only in a GL30 environment. Passing a
 * {@link TextureAtlasLoader.TextureAtlasParameter} to {@link AssetManager#load(String, Class, AssetLoaderParameters)} allows one
 * to specify whether the atlas regions should be flipped on the y-axis or not.
 * @author mzechner, cypherdare */
public class TextureArrayAtlasLoader
	extends TextureAtlasLoader<TextureArrayAtlas, TextureAtlasLoader.TextureAtlasParameter<TextureArrayAtlas>> {

	ObjectMap<Page, String> pagesToTextureFileNames;

	public TextureArrayAtlasLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public TextureArrayAtlas load (AssetManager assetManager, String fileName, FileHandle file, TextureAtlasParameter parameter) {
		for (Page page : data.getPages()) {
			page.texture = assetManager.get(pagesToTextureFileNames.get(page), TextureArray.class);
		}

		return new TextureArrayAtlas(data);
	}

	@Override
	protected Array<AssetDescriptor> getDependencies (TextureAtlasData data) {
		Array<AssetDescriptor> dependencies = new Array();
		ObjectSet<Array<Page>> pageArrays = TextureArrayAtlas.groupPagesBySharableTexture(data.getPages());
		pagesToTextureFileNames = new ObjectMap<Page, String>(data.getPages().size);

		for (Array<Page> pageArray : pageArrays) {
			// Each texture array is identified by the image file corresponding to the first page of that group
			Page firstPage = pageArray.first();
			String textureFileIdentifier = toFilePath(firstPage.textureFile);
			for (Page page : pageArray) {
				pagesToTextureFileNames.put(page, textureFileIdentifier);
			}

			TextureArrayParameter params = new TextureArrayParameter();
			params.format = firstPage.format;
			params.genMipMaps = firstPage.useMipMaps;
			params.minFilter = firstPage.minFilter;
			params.magFilter = firstPage.magFilter;
			params.fileNames = new String[pageArray.size];
			for (int i = 0; i < pageArray.size; i++)
				params.fileNames[i] = toFilePath(pageArray.get(i).textureFile);
			dependencies.add(new AssetDescriptor(textureFileIdentifier, TextureArray.class, params));
		}

		return dependencies;
	}
}
