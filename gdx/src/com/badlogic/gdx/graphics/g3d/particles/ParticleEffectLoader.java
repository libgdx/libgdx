package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect.ParticleEffectData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

public class ParticleEffectLoader extends AsynchronousAssetLoader<ParticleEffect, ParticleEffectLoader.ParticleEffectParameter> {
	public static class ParticleEffectParameter extends AssetLoaderParameters<ParticleEffect> {}
	
	protected Array<ObjectMap.Entry<String, ParticleEffect>> items = new Array<ObjectMap.Entry<String, ParticleEffect>>(); 
	
	public ParticleEffectLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle file, ParticleEffectParameter parameter) {}

	@Override
	public ParticleEffect loadSync (AssetManager manager, String fileName, FileHandle file, ParticleEffectParameter parameter) {
		ParticleEffect effect = null;
		synchronized(items) {
			for(int i=0; i < items.size; ++i){
				ObjectMap.Entry<String, ParticleEffect> entry = items.get(i);
				if(entry.key.equals(fileName)){
					effect = entry.value;
					items.removeIndex(i);
					break;
				}
			}
		}
		
		effect.loadAssets(manager);
		effect.setData(null);
		return effect;
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, ParticleEffectParameter parameter) {
		Array<AssetDescriptor> deps = new Array<AssetDescriptor>();
		Json json = new Json();
		ParticleEffect effect = json.fromJson(ParticleEffect.class, file);
		synchronized (items) {
			ObjectMap.Entry<String, ParticleEffect> entry = new ObjectMap.Entry<String, ParticleEffect>();
			entry.key = fileName;
			entry.value = effect;
			items.add(entry);
			deps.addAll(effect.getData().getAssets());
		}
		return deps;
	}
	
}
