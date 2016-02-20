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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.ScreenUtils;

public class FramebufferToTextureTest extends GdxTest {

	TextureRegion fbTexture;
	Texture texture;
	Model mesh;
	ModelInstance modelInstance;
	ModelBatch modelBatch;
	PerspectiveCamera cam;
	SpriteBatch batch;
	BitmapFont font;
	Color clearColor = new Color(0.2f, 0.2f, 0.2f, 1);
	float angle = 0;

	@Override
	public void create () {
		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"), true);
		texture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
		ObjLoader objLoader = new ObjLoader();
		mesh = objLoader.loadModel(Gdx.files.internal("data/cube.obj"));
		mesh.materials.get(0).set(new TextureAttribute(TextureAttribute.Diffuse, texture));
		modelInstance = new ModelInstance(mesh);
		modelBatch = new ModelBatch();

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(3, 3, 3);
		cam.direction.set(-1, -1, -1);
		batch = new SpriteBatch();
		font = new BitmapFont();
	}

	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl.glClearColor(clearColor.g, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);

		cam.update();

		modelInstance.transform.rotate(Vector3.Y, 45 * Gdx.graphics.getDeltaTime());
		modelBatch.begin(cam);
		modelBatch.render(modelInstance);
		modelBatch.end();

		if (Gdx.input.justTouched() || fbTexture == null) {
			if (fbTexture != null) fbTexture.getTexture().dispose();
			fbTexture = ScreenUtils.getFrameBufferTexture();
		}

		batch.begin();
		if (fbTexture != null) {
			batch.draw(fbTexture, 0, 0, 100, 100);
		}
		font.draw(batch, "Touch screen to take a snapshot", 10, 40);
		batch.end();
	}

	@Override
	public void pause () {
		fbTexture = null;
	}
}
