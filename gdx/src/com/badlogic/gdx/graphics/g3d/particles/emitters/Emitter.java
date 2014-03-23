package com.badlogic.gdx.graphics.g3d.particles.emitters;

import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public abstract class  Emitter<T> extends ParticleSystem<T> implements Json.Serializable{
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
	
	@Override
	public void end () {
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
	
	@Override
	public void write (Json json) {
		json.writeValue("minParticleCount", minParticleCount);
		json.writeValue("maxParticleCount", maxParticleCount);
	}
	
	@Override
	public void read (Json json, JsonValue jsonData) {
		minParticleCount = json.readValue("minParticleCount", Integer.class, jsonData);
		maxParticleCount = json.readValue("maxParticleCount", Integer.class, jsonData);
	}

}
