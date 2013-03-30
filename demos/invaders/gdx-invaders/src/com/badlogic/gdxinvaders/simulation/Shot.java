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

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

public class Shot extends ModelInstance {
	public static float SHOT_VELOCITY = 10;
	public boolean isInvaderShot;
	public boolean hasLeftField = false;
	private final static Vector3 tmpV = new Vector3();

	public Shot (Model model, Vector3 position, boolean isInvaderShot) {
		super(model, position);
		// this.position.set(position);
		this.isInvaderShot = isInvaderShot;
	}

	public void update (float delta) {
		if (isInvaderShot)
			transform.trn(0, 0, SHOT_VELOCITY * delta);
		else
			transform.trn(0, 0, -SHOT_VELOCITY * delta);

		transform.getTranslation(tmpV);
		if (tmpV.z > Simulation.PLAYFIELD_MAX_Z) hasLeftField = true;
		if (tmpV.z < Simulation.PLAYFIELD_MIN_Z) hasLeftField = true;
	}
}
