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

	/** controller **/
// private Controller controller;
// private int buttonsPressed = 0;
// private ControllerListener listener = new ControllerAdapter() {
// @Override
// public boolean buttonDown (Controller controller, int buttonIndex) {
// buttonsPressed++;
// return true;
// }
//
// @Override
// public boolean buttonUp (Controller controller, int buttonIndex) {
// buttonsPressed--;
// return true;
// }
// };

	public GameLoop () {
		simulation = new Simulation();
		simulation.listener = this;
		renderer = new Renderer();
		explosion = Gdx.audio.newSound(Gdx.files.internal("data/explosion.wav"));
		shot = Gdx.audio.newSound(Gdx.files.internal("data/shot.wav"));

		// check for attached controllers and if we are on
		// Ouya, take the first controller. Doesn't handle disconnects :D
// if(Controllers.getControllers().size > 0) {
// Controller controller = Controllers.getControllers().get(0);
// if(Ouya.ID.equals(controller.getName())) {
// this.controller = controller;
// controller.addListener(listener);
// }
// }
	}

	@Override
	public void dispose () {
		renderer.dispose();
		shot.dispose();
		explosion.dispose();
// if (controller != null)
// controller.removeListener(listener);
		simulation.dispose();
	}

	@Override
	public boolean isDone () {
		return simulation.ship.lives == 0;
	}

	@Override
	public void draw (float delta) {
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

// if(controller != null) {
// // if any button is pressed, we shoot.
// if(buttonsPressed > 0) simulation.shot();
//
// // if the left stick moved, move the ship
// float axisValue = controller.getAxis(Ouya.AXIS_LEFT_X) * 0.5f;
// if(Math.abs(axisValue) > 0.25f) {
// if(axisValue > 0) {
// simulation.moveShipRight(delta, axisValue);
// } else {
// simulation.moveShipLeft(delta, -axisValue);
// }
// }
// }

		if (Gdx.input.isKeyPressed(Keys.DPAD_LEFT) || Gdx.input.isKeyPressed(Keys.A)) simulation.moveShipLeft(delta, 0.5f);
		if (Gdx.input.isKeyPressed(Keys.DPAD_RIGHT) || Gdx.input.isKeyPressed(Keys.D)) simulation.moveShipRight(delta, 0.5f);
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
