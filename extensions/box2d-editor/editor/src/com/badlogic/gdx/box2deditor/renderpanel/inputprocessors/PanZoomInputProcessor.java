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

import com.badlogic.gdx.box2deditor.renderpanel.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

/**
 *
 * @author Aurelien Ribon (aurelien.ribon@gmail.com)
 */
public class PanZoomInputProcessor extends InputAdapter {
	private final Vector2 lastTouch = new Vector2();

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		if (button != Buttons.RIGHT)
			return false;

		lastTouch.set(x, y);
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		if (!Gdx.input.isButtonPressed(Buttons.RIGHT))
			return false;

		OrthographicCamera camera = App.instance().getCamera();
		Vector2 delta = new Vector2(x, y).sub(lastTouch).mul(camera.zoom);
		camera.translate(-delta.x, delta.y, 0);
		camera.update();
		lastTouch.set(x, y);
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		App app = App.instance();
		int[] zl = app.getZoomLevels();

		if (app.getZoom() == zl[0] && amount < 0) {
			app.setZoom(zl[1]);
		} else  if (app.getZoom() == zl[zl.length-1] && amount > 0) {
			app.setZoom(zl[zl.length-2]);
		} else {
			for (int i=1; i<zl.length-1; i++) {
				if (zl[i] == app.getZoom()) {
					app.setZoom(amount > 0 ? zl[i-1] : zl[i+1]);
					break;
				}
			}
		}

		app.getCamera().zoom = 100f / app.getZoom();
		app.getCamera().update();
		return false;
	}
}