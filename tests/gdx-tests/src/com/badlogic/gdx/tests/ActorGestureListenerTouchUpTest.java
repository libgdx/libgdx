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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.ScreenUtils;

/** Regression test for the ActorGestureListener touchUp reentrancy bug reported in issue #7660. */
public class ActorGestureListenerTouchUpTest extends GdxTest {

	private Stage stage;
	private Table table;
	private boolean inputTriggered;

	@Override
	public void create () {

		stage = new Stage();

		table = new Table();
		table.addListener(new ActorGestureListener() {
			@Override
			public void tap (InputEvent event, float x, float y, int count, int button) {
				stage.cancelTouchFocus();
			}

			@Override
			public void panStop (InputEvent event, float x, float y, int pointer, int button) {
				stage.cancelTouchFocus();
			}
		});
		table.setFillParent(true);
		table.setTouchable(Touchable.enabled);

		input.setInputProcessor(stage);
		stage.addActor(table);

	}

	@Override
	public void render () {
		update(graphics.getDeltaTime());
		draw();
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose () {
		stage.dispose();
	}

	private void update (float deltaTime) {

		stage.act(deltaTime);

		if (inputTriggered) return;
		final Vector2 screenTableCoords = table.localToScreenCoordinates(new Vector2(10, 10));

		int x = (int)screenTableCoords.x, y = (int)screenTableCoords.y;

		stage.touchDown(x, y, 0, 0);
		stage.touchDown(x + 5, y + 5, 0, 1);
		stage.touchUp(x + 5, y + 5, 0, 1);

		stage.touchDown(x, y, 0, 0);
		stage.touchDown(x + 5, y + 5, 0, 1);
		stage.touchDragged(x + 80, y + 80, 0);
		stage.touchUp(x + 80, y + 80, 0, 0);

		inputTriggered = true;

	}

	private void draw () {
		ScreenUtils.clear(0, 0, 0, 1);
		stage.draw();
	}

}
