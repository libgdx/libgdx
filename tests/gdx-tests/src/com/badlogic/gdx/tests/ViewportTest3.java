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
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Cycles viewports while rendering with SpriteBatch. */
public class ViewportTest3 extends GdxTest {
	Array<Viewport> viewports;
	Viewport viewport;
	Array<String> names;
	String name;

	private PerspectiveCamera camera;
	public Environment environment;
	public DirectionalLight shadowLight;
	public ModelBuilder modelBuilder;
	public ModelBatch modelBatch;
	public ModelInstance boxInstance;

	public void create () {
		modelBatch = new ModelBatch();
		modelBuilder = new ModelBuilder();

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.3f, 0.3f, 0.3f, 1.f));
		shadowLight = new DirectionalLight();
		shadowLight.set(0.8f, 0.8f, 0.8f, -0.5f, -1f, 0.7f);
		environment.add(shadowLight);

		modelBatch = new ModelBatch();

		camera = new PerspectiveCamera();
		camera.fieldOfView = 67;
		camera.near = 0.1f;
		camera.far = 300f;
		camera.position.set(0, 0, 100);
		camera.lookAt(0, 0, 0);

		viewports = ViewportTest1.getViewports(camera);
		viewport = viewports.first();

		names = ViewportTest1.getViewportNames();
		name = names.first();

		ModelBuilder modelBuilder = new ModelBuilder();
		Model boxModel = modelBuilder.createBox(50f, 50f, 50f, new Material(ColorAttribute.createDiffuse(Color.GREEN)),
			Usage.Position | Usage.Normal);
		boxInstance = new ModelInstance(boxModel);
		boxInstance.transform.rotate(1, 0, 0, 30);
		boxInstance.transform.rotate(0, 1, 0, 30);

		Gdx.input.setInputProcessor(new InputAdapter() {
			public boolean keyDown (int keycode) {
				if (keycode == Input.Keys.SPACE) {
					int index = (viewports.indexOf(viewport, true) + 1) % viewports.size;
					name = names.get(index);
					viewport = viewports.get(index);
					resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				}
				return false;
			}
		});
	}

	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(camera);
		modelBatch.render(boxInstance, environment);
		modelBatch.end();
	}

	public void resize (int width, int height) {
		System.out.println(name);
		viewport.update(width, height);
	}
}
