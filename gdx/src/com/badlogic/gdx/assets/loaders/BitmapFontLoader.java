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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class BitmapFontLoader extends AsynchronousAssetLoader<BitmapFont, BitmapFontLoader.BitmapFontParameter> {
	public BitmapFontLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	BitmapFontData data;

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, BitmapFontParameter parameter) {
		FileHandle handle = resolve(fileName);
		data = new BitmapFontData(handle, parameter != null ? parameter.flip : false);

		Array<AssetDescriptor> deps = new Array<AssetDescriptor>();
		deps.add(new AssetDescriptor(data.getImagePath(), Texture.class, null));
		return deps;
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, BitmapFontParameter parameter) {
	}

	@Override
	public BitmapFont loadSync (AssetManager manager, String fileName, BitmapFontParameter parameter) {
		FileHandle handle = resolve(fileName);
		TextureRegion region = new TextureRegion(manager.get(data.getImagePath(), Texture.class));
		return new BitmapFont(data, region, true);
	}

	static public class BitmapFontParameter implements AssetLoaderParameters<BitmapFont> {
		/** whether to flipY the font or not **/
		public boolean flip = false;
	}
}
