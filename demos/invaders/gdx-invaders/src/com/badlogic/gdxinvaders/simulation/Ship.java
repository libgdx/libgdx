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

public class Ship {
	public static final float SHIP_RADIUS = 1;
	public static final float SHIP_VELOCITY = 20;
	public final Vector3 position = new Vector3(0, 0, 0);
	public int lives = 3;
	public boolean isExploding = false;
	public float explodeTime = 0;

	public void update (float delta) {
		if (isExploding) {
			explodeTime += delta;
			if (explodeTime > Explosion.EXPLOSION_LIVE_TIME) {
				isExploding = false;
				explodeTime = 0;
			}
		}
	}
}
