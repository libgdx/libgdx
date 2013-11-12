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
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.tests.utils.GdxTest;

public class PickingTest extends GdxTest {

	@Override
	public boolean needsGL20 () {
		return false;
	}

	static final int BORDER = 20;
	static final int VP_X = BORDER;
	static final int VP_Y = BORDER * 2;
	static int VP_WIDTH;
	static int VP_HEIGHT;
	Model sphere;
	Camera cam;
	ModelInstance[] instances = new ModelInstance[100];
	ModelBatch modelBatch;
	ImmediateModeRenderer10 renderer;
	SpriteBatch batch;
	Texture logo;
	Vector3 tempVector = new Vector3();

	@Override
	public void create () {
		VP_WIDTH = Gdx.graphics.getWidth() - 4 * BORDER;
		VP_HEIGHT = Gdx.graphics.getHeight() - 4 * BORDER;
		ObjLoader objLoader = new ObjLoader();
		sphere = objLoader.loadObj(Gdx.files.internal("data/sphere.obj"));
		sphere.materials.get(0).set(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE));
		cam = new PerspectiveCamera(45, VP_WIDTH, VP_HEIGHT);
// cam = new OrthographicCamera(10, 10);
		cam.far = 200;
		batch = new SpriteBatch();
		logo = new Texture(Gdx.files.internal("data/badlogicsmall.jpg"));
		Random rand = new Random(10);
		for (int i = 0; i < instances.length; i++) {
			instances[i] = new ModelInstance(sphere, rand.nextFloat() * 100 - rand.nextFloat() * 100, 
				rand.nextFloat() * 100 - rand.nextFloat() * 100, rand.nextFloat() * 100 - rand.nextFloat() * 100);
		}
		instances[0].transform.setToTranslation(0, 0, -10);
		renderer = new ImmediateModeRenderer10();
		modelBatch = new ModelBatch();
	}

	Vector3 intersection = new Vector3();

	@Override
	public void render () {
		GL10 gl = Gdx.gl10;

		gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL10.GL_DEPTH_TEST);

		cam.update();
		gl.glViewport(VP_X, VP_Y, VP_WIDTH, VP_HEIGHT);

		Ray pickRay = null;
		if (Gdx.input.isTouched()) {
			pickRay = cam.getPickRay(Gdx.input.getX(), Gdx.input.getY(), VP_X, VP_Y, VP_WIDTH, VP_HEIGHT);
// Gdx.app.log("PickingTest", "ray: " + pickRay);
		}

		boolean intersected = false;
		modelBatch.begin(cam);
		for (int i = 0; i < instances.length; i++) {
			instances[i].transform.getTranslation(tempVector);
			if (pickRay != null && Intersector.intersectRaySphere(pickRay, tempVector, 1, intersection)) {
				((ColorAttribute)instances[i].materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.RED);
				intersected = true;
			} else {
				((ColorAttribute)instances[i].materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.WHITE);
			}
			modelBatch.render(instances[i]);
		}
		modelBatch.end();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.begin();
		if (intersected) {
			cam.project(intersection, VP_X, VP_Y, VP_WIDTH, VP_HEIGHT);
			batch.draw(logo, intersection.x, intersection.y);
		}
		batch.end();

		renderer.begin(GL10.GL_LINE_LOOP);
		renderer.color(1, 1, 1, 1);
		renderer.vertex(VP_X, VP_Y, 0);
		renderer.color(1, 1, 1, 1);
		renderer.vertex(VP_X + VP_WIDTH, VP_Y, 0);
		renderer.color(1, 1, 1, 1);
		renderer.vertex(VP_X + VP_WIDTH, VP_Y + VP_HEIGHT, 0);
		renderer.color(1, 1, 1, 1);
		renderer.vertex(VP_X, VP_Y + VP_HEIGHT, 0);
		renderer.end();

		if (Gdx.input.isKeyPressed(Keys.A)) cam.rotate(20 * Gdx.graphics.getDeltaTime(), 0, 1, 0);
		if (Gdx.input.isKeyPressed(Keys.D)) cam.rotate(-20 * Gdx.graphics.getDeltaTime(), 0, 1, 0);
	}
}
