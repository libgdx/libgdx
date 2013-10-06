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

public class Bullet extends Entity {
	public Entity source;
	public int noHitTime = 10;
	private int tick = 0;

	public Bullet (Entity source, double x, double y, double xa, double ya) {
		this.source = source;
		this.x = x;
		this.y = y;
		this.w = 1;
		this.h = 1;
		this.xa = xa + (random.nextDouble() - random.nextDouble()) * 0.1;
		this.ya = ya + (random.nextDouble() - random.nextDouble()) * 0.1;

		interactsWithWorld = true;
	}

	@Override
	public void tick () {
		tick++;
		tryMove(xa, ya);

		if (noHitTime > 0) {
			noHitTime--;
			return;
		}
		java.util.List<Entity> entities = level.getEntities((int)x, (int)y, 1, 1);
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			if (source == e) continue;

			if (e.shot(this)) {
				remove();
			}
		}
	}

	@Override
	protected void hitWall (double xa, double ya) {
		for (int i = 0; i < 3; i++) {
			level.add(new Spark(x, y, 0, 0));
		}
		remove();
	}

	@Override
	public void render (Screen g, Camera camera) {
		// FIXME
// if (tick % 2 == 0) {
// g.setColor(Color.YELLOW);
// int x1 = (int) (x + w / 2 - xa * 3);
// int y1 = (int) (y + h / 2 - ya * 3);
// int x2 = (int) (x + w / 2);
// int y2 = (int) (y + h / 2);
//
// g.drawLine(x1, y1, x2, y2);
// g.setColor(Color.WHITE);
//
// x1 = (int) (x + w / 2 - xa);
// y1 = (int) (y + h / 2 - ya);
// x2 = (int) (x + w / 2 + xa);
// y2 = (int) (y + h / 2 + ya);
//
// g.drawLine(x1, y1, x2, y2);
// } else {
// g.setColor(Color.YELLOW);
// int x1 = (int) (x + w / 2 - xa);
// int y1 = (int) (y + h / 2 - ya);
// int x2 = (int) (x + w / 2 + xa);
// int y2 = (int) (y + h / 2 + ya);
//
// g.drawLine(x1, y1, x2, y2);
// }

		g.draw(Art.shot, (int)x, (int)y);
		// g.fillRect(xp, yp, w, h);
	}
}
