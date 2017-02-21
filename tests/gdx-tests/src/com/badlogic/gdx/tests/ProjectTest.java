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

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;

public class ProjectTest extends GdxTest {

	Model sphere;
	Camera cam;
	SpriteBatch batch;
	BitmapFont font;
	ModelInstance[] instances = new ModelInstance[100];
	ModelBatch modelBatch;
	Vector3 tmp = new Vector3();
	TextureRegion logo;

	@Override
	public void create () {
		ObjLoader objLoader = new ObjLoader();
		sphere = objLoader.loadModel(Gdx.files.internal("data/sphere.obj"));
		sphere.materials.get(0).set(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE));
		cam = new PerspectiveCamera(45, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.far = 200;
		Random rand = new Random();
		for (int i = 0; i < instances.length; i++) {
			instances[i] = new ModelInstance(sphere, rand.nextFloat() * 100 - rand.nextFloat() * 100, rand.nextFloat() * 100
				- rand.nextFloat() * 100, rand.nextFloat() * -100 - 3);
		}
		batch = new SpriteBatch();
		font = new BitmapFont();
		logo = new TextureRegion(new Texture(Gdx.files.internal("data/badlogicsmall.jpg")));
		modelBatch = new ModelBatch();
	}

	@Override
	public void render () {

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

		cam.update();

		modelBatch.begin(cam);

		int visible = 0;
		for (int i = 0; i < instances.length; i++) {
			instances[i].transform.getTranslation(tmp);
			if (cam.frustum.sphereInFrustum(tmp, 1)) {
				((ColorAttribute)instances[i].materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.WHITE);
				visible++;
			} else {
				((ColorAttribute)instances[i].materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.RED);
			}
			modelBatch.render(instances[i]);
		}
		modelBatch.end();

		if (Gdx.input.isKeyPressed(Keys.A)) cam.rotate(20 * Gdx.graphics.getDeltaTime(), 0, 1, 0);
		if (Gdx.input.isKeyPressed(Keys.D)) cam.rotate(-20 * Gdx.graphics.getDeltaTime(), 0, 1, 0);

		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		batch.begin();
		for (int i = 0; i < instances.length; i++) {
			instances[i].transform.getTranslation(tmp);
			cam.project(tmp);
			if (tmp.z < 0) continue;
			batch.draw(logo, tmp.x, tmp.y);
		}
		batch.end();
	}
}
