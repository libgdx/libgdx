package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Forever;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleTo;
import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
import com.badlogic.gdx.scenes.scene2d.actors.Image;
import com.badlogic.gdx.tests.utils.GdxTest;

public class ActionTest extends GdxTest {

	@Override public boolean needsGL20 () {
		return false;
	}

	Stage stage;
	
	@Override public void create() {
		stage = new Stage(480, 320, true);
		Texture texture = Gdx.graphics.newTexture(Gdx.files.internal("data/badlogic.jpg"),
																TextureFilter.Linear, TextureFilter.Linear,
																TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		Image img = new Image("actor", texture);
		img.width = img.height = 100;
		img.x = img.y = 100;
		img.action(Forever.$(Sequence.$(ScaleTo.$(1.1f, 1.1f,0.3f),ScaleTo.$(1f, 1f, 0.3f))));
		stage.addActor(img);
	}
	
	@Override public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.render();
	}
}
