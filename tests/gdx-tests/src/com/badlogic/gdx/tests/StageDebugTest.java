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

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.tests.utils.GdxTest;

/** @author Daniel Holderbaum */
public class StageDebugTest extends GdxTest {
	static TextureRegion textureRegion;

	private Stage stage;
	private Stage stage1;
	private Stage stage2;

	class DebugActor extends Actor {
		@Override
		public void draw (Batch batch, float parentAlpha) {
			batch.draw(textureRegion, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(),
				getRotation());
		}
	}

	@Override
	public void create () {
		textureRegion = new TextureRegion(new Texture("data/badlogic.jpg"));

		Gdx.input.setInputProcessor(this);

		stage1 = new Stage();
		stage1.getCamera().position.set(100, 100, 0);

		Group group = new Group();
		// group.setBounds(0, 0, 10, 10);
		// group.setOrigin(25, 50);
		group.setRotation(10);
		group.setScale(1.2f);
		stage1.addActor(group);

		DebugActor actor = new DebugActor();
		actor.setBounds(300, 140, 50, 100);
		actor.setOrigin(25, 50);
		actor.setRotation(-45);
		actor.setScale(2f);
		actor.addAction(forever(rotateBy(360, 8f)));
		group.addActor(actor);

		group.debugAll();

		stage2 = new Stage();
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		TextButton shortButton = new TextButton("Button short", skin);
		shortButton.debug();

		TextButton longButton = new TextButton("Button loooooooooong", skin);
		longButton.debug();

		Table root = new Table(skin);
		root.setFillParent(true);
		root.setBackground(skin.getDrawable("default-pane"));
		root.defaults().space(6);
		root.setTransform(true);
		root.rotateBy(10);
		root.setScale(1.3f, 1);
		root.debug();
		stage2.addActor(root);

		root.add(shortButton).pad(5);
		root.add(longButton).row();
		root.add("Colspan").colspan(2).row();

		switchStage();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		stage.act();
		stage.draw();
	}

	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		switchStage();
		return false;
	}

	@Override
	public void resize (int width, int height) {
		stage1.getViewport().update(width, height, true);
		stage2.getViewport().update(width, height, true);
	}

	private void switchStage () {
		if (stage != stage2) {
			stage = stage2;
		} else {
			stage = stage1;
		}
	}

}
