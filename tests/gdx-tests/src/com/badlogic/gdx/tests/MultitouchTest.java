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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.tmp.OrthographicCamera;
import com.badlogic.gdx.tests.utils.GdxTest;

public class MultitouchTest extends GdxTest implements InputProcessor {
	ImmediateModeRenderer renderer;
	OrthographicCamera camera;
	long startTime = System.nanoTime();

	Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.WHITE};

	@Override public void render () {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		Gdx.graphics.getGL10().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.update();
		camera.apply(Gdx.gl10);
		renderer.begin(GL10.GL_TRIANGLES);
		int size = Math.max(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) / 10;
		for (int i = 0; i < 10; i++) {
			if (Gdx.input.isTouched(i) == false) continue;

			float x = Gdx.input.getX(i);
			float y = Gdx.graphics.getHeight() - Gdx.input.getY(i) - 1;
			Color col = colors[i % colors.length];
			renderer.color(col.r, col.g, col.b, col.a);
			renderer.vertex(x, y + size, 0);
			renderer.color(col.r, col.g, col.b, col.a);
			renderer.vertex(x + size, y - size, 0);
			renderer.color(col.r, col.g, col.b, col.a);
			renderer.vertex(x - size, y - size, 0);
		}

		renderer.end();

		if (System.nanoTime() - startTime > 1000000000l) {
			Gdx.app.log("MultiTouhTest", "fps:" + Gdx.graphics.getFramesPerSecond());
			startTime = System.nanoTime();
		}
	}

	@Override public void create () {
		Gdx.app.log("Multitouch", "multitouch supported: " + Gdx.input.supportsMultitouch());
		renderer = new ImmediateModeRenderer();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());		
		camera.position.set(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f, 0);
		Gdx.input.setInputProcessor(this);
	}

	@Override public boolean keyDown (int keycode) {
		return false;
	}

	@Override public boolean keyTyped (char character) {
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		return false;
	}

	@Override public boolean touchDown (int x, int y, int pointer, int newParam) {
// Gdx.app.log("Multitouch", "down: " + pointer);
		return false;
	}

	@Override public boolean touchDragged (int x, int y, int pointer) {
// Gdx.app.log("Multitouch", "drag: " + pointer);
		return false;
	}

	@Override public boolean touchUp (int x, int y, int pointer, int button) {
// Gdx.app.log("Multitouch", "up: " + pointer);
		return false;
	}

	@Override public boolean needsGL20 () {
		return false;
	}

	@Override public boolean touchMoved (int x, int y) {
		return false;
	}

	@Override public boolean scrolled (int amount) {
		return false;
	}
}
