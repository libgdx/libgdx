package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.OnActionCompleted;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Delay;
import com.badlogic.gdx.scenes.scene2d.actions.FadeIn;
import com.badlogic.gdx.scenes.scene2d.actions.FadeOut;
import com.badlogic.gdx.scenes.scene2d.actions.MoveBy;
import com.badlogic.gdx.scenes.scene2d.actions.MoveTo;
import com.badlogic.gdx.scenes.scene2d.actions.Parallel;
import com.badlogic.gdx.scenes.scene2d.actions.Repeat;
import com.badlogic.gdx.scenes.scene2d.actions.RotateTo;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleTo;
import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
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
		final Image img = new Image("actor", texture);
		img.width = img.height = 100;
		img.originX = 50;
		img.originY = 50;
		img.x = img.y = 100;
		// img.action(Forever.$(Sequence.$(ScaleTo.$(1.1f,
		// 1.1f,0.3f),ScaleTo.$(1f, 1f, 0.3f))));
		// img.action(Forever.$(Parallel.$(RotateTo.$(1, 1))));
		// img.action(Delay.$(RotateBy.$(45, 2),
		// 1).setCompletionListener(this));
//		Action actionMoveBy = MoveBy.$(30, 0, 0.5f).setCompletionListener(
//				new OnActionCompleted() {
//
//					@Override
//					public void completed(Action action) {
//						System.out.println("move by complete");
//					}
//				});
//
//		Action actionDelay = Delay.$(actionMoveBy, 1).setCompletionListener(
//				new OnActionCompleted() {
//
//					@Override
//					public void completed(Action action) {
//						System.out.println("delay complete");
//					}
//				});
//
//		img.action(actionDelay);

//		img.action(Repeat.$(Sequence.$(MoveBy.$(50, 0, 1), MoveBy.$(0, 50, 1), MoveBy.$(-50, 0, 1), MoveBy.$(0, -50, 1)), 3));
//		img.action(Sequence.$(FadeOut.$(1), 
//									 FadeIn.$(1), 
//									 Delay.$(MoveTo.$(100, 100, 1), 2), 
//									 ScaleTo.$(0.5f, 0.5f, 1),
//			                   FadeOut.$(0.5f), 
//			                   Delay.$(Parallel.$( RotateTo.$(360, 1), 
//			                  	 						FadeIn.$(1), 
//			                  	 						ScaleTo.$(1, 1, 1)), 1)));
		OnActionCompleted listener = new OnActionCompleted() {			
			@Override public void completed (Action action) {
				img.action(Parallel.$(Sequence.$(FadeOut.$(2), FadeIn.$(2)),
					Sequence.$(ScaleTo.$(0.1f, 0.1f, 1.5f), ScaleTo.$(1.0f, 1.0f, 1.5f))).setCompletionListener(this));				
			}
		};
		
		img.action(Parallel.$(Sequence.$(FadeOut.$(2), FadeIn.$(2)),
			Sequence.$(ScaleTo.$(0.1f, 0.1f, 1.5f), ScaleTo.$(1.0f, 1.0f, 1.5f))).setCompletionListener(listener));
		
		stage.addActor(img);
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
	}

	@Override
	public void completed(Action action) {
		System.out.println("completed");
	}
}
