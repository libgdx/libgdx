package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData.SaveData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public abstract class ModelInfluencer extends Influencer<ModelInstanceParticle> {

	public static class ModelInstanceSingleInfluencer extends ModelInfluencer{

		public ModelInstanceSingleInfluencer(){
			super();
		}
		
		public ModelInstanceSingleInfluencer(ModelInstanceSingleInfluencer influencer){
			super(influencer);
		}
		
		public ModelInstanceSingleInfluencer (Model...models) {
			super(models);
		}

		@Override
		public void init () {
			Model first = models.first();
			for(int i=0; i < controller.emitter.maxParticleCount; ++i){
				controller.particles[i].instance = new ModelInstance(first);
			}
		}
		
		@Override
		public ModelInstanceSingleInfluencer copy () {
			return new ModelInstanceSingleInfluencer(this);
		}
	}
	
	public static class ModelInstanceRandomInfluencer extends ModelInfluencer{
		private class ModelInstancePool extends Pool<ModelInstance>{
			public ModelInstancePool () {}

			@Override
			public ModelInstance newObject () {
				return new ModelInstance(models.random());
			}
		}
		
		ModelInstancePool pool;
		public ModelInstanceRandomInfluencer(){
			super();
			pool = new ModelInstancePool();
		}
		
		public ModelInstanceRandomInfluencer(ModelInstanceRandomInfluencer influencer){
			super(influencer);
			pool = new ModelInstancePool();
		}
		
		public ModelInstanceRandomInfluencer (Model...models) {
			super(models);
			pool = new ModelInstancePool();
		}
		
		@Override
		public void init () {
			pool.clear();
		}
		
		@Override
		public void activateParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				controller.particles[i].instance = pool.obtain();
			}
		}
		@Override
		public void killParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				pool.free(particle.instance);
				particle.instance = null;
			}
		}
		
		@Override
		public ModelInstanceRandomInfluencer copy () {
			return new ModelInstanceRandomInfluencer(this);
		}
	}
	
	
	public Array<Model> models;
	
	public ModelInfluencer(){
		this.models = new Array<Model>(true, 1, Model.class);
	}
	
	public ModelInfluencer(Model...models){
		this.models = new Array<Model>(models);
	}
	
	public ModelInfluencer (ModelInfluencer influencer) {
		this((Model[])influencer.models.toArray(Model.class));
	}

	@Override
	public void save (AssetManager manager, ResourceData resources) {
		SaveData data = resources.createSaveData();
		for(Model model : models)
			data.saveAsset(manager.getAssetFileName(model), Model.class);
	}
	
	@Override
	public void load (AssetManager manager, ResourceData resources) {
		SaveData data = resources.getSaveData();
		AssetDescriptor descriptor;
		while((descriptor = data.loadAsset()) != null){
			Model model = (Model)manager.get(descriptor);
			if(model == null)
				throw new RuntimeException("Model is null");
			models.add(model);
		}
	}
}
