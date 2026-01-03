/*******************************************************************************
 * Copyright 2026 See AUTHORS file.
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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.ScreenUtils;

/** Tests whether the cursor stays at the right position in a TextArea when cut/copy/paste are used. */
public class TextAreaTest4 extends GdxTest {
	private Stage stage;
	private Skin skin;
	private boolean cancelled = false;

	@Override
	public void create () {
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		final TextArea textArea = new TextArea("Line 1\nLine 2\n0123456789\nABCDEFGHIJ", skin);
		textArea.setBounds(20, 20, 300, 200);

		textArea.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				if (cancelled) {
					event.cancel();
					Gdx.app.log("Test", "ChangeEvent Cancelled.");
				}
			}
		});

		Table table = new Table();
		table.setFillParent(true);
		table.top().right().pad(20);

		final CheckBox cancelToggle = new CheckBox(" Cancel Event", skin);
		cancelToggle.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				cancelled = cancelToggle.isChecked();
			}
		});

		final Label statusLabel = new Label("", skin);

		table.add(cancelToggle).left().row();
		table.add(new Label("--- Status ---", skin)).padTop(10).row();
		table.add(statusLabel).left();

		stage.addActor(textArea);
		stage.addActor(table);

		stage.getRoot().addCaptureListener(new InputListener() {
			@Override
			public boolean mouseMoved (InputEvent event, float x, float y) {
				updateStatus(textArea, statusLabel);
				return false;
			}

			@Override
			public boolean keyTyped (InputEvent event, char character) {
				updateStatus(textArea, statusLabel);
				return false;
			}
		});
	}

	private void updateStatus (TextArea textArea, Label label) {
		String info = "Cursor: " + textArea.getCursorPosition() + "\n" + "Selection: " + textArea.getSelectionStart() + "\n"
			+ "Text Len: " + textArea.getText().length();
		label.setText(info);
	}

	@Override
	public void render () {
		ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose () {
		stage.dispose();
		skin.dispose();
	}
}
