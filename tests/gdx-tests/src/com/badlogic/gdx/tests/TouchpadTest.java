
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.tests.utils.GdxTest;

public class TouchpadTest extends GdxTest {
	Stage stage;
	Touchpad touchpad;

	public void create () {
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		touchpad = new Touchpad(20, skin);
		touchpad.setBounds(15, 15, 100, 100);
		stage.addActor(touchpad);
	}

	public void render () {
		//System.out.println(touchpad.getKnobPercentX() + " " + touchpad.getKnobPercentY());
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	public void resize (int width, int height) {
		stage.setViewport(width, height, true);
	}

	public boolean needsGL20 () {
		return false;
	}

	public void dispose () {
		stage.dispose();
	}
}
