
package com.mojang.metagun.entity;

import com.mojang.metagun.Art;
import com.mojang.metagun.Sound;
import com.mojang.metagun.level.Camera;
import com.mojang.metagun.level.Level;
import com.mojang.metagun.screen.Screen;

public class Jabberwocky extends Entity {
	private static final int MAX_TEMPERATURE = 80 * 5;
	private int temperature = 0;
	public int slamTime = 0;

	public Jabberwocky (int x, int y) {
		this.x = x;
		this.y = y;
		w = 30;
		h = 20;
		bounce = 0;
	}

	@Override
	public void tick () {
		slamTime++;
		if (temperature > 0) {
			temperature--;
			for (int i = 0; i < 1; i++) {
				if (random.nextInt(MAX_TEMPERATURE) <= temperature) {
					double xd = (random.nextDouble() - random.nextDouble()) * 0.2;
					double yd = (random.nextDouble() - random.nextDouble()) * 0.2;
					level.add(new Spark(x + random.nextDouble() * w, y + random.nextDouble() * h, xa * 0.2 + xd, ya * 0.2 + yd));
				}
			}
		}
		tryMove(xa, ya);
		xa *= Level.FRICTION;
		ya *= Level.FRICTION;
		ya += Level.GRAVITY;

		java.util.List<Entity> entities = level.getEntities((int)x + 4, (int)y + 4, w - 8, h - 4);
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			if (e instanceof Gunner) {
				temperature += 10;
				if (temperature >= MAX_TEMPERATURE) {
					die();
				}
			}
			e.collideMonster(this);
		}
	}

	@Override
	public void render (Screen g, Camera camera) {
		int xp = (int)x;
		int yp = (int)y - 10;
		g.draw(Art.gremlins[3 + (slamTime / 10 % 5 == 2 ? 1 : 0)][0], xp, yp);
		// FIXME
// g.setColor(Color.BLACK);
// yp+=10;
// g.fillRect(xp + 5, yp - 8, 20, 3);
// g.setColor(Color.RED);
// g.fillRect(xp + 5, yp - 8, (20 * temperature / MAX_TEMPERATURE), 2);
	}

	@Override
	public void hitSpikes () {
		die();
	}

	private void die () {
		Sound.death.play();
		for (int i = 0; i < 16; i++) {
			level.add(new PlayerGore(x + random.nextDouble() * w, y + random.nextDouble() * h));
		}
		Sound.boom.play();
		for (int i = 0; i < 32; i++) {
			double dir = i * Math.PI * 2 / 8.0;
			double xa = Math.sin(dir);
			double ya = Math.cos(dir);
			double dist = i / 8 + 1;
			level.add(new Explosion(0, i * 3, x + w / 2 + xa * dist, y + h / 2 + ya * dist, xa, ya));
		}
		remove();
	}

	@Override
	public boolean shot (Bullet bullet) {
		return true;
	}

	@Override
	public void explode (Explosion explosion) {
		die();
	}
}
