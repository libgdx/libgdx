package de.swagner.paxbritannica.background;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import de.swagner.paxbritannica.Resources;

public class Debris extends Sprite {

	private float SPEED = 5.0f;
	private float LIFETIME = MathUtils.random(8, 12);
	private float FADE_TIME = 2;

	private float random_direction = MathUtils.random(-360, 360);
	private float random_scale = MathUtils.random() * 0.75f + 0.5f;
	private float random_speed = (MathUtils.random() * 2f) - 1f;
	private float random_opacity = MathUtils.random() * 0.35f + 0.6f;

	private Vector2 position = new Vector2();
	private Vector2 facing = new Vector2(1, 0);

	public boolean alive = true;

	private float since_alive = 0;

	private float delta;

	public Debris(Vector2 position) {
		super();
		this.position = position;
		this.setPosition(position.x, position.y);

		this.facing.rotate(random_direction);
		this.setScale(random_scale, random_scale);

		switch (MathUtils.random(0, 2)) {
		case 0:
			this.set(Resources.getInstance().debrisSmall);
			break;
		case 1:
			this.set(Resources.getInstance().debrisMed);
			break;
		default:
			this.set(Resources.getInstance().debrisLarge);
			break;
		}
	}

	@Override
	public void draw(SpriteBatch spriteBatch) {
		super.draw(spriteBatch);

		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());

		since_alive += delta;

		facing.rotate((SPEED + random_speed) * delta).nor();
		position.add(facing.scl((SPEED + random_speed) * delta));
		this.setPosition(position.x, position.y);

		if (since_alive < FADE_TIME) {
			super.setColor(1, 1, 1, Math.min((since_alive / FADE_TIME) * random_opacity, random_opacity));
		} else {
			this.setColor(1, 1, 1, Math.min(1 - (since_alive - LIFETIME + FADE_TIME) / FADE_TIME, 1) * random_opacity);
		}
		if (since_alive > LIFETIME) {
			alive = false;
			this.setColor(1, 1, 1, 0);
		}
	}

	public void reset() {
		SPEED = 5.0f;
		LIFETIME = MathUtils.random(8, 12);
		FADE_TIME = 2;

		random_direction = MathUtils.random(-360, 360);
		random_scale = MathUtils.random() * 0.75f + 0.5f;
		random_speed = (MathUtils.random() * 2f) - 1f;
		random_opacity = MathUtils.random() * 0.35f + 0.6f;

		alive = true;
		since_alive = 0;

		this.position = new Vector2(MathUtils.random(-100, 800), MathUtils.random(-100, 400));
		facing = new Vector2(1, 0);

		this.setPosition(position.x, position.y);

		this.facing.rotate(random_direction);
		this.setScale(random_scale, random_scale);
	}
}
