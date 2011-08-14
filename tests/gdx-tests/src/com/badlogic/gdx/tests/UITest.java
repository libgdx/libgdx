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
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ComboBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageToggleButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.ValueChangedListener;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.ui.ToggleButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.TableLayout;
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
		skin = new Skin(Gdx.files.internal("data/uiskin.xml"), Gdx.files.internal("data/uiskin.png"));
		skin.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		TextureRegion image = new TextureRegion(new Texture(Gdx.files.internal("data/badlogicsmall.jpg")));
		TextureRegion image2 = new TextureRegion(new Texture(Gdx.files.internal("data/badlogic.jpg")));
		ui = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		Gdx.input.setInputProcessor(ui);

		Window window = skin.newWindow("window", ui, "Dialog", 320, 240);
		window.x = window.y = 0;

		final Button button = skin.newButton("button-sl", "Single");
		final ToggleButton buttonMulti = skin.newToggleButton("button-ml-tgl", "Multi\nLine\nToggle");
		final ImageButton imgButton = skin.newImageButton("button-img", image);
		final ImageToggleButton imgToggleButton = skin.newImageToggleButton("button-img-tgl", image);
		final CheckBox checkBox = skin.newCheckBox("checkbox", "Check me");
		final Slider slider = skin.newSlider("slider", 100, 0, 10, 1);
		final TextField textfield = skin.newTextField("textfield", 100);
		final ComboBox combobox = skin.newComboBox("combo", new String[] {"Android", "Windows", "Linux", "OSX"}, ui);
		final Image imageActor = new Image("image", image2);
		final ScrollPane scrollPane = skin.newScrollPane("scroll", ui, imageActor, 100, 100);
		final List list = skin.newList("list", listEntries);
		final ScrollPane scrollPane2 = skin.newScrollPane("scroll2", ui, list, 100, 100);
		final SplitPane splitPane = skin.newSplitPane("split", ui, scrollPane, scrollPane2, false, 0, 0, "default-horizontal");
		final Label label = skin.newLabel("label", "fps:");

		imgButton.setImageSize(16, 20);
		imgToggleButton.setImageSize(10, 10);

		TableLayout layout = window.getTableLayout();
		layout.defaults().spaceBottom(10);
		layout.row().fill().expandX();
		layout.add(button);
		layout.add(buttonMulti);
		layout.add(imgButton);
		layout.add(imgToggleButton);
		layout.row();
		layout.add(checkBox);
		layout.add(slider).fillX().colspan(3);
		layout.row();
		layout.add(combobox);
		layout.add(textfield).expandX().fillX().colspan(3);
		layout.row();
		layout.add(splitPane).fill().expand().colspan(4);
		layout.row();
		layout.add(label).fill().expand();

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