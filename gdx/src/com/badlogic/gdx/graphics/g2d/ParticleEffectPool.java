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
import com.badlogic.gdx.utils.Pool;

public class ParticleEffectPool extends Pool<PooledEffect> {
	private final ParticleEffect effect;

	public ParticleEffectPool (ParticleEffect effect, int initialCapacity, int max) {
		super(initialCapacity, max);
		this.effect = effect;
	}

	protected PooledEffect newObject () {
		return new PooledEffect(effect);
	}

	public PooledEffect obtain () {
		PooledEffect effect = super.obtain();
		effect.reset();
		return effect;
	}

	public class PooledEffect extends ParticleEffect {
		PooledEffect (ParticleEffect effect) {
			super(effect);
		}

		@Override
		public void reset () {
			super.reset();
		}

		public void free () {
			ParticleEffectPool.this.free(this);
		}
	}
}
