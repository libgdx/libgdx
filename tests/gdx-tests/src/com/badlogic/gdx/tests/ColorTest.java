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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ColorTest extends GdxTest {
	Stage stage;

	@Override
	public void create () {
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);

		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		skin.add("default", new BitmapFont(Gdx.files.internal("data/arial-32.fnt"), false));

		Table root = new Table();
		stage.addActor(root);
		root.setFillParent(true);

		Table column1 = new Table(skin);
		column1.add("WHITE", "default", Color.WHITE).row();
		column1.add("LIGHT_GRAY", "default", Color.LIGHT_GRAY).row();
		column1.add("GRAY", "default", Color.GRAY).row();
		column1.add("DARK_GRAY", "default", Color.DARK_GRAY).row();

		column1.add("BLUE", "default", Color.BLUE).row();
		column1.add("NAVY", "default", Color.NAVY).row();
		column1.add("ROYAL", "default", Color.ROYAL).row();
		column1.add("SLATE", "default", Color.SLATE).row();
		column1.add("SKY", "default", Color.SKY).row();
		column1.add("CYAN", "default", Color.CYAN).row();
		column1.add("TEAL", "default", Color.TEAL).row();

		Table column2 = new Table(skin);
		column2.add("GREEN", "default", Color.GREEN).row();
		column2.add("CHARTREUSE", "default", Color.CHARTREUSE).row();
		column2.add("LIME", "default", Color.LIME).row();
		column2.add("FOREST", "default", Color.FOREST).row();
		column2.add("OLIVE", "default", Color.OLIVE).row();

		column2.add("YELLOW", "default", Color.YELLOW).row();
		column2.add("GOLD", "default", Color.GOLD).row();
		column2.add("GOLDENROD", "default", Color.GOLDENROD).row();
		column2.add("ORANGE", "default", Color.ORANGE).row();

		column2.add("BROWN", "default", Color.BROWN).row();
		column2.add("TAN", "default", Color.TAN).row();
		column2.add("FIREBRICK", "default", Color.FIREBRICK).row();

		Table column3 = new Table(skin);
		column3.add("RED", "default", Color.RED).row();
		column3.add("SCARLET", "default", Color.SCARLET).row();
		column3.add("CORAL", "default", Color.CORAL).row();
		column3.add("SALMON", "default", Color.SALMON).row();
		column3.add("PINK", "default", Color.PINK).row();
		column3.add("MAGENTA", "default", Color.MAGENTA).row();

		column3.add("PURPLE", "default", Color.PURPLE).row();
		column3.add("VIOLET", "default", Color.VIOLET).row();
		column3.add("MAROON", "default", Color.MAROON).row();

		root.add(column1);
		root.add(column2);
		root.add(column3);
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
	}
}
