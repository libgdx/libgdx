/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.g3d.particles.emitters;

import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerComponent;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** An {@link Emitter} is a {@link ParticleControllerComponent} which will handle the particles emission. It must update the
 * {@link Emitter#percent} to reflect the current percentage of the current emission cycle. It should consider
 * {@link Emitter#minParticleCount} and {@link Emitter#maxParticleCount} to rule particle emission. It should notify the particle
 * controller when particles are activated, killed, or when an emission cycle begins.
 * @author Inferno */
public abstract class Emitter extends ParticleControllerComponent implements Json.Serializable {
	/** The min/max quantity of particles */
	public int minParticleCount, maxParticleCount = 4;

	/** Current state of the emission, should be currentTime/ duration Must be updated on each update */
	public float percent;

	public Emitter (Emitter regularEmitter) {
		set(regularEmitter);
	}

	public Emitter () {
	}

	@Override
	public void init () {
		controller.particles.size = 0;
	}

	@Override
	public void end () {
		controller.particles.size = 0;
	}

	public boolean isComplete () {
		return percent >= 1.0f;
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

	public void setParticleCount (int aMin, int aMax) {
		setMinParticleCount(aMin);
		setMaxParticleCount(aMax);
	}

	public void set (Emitter emitter) {
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
		minParticleCount = json.readValue("minParticleCount", int.class, jsonData);
		maxParticleCount = json.readValue("maxParticleCount", int.class, jsonData);
	}

}
