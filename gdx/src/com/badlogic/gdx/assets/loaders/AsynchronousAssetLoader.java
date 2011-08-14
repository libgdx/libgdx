
package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetManager;

public abstract class AsynchronousAssetLoader<T, P> extends AssetLoader<T, P> {
	public AsynchronousAssetLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	public abstract void loadAsync (AssetManager manager, String fileName, P parameter);

	public abstract T loadSync ();
}
