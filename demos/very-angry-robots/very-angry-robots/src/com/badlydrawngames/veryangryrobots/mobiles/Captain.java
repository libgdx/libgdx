/*
 * Copyright 2011 Rod Hyde (rod@badlydrawngames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlydrawngames.veryangryrobots.mobiles;

import com.badlydrawngames.general.Config;
import com.badlydrawngames.veryangryrobots.Assets;

import static com.badlogic.gdx.math.MathUtils.*;

public class Captain extends GameObject {

	public static final int LURKING = INACTIVE + 1;
	public static final int CHASING = LURKING + 1;

	private static final float SPEED = Config.asFloat("Captain.speed", 3.75f);
	private static final float BOUNCE_SIZE = Config.asFloat("Captain.bounceSize", 5.625f);
	private static final float BOUNCE_FREQUENCY = Config.asFloat("Captain.bounceFrequency", 10.0f);

	private float activateTime;
	private Player player;
	private float speed;
	private float t;

	public Captain () {
		width = Assets.captainWidth;
		height = Assets.captainHeight;
		geometry = Assets.captainGeometry;
		setState(INACTIVE);
		speed = SPEED;
	}

	@Override
	public void update (float delta) {
		stateTime += delta;
		if (state == LURKING) {
			updateLurking(delta);
		} else if (state == CHASING) {
			updateChasing(delta);
		}
	}

	public void activateAfter (float interval) {
		activateTime = stateTime + interval;
	}

	public void setPlayer (Player player) {
		this.player = player;
	}

	private void updateLurking (float delta) {
		if (stateTime >= activateTime) {
			setState(Captain.CHASING);
		}
	}

	private void updateChasing (float delta) {
		float dx = player.x - x;
		float dy = player.y - y;
		float angle = atan2(dy, dx);
		float sd = speed * delta;
		x += cos(angle) * sd;
		y += sin(angle) * sd;
		if (sin(t) > 0) {
			y += BOUNCE_SIZE * delta;
		} else {
			y -= BOUNCE_SIZE * delta;
		}
		t += delta * BOUNCE_FREQUENCY;
	}
}
