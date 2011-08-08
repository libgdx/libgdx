package com.badlogic.gdx.assets.loaders;

public interface AsynchronousAssetLoader<T,P> extends AssetLoader<T, P> {
	void loadAsync(String fileName, P parameter);
	T loadSync();
}
