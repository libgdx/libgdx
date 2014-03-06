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
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.tests.utils.GdxTest;

public class PixmapPackerTest extends GdxTest {

	OrthographicCamera camera;
	SpriteBatch batch;

	Texture texture;
	TextureAtlas atlas;

	@Override
	public void create () {
		batch = new SpriteBatch();

		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0);
		camera.update();

		Pixmap pixmap1 = new Pixmap(Gdx.files.internal("data/badlogic.jpg"));
		Pixmap pixmap2 = new Pixmap(Gdx.files.internal("data/wheel.png"));
		Pixmap pixmap3 = new Pixmap(Gdx.files.internal("data/egg.png"));

		PixmapPacker packer = new PixmapPacker(1024, 1024, Format.RGBA8888, 2, true);
		packer.pack("badlogic", pixmap1);
		packer.pack("wheel", pixmap1);
		packer.pack("egg", pixmap1);

		pixmap1.dispose();
		pixmap2.dispose();
		pixmap3.dispose();

		atlas = packer.generateTextureAtlas(TextureFilter.Nearest, TextureFilter.Nearest, false);
		Gdx.app.log("PixmaPackerTest", "Number of textures: " + atlas.getTextures().size);
	}

	@Override
	public void render () {

	}

	@Override
	public void dispose () {
		atlas.dispose();
	}
}
