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

package com.badlogic.gdx.tests.extensions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.utils.GdxTest;

/** Test for the gdx-controllers extension. */
public class ControllersTest extends GdxTest {
	String descriptor;
	Skin skin;
	Table ui;
	Stage stage;
	ScrollPane scrollPane;
	List<String> console;

	@Override
	public void create () {
		setupUi();
	}

	void print (String message) {
		String[] lines = console.getItems().toArray(String.class);
		String[] newLines = new String[lines.length + 1];
		System.arraycopy(lines, 0, newLines, 0, lines.length);
		newLines[newLines.length - 1] = message;
		console.setItems(newLines);
		scrollPane.invalidate();
		scrollPane.validate();
		scrollPane.setScrollPercentY(1.0f);
	}

	void clear () {
		console.setItems(new String[0]);
	}

	private void setupUi () {
		// setup a tiny ui with a console and a clear button.
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		stage = new Stage();
		ui = new Table();
		ui.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		console = new List(skin);
		scrollPane = new ScrollPane(console);
		scrollPane.setScrollbarsOnTop(true);
		TextButton clear = new TextButton("Clear", skin);
		ui.add(scrollPane).expand(true, true).fill();
		ui.row();
		ui.add(clear).expand(true, false).fill();
		stage.addActor(ui);
		clear.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				clear();
			}
		});
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void resize (int width, int height) {
		ui.setSize(width, height);
		ui.invalidate();
		stage.getViewport().update(width, height, true);
	}

	boolean initialized = false;

	private void initialize () {
		if (initialized) return;
		// print the currently connected controllers to the console
		print("Controllers: " + Controllers.getControllers().size);
		int i = 0;
		for (Controller controller : Controllers.getControllers()) {
			print("#" + i++ + ": " + controller.getName());
		}
		if (Controllers.getControllers().size == 0) print("No controllers attached");

		// setup the listener that prints events to the console
		Controllers.addListener(new ControllerListener() {
			public int indexOf (Controller controller) {
				return Controllers.getControllers().indexOf(controller, true);
			}

			@Override
			public void connected (Controller controller) {
				print("connected " + controller.getName());
				int i = 0;
				for (Controller c : Controllers.getControllers()) {
					print("#" + i++ + ": " + c.getName());
				}
			}

			@Override
			public void disconnected (Controller controller) {
				print("disconnected " + controller.getName());
				int i = 0;
				for (Controller c : Controllers.getControllers()) {
					print("#" + i++ + ": " + c.getName());
				}
				if (Controllers.getControllers().size == 0) print("No controllers attached");
			}

			@Override
			public boolean buttonDown (Controller controller, int buttonIndex) {
				print("#" + indexOf(controller) + ", button " + buttonIndex + " down");
				return false;
			}

			@Override
			public boolean buttonUp (Controller controller, int buttonIndex) {
				print("#" + indexOf(controller) + ", button " + buttonIndex + " up");
				return false;
			}

			@Override
			public boolean axisMoved (Controller controller, int axisIndex, float value) {
				print("#" + indexOf(controller) + ", axis " + axisIndex + ": " + value);
				return false;
			}

			@Override
			public boolean povMoved (Controller controller, int povIndex, PovDirection value) {
				print("#" + indexOf(controller) + ", pov " + povIndex + ": " + value);
				return false;
			}

			@Override
			public boolean xSliderMoved (Controller controller, int sliderIndex, boolean value) {
				print("#" + indexOf(controller) + ", x slider " + sliderIndex + ": " + value);
				return false;
			}

			@Override
			public boolean ySliderMoved (Controller controller, int sliderIndex, boolean value) {
				print("#" + indexOf(controller) + ", y slider " + sliderIndex + ": " + value);
				return false;
			}

			@Override
			public boolean accelerometerMoved (Controller controller, int accelerometerIndex, Vector3 value) {
				// not printing this as we get to many values
				return false;
			}
		});
		initialized = true;
	}

	@Override
	public void render () {
		initialize();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}
}
