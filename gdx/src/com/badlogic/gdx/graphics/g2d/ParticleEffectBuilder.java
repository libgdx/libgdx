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

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.utils.Array;

/** This class is to manipulate {@link ParticleEffect} on runtime. It allows to get all {@link ParticleEmitterBuilder} for an
 * effect. */
public class ParticleEffectBuilder {
	private ParticleEffect effect;
	private Array<ParticleEmitterBuilder> emitter;

	public ParticleEffectBuilder (ParticleEffect effect) {
		this.effect = effect;
		emitter = new Array<ParticleEmitterBuilder>();
		loadEmitterBuilder();
	}

	private void loadEmitterBuilder () {
		emitter.clear();
		for (ParticleEmitter e : effect.getEmitters()) {
			emitter.add(new ParticleEmitterBuilder(e));
		}
	}

	/** Set the ParticleEffect to work with.
	 * @param effect the effect
	 * @return the old ParticleEffect */
	public ParticleEffect setParticleEffect (ParticleEffect effect) {
		ParticleEffect temp = this.effect;
		this.effect = effect;
		this.loadEmitterBuilder();
		return temp;
	}

	/** @return the current ParticleEffect */
	public ParticleEffect getParticleEffect () {
		return this.effect;
	}

	/** Add and return a new Emitter
	 * @return an {@link ParticleEmitterBuilder} containing the new emitter */
	public ParticleEmitterBuilder addEmitter () {
		ParticleEmitterBuilder e = new ParticleEmitterBuilder(new ParticleEmitter());
		this.effect.getEmitters().add(e.getEmitter());
		return e;
	}

	/** Add and return a number of emitters
	 * @return a List with {@link ParticleEmitterBuilder} containing the new emitters */
	public Array<ParticleEmitterBuilder> addEmitter (int i) {
		final Array<ParticleEmitterBuilder> temp = new Array<ParticleEmitterBuilder>();
		for (int j = 0; j < i; j++) {
			ParticleEmitterBuilder e = new ParticleEmitterBuilder(new ParticleEmitter());
			this.effect.getEmitters().add(e.getEmitter());
			temp.add(e);
		}
		return temp;
	}

	/** Add an given {@link ParticleEmitter}
	 * @param emitter */
	public void addEmitter (ParticleEmitter emitter) {
		ParticleEmitterBuilder e = new ParticleEmitterBuilder(emitter);
		this.effect.getEmitters().add(emitter);
	}

	/** Returns the {@link ParticleEmitterBuilder} for the emitter i
	 * @param i emitter number
	 * @return {@link ParticleEmitterBuilder} for emitter i */
	public ParticleEmitterBuilder getBuilder (int i) {
		return this.emitter.get(i);
	}

	/** @return an Array with all {@link ParticleEmitterBuilder} for the effect */
	public Array<ParticleEmitterBuilder> getBuilder () {
		return this.emitter;
	}

	/** return emitter i
	 * @param i the emitter number
	 * @return the ParticleEmitter i */
	public ParticleEmitter getEmitter (int i) {
		return this.emitter.get(i).getEmitter();
	}

	/** @return all Emitter */
	public Array<ParticleEmitter> getEmitter () {
		final Array<ParticleEmitter> temp = new Array<ParticleEmitter>();
		for (int i = 0; i < emitter.size; i++) {
			temp.add(emitter.get(i).getEmitter());
		}
		return temp;
	}
}
