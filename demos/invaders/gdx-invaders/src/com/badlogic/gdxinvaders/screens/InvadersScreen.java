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

import com.badlogic.gdx.Screen;

/** Common class for a game screen, e.g. main menu, game loop, game over screen and so on.
 * @author mzechner */
public abstract class InvadersScreen implements Screen {
	/** Called when the screen should update itself, e.g. continue a simulation etc.
	 */
	public abstract void update (float delta);

	/** Called when a screen should render itself
	 */
	public abstract void draw (float delta);

	/** Called by GdxInvaders to check whether the screen is done.
	 * @return true when the screen is done, false otherwise */
	public abstract boolean isDone ();

	@Override
	public void render (float delta) {
		update(delta);
		draw(delta);
	}

	@Override
	public void resize (int width, int height) {
	}

	@Override
	public void show () {
	}

	@Override
	public void hide () {
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void dispose () {
	}
}
