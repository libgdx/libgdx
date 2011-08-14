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
package com.badlogic.gdx.box2deditor.renderpanel.inputprocessors;

import com.badlogic.gdx.box2deditor.AppContext;
import com.badlogic.gdx.box2deditor.renderpanel.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;

/**
 *
 * @author Aurelien Ribon (aurelien.ribon@gmail.com)
 */
public class BallThrowInputProcessor extends InputAdapter {
	boolean isActive = false;

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		boolean isValid = button == Buttons.LEFT
			&& (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) ||
			    Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT))
			&& (!Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) &&
			    !Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT));

		if (!isValid)
			return false;
		isActive = true;

		if (!AppContext.instance().isCurrentModelValid())
			return true;

		Vector2 p = App.instance().screenToWorld(x, y);
		AppContext.instance().ballThrowFirstPoint = p;
		return true;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		if (!isActive)
			return false;
		isActive = false;

		if (!AppContext.instance().isCurrentModelValid())
			return true;

		Vector2 p = App.instance().screenToWorld(x, y);
		AppContext.instance().ballThrowLastPoint = p;
		
		if (App.instance().isWorldReady()) {
			Vector2 delta = new Vector2(AppContext.instance().ballThrowLastPoint).sub(AppContext.instance().ballThrowFirstPoint);
			App.instance().fireBall(AppContext.instance().ballThrowFirstPoint, delta);
		}
		
		AppContext.instance().ballThrowFirstPoint = null;
		AppContext.instance().ballThrowLastPoint = null;
		return true;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		if (!isActive)
			return false;

		if (!AppContext.instance().isCurrentModelValid())
			return true;

		Vector2 p = App.instance().screenToWorld(x, y);
		AppContext.instance().ballThrowLastPoint = p;
		return true;
	}
}