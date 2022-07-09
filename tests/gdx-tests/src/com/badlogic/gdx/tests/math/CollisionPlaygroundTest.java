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
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.FrustumShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.tests.utils.GdxTest;

import java.util.ArrayList;
import java.util.List;

public class CollisionPlaygroundTest extends GdxTest implements ApplicationListener {

	private static final int NUM_SHAPES = 30;
	private static final int RANGE = 4;

	private int PRIMITIVE_TYPE = GL20.GL_LINES;

	private static final Color COLOR_STANDARD = Color.BLUE;
	private static final Color COLOR_MOUSE_OVER = Color.GREEN;
	private static final Color COLOR_INTERSECTION = Color.GOLD;

	private PerspectiveCamera camera;
	private CameraInputController cameraController;
	private PerspectiveCamera collisionCamera;

	private ModelBatch modelBatch;

	private SpriteBatch batch;
	private BitmapFont font;

	private ModelInstance frustum;
	private List<Shape> shapes = new ArrayList<>();

	private long seed;

	@Override
	public void create () {
		font = new BitmapFont(Gdx.files.internal("data/lsans-15.fnt"), false);
		batch = new SpriteBatch();
		modelBatch = new ModelBatch();

		setupCamera();
		setupScene();
		InputMultiplexer inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(cameraController);
		inputMultiplexer.addProcessor(this);
		Gdx.input.setInputProcessor(inputMultiplexer);
	}

	private void setupScene () {
		seed = MathUtils.random.nextLong();
		MathUtils.random.setSeed(seed);

		if (frustum != null) {
			frustum.model.dispose();
		}

		for (Shape shape : shapes) {
			shape.dispose();
		}
		shapes.clear();

		for (int i = 0; i < NUM_SHAPES; i++) {
			createRandomShape();
		}

		frustum = createFrustum(collisionCamera);
	}

	private void setupCamera () {
		camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = 0.01f;
		camera.far = 100f;
		camera.position.set(0, 5, 0);
		camera.lookAt(Vector3.Zero);
		camera.update();
		cameraController = new CameraInputController(camera);

		collisionCamera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		collisionCamera.near = 0.01f;
		collisionCamera.far = 3f;
		collisionCamera.position.set(1, 0, 0);
		collisionCamera.lookAt(0, 0, -1);
		collisionCamera.update(true);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// Draw FPS
		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.begin();
		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 0, 30);
		font.draw(batch, "seed: " + seed, 0, 50);
		batch.end();

		checkCollision();

