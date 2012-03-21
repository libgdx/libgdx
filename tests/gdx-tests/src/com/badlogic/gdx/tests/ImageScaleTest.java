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
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Scaling;

public class ImageScaleTest extends GdxTest {
	Stage stage;
	Texture texture;

	public void create () {
		stage = new Stage(0, 0, false);
		Gdx.input.setInputProcessor(stage);

		texture = new Texture("data/group-debug.png");
		Image image = new Image(texture, Scaling.fit);
		image.x = image.y = 100;
		image.width = 400;
		image.height = 200;
		stage.addActor(image);

		Image image2 = new Image(texture, Scaling.fit);
		image2.x = image2.y = 100;
		image2.width = 400;
		image2.height = 200;
		image2.originX = 200;
		image2.originY = 100;
		image2.scaleX = image2.scaleY = 0.5f;
		stage.addActor(image2);

	}

	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.draw();
	}
	
	@Override
	public void dispose () {
		stage.dispose();
		texture.dispose();
	}

	public void resize (int width, int height) {
		stage.setViewport(width, height, false);
	}

	public boolean needsGL20 () {
		return false;
	}
}
