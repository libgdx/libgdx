package com.badlogic.gdx.maps;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Resolves an image by a string, wrapper around a Map or AssetManager
 * to load maps either directly or via AssetManager.
 * @author mzechner
 *
 */
public interface ImageResolver {
	/**
	 * @param name
	 * @return the Texture for the given image name or null.
	 */
	public Texture getImage(String name);
	
	public static class DirectImageResolver implements ImageResolver {
		private final ObjectMap<String, Texture> images;
		
		public DirectImageResolver(ObjectMap<String, Texture> images) {
			this.images = images;
		}

		@Override
		public Texture getImage (String name) {
			return images.get(name);
		}
	}
	
	public static class AssetManagerImageResolver implements ImageResolver {
		private final AssetManager assetManager;
		
		public AssetManagerImageResolver(AssetManager assetManager) {
			this.assetManager = assetManager;
		}
		
		@Override
		public Texture getImage (String name) {
			return assetManager.get(name, Texture.class);
		}
	}
}
