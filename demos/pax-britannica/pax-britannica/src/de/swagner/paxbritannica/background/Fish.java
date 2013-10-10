package de.swagner.paxbritannica.background;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import de.swagner.paxbritannica.Resources;

public class Fish extends Sprite {

	private float SPEED = 0.2f;
	private float LIFETIME = MathUtils.random(8, 12);
	private float FADE_TIME = 2;

	private float random_direction = MathUtils.random()<0.5f ? 1:-1;
	private float random_scale = MathUtils.random() * 0.75f + 0.2f;
	private float random_speed = MathUtils.random() + 0.5f;
	private float random_opacity = MathUtils.random() * 0.1f + 0.1f;

	private Vector2 position = new Vector2();

	public boolean alive = true;

	private float since_alive = 0;
	
	float delta;

	public Fish(Vector2 position) {
		super();
		this.position = position;
		this.setPosition(position.x, position.y);
		this.setRotation(random_direction);
		this.setScale(random_scale, random_scale);

		switch (MathUtils.random(0, 7)) {
		case 0:
			this.set(Resources.getInstance().fish1);
			break;
		case 1:
			this.set(Resources.getInstance().fish2);
			break;
		case 2:
			this.set(Resources.getInstance().fish3);
			break;
		case 3:
			this.set(Resources.getInstance().fish4);
			break;
		case 4:
			this.set(Resources.getInstance().fish5);
			break;
		case 5:
			this.set(Resources.getInstance().fish6);
			break;
		case 6:
			this.set(Resources.getInstance().fish7);
			break;
		default:
			this.set(Resources.getInstance().fish8);
			break;
		}
		
		if(random_direction==-1) {
			flip(true, false);
		}
	}

	@Override
	public void draw(SpriteBatch spriteBatch) {
		super.draw(spriteBatch);
		
		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
		
		since_alive += delta/2.f;
		position.add((SPEED + random_speed) * delta * 5.f*random_direction,0);
		this.setPosition(position.x, position.y);

		
		if (since_alive < FADE_TIME) {
			super.setColor(1, 1, 1, Math.min((since_alive / FADE_TIME)*random_opacity,random_opacity));
		} else {
			this.setColor(1, 1, 1, Math.min(1 - (since_alive - LIFETIME + FADE_TIME) / FADE_TIME, 1) * random_opacity);
		}
		if (since_alive > LIFETIME) {
			alive = false;
		}
	}
	
	public void reset() {
		SPEED = 0.2f;
		LIFETIME = MathUtils.random(8, 12);
		FADE_TIME = 2;

		random_direction = MathUtils.random()<0.5f ? 1:-1;
		random_scale = MathUtils.random() * 0.75f + 0.2f;
		random_speed = MathUtils.random() + 0.5f;
		random_opacity = MathUtils.random() * 0.1f + 0.1f;
		
		alive = true;
		since_alive = 0;
		
		this.position = new Vector2(MathUtils.random(-100, 800),MathUtils.random(-100, 400));

		if(random_direction==-1) {
			flip(true, false);
		}
		this.setPosition(position.x, position.y);
		this.setRotation(random_direction);
		this.setScale(random_scale, random_scale);
	}
}
