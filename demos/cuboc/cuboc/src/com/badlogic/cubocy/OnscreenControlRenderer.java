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

 
package com.badlogic.cubocy;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class OnscreenControlRenderer {
	Map map;
	SpriteBatch batch;
	TextureRegion dpad;
	TextureRegion left;
	TextureRegion right;
	TextureRegion jump;
	TextureRegion cubeControl;
	TextureRegion cubeFollow;

	public OnscreenControlRenderer (Map map) {
		this.map = map;
		loadAssets();
	}

	private void loadAssets () {
		Texture texture = new Texture(Gdx.files.internal("data/controls.png"));
		TextureRegion[] buttons = TextureRegion.split(texture, 64, 64)[0];
		left = buttons[0];
		right = buttons[1];
		jump = buttons[2];
		cubeControl = buttons[3];
		cubeFollow = TextureRegion.split(texture, 64, 64)[1][2];
		dpad = new TextureRegion(texture, 0, 64, 128, 128);
		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 480, 320);
	}

	public void render () {
		if (Gdx.app.getType() != ApplicationType.Android) return;
		if (map.cube.state != Cube.CONTROLLED) {
			batch.begin();
			batch.draw(left, 0, 0);
			batch.draw(right, 70, 0);
			batch.draw(cubeControl, 480 - 64, 320 - 64);
			batch.draw(cubeFollow, 480 - 64, 320 - 138);
			batch.draw(jump, 480 - 64, 0);
			batch.end();
		} else {
			batch.begin();
			batch.draw(dpad, 0, 0);
			batch.draw(cubeFollow, 480 - 64, 320 - 138);
			batch.draw(cubeControl, 480 - 64, 320 - 64);
			batch.end();
		}
	}

	public void dispose () {
		dpad.getTexture().dispose();
		batch.dispose();
	}
}
