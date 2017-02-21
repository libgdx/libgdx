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

package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

public abstract class BaseG3dTest extends GdxTest {
	public AssetManager assets;

	public PerspectiveCamera cam;
	public CameraInputController inputController;
	public ModelBatch modelBatch;
	public Model axesModel;
	public ModelInstance axesInstance;
	public boolean showAxes = true;
	public Array<ModelInstance> instances = new Array<ModelInstance>();
	public final Color bgColor = new Color(0, 0, 0, 1);

	@Override
	public void create () {
		if (assets == null) assets = new AssetManager();

		modelBatch = new ModelBatch();

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(10f, 10f, 10f);
		cam.lookAt(0, 0, 0);
		cam.near = 0.1f;
		cam.far = 1000f;
		cam.update();

		createAxes();

		Gdx.input.setInputProcessor(inputController = new CameraInputController(cam));
	}

	final float GRID_MIN = -10f;
	final float GRID_MAX = 10f;
	final float GRID_STEP = 1f;

	private void createAxes () {
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		MeshPartBuilder builder = modelBuilder.part("grid", GL20.GL_LINES, Usage.Position | Usage.ColorUnpacked, new Material());
		builder.setColor(Color.LIGHT_GRAY);
		for (float t = GRID_MIN; t <= GRID_MAX; t += GRID_STEP) {
			builder.line(t, 0, GRID_MIN, t, 0, GRID_MAX);
			builder.line(GRID_MIN, 0, t, GRID_MAX, 0, t);
		}
		builder = modelBuilder.part("axes", GL20.GL_LINES, Usage.Position | Usage.ColorUnpacked, new Material());
		builder.setColor(Color.RED);
		builder.line(0, 0, 0, 100, 0, 0);
		builder.setColor(Color.GREEN);
		builder.line(0, 0, 0, 0, 100, 0);
		builder.setColor(Color.BLUE);
		builder.line(0, 0, 0, 0, 0, 100);
		axesModel = modelBuilder.end();
		axesInstance = new ModelInstance(axesModel);
	}

	protected abstract void render (final ModelBatch batch, final Array<ModelInstance> instances);

	protected boolean loading = false;

	protected void onLoaded () {
	}

	public void render (final Array<ModelInstance> instances) {
		modelBatch.begin(cam);
		if (showAxes) modelBatch.render(axesInstance);
		if (instances != null) render(modelBatch, instances);
		modelBatch.end();
	}

	@Override
	public void render () {
		if (loading && assets.update()) {
			loading = false;
			onLoaded();
		}

		inputController.update();

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a);

		render(instances);
	}

	@Override
	public void dispose () {
		modelBatch.dispose();
		assets.dispose();
		assets = null;
		axesModel.dispose();
		axesModel = null;
	}
}
