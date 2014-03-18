package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;

public abstract class ParticleControllerInfluencer<T> extends Influencer<T> {

	public static class ParticleControllerSingleInfluencer extends ParticleControllerInfluencer<ParticleControllerParticle>{

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
				controller.particles[i].controller.dispose();
			}
		}
		
		@Override
		public void activateParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				controller.particles[i].controller.start();
			}
		}

		@Override
		public ParticleControllerSingleInfluencer copy () {
			return new ParticleControllerSingleInfluencer(this);
		}
	}
	
	
	
	public static class ParticleControllerRandomInfluencer extends ParticleControllerInfluencer<ParticleControllerParticle>{
		private class ParticleControllerPool extends Pool<ParticleController>{
			public ParticleControllerPool () {}

			@Override
			public ParticleController newObject () {
				ParticleController controller = templates.random().copy();
				controller.init();
				return controller;
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
			//Allocate the new instances
			for(int i=0; i < controller.emitter.maxParticleCount; ++i){
				pool.free(pool.newObject());
			}
		}
		
		@Override
		public void end(){
			for(int i=0; i < controller.emitter.activeCount; ++i){
				ParticleControllerParticle particle = controller.particles[i];
				particle.controller.dispose();
				particle.controller = null;
			}
			
			//Dispose every allocated instance because the templates may be changed 
			for(int i=0, free = pool.getFree(); i < free; ++i){
				pool.obtain().dispose();
			}
		}

		@Override
		public void activateParticles (int startIndex, int count) {
			for(int i=startIndex, c = startIndex +count; i < c; ++i){
				ParticleControllerParticle particle = controller.particles[i];
				particle.controller = pool.obtain();
				particle.controller.start();
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
		this.templates = new Array<ParticleController>();
	}
	
	public ParticleControllerInfluencer(ParticleController... templates){
		this.templates = new Array<ParticleController>(templates);
	}
	
	public ParticleControllerInfluencer(ParticleControllerInfluencer<T> influencer){
		this(influencer.templates.items);
	}

	@Override
	public void write (Json json) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		// TODO Auto-generated method stub
		
	}
}
