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

package com.badlogic.gdx.physics.box2d.graphics;

import java.io.BufferedReader;
import java.io.IOException;

import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.Particle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

/** @author kalle_h
 * 
 *         ParticleEmitterBox2D use box2d rayCast:ing to achieve continuous collision detection against box2d fixtures. If
 *         particle detect collision it change it's direction before actual collision would occur. Velocity is 100% reflected.
 * 
 *         These particles does not have any other physical attributes or functionality. Particles can't collide to other
 *         particles. */
public class ParticleEmitterBox2D extends ParticleEmitter {
	final World world;
	final Vector2 startPoint = new Vector2();
	final Vector2 endPoint = new Vector2();
	/** collision flag */
	boolean particleCollided;
	float normalAngle;
	/** If velocities squared is shorter than this it could lead 0 length rayCast that cause c++ assertion at box2d */
	private final static float EPSILON = 0.001f;

	/** default visibility to prevent synthetic accessor creation */
	final RayCastCallback rayCallBack = new RayCastCallback() {
		public float reportRayFixture (Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
			ParticleEmitterBox2D.this.particleCollided = true;
			ParticleEmitterBox2D.this.normalAngle = MathUtils.atan2(normal.y, normal.x) * MathUtils.radiansToDegrees;
			return fraction;
		}
	};

	/** Constructs default ParticleEmitterBox2D. Box2d World is used for rayCasting. Assumes that particles use same unit system
	 * that box2d world does.
	 * 
	 * @param world */
	public ParticleEmitterBox2D (World world) {
		super();
		this.world = world;
	}

	/** /**Constructs ParticleEmitterBox2D using bufferedReader. Box2d World is used for rayCasting. Assumes that particles use same
	 * unit system that box2d world does.
	 * 
	 * @param world
	 * @param reader
	 * @throws IOException */
	public ParticleEmitterBox2D (World world, BufferedReader reader) throws IOException {
		super(reader);
		this.world = world;
	}

	/** Constructs ParticleEmitterBox2D fully copying given emitter attributes. Box2d World is used for rayCasting. Assumes that
	 * particles use same unit system that box2d world does.
	 * 
	 * @param world
	 * @param emitter */
	public ParticleEmitterBox2D (World world, ParticleEmitter emitter) {
		super(emitter);
		this.world = world;
	}

	@Override
	protected Particle newParticle (Sprite sprite) {
		return new ParticleBox2D(sprite);
	}

	/** Particle that can collide to box2d fixtures */
	private class ParticleBox2D extends Particle {
		public ParticleBox2D (Sprite sprite) {
			super(sprite);
		}

		/** translate particle given amount. Continuous collision detection achieved by using RayCast from oldPos to newPos.
		 * 
		 * @param velocityX
		 * @param velocityY */
		@Override
		public void translate (float velocityX, float velocityY) {
			/** If velocities squares summed is shorter than Epsilon it could lead ~0 length rayCast that cause nasty c++ assertion
			 * inside box2d. This is so short distance that moving particle has no effect so this return early. */
			if ((velocityX * velocityX + velocityY * velocityY) < EPSILON) return;

			/** Position offset is half of sprite texture size. */
			final float x = getX() + getWidth() / 2f;
			final float y = getY() + getHeight() / 2f;

			/** collision flag to false */
			particleCollided = false;
			startPoint.set(x, y);
			endPoint.set(x + velocityX, y + velocityY);
			if (world != null) world.rayCast(rayCallBack, startPoint, endPoint);

			/** If ray collided boolean has set to true at rayCallBack */
			if (particleCollided) {
				// perfect reflection
				angle = 2f * normalAngle - angle - 180f;
				angleCos = MathUtils.cosDeg(angle);
				angleSin = MathUtils.sinDeg(angle);
				velocityX *= angleCos;
				velocityY *= angleSin;
			}

			super.translate(velocityX, velocityY);
		}
	}
}
