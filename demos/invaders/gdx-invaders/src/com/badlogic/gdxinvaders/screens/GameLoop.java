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

package com.badlogic.gdxinvaders.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdxinvaders.Renderer;
import com.badlogic.gdxinvaders.simulation.Simulation;
import com.badlogic.gdxinvaders.simulation.SimulationListener;

public class GameLoop extends InvadersScreen implements SimulationListener {
	/** the simulation **/
	private final Simulation simulation;
	/** the renderer **/
	private final Renderer renderer;
	/** explosion sound **/
	private final Sound explosion;
	/** shot sound **/
	private final Sound shot;

	public GameLoop () {
		simulation = new Simulation();
		simulation.listener = this;
		renderer = new Renderer();
		explosion = Gdx.audio.newSound(Gdx.files.internal("data/explosion.ogg"));
		shot = Gdx.audio.newSound(Gdx.files.internal("data/shot.ogg"));
	}

	@Override
	public void dispose () {
		renderer.dispose();
		shot.dispose();
		explosion.dispose();
	}

	@Override
	public boolean isDone () {
		return simulation.ship.lives == 0;
	}

	@Override
	public void draw (float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		renderer.render(simulation, delta);
	}

	@Override
	public void update (float delta) {
		simulation.update(delta);

		float accelerometerY = Gdx.input.getAccelerometerY();
		if (accelerometerY < 0)
			simulation.moveShipLeft(delta, Math.abs(accelerometerY) / 10);
		else
			simulation.moveShipRight(delta, Math.abs(accelerometerY) / 10);

		if (Gdx.input.isKeyPressed(Keys.DPAD_LEFT)) simulation.moveShipLeft(delta, 0.5f);
		if (Gdx.input.isKeyPressed(Keys.DPAD_RIGHT)) simulation.moveShipRight(delta, 0.5f);

		if (Gdx.input.isTouched() || Gdx.input.isKeyPressed(Keys.SPACE)) simulation.shot();
	}

	@Override
	public void explosion () {
		explosion.play();
	}

	@Override
	public void shot () {
		shot.play();
	}
}
