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
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeBitmapFontLoader.FreeTypeFontGeneratorBitmapFontParameters;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeBitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader.FreeTypeFontGeneratorParameters;
import com.badlogic.gdx.utils.Array;

/** Load {@link FreeTypeBitmapFont} with {@link FreeTypeFontGenerator} asynchronously via {@link AssetManager}.
 * {@link FreeTypeBitmapFont} are thus managed automatically. To be loaded, the {@link FreeTypeFontGenerator} must
 * be created through {@link FreeTypeFontGeneratorLoader} for the desired font.
 * @author https://github.com/avianey */
public class FreeTypeBitmapFontLoader extends AsynchronousAssetLoader<FreeTypeBitmapFont, FreeTypeFontGeneratorBitmapFontParameters> {

	public FreeTypeBitmapFontLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	static public class FreeTypeFontGeneratorBitmapFontParameters extends AssetLoaderParameters<FreeTypeBitmapFont> {
		/** Allow the configuration of the {@link FreeTypeFontGeneratorParameters} through {@link FreeTypeFontGenerator#scaleForPixelHeight(int)},
		 *  {@link FreeTypeFontGenerator#scaleToFitSquare(int, int, int)} or {@link FreeTypeFontGenerator#scaleForPixelWidth(int, int)} */
		public static interface FreeTypeFontParameterConfigurator {
			void configure(FreeTypeFontParameter parameters, FreeTypeFontGenerator generator);
		}
		public FreeTypeFontGeneratorBitmapFontParameters() {}
		/** Parameters for the font to be generated
		 * @param fontFile the name of the font file
		 * @param parameters for the {@link FreeTypeBitmapFont} : size, ... */
		public FreeTypeFontGeneratorBitmapFontParameters(String fontFile, FreeTypeFontParameter parameters) {
			this.parameters = parameters;
			this.fontFile = fontFile;
		}
		/** Parameters for the font to be generated
		 * @param fontFile the name of the font file
		 * @param parameters for the {@link FreeTypeBitmapFont} : size, ...
		 * @param configurator allow to set {@link FreeTypeFontParameter} params to values depending on the {@link FreeTypeFontGenerator} */
		public FreeTypeFontGeneratorBitmapFontParameters(String fontFile, FreeTypeFontParameter parameters, FreeTypeFontParameterConfigurator configurator) {
			this.parameters = parameters;
			this.fontFile = fontFile;
			this.configurator = configurator;
		}
		public String fontFile;
		public FreeTypeFontParameter parameters;
		public FreeTypeFontParameterConfigurator configurator;
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file, FreeTypeFontGeneratorBitmapFontParameters parameter) {}

	@Override
	public FreeTypeBitmapFont loadSync(AssetManager manager, String fileName, FileHandle file, FreeTypeFontGeneratorBitmapFontParameters parameter) {
		FreeTypeFontGenerator generator = manager.get(parameter.fontFile, FreeTypeFontGenerator.class);
		if (parameter.configurator != null) {
			parameter.configurator.configure(parameter.parameters, generator);
		}
		return generator.generateFont(parameter.parameters);
	}

	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, FreeTypeFontGeneratorBitmapFontParameters parameter) {
		// the FreeTypeFontGenerator is mandatory
		return Array.with(new AssetDescriptor(parameter.fontFile, FreeTypeFontGenerator.class));
	}
}
