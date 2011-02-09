package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.OnActionCompleted;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Delay;
import com.badlogic.gdx.scenes.scene2d.actions.MoveBy;
import com.badlogic.gdx.scenes.scene2d.actors.Image;
import com.badlogic.gdx.tests.utils.GdxTest;

public class ActionTest extends GdxTest implements OnActionCompleted {

	@Override
	public boolean needsGL20() {
		return false;
	}

	Stage stage;

	@Override
	public void create() {
		stage = new Stage(480, 320, true);
		Texture texture = new Texture(Gdx.files.internal("data/badlogic.jpg"),
				false);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		Image img = new Image("actor", texture);
		img.width = img.height = 100;
		img.originX = 50;
		img.originY = 50;
		img.x = img.y = 100;
		// img.action(Forever.$(Sequence.$(ScaleTo.$(1.1f,
		// 1.1f,0.3f),ScaleTo.$(1f, 1f, 0.3f))));
		// img.action(Forever.$(Parallel.$(RotateTo.$(1, 1))));
		// img.action(Delay.$(RotateBy.$(45, 2),
		// 1).setCompletionListener(this));
		Action actionMoveBy = MoveBy.$(30, 0, 0.5f).setCompletionListener(
				new OnActionCompleted() {

					@Override
					public void completed(Action action) {
						System.out.println("move by complete");
					}
				});

		Action actionDelay = Delay.$(actionMoveBy, 1).setCompletionListener(
				new OnActionCompleted() {

					@Override
					public void completed(Action action) {
						System.out.println("delay complete");
					}
				});

		img.action(actionDelay);

		stage.addActor(img);
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.render();
	}

	@Override
	public void completed(Action action) {
		System.out.println("completed");
	}
}
