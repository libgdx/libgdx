
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.tests.utils.GdxTest;

public class GroupTest extends GdxTest {
	Stage stage;

	public void create () {
		stage = new Stage(0, 0, false);
		Gdx.input.setInputProcessor(stage);

		Group.enableDebugging("data/group-debug.png");

		Group group1 = new Group("group1");
		group1.rotation = 30;
		group1.transform = true;
		group1.x = 50;
		group1.y = 50;
		group1.width = 150;
		group1.height = 150;
		stage.addActor(group1);

		Group group2 = new Group("group2");
		group2.transform = false;
		group2.x = 50;
		group2.y = 50;
		group2.width = 50;
		group2.height = 50;
		group1.addActor(group2);

		Group group3 = new Group("group3");
		group3.transform = true;
		group3.originX = 100;
		group3.rotation = 45;
		group3.x = 10;
		group3.y = 10;
		group3.width = 35;
		group3.height = 35;
		group2.addActor(group3);

		Group group4 = new Group("group4");
		group4.transform = false;
		group4.x = 5;
		group4.y = 5;
		group4.width = 25;
		group4.height = 25;
		group3.addActor(group4);
	}

	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.draw();
	}

	public void resize (int width, int height) {
		stage.setViewport(width, height, false);
	}

	public boolean needsGL20 () {
		return false;
	}
}
