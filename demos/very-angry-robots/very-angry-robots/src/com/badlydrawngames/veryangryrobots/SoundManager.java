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

import com.badlogic.gdx.audio.Sound;
import com.badlydrawngames.veryangryrobots.StatusManager.Achievements;
import com.badlydrawngames.veryangryrobots.mobiles.BaseShot;
import com.badlydrawngames.veryangryrobots.mobiles.Robot;

import static com.badlogic.gdx.math.MathUtils.*;

public class SoundManager implements WorldListener, AchievementsListener, ScoreListener {
	private static final float MIN_TAUNT_INTERVAL = 2.5f;
	private static final float MAX_TAUNT_INTERVAL = 15.0f;

	private float now;
	private float nextTauntTime;
	private float lastRobotFireTime;
	private float lastRobotHitTime;
	private float lastPlayerHitTime;
	private boolean canTaunt;
	private boolean isChicken;
	private int lives;

	@Override
	public void onEnteredRoom (float time, int robots) {
		Assets.playSound(Assets.spawnPlayerSound);
	}

	@Override
	public void onExitedRoom (float time, int robots) {
		isChicken = robots > 0;
		if (isChicken) {
			Assets.playSound(Assets.exitRoomAsChickenSpeech);
		}
		canTaunt = false;
	}

	@Override
	public void onPlayerSpawned () {
		canTaunt = true;
		scheduleTaunt();
	}

	@Override
	public void onPlayerFired () {
		Assets.playSound(Assets.playerShotSound);
	}

	@Override
	public void onRobotFired (Robot robot) {
		if (lastRobotFireTime != now) {
			Assets.playSound(Assets.robotShotSound);
			lastRobotFireTime = now;
		}
	}

	@Override
	public void onPlayerHit () {
		if (lastPlayerHitTime != now) {
			canTaunt = false;
			Assets.playSound(Assets.killPlayerSound);
			lastPlayerHitTime = now;
		}
	}

	@Override
	public void onRobotDestroyed (Robot robot) {
		if (lastRobotHitTime != now) {
			Assets.playSound(Assets.killRobotSound);
			lastRobotHitTime = now;
		}
	}

	@Override
	public void onCaptainActivated (float time) {
		Assets.playSound(Assets.captainEnterRoomSpeech);
		// We can't do much about any speech going on, but we can make sure nothing else talks over the top of this
		// one.
		scheduleTaunt();
	}

	@Override
	public void onRobotHit (Robot robot) {
	}

	@Override
	public void onShotDestroyed (BaseShot shot) {
	}

	@Override
	public void onWorldReset () {
		isChicken = false;
		lives = 0;
	}

	@Override
	public void onAttained (Achievements achievement) {
		Assets.playSound(Assets.achievementSound);
	}

	public void update (float delta) {
		now += delta;
		if (canTaunt && now >= nextTauntTime) {
			Sound[] sounds = isChicken ? Assets.chickenTaunts : Assets.standardTaunts;
			Assets.playSound(sounds[random(sounds.length - 1)]);
			scheduleTaunt();
		}
	}

	private void scheduleTaunt () {
		nextTauntTime = now + random(MIN_TAUNT_INTERVAL, MAX_TAUNT_INTERVAL);
	}

	@Override
	public void onScoreChanged (int score) {
	}

	@Override
	public void onLivesChanged (int lives) {
		if (lives - 1 == this.lives) {
			Assets.playSound(Assets.extraLifeSound);
		}
		this.lives = lives;
	}
}
