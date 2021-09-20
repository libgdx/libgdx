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
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class PixmapPackerTest extends GdxTest {

	OrthographicCamera camera;
	SpriteBatch batch;
	ShapeRenderer shapeRenderer;

	TextureAtlas atlas;

	int pageToShow = 0;
	Array<TextureRegion> textureRegions;

	NinePatch ninePatch;
	NinePatch officialPatch;

	Animation<TextureRegion> animation;

	Skin skin;

	float stateTime = 0;

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

		PixmapPacker packer = new PixmapPacker(1024, 1024, Format.RGBA8888, 8, false, true, true,
			new PixmapPacker.GuillotineStrategy());
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

		packer.pack("badlogic-anim_0", pixmap1);
		packer.pack("badlogic-anim_1", pixmap5);
		packer.pack("badlogic-anim_2", pixmap1);
		packer.pack("badlogic-anim_3", pixmap5);

		pixmap1.dispose();
		pixmap2.dispose();
		pixmap3.dispose();
		pixmap4.dispose();
		pixmap5.dispose();

		packer.updateTextureAtlas(atlas, TextureFilter.Nearest, TextureFilter.Nearest, false);
		animation = new Animation<TextureRegion>(0.33f, atlas.findRegions("badlogic-anim"), Animation.PlayMode.LOOP);

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

	}

	@Override
	public void render () {
		ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1);
		int size = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		int quarterSize = (int)(size / 4f);
		batch.begin();
		batch.draw(textureRegions.get(pageToShow), 0, 0, size, size);
		ninePatch.draw(batch, 10, 10, quarterSize, quarterSize);
		officialPatch.draw(batch, (int)(size * 0.25f + 20), 10, quarterSize, quarterSize);
		batch.draw(animation.getKeyFrame(stateTime), 30 + (quarterSize * 2), 10, quarterSize, quarterSize);
		batch.end();
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.rect(0, 0, size, size);
		shapeRenderer.end();

		stateTime += Gdx.graphics.getDeltaTime();
	}

	@Override
	public void dispose () {
		atlas.dispose();
		skin.dispose();
	}
}
