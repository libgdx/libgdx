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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Scaling;

public class ImageTest extends GdxTest {
	Skin skin;
	Stage ui;
	Table root;
	TextureRegion image2;

	@Override
	public void create () {
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		image2 = new TextureRegion(new Texture(Gdx.files.internal("data/badlogic.jpg")));
		ui = new Stage();
		Gdx.input.setInputProcessor(ui);

		root = new Table();
		root.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		ui.addActor(root);
		root.debug();

		Image image = new Image(image2);
		image.setScaling(Scaling.fill);
		root.add(image).width(image2.getRegionWidth()).height(image2.getRegionHeight());
	}

	@Override
	public void dispose () {
		ui.dispose();
		skin.dispose();
		image2.getTexture().dispose();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		ui.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		ui.draw();
	}

	@Override
	public void resize (int width, int height) {
		ui.getViewport().update(width, height, true);
	}
}
