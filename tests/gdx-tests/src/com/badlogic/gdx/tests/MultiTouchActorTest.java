
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actors.Button;
import com.badlogic.gdx.tests.utils.GdxTest;

public class MultiTouchActorTest extends GdxTest implements InputProcessor {

	@Override
	public boolean needsGL20 () {
		return false;
	}

	class LogButton extends Button {
		public LogButton (String name, Texture texture) {
			super(name, texture);
		}

		@Override
		public boolean touchUp (float x, float y, int pointer) {
			boolean result = super.touchUp(x, y, pointer);
			if (result) Gdx.app.log("MultiTouchActorTest", "button '" + name + "', touch up, pointer " + pointer);
			return result;
		}

		@Override
		public boolean touchDown (float x, float y, int pointer) {
			boolean result = super.touchDown(x, y, pointer);
			if (result) Gdx.app.log("MultiTouchActorTest", "button '" + name + "', touch down, pointer " + pointer);
			return result;
		}
	}

	Stage stage;
	Texture texture;
	private LogButton buttonB;

	public void create () {
// Gdx.input = new RemoteInput();
		Gdx.input.setInputProcessor(this);

		stage = new Stage(480, 320, true);
		texture = new Texture(Gdx.files.internal("data/badlogicsmall.jpg"));

		LogButton buttonA = new LogButton("A", texture);
		buttonB = new LogButton("B", texture);

		buttonA.width = buttonA.height = buttonB.width = buttonB.height = 100;
		buttonB.x = 480 - buttonB.width;

		stage.addActor(buttonA);
		stage.addActor(buttonB);
	}

	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	@Override
	public boolean touchDown (int x, int y, int pointer, int button) {
		stage.touchDown(x, y, pointer, button);
		return false;
	}

	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		stage.touchUp(x, y, pointer, button);
		return false;
	}

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		stage.touchDragged(x, y, pointer);
		return false;
	}

	@Override
	public boolean touchMoved (int x, int y) {
		stage.touchMoved(x, y);
		return false;
	}

	@Override
	public boolean scrolled (int amount) {
		return false;
	}

	@Override
	public boolean keyDown (int keycode) {
		return false;
	}

	@Override
	public boolean keyUp (int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped (char character) {
		return false;
	}
}
