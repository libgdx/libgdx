package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3djModelLoader;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelTexture;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.utils.Array;

public abstract class ModelLoader<P extends AssetLoaderParameters<Model>> extends AsynchronousAssetLoader<Model, P> {
	public ModelLoader (FileHandleResolver resolver) {
		super(resolver);
	}
	
	protected ModelData data;

	protected abstract ModelData loadModelData(final FileHandle fileHandle, P parameters);
	
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, P parameters) {
		final Array<AssetDescriptor> deps = new Array();
		data = loadModelData(resolve(fileName), parameters);
		if (data == null)
			return deps;
		
		for (final ModelMaterial modelMaterial : data.materials) {
			if (modelMaterial.diffuseTextures != null) {
				for (final ModelTexture modelTexture : modelMaterial.diffuseTextures) {
					deps.add(new AssetDescriptor(modelTexture.fileName, Texture.class));
				}
			}
		}
		return deps;
	}
	
	@Override
	public void loadAsync (AssetManager manager, String fileName, P parameters) {
	}

	@Override
	public Model loadSync (AssetManager manager, String fileName, P parameters) {
		if (data == null)
			return null;
		final Model result = new Model(data, new TextureProvider.AssetTextureProvider(manager));
		data = null;
		return result;
	}
}
