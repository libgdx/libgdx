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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

/** @author Nathan Sweet */
public class SkinLoader extends AsynchronousAssetLoader<Skin, SkinLoader.SkinParameter> {
	public SkinLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, SkinParameter parameter) {
		Array<AssetDescriptor> deps = new Array();
		if (parameter == null)
			deps.add(new AssetDescriptor(Gdx.files.internal(fileName).nameWithoutExtension() + ".png", Texture.class));
		else
			deps.add(new AssetDescriptor(parameter.texturePath, Texture.class));
		return deps;
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, SkinParameter parameter) {
	}

	@Override
	public Skin loadSync (AssetManager manager, String fileName, SkinParameter parameter) {
		String texturePath;
		if (parameter == null)
			texturePath = Gdx.files.internal(fileName).nameWithoutExtension() + ".png";
		else
			texturePath = parameter.texturePath;
		Texture texture = manager.get(texturePath, Texture.class);
		return new Skin(resolve(fileName), texture);
	}

	static public class SkinParameter extends AssetLoaderParameters<Skin> {
		public final String texturePath;

		public SkinParameter (String texturePath) {
			this.texturePath = texturePath;
		}
	}
}
