package com.badlogic.gdx.graphics.g3d.particles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData.AssetData;
import com.badlogic.gdx.graphics.g3d.particles.renderers.IParticleBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

public class ParticleEffectLoader extends AsynchronousAssetLoader<ParticleEffect, ParticleEffectLoader.ParticleEffectLoadParameter> {
	protected Array<ObjectMap.Entry<String, ResourceData<ParticleEffect>>> items = new Array<ObjectMap.Entry<String, ResourceData<ParticleEffect>>>(); 
	
	public ParticleEffectLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle file, ParticleEffectLoadParameter parameter) {}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, ParticleEffectLoadParameter parameter) {
		Json json = new Json();
		ResourceData data = json.fromJson(ResourceData.class, file);
		Array<AssetData> assets = null;
		synchronized (items) {
			ObjectMap.Entry<String, ResourceData<ParticleEffect>> entry = new ObjectMap.Entry<String, ResourceData<ParticleEffect>>();
			entry.key = fileName;
			entry.value = data;
			items.add(entry);
			assets = data.getAssets();
		}
		
		//Handle the Loading of ParticleEffect as resources
		Array<AssetDescriptor> descriptors = new Array<AssetDescriptor>();
		for(AssetData assetData : assets){
			if(assetData.type == ParticleEffect.class){
				descriptors.add(new AssetDescriptor(assetData.filename, assetData.type, parameter));
			}
			else 
				descriptors.add(new AssetDescriptor(assetData.filename, assetData.type));
		}
		
		return descriptors;
		
	}
	
	public void save(ParticleEffect effect, ParticleEffectSaveParameter parameter) throws IOException{
		ResourceData<ParticleEffect> data =  new ResourceData<ParticleEffect>(effect);
		
		//effect assets
		effect.save(parameter.manager, data);
		
		//Batch cfgs
		if(parameter.batches != null){
			for(IParticleBatch batch : parameter.batches){
				batch.save(parameter.manager, data);
			}
		}
		
		//save
		Writer fileWriter = new FileWriter(parameter.file);
		Json json = new Json();
		json.toJson(data, fileWriter);
		System.out.println(json.prettyPrint(data));
	}
	
	@Override
	public ParticleEffect loadSync (AssetManager manager, String fileName, FileHandle file, ParticleEffectLoadParameter parameter) {
		ResourceData<ParticleEffect> effectData = null;
		synchronized(items) {
			for(int i=0; i < items.size; ++i){
				ObjectMap.Entry<String, ResourceData<ParticleEffect>> entry = items.get(i);
				if(entry.key.equals(fileName)){
					effectData = entry.value;
					items.removeIndex(i);
					break;
				}
			}
		}

		effectData.resource.load(manager, effectData);
		if(parameter != null){
			if(parameter.batches != null){
				for(IParticleBatch batch : parameter.batches){
					batch.load(manager, effectData);
				}
			}
			effectData.resource.setBatch(parameter.batches.items);	
		}
		return effectData.resource;
	}
	
	private <T> T find(Array array, Class<T> type){
		for(Object object : array){
			if(type.isAssignableFrom(object.getClass()))
				return (T)object;
		}
		return null;
	}
	
	public static class ParticleEffectLoadParameter extends AssetLoaderParameters<ParticleEffect> {
		Array<IParticleBatch> batches;
		public ParticleEffectLoadParameter(IParticleBatch...batches){
			this.batches = new Array<IParticleBatch>(batches);
		}
	}
	
	public static class ParticleEffectSaveParameter extends AssetLoaderParameters<ParticleEffect> {
		/**optional params, but should be present to correctly load the settings*/
		Array<IParticleBatch> batches;
		
		/** required params */
		File file;
		AssetManager manager;
		public ParticleEffectSaveParameter(File file, AssetManager manager, IParticleBatch... batches){
			this.batches = new Array<IParticleBatch>(batches);
			this.file = file;
			this.manager = manager;
		}
	}
	
}
