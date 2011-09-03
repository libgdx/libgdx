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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Skin.SkinData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;

/** @author Nathan Sweet */
public class SkinLoader extends AsynchronousAssetLoader<Skin, SkinLoader.SkinParameter> {
	public SkinLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	private FileHandle skinFile;
	private ObjectMap<String, String> fontPaths;

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, SkinParameter parameter) {
		if (parameter == null) throw new IllegalArgumentException("Missing SkinParameter: " + fileName);

		Array<AssetDescriptor> deps = new Array();
		deps.add(new AssetDescriptor(parameter.texturePath, Texture.class));

		ObjectMap<String, ObjectMap> root = (ObjectMap)new JsonReader().parse(resolve(fileName));
		ObjectMap<String, ObjectMap> resources = root.get("resources");
		ObjectMap<String, String> bitmapFontMap = null;
		if (resources != null) bitmapFontMap = resources.get(BitmapFont.class.getName());

		fontPaths = new ObjectMap();
		if (bitmapFontMap != null) {
			skinFile = resolve(fileName);
			for (Entry<String, String> entry : bitmapFontMap.entries()) {
				FileHandle fontFile = skinFile.parent().child(entry.value);
				if (!fontFile.exists()) fontFile = Gdx.files.internal(entry.value);
				AssetDescriptor asset = new AssetDescriptor(fontFile.path(), BitmapFont.class);
				fontPaths.put(entry.key, asset.fileName);
				deps.add(asset);
			}
		}

		return deps;
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, SkinParameter parameter) {
	}

	@Override
	public Skin loadSync (AssetManager manager, String fileName, SkinParameter parameter) {
		SkinData data = new SkinData();
		data.texture = manager.get(parameter.texturePath, Texture.class);

		ObjectMap<String, Object> fonts = new ObjectMap();
		data.resources.put(BitmapFont.class, fonts);
		for (Entry<String, String> entry : fontPaths.entries())
			fonts.put(entry.key, manager.get(entry.value, BitmapFont.class));

		return new Skin(skinFile, data);
	}

	static public class SkinParameter extends AssetLoaderParameters<Skin> {
		public final String texturePath;

		public SkinParameter (String texturePath) {
			this.texturePath = texturePath;
		}
	}
}
