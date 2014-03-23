package com.badlogic.gdx.graphics.g3d.particles.controllers;

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.PointParticle;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardBatch;
import com.badlogic.gdx.graphics.g3d.particles.renderers.IParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.renderers.PointSpriteBatch;

public class PointSpriteParticleController extends ParticleController<PointParticle> {

	public PointSpriteParticleController(){}
	
	public PointSpriteParticleController (String name, Emitter<PointParticle> emitter, IParticleBatch<PointParticle> batch,
																											Influencer<PointParticle>... influencers) {
		super(name, emitter, batch, influencers);
	}

	@Override
	public PointParticle[] allocParticles (int count) {
		PointParticle[] particles = new PointParticle[count];
		for(int i=0; i <count; ++i)
			particles[i] = new PointParticle();
		return particles;
	}
	
	@Override
	protected void initParticles () {
		for(PointParticle particle : particles){
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
		return new PointSpriteParticleController(new String(this.name), emitter, batch, influencers);
	}

	@Override
	protected void calculateBoundingBox () {
		boundingBox.clr();
		for(int i=0; i < emitter.activeCount; ++i){
			PointParticle particle = particles[i];
			boundingBox.ext(particle.x, particle.y, particle.z);
		}
	}
	
	@Override
	public boolean isCompatible (IParticleBatch batch) {
		return batch.getClass().isAssignableFrom(PointSpriteBatch.class);
	}
}