		// Draw Box
		modelBatch.begin(camera);
		modelBatch.render(frustum);
		for (Shape shape : shapes) {
			modelBatch.render(shape.instance);
		}
		modelBatch.end();
	}

	private void checkCollision () {
		Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());

		for (Shape shape : shapes) {
			if (shape.isColliding(ray)) {
				shape.updateColor(COLOR_MOUSE_OVER);
			} else if (shape.isColliding(collisionCamera.frustum)) {
				shape.updateColor(COLOR_INTERSECTION);
			} else {
				shape.updateColor(COLOR_STANDARD);
			}
		}
	}

	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
		modelBatch.dispose();
		frustum.model.dispose();
		for (Shape shape : shapes) {
			shape.dispose();
		}
	}

	@Override
	public boolean keyUp (int keycode) {
		if (Input.Keys.SPACE == keycode) {
			setupScene();
		}
		return super.keyUp(keycode);
	}

	private void createRandomShape () {
		int shape = MathUtils.random.nextInt(3);

		switch (shape) {
		case 1:
			shapes.add(new Sphere());
			break;
		case 2:
			shapes.add(new OBB());
			break;
		default:
			shapes.add(new AABB());
		}

	}

	private ModelInstance createFrustum (PerspectiveCamera camera) {
		Material material = new Material(ColorAttribute.createDiffuse(1, 0, 0, 0));
		com.badlogic.gdx.graphics.g3d.utils.ModelBuilder mb = new com.badlogic.gdx.graphics.g3d.utils.ModelBuilder();
		mb.begin();

		MeshPartBuilder meshPartBuilder = mb.part("frustum", PRIMITIVE_TYPE, VertexAttributes.Usage.Position, material);

		FrustumShapeBuilder.build(meshPartBuilder, camera);

		return new ModelInstance(mb.end());
	}

	abstract class Shape {
		ModelInstance instance;

		abstract boolean isColliding (Frustum frustum);

		abstract boolean isColliding (Ray ray);

		void updateColor (Color color) {
			Material material = instance.materials.get(0);
			ColorAttribute attribute = (ColorAttribute)material.get(ColorAttribute.Diffuse);
			attribute.color.set(color);
		}

		void dispose () {
			instance.model.dispose();
		}

		Vector3 randomPosition () {
			return new Vector3(MathUtils.random(-RANGE, RANGE), MathUtils.random(-RANGE, RANGE), MathUtils.random(-RANGE, RANGE));
		}
	}

	class AABB extends Shape {
		private final BoundingBox aabb;

		@Override
		public boolean isColliding (Frustum frustum) {
			return Intersector.intersectFrustumBounds(frustum, aabb);
		}

		@Override
		public boolean isColliding (Ray ray) {
			return Intersector.intersectRayBoundsFast(ray, aabb);
		}

		AABB () {
			Vector3 position = randomPosition();

			float width = MathUtils.random(0.01f, 1f);
			float height = MathUtils.random(0.01f, 1f);
			float depth = MathUtils.random(0.01f, 1f);

			Vector3 min = new Vector3(position.x - width / 2, position.y - height / 2, position.z - depth / 2);
			Vector3 max = new Vector3(position.x + width / 2, position.y + height / 2, position.z + depth / 2);
			aabb = new BoundingBox(min, max);

			Matrix4 transform = new Matrix4().setToTranslation(position);

			Material material = new Material(ColorAttribute.createDiffuse(COLOR_STANDARD));
			com.badlogic.gdx.graphics.g3d.utils.ModelBuilder mb = new com.badlogic.gdx.graphics.g3d.utils.ModelBuilder();
			mb.begin();
			MeshPartBuilder meshPartBuilder = mb.part("aabb", PRIMITIVE_TYPE, VertexAttributes.Usage.Position, material);
			meshPartBuilder.setVertexTransform(transform);
			BoxShapeBuilder.build(meshPartBuilder, width, height, depth);

			instance = new ModelInstance(mb.end());
		}
	}

	class Sphere extends Shape {
		private final com.badlogic.gdx.math.collision.Sphere sphere;

		@Override
		public boolean isColliding (Frustum frustum) {
			return frustum.sphereInFrustum(sphere.center, sphere.radius);
		}

		@Override
		public boolean isColliding (Ray ray) {
			return Intersector.intersectRaySphere(ray, sphere.center, sphere.radius, null);
		}

		Sphere () {
			Vector3 position = randomPosition();

			float diameter = MathUtils.random(0.01f, 1f);

			sphere = new com.badlogic.gdx.math.collision.Sphere(position, diameter / 2);

			Matrix4 transform = new Matrix4().setToTranslation(position);

			Material material = new Material(ColorAttribute.createDiffuse(COLOR_STANDARD));
			com.badlogic.gdx.graphics.g3d.utils.ModelBuilder mb = new com.badlogic.gdx.graphics.g3d.utils.ModelBuilder();
			mb.begin();
			MeshPartBuilder meshPartBuilder = mb.part("sphere", PRIMITIVE_TYPE, VertexAttributes.Usage.Position, material);
			meshPartBuilder.setVertexTransform(transform);
			SphereShapeBuilder.build(meshPartBuilder, diameter, diameter, diameter, 16, 16);

			instance = new ModelInstance(mb.end());
		}
	}

	class OBB extends Shape {
		private final OrientedBoundingBox obb;

		@Override
		public boolean isColliding (Frustum frustum) {
			return Intersector.intersectFrustumBounds(frustum, obb);
		}

		@Override
		public boolean isColliding (Ray ray) {
			return Intersector.intersectRayOrientedBoundsFast(ray, obb);
		}

		OBB () {
			Vector3 position = randomPosition();

			float width = MathUtils.random(0.01f, 1f);
			float height = MathUtils.random(0.01f, 1f);
			float depth = MathUtils.random(0.01f, 1f);

			Vector3 min = new Vector3(position.x - width / 2, position.y - height / 2, position.z - depth / 2);
			Vector3 max = new Vector3(position.x + width / 2, position.y + height / 2, position.z + depth / 2);

			BoundingBox bounds = new BoundingBox(min, max);
			Matrix4 transform = new Matrix4().rotate(Vector3.Y, MathUtils.random.nextFloat() * 180).rotate(Vector3.X,
				MathUtils.random.nextFloat() * 180);

			obb = new OrientedBoundingBox(bounds, transform);

			Material material = new Material(ColorAttribute.createDiffuse(COLOR_STANDARD));
			com.badlogic.gdx.graphics.g3d.utils.ModelBuilder mb = new com.badlogic.gdx.graphics.g3d.utils.ModelBuilder();
			mb.begin();
			MeshPartBuilder meshPartBuilder = mb.part("obb", GL20.GL_LINES, VertexAttributes.Usage.Position, material);
			BoxShapeBuilder.build(meshPartBuilder, obb.getCorner000(new Vector3()), obb.getCorner010(new Vector3()),
				obb.getCorner100(new Vector3()), obb.getCorner110(new Vector3()), obb.getCorner001(new Vector3()),
				obb.getCorner011(new Vector3()), obb.getCorner101(new Vector3()), obb.getCorner111(new Vector3()));

			instance = new ModelInstance(mb.end());
		}
	}

}
