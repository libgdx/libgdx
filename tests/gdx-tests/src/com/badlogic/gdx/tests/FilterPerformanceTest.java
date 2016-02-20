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
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tests.utils.GdxTest;

public class FilterPerformanceTest extends GdxTest {
	SpriteBatch batch;
	Sprite sprite;
	Sprite sprite2;
	TextureAtlas atlas;
	Texture texture;
	Matrix4 sceneMatrix;
	Matrix4 textMatrix;
	BitmapFont font;
	int mode = 0;
	String modeString = "";
	int[] filters = {GL20.GL_NEAREST, GL20.GL_LINEAR, GL20.GL_NEAREST_MIPMAP_NEAREST, GL20.GL_LINEAR_MIPMAP_NEAREST,
		GL20.GL_LINEAR_MIPMAP_LINEAR};
	String[] filterNames = {"nearest", "linear", "nearest mipmap nearest", "linear mipmap nearest", "linear mipmap linear"};

	void setTextureFilter (int filter) {
		atlas.findRegion("map").getTexture().bind();
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, filters[filter]);
		texture.bind();
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, filters[filter]);
	}

	void setModeString () {
		modeString = (mode % 2 == 0 ? "Sprite" : "Atlas") + " " + filterNames[mode / 2];
	}

	public void create () {
		batch = new SpriteBatch();
		sceneMatrix = new Matrix4().setToOrtho2D(0, 0, 480, 320);
		textMatrix = new Matrix4().setToOrtho2D(0, 0, 480, 320);

		atlas = new TextureAtlas(Gdx.files.internal("data/issue_pack"), Gdx.files.internal("data/"));
		texture = new Texture(Gdx.files.internal("data/resource1.jpg"), true);
		texture.setFilter(TextureFilter.MipMap, TextureFilter.Nearest);
		setTextureFilter(0);
		setModeString();

		sprite = atlas.createSprite("map");
		sprite2 = new Sprite(texture, 0, 0, 855, 480);
		font = new BitmapFont(Gdx.files.internal("data/font.fnt"), Gdx.files.internal("data/font.png"), false);

		Gdx.input.setInputProcessor(new InputAdapter() {
			public boolean touchDown (int x, int y, int pointer, int newParam) {
				mode++;
				if (mode == filters.length * 2) mode = 0;
				setTextureFilter(mode / 2);
				setModeString();
				return false;
			}
		});
	}

	@Override
	public void dispose () {
		batch.dispose();
		atlas.dispose();
		texture.dispose();
		font.dispose();
	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(sceneMatrix);
		batch.begin();
		renderSprite();
		batch.end();

		batch.setProjectionMatrix(textMatrix);
		batch.begin();
		font.draw(batch, modeString + " fps:" + Gdx.graphics.getFramesPerSecond(), 26, 65);
		batch.end();
	}

	public void renderSprite () {
		batch.disableBlending();
		if (mode % 2 == 0)
			sprite2.draw(batch);
		else
			sprite.draw(batch);
		batch.enableBlending();
	}
}
