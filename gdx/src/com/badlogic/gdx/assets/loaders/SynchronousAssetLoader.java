package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetManager;

public interface SynchronousAssetLoader<T, P> extends AssetLoader<T, P>{
	T load(AssetManager assetManager, String fileName, P parameter);
}
