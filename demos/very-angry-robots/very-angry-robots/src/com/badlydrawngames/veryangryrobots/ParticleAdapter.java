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

import com.badlogic.gdx.graphics.Color;
import com.badlydrawngames.general.ParticleManager;
import com.badlydrawngames.veryangryrobots.mobiles.BaseShot;
import com.badlydrawngames.veryangryrobots.mobiles.Player;
import com.badlydrawngames.veryangryrobots.mobiles.Robot;

class ParticleAdapter implements WorldListener {

	final int PARTICLES = 32;

	final private World world;
	final private ParticleManager particleManager;
	private Color robotColor;
	private Color shotExplosionColor;

	public ParticleAdapter (World world, ParticleManager particleManager) {
		this.world = world;
		this.particleManager = particleManager;
		shotExplosionColor = new Color(1.0f, 0.5f, 0.0f, 1.0f);
	}

	public void setRobotColor (Color color) {
		this.robotColor = color;
	}

	public void update (float delta) {
		particleManager.update(delta);
	}

	@Override
	public void onEnteredRoom (float time, int robots) {
		particleManager.clear();
	}

	@Override
	public void onPlayerHit () {
		Player player = world.getPlayer();
		float x = player.x + player.width / 2;
		float y = player.y + player.height / 2;
		particleManager.add(x, y, 2 * PARTICLES, Color.WHITE);
	}

	@Override
	public void onRobotDestroyed (Robot robot) {
		float x = robot.x + robot.width / 2;
		float y = robot.y + robot.height / 2;
		particleManager.add(x, y, PARTICLES, robotColor);
	}

	@Override
	public void onShotDestroyed (BaseShot shot) {
		float x = shot.x + shot.width / 2;
		float y = shot.y + shot.height / 2;
		particleManager.add(x, y, PARTICLES / 8, shotExplosionColor);
	}

	@Override
	public void onCaptainActivated (float time) {
	}

	@Override
	public void onExitedRoom (float time, int robots) {
	}

	@Override
	public void onPlayerFired () {
	}

	@Override
	public void onPlayerSpawned () {
	}

	@Override
	public void onRobotFired (Robot robot) {
	}

	@Override
	public void onRobotHit (Robot robot) {
	}

	@Override
	public void onWorldReset () {
	}

}
