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

package com.badlogic.gdx.tests.viewport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.tests.utils.DebugActor;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.DoubleRatioViewport;
import com.badlogic.gdx.utils.viewport.FixedViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StaticViewport;
import com.badlogic.gdx.utils.viewport.StretchedViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** This test makes use of the different kind of viewports, while using a PerspectiveCamera and 3D. */
public class ViewportTest4 extends GdxTest {

	private float delay;

	Array<Viewport> viewports = new Array<Viewport>(4);
	private Viewport viewport;

	private PerspectiveCamera camera;
	public Environment environment;
	public DirectionalLight shadowLight;
	public ModelBuilder modelBuilder;
	public ModelBatch modelBatch;
	public ModelInstance boxInstance;
	public ModelInstance backgroundInstance;

	public void create () {
		modelBatch = new ModelBatch();
		modelBuilder = new ModelBuilder();

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.3f, 0.3f, 0.3f, 1.f));
		shadowLight = new DirectionalLight();
		shadowLight.set(0.8f, 0.8f, 0.8f, -0.5f, -1f, 0.7f);
		environment.add(shadowLight);

		modelBatch = new ModelBatch();

		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(2f, 2f, -10f);
		camera.lookAt(0, 0, 0);
		camera.near = 0.1f;
		camera.far = 300f;
		camera.update();

		ModelBuilder modelBuilder = new ModelBuilder();
		Model boxModel = modelBuilder.createBox(5f, 5f, 5f, new Material(ColorAttribute.createDiffuse(Color.GREEN)), Usage.Position
			| Usage.Normal);
		// we need a white background so we are able to see the black bars appearing
		Model backgroundModel = modelBuilder.createRect(-100000, -100000, 50, -100000, 100000, 50, 100000, 100000, 50, 100000,
			-100000, 50, 0, 0, -1, new Material(ColorAttribute.createDiffuse(Color.WHITE)), Usage.Position | Usage.Normal);
		boxInstance = new ModelInstance(boxModel);
		backgroundInstance = new ModelInstance(backgroundModel);

		viewports.add(new StretchedViewport(camera, 300, 200));
		viewports.add(new FixedViewport(camera, 300, 200));
		viewports.add(new ScreenViewport(camera));
		viewports.add(new StaticViewport(camera, 300, 200));
// viewports.add(new DoubleRatioViewport(300, 200, 600, 400));
		viewport = viewports.first();
	}

	public void render () {
		delay -= Gdx.graphics.getDeltaTime();
		// iterate through the viewports
		if (delay <= 0) {
			if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
				viewport = viewports.get((viewports.indexOf(viewport, true) + 1) % viewports.size);
				viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				delay = 1f;
			}
		}

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(camera);

		modelBatch.render(backgroundInstance, environment);
		modelBatch.render(boxInstance, environment);

		modelBatch.end();
	}

	public void resize (int width, int height) {
		viewport.update(width, height);
	}

	public void dispose () {
	}
}
