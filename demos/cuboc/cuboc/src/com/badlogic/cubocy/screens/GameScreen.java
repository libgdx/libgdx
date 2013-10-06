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

 
package com.badlogic.cubocy.screens;

import com.badlogic.cubocy.Map;
import com.badlogic.cubocy.MapRenderer;
import com.badlogic.cubocy.OnscreenControlRenderer;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;

public class GameScreen extends CubocScreen {
	Map map;
	MapRenderer renderer;
	OnscreenControlRenderer controlRenderer;

	public GameScreen (Game game) {
		super(game);
	}

	@Override
	public void show () {
		map = new Map();
		renderer = new MapRenderer(map);
		controlRenderer = new OnscreenControlRenderer(map);
	}

	@Override
	public void render (float delta) {
		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
		map.update(delta);
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		renderer.render(delta);
		controlRenderer.render();

		if (map.bob.bounds.overlaps(map.endDoor.bounds)) {
			game.setScreen(new GameOverScreen(game));
		}

		if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			game.setScreen(new MainMenu(game));
		}
	}

	@Override
	public void hide () {
		Gdx.app.debug("Cubocy", "dispose game screen");
		renderer.dispose();
		controlRenderer.dispose();
	}
}
