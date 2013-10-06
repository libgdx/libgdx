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

 package de.swagner.paxbritannica.frigate;

import com.badlogic.gdx.math.Vector2;

import de.swagner.paxbritannica.GameInstance;
import de.swagner.paxbritannica.Ship;
import de.swagner.paxbritannica.Targeting;

public class MissileAI {
	private float MAX_LIFETIME = 5; // 5 seconds to auto-destruct

	private Ship target;

	private Missile missile;
	
	Vector2 relativeVel = new Vector2();
	Vector2 toTarget = new Vector2();

	public MissileAI(Missile missile) {
		this.missile = missile;
		retarget();
	}

	public void retarget() {
		target = Targeting.getTypeInRange(missile, 0, 500);
		if (target == null) {
			target = Targeting.getTypeInRange(missile, 1, 500);
		} else
			return;
		if (target == null) {
			target = Targeting.getTypeInRange(missile, 2, 500);
		} else
			return;
		if (target == null) {
			target = Targeting.getNearestOfType(missile, 1);
		} else
			return;
		if (target == null) {
			target = Targeting.getNearestOfType(missile, 3);
		} else
			target = null;
	}

	public void selfDestruct() {
		// EXPLODE!
		missile.alive = false;
		GameInstance.getInstance().explosionParticles.addTinyExplosion(missile.collisionCenter);
	}

	public Vector2 predict() {
		relativeVel.set(missile.velocity).sub(target.velocity);
		toTarget.set(target.collisionCenter).sub(missile.collisionCenter);
		if (missile.velocity.dot(toTarget) != 0) {
			float time_to_target = toTarget.dot(toTarget) / relativeVel.dot(toTarget);
			return new Vector2(target.collisionCenter).sub(relativeVel.scl(Math.max(0, time_to_target)));
		} else {
			return target.collisionCenter;
		}
	}

	public void update() {
		if (target == null || missile.aliveTime > MAX_LIFETIME) {
			selfDestruct();
		} else if (!target.alive) {
			retarget();
		} else {
			missile.goTowards(predict(), true);
		}
	}
}
