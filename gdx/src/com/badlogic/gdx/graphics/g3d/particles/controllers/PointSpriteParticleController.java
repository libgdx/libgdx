package com.badlogic.gdx.graphics.g3d.particles.controllers;

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerParticle;
import com.badlogic.gdx.graphics.g3d.particles.PointSpriteParticle;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardBatch;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.renderers.PointSpriteBatch;

/** A {@link ParticleController} which will handle {@link PointSpriteParticle} particles. */
/** @author Inferno */
public class PointSpriteParticleController extends ParticleController<PointSpriteParticle> {

	public PointSpriteParticleController(){}
	
	public PointSpriteParticleController (String name, Emitter<PointSpriteParticle> emitter, ParticleBatch<PointSpriteParticle> batch,
																											Influencer<PointSpriteParticle>... influencers) {
		super(name, emitter, batch, influencers);
	}

	@Override
	public PointSpriteParticle[] allocParticles (int count) {
		PointSpriteParticle[] particles = new PointSpriteParticle[count];
		for(int i=0; i <count; ++i)
			particles[i] = new PointSpriteParticle();
		return particles;
	}
	
	@Override
	protected void initParticles () {
		for(PointSpriteParticle particle : particles){
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
			PointSpriteParticle particle = particles[i];
			boundingBox.ext(particle.x, particle.y, particle.z);
		}
	}
	
	@Override
	public boolean isCompatible (ParticleBatch batch) {
		return batch.getClass().isAssignableFrom(PointSpriteBatch.class);
	}
}
