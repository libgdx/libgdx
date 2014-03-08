package com.badlogic.gdx.graphics.g3d.newparticles.controllers;

import com.badlogic.gdx.graphics.g3d.newparticles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleControllerParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.newparticles.influencers.Influencer;
import com.badlogic.gdx.graphics.g3d.newparticles.renderers.Renderer;
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
