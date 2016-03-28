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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.controllers.mappings.Xbox;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.tests.utils.GdxTest;

public class XboxControllerTest extends GdxTest {

	private static final String LOG_TAG = "Xbox Controller";
	private Controller controller;

	public void create () {
		controller = Controllers.getControllers().first();
		if (controller == null) {
			Gdx.app.log(LOG_TAG, "No controller connected");
			Gdx.app.exit();
			return;
		}

		Gdx.app.log(LOG_TAG, controller.getName());
		Gdx.app.log(LOG_TAG, "Is Xbox: " + Xbox.isXboxController(controller));
	}

	float lastLt = 0.0f;
	float lastRt = 0.0f;
	PovDirection lastPov;

	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (controller.getButton(Xbox.A)) {
			Gdx.app.log(LOG_TAG, "Button A was pressed");
		}

		if (controller.getButton(Xbox.B)) {
			Gdx.app.log(LOG_TAG, "Button B was pressed");
		}

		if (controller.getButton(Xbox.X)) {
			Gdx.app.log(LOG_TAG, "Button X was pressed");
		}

		if (controller.getButton(Xbox.Y)) {
			Gdx.app.log(LOG_TAG, "Button Y was pressed");
		}

		if (controller.getButton(Xbox.BACK)) {
			Gdx.app.log(LOG_TAG, "Button BACK was pressed");
		}

		if (controller.getButton(Xbox.GUIDE)) {
			Gdx.app.log(LOG_TAG, "Button GUIDE was pressed");
		}

		if (controller.getButton(Xbox.START)) {
			Gdx.app.log(LOG_TAG, "Button START was pressed");
		}

		if (controller.getButton(Xbox.L_BUMPER)) {
			Gdx.app.log(LOG_TAG, "Button LB was pressed");
		}

		if (controller.getButton(Xbox.R_BUMPER)) {
			Gdx.app.log(LOG_TAG, "Button RB was pressed");
		}

		if (controller.getButton(Xbox.L3)) {
			Gdx.app.log(LOG_TAG, "Button L3 was pressed");
		}

		if (controller.getButton(Xbox.R3)) {
			Gdx.app.log(LOG_TAG, "Button R3 was pressed");
		}

		if (Math.abs(controller.getAxis(Xbox.L_STICK_HORIZONTAL_AXIS)) > 0.2f) {
			Gdx.app.log(LOG_TAG, "Left stick (horizontal): " + controller.getAxis(Xbox.L_STICK_HORIZONTAL_AXIS));
		}

		if (Math.abs(controller.getAxis(Xbox.L_STICK_VERTICAL_AXIS)) > 0.2f) {
			Gdx.app.log(LOG_TAG, "Left stick (vertical): " + controller.getAxis(Xbox.L_STICK_VERTICAL_AXIS));
		}

		if (Math.abs(controller.getAxis(Xbox.R_STICK_HORIZONTAL_AXIS)) > 0.2f) {
			Gdx.app.log(LOG_TAG, "Right stick (horizontal): " + controller.getAxis(Xbox.R_STICK_HORIZONTAL_AXIS));
		}

		if (Math.abs(controller.getAxis(Xbox.R_STICK_VERTICAL_AXIS)) > 0.2f) {
			Gdx.app.log(LOG_TAG, "Right stick (vertical): " + controller.getAxis(Xbox.R_STICK_VERTICAL_AXIS));
		}

		if (controller.getAxis(Xbox.L_TRIGGER) != lastLt) {
			lastLt = controller.getAxis(Xbox.L_TRIGGER);
			Gdx.app.log(LOG_TAG, "Left trigger: " + lastLt);
		}

		if (controller.getAxis(Xbox.R_TRIGGER) != lastRt) {
			lastRt = controller.getAxis(Xbox.R_TRIGGER);
			Gdx.app.log(LOG_TAG, "Right trigger: " + lastRt);
		}

		PovDirection pov;
		if ((pov = controller.getPov(Xbox.DPAD_LEFT)) != lastPov) {
			lastPov = pov;
			Gdx.app.log(LOG_TAG, "POV: " + pov);
		} else if ((pov = controller.getPov(Xbox.DPAD_UP)) != lastPov) {
			lastPov = pov;
			Gdx.app.log(LOG_TAG, "POV: " + pov);
		} else if ((pov = controller.getPov(Xbox.DPAD_RIGHT)) != lastPov) {
			lastPov = pov;
			Gdx.app.log(LOG_TAG, "POV: " + pov);
		} else if ((pov = controller.getPov(Xbox.DPAD_DOWN)) != lastPov) {
			lastPov = pov;
			Gdx.app.log(LOG_TAG, "POV: " + pov);
		}

	}

}
