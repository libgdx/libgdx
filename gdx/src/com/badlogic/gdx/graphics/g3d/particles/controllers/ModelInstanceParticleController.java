package com.badlogic.gdx.graphics.g3d.particles.controllers;

import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ModelInstanceParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;
import com.badlogic.gdx.math.Vector3;

/** A {@link ParticleController} which will handle {@link ModelInstanceParticle} particles. */
/** @author Inferno */
public class ModelInstanceParticleController extends ParticleController<ModelInstanceParticle> {
	private static final Vector3 	TMP_V1 = new Vector3(), 
											TMP_V2 = new Vector3();
	
	public ModelInstanceParticleController (){}
	
	public ModelInstanceParticleController (String name, Emitter<ModelInstanceParticle> emitter, ParticleBatch<ModelInstanceParticle> batch,
																											Influencer<ModelInstanceParticle>... influencers) {
		super(name, emitter, batch, influencers);
	}

	@Override
	public ModelInstanceParticle[] allocParticles (int count) {
		ModelInstanceParticle[] particles = new ModelInstanceParticle[count];
		for(int i=0; i <count; ++i)
			particles[i] = new ModelInstanceParticle();
		return particles;
	}

	@Override
	protected void initParticles () {
		for(ModelInstanceParticle particle : particles){
			particle.reset();
		}
	}
	
	@Override
	public void update (float dt) {
		super.update(dt);
		for (int i = 0, count = emitter.activeCount; i < count; ++i) {
			ModelInstanceParticle particle = particles[i];
			particle.instance.transform.set(	particle.instance.transform.getTranslation(TMP_V1), 
				particle.rotation, 
				TMP_V2.set(particle.scale, particle.scale, particle.scale));
		}
	}
	
	@Override
	public ParticleController copy () {
		Emitter emitter = (Emitter)this.emitter.copy();
		Influencer[] influencers = new Influencer[this.influencers.size];
		int i=0;
		for(Influencer influencer : this.influencers){
			influencers[i++] = (Influencer)influencer.copy();
		}
		return new ModelInstanceParticleController(new String(this.name), emitter, batch, influencers);
	}

	@Override
	protected void calculateBoundingBox () {
		boundingBox.clr();
		for(int i=0; i < emitter.activeCount; ++i){
			ModelInstanceParticle particle = particles[i];
			particle.instance.extendBoundingBox(boundingBox);
		}
	}
	
	@Override
	public boolean isCompatible (ParticleBatch batch) {
		return batch.getClass().isAssignableFrom(ModelInstanceParticleBatch.class);
	}
}
