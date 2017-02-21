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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.tests.utils.GdxTest;

public class ScrollPane2Test extends GdxTest {
	Stage stage;
	Skin skin;

	public void create () {
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		ScrollPane pane2 = new ScrollPane(new Image(new Texture("data/group-debug.png")), skin);
		pane2.setScrollingDisabled(false, true);
		// pane2.setCancelTouchFocus(false);
		pane2.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				event.stop();
				return true;
			}
		});

		Table mytable = new Table();
		mytable.debug();
		mytable.add(new Image(new Texture("data/group-debug.png")));
		mytable.row();
		mytable.add(new Image(new Texture("data/group-debug.png")));
		mytable.row();
		mytable.add(pane2).size(100);
		mytable.row();
		mytable.add(new Image(new Texture("data/group-debug.png")));
		mytable.row();
		mytable.add(new Image(new Texture("data/group-debug.png")));

		ScrollPane pane = new ScrollPane(mytable, skin);
		pane.setScrollingDisabled(true, false);
		// pane.setCancelTouchFocus(false);
		if (false) {
			// This sizes the pane to the size of it's contents.
			pane.pack();
			// Then the height is hardcoded, leaving the pane the width of it's contents.
			pane.setHeight(Gdx.graphics.getHeight());
		} else {
			// This shows a hardcoded size.
			pane.setWidth(300);
			pane.setHeight(Gdx.graphics.getHeight());
		}

		stage.addActor(pane);
	}

	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose () {
		stage.dispose();
		skin.dispose();
	}
}
