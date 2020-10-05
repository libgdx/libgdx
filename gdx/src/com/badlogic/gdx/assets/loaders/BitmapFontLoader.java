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
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** {@link AssetLoader} for {@link BitmapFont} instances. Loads the font description file (.fnt) asynchronously, loads the
 * {@link Texture} containing the glyphs as a dependency. The {@link BitmapFontParameter} allows you to set things like texture
 * filters or whether to flip the glyphs vertically.
 * @author mzechner */
public class BitmapFontLoader extends AsynchronousAssetLoader<BitmapFont, BitmapFontLoader.BitmapFontParameter> {
	public BitmapFontLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	BitmapFontData data;

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, BitmapFontParameter parameter) {
		Array<AssetDescriptor> deps = new Array();
		if (parameter != null && parameter.bitmapFontData != null) {
			data = parameter.bitmapFontData;
			return deps;
		}

		data = new BitmapFontData(file, parameter != null && parameter.flip);
		if (parameter != null && parameter.atlasName != null) {
			deps.add(new AssetDescriptor(parameter.atlasName, TextureAtlas.class));
		} else {
			for (int i = 0; i < data.getImagePaths().length; i++) {
				String path = data.getImagePath(i);
				FileHandle resolved = resolve(path);

				TextureLoader.TextureParameter textureParams = new TextureLoader.TextureParameter();

				if (parameter != null) {
					textureParams.genMipMaps = parameter.genMipMaps;
					textureParams.minFilter = parameter.minFilter;
					textureParams.magFilter = parameter.magFilter;
				}

				AssetDescriptor descriptor = new AssetDescriptor(resolved, Texture.class, textureParams);
				deps.add(descriptor);
			}
		}

		return deps;
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle file, BitmapFontParameter parameter) {
	}

	@Override
	public BitmapFont loadSync (AssetManager manager, String fileName, FileHandle file, BitmapFontParameter parameter) {
		if (parameter != null && parameter.atlasName != null) {
			TextureAtlas atlas = manager.get(parameter.atlasName, TextureAtlas.class);
			String name = file.sibling(data.imagePaths[0]).nameWithoutExtension().toString();
			AtlasRegion region = atlas.findRegion(name);

			if (region == null)
				throw new GdxRuntimeException("Could not find font region " + name + " in atlas " + parameter.atlasName);
			return new BitmapFont(file, region);
		} else {
			int n = data.getImagePaths().length;
			Array<TextureRegion> regs = new Array(n);
			for (int i = 0; i < n; i++) {
				regs.add(new TextureRegion(manager.get(data.getImagePath(i), Texture.class)));
			}
			return new BitmapFont(data, regs, true);
		}
	}

	/** Parameter to be passed to {@link AssetManager#load(String, Class, AssetLoaderParameters)} if additional configuration is
	 * necessary for the {@link BitmapFont}.
	 * @author mzechner */
	static public class BitmapFontParameter extends AssetLoaderParameters<BitmapFont> {
		/** Flips the font vertically if {@code true}. Defaults to {@code false}. **/
		public boolean flip = false;

		/** Generates mipmaps for the font if {@code true}. Defaults to {@code false}. **/
		public boolean genMipMaps = false;

		/** The {@link TextureFilter} to use when scaling down the {@link BitmapFont}. Defaults to {@link TextureFilter#Nearest}. */
		public TextureFilter minFilter = TextureFilter.Nearest;

		/** The {@link TextureFilter} to use when scaling up the {@link BitmapFont}. Defaults to {@link TextureFilter#Nearest}. */
		public TextureFilter magFilter = TextureFilter.Nearest;

		/** optional {@link BitmapFontData} to be used instead of loading the {@link Texture} directly. Use this if your font is
		 * embedded in a {@link Skin}. **/
		public BitmapFontData bitmapFontData = null;

		/** The name of the {@link TextureAtlas} to load the {@link BitmapFont} itself from. Optional; if {@code null}, will look for
		 * a separate image */
		public String atlasName = null;
	}
}
