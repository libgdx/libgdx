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
import com.badlogic.gdx.Input.OnscreenKeyboardType;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.input.NativeInputConfiguration;
import com.badlogic.gdx.input.TextInputWrapper;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.NativeOnscreenKeyboard;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.utils.GdxTest;

public class NativeInputTest extends GdxTest {

	private Stage stage;
	private Skin skin;

	private SelectBox<OnscreenKeyboardType> keyboardTypeSelect;

	private CheckBox maskInputButton;
	private CheckBox showUnmaskButton;
	private CheckBox multilineButton;
	private CheckBox noAutocorrectButton;
	private CheckBox useValidatorButton;
	private CheckBox useCustomAutocompleteButton;

	private TextField placeHolderField;
	private Slider maxLengthSlider;

	private TextArea resultArea;

	public void create () {
		stage = new Stage();
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		Table table = new Table();
		table.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (!event.isStopped()) {
					Gdx.input.closeTextInputField(false);
				}
				super.clicked(event, x, y);
			}
		});
		table.setFillParent(true);

		keyboardTypeSelect = new SelectBox<>(skin);
		keyboardTypeSelect.setItems(OnscreenKeyboardType.values());
		keyboardTypeSelect.setWidth(200);
		keyboardTypeSelect.setPosition(200, 200);

		Label maxLengthLabel = new Label("--", skin);
		maxLengthSlider = new Slider(0, 15, 1, false, skin);
		maxLengthSlider.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				if (maxLengthSlider.getValue() == 0)
					maxLengthLabel.setText("--");
				else
					maxLengthLabel.setText((int)maxLengthSlider.getValue());
			}
		});

		maskInputButton = new CheckBox("Mask Input", skin);
		showUnmaskButton = new CheckBox("Show Password button", skin);
		multilineButton = new CheckBox("Multiline", skin);
		noAutocorrectButton = new CheckBox("No Autocorrect", skin);
		useValidatorButton = new CheckBox("Use validator", skin);
		useCustomAutocompleteButton = new CheckBox("Custom Autocomplete", skin);

		Label placeHodlerLabel = new Label("Placeholder:", skin);
		placeHolderField = new TextField(null, skin);
		placeHolderField.setOnscreenKeyboard(new NativeOnscreenKeyboard());
		placeHolderField.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {

				super.clicked(event, x, y);
				event.stop();
			}
		});

		resultArea = new TextArea(null, skin);
		resultArea.setDisabled(true);

		TextButton openInput = new TextButton("Open TextInput", skin);
		openInput.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (Gdx.input.isTextInputFieldOpened()) {
					Gdx.input.closeTextInputField(false, (confirmative) -> {
						openNativeInputField();
						return true;
					});
				} else {
					openNativeInputField();
				}

				event.stop();
			}
		});

		HorizontalGroup g1 = new HorizontalGroup();
		g1.addActor(keyboardTypeSelect);
		g1.addActor(maxLengthSlider);
		g1.addActor(maxLengthLabel);
		table.add(g1);
		table.row();
		HorizontalGroup g2 = new HorizontalGroup();
		g2.space(5);
		g2.addActor(maskInputButton);
		g2.addActor(showUnmaskButton);
		g2.addActor(multilineButton);
		g2.addActor(noAutocorrectButton);
		g2.addActor(useValidatorButton);
		table.add(g2);
		table.row();

		HorizontalGroup g3 = new HorizontalGroup();
		g3.addActor(placeHodlerLabel);
		g3.addActor(placeHolderField);
		table.add(g3);
		table.row();
		table.add(useCustomAutocompleteButton);
		table.row().padTop(15);
		table.add(openInput);
		table.row().padTop(15);
		table.add(resultArea).grow();

		stage.addActor(table);

		Gdx.input.setInputProcessor(stage);
	}

	public void openNativeInputField () {
		NativeInputConfiguration configuration = new NativeInputConfiguration();
		configuration.setPreventCorrection(noAutocorrectButton.isChecked()).setMultiLine(multilineButton.isChecked())
			.setMaskInput(maskInputButton.isChecked()).setShowUnmaskButton(showUnmaskButton.isChecked())
			.setPlaceholder(placeHolderField.getText()).setType(keyboardTypeSelect.getSelected());
		if (useCustomAutocompleteButton.isChecked())
			configuration.setAutoComplete(new String[] {"Hello", "Hillo", "Hellale", "Dog", "Dogfood"});
		if (maxLengthSlider.getValue() != 0) configuration.setMaxLength((int)maxLengthSlider.getValue());
		if (useValidatorButton.isChecked()) configuration.setValidator(toCheck -> !toCheck.contains("!"));

		configuration.setTextInputWrapper(new TextInputWrapper() {
			@Override
			public String getText () {
				return resultArea.getText();
			}

			@Override
			public int getSelectionStart () {
				return resultArea.getCursorPosition();
			}

			@Override
			public int getSelectionEnd () {
				return resultArea.getCursorPosition();
			}

			@Override
			public void writeResults (String text, int selectionStart, int selectionEnd) {
				resultArea.setText(text);
				resultArea.setSelection(selectionStart, selectionEnd);
			}
		});
		try {
			configuration.validate();
			Gdx.input.openTextInputField(configuration);
		} catch (IllegalArgumentException e) {
			resultArea.setText(e.getMessage());
		}
	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}
}
