package com.badlogic.gdx.assets.loaders;

public interface SynchronousAssetLoader<T, P> extends AssetLoader<T, P>{
	T load(String fileName, P parameter);
}
