package de.swagner.paxbritannica.particlesystem;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import de.swagner.paxbritannica.GameInstance;
import de.swagner.paxbritannica.Resources;

public class ExplosionParticleEmitter extends ParticleEmitter {

	public ExplosionParticleEmitter() {
		super();

		life = 0.5f;
		damping =1f;
		delta_scale = 1.0f;

		set(Resources.getInstance().explosion);
	}

	public void addParticle(Vector2 position, float scale) {
		super.addParticle(position, new Vector2(0,0), life, scale);
	}

	public void addBigExplosion(Vector2 position) {
		delta_scale = 5;
		addParticle(position, 0.5f);
		Vector2 random = new Vector2(MathUtils.cos((float) ((MathUtils.random() * MathUtils.PI * 2f) * Math.sqrt(MathUtils.random()))),
				(float) (MathUtils.sin(MathUtils.random() * MathUtils.PI * 2f) * Math.sqrt(MathUtils.random())));
		for (int i = 1; i <= 20; ++i) {
			Vector2 vel = new Vector2().set(random).add(random);
			Vector2 velp = new Vector2().set(vel).scl(i / 20.f * 2.f);
			Vector2 offset = new Vector2().set(random).scl(10);
			GameInstance.getInstance().sparkParticles.addLaserExplosion(new Vector2(position.x + offset.x, position.y + offset.y), velp);
		}
		for (int i = 1; i <= 5; ++i) {
			Vector2 vel = new Vector2(MathUtils.random() * 2 - 1, MathUtils.random() * 2 - 1).scl(4);
			Vector2 offset = new Vector2().set(random).scl(3);
			GameInstance.getInstance().sparkParticles.addLaserExplosion(new Vector2(position.x + offset.x, position.y + offset.y), vel);
		}
		for (int i = 1; i <= 50; ++i) {
			Vector2 offset = new Vector2().set(random).scl(17);
			GameInstance.getInstance().bigBubbleParticles.addParticle(new Vector2(position.x + offset.x, position.y + offset.y));
		}
	}

	public void addMediumExplosion(Vector2 position) {
		delta_scale = 3;
		addParticle(position, 0.4f);
		Vector2 random = new Vector2(MathUtils.cos((float) ((MathUtils.random() * MathUtils.PI * 2f) * Math.sqrt(MathUtils.random()))),
				(float) (MathUtils.sin(MathUtils.random() * MathUtils.PI * 2f) * Math.sqrt(MathUtils.random())));
		for (int i = 1; i <= 10; ++i) {
			Vector2 vel = new Vector2().set(random).add(random);
			Vector2 velp = new Vector2().set(vel).scl(i / 20.f * 2.f);
			Vector2 offset = new Vector2().set(random).scl(10);
			GameInstance.getInstance().sparkParticles.addLaserExplosion(new Vector2(position.x + offset.x, position.y + offset.y), velp);
		}
		for (int i = 1; i <= 3; ++i) {
			Vector2 vel = new Vector2(MathUtils.random() * 2 - 1, MathUtils.random() * 2 - 1).scl(3);
			Vector2 offset = new Vector2().set(random).scl(3);
			GameInstance.getInstance().sparkParticles.addLaserExplosion(new Vector2(position.x + offset.x, position.y + offset.y), vel);
		}
		for (int i = 1; i <= 20; ++i) {
			Vector2 offset = new Vector2().set(random).scl(2);
			GameInstance.getInstance().bigBubbleParticles.addParticle(new Vector2(position.x + offset.x, position.y + offset.y));
		}
	}

	public void addSmallExplosion(Vector2 position) {
		delta_scale = 2;
		addParticle(position, 0.3f);
		Vector2 random = new Vector2(MathUtils.cos((float) ((MathUtils.random() * MathUtils.PI * 2f) * Math.sqrt(MathUtils.random()))),
				(float) (MathUtils.sin(MathUtils.random() * MathUtils.PI * 2f) * Math.sqrt(MathUtils.random())));
		for (int i = 1; i <= 2; ++i) {
			Vector2 vel = new Vector2().set(random).add(random);
			Vector2 velp = new Vector2().set(vel).scl(i / 20.f * 2.f);
			Vector2 offset = new Vector2().set(random).scl(10);
			GameInstance.getInstance().sparkParticles.addLaserExplosion(new Vector2(position.x + offset.x, position.y + offset.y), velp);
		}
		for (int i = 1; i <= 2; ++i) {
			Vector2 vel = new Vector2(MathUtils.random() * 2 - 1, MathUtils.random() * 2 - 1).scl(2);
			Vector2 offset = new Vector2().set(random).scl(3);
			GameInstance.getInstance().sparkParticles.addLaserExplosion(new Vector2(position.x + offset.x, position.y + offset.y), vel);
		}
		for (int i = 1; i <= 10; ++i) {
			Vector2 offset = new Vector2().set(random).scl(2);
			GameInstance.getInstance().bigBubbleParticles.addParticle(new Vector2(position.x + offset.x, position.y + offset.y));
		}
	}

	public void addTinyExplosion(Vector2 position) {
		delta_scale = 1;
		addParticle(position, 0.1f);
		Vector2 random = new Vector2(MathUtils.cos((float) ((MathUtils.random() * MathUtils.PI * 2f) * Math.sqrt(MathUtils.random()))),
				(float) (MathUtils.sin(MathUtils.random() * MathUtils.PI * 2f) * Math.sqrt(MathUtils.random())));
		for (int i = 1; i <= 1; ++i) {
			Vector2 vel = new Vector2().set(random).add(random);
			Vector2 velp = new Vector2().set(vel).scl(i / 20.f * 2.f);
			Vector2 offset = new Vector2().set(random).scl(10);
			GameInstance.getInstance().sparkParticles.addLaserExplosion(new Vector2(position.x + offset.x, position.y + offset.y), velp);
		}
		for (int i = 1; i <= 1; ++i) {
			Vector2 vel = new Vector2(MathUtils.random() * 2 - 1, MathUtils.random() * 2 - 1);
			Vector2 offset = new Vector2().set(random).scl(3);
			GameInstance.getInstance().sparkParticles.addLaserExplosion(new Vector2(position.x + offset.x, position.y + offset.y), vel);
		}
		for (int i = 1; i <= 5; ++i) {
			Vector2 offset = new Vector2().set(random).scl(17);
			GameInstance.getInstance().bigBubbleParticles.addParticle(new Vector2(position.x + offset.x, position.y + offset.y));
		}
	}
}
