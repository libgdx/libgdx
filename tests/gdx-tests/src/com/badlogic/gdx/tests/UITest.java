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
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actors.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ComboBox;
import com.badlogic.gdx.scenes.scene2d.ui.ComboBox.ComboBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.FlickScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageToggleButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageToggleButton.ImageToggleButtonStyle;
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
import com.badlogic.gdx.scenes.scene2d.ui.ToggleButton;
import com.badlogic.gdx.scenes.scene2d.ui.ToggleButton.ToggleButtonStyle;
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
		skin.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		TextureRegion image = new TextureRegion(new Texture(Gdx.files.internal("data/badlogicsmall.jpg")));
		TextureRegion image2 = new TextureRegion(new Texture(Gdx.files.internal("data/badlogic.jpg")));
		ui = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		Gdx.input.setInputProcessor(ui);

		Window window = new Window("window", "Dialog", ui, skin.getStyle(WindowStyle.class), 420, 440);
		window.x = window.y = 0;

		// Group.debug = true;

		final Button button = new Button("button-sl", "Single", skin.getStyle(ButtonStyle.class));
		final ToggleButton buttonMulti = new ToggleButton("button-ml-tgl", "Multi\nLine\nToggle",
			skin.getStyle(ToggleButtonStyle.class));
		final ImageButton imgButton = new ImageButton("button-img", image, skin.getStyle(ImageButtonStyle.class));
		final ImageToggleButton imgToggleButton = new ImageToggleButton("button-img-tgl", image,
			skin.getStyle(ImageToggleButtonStyle.class));
		final CheckBox checkBox = new CheckBox("checkbox", "Check me", skin.getStyle(CheckBoxStyle.class));
		final Slider slider = new Slider("slider", 0, 10, 1, skin.getStyle(SliderStyle.class), 100);
		final TextField textfield = new TextField("textfield", "", skin.getStyle(TextFieldStyle.class), 100);
		final ComboBox combobox = new ComboBox("combo", new String[] {"Android", "Windows", "Linux", "OSX"}, ui,
			skin.getStyle(ComboBoxStyle.class));
		// BOZO - Need an image actor in UI package that has a pref size separate from the actor size.
		final Image imageActor = new Image("image", image2);
		final FlickScrollPane scrollPane = new FlickScrollPane("scroll", imageActor, ui, 0, 0);
		final List list = new List("list", listEntries, skin.getStyle(ListStyle.class));
		final ScrollPane scrollPane2 = new ScrollPane("scroll2", list, ui, skin.getStyle(ScrollPaneStyle.class), 0, 0);
		final SplitPane splitPane = new SplitPane("split", scrollPane, scrollPane2, false, ui, skin.getStyle("default-horizontal",
			SplitPaneStyle.class), 0, 0);
		final Label fpsLabel = new Label("label", "fps:", skin.getStyle(LabelStyle.class));

		imgButton.setImageSize(16, 20);
		imgToggleButton.setImageSize(10, 10);

		// window.debug();
		window.defaults().spaceBottom(10);
		window.row().fill().expandX();
		window.add(button).fill(0f, 0f);
		window.add(buttonMulti);
		window.add(imgButton);
		window.add(imgToggleButton);
		window.row();
		window.add(checkBox);
		window.add(slider).fillX().colspan(3);
		window.row();
		window.add(combobox);
		window.add(textfield).expandX().fillX().colspan(3);
		window.row();
		window.add(splitPane).fill().expand().colspan(4).minHeight(200);
		window.row();
		window.add(fpsLabel).colspan(4);

		textfield.setTextFieldListener(new TextFieldListener() {
			@Override
			public void keyTyped (TextField textField, char key) {
				if (key == '\n') textField.getOnscreenKeyboard().show(false);
			}
		});

		slider.setValueChangedListener(new ValueChangedListener() {

			@Override
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
