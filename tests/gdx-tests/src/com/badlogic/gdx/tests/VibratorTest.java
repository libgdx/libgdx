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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.tests.utils.GdxTest;

public class VibratorTest extends GdxTest {

	Stage stage;
	SpriteBatch batch;
	Skin skin;

	@Override
	public void create () {
		batch = new SpriteBatch();
		stage = new Stage();
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		Gdx.input.setInputProcessor(stage);

		// Create a table that fills the screen. Everything else will go inside this table.
		Table table = new Table();
		table.setFillParent(true);
		stage.addActor(table);

		final CheckBox fallbackCheckbox = new CheckBox("Fallback", skin);
		final Button button = getButton("Vibrate");
		button.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Gdx.input.vibrate(50);
			}
		});
		final Button buttonVibrateAmplitude = getButton("Vibrate \n Amplitude \n Random");
		buttonVibrateAmplitude.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				int randomLength = MathUtils.random(10, 200);
				int randomAmplitude = MathUtils.random(0, 255);
				Gdx.input.vibrate(randomLength, randomAmplitude, fallbackCheckbox.isChecked());
				Gdx.app.log("VibratorTest", "Length: " + randomLength + "ms, Amplitude: " + randomAmplitude);
			}
		});
		final Button buttonVibrateType = getButton("Vibrate \n Type \n Random");
		buttonVibrateType.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				Input.VibrationType vibrationType = Input.VibrationType.values()[MathUtils.random(0,
					Input.VibrationType.values().length - 1)];
				Gdx.input.vibrate(vibrationType);
				Gdx.app.log("VibratorTest", "VibrationType: " + vibrationType.name());
			}
		});

		table.defaults().pad(20f);
		table.add(button).size(120f);
		table.add(buttonVibrateAmplitude).size(120f);
		table.add(buttonVibrateType).size(120f);
		table.row();
		table.add(fallbackCheckbox).colspan(3).height(120f);

	}

	private Button getButton (String text) {
		final Button button = new Button(skin);
		Label label = new Label(text, skin);
		button.add(label);
		return button;
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose () {
		stage.dispose();
		skin.dispose();
	}
}
