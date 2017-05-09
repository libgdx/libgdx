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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class VibratorAdvancedTest extends GdxTest {
	Stage stage;
	TextButton vibratorCancelButton;
	TextButton vibratorSingleVibrationButton;
	TextButton vibratorPattern1Button;
	TextButton vibratorPattern2Button;
	TextButton vibratorPatternRepeated1Button;
	TextButton vibratorPatternRepeated2Button;

	@Override
	public void create () {
		stage = new Stage(new StretchViewport(800, 600));
		Gdx.input.setInputProcessor(stage);
		TextButtonStyle style = new TextButtonStyle();
		style.font = new BitmapFont();
		style.fontColor = Color.BLACK;
		style.downFontColor = Color.RED;
		vibratorCancelButton = new TextButton("Touch to cancel vibration", style);
		vibratorCancelButton.pad(10);
		vibratorCancelButton.addListener(new ClickListener() {

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				Gdx.input.cancelVibrate();
				Gdx.app.log("Advanced Vibration Test", "Canceled vibration");
			}

		});
		vibratorSingleVibrationButton = new TextButton("Touch to vibrate for 2000ms", style);
		vibratorSingleVibrationButton.pad(10);
		vibratorSingleVibrationButton.addListener(new ClickListener() {

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				Gdx.input.vibrate(2000);
				Gdx.app.log("Advanced Vibration Test", "Started vibration for 2000ms");
			}

		});
		vibratorPattern1Button = new TextButton("Touch to vibrate for 2000ms, wait 2000ms and vibrate 1000ms, no repeat", style);
		vibratorPattern1Button.pad(10);
		vibratorPattern1Button.addListener(new ClickListener() {

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				Gdx.input.vibrate(new long[] {0, 2000, 2000, 1000}, -1);
				Gdx.app.log("Advanced Vibration Test",
					"Started vibration for 2000ms, waiting for 2000ms, vibrating for 1000ms and no repeat");
			}

		});
		vibratorPattern2Button = new TextButton(
			"Touch to vibrate, wait 2000ms vibrate for 2000ms, wait for 1000ms and vibrate 200ms, no repeat", style);
		vibratorPattern2Button.pad(10);
		vibratorPattern2Button.addListener(new ClickListener() {

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				Gdx.input.vibrate(new long[] {2000, 2000, 1000, 200}, -1);
				Gdx.app.log("Advanced Vibration Test",
					"Started waiting for 2000ms, vibrating for 2000ms, waiting for 1000ms, vibrating for 200ms and no repeat");
			}

		});
		vibratorPatternRepeated1Button = new TextButton("Touch to vibrate, vibrate for 100ms wait for 4000ms, repeated @ index 0",
			style);
		vibratorPatternRepeated1Button.pad(10);
		vibratorPatternRepeated1Button.addListener(new ClickListener() {

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				Gdx.input.vibrate(new long[] {0, 100, 4000}, 0);
				Gdx.app.log("Advanced Vibration Test", "Started vibrating for 100ms, waiting for 4000ms and repeated @ indedx 0");
			}

		});

		vibratorPatternRepeated2Button = new TextButton("Touch to vibrate, vibrate for 2000ms wait for 2000ms, repeated @ index 1",
			style);
		vibratorPatternRepeated2Button.pad(10);
		vibratorPatternRepeated2Button.addListener(new ClickListener() {

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				Gdx.input.vibrate(new long[] {0, 2000, 2000}, 2);
				Gdx.app.log("Advanced Vibration Test", "Started vibrating for 2000ms, waiting for 2000ms and repeated @ indedx 1");
			}

		});

		Table table = new Table();
		table.setFillParent(true);
		table.add(vibratorCancelButton).row();
		table.add(vibratorPattern1Button).row();
		table.add(vibratorPattern2Button).row();
		table.add(vibratorPatternRepeated1Button).row();
		table.add(vibratorPatternRepeated2Button).row();
		table.add(vibratorSingleVibrationButton).bottom().row();
		stage.addActor(table);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act();
		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

}
