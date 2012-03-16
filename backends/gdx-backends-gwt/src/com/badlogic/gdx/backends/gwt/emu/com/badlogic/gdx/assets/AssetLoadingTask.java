package com.badlogic.gdx.assets;

import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.utils.Array;

public class AssetLoadingTask {
	AssetManager manager;
	public AssetDescriptor assetDesc;
	AssetLoader loader;
	boolean cancel;
	long startTime;
	boolean dependenciesLoaded;
	Array<AssetDescriptor> dependencies;
	Object asset;
	
	public AssetLoadingTask(AssetManager manager, AssetDescriptor desc, AssetLoader loader) {
		this.manager = manager;
		this.assetDesc = desc;
		this.loader = loader;
	}
	
	public boolean update () {
		if(loader instanceof AsynchronousAssetLoader) {
			handleAsynchLoader((AsynchronousAssetLoader)loader);
		} else {
			handleSynchLoader((SynchronousAssetLoader)loader);
		}
		return asset != null;
	}
	
	private void handleSynchLoader (SynchronousAssetLoader loader) {
		if(!dependenciesLoaded) {
			Array<AssetDescriptor> dependencies = loader.getDependencies(assetDesc.fileName, assetDesc.params);
			if (dependencies != null) {
				for (AssetDescriptor desc : dependencies) {
					manager.injectDependency(assetDesc.fileName, desc);
				}
				dependenciesLoaded = true;
			} else {
				// if we have no dependencies, we load the async part of the task immediately.
				asset = loader.load(manager, assetDesc.fileName, assetDesc.params);
			}
		} else {
			asset = loader.load(manager, assetDesc.fileName, assetDesc.params);
		}
	}

	private void handleAsynchLoader(AsynchronousAssetLoader loader) {
		if(!dependenciesLoaded) {
			Array<AssetDescriptor> dependencies = loader.getDependencies(assetDesc.fileName, assetDesc.params);
			if (dependencies != null) {
				for (AssetDescriptor desc : dependencies) {
					manager.injectDependency(assetDesc.fileName, desc);
				}
				dependenciesLoaded = true;
			} else {
				// we can load everything we are always on the rendering thread.
				loader.loadAsync(manager, assetDesc.fileName, assetDesc.params);
				asset = loader.loadSync(manager, assetDesc.fileName, assetDesc.params);
			}
		} else {
			loader.loadAsync(manager, assetDesc.fileName, assetDesc.params);
			asset = loader.loadSync(manager, assetDesc.fileName, assetDesc.params);
		}
	}
	
	public Object getAsset () {
		return asset;
	}
}
