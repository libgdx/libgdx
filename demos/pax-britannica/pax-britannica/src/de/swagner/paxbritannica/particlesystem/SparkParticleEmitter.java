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

 package de.swagner.paxbritannica.particlesystem;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import de.swagner.paxbritannica.Resources;

public class SparkParticleEmitter extends ParticleEmitter {

	Vector2 particleVelocity = new Vector2();
	
	public SparkParticleEmitter() {
		super();
				
		life = 1.0f;
		damping =5.95f;

		set(Resources.getInstance().spark);
	}

	public void addLaserExplosion(Vector2 position, Vector2 velocity) {
		for (int i = 1; i <= 10; ++i) {
			random.set(MathUtils.cos((float) ((MathUtils.random() * MathUtils.PI * 2f) * Math.sqrt(MathUtils.random()))),
					(float) (MathUtils.sin(MathUtils.random() * MathUtils.PI * 2f) * Math.sqrt(MathUtils.random())));

			particleVelocity.set(-velocity.x + random.x, -velocity.y + random.y);
			addParticle(position, particleVelocity, 1f, 1);
		}
	}

}
