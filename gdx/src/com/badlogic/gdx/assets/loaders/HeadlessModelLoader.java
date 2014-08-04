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
import com.badlogic.gdx.graphics.g3d.HeadlessModel;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.Iterator;

public abstract class HeadlessModelLoader<P extends HeadlessModelLoader.HeadlessModelParameters> extends AsynchronousAssetLoader<HeadlessModel, P> {
	public HeadlessModelLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	protected Array<ObjectMap.Entry<String, ModelData>> items = new Array<ObjectMap.Entry<String, ModelData>>();
	protected HeadlessModelParameters defaultParameters = new HeadlessModelParameters();

	/** Directly load the raw HeadlessModel data on the calling thread. */
	public abstract ModelData loadModelData (final FileHandle fileHandle, P parameters);

	/** Directly load the raw HeadlessModel data on the calling thread. */
	public ModelData loadModelData (final FileHandle fileHandle) {
		return loadModelData(fileHandle, null);
	}

	/** Directly load the HeadlessModel on the calling thread. The HeadlessModel with not be managed by an {@link com.badlogic.gdx.assets.AssetManager}. */
	public HeadlessModel loadHeadlessModel (final FileHandle fileHandle, TextureProvider textureProvider, P parameters) {
		final ModelData data = loadModelData(fileHandle, parameters);
		return data == null ? null : new HeadlessModel(data, textureProvider);
	}

	/** Directly load the HeadlessModel on the calling thread. The HeadlessModel with not be managed by an {@link com.badlogic.gdx.assets.AssetManager}. */
	public HeadlessModel loadHeadlessModel (final FileHandle fileHandle, P parameters) {
		return loadHeadlessModel(fileHandle, new TextureProvider.FileTextureProvider(), parameters);
	}

	/** Directly load the HeadlessModel on the calling thread. The HeadlessModel with not be managed by an {@link com.badlogic.gdx.assets.AssetManager}. */
	public HeadlessModel loadHeadlessModel (final FileHandle fileHandle, TextureProvider textureProvider) {
		return loadHeadlessModel(fileHandle, textureProvider, null);
	}

	/** Directly load the HeadlessModel on the calling thread. The HeadlessModel with not be managed by an {@link com.badlogic.gdx.assets.AssetManager}. */
	public HeadlessModel loadHeadlessModel (final FileHandle fileHandle) {
		return loadHeadlessModel(fileHandle, new TextureProvider.FileTextureProvider(), null);
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, P parameters) {
		final Array<AssetDescriptor> deps = new Array();
		ModelData data = loadModelData(file, parameters);
		if (data == null) return deps;

		ObjectMap.Entry<String, ModelData> item = new ObjectMap.Entry<String, ModelData>();
		item.key = fileName;
		item.value = data;
		synchronized (items) {
			items.add(item);
		}

		/*TextureLoader.TextureParameter textureParameter = (parameters != null)
				? parameters.textureParameter
				: defaultParameters.textureParameter;

		for (final ModelMaterial modelMaterial : data.materials) {
			if (modelMaterial.textures != null) {
				for (final ModelTexture modelTexture : modelMaterial.textures)
					deps.add(new AssetDescriptor(modelTexture.fileName, Texture.class, textureParameter));
			}
		}*/
		return deps;
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle file, P parameters) {
	}

	@Override
	public HeadlessModel loadSync (AssetManager manager, String fileName, FileHandle file, P parameters) {
		ModelData data = null;
		synchronized (items) {
			for (int i = 0; i < items.size; i++) {
				if (items.get(i).key.equals(fileName)) {
					data = items.get(i).value;
					items.removeIndex(i);
				}
			}
		}
		if (data == null) return null;
		final HeadlessModel result = new HeadlessModel(data, new TextureProvider.AssetTextureProvider(manager));
		// need to remove the textures from the managed disposables, or else ref counting
		// doesn't work!
		Iterator<Disposable> disposables = result.getManagedDisposables().iterator();
		while (disposables.hasNext()) {
			Disposable disposable = disposables.next();
			if (disposable instanceof Texture) {
				disposables.remove();
			}
		}
		data = null;
		return result;
	}

	static public class HeadlessModelParameters extends AssetLoaderParameters<HeadlessModel> {
		public TextureLoader.TextureParameter textureParameter;

		public HeadlessModelParameters() {
			textureParameter = new TextureLoader.TextureParameter();
			textureParameter.minFilter = textureParameter.magFilter = Texture.TextureFilter.Linear;
			textureParameter.wrapU = textureParameter.wrapV = Texture.TextureWrap.Repeat;
		}
	}
}
