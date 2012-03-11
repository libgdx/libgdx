package com.badlogic.gdx.assets;

import java.util.ArrayList;

import com.badlogic.gdx.assets.loaders.AssetLoader;

public class AssetLoadingTask {
	AssetManager manager;
	public AssetDescriptor assetDesc;
	AssetLoader loader;
	public boolean cancel;
	public long startTime;
	public boolean dependenciesLoaded;
	public ArrayList<AssetDescriptor> dependencies;
	
	public AssetLoadingTask(AssetManager manager, AssetDescriptor desc, AssetLoader loader) {
		this.manager = manager;
		this.assetDesc = desc;
		this.loader = loader;
	}
	
	public boolean update () {
		return false;
	}
	public Object getAsset () {
		return null;
	}
}
