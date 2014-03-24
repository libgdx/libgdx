package com.badlogic.gdx.graphics.g3d.particles.controllers;

import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerParticle;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;


/** A {@link ParticleController} which will handle {@link ParticleControllerParticle} particles. */
/** @author Inferno */
public class ParticleControllerParticleController extends ParticleController<ParticleControllerParticle> {
	protected static final Vector3 TMP_V1 = new Vector3(), TMP_V2 = new Vector3();
	protected static final Matrix4 TMP_M4 = new Matrix4();
	
	public ParticleControllerParticleController(){}
	
	public ParticleControllerParticleController (String name, Emitter<ParticleControllerParticle> emitter, 
																				 Influencer<ParticleControllerParticle>... influencers) {
		super(name, emitter, null, influencers);
	}

	@Override
	public ParticleControllerParticle[] allocParticles (int count) {
		ParticleControllerParticle[] particles = new ParticleControllerParticle[count];
		for(int i=0; i <count; ++i)
			particles[i] = new ParticleControllerParticle();
		return particles;
	}
	
	@Override
	protected void initParticles () {
		for(ParticleControllerParticle particle : particles){
			particle.reset();
		}
	}

	@Override
	public void update (float dt) {
		super.update(dt);
		for(int i=0; i< emitter.activeCount; ++i){
			ParticleControllerParticle particle = particles[i];
			particle.controller.setTransform( TMP_M4.set( particle.controller.transform.getTranslation(TMP_V1), 
				particle.rotation, 
				TMP_V2.set(particle.scale, particle.scale, particle.scale)) );
			particles[i].controller.update(deltaTime);
		}
	}
	
	@Override
	public void draw () {
		for(int i=0; i< emitter.activeCount; ++i){
			particles[i].controller.draw();
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
		return new ParticleControllerParticleController(new String(this.name), emitter, influencers);
	}

	@Override
	protected void calculateBoundingBox () {
		boundingBox.clr();
		for(int i=0; i < emitter.activeCount; ++i){
			ParticleControllerParticle particle = particles[i];
			boundingBox.ext(particle.controller.getBoundingBox());
		}
	}
	
	@Override
	public boolean isCompatible (ParticleBatch batch) {
		return false;
	}
}
