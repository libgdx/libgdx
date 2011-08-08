package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.utils.Array;

public interface AssetLoader<T, P> {
	Array<AssetDescriptor> getDependencies(String fileName, P parameter);
}
