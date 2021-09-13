/*******************************************************************************
 * Copyright 2020 See AUTHORS file.
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

package com.badlogic.gdx.tests.math;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Octree;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

import java.util.Random;

public class OctreeTest extends GdxTest implements ApplicationListener {
	private static final float AREA_SIZE = 100;
	private static final int BOXES = 5000;
	private static final int REMOVE_BOXES = 500;
	public static final int MAX_DEPTH = 8;
	public static final int MAX_ITEMS_PER_NODE = 200;

	public boolean octreeVisible = true;

	public PerspectiveCamera cam;
	public FirstPersonCameraController camController;
	public ModelBatch modelBatch;
	public Environment lights;

	public Octree<GameObject> octree;
	public ObjectSet<GameObject> tmpResult = new ObjectSet<GameObject>();
	public Array<GameObject> gameObjects = new Array<GameObject>();
	public Array<ModelInstance> octreeBounds = new Array<ModelInstance>();

	private GameObject lastSelected;

	@Override
	public void create () {
		modelBatch = new ModelBatch();
		lights = new Environment();
		lights.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
		lights.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0f, 0f, 15f);
		cam.lookAt(0, 0, 1);
		cam.near = 0.1f;
		cam.far = 300f;
		cam.update(true);

		camController = new FirstPersonCameraController(cam);
		Gdx.input.setInputProcessor(this);

		Vector3 min = new Vector3(-AREA_SIZE / 2, -AREA_SIZE / 2, -AREA_SIZE / 2);
		Vector3 max = new Vector3(AREA_SIZE / 2, AREA_SIZE / 2, AREA_SIZE / 2);
		octree = new Octree<GameObject>(min, max, MAX_DEPTH, MAX_ITEMS_PER_NODE, new Octree.Collider<GameObject>() {
			@Override
			public boolean intersects (BoundingBox nodeBounds, GameObject geometry) {
				return nodeBounds.intersects(geometry.box);
			}

			@Override
			public boolean intersects (Frustum frustum, GameObject geometry) {
				return frustum.boundsInFrustum(geometry.box);
			}

			final Vector3 tmp = new Vector3();

			@Override
			public float intersects (Ray ray, GameObject geometry) {
				if (Intersector.intersectRayBounds(ray, geometry.box, tmp)) {
					return tmp.dst2(ray.origin);
				}
				return Float.MAX_VALUE;
			}
		});

		generateGameObjects();

		for (int i = 0; i < REMOVE_BOXES; i++)
			octree.remove(gameObjects.removeIndex(MathUtils.random(0, gameObjects.size - 1)));

		generateOctreeInstances();
	}

	@Override
	public void render () {
		camController.update();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);

		octree.query(cam.frustum, tmpResult);
		for (GameObject gameObject : tmpResult) {
			Gdx.app.log("", "Rendering: " + tmpResult.size);
			modelBatch.render(gameObject.instance, lights);
			modelBatch.render(gameObject.boxEdges);
		}
		tmpResult.clear();

		if (octreeVisible) {
			for (ModelInstance instance : octreeBounds) {
				modelBatch.render(instance);
			}
		}

		modelBatch.end();
	}

	@Override
	public boolean keyDown (int keycode) {
		camController.keyDown(keycode);
		// Space toggle octree render
		if (keycode == Input.Keys.SPACE) {
			octreeVisible = !octreeVisible;
		}
		return super.keyDown(keycode);
	}

	@Override
	public boolean keyUp (int keycode) {
		camController.keyUp(keycode);
		return super.keyUp(keycode);
	}

	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		camController.touchDragged(screenX, screenY, pointer);
		return super.touchDragged(screenX, screenY, pointer);
	}

	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		if (lastSelected != null) {
			lastSelected.deselect();
			lastSelected = null;
		}
		selectGameObject(cam.getPickRay(screenX, screenY));
		return super.touchDown(screenX, screenY, pointer, button);
	}

	private void selectGameObject (Ray ray) {
		GameObject selected = octree.rayCast(ray, new Octree.RayCastResult<GameObject>());
		if (selected != null) {
			selected.select();
			lastSelected = selected;
		}
	}

	private void generateGameObjects () {
		Random random = new Random();
		ModelBuilder modelBuilder = new ModelBuilder();

		Material objectMaterial = new Material();
		objectMaterial.set(ColorAttribute.createDiffuse(Color.WHITE));

		Material wireframeMaterial = new Material();
		wireframeMaterial.set(ColorAttribute.createDiffuse(Color.RED));

		for (int i = 0, n = BOXES + REMOVE_BOXES; i < n; i++) {
			float width = random.nextFloat() * 3;
			float height = random.nextFloat() * 3;
			float depth = random.nextFloat() * 3;

			Vector3 center = new Vector3(-AREA_SIZE / 2 + random.nextFloat() * AREA_SIZE, random.nextFloat() * AREA_SIZE / 2,
				-AREA_SIZE / 2 + random.nextFloat() * AREA_SIZE);

			GameObject gameObject = new GameObject();
			Model modelBox = modelBuilder.createBox(width, height, depth, GL20.GL_TRIANGLES, objectMaterial,
				VertexAttributes.Usage.Position);
			gameObject.instance = new ModelInstance(modelBox);
			gameObject.instance.transform.translate(center);

			Vector3 min = new Vector3(center).sub(width / 2, height / 2, depth / 2);
			Vector3 max = new Vector3(center).add(width / 2, height / 2, depth / 2);

			gameObject.box = new BoundingBox(min, max);

			modelBox = modelBuilder.createBox(width, height, depth, GL20.GL_LINES, wireframeMaterial,
				VertexAttributes.Usage.Position);
			gameObject.boxEdges = new ModelInstance(modelBox);
			gameObject.boxEdges.transform.translate(center);

			gameObjects.add(gameObject);
			octree.add(gameObject);
		}
	}

	private void generateOctreeInstances () {
		ObjectSet<BoundingBox> boxes = new ObjectSet<>();
		octree.getNodesBoxes(boxes);

		ObjectSet.ObjectSetIterator<BoundingBox> iterator = boxes.iterator();

		ModelBuilder modelBuilder = new ModelBuilder();
		Material material = new Material();
		material.set(ColorAttribute.createDiffuse(Color.GREEN));

		while (iterator.hasNext()) {
			BoundingBox box = iterator.next();

			Model model = modelBuilder.createBox(box.getWidth(), box.getHeight(), box.getDepth(), GL20.GL_LINES, material,
				VertexAttributes.Usage.Position);
			ModelInstance instance = new ModelInstance(model);
			instance.transform.translate(box.getCenterX(), box.getCenterY(), box.getCenterZ());

			octreeBounds.add(instance);
		}
	}

	@Override
	public void dispose () {
		modelBatch.dispose();
		gameObjects.clear();
	}

	static class GameObject {
		ModelInstance instance;
		ModelInstance boxEdges;
		BoundingBox box;

		public void select () {
			instance.materials.get(0).set(ColorAttribute.createDiffuse(Color.BLUE));
		}

		public void deselect () {
			instance.materials.get(0).set(ColorAttribute.createDiffuse(Color.WHITE));
		}
	}
}
