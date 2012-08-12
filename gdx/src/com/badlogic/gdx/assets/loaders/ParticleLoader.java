package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

public class ParticleLoader extends SynchronousAssetLoader<ParticleEffect, ParticleLoader.ParticleParameter>{
	
	public ParticleLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public ParticleEffect load (AssetManager assetManager, String fileName,
			ParticleParameter parameter) {
		ParticleEffect effect = new ParticleEffect();
		FileHandle emitter = resolve(fileName);
		if(parameter == null ){
			String floderName = emitter.parent().toString();
			effect.load(emitter, resolve(floderName));
		}else{
			if(parameter.imgDir != null)
				effect.load(resolve(fileName), resolve(parameter.imgDir));
			else if(parameter.atlasPath != null){
				TextureAtlas atlas = new TextureAtlas(resolve(parameter.atlasPath));
				effect.loadEmitters(resolve(fileName));
				effect.loadEmitterImages(atlas);
			}else{
				String floderName = emitter.parent().toString();
				effect.load(emitter, resolve(floderName));
			}
		}
		return effect;
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName,
			ParticleParameter parameter) {
		return null;
	}
	
	/**
	 * 
	 * @author trung
	 */
	public static class ParticleParameter extends AssetLoaderParameters<ParticleEffect>{
		public String imgDir;
		public String atlasPath;
	}
}
