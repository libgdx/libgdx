
package com.mojang.metagun.entity;

import com.mojang.metagun.Art;
import com.mojang.metagun.level.Camera;
import com.mojang.metagun.level.Level;
import com.mojang.metagun.screen.Screen;

public class Explosion extends Entity {
	private int life, delay;
	private final int color;
	private final int duration;
	public int power;

	public Explosion (int power, int delay, double x, double y, double xa, double ya) {
		this.power = power;
		this.x = x;
		this.y = y;
		this.w = 1;
		this.h = 1;
		bounce = 0.2;
		this.xa = xa + (random.nextDouble() - random.nextDouble()) * 0.2;
		this.ya = ya + (random.nextDouble() - random.nextDouble()) * 0.2;

		color = random.nextInt(3);

		duration = random.nextInt(20) + 10;
		life = 0;
	}

	@Override
	public void tick () {
		if (delay > 0) {
			delay--;
			return;
		}
		if (life++ >= duration) remove();
		interactsWithWorld = life > 10;
		onGround = false;
		// tryMove(xa, ya);
		x += xa;
		y += ya;

		level.isFree(this, x, y, w, h, 0, 0);
		xa *= 0.95;
		ya *= 0.95;
		ya -= Level.GRAVITY * 0.15;

		if (interactsWithWorld && life < duration * 0.75) {
			java.util.List<Entity> entities = level.getEntities((int)x, (int)y, 1, 1);
			for (int i = 0; i < entities.size(); i++) {
				entities.get(i).explode(this);
			}
		}
	}

	@Override
	protected void hitWall (double xa, double ya) {
		this.xa *= 0.4;
		this.ya *= 0.4;
	}

	@Override
	public void render (Screen g, Camera camera) {
		int xp = (int)x;
		int yp = (int)y;
		g.draw(Art.guys[(life - 1) * 8 / duration][4 + color], xp - 3, yp - 3);
	}
}
