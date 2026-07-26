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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.ScreenUtils;

/** Regression test for the multiple-pointer crash reported in issue #7660. */
public class ActorGestureListenerPointerTest extends GdxTest {

	private Stage stage;
	private Skin skin;

	private ScrollPane firstPane;
	private ScrollPane secondPane;

	private boolean inputTriggered = false;

	public void create () {

		stage = new Stage();
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		firstPane = new ScrollPane(new Table(), skin);
		secondPane = new ScrollPane(new Table(), skin);

		Table table = new Table();
		table.setFillParent(true);
		table.add(firstPane).grow();
		table.add(secondPane).grow();

		Gdx.input.setInputProcessor(stage);
		stage.addActor(table);

	}

	public void render () {
		update(Gdx.graphics.getDeltaTime());
		draw();
	}

	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose () {
		stage.dispose();
		skin.dispose();
	}

	private void update (float deltaTime) {

		stage.act(Gdx.graphics.getDeltaTime());

		if (inputTriggered) return;

		Vector2 firstPanePosition = firstPane.localToScreenCoordinates(new Vector2(5, 5));
		Vector2 secondPanePosition = secondPane.localToScreenCoordinates(new Vector2(5, 5));

		// First touch
		stage.touchDown((int)firstPanePosition.x, (int)firstPanePosition.y, 0, 0);

		// Second touch
		stage.touchDown((int)secondPanePosition.x, (int)secondPanePosition.y, 1, 0);

		// Third touch (ignored)
		stage.touchDown((int)firstPanePosition.x + 5, (int)firstPanePosition.y, 2, 0);

		// Release ignored third touch
		stage.touchUp((int)firstPanePosition.x + 5, (int)firstPanePosition.y, 2, 0);

		inputTriggered = true;

	}

	private void draw () {
		ScreenUtils.clear(1f, 1f, 1f, 1f);
		stage.draw();
	}

}
