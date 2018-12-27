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

package com.badlogic.gdx.tests.gwt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.utils.GdxTest;

public class GwtWindowModeTest extends GdxTest {
	Stage stage;

	private boolean windowState;
	private TextButton tb;
	private final String window_mode_txt = "click for Full screen Mode";
	private final String fullscreen_mode_txt = "click for window Mode";

	public void create () {
		stage = new Stage();
		windowState = true;
		Gdx.input.setInputProcessor(stage);
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		tb = new TextButton(window_mode_txt, skin);
		tb.setSize(Gdx.graphics.getWidth()/2,Gdx.graphics.getHeight()/2);
		tb.setPosition(Gdx.graphics.getWidth()/4,Gdx.graphics.getHeight()/4);
		stage.addActor(tb);

		tb.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				if(windowState){
					windowState = false;
					tb.setText(fullscreen_mode_txt);
					Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
				}
				else{
					windowState = true;
					tb.setText(window_mode_txt);
					Gdx.graphics.setWindowedMode(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
				}
			}
		});


	}

	public void render () {
		// System.out.println(meow.getValue());
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
}
