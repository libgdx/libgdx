package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetManager;

public interface AsynchronousAssetLoader<T,P> extends AssetLoader<T, P> {
	void loadAsync(AssetManager manager, String fileName, P parameter);
	T loadSync();
}
