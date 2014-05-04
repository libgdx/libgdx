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

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.CameraMovementController;
import com.badlogic.gdx.graphics.g3d.utils.CameraPath;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

/** @author florianbaethge (evident) */
public class CameraMovementControllerTest extends GdxTest implements ApplicationListener, InputProcessor {
	public PerspectiveCamera overviewCam;
	public PerspectiveCamera pathCam;
	public PerspectiveCamera pathCam2;
	public PerspectiveCamera currentCam;

	public CameraMovementController movementController;
	public CameraMovementController movementController2;

	public InputMultiplexer inputMultiplexer;
	public CameraInputController camController;

	public ModelBatch modelBatch;
	public AssetManager assets;
	public Array<ModelInstance> instances = new Array<ModelInstance>();
	public Environment lights;
	public boolean loading;

	public Array<ModelInstance> blocks = new Array<ModelInstance>();
	public Array<ModelInstance> invaders = new Array<ModelInstance>();
	public ModelInstance ship;
	public ModelInstance space;

	SpriteBatch spriteBatch;
	BitmapFont font;

	@Override
	public void create () {
		spriteBatch = new SpriteBatch();
		font = new BitmapFont();

		modelBatch = new ModelBatch();
		lights = new Environment();
		lights = new Environment();
		lights.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
		lights.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		overviewCam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		overviewCam.position.set(0f, 7f, 10f);
		overviewCam.lookAt(0, 0, 0);
		overviewCam.near = 0.1f;
		overviewCam.far = 300f;
		overviewCam.update();

		inputMultiplexer = new InputMultiplexer();
		camController = new CameraInputController(overviewCam);

		inputMultiplexer.addProcessor(this);
		inputMultiplexer.addProcessor(camController);

		Gdx.input.setInputProcessor(inputMultiplexer);

		currentCam = overviewCam; // use overview camera on startup

		pathCam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		pathCam.position.set(0f, 7f, 10f);
		pathCam.up.set(Vector3.Y);
		pathCam.lookAt(0, 0, 0);
		pathCam.near = 0.1f;
		pathCam.far = 300f;
		pathCam.update();

		pathCam2 = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		pathCam2.position.set(0f, 7f, 10f);
		pathCam2.up.set(Vector3.Y);
		pathCam2.lookAt(0, 0, 0);
		pathCam2.near = 0.1f;
		pathCam2.far = 300f;
		pathCam2.update();

		Vector3[] path1Keys = new Vector3[] {new Vector3(10f, 1f, 5f), new Vector3(10f, 10f, -10f), new Vector3(7f, 5f, -10f),
			new Vector3(-10f, 1f, -8f), new Vector3(-10f, 1f, 2f), new Vector3(-6f, 1f, 2f), new Vector3(-2f, 1f, 2f),
			new Vector3(5f, 1f, 2f),};
		CameraPath path1 = new CameraPath(path1Keys, true);
		CameraPath path2 = new CameraPath(path1Keys, true);

		Vector3[] path2Keys = new Vector3[] {new Vector3(-4f, 0f, 0f), new Vector3(1f, 0f, 0f), new Vector3(1f, 0f, -5f),
			new Vector3(-4f, 0f, -3f),};
		CameraPath path3 = new CameraPath(path2Keys, true);
		CameraPath path4 = new CameraPath(path2Keys, true);

		movementController = new CameraMovementController(pathCam);
		movementController.setPositionPath(path1);
		movementController.setLookAtPath(path3);
		movementController.setUpVector(Vector3.Y);

		movementController2 = new CameraMovementController(pathCam2);
		movementController2.setPositionPath(path2);
		movementController2.setLookAtPath(path4);
		movementController2.setUpVector(Vector3.Y);

		assets = new AssetManager();
		assets.load("data/g3d/invaders.g3dj", Model.class);
		loading = true;
	}

	private void doneLoading () {
		Model model = assets.get("data/g3d/invaders.g3dj", Model.class);
		for (int i = 0; i < model.nodes.size; i++) {
			String id = model.nodes.get(i).id;
			ModelInstance instance = new ModelInstance(model, id);
			Node node = instance.getNode(id);

			instance.transform.set(node.globalTransform);
			node.translation.set(0, 0, 0);
			node.scale.set(1, 1, 1);
			node.rotation.idt();
			instance.calculateTransforms();

			if (id.equals("space")) {
				space = instance;
				continue;
			}

			instances.add(instance);

			if (id.equals("ship"))
				ship = instance;
			else if (id.startsWith("block"))
				blocks.add(instance);
			else if (id.startsWith("invader")) invaders.add(instance);
		}

		loading = false;

		movementController.getPositionPath().start(20.0f, -1, false);
		movementController.getLookAtPath().start(20.0f, -1, false);
		movementController2.getPositionPath().start(20.0f, -1, true);
		movementController2.getLookAtPath().start(20.0f, -1, true);
	}

	@Override
	public void render () {
		if (loading && assets.update()) doneLoading();
		camController.update();
		movementController.update(Gdx.graphics.getDeltaTime());
		movementController2.update(Gdx.graphics.getDeltaTime());

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(currentCam);
		for (ModelInstance instance : instances)
			modelBatch.render(instance, lights);
		if (space != null) modelBatch.render(space);
		modelBatch.end();

		if (currentCam == overviewCam) {
			movementController.debugDraw(currentCam);
			movementController2.debugDraw(currentCam);
		}

		spriteBatch.begin();
		font.draw(spriteBatch, "1 - Overview | 2 - Cam1 | 3 - Cam2", 10, 60);
		font.draw(spriteBatch, "y/x - Cam1 Position pause/resume | c/v - Cam1 LookAt pause/resume", 10, 40);
		font.draw(spriteBatch, "f/g - Cam2 Position pause/resume | h/j - Cam2 LookAt pause/resume", 10, 20);
		spriteBatch.end();

	}

	@Override
	public void dispose () {
		modelBatch.dispose();
		instances.clear();
		assets.dispose();
	}

	@Override
	public boolean keyDown (int keycode) {
		if (keycode == Keys.NUM_1) {
			currentCam = overviewCam;
			return true;
		}
		if (keycode == Keys.NUM_2) {
			currentCam = pathCam;
			return true;
		}
		if (keycode == Keys.NUM_3) {
			currentCam = pathCam2;
			return true;
		}

		if (keycode == Keys.Y) {
			movementController.getPositionPath().pause();
			return true;
		}

		if (keycode == Keys.X) {
			movementController.getPositionPath().resume();
			return true;
		}

		if (keycode == Keys.C) {
			movementController.getLookAtPath().pause();
			return true;
		}

		if (keycode == Keys.V) {
			movementController.getLookAtPath().resume();
			return true;
		}

		if (keycode == Keys.F) {
			movementController2.getPositionPath().pause();
			return true;
		}

		if (keycode == Keys.G) {
			movementController2.getPositionPath().resume();
			return true;
		}

		if (keycode == Keys.H) {
			movementController2.getLookAtPath().pause();
			return true;
		}

		if (keycode == Keys.J) {
			movementController2.getLookAtPath().resume();
			return true;
		}

		return super.keyDown(keycode);
	}

}
