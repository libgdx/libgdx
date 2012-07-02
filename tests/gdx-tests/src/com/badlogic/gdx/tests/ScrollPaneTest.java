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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ActorEvent;
import com.badlogic.gdx.scenes.scene2d.ActorListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.utils.GdxTest;

public class ScrollPaneTest extends GdxTest {
	private Stage stage;
	private Table container;

	public void create () {
		stage = new Stage(0, 0, false);
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		Gdx.input.setInputProcessor(stage);

		Gdx.graphics.setVSync(false);

		container = new Table();
		stage.addActor(container);
		container.setFillParent(true);
		// container.getTableLayout().debug();

		Table table = new Table();

		final ScrollPane scroll = new ScrollPane(table, skin);

		ActorListener stopTouchDown = new ActorListener() {
			public boolean touchDown (ActorEvent event, float x, float y, int pointer, int button) {
				event.stop();
				return false;
			}
		};

		table.pad(10).defaults().expandX().space(4);
		for (int i = 0; i < 100; i++) {
			table.row();
			table.add(new Label(i + "uno", skin)).expandX().fillX();

			TextButton button = new TextButton(i + "dos", skin);
			table.add(button);
			button.addListener(new ClickListener() {
				public void clicked (ActorEvent event, float x, float y) {
					System.out.println("click " + x + ", " + y);
				}
			});

			Slider slider = new Slider(0, 100, 100, skin);
			slider.addListener(stopTouchDown); // Stops touchDown events from propagating to the FlickScrollPane.
			table.add(slider);

			table.add(new Label(i + "tres long0 long1 long2 long3 long4 long5 long6 long7 long8 long9 long10 long11 long12", skin));
		}

		final TextButton flickBbutton = new TextButton("Flick Scroll", skin.get("toggle", TextButtonStyle.class));
		flickBbutton.setChecked(true);
		flickBbutton.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				scroll.setFlickScroll(flickBbutton.isChecked());
			}
		});

		container.add(scroll).expand().fill();
		container.row();
		container.add(flickBbutton).pad(10);
	}

	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		Table.drawDebug(stage);
	}

	public void resize (int width, int height) {
		stage.setViewport(width, height, false);
	}

	public void dispose () {
		stage.dispose();
	}

	public boolean needsGL20 () {
		return false;
	}
}
