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
import com.badlogic.gdx.box2deditor.models.ShapeModel;
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
public class ShapeCreationInputProcessor extends InputAdapter {
	boolean isActive = false;

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		boolean isValid = button == Buttons.LEFT 
			&& (!Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) &&
			    !Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT))
			&& (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) ||
			    Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT));

		if (!isValid)
			return false;
		isActive = true;

		if (!AppContext.instance().isCurrentModelValid())
			return true;

		ShapeModel lastShape = AppContext.instance().getLastTempShape();

		if (lastShape == null || lastShape.isClosed()) {
			AppContext.instance().createNewTempShape();
			lastShape = AppContext.instance().getLastTempShape();
		}

		if (lastShape.getPointCount() >= 3 && AppContext.instance().nearestPoint == lastShape.getPoint(0)) {
			lastShape.close();
			AppContext.instance().saveCurrentModel();
		} else {
			Vector2 p = App.instance().screenToWorld(x, y);
			lastShape.addPoint(p);
		}

		return true;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		if (!isActive)
			return false;
		isActive = false;
		return true;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		if (!isActive)
			return false;
		touchMoved(x, y);
		return true;
	}

	@Override
	public boolean touchMoved(int x, int y) {
		if (!AppContext.instance().isCurrentModelValid())
			return false;

		Vector2 p = App.instance().screenToWorld(x, y);

		// Nearest point computation
		AppContext.instance().nearestPoint = null;
		ShapeModel shape = AppContext.instance().getLastTempShape();
		if (shape != null && !shape.isClosed() && shape.getPointCount() >= 3)
			if (shape.getPoint(0).dst(p) < 10 * App.instance().getCamera().zoom)
				AppContext.instance().nearestPoint = shape.getPoint(0);

		// Next point assignment
		AppContext.instance().nextPoint = p;
		return false;
	}
}