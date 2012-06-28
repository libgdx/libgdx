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
package aurelienribon.texturepackergui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class PanZoomInputProcessor extends InputAdapter {
	private final Canvas canvas;
	private final Vector2 lastTouch = new Vector2();
	private final int[] zoomLevels = {5, 10, 16, 25, 33, 50, 66, 100, 150, 200, 300, 400, 600, 800, 1000};
	private int zoomLevel = 100;

	public PanZoomInputProcessor(Canvas canvas) {
		this.canvas = canvas;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		Vector2 p = canvas.screenToWorld(x, y);
		lastTouch.set(p);
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		Vector2 p = canvas.screenToWorld(x, y);
		Vector2 delta = new Vector2(p).sub(lastTouch);
		canvas.getCamera().translate(-delta.x, -delta.y, 0);
		canvas.getCamera().update();
		lastTouch.set(canvas.screenToWorld(x, y));
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		if (zoomLevel == zoomLevels[0] && amount < 0) {
			zoomLevel = zoomLevels[1];
		} else  if (zoomLevel == zoomLevels[zoomLevels.length-1] && amount > 0) {
			zoomLevel = zoomLevels[zoomLevels.length-2];
		} else {
			for (int i=1; i<zoomLevels.length-1; i++) {
				if (zoomLevels[i] == zoomLevel) {
					zoomLevel = amount > 0 ? zoomLevels[i-1] : zoomLevels[i+1];
					break;
				}
			}
		}

		canvas.getCamera().zoom = 100f / zoomLevel;
		canvas.getCamera().update();
		return false;
	}
}