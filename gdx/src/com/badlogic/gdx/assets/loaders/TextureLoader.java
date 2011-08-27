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
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.ReferenceCountedAsset;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.utils.Array;

public class TextureLoader extends AsynchronousAssetLoader<Texture, TextureParameter> {
	TextureData data;
	Texture texture;

	public TextureLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, TextureParameter parameter) {
		if(parameter == null || (parameter != null && parameter.textureData == null)) {
			FileHandle handle = resolve(fileName);
			Pixmap pixmap = new Pixmap(handle);
			Format format = null;
			boolean genMipMaps = false;
			texture = null;
	
			if (parameter != null) {
				format = parameter.format;
				genMipMaps = parameter.genMipMaps;
				texture = parameter.texture;
			}
	
			data = new FileTextureData(handle, pixmap, format, genMipMaps);
		} else {
			// FIXME use TextureData in parameter
			data = parameter.textureData;
		}
	}

	@Override
	public Texture loadSync () {
		if (texture != null) {
			texture.load(data);
			return texture;
		} else {
			return new ReferenceCountedTexture(data);
		}
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, TextureParameter parameter) {
		return null;
	}

	public static class ReferenceCountedTexture extends Texture implements ReferenceCountedAsset {
		public ReferenceCountedTexture (TextureData data) {
			super(data);
		}

		private int refCount = 1;

		@Override
		public void incRefCount () {
			refCount++;
		}

		@Override
		public int getRefCount () {
			return refCount;
		}

		@Override
		public void dispose () {
			refCount--;
			if (refCount > 0) return;
			super.dispose();
		}
	}
}