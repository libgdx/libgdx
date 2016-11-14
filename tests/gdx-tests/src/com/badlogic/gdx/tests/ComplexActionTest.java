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

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateBy;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo;
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

public class ComplexActionTest extends GdxTest {

	Stage stage;
	Texture texture;

	@Override
	public void create () {
		stage = new Stage();

		Action complexAction = forever(sequence(parallel(rotateBy(180, 2), scaleTo(1.4f, 1.4f, 2), alpha(0.7f, 2)),
			parallel(rotateBy(180, 2), scaleTo(1.0f, 1.0f, 2), alpha(1.0f, 2))));

		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"), false);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		final Image img1 = new Image(new TextureRegion(texture));
		img1.setSize(100, 100);
		img1.setOrigin(50, 50);
		img1.setPosition(50, 50);

		final Image img2 = new Image(new TextureRegion(texture));
		img2.setSize(50, 50);
		img2.setOrigin(50, 50);
		img2.setPosition(150, 150);

		stage.addActor(img1);
		stage.addActor(img2);

		img1.addAction(complexAction);
		// img2.action(complexAction.copy());
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
	}

	@Override
	public void dispose () {
		stage.dispose();
		texture.dispose();
	}
}
