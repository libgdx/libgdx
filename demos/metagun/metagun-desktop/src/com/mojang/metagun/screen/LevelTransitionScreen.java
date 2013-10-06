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

 
package com.mojang.metagun.screen;

import com.mojang.metagun.Art;
import com.mojang.metagun.Input;
import com.mojang.metagun.level.Camera;
import com.mojang.metagun.level.Level;

public class LevelTransitionScreen extends Screen {
	private static final int TRANSITION_DURATION = 20;
	private final Level level1;
	private final Level level2;
	private int time = 0;
	private final Screen parent;
	private final int xa, ya;
	private final int xLevel, yLevel;

	public LevelTransitionScreen (Screen parent, int xLevel, int yLevel, Level level1, Level level2, int xa, int ya) {
		this.level1 = level1;
		this.level2 = level2;
		this.xLevel = xLevel;
		this.yLevel = yLevel;
		this.parent = parent;
		this.xa = xa;
		this.ya = ya;
	}

	@Override
	public void tick (Input input) {
		time++;
		if (time == TRANSITION_DURATION) {
			setScreen(parent);
		}
	}

	Camera c = new Camera(320, 240);

	@Override
	public void render () {
		double pow = time / (double)TRANSITION_DURATION;

		spriteBatch.getTransformMatrix().idt();
		spriteBatch.begin();
		draw(Art.bg, 0, 0);
		spriteBatch.end();

		c.x = (int)(-xa * 320 * pow);
		c.y = (int)(-ya * 240 * pow);
		level1.render(this, c);

		c.x = (int)(xa * 320 * (1 - pow));
		c.y = (int)(ya * 240 * (1 - pow));
		level2.render(this, c);
	}
}
