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

import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class ParticleEffectPool extends Pool<PooledEffect> {
	private final ParticleEffect effect;

	public ParticleEffectPool (ParticleEffect effect, int initialCapacity, int max) {
		super(initialCapacity, max);
		this.effect = effect;
	}

	public ParticleEffectPool (ParticleEffect effect, int initialCapacity, int max, boolean preFill) {
		super(initialCapacity, max, preFill);
		this.effect = effect;
	}

	protected PooledEffect newObject () {
		PooledEffect pooledEffect = new PooledEffect(effect);
		pooledEffect.start();
		return pooledEffect;
	}
	
	public void free (PooledEffect effect) {
		super.free(effect);
		
		effect.reset(false); // copy parameters exactly to avoid introducing error
		if (effect.xSizeScale != this.effect.xSizeScale || effect.ySizeScale != this.effect.ySizeScale || effect.motionScale != this.effect.motionScale){
			Array<ParticleEmitter> emitters = effect.getEmitters();
			Array<ParticleEmitter> templateEmitters = this.effect.getEmitters();
			for (int i=0; i<emitters.size; i++){
				ParticleEmitter emitter = emitters.get(i);
				ParticleEmitter templateEmitter = templateEmitters.get(i);
				emitter.matchSize(templateEmitter);
				emitter.matchMotion(templateEmitter);
			}
			effect.xSizeScale = this.effect.xSizeScale;
			effect.ySizeScale = this.effect.ySizeScale;
			effect.motionScale = this.effect.motionScale;
		}
	}

	public class PooledEffect extends ParticleEffect {
		PooledEffect (ParticleEffect effect) {
			super(effect);
		}

		public void free () {
			ParticleEffectPool.this.free(this);
		}
	}
}
