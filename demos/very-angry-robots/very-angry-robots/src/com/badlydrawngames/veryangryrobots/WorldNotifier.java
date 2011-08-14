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

package com.badlydrawngames.veryangryrobots;

import com.badlogic.gdx.utils.Array;
import com.badlydrawngames.veryangryrobots.mobiles.BaseShot;
import com.badlydrawngames.veryangryrobots.mobiles.Robot;

class WorldNotifier implements WorldListener {

	private final Array<WorldListener> listeners;

	public WorldNotifier () {
		listeners = new Array<WorldListener>();
	}

	public void addListener (WorldListener listener) {
		listeners.add(listener);
	}

	@Override
	public void onCaptainActivated (float time) {
		for (WorldListener listener : listeners) {
			listener.onCaptainActivated(time);
		}
	}

	@Override
	public void onEnteredRoom (float time, int robots) {
		for (WorldListener listener : listeners) {
			listener.onEnteredRoom(time, robots);
		}
	}

	@Override
	public void onExitedRoom (float time, int robots) {
		for (WorldListener listener : listeners) {
			listener.onExitedRoom(time, robots);
		}
	}

	@Override
	public void onPlayerFired () {
		for (WorldListener listener : listeners) {
			listener.onPlayerFired();
		}
	}

	@Override
	public void onPlayerHit () {
		for (WorldListener listener : listeners) {
			listener.onPlayerHit();
		}
	}

	@Override
	public void onPlayerSpawned () {
		for (WorldListener listener : listeners) {
			listener.onPlayerSpawned();
		}
	}

	@Override
	public void onRobotDestroyed (Robot robot) {
		for (WorldListener listener : listeners) {
			listener.onRobotDestroyed(robot);
		}
	}

	@Override
	public void onRobotFired (Robot robot) {
		for (WorldListener listener : listeners) {
			listener.onRobotFired(robot);
		}
	}

	@Override
	public void onRobotHit (Robot robot) {
		for (WorldListener listener : listeners) {
			listener.onRobotHit(robot);
		}
	}

	@Override
	public void onShotDestroyed (BaseShot shot) {
		for (WorldListener listener : listeners) {
			listener.onShotDestroyed(shot);
		}
	}

	@Override
	public void onWorldReset () {
		for (WorldListener listener : listeners) {
			listener.onWorldReset();
		}
	}
}
