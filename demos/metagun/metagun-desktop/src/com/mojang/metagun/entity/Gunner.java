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

 
package com.mojang.metagun.entity;

import com.mojang.metagun.Art;
import com.mojang.metagun.Sound;
import com.mojang.metagun.level.Camera;
import com.mojang.metagun.level.Level;
import com.mojang.metagun.screen.Screen;

public class Gunner extends Entity {
	public static final int CHARGE_DURATION = 100;
	public int chargeTime = 0;
	private int sliding = 0;

	public Gunner (double x, double y, double xa, double ya) {
		this.x = x;
		this.y = y;
		this.w = 6;
		this.h = 6;
		bounce = -0.1;
		this.xa = xa + (random.nextDouble() - random.nextDouble()) * 0.5;
		this.ya = ya + (random.nextDouble() - random.nextDouble()) * 0.5;
	}

	@Override
	public void tick () {
		onGround = false;
		tryMove(xa, ya);

		if ((onGround || sliding != 0) && xa * xa < 0.01) {
			if (chargeTime++ >= CHARGE_DURATION) {
				chargeTime = 0;
				double xd = level.player.x + level.player.w / 2 - (x + w / 2);
				double yd = level.player.y + level.player.h / 2 - (y + h / 2);
				double dd = Math.sqrt(xd * xd + yd * yd);
				xd /= dd;
				yd /= dd;
				Sound.hit.play();
				level.add(new Bullet(this, x + 2, y + 2, xd, yd));
			}
		}
		xa *= Level.FRICTION;
		ya *= Level.FRICTION;
		ya += Level.GRAVITY;
	}

	@Override
	protected void hitWall (double xa, double ya) {
		sliding = 0;
		if (xa != 0) {
			if (xa > 0) {
				this.xa = 1;
				sliding = 1;
			}
			if (xa < 0) {
				this.xa = -1;
				sliding = -1;
			}
		}
		this.xa *= 0.4;
		this.ya *= 0.4;
	}

	@Override
	public void render (Screen g, Camera camera) {
		// g.setColor(Color.red);
		int xp = (int)x;
		int yp = (int)y;
		// g.fillRect(xp, yp, w, h);

		int xFrame = 0;
		int yFrame = 0;
		if (onGround) {
			double xd = level.player.x + level.player.w / 2 - (x + w / 2);
			double yd = level.player.y + level.player.h / 2 - (y + h / 2);
			double dd = Math.sqrt(xd * xd + yd * yd);
			xd /= dd;
			yd /= dd;
			xFrame = 3;
			yFrame = 2;
			double s = 0.3;
			if (xd > s) xFrame++;
			if (xd < -s) xFrame--;
			if (yd > s) yFrame++;
			if (yd < -s) yFrame--;
		} else if (sliding != 0) {
			double xd = level.player.x + level.player.w / 2 - (x + w / 2);
			double yd = level.player.y + level.player.h / 2 - (y + h / 2);
			double dd = Math.sqrt(xd * xd + yd * yd);
			xd /= dd;
			yd /= dd;
			xFrame = 0;
			yFrame = 2;
			if (sliding > 0) xFrame = 1;
			double s = 0.3;
			if (yd > s) yFrame++;
			if (yd < -s) yFrame--;
		} else {
			xFrame = (int)(1 - Math.floor(ya * 0.1));
			if (xFrame < 0) xFrame = 0;
			if (xFrame > 2) xFrame = 2;
			yFrame = 0;
		}

		g.draw(Art.guys[xFrame][yFrame], xp, yp);

		java.util.List<Entity> entities = level.getEntities((int)x, (int)y, 1, 1);
		for (int i = 0; i < entities.size(); i++) {
			entities.get(i).shove(this);
		}
	}

	@Override
	public boolean shot (Bullet bullet) {
		die();
		return true;
	}

	@Override
	public void hitSpikes () {
		die();
	}

	protected void die () {
		Sound.splat.play();
		level.add(new HeadGore(x + 2, y));
		for (int i = 0; i < 10; i++) {
			double xd = (random.nextDouble() - random.nextDouble()) * 4;
			double yd = (random.nextDouble() - random.nextDouble()) * 4;

			level.add(new Gore(x + 2 + random.nextDouble(), y + random.nextDouble() * 6, xa + xd, ya + yd));
		}
		remove();
	}

	@Override
	public void shove (Gunner enemy) {

		double xd = enemy.x - x;
		if (xd < 0) {
			xd = -0.01;
		} else if (xd > 0) {
			xd = 0.01;
		} else {
			if (random.nextBoolean()) {
				xd = -0.01;
			} else {
				xd = 0.01;
			}
		}

		enemy.xa += xd;
		xa -= xd;
	}

	@Override
	public void explode (Explosion explosion) {
		die();
	}

	@Override
	public void collideMonster (Entity e) {
		die();
	}
}
