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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.tests.utils.GdxTest;

public class GroupCullingTest extends GdxTest {
	static private final int count = 100;

	private Stage stage;
	private Skin skin;
	private Table root;
	private Label drawnLabel;
	int drawn;

	public void create () {
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		root = new Table();
		root.setFillParent(true);
		stage.addActor(root);

		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		Table labels = new Table();
		root.add(new ScrollPane(labels, skin)).expand().fill();
		root.row();
		root.add(drawnLabel = new Label("", skin));

		for (int i = 0; i < count; i++) {
			labels.add(new Label("Label: " + i, skin) {
				public void draw (Batch batch, float parentAlpha) {
					super.draw(batch, parentAlpha);
					drawn++;
				}
			});
			labels.row();
		}
	}

	@Override
	public void dispose () {
		stage.dispose();
		skin.dispose();
	}

	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
		root.invalidate();
	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		drawn = 0;
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		drawnLabel.setText("Drawn: " + drawn + "/" + count);
		drawnLabel.invalidateHierarchy();
	}

	public boolean needsGL20 () {
		return false;
	}
}
