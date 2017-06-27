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

import java.io.File;
import java.math.BigDecimal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Spinner;
import com.badlogic.gdx.scenes.scene2d.ui.SpinnerArrayModel;
import com.badlogic.gdx.scenes.scene2d.ui.SpinnerDecimalModel;
import com.badlogic.gdx.scenes.scene2d.ui.SpinnerNumberModel;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Spinner.SpinnerStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

/** Class that displays the usage of spinners, with a number model and an array model.
 * @author Jeremy Gillespie-Cloutier */
public class SpinnerTest extends GdxTest {

	private Stage stage;

	@Override
	public void create () {
		stage = new Stage();
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		Gdx.input.setInputProcessor(stage);

		Table table = new Table();
		table.setFillParent(true);
		stage.addActor(table);

		// This model displays natural numbers
		SpinnerNumberModel numberModel = new SpinnerNumberModel(-6, 10, 2, 2);
		SpinnerStyle style = new CustomStyle();
		Spinner spin1 = new Spinner(style, numberModel);
		table.add(spin1);
		table.row();

		// This model does not specify display values (i.e. toString() is used)
		SpinnerArrayModel<String> arrayModel = new SpinnerArrayModel<String>(
			new Array<String>(new String[] {"first", "second", "third"}), 0, true);
		Spinner spin2 = new Spinner(style, arrayModel);
		table.add(spin2).width(150).height(60);
		table.row();

		// This model specifies display values
		SpinnerArrayModel<Object> objectArrayModel = new SpinnerArrayModel<Object>(
			new Array<Object>(new Object[] {new Object(), new Object(), new Object()}),
			new Array<CharSequence>(new CharSequence[] {"Object 1", "Object 2", "Object 3"}), 0, true);
		Spinner spin3 = new Spinner(style, objectArrayModel);
		table.add(spin3);
		table.row();

		// This model displays decimal numbers
		SpinnerDecimalModel decimalModel = new SpinnerDecimalModel(new BigDecimal("0"), new BigDecimal("1"), new BigDecimal("0.05"),
			new BigDecimal("0.50"));
		Spinner spin4 = new Spinner(style, decimalModel);
		table.add(spin4);
		table.row();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glClearColor(1, 0, 0, 1);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	@Override
	public void dispose () {
		stage.dispose();
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	class CustomStyle extends SpinnerStyle {
		public CustomStyle () {
			this.background = new NinePatchDrawable(
				new NinePatch(new TextureRegion(new Texture(Gdx.files.internal("data/background.png"))), 2, 19, 15, 15));
			this.backgroundHover = new NinePatchDrawable(
				new NinePatch(new TextureRegion(new Texture(Gdx.files.internal("data/backgroundHover.png"))), 2, 19, 15, 15));
			this.backgroundNext = new NinePatchDrawable(
				new NinePatch(new TextureRegion(new Texture(Gdx.files.internal("data/backgroundNext.png"))), 2, 19, 15, 15));
			this.backgroundPrev = new NinePatchDrawable(
				new NinePatch(new TextureRegion(new Texture(Gdx.files.internal("data/backgroundPrev.png"))), 2, 19, 15, 15));
			this.font = new BitmapFont();
			font.setColor(Color.DARK_GRAY);
		}
	}

}
