
package com.badlogic.gdx.twl.tests;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
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

		twl = new TWL("data/widgets.xml", FileType.Internal, layout);

		input.addProcessor(twl);
		input.addProcessor(this);
	}

	@Override public void resize (int width, int height) {
	}

	@Override public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		Gdx.input.processEvents(input);
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

	public boolean touchDown (int x, int y, int pointer) {
		System.out.println("Not handled by TWL!");
		return false;
	}

	public boolean touchUp (int x, int y, int pointer) {
		return false;
	}

	public boolean touchDragged (int x, int y, int pointer) {
		return false;
	}
}
