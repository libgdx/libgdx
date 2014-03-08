package com.badlogic.gdx.graphics.g3d.newparticles.influencers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.newparticles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.Particle;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleControllerParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.renderers.BillboardRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class RandomParticleControllerInfluencer extends Influencer<ParticleControllerParticle> {
	private class ParticleControllerPool extends Pool<ParticleController>{
		public ParticleControllerPool () {}

		@Override
		public ParticleController newObject () {
			ParticleController controller = RandomParticleControllerInfluencer.this.templates.get(MathUtils.random(RandomParticleControllerInfluencer.this.templates.size -1)).copy();
			controller.init();
			return controller;
		}
	}
	Array<ParticleController> templates;
	ParticleControllerPool instances;
	
	/** All the regions must be defined on the same Texture */
	public RandomParticleControllerInfluencer(ParticleController...templates){
		this.templates = new Array<ParticleController>(templates);
		instances = new ParticleControllerPool();
	}

	@Override
	public void init () {
		//Dispose every allocated instance because the templates may be changed 
		for(int i=0, free = instances.getFree(); i < free; ++i){
			instances.obtain().dispose();
		}
		
		//Allocate the new instances
		for(int i=0; i < controller.emitter.maxParticleCount; ++i)
			instances.free(instances.newObject());
	}

	@Override
	public void initParticles (int startIndex, int count) {
		for(int i=startIndex, c = startIndex +count; i < c; ++i){
			ParticleControllerParticle particle = controller.particles[i];
			particle.controller = instances.obtain();
			particle.controller.start();
		}
	}
	
	@Override
	public void killParticles (int startIndex, int count) {
		for(int i=startIndex, c = startIndex +count; i < c; ++i){
			ParticleControllerParticle particle = controller.particles[i];
			instances.free(particle.controller);
			particle.controller = null;
		}
	}

	@Override
	public RandomParticleControllerInfluencer copy () {
		return new RandomParticleControllerInfluencer(templates.items);
	}

	public void set (RandomParticleControllerInfluencer system) {
		templates.clear();
		templates.addAll(system.templates);
	}
}
