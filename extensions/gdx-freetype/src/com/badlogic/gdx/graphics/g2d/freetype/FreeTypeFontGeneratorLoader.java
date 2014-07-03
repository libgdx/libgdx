/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.g2d.freetype;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader.FreeTypeFontGeneratorParameters;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader.FreeTypeFontGeneratorParameters;
import com.badlogic.gdx.utils.Array;

/** Makes {@link FreeTypeFontGenerator} managable via {@link AssetManager}.
 * <p>
 * Do
 * {@code assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(new InternalFileHandleResolver()))}
 * to register it.
 * </p>
 * @author Daniel Holderbaum */
public class FreeTypeFontGeneratorLoader extends AsynchronousAssetLoader<FreeTypeFontGenerator, FreeTypeFontGeneratorParameters> {

	public FreeTypeFontGeneratorLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	static public class FreeTypeFontGeneratorParameters extends AssetLoaderParameters<FreeTypeFontGenerator> {
		public FreeTypeFontGeneratorParameters() {}
		FreeTypeFontGenerator generator;
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file, FreeTypeFontGeneratorParameters parameter) {
		parameter.generator = new FreeTypeFontGenerator(file);
	}

	@Override
	public FreeTypeFontGenerator loadSync (AssetManager manager, String fileName, FileHandle file, FreeTypeFontGeneratorParameters parameter) {
		return parameter.generator;
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, FreeTypeFontGeneratorParameters parameter) {
		return null;
	}
}
