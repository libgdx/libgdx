package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;

/**
 * Used by {@link Model} to load textures from {@link ModelData}.
 * @author badlogic
 *
 */
public interface TextureProvider {
	public Texture load(String fileName);
	
	public static class FileTextureProvider implements TextureProvider {
		@Override
		public Texture load (String fileName) {
			return new Texture(Gdx.files.internal(fileName));
		}
	}
	
	public static class AssetTextureProvider implements TextureProvider {
		public final AssetManager assetManager;
		public AssetTextureProvider(final AssetManager assetManager) {
			this.assetManager = assetManager;
		}
		@Override
		public Texture load (String fileName) {
			return assetManager.get(fileName, Texture.class);
		}
	}
}
