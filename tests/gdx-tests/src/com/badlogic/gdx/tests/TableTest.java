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
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.tests.utils.GdxTest;

public class TableTest extends GdxTest {
	Skin skin;
	Stage stage;
	Table root;

	@Override
	public void create () {
		stage = new Stage(0, 0, false);
		Gdx.input.setInputProcessor(stage);

		skin = new Skin(Gdx.files.internal("data/uiskin.json"), Gdx.files.internal("data/uiskin.png"));

		TextureRegion region = new TextureRegion(new Texture(Gdx.files.internal("data/badlogic.jpg")));

		NinePatch patch = skin.getResource("default-round", NinePatch.class);

		Label label = new Label("This is some text.", skin);

		root = new Table();
		stage.addActor(root);

		Table table = new Table();
		// root.add(table);

		// table.setBackground(region);
		table.setBackground(patch);
		table.enableClipping(stage);
		table.size(75, 75);
		table.add(label);

		table.setClickListener(new ClickListener() {
			public void click (Actor actor, float x, float y) {
				System.out.println("click!");
			}
		});

		root.debug();
		root.add(new Label("meow meow meow meow meow meow meow meow meow meow meow meow meow ", skin)).colspan(3);
		root.row();
		root.add(new TextButton("Text Button", skin));
		root.add(new TextButton("Toggle Button", skin.getStyle("toggle", TextButtonStyle.class)));
		root.add(new CheckBox("meow", skin));
		root.pack();
		//root.add(new Button(new Image(region), skin));
//		root.add(new LabelButton("Toggley", skin.getStyle("toggle", LabelButtonStyle.class)));
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
		Table.drawDebug(stage);
	}

	@Override
	public void resize (int width, int height) {
		stage.setViewport(width, height, false);
//		root.width = width;
//		root.height = height;
//		root.invalidate();
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}
}
