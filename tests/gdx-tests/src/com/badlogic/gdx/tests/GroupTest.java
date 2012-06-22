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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.tests.utils.GdxTest;

public class GroupTest extends GdxTest {
	Stage stage;
	TextureRegion region;

	public void create () {
		stage = new Stage(0, 0, false);
		Gdx.input.setInputProcessor(stage);

		region = new TextureRegion(new Texture(Gdx.files.internal("data/group-debug.png")));

		Group group1 = new ImageGroup();
		group1.setRotation(30);
		group1.setTransform(true);
		group1.setBounds(50, 50, 150, 150);
		stage.addActor(group1);

		Group group2 = new ImageGroup();
		group2.setTransform(false);
		group2.setBounds(50, 50, 50, 50);
		group1.addActor(group2);

		Group group3 = new ImageGroup();
		group3.setTransform(true);
		group3.setBounds(10, 10, 35, 35);
		group3.setOriginX(100);
		group3.setRotation(45);
		group2.addActor(group3);

		Group group4 = new ImageGroup();
		group4.setTransform(false);
		group4.setBounds(5, 5, 25, 25);
		group4.setOriginX(100);
		group4.setRotation(45);
		group3.addActor(group4);
	}

	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.draw();
	}

	public void resize (int width, int height) {
		stage.setViewport(width, height, false);
	}

	public boolean needsGL20 () {
		return false;
	}

	class ImageGroup extends Group {
		public void draw (SpriteBatch batch, float parentAlpha) {
			batch.setColor(getColor());
			batch.draw(region, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(),
				getRotation());
			super.draw(batch, parentAlpha);
		}
	}
}
