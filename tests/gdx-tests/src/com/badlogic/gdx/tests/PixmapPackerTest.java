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
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.PixmapPackerIO;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.IOException;

public class PixmapPackerTest extends GdxTest {

	OrthographicCamera camera;
	SpriteBatch batch;
	ShapeRenderer shapeRenderer;

	TextureAtlas atlas;

	int pageToShow = 0;
	Array<TextureRegion> textureRegions;

	NinePatch ninePatch;
	NinePatch officialPatch;

	Skin skin;

	@Override
	public void create () {
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();

		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0);
		camera.update();

		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		Pixmap pixmap1 = new Pixmap(Gdx.files.internal("data/badlogic.jpg"));
		Pixmap pixmap2 = new Pixmap(Gdx.files.internal("data/particle-fire.png"));
		Pixmap pixmap3 = new Pixmap(Gdx.files.internal("data/isotile.png"));
		Pixmap pixmap4 = new Pixmap(Gdx.files.internal("data/textfield.9.png"));
		Pixmap pixmap5 = new Pixmap(Gdx.files.internal("data/badlogic-with-whitespace.png"));

		PixmapPacker packer = new PixmapPacker(1024, 1024, Format.RGBA8888, 8, false,
				true, true, new PixmapPacker.GuillotineStrategy());
		packer.setTransparentColor(Color.PINK);
		for (int count = 1; count <= 3; ++count) {
			packer.pack("badlogic " + count, pixmap1);
			packer.pack("fire " + count, pixmap2);
			packer.pack("isotile " + count, pixmap3);
			packer.pack("textfield-" + count + ".9", pixmap4);
			packer.pack("badlogic-whitespace " + count, pixmap5);
		}

		atlas = packer.generateTextureAtlas(TextureFilter.Nearest, TextureFilter.Nearest, false);
		Gdx.app.log("PixmapPackerTest", "Number of initial textures: " + atlas.getTextures().size);

		packer.setPackToTexture(true);

		for (int count = 4; count <= 10; ++count) {
			packer.pack("badlogic " + count, pixmap1);
			packer.pack("fire " + count, pixmap2);
			packer.pack("isotile " + count, pixmap3);
			packer.pack("textfield-" + count + ".9", pixmap4);
			packer.pack("badlogic-whitespace -" + count, pixmap5);
		}

		pixmap1.dispose();
		pixmap2.dispose();
		pixmap3.dispose();
		pixmap4.dispose();
		pixmap5.dispose();

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

		ninePatch = atlas.createPatch("textfield-1");
		officialPatch = skin.getPatch("textfield");
		officialPatch.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

		PixmapPackerIO pixmapPackerIO = new PixmapPackerIO();
		try {
			PixmapPackerIO.SaveParameters saveParameters = new PixmapPackerIO.SaveParameters();
			saveParameters.format = PixmapPackerIO.ImageFormat.PNG;
			pixmapPackerIO.save(Gdx.files.local("pixmapPackerTest.atlas"), packer, saveParameters);
		} catch (IOException e) {
			e.printStackTrace();
		}

		TextureAtlas loaded = new TextureAtlas(Gdx.files.local("pixmapPackerTest.atlas"));
		for (int i = 0; i < loaded.getRegions().size; i++) {
			final TextureAtlas.AtlasRegion atlasRegion = loaded.getRegions().get(i);
			compare(atlas, atlasRegion);
		}
	}

	private void compare (TextureAtlas original, TextureAtlas.AtlasRegion loaded) {
		final TextureAtlas.AtlasRegion originalRegion = original.findRegion(loaded.name);
		if (originalRegion == null) throw new GdxRuntimeException("No original AtlasRegion found");

		if (originalRegion.index != loaded.index) throw new GdxRuntimeException("Original AtlasRegion differs from loaded");
		if (originalRegion.offsetX != loaded.offsetX) throw new GdxRuntimeException("Original AtlasRegion differs from loaded");
		if (originalRegion.offsetY != loaded.offsetY) throw new GdxRuntimeException("Original AtlasRegion differs from loaded");
		if (originalRegion.packedWidth != loaded.packedWidth) throw new GdxRuntimeException("Original AtlasRegion differs from loaded");
		if (originalRegion.packedHeight != loaded.packedHeight) throw new GdxRuntimeException("Original AtlasRegion differs from loaded");
		if (originalRegion.originalWidth != loaded.originalWidth) throw new GdxRuntimeException("Original AtlasRegion differs from loaded");
		if (originalRegion.originalHeight != loaded.originalHeight) throw new GdxRuntimeException("Original AtlasRegion differs from loaded");
		if (originalRegion.rotate != loaded.rotate) throw new GdxRuntimeException("Original AtlasRegion differs from loaded");
		if (originalRegion.splits != null && loaded.splits != null) {
			for (int i = 0; i < originalRegion.splits.length; i++) {
				if (originalRegion.splits[i] != loaded.splits[i]) throw new GdxRuntimeException("Original AtlasRegion differs from loaded");
			}
		} else {
			if (originalRegion.splits != loaded.splits) throw new GdxRuntimeException("Original AtlasRegion differs from loaded");
		}
		if (originalRegion.pads != null && loaded.pads != null) {
			for (int i = 0; i < originalRegion.pads.length; i++) {
				if (originalRegion.pads[i] != loaded.pads[i]) throw new GdxRuntimeException("Original AtlasRegion differs from loaded");
			}
		} else {
			if (originalRegion.pads != loaded.pads) throw new GdxRuntimeException("Original AtlasRegion differs from loaded");
		}	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		int size = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		int quarterSize = (int) (size/4f);
		batch.begin();
		batch.draw(textureRegions.get(pageToShow), 0, 0, size, size);
		ninePatch.draw(batch, 10, 10, quarterSize, quarterSize);
		officialPatch.draw(batch, (int)(size * 0.25f + 20), 10, quarterSize, quarterSize);
		batch.end();
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.rect(0, 0, size, size);
		shapeRenderer.end();
	}

	@Override
	public void dispose () {
		atlas.dispose();
		skin.dispose();
	}
}
