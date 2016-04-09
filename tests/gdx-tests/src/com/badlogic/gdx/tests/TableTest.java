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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.utils.GdxTest;

public class TableTest extends GdxTest {
	Skin skin;
	Stage stage;
	Texture texture;
	Table root;

	@Override
	public void create () {
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		TextureRegion region = new TextureRegion(texture);

		NinePatch patch = skin.getPatch("default-round");

		Label label = new Label("This is some text.", skin);

		root = new Table() {
			public void draw (Batch batch, float parentAlpha) {
				super.draw(batch, parentAlpha);
			}
		};
		stage.addActor(root);
		// root.setTransform(true);

		Table table = new Table();
		table.setTransform(true);
		table.setPosition(100, 100);
		table.setOrigin(0, 0);
		table.setRotation(45);
		table.setScaleY(2);
		table.add(label);
		table.add(new TextButton("Text Button", skin));
		table.pack();
		// table.debug();
		table.addListener(new ClickListener() {
			public void clicked (InputEvent event, float x, float y) {
				System.out.println("click!");
			}
		});
//		root.addActor(table);

		TextButton button = new TextButton("Text Button", skin);
		Table table2 = new Table();
		// table2.debug()
		table2.add(button);
		table2.setTransform(true);
		table2.setScaleX(1.5f);
		table2.setOrigin(table2.getPrefWidth() / 2, table2.getPrefHeight() / 2);

		// Test colspan with expandX.
		// root.setPosition(10, 10);
		root.debug();
		root.setFillParent(true);
		root.add(new Label("meow meow meow meow meow meow meow meow meow meow meow meow", skin)).colspan(3).expandX();
		root.add(new TextButton("Text Button", skin));
		root.row();
		root.add(new TextButton("Text Button", skin));
		root.add(new TextButton("Toggle Button", skin.get("toggle", TextButtonStyle.class)));
		root.add(new CheckBox("meow meow meow meow meow meow meow meow", skin));
		// root.pack();
		// root.add(new Button(new Image(region), skin));
		// root.add(new LabelButton("Toggley", skin.getStyle("toggle", LabelButtonStyle.class)));
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
// root.width = width;
// root.height = height;
// root.invalidate();
	}

	@Override
	public void dispose () {
		stage.dispose();
		texture.dispose();
		skin.dispose();
	}
}
