/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
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

package com.badlogic.gdxinvaders.simulation;

import com.badlogic.gdx.math.Vector3;

public class Invader {
	public static float INVADER_RADIUS = 0.75f;
	public static float INVADER_VELOCITY = 1;
	public static int INVADER_POINTS = 40;
	public final static int STATE_MOVE_LEFT = 0;
	public final static int STATE_MOVE_DOWN = 1;
	public final static int STATE_MOVE_RIGHT = 2;

	public final Vector3 position = new Vector3();
	public int state = STATE_MOVE_LEFT;
	public boolean wasLastStateLeft = true;
	public float movedDistance = Simulation.PLAYFIELD_MAX_X / 2;;

	public Invader (Vector3 position) {
		this.position.set(position);
	}

	public void update (float delta, float speedMultiplier) {
		movedDistance += delta * INVADER_VELOCITY * speedMultiplier;
		if (state == STATE_MOVE_LEFT) {
			position.x -= delta * INVADER_VELOCITY * speedMultiplier;
			if (movedDistance > Simulation.PLAYFIELD_MAX_X) {
				state = STATE_MOVE_DOWN;
				movedDistance = 0;
				wasLastStateLeft = true;
			}
		}
		if (state == STATE_MOVE_RIGHT) {
			position.x += delta * INVADER_VELOCITY * speedMultiplier;
			if (movedDistance > Simulation.PLAYFIELD_MAX_X) {
				state = STATE_MOVE_DOWN;
				movedDistance = 0;
				wasLastStateLeft = false;
			}
		}
		if (state == STATE_MOVE_DOWN) {
			position.z += delta * INVADER_VELOCITY * speedMultiplier;
			if (movedDistance > 1) {
				if (wasLastStateLeft)
					state = STATE_MOVE_RIGHT;
				else
					state = STATE_MOVE_LEFT;
				movedDistance = 0;
			}
		}
	}
}
