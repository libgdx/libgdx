package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.particles.AspectTextureRegion;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect.ParticleEffectData;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;

public abstract class ModelInfluencer<T> extends Influencer<T> {

	public static class ModelInstanceSingleInfluencer extends ModelInfluencer<ModelInstanceParticle>{

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
	
	public static class ModelInstanceRandomInfluencer extends ModelInfluencer<ModelInstanceParticle>{
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
		public void activateParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ModelInstanceParticle particle = controller.particles[i];
				particle.instance = pool.obtain();
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
		public void end () {
			pool.clear();
			for(int i=0; i < controller.emitter.activeCount; ++i){
				controller.particles[i].instance = null;
			}
		}
		
		@Override
		public ModelInstanceRandomInfluencer copy () {
			return new ModelInstanceRandomInfluencer(this);
		}
	}
	
	
	public Array<Model> models;
	
	public ModelInfluencer(){
		this.models = new Array<Model>();
	}
	
	public ModelInfluencer(Model...models){
		this.models = new Array<Model>(models);
	}
	
	public ModelInfluencer (ModelInfluencer<T> influencer) {
		this(influencer.models.items);
	}

	@Override
	public void write (Json json) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	@Override
	public void write (Json json) {
		AspectTextureRegion[] regionsCopy = new AspectTextureRegion[models.size];
		System.arraycopy(models.items, 0, regionsCopy, 0, models.size);
		json.writeValue("models", regionsCopy);
	}
	
	@Override
	public void read (Json json, JsonValue jsonData) {
		models.items = json.readValue("models", AspectTextureRegion[].class, jsonData);
		models.size = models.items.length;
	}
	*/
	
}
