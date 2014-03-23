package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData.SaveData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.Pool;

public abstract class ParticleControllerInfluencer extends Influencer<ParticleControllerParticle> {

	public static class ParticleControllerSingleInfluencer extends ParticleControllerInfluencer{

		public ParticleControllerSingleInfluencer (ParticleController... templates) {
			super(templates);
		}
		
		public ParticleControllerSingleInfluencer (){
			super();
		}
			
		public ParticleControllerSingleInfluencer (ParticleControllerSingleInfluencer particleControllerSingle) {
			super(particleControllerSingle);
		}

		@Override
		public void init () {
			ParticleController first = templates.first();
			for(int i=0, c = controller.particles.length; i < c; ++i){
				ParticleControllerParticle particle = controller.particles[i];
				particle.controller = first.copy();
				particle.controller.init();
			}
		}
		
		@Override
		public void end(){
			for(int i=0; i < controller.emitter.activeCount; ++i){
				controller.particles[i].controller.end();
			}
		}
		
		@Override
		public void activateParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				controller.particles[i].controller.reset();
			}
		}

		@Override
		public ParticleControllerSingleInfluencer copy () {
			return new ParticleControllerSingleInfluencer(this);
		}
	}
	
	
	
	public static class ParticleControllerRandomInfluencer extends ParticleControllerInfluencer{
		private class ParticleControllerPool extends Pool<ParticleController>{
			public ParticleControllerPool () {}

			@Override
			public ParticleController newObject () {
				ParticleController controller = templates.random().copy();
				controller.init();
				return controller;
			}
			
			@Override
			public void clear () {
				//Dispose every allocated instance because the templates may be changed 
				for(int i=0, free = pool.getFree(); i < free; ++i){
					pool.obtain().dispose();
				}
				super.clear();
			}
		}
		
		ParticleControllerPool pool;
		
		public ParticleControllerRandomInfluencer (){
			super();
			pool = new ParticleControllerPool();
		}
		public ParticleControllerRandomInfluencer (ParticleController... templates) {
			super(templates);
			pool = new ParticleControllerPool();
		}

		public ParticleControllerRandomInfluencer (ParticleControllerRandomInfluencer particleControllerRandom) {
			super(particleControllerRandom);
			pool = new ParticleControllerPool();
		}
		
		@Override
		public void init () {
			pool.clear();
			//Allocate the new instances
			for(int i=0; i < controller.emitter.maxParticleCount; ++i){
				pool.free(pool.newObject());
			}
		}
		
		@Override
		public void end(){
			for(int i=0; i < controller.emitter.activeCount; ++i){
				ParticleControllerParticle particle = controller.particles[i];
				particle.controller.end();
			}
		}
		
		@Override
		public void dispose(){
			pool.clear();
			super.dispose();
		}

		@Override
		public void activateParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ParticleControllerParticle particle = controller.particles[i];
				particle.controller = pool.obtain();
				particle.controller.reset();
			}
		}
		
		@Override
		public void killParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ParticleControllerParticle particle = controller.particles[i];
				pool.free(particle.controller);
				particle.controller = null;
			}
		}

		@Override
		public ParticleControllerRandomInfluencer copy () {
			return new ParticleControllerRandomInfluencer(this);
		}
	}
	
	
	
	
	public Array<ParticleController> templates;
	
	public ParticleControllerInfluencer(){
		this.templates = new Array<ParticleController>(true, 1, ParticleController.class);
	}
	
	public ParticleControllerInfluencer(ParticleController... templates){
		this.templates = new Array<ParticleController>(templates);
	}
	
	public ParticleControllerInfluencer(ParticleControllerInfluencer influencer){
		this(influencer.templates.items);
	}
	
	@Override
	public void dispose () {
		if(controller != null){
			for(int i=0; i < controller.particles.length; ++i){
				ParticleControllerParticle particle = controller.particles[i];
				if(particle != null){
					particle.controller.dispose();
					particle.controller = null;
				}
			}
		}
	}

	@Override
	public void save (AssetManager manager, ResourceData resources) {
		SaveData data = resources.createSaveData();
		Array<ParticleEffect> effects = manager.get(ParticleEffect.class, new Array<ParticleEffect>());
		ObjectMap<String, Array<Integer>> controllerIndices = new ObjectMap<String, Array<Integer>>();
		
		for(ParticleController controller : templates){
			for(ParticleEffect effect : effects){
				int index = -1;
				if( (index = effect.getControllers().indexOf(controller, true)) >-1){
					String effectFilename = manager.getAssetFileName(effect);
					data.saveAsset(effectFilename, ParticleEffect.class);
					Array<Integer> indices= controllerIndices.get(effectFilename);
					if(indices == null){
						indices = new Array<Integer>();
						controllerIndices.put(effectFilename, indices);
					}
					indices.add(index);
					break;
				}
			}
		}
		data.save("indices", controllerIndices);
	}
	
	@Override
	public void load (AssetManager manager, ResourceData resources) {
		SaveData data = resources.getSaveData();
		AssetDescriptor descriptor;
		ObjectMap<String, Array<Integer>> indices = (ObjectMap<String, Array<Integer>>)data.load("indices");
		while((descriptor = data.loadAsset()) != null){
			ParticleEffect effect = (ParticleEffect)manager.get(descriptor);
			if(effect == null)
				throw new RuntimeException("Template is null");
			Array<Integer> controllerIndices = indices.get(descriptor.fileName);
			Array<ParticleController> effectControllers = effect.getControllers();
			for(Integer index : controllerIndices ){
				if(index < effectControllers.size)
					templates.add(effectControllers.get(index));
			}
		}
	}
}
