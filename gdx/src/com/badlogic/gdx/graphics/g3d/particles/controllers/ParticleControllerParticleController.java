package com.badlogic.gdx.graphics.g3d.particles.controllers;

import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerParticle;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.Renderer;
import com.badlogic.gdx.math.collision.BoundingBox;

public class ParticleControllerParticleController extends ParticleController<ParticleControllerParticle> {

	public ParticleControllerParticleController (String name, Emitter<ParticleControllerParticle> emitter, Renderer<ParticleControllerParticle> renderer,
																											Influencer<ParticleControllerParticle>... influencers) {
		super(name, emitter, renderer, influencers);
	}

	@Override
	public ParticleControllerParticle[] allocParticles (int count) {
		ParticleControllerParticle[] particles = new ParticleControllerParticle[count];
		for(int i=0; i <count; ++i)
			particles[i] = new ParticleControllerParticle();
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
		return new ParticleControllerParticleController(new String(this.name), emitter, renderer, influencers);
	}

	@Override
	protected void calculateBoundingBox () {
		boundingBox.clr();
		for(int i=0; i < emitter.activeCount; ++i){
			ParticleControllerParticle particle = particles[i];
			boundingBox.ext(particle.controller.getBoundingBox());
		}
	}
}
