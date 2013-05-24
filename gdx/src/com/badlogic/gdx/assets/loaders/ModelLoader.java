package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelTexture;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public abstract class ModelLoader<P extends AssetLoaderParameters<Model>> extends AsynchronousAssetLoader<Model, P> {
	public ModelLoader (FileHandleResolver resolver) {
		super(resolver);
	}
	
	protected Array<ObjectMap.Entry<String, ModelData>> items = new Array<ObjectMap.Entry<String, ModelData>>(); 
	
	protected abstract ModelData loadModelData(final FileHandle fileHandle, P parameters);
	
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, P parameters) {
		final Array<AssetDescriptor> deps = new Array();
		ModelData data = loadModelData(resolve(fileName), parameters);
		if (data == null)
			return deps;
		
		ObjectMap.Entry<String, ModelData> item = new ObjectMap.Entry<String, ModelData>();
		item.key = fileName;
		item.value = data;
		synchronized(items) {
			items.add(item);
		}
		
		for (final ModelMaterial modelMaterial : data.materials) {
			if (modelMaterial.textures != null) {
				for (final ModelTexture modelTexture : modelMaterial.textures)
					deps.add(new AssetDescriptor(modelTexture.fileName, Texture.class));
			}
		}
		return deps;
	}
	
	@Override
	public void loadAsync (AssetManager manager, String fileName, P parameters) {
	}

	@Override
	public Model loadSync (AssetManager manager, String fileName, P parameters) {
		ModelData data = null;
		synchronized(items) {
			for (int i = 0; i < items.size; i++) {
				if (items.get(i).key.equals(fileName)) {
					data = items.get(i).value;
					items.removeIndex(i);
				}
			}
		}
		if (data == null)
			return null;
		final Model result = new Model(data, new TextureProvider.AssetTextureProvider(manager));
		data = null;
		return result;
	}
}
