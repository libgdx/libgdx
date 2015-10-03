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
import com.badlogic.gdx.controllers.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

/** Test for the gdx-controllers extension. */
public class ControllersTest extends GdxTest {

	private Stage stage;
	private Table table;

	private Array<String> messages;
	private List<String> messageConsole;
	private ScrollPane messageScrollPane;

	private Array<ControlValue> values;
	private List<ControlValue> valueConsole;
	private ScrollPane valueScrollPane;

	@Override
	public void create () {
		setupUi();
	}

	private void setupUi () {
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		messages = new Array<String>();
		messageConsole = new List<String>(skin);
		messageScrollPane = new ScrollPane(messageConsole);
		messageScrollPane.setScrollbarsOnTop(true);

		values = new Array<ControlValue>();
		valueConsole = new List<ControlValue>(skin);
		valueScrollPane = new ScrollPane(valueConsole);
		valueScrollPane.setScrollbarsOnTop(true);

		TextButton clear = new TextButton("Clear", skin);
		clear.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				clear();
			}
		});

		table = new Table();
		table.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		table.add(messageScrollPane).expand(true, true).fill().width(Value.percentWidth(0.5f, table));
		table.add(valueScrollPane).expand(true, true).fill().width(Value.percentWidth(0.5f, table));
		table.row();
		table.add(clear).expand(true, false).fill().colspan(2);

		stage = new Stage();
		stage.addActor(table);
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void resize (int width, int height) {
		table.setSize(width, height);
		table.invalidate();
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void render () {
		initialize();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	private boolean initialized = false;

	private void initialize () {
		if (initialized) return;
		initialized = true;
		displayConnectedControllers();
		registerListener();
	}

	private void displayConnectedControllers () {
		addMessage("Controllers: " + Controllers.getControllers().size);
		int i = 0;
		for (Controller controller : Controllers.getControllers()) {
			addMessage("#" + (i++) + ": " + controller.getName());
		}
		if (Controllers.getControllers().size == 0) addMessage("No controllers attached");
	}

	private void registerListener () {
		Controllers.addListener(new ControllerListener() {

			public int indexOf (Controller controller) {
				return Controllers.getControllers().indexOf(controller, true);
			}

			@Override
			public void connected (Controller controller) {
				addMessage("connected " + controller.getName());
				int i = 0;
				for (Controller c : Controllers.getControllers()) {
					addMessage("#" + i++ + ": " + c.getName());
				}
			}

			@Override
			public void disconnected (Controller controller) {
				addMessage("disconnected " + controller.getName());
				int i = 0;
				for (Controller c : Controllers.getControllers()) {
					addMessage("#" + i++ + ": " + c.getName());
				}
				if (Controllers.getControllers().size == 0) addMessage("No controllers attached");
			}

			@Override
			public boolean buttonDown (Controller controller, int buttonIndex) {
				addMessage("#" + indexOf(controller) + ", button " + buttonIndex + " down");
				return false;
			}

			@Override
			public boolean buttonUp (Controller controller, int buttonIndex) {
				addMessage("#" + indexOf(controller) + ", button " + buttonIndex + " up");
				return false;
			}

			@Override
			public boolean povMoved (Controller controller, int povIndex, PovDirection value) {
				addMessage("#" + indexOf(controller) + ", pov " + povIndex + ": " + value);
				return false;
			}

			@Override
			public boolean xSliderMoved (Controller controller, int sliderIndex, boolean value) {
				addMessage("#" + indexOf(controller) + ", x slider " + sliderIndex + ": " + value);
				return false;
			}

			@Override
			public boolean ySliderMoved (Controller controller, int sliderIndex, boolean value) {
				addMessage("#" + indexOf(controller) + ", y slider " + sliderIndex + ": " + value);
				return false;
			}

			@Override
			public boolean axisMoved (Controller controller, int axisIndex, float value) {
				ControlValue controlValue = findControlValue(indexOf(controller), axisIndex, AxisValue.TYPE);
				if (controlValue != null) {
					AxisValue axisValue = (AxisValue)controlValue;
					axisValue.axisValue = value;
					updateValues();
				} else {
					addControlValue(new AxisValue(indexOf(controller), axisIndex, value));
				}
				return false;
			}

			@Override
			public boolean accelerometerMoved (Controller controller, int accelerometerIndex, Vector3 value) {
				ControlValue controlValue = findControlValue(indexOf(controller), accelerometerIndex, AccelerometerValue.TYPE);
				if (controller != null) {
					AccelerometerValue accelerometerValue = (AccelerometerValue)controlValue;
					accelerometerValue.accelerometerValue = value;
					updateValues();
				} else {
					addControlValue(new AccelerometerValue(indexOf(controller), accelerometerIndex, value));
				}
				return false;
			}

		});
	}

	void clear () {
		messages.clear();
		messageConsole.setItems(messages);
		values.clear();
		valueConsole.setItems(values);
	}

	void addMessage (String message) {
		messages.add(message);
		messageConsole.setItems(messages);
		messageScrollPane.invalidate();
		messageScrollPane.validate();
		messageScrollPane.setScrollPercentY(1.0f);
	}

	ControlValue findControlValue (int controller, int control, int type) {
		for (ControlValue value : values)
			if (value.controllerIndex == controller && value.controlIndex == control && value.controlType == type) return value;
		return null;
	}

	void addControlValue (ControlValue value) {
		values.add(value);
		valueConsole.setItems(values);
		valueScrollPane.invalidate();
		valueScrollPane.validate();
		valueScrollPane.setScrollPercentY(1.0f);
	}

	void updateValues () {
		valueConsole.setItems(values);
	}

	private static abstract class ControlValue {

		public final int controllerIndex;
		public final int controlIndex;
		public final int controlType;

		public ControlValue (int controllerIndex, int controlIndex, int controlType) {
			this.controllerIndex = controllerIndex;
			this.controlIndex = controlIndex;
			this.controlType = controlType;
		}

	}

	private static class AxisValue extends ControlValue {

		public static final int TYPE = 0;

		public float axisValue;

		public AxisValue (int controllerIndex, int controlIndex, float axisValue) {
			super(controllerIndex, controlIndex, TYPE);
			this.axisValue = axisValue;
		}

		@Override
		public String toString () {
			return "#" + controllerIndex + ", axis " + controlIndex + ": " + axisValue;
		}

	}

	private static class AccelerometerValue extends ControlValue {

		public static final int TYPE = 1;

		public Vector3 accelerometerValue;

		public AccelerometerValue (int controllerIndex, int controlIndex, Vector3 accelerometerValue) {
			super(controllerIndex, controlIndex, TYPE);
			this.accelerometerValue = accelerometerValue;
		}

		@Override
		public String toString () {
			return "#" + controllerIndex + ", accelerometer " + controlIndex + ": " + accelerometerValue;
		}

	}

}
