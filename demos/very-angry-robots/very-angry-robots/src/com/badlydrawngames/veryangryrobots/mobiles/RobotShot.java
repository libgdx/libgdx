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

public class RobotShot extends BaseShot {

	private static final float DEFAULT_SHOT_SPEED = Config.asFloat("RobotShot.slowSpeed", 4.6875f);

	private GameObject owner;

	public RobotShot () {
		width = Assets.robotShotWidth;
		height = Assets.robotShotHeight;
		setShotSpeed(DEFAULT_SHOT_SPEED);
	}

	public void setOwner (GameObject owner) {
		this.owner = owner;
	}

	@Override
	public boolean intersects (GameObject other) {
		return (owner != other) && super.intersects(other);
	}
}
