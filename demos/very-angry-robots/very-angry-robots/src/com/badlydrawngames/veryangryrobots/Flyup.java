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

 
package com.badlydrawngames.veryangryrobots;

import com.badlydrawngames.general.Config;
import com.badlydrawngames.general.ScoreString;

public class Flyup {

	private static final float LIFE_TIME = Config.asFloat("flyup.lifetime", 1.0f);
	private static final float X_OFFSET = (Assets.flyupFont.getBounds("888").width / 2) / Assets.pixelDensity;
	private static final float Y_OFFSET = (Assets.flyupFont.getBounds("888").height) / Assets.pixelDensity;
	private static final float SPEED = Config.asFloat("flyup.speed", 25.0f);

	public final ScoreString scoreString;
	public boolean active;
	public float x;
	public float y;
	private float stateTime;

	public Flyup () {
		scoreString = new ScoreString(3);
	}

	public void update (float delta) {
		stateTime += delta;
		active = stateTime < LIFE_TIME;
		y += SPEED * delta;
	}

	public void spawn (float x, float y, int points) {
		active = true;
		stateTime = 0.0f;
		scoreString.setScore(points);
		this.x = x - X_OFFSET;
		this.y = y + Y_OFFSET;
	}
}
