/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.tests.utils.GdxTest;

public class ActionTest extends GdxTest implements Runnable {
	Stage stage;
	Texture texture;

	@Override
	public void create () {
		stage = new Stage();
		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"), false);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		final Image img = new Image(new TextureRegion(texture));
		img.setSize(100, 100);
		img.setOrigin(50, 50);
		img.setPosition(100, 100);

		img.addAction(forever(sequence(delay(1.0f), new Action() {
			public boolean act (float delta) {
				System.out.println(1);
				img.clearActions();
				return true;
			}
		})));

		// img.action(Forever.$(Sequence.$(ScaleTo.$(1.1f,
		// 1.1f,0.3f),ScaleTo.$(1f, 1f, 0.3f))));
		// img.action(Forever.$(Parallel.$(RotateTo.$(1, 1))));
		// img.action(Delay.$(RotateBy.$(45, 2),
		// 1).setCompletionListener(this));
// // Action actionMoveBy = MoveBy.$(30, 0, 0.5f).setCompletionListener(
// // new OnActionCompleted() {
// //
// // @Override
// // public void completed(Action action) {
// // System.out.println("move by complete");
// // }
// // });
// //
// // Action actionDelay = Delay.$(actionMoveBy, 1).setCompletionListener(
// // new OnActionCompleted() {
// //
// // @Override
// // public void completed(Action action) {
// // System.out.println("delay complete");
// // }
// // });
// //
// // img.action(actionDelay);
//
// // img.action(Repeat.$(Sequence.$(MoveBy.$(50, 0, 1), MoveBy.$(0, 50, 1), MoveBy.$(-50, 0, 1), MoveBy.$(0, -50, 1)), 3));
// // img.action(Sequence.$(FadeOut.$(1),
// // FadeIn.$(1),
// // Delay.$(MoveTo.$(100, 100, 1), 2),
// // ScaleTo.$(0.5f, 0.5f, 1),
// // FadeOut.$(0.5f),
// // Delay.$(Parallel.$( RotateTo.$(360, 1),
// // FadeIn.$(1),
// // ScaleTo.$(1, 1, 1)), 1)));
// // OnActionCompleted listener = new OnActionCompleted() {
// // @Override public void completed (Action action) {
// // img.action(Parallel.$(Sequence.$(FadeOut.$(2), FadeIn.$(2)),
// // Sequence.$(ScaleTo.$(0.1f, 0.1f, 1.5f), ScaleTo.$(1.0f, 1.0f, 1.5f))).setCompletionListener(this));
// // }
// // };
// //
// // img.action(Parallel.$(Sequence.$(FadeOut.$(2), FadeIn.$(2)),
// // Sequence.$(ScaleTo.$(0.1f, 0.1f, 1.5f), ScaleTo.$(1.0f, 1.0f, 1.5f))).setCompletionListener(listener));
//
// // img.action(
// // Sequence.$(
// // Parallel.$(RotateBy.$(180, 2), ScaleTo.$(1.4f, 1.4f, 2), FadeTo.$(0.7f, 2)),
// // Parallel.$(RotateBy.$(180, 2), ScaleTo.$(1.0f, 1.0f, 2), FadeTo.$(1.0f, 2)),
// // Remove.$()
// // )
// // );
// //
// // Action action = Repeat.$(Sequence.$(
// // MoveBy.$(8, 0, 0.5f),
// // MoveBy.$(0, 8, 0.5f),
// // MoveBy.$(-8, 0, 0.5f),
// // MoveBy.$(0, -8, 0.5f)), 20);
// // Action action2 = action.copy();
// // img.action(action2);
//
// // float scale = 1;
// // float showDuration = 1;
// // ScaleTo scaleCountdown = ScaleTo.$(scale * 1.0f, scale * 1.0f, 1.0f);
// // scaleCountdown.setInterpolator(DecelerateInterpolator.$(3.0f));
// // Parallel parallel = Parallel.$(scaleCountdown);
// // // Sequence.$(FadeIn.$(0.25f), Delay.$(FadeOut.$(0.25f), 0.5f)));
// // Sequence cdAnim = Sequence.$(Delay.$(parallel, showDuration), Remove.$());
// // cdAnim.setCompletionListener(this);
// // img.action(cdAnim);
// //
// // Delay delay = Delay.$(MoveBy.$(100, 100, 1).setCompletionListener(this), 1);
// // delay.setCompletionListener(this);
// // img.action(Sequence.$(delay).setCompletionListener(this));

		stage.addActor(img);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
	}

	@Override
	public void run () {
		System.out.println("completed action");
	}

	@Override
	public void dispose () {
		stage.dispose();
		texture.dispose();
	}
}
