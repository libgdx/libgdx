package de.swagner.paxbritannica;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import de.swagner.paxbritannica.factory.FactoryProduction;

public class Ship extends Sprite {

	protected float amount = 1.0f;

	protected float turnSpeed = 1.0f;
	protected float accel = 0.0f;
	protected float hitPoints = 0;

	protected float maxHitPoints = 0;

	private float delta = 0.0f;

	public float aliveTime = 0.0f;

	public Vector2 position = new Vector2();
	public Vector2 velocity = new Vector2();
	public Vector2 facing = new Vector2();
	
	public Vector2 collisionCenter = new Vector2();
	public ArrayList<Vector2> collisionPoints = new ArrayList<Vector2>();

	public boolean alive = true;

	private float deathCounter = 50f;
	private float nextExplosion = 10f;
	private float opacity = 5.0f;

	public int id = 0;

	public Ship(int id, Vector2 position, Vector2 facing) {
		super();

		this.id = id;

		this.position.set(position);
		this.facing.set(facing);
		
		collisionPoints.clear();
		collisionPoints.add(new Vector2());
		collisionPoints.add(new Vector2());
		collisionPoints.add(new Vector2());
		collisionPoints.add(new Vector2());

		this.setOrigin(this.getWidth() / 2.f, this.getHeight() / 2.f);
	}

	@Override
	public void draw(SpriteBatch spriteBatch) {
		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
		
		aliveTime += delta;
		collisionPoints.get(0).set( this.getVertices()[0], this.getVertices()[1]);
		collisionPoints.get(1).set( this.getVertices()[5], this.getVertices()[6]);
		collisionPoints.get(2).set( this.getVertices()[10], this.getVertices()[11]);
		collisionPoints.get(3).set( this.getVertices()[15], this.getVertices()[16]);
		
		collisionCenter.set(collisionPoints.get(2)).scl(0.5f).add(collisionPoints.get(0));

		velocity.scl( (float) Math.pow(0.97f, delta * 30.f));
		position.add(velocity.x * delta, velocity.y * delta);
		
		this.setRotation(facing.angle());
		this.setPosition(position.x, position.y);

		if (!(this instanceof Bullet) && hitPoints <= 0)
			destruct();
		if (MathUtils.random() < velocity.len() / 900.f) {
			GameInstance.getInstance().bubbleParticles.addParticle(randomPointOnShip());
		}
		super.draw(spriteBatch);
	}

	public void turn(float direction) {
		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
		
		facing.rotate(direction * turnSpeed * delta).nor();
	}

	public void thrust() {
		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
		
		velocity.add(facing.x * accel * delta, facing.y * accel * delta);
	}
	
	public void thrust(float amount) {
		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
		
		velocity.add(facing.x * accel * delta, facing.y * accel * amount * delta);
	}

	public Vector2 randomPointOnShip() {
		return new Vector2(collisionCenter.x + MathUtils.random(-this.getWidth() / 2, this.getWidth() / 2), collisionCenter.y
				+ MathUtils.random(-this.getHeight() / 2, this.getHeight() / 2));
	}

	/*
	 * Scratch space for computing a target's direction. This is safe (as a static) because
	 * its only used in goTowardsOrAway which is only called on the render thread (via update).
	 */
	private static final Vector2 target_direction = new Vector2(); 

	public void goTowardsOrAway(Vector2 targetPos, boolean forceThrust, boolean isAway) {
		target_direction.set(targetPos).sub(collisionCenter);
		if (isAway) {
			target_direction.scl(-1);
		}

		if (facing.crs(target_direction) > 0) {
			turn(1);
		} else {
			turn(-1);
		}

		if (forceThrust || facing.dot(target_direction) > 0) {
			thrust();
		}
	}

	public float healthPercentage() {
		return Math.max(hitPoints / maxHitPoints, 0);
	}

	public void damage(float amount) {
		hitPoints = Math.max(hitPoints - amount, 0);
	}

	public void destruct() {
		if (this instanceof FactoryProduction) {
			factoryDestruct();
		} else {
			GameInstance.getInstance().explode(this);
			alive = false;
			for (Ship factory : GameInstance.getInstance().factorys) {
				if(factory instanceof FactoryProduction && factory.id == this.id) ((FactoryProduction) factory).ownShips--;
			}
		}
	}

	public void factoryDestruct() {
		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());

		if (deathCounter > 0) {
			((FactoryProduction) this).production.halt_production = true;
			this.setColor(1, 1, 1, Math.min(1, opacity));
			opacity -= 1 * delta;
			if (Math.floor(deathCounter) % nextExplosion == 0) {
				GameInstance.getInstance().explode(this, randomPointOnShip());
				nextExplosion = MathUtils.random(2, 6);
			}
			deathCounter -= 10 * delta;
		} else {
			for (int i = 1; i <= 10; ++i) {
				GameInstance.getInstance().explode(this, randomPointOnShip());
			}
			alive = false;
		}
	}

	// automatically thrusts and turns according to the target
	public void goTowards(Vector2 targetPos, boolean forceThrust) {
		goTowardsOrAway(targetPos, forceThrust, false);
	}

	public void goAway(Vector2 targetPos, boolean forceThrust) {
		goTowardsOrAway(targetPos, forceThrust, true);
	}

}
