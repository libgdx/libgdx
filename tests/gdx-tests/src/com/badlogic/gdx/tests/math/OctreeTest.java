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

package com.badlogic.gdx.tests.math;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
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
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Octree;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

public class OctreeTest extends GdxTest implements ApplicationListener {
	private static final float OCTREE_SIZE = 20;

	public PerspectiveCamera cam;
	public FirstPersonCameraController camController;
	public ModelBatch modelBatch;
	public AssetManager assets;
	public Array<ModelInstance> instances = new Array<ModelInstance>();
	public Environment lights;
	public boolean loading;

	public Octree<BoundingBox> octree;
	public Array<ModelInstance> blocks = new Array<ModelInstance>();
	public Array<ModelInstance> invaders = new Array<ModelInstance>();
	public ModelInstance ship;
	public ModelInstance space;

	@Override
	public void create () {
		modelBatch = new ModelBatch();
		lights = new Environment();
		lights.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
		lights.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0f, 7f, 10f);
		cam.lookAt(0, 0, 0);
		cam.near = 0.1f;
		cam.far = 300f;
		cam.update();

		camController = new FirstPersonCameraController(cam);
		Gdx.input.setInputProcessor(camController);

		Vector3 min = new Vector3(-OCTREE_SIZE / 2, -OCTREE_SIZE / 2, -OCTREE_SIZE / 2);
		Vector3 max = new Vector3(OCTREE_SIZE / 2, OCTREE_SIZE / 2, OCTREE_SIZE / 2);
		octree = new Octree<>(min, max, new Octree.Collider<BoundingBox>() {
			@Override public boolean intersects(BoundingBox nodeBounds, BoundingBox geometry) {
				return nodeBounds.contains(geometry);
			}

			final Vector3 tmp = new Vector3();
			@Override public float intersects(Ray ray, BoundingBox geometry) {
				if (!Intersector.intersectRayBounds(ray, geometry, tmp)) {
					return tmp.dst2(ray.origin);
				}
				return Float.POSITIVE_INFINITY;
			}
		});
		octree.setMaxItemsPerNode(3);
		octree.setMaxDepth(6);

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
			octree.add(instance.calculateBoundingBox(new BoundingBox()));

			if (id.equals("ship"))
				ship = instance;
			else if (id.startsWith("block"))
				blocks.add(instance);
			else if (id.startsWith("invader")) invaders.add(instance);
		}

		generateOctreeInstances(octree, instances);

		loading = false;
	}

	@Override
	public void render () {
		if (loading && assets.update()) doneLoading();
		camController.update();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		for (ModelInstance instance : instances)
			modelBatch.render(instance, lights);
		if (space != null) modelBatch.render(space);

		modelBatch.end();
	}

	private void generateOctreeInstances(Octree octree, Array<ModelInstance> instances) {
		ObjectSet<BoundingBox> boxes = new ObjectSet<>();
		octree.getNodesBoxes(boxes);

		ObjectSet.ObjectSetIterator<BoundingBox> iterator = boxes.iterator();

		ModelBuilder modelBuilder = new ModelBuilder();
		Material material = new Material();
		material.set(ColorAttribute.createDiffuse(Color.GREEN));

		while (iterator.hasNext()) {
			BoundingBox box = iterator.next();

			Model model = modelBuilder.createBox(box.getWidth(), box.getHeight(), box.getDepth(), GL20.GL_LINES, material, VertexAttributes.Usage.Position);
			ModelInstance instance = new ModelInstance(model);
			instance.transform.translate(box.getCenterX(), box.getCenterY(), box.getCenterZ());

			instances.add(instance);
		}
	}

	@Override
	public void dispose () {
		modelBatch.dispose();
		instances.clear();
		assets.dispose();
	}
}
