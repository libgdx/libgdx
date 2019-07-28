/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Align;

public class ScrollPaneWithDynamicScrolling extends GdxTest {
	private Stage stage;
	private Table container;
	Label dynamicLabel;
	ScrollPane scrollPane;
	int count;
	
	public void create () {
		stage = new Stage();
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		Gdx.input.setInputProcessor(stage);
		
		dynamicLabel = new Label("Chat box begin here", skin);

		float chatWidth = 200;
		float controlHeight = 300;
		
		scrollPane = new ScrollPane(dynamicLabel, skin);

		Table main = new Table();
		
		main.setFillParent(true);
		
		TextButton btAdd = new TextButton("Add text and scroll down", skin);
		
		main.add(btAdd).row();
		main.add(scrollPane).size(200, 100);
		
		stage.addActor(main);
		
		stage.setDebugAll(true);
		
		btAdd.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				dynamicLabel.setText(dynamicLabel.getText() + "\nline " + count++);
				scrollPane.scrollTo(0, 0, 0, 0);
			}
		});
		
	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	public void dispose () {
		stage.dispose();
	}

	public boolean needsGL20 () {
		return false;
	}
}
