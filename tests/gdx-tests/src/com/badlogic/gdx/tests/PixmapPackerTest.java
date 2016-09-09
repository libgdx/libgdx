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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

public class PixmapPackerTest extends GdxTest {

	OrthographicCamera camera;
	SpriteBatch batch;
	ShapeRenderer shapeRenderer;

	TextureAtlas atlas;

	int pageToShow = 0;
	Array<TextureRegion> textureRegions;

	@Override
	public void create () {
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();

		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0);
		camera.update();

		Pixmap pixmap1 = new Pixmap(Gdx.files.internal("data/badlogic.jpg"));
		Pixmap pixmap2 = new Pixmap(Gdx.files.internal("data/particle-fire.png"));
		Pixmap pixmap3 = new Pixmap(Gdx.files.internal("data/isotile.png"));

		PixmapPacker packer = new PixmapPacker(1024, 1024, Format.RGBA8888, 8, false);
		for (int count = 1; count <= 3; ++count) {
			packer.pack("badlogic " + count, pixmap1);
			packer.pack("fire " + count, pixmap2);
			packer.pack("isotile " + count, pixmap3);
		}

		atlas = packer.generateTextureAtlas(TextureFilter.Nearest, TextureFilter.Nearest, false);
		Gdx.app.log("PixmapPackerTest", "Number of initial textures: " + atlas.getTextures().size);

		packer.setPackToTexture(true);

		for (int count = 4; count <= 10; ++count) {
			packer.pack("badlogic " + count, pixmap1);
			packer.pack("fire " + count, pixmap2);
			packer.pack("isotile " + count, pixmap3);
		}

		pixmap1.dispose();
		pixmap2.dispose();
		pixmap3.dispose();

		packer.updateTextureAtlas(atlas, TextureFilter.Nearest, TextureFilter.Nearest, false);
		textureRegions = new Array<TextureRegion>();
		packer.updateTextureRegions(textureRegions, TextureFilter.Nearest, TextureFilter.Nearest, false);
		Gdx.app.log("PixmapPackerTest", "Number of updated textures: " + atlas.getTextures().size);
		Gdx.input.setInputProcessor(new InputAdapter() {
			@Override
			public boolean keyDown (int keycode) {
				if (keycode >= Input.Keys.NUM_0 && keycode <= Input.Keys.NUM_9) {
					int number = keycode - Input.Keys.NUM_0;
					if (number < textureRegions.size) {
						pageToShow = number;
					}
				}
				return super.keyDown(keycode);
			}
		});
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		int size = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.begin();
		batch.draw(textureRegions.get(pageToShow), 0, 0, size, size);
		batch.end();
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.rect(0, 0, size, size);
		shapeRenderer.end();
	}

	@Override
	public void dispose () {
		atlas.dispose();
	}
}
