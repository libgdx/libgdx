package com.badlogic.gdx.graphics.g3d.particles.controllers;

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.PointParticle;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.Renderer;
import com.badlogic.gdx.math.collision.BoundingBox;

public class PointParticleController extends ParticleController<PointParticle> {

	public PointParticleController (String name, Emitter<PointParticle> emitter, Renderer<PointParticle> renderer,
																											Influencer<PointParticle>... influencers) {
		super(name, emitter, renderer, influencers);
	}

	@Override
	public PointParticle[] allocParticles (int count) {
		PointParticle[] particles = new PointParticle[count];
		for(int i=0; i <count; ++i)
			particles[i] = new PointParticle();
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
		return new PointParticleController(new String(this.name), emitter, renderer, influencers);
	}

	@Override
	protected void calculateBoundingBox () {
		boundingBox.clr();
		for(int i=0; i < emitter.activeCount; ++i){
			PointParticle particle = particles[i];
			boundingBox.ext(particle.x, particle.y, particle.z);
		}
	}
}
