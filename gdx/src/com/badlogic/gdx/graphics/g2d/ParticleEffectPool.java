
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
		effect.start();
		return effect;
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
