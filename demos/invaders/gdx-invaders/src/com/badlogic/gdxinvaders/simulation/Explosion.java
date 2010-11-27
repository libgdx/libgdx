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

public class Explosion {
	public static final float EXPLOSION_LIVE_TIME = 1;
	public float aliveTime = 0;
	public final Vector3 position = new Vector3();

	public Explosion (Vector3 position) {
		this.position.set(position);
	}

	public void update (float delta) {
		aliveTime += delta;
	}
}
