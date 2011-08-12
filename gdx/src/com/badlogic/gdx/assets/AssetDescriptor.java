package com.badlogic.gdx.assets;

/**
 * Describes an asset to be loaded by it's filename, type and {@link AssetLoaderParameters}.
 * Instances of this are used in {@link AssetLoadingTask} to load the actual asset.
 * @author mzechner
 *
 */
public class AssetDescriptor {
	final String fileName;
	final Class type;
	final AssetLoaderParameters params;
	
	public AssetDescriptor(String fileName, Class assetType, AssetLoaderParameters params) {
		this.fileName = fileName.replaceAll("\\\\", "/");
		this.type = assetType;
		this.params = params;
	}
	
	@Override
	public String toString() {
		return fileName;
	}
}
