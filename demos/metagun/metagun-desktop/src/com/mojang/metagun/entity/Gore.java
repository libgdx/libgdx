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
import com.mojang.metagun.level.Level;
import com.mojang.metagun.screen.Screen;

public class Gore extends Entity {
	private int life;

	public Gore (double x, double y, double xa, double ya) {
		this.x = x;
		this.y = y;
		this.w = 1;
		this.h = 1;
		bounce = 0.2;
		this.xa = (xa + (random.nextDouble() - random.nextDouble()) * 1) * 0.2;
		this.ya = (ya + (random.nextDouble() - random.nextDouble()) * 1) * 0.2;

		life = random.nextInt(20) + 10;
	}

	@Override
	public void tick () {
		if (life-- <= 0) remove();
		onGround = false;
		tryMove(xa, ya);

		xa *= 0.999;
		ya *= 0.999;
		ya += Level.GRAVITY * 0.15;
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
		g.draw(Art.guys[7][1], xp, yp);
	}
}
