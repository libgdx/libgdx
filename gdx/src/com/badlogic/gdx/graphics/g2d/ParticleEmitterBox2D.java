
package com.badlogic.gdx.graphics.g2d;

import java.io.BufferedReader;
import java.io.IOException;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

/** @author pitbuller */
public class ParticleEmitterBox2D extends ParticleEmitter {
	final World world;
	final Vector2 start = new Vector2();
	final Vector2 end = new Vector2();
	float fraction, normal;

	final RayCastCallback ray = new RayCastCallback() {
		public float reportRayFixture (Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
			ParticleEmitterBox2D.this.fraction = fraction;
			ParticleEmitterBox2D.this.normal = MathUtils.atan2(normal.y, normal.x) * MathUtils.radiansToDegrees;
			return fraction;
		}
	};

	public ParticleEmitterBox2D (World world) {
		super();
		this.world = world;
	}

	public ParticleEmitterBox2D (World world, BufferedReader reader) throws IOException {
		super(reader);
		this.world = world;
	}

	public ParticleEmitterBox2D (World world, ParticleEmitter emitter) {
		super(emitter);
		this.world = world;
	}

	@Override
	protected Particle newParticle (Sprite sprite) {
		return new ParticleBox2D(sprite);
	}

	private class ParticleBox2D extends Particle {
		public ParticleBox2D (Sprite sprite) {
			super(sprite);
		}

		public void translate (float velocityX, float velocityY) {
			if (velocityX == 0 && velocityY == 0) return;

			// Position offset is half of sprite texture size. Currently this does not work right with textureAtlas.
			final float x = getX() + getWidth() / 2f;
			final float y = getY() + getHeight() / 2f;

			fraction = 1f;
			start.set(x, y);
			end.set(x + velocityX, y + velocityY);
			world.rayCast(ray, start, end);

			if (fraction < 1f) {
				angle = 2f * normal - angle - 180f;
				angleCos = MathUtils.cosDeg(angle);
				angleSin = MathUtils.sinDeg(angle);
				velocityX = velocity * angleCos;
				velocityY = velocity * angleSin;
			}
			super.translate(velocityX, velocityY);
		}
	}
}
