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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorBitmapFontLoader.FreeTypeFontGeneratorBitmapFontParameters;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader.FreeTypeFontGeneratorParameters;
import com.badlogic.gdx.utils.Array;

public class FreeTypeFontGeneratorBitmapFontLoader extends AsynchronousAssetLoader<BitmapFont, FreeTypeFontGeneratorBitmapFontParameters> {

	public FreeTypeFontGeneratorBitmapFontLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	static public class FreeTypeFontGeneratorBitmapFontParameters extends AssetLoaderParameters<BitmapFont> {
		/** Allow the configuration of the {@link FreeTypeFontGeneratorParameters} through {@link FreeTypeFontGenerator#scaleForPixelHeight(int)},
		 *  {@link FreeTypeFontGenerator#scaleToFitSquare(int, int, int)} or  {@link FreeTypeFontGenerator#scaleForPixelWidth(int, int)} */
		public static interface Configurator {
			void configure(FreeTypeFontParameter parameters, FreeTypeFontGenerator generator);
		}
		public FreeTypeFontGeneratorBitmapFontParameters() {}
		public FreeTypeFontGeneratorBitmapFontParameters(String fontFile, FreeTypeFontParameter parameters) {
			this.parameters = parameters;
			this.fontFile = fontFile;
		}
		public String fontFile;
		public FreeTypeFontParameter parameters;
		public Configurator configurator;
		BitmapFont font;
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file, FreeTypeFontGeneratorBitmapFontParameters parameter) {
		FreeTypeFontGenerator generator = manager.get(parameter.fontFile, FreeTypeFontGenerator.class);
		if (parameter.configurator != null) {
			parameter.configurator.configure(parameter.parameters, generator);
		}
		parameter.font = generator.generateFont(parameter.parameters);
	}

	@Override
	public BitmapFont loadSync(AssetManager manager, String fileName, FileHandle file, FreeTypeFontGeneratorBitmapFontParameters parameter) {
		return parameter.font;
	}

	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, FreeTypeFontGeneratorBitmapFontParameters parameter) {
		// the FreeTypeFontGenerator is mandatory
		return Array.with(new AssetDescriptor(parameter.fontFile, FreeTypeFontGenerator.class));
	}
}
