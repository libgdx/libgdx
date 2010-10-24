
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class MultitouchTest implements RenderListener, InputListener {
	ImmediateModeRenderer renderer;
	OrthographicCamera camera;

	Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.WHITE};

	@Override public void dispose () {
	}

	@Override public void render () {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
		Gdx.graphics.getGL10().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.setMatrices();
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
	}

	@Override public void surfaceChanged (int width, int height) {

	}

	@Override public void surfaceCreated () {
		if (renderer == null) {
			Gdx.app.log("Multitouch", "multitouch supported: " + Gdx.input.supportsMultitouch());
			Gdx.input.addInputListener(this); // only add this once...
			renderer = new ImmediateModeRenderer();
			camera = new OrthographicCamera();
			camera.setViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			camera.getPosition().set(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f, 0);
		}
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

	@Override public boolean touchDown (int x, int y, int pointer) {
		Gdx.app.log("Multitouch", "down: " + pointer);
		return false;
	}

	@Override public boolean touchDragged (int x, int y, int pointer) {
		Gdx.app.log("Multitouch", "drag: " + pointer);
		return false;
	}

	@Override public boolean touchUp (int x, int y, int pointer) {
		Gdx.app.log("Multitouch", "up: " + pointer);
		return false;
	}
}
