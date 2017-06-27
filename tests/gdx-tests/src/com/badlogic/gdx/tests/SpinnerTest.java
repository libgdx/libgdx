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
import com.badlogic.gdx.scenes.scene2d.ui.SpinnerNumberModel;
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

		SpinnerArrayModel<String> arrayModel = new SpinnerArrayModel<String>(
			new Array<String>(new String[] {"first", "second", "third"}), 0, true);
		SpinnerNumberModel numberModel = new SpinnerNumberModel(-6, 10, 2, 2);
		SpinnerStyle style = new CustomStyle();
		Spinner spin1 = new Spinner(style, numberModel);
		stage.addActor(spin1);
		Spinner spin2 = new Spinner(style, arrayModel);
		spin2.setPosition(100, 100);
		spin2.setWidth(150);
		spin2.setHeight(60);
		stage.addActor(spin2);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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
			this.backgroundNext = new NinePatchDrawable(
				new NinePatch(new TextureRegion(new Texture(Gdx.files.internal("data/backgroundNext.png"))), 2, 19, 15, 15));
			this.backgroundPrev = new NinePatchDrawable(
				new NinePatch(new TextureRegion(new Texture(Gdx.files.internal("data/backgroundPrev.png"))), 2, 19, 15, 15));
			this.font = new BitmapFont();
			font.setColor(Color.DARK_GRAY);
		}
	}

}
