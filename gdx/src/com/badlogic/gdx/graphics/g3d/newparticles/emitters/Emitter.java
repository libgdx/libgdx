package com.badlogic.gdx.graphics.g3d.newparticles.emitters;

import com.badlogic.gdx.graphics.g3d.newparticles.ParticleController;
import com.badlogic.gdx.graphics.g3d.newparticles.ParticleSystem;

public abstract class  Emitter<T> extends ParticleSystem<T> {
	/** The min/max quantity of particles */
	public int minParticleCount, maxParticleCount = 4;
	
	/** Current number of active particles */
	public int activeCount = 0;

	/** Current state of the emission, should be currentTime/ duration
	 * Must be updated on each update */
	public float percent;
	
	public Emitter (Emitter<T> regularEmitter) {
		set(regularEmitter);
	}

	public Emitter () {}

	@Override
	public void init () {
		activeCount = 0;
	}
	
	public int getMinParticleCount () {
		return minParticleCount;
	}

	public void setMinParticleCount (int minParticleCount) {
		this.minParticleCount = minParticleCount;
	}

	public int getMaxParticleCount () {
		return maxParticleCount;
	}
	
	public void setMaxParticleCount (int maxParticleCount) {
		this.maxParticleCount = maxParticleCount;
	}
	
	public void setParticleCount(int aMin, int aMax){
		setMinParticleCount(aMin);
		setMaxParticleCount(aMax);
	}
	
	public void set(Emitter emitter){
		minParticleCount = emitter.minParticleCount;
		maxParticleCount = emitter.maxParticleCount;
	}
}
