package com.badlogic.gdx.graphics.g3d.particles.controllers;

import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardBatch;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ParticleBatch;

/** A {@link ParticleController} which will handle {@link BillboardParticle} particles. */
/** @author Inferno */
public class BillboardParticleController extends ParticleController<BillboardParticle> {

	public BillboardParticleController(){}
	
	public BillboardParticleController (String name, Emitter<BillboardParticle> emitter, ParticleBatch<BillboardParticle> batch,
																											Influencer<BillboardParticle>... influencers) {
		super(name, emitter, batch, influencers);
	}

	@Override
	public BillboardParticle[] allocParticles (int count) {
		BillboardParticle[] particles = new BillboardParticle[count];
		for(int i=0; i <count; ++i)
			particles[i] = new BillboardParticle();
		return particles;
	}
	
	@Override
	protected void initParticles () {
		for(BillboardParticle particle : particles){
			particle.reset();
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
		return new BillboardParticleController(new String(this.name), emitter, batch, influencers);
	}

	@Override
	protected void calculateBoundingBox () {
		boundingBox.clr();
		for(int i=0; i < emitter.activeCount; ++i){
			BillboardParticle particle = particles[i];
			boundingBox.ext(particle.x, particle.y, particle.z);
		}
	}

	@Override
	public boolean isCompatible (ParticleBatch batch) {
		return batch.getClass().isAssignableFrom(BillboardBatch.class);
	}
}
