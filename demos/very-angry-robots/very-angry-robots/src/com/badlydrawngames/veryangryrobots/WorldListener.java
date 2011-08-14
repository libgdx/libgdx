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

import com.badlydrawngames.veryangryrobots.mobiles.BaseShot;
import com.badlydrawngames.veryangryrobots.mobiles.Robot;

public interface WorldListener {
	public void onCaptainActivated (float time);

	public void onEnteredRoom (float time, int robots);

	public void onExitedRoom (float time, int robots);

	public void onPlayerFired ();

	public void onPlayerHit ();

	public void onPlayerSpawned ();

	public void onRobotDestroyed (Robot robot);

	public void onRobotFired (Robot robot);

	public void onRobotHit (Robot robot);

	public void onShotDestroyed (BaseShot shot);

	public void onWorldReset ();
}
