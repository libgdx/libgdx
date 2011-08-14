
package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public abstract class AssetLoader<T, P> {
	private FileHandleResolver resolver;

	public AssetLoader (FileHandleResolver resolver) {
		this.resolver = resolver;
	}

	public FileHandle resolve (String fileName) {
		return resolver.resolve(fileName);
	}

	public abstract Array<AssetDescriptor> getDependencies (String fileName, P parameter);
}
