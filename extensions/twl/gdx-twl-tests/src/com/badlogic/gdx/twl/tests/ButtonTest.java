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

package com.badlogic.gdx.twl.tests;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.twl.Layout;
import com.badlogic.gdx.twl.TWL;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.FPSCounter;

public class ButtonTest implements ApplicationListener, InputProcessor {
	private TWL twl;
	private InputMultiplexer input = new InputMultiplexer();

	@Override public void create () {
		Button button = new Button("Click Me");
		FPSCounter fpsCounter = new FPSCounter(4, 2);

		Layout layout = new Layout();
		layout.horizontal().sequence(0).parallel(button, fpsCounter).end().gap();
		layout.vertical().sequence(0, button, 5, fpsCounter, 0);

		SpriteBatch batch = new SpriteBatch();
		twl = new TWL(batch, "data/widgets.xml", FileType.Internal, layout);

		input.addProcessor(twl);
		input.addProcessor(this);
		Gdx.input.setInputProcessor(input);
	}

	@Override public void resize (int width, int height) {
	}

	@Override public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		twl.render();
	}

	@Override public void dispose () {
		twl.dispose();
	}

	@Override public void pause () {
	}

	@Override public void resume () {
	}

	public boolean keyDown (int keycode) {
		return false;
	}

	public boolean keyUp (int keycode) {
		return false;
	}

	public boolean keyTyped (char character) {
		return false;
	}

	public boolean touchDown (int x, int y, int pointer, int button) {
		System.out.println("This touch made it through and was not handled by TWL.");
		return false;
	}

	public boolean touchUp (int x, int y, int pointer, int button) {
		return false;
	}

	public boolean touchDragged (int x, int y, int pointer) {
		return false;
	}

	@Override public boolean touchMoved (int x, int y) {
		return false;
	}

	@Override public boolean scrolled (int amount) {
		return false;
	}
}
