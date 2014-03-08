package com.badlogic.gdx.graphics.g3d.newparticles.controllers;

import com.badlogic.gdx.graphics.g3d.newparticles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.ModelInstanceParticle;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.newparticles.influencers.Influencer;
import com.badlogic.gdx.graphics.g3d.newparticles.renderers.Renderer;
import com.badlogic.gdx.math.collision.BoundingBox;

public class ModelInstanceParticleController extends ParticleController<ModelInstanceParticle> {

	public ModelInstanceParticleController (String name, Emitter<ModelInstanceParticle> emitter, Renderer<ModelInstanceParticle> renderer,
																											Influencer<ModelInstanceParticle>... influencers) {
		super(name, emitter, renderer, influencers);
	}

	@Override
	public ModelInstanceParticle[] allocParticles (int count) {
		ModelInstanceParticle[] particles = new ModelInstanceParticle[count];
		for(int i=0; i <count; ++i)
			particles[i] = new ModelInstanceParticle();
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
		return new ModelInstanceParticleController(new String(this.name), emitter, renderer, influencers);
	}

	@Override
	protected void calculateBoundingBox () {
		boundingBox.clr();
		for(int i=0; i < emitter.activeCount; ++i){
			ModelInstanceParticle particle = particles[i];
			particle.instance.extendBoundingBox(boundingBox);
		}
	}
}
