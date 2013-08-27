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
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

/** {@link AssetLoader} for {@link Skin} instances. All {@link Texture} and {@link BitmapFont} instances will be loaded as
 * dependencies. Passing a {@link SkinParameter} allows one to specify the exact name of the texture associated with the skin.
 * Otherwise the skin texture is looked up just as with a call to {@link Skin#Skin(com.badlogic.gdx.files.FileHandle)}.
 * @author Nathan Sweet */
public class SkinLoader extends AsynchronousAssetLoader<Skin, SkinLoader.SkinParameter> {
	public SkinLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, SkinParameter parameter) {
		Array<AssetDescriptor> deps = new Array();
		if (parameter == null)
			deps.add(new AssetDescriptor(file.pathWithoutExtension() + ".atlas", TextureAtlas.class));
		else if (parameter.textureAtlasPath != null)
			deps.add(new AssetDescriptor(parameter.textureAtlasPath, TextureAtlas.class));
		return deps;
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle file, SkinParameter parameter) {
	}

	@Override
	public Skin loadSync (AssetManager manager, String fileName, FileHandle file, SkinParameter parameter) {
		String textureAtlasPath;
		if (parameter == null)
			textureAtlasPath = file.pathWithoutExtension() + ".atlas";
		else
			textureAtlasPath = parameter.textureAtlasPath;
		TextureAtlas atlas = manager.get(textureAtlasPath, TextureAtlas.class);
		return new Skin(file, atlas);
	}

	static public class SkinParameter extends AssetLoaderParameters<Skin> {
		public final String textureAtlasPath;

		public SkinParameter (String textureAtlasPath) {
			this.textureAtlasPath = textureAtlasPath;
		}
	}
}
