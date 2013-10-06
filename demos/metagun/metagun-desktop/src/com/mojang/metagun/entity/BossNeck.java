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
import com.mojang.metagun.level.Camera;
import com.mojang.metagun.screen.Screen;

public class BossNeck extends BossPart {
	public int slamTime = 0;
	public BossPart child;
	public double baseRot = Math.PI * 1.25;
	public double rot = 0, rota = 0;
	public int time = 0;

	public BossNeck (int x, int y, BossPart child) {
		this.child = child;
		this.x = x;
		this.y = y;
		w = 12;
		h = 12;
		bounce = 0;
	}

	@Override
	public void tick () {
		if (dieIn > 0) {
			if (--dieIn == 0) die();
		}
		time++;

		rot = Math.sin(time / 40.0) * Math.cos(time / 13.0) * 0.5;
		rota *= 0.9;
		rot *= 0.9;
		double rr = baseRot + rot;
		double xa = Math.sin(rr);
		double ya = Math.cos(rr);
		child.x = x + xa * 8;
		child.y = y + ya * 8;
		child.setRot(rr);

		java.util.List<Entity> entities = level.getEntities((int)x + 4, (int)y + 4, w - 8, h - 4);
		for (int i = 0; i < entities.size(); i++) {
			entities.get(i).collideMonster(this);
		}
	}

	@Override
	public void setRot (double rot) {
		baseRot = rot;
	}

	@Override
	public void render (Screen screen, Camera camera) {
		int xp = (int)x - 1;
		int yp = (int)y - 1;
		screen.draw(Art.gremlins[4][1], xp, yp);
	}

	@Override
	public void hitSpikes () {
		die();
	}

	private void die () {
		child.dieIn = 5;
		for (int i = 0; i < 4; i++) {
			level.add(new PlayerGore(x + random.nextDouble() * w, y + random.nextDouble() * h));
		}
		for (int i = 0; i < 4; i++) {
			double dir = i * Math.PI * 2 / 8.0;
			double xa = Math.sin(dir);
			double ya = Math.cos(dir);
			double dist = i / 8 + 1;
			level.add(new Explosion(1, i * 3, x + w / 2 + xa * dist, y + h / 2 + ya * dist, xa, ya));
		}
		remove();
	}

	@Override
	public boolean shot (Bullet bullet) {
		return true;
	}

	@Override
	public void explode (Explosion explosion) {
		if (explosion.power > 0) {
			die();
		}
	}
}
