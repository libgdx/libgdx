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
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.TextureArray;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.TextureArrayData;
import com.badlogic.gdx.graphics.glutils.ETC1TextureData;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.graphics.glutils.KTXTextureData;
import com.badlogic.gdx.utils.Array;

/** {@link AssetLoader} for {@link TextureArray} instances. Multiple files can be specified by specifying a file name that ends in
 * the integer 0 before the file suffix (so similarly named files with consecutive following integers are also included), or by
 * explicitly naming all the files in a passed {@link TextureArrayParameter}. All image files used for a single texture array must
 * have the same dimensions.
 * 
 * The pixel data is loaded asynchronously. The texture is then created on the rendering thread, synchronously. Passing a
 * {@link TextureArrayParameter} to {@link AssetManager#load(String, Class, AssetLoaderParameters)} allows one to specify
 * parameters to be passed to the TextureArray constructors, e.g. filtering, whether to generate mipmaps and so on.
 * @author mzechner, cypherdare */
public class TextureArrayLoader extends AsynchronousAssetLoader<TextureArray, TextureArrayLoader.TextureArrayParameter> {
	TextureArrayData data;
	TextureArray textureArray;

	public TextureArrayLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle file, TextureArrayParameter parameter) {
		if (parameter == null || parameter.textureData == null) {
			Array<FileHandle> fileHandles = new Array<FileHandle>(FileHandle.class);
			if (parameter != null && parameter.fileNames != null){
				for (String name : parameter.fileNames)
					fileHandles.add(resolve(name));
			} else {
				String baseName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName; // strip file suffix
				
				if (baseName.charAt(baseName.length() - 1) == '0' && !Character.isDigit(baseName.charAt(baseName.length() - 2))){
					// baseName ends in the integer 0, search for all files with consecutive integers from 0 to ?
					baseName = baseName.substring(0, baseName.length() - 1);
					String fileSuffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.'), fileName.length()) : "";
					Array<String> fileNames = new Array<String>(String.class);
					while (true){
						int i = 0;
						String iFileName = baseName + fileSuffix + i;
						FileHandle iFileHandle = resolve(iFileName);
						if (!iFileHandle.exists())
							break;
						fileHandles.add(iFileHandle);
					}
				} else {
					fileHandles.add(resolve(fileName));
				}
			}
			
			Format format = null;
			boolean genMipMaps = false;
	
			if (parameter != null) {
				format = parameter.format;
				genMipMaps = parameter.genMipMaps;
				textureArray = (TextureArray)parameter.texture;
			}
	
			data = TextureArrayData.Factory.loadFromFiles(format, genMipMaps, fileHandles.toArray());
		} else {
			data = (TextureArrayData)parameter.textureData;
			textureArray = (TextureArray)parameter.texture;
		}
		if (!data.isPrepared()) data.prepare();
	}

	@Override
	public TextureArray loadSync (AssetManager manager, String fileName, FileHandle file, TextureArrayParameter parameter) {
		if (data == null) return null;
		if (textureArray != null){
			textureArray.load(data);
		} else {
			textureArray = new TextureArray(data);
		}
		if (parameter != null) {
			textureArray.setFilter(parameter.minFilter, parameter.magFilter);
			textureArray.setWrap(parameter.wrapU, parameter.wrapV);
		}
		return textureArray;
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, TextureArrayParameter parameter) {
		return null;
	}

	static public class TextureArrayParameter extends TextureLoader.TextureParameter<TextureArray> {
		/** Optional explicitly named files to load instead of using the file name passed to the AssetManager. */
		public String[] fileNames;
		/** Sets optional explicitly named files to load instead of using the file name passed to the AssetManager. */
		public void setFiles(String...fileNames){
			this.fileNames = fileNames;
		}
	}
}
