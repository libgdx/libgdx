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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.input.NativeInputConfiguration;
import com.badlogic.gdx.input.TextInputWrapper;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.NativeOnscreenKeyboard;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class NativeInputTest extends GdxTest {

	/** The world size of the viewport's short edge: the controls column is designed to fill this width in portrait, so phones
	 * scale up until it spans the screen. */
	private static final float TARGET_SHORT_EDGE = 280;
	/** On large screens (tablets/desktop) filling the short edge would blow the widgets up comically — cap the scale at this
	 * factor of the density-independent (dp) size instead. */
	private static final float MAX_DENSITY_SCALE = 1.5f;

	private Stage stage;
	private Skin skin;
	private Table rootTable;

	private SelectBox<OnscreenKeyboardType> keyboardTypeSelect;

	private CheckBox maskInputButton;
	private CheckBox showUnmaskButton;
	private CheckBox multilineButton;
	private CheckBox noAutocorrectButton;
	private CheckBox useValidatorButton;
	private CheckBox useCustomAutocompleteButton;
	private CheckBox customColorsButton;
	private SelectBox<NativeInputConfiguration.WriteMode> writeModeSelect;
	private SelectBox<NativeInputConfiguration.ReturnKeyType> returnKeySelect;
	private SelectBox<String> contentTypeSelect;
	private SelectBox<String> autocapitalizationSelect;

	private TextField placeHolderField;
	private Slider maxLengthSlider;
	private Slider autoCompleteThresholdSlider;
	private Slider cornerRadiusSlider;
	private Slider textMarginSlider;

	private TextArea resultArea;

	public void create () {
		ScreenViewport viewport = new ScreenViewport();
		viewport.setUnitsPerPixel(computeUnitsPerPixel());
		stage = new Stage(viewport);
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		rootTable = new Table();
		rootTable.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (!event.isStopped()) {
					Gdx.input.closeTextInputField(false);
				}
				super.clicked(event, x, y);
			}
		});
		rootTable.setFillParent(true);

		InputListener stopTouchDown = new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				event.stop();
				return false;
			}
		};

		keyboardTypeSelect = new SelectBox<>(skin);
		keyboardTypeSelect.setItems(OnscreenKeyboardType.values());

		Label maxLengthLabel = new Label("--", skin);
		maxLengthSlider = new Slider(0, 15, 1, false, skin);
		maxLengthSlider.addListener(stopTouchDown); // Stops touchDown events from propagating to the ScrollPane.
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
		customColorsButton = new CheckBox("Custom colors", skin);
		writeModeSelect = new SelectBox<>(skin);
		writeModeSelect.setItems(NativeInputConfiguration.WriteMode.values());
		writeModeSelect.setSelected(NativeInputConfiguration.WriteMode.ONLY_FINAL);
		returnKeySelect = new SelectBox<>(skin);
		returnKeySelect.setItems(NativeInputConfiguration.ReturnKeyType.values());
		returnKeySelect.setSelected(NativeInputConfiguration.ReturnKeyType.DONE);
		contentTypeSelect = new SelectBox<>(skin);
		Array<String> contentTypes = new Array<>();
		contentTypes.add("No content type");
		for (NativeInputConfiguration.ContentType value : NativeInputConfiguration.ContentType.values()) {
			contentTypes.add(value.name());
		}
		contentTypeSelect.setItems(contentTypes);
		autocapitalizationSelect = new SelectBox<>(skin);
		Array<String> autocapitalizations = new Array<>();
		autocapitalizations.add("Default capitalization");
		for (NativeInputConfiguration.Autocapitalization value : NativeInputConfiguration.Autocapitalization.values()) {
			autocapitalizations.add(value.name());
		}
		autocapitalizationSelect.setItems(autocapitalizations);

		Label autoCompleteThresholdLabel = new Label("0", skin);
		autoCompleteThresholdSlider = new Slider(0, 5, 1, false, skin);
		autoCompleteThresholdSlider.addListener(stopTouchDown);
		autoCompleteThresholdSlider.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				autoCompleteThresholdLabel.setText((int)autoCompleteThresholdSlider.getValue());
			}
		});

		Label textMarginLabel = new Label("10", skin);
		textMarginSlider = new Slider(0, 30, 1, false, skin);
		textMarginSlider.setValue(10);
		textMarginSlider.addListener(stopTouchDown);
		textMarginSlider.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				textMarginLabel.setText((int)textMarginSlider.getValue());
			}
		});

		Label cornerRadiusLabel = new Label("10", skin);
		cornerRadiusSlider = new Slider(0, 30, 1, false, skin);
		cornerRadiusSlider.setValue(10);
		cornerRadiusSlider.addListener(stopTouchDown);
		cornerRadiusSlider.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				cornerRadiusLabel.setText((int)cornerRadiusSlider.getValue());
			}
		});

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

		// Single full-width column: side-by-side label/control pairs don't survive narrow phone-width worlds
		Table sections = new Table();
		sections.top();
		sections.defaults().growX().left().pad(4);

		sections.add(sectionHeader("Keyboard")).padTop(0);
		sections.row();
		sections.add(labeled("Type:", keyboardTypeSelect));
		sections.row();
		sections.add(labeled("Return key:", returnKeySelect));
		sections.row();
		sections.add(labeled("Content type:", contentTypeSelect));
		sections.row();
		sections.add(labeled("Capitalization:", autocapitalizationSelect));
		sections.row();

		sections.add(sectionHeader("Behavior"));
		sections.row();
		sections.add(multilineButton).fill(false, false);
		sections.row();
		sections.add(noAutocorrectButton).fill(false, false);
		sections.row();
		sections.add(useValidatorButton).fill(false, false);
		sections.row();
		sections.add(useCustomAutocompleteButton).fill(false, false);
		sections.row();
		sections.add(labeled("Autocomplete threshold:", sliderRow(autoCompleteThresholdSlider, autoCompleteThresholdLabel)));
		sections.row();
		sections.add(labeled("Write mode:", writeModeSelect));
		sections.row();
		sections.add(labeled("Max length:", sliderRow(maxLengthSlider, maxLengthLabel)));
		sections.row();

		sections.add(sectionHeader("Password"));
		sections.row();
		sections.add(maskInputButton).fill(false, false);
		sections.row();
		sections.add(showUnmaskButton).fill(false, false);
		sections.row();

		sections.add(sectionHeader("Appearance"));
		sections.row();
		sections.add(customColorsButton).fill(false, false);
		sections.row();
		sections.add(labeled("Corner radius:", sliderRow(cornerRadiusSlider, cornerRadiusLabel)));
		sections.row();
		sections.add(labeled("Text margin:", sliderRow(textMarginSlider, textMarginLabel)));
		sections.row();

		sections.add(sectionHeader("Placeholder"));
		sections.row();
		sections.add(placeHolderField);
		sections.row();

		ScrollPane scrollPane = new ScrollPane(sections, skin);
		scrollPane.setScrollingDisabled(true, false);
		scrollPane.setFadeScrollBars(false);

		rootTable.add(scrollPane).grow().padLeft(8).padRight(8);
		rootTable.row();
		rootTable.add(openInput).growX().padLeft(8).padRight(8).padTop(8);
		rootTable.row();
		rootTable.add(resultArea).growX().height(96).pad(8);

		stage.addActor(rootTable);
		applySafeInsets();

		Gdx.input.setInputProcessor(stage);
	}

	private Label sectionHeader (String title) {
		Label label = new Label(title, skin);
		label.setColor(Color.SKY);
		return label;
	}

	/** Stacks a label on top of its control; full-width rows survive narrow (phone) worlds where side-by-side columns don't. */
	private Table labeled (String text, Actor control) {
		Table table = new Table();
		table.add(new Label(text, skin)).left().padBottom(2);
		table.row();
		table.add(control).growX();
		return table;
	}

	private Table sliderRow (Slider slider, Label valueLabel) {
		Table table = new Table();
		table.add(slider).growX();
		table.add(valueLabel).width(35).padLeft(8);
		return table;
	}

	/** Scales the UI so the controls fill the screen's short edge on phones, capped on big screens so widgets stay a sane physical
	 * size. */
	private float computeUnitsPerPixel () {
		float shortEdge = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		// Viewport pixels per dp; the width/backBufferWidth ratio corrects for point-based (HdpiMode.Logical) backends, where
		// the viewport works in points but getDensity() reports physical pixel density
		float pixelsPerDp = Gdx.graphics.getDensity() * Gdx.graphics.getWidth() / (float)Gdx.graphics.getBackBufferWidth();
		float pixelsPerUnit = Math.min(shortEdge / TARGET_SHORT_EDGE, pixelsPerDp * MAX_DENSITY_SCALE);
		return 1f / pixelsPerUnit;
	}

	private void applySafeInsets () {
		float unitsPerPixel = ((ScreenViewport)stage.getViewport()).getUnitsPerPixel();
		rootTable.padLeft(Gdx.graphics.getSafeInsetLeft() * unitsPerPixel)
			.padRight(Gdx.graphics.getSafeInsetRight() * unitsPerPixel).padTop(Gdx.graphics.getSafeInsetTop() * unitsPerPixel)
			.padBottom(Gdx.graphics.getSafeInsetBottom() * unitsPerPixel);
		rootTable.invalidate();
	}

	@Override
	public void resize (int width, int height) {
		((ScreenViewport)stage.getViewport()).setUnitsPerPixel(computeUnitsPerPixel());
		stage.getViewport().update(width, height, true);
		applySafeInsets();
	}

	public void openNativeInputField () {
		NativeInputConfiguration configuration = new NativeInputConfiguration();
		configuration.setPreventCorrection(noAutocorrectButton.isChecked()).setMultiLine(multilineButton.isChecked())
			.setMaskInput(maskInputButton.isChecked()).setShowUnmaskButton(showUnmaskButton.isChecked())
			.setPlaceholder(placeHolderField.getText()).setType(keyboardTypeSelect.getSelected())
			.setWriteMode(writeModeSelect.getSelected()).setReturnKeyType(returnKeySelect.getSelected())
			.setCornerRadius(cornerRadiusSlider.getValue()).setTextMargin(textMarginSlider.getValue());
		if (contentTypeSelect.getSelectedIndex() != 0)
			configuration.setContentType(NativeInputConfiguration.ContentType.valueOf(contentTypeSelect.getSelected()));
		if (autocapitalizationSelect.getSelectedIndex() != 0) configuration
			.setAutocapitalization(NativeInputConfiguration.Autocapitalization.valueOf(autocapitalizationSelect.getSelected()));
		if (customColorsButton.isChecked()) {
			configuration.setBackgroundColor(new Color(0x202030ff)).setTextColor(new Color(Color.WHITE))
				.setPlaceholderColor(new Color(Color.ORANGE));
		}
		if (useCustomAutocompleteButton.isChecked())
			configuration.setAutoComplete(new String[] {"Hello", "Hillo", "Hellale", "Dog", "Dogfood"})
				.setAutoCompleteThreshold((int)autoCompleteThresholdSlider.getValue());
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
				if (selectionStart == selectionEnd) {
					resultArea.setCursorPosition(selectionEnd);
				} else {
					resultArea.setSelection(selectionStart, selectionEnd);
				}
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
