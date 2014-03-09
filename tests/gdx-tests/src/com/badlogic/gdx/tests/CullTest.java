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

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;

public class CullTest extends GdxTest implements ApplicationListener {

	Model sphere;
	Camera cam;
	SpriteBatch batch;
	ModelBatch modelBatch;
	BitmapFont font;
	ModelInstance[] instances = new ModelInstance[100];
	final Vector3 pos = new Vector3();

	@Override
	public void create () {
		ModelBuilder builder = new ModelBuilder();
		sphere = builder.createSphere(2f, 2f, 2f, 16, 16, new Material(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE)),
			Usage.Position | Usage.Normal);
		// cam = new PerspectiveCamera(45, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam = new OrthographicCamera(45, 45 * (Gdx.graphics.getWidth() / (float)Gdx.graphics.getHeight()));

		cam.near = 1;
		cam.far = 200;

		Random rand = new Random();
		for (int i = 0; i < instances.length; i++) {
			pos.set(rand.nextFloat() * 100 - rand.nextFloat() * 100, rand.nextFloat() * 100 - rand.nextFloat() * 100,
				rand.nextFloat() * -100 - 3);
			instances[i] = new ModelInstance(sphere, pos);
		}
		modelBatch = new ModelBatch();

		batch = new SpriteBatch();
		font = new BitmapFont();
		// Gdx.graphics.setVSync(true);
		// Gdx.app.log("CullTest", "" + Gdx.graphics.getBufferFormat().toString());
	}

	@Override
	public void render () {
		GL20 gl = Gdx.gl20;

		gl.glClearColor(0, 0, 0, 0);
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL20.GL_DEPTH_TEST);

		cam.update();
		modelBatch.begin(cam);

		int visible = 0;
		for (int i = 0; i < instances.length; i++) {
			instances[i].transform.getTranslation(pos);
			if (cam.frustum.sphereInFrustum(pos, 1)) {
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

		gl.glDisable(GL20.GL_DEPTH_TEST);
		batch.begin();
		font.draw(batch, "visible: " + visible + "/100" + ", fps: " + Gdx.graphics.getFramesPerSecond(), 0, 20);
		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
		sphere.dispose();
	}
}
