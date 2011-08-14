/*
 * Copyright 2011 Rod Hyde (rod@badlydrawngames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlydrawngames.general;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

public class ParticleManager {

	private final Array<Particle> particles;
	private final int maxParticles;
	private int index;

	public ParticleManager (int maxParticles, float size) {
		particles = new Array<Particle>(maxParticles);
		this.maxParticles = maxParticles;
		for (int i = 0; i < maxParticles; i++) {
			particles.add(new Particle(size));
		}
		index = 0;
	}

	public Array<Particle> getParticles () {
		return particles;
	}

	public void clear () {
		for (Particle particle : particles) {
			particle.active = false;
		}
	}

	public void add (float x, float y, int n, Color c) {
		for (int i = 0; i < n; i++) {
			particles.get(index).spawn(c, x, y);
			if (++index == maxParticles) {
				index = 0;
			}
		}
	}

	public void update (float delta) {
		for (Particle particle : particles) {
			if (particle.active) {
				particle.update(delta);
			}
		}
	}
}
