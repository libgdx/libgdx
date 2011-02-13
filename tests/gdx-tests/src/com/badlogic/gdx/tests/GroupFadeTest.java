package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.FadeIn;
import com.badlogic.gdx.scenes.scene2d.actions.FadeOut;
import com.badlogic.gdx.scenes.scene2d.actions.Forever;
import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
import com.badlogic.gdx.scenes.scene2d.actors.Image;
import com.badlogic.gdx.tests.utils.GdxTest;

public class GroupFadeTest extends GdxTest {

	@Override public boolean needsGL20 () {
		return false;
	}
	
	Stage stage;	
	
	@Override public void create() {
		Texture texture = new Texture(Gdx.files.internal("data/badlogicsmall.jpg"));
		stage = new Stage(480, 320, true);
		
		for(int i = 0; i < 100; i++) {
			Image img = new Image("img" + i, texture);
			img.x = (float)Math.random() * 480;
			img.y = (float)Math.random() * 320;
			img.color.a = (float)Math.random() * 0.5f + 0.5f;
			stage.addActor(img);
		}
		
		stage.getRoot().action(Forever.$(Sequence.$(FadeOut.$(3), FadeIn.$(3))));
	}

	@Override public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}
}
