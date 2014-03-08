package com.badlogic.gdx.graphics.g3d.particles.controllers;

import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.Renderer;
import com.badlogic.gdx.math.collision.BoundingBox;

public class BillboardParticleController extends ParticleController<BillboardParticle> {

	public BillboardParticleController (String name, Emitter<BillboardParticle> emitter, Renderer<BillboardParticle> renderer,
																											Influencer<BillboardParticle>... influencers) {
		super(name, emitter, renderer, influencers);
	}

	@Override
	public BillboardParticle[] allocParticles (int count) {
		BillboardParticle[] particles = new BillboardParticle[count];
		for(int i=0; i <count; ++i)
			particles[i] = new BillboardParticle();
		return particles;
	}

	@Override
	public ParticleController copy () {
		Emitter emitter = (Emitter)this.emitter.copy();
		Renderer renderer = (Renderer)this.renderer.copy();
		Influencer[] influencers = new Influencer[this.influencers.size];
		int i=0;
		for(Influencer influencer : this.influencers){
			influencers[i++] = (Influencer)influencer.copy();
		}
		return new BillboardParticleController(new String(this.name), emitter, renderer, influencers);
	}

	@Override
	protected void calculateBoundingBox () {
		boundingBox.clr();
		for(int i=0; i < emitter.activeCount; ++i){
			BillboardParticle particle = particles[i];
			boundingBox.ext(particle.x, particle.y, particle.z);
		}
	}
}
