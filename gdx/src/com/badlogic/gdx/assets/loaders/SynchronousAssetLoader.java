
package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetManager;

public abstract class SynchronousAssetLoader<T, P> extends AssetLoader<T, P> {
	public SynchronousAssetLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	public abstract T load (AssetManager assetManager, String fileName, P parameter);
}
