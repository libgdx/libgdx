package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.Particle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class RandomModelInfluencer extends Influencer<ModelInstanceParticle> {
	private class ModelInstancePool extends Pool<ModelInstance>{
		public ModelInstancePool () {}

		@Override
		public ModelInstance newObject () {
			return new ModelInstance(RandomModelInfluencer.this.models.get(MathUtils.random(RandomModelInfluencer.this.models.size -1)));
		}
	}
	
	Array<Model> models;
	ModelInstancePool instances;
	
	/** All the regions must be defined on the same Texture */
	public RandomModelInfluencer(Model...models){
		this.models = new Array<Model>(models);
		instances = new ModelInstancePool();
	}
	
	@Override
	public void init () {
		//Dispose every allocated instance because the templates may be changed 
		instances.clear();
		
		for(int i=0; i < controller.emitter.maxParticleCount; ++i)
			instances.free(instances.newObject());
	}

	@Override
	public void initParticles (int startIndex, int count) {
		for(int i=startIndex, c = startIndex +count; i < c; ++i){
			ModelInstanceParticle particle = controller.particles[i];
			particle.instance = instances.obtain();
		}
	}
	@Override
	public void killParticles (int startIndex, int count) {
		for(int i=startIndex, c = startIndex +count; i < c; ++i){
			ModelInstanceParticle particle = controller.particles[i];
			instances.free(particle.instance);
			particle.instance = null;
		}
	}

	@Override
	public RandomModelInfluencer copy () {
		return new RandomModelInfluencer(models.items);
	}

	public void set (RandomModelInfluencer system) {
		models.clear();
		models.addAll(system.models);
	}
}
