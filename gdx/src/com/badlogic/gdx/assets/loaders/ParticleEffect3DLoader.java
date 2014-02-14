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
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.utils.Array;

/** {@link AssetLoader} to load {@link ParticleEffect} instances. Passing a {@link ParticleEffect3DParameter} to
 * {@link AssetManager#load(String, Class, AssetLoaderParameters)} allows to specify an atlas file or an image directory to be
 * used for the effect's images. Per default images are loaded from the directory in which the effect file is found. */
public class ParticleEffect3DLoader extends SynchronousAssetLoader<ParticleEffect, ParticleEffect3DLoader.ParticleEffect3DParameter> 
{
	public ParticleEffect3DLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public ParticleEffect load (AssetManager am, String fileName, FileHandle file, ParticleEffect3DParameter param) {
		ParticleEffect effect = new ParticleEffect();
		if (param != null && param.atlasFile != null)
			effect.load(file, am.get(param.atlasFile, TextureAtlas.class));
		else if (param != null && param.imagesDir != null)
			effect.load(file, param.imagesDir);
		else
			effect.load(file, file.parent());
		return effect;
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, ParticleEffect3DParameter param) {
		Array<AssetDescriptor> deps = null;
		if (param != null && param.atlasFile != null) {
			deps = new Array<AssetDescriptor>();
			deps.add(new AssetDescriptor<TextureAtlas>(param.atlasFile, TextureAtlas.class));
		}
		return deps;
	}

	/** Parameter to be passed to {@link AssetManager#load(String, Class, AssetLoaderParameters)} if additional configuration is
	 * necessary for the {@link ParticleEffect}. */
	public static class ParticleEffect3DParameter extends AssetLoaderParameters<ParticleEffect> 
	{
		/** Atlas file name. */
		public String atlasFile;
		/** Image directory. */
		public FileHandle imagesDir;
	}
}