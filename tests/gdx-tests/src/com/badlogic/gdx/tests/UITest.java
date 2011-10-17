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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ComboBox;
import com.badlogic.gdx.scenes.scene2d.ui.ComboBox.ComboBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.FlickScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.ValueChangedListener;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane.SplitPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.tests.utils.GdxTest;

public class UITest extends GdxTest {
	String[] listEntries = {"This is a list entry", "And another one", "The meaning of life", "Is hard to come by",
		"This is a list entry", "And another one", "The meaning of life", "Is hard to come by", "This is a list entry",
		"And another one", "The meaning of life", "Is hard to come by", "This is a list entry", "And another one",
		"The meaning of life", "Is hard to come by", "This is a list entry", "And another one", "The meaning of life",
		"Is hard to come by"};

	Skin skin;
	Stage ui;
	SpriteBatch batch;
	Actor root;

	@Override
	public void create () {
		batch = new SpriteBatch();
		skin = new Skin(Gdx.files.internal("data/uiskin.json"), Gdx.files.internal("data/uiskin.png"));
		TextureRegion image = new TextureRegion(new Texture(Gdx.files.internal("data/badlogicsmall.jpg")));
		TextureRegion image2 = new TextureRegion(new Texture(Gdx.files.internal("data/badlogic.jpg")));
		ui = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		Gdx.input.setInputProcessor(ui);

		Window window = new Window("window", "Dialog", ui, skin.getStyle(WindowStyle.class), 420, 440);
		window.x = window.y = 0;

		// Group.debug = true;

		final Button button = new Button("Single", skin.getStyle(ButtonStyle.class), "button-sl");
		final Button buttonMulti = new Button("Multi\nLine\nToggle", skin.getStyle("toggle", ButtonStyle.class), "button-ml-tgl");
		final Button imgButton = new Button(new Image(image), skin.getStyle(ButtonStyle.class));
		final Button imgToggleButton = new Button(new Image(image), skin.getStyle("toggle", ButtonStyle.class));
		final CheckBox checkBox = new CheckBox("Check me", skin.getStyle(CheckBoxStyle.class), "checkbox");
		final Slider slider = new Slider(0, 10, 1, skin.getStyle(SliderStyle.class), "slider");
		final TextField textfield = new TextField("", "Click here!", skin.getStyle(TextFieldStyle.class), "textfield");
		final ComboBox combobox = new ComboBox(new String[] {"Android", "Windows", "Linux", "OSX"}, ui,
			skin.getStyle(ComboBoxStyle.class), "combo");
		final Image imageActor = new Image(image2);
		final FlickScrollPane scrollPane = new FlickScrollPane(imageActor, ui, "flickscroll");
		final List list = new List(listEntries, skin.getStyle(ListStyle.class), "list");
		final ScrollPane scrollPane2 = new ScrollPane(list, ui, skin.getStyle(ScrollPaneStyle.class), "scroll");
		final SplitPane splitPane = new SplitPane(scrollPane, scrollPane2, false, ui, skin.getStyle("default-horizontal",
			SplitPaneStyle.class), "split");
		final Label fpsLabel = new Label("fps:", skin.getStyle(LabelStyle.class), "label");

		// window.debug();
		window.defaults().spaceBottom(10);
		window.row().fill().expandX();
		window.add(button).fill(0f, 0f);
		window.add(buttonMulti);
		window.add(imgButton);
		window.add(imgToggleButton);
		window.row();
		window.add(checkBox);
		window.add(slider).minWidth(100).fillX().colspan(3);
		window.row();
		window.add(combobox);
		window.add(textfield).minWidth(100).expandX().fillX().colspan(3);
		window.row();
		window.add(splitPane).fill().expand().colspan(4).minHeight(200);
		window.row();
		window.add(fpsLabel).colspan(4);

		textfield.setTextFieldListener(new TextFieldListener() {
			public void keyTyped (TextField textField, char key) {
				if (key == '\n') textField.getOnscreenKeyboard().show(false);
			}
		});

		slider.setValueChangedListener(new ValueChangedListener() {
			public void changed (Slider slider, float value) {
				Gdx.app.log("UITest", "slider: " + value);
			}
		});

		ui.addActor(window);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		((Label)ui.findActor("label")).setText("fps: " + Gdx.graphics.getFramesPerSecond());

		ui.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		ui.draw();
		Table.drawDebug(ui);
	}

	@Override
	public void resize (int width, int height) {
		ui.setViewport(width, height, false);
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}
}
