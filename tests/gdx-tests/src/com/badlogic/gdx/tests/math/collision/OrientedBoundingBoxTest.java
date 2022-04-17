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

package com.badlogic.gdx.tests.math.collision;

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
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

public class OrientedBoundingBoxTest extends GdxTest implements ApplicationListener {

	private static final int NUM_BOXES = 100;

	private PerspectiveCamera camera;
	private CameraInputController cameraController;
	private ModelBatch modelBatch;

	private SpriteBatch batch;
	private BitmapFont font;

	private static final Color COLOR_STANDARD = Color.BLUE;
	private static final Color COLOR_MOUSE_OVER = Color.GREEN;
	private static final Color COLOR_INTERSECTION = Color.GOLD;

	private long seed;
	private Array<Box> boxes = new Array<>();

	@Override
	public void create () {
		font = new BitmapFont(Gdx.files.internal("data/lsans-15.fnt"), false);
		batch = new SpriteBatch();
		modelBatch = new ModelBatch();

		setupScene();
		setupCamera();
		InputMultiplexer inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(cameraController);
		inputMultiplexer.addProcessor(this);
		Gdx.input.setInputProcessor(inputMultiplexer);
	}

	private void setupScene () {
		seed = MathUtils.random.nextLong();
		MathUtils.random.setSeed(seed);

		// Dispose models if any
		for (Box box : boxes) {
			box.model.dispose();
		}
		// Clear the list
		boxes.clear();
		for (int i = 0; i < NUM_BOXES; i++) {
			boxes.add(new Box());
		}
	}

	private void setupCamera () {
		camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = 0.01f;
		camera.far = 100f;
		camera.position.set(0, 5, -2);
		camera.lookAt(Vector3.Zero);
		camera.update();

		cameraController = new CameraInputController(camera);
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

		cameraController.update();
		checkCollision();

		// Draw Box
		modelBatch.begin(camera);
		for (Box box : boxes) {
			box.update();
			modelBatch.render(box.instance);
		}
		modelBatch.end();
	}

	private void checkCollision () {
		Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());

		// Reset all boxes
		for (Box box : boxes) {
			box.intersects = false;
			box.updateColor(COLOR_STANDARD);
		}

		for (int i = 0; i < boxes.size; i++) {
			Box box = boxes.get(i);

			for (int j = i + 1; j < boxes.size; j++) {
				Box anotherBox = boxes.get(j);

				if (box.orientedBoundingBox.intersects(anotherBox.orientedBoundingBox)) {
					if (!box.intersects) {
						box.updateColor(COLOR_INTERSECTION);
						box.intersects = true;
					}

					anotherBox.updateColor(COLOR_INTERSECTION);
					anotherBox.intersects = true;
				}
			}

			boolean mouseOver = Intersector.intersectRayOrientedBoundsFast(ray, box.orientedBoundingBox);
			if (mouseOver) {
				box.updateColor(COLOR_MOUSE_OVER);
				box.intersects = true;
			}
		}

	}

	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
		modelBatch.dispose();
		for (Box box : boxes) {
			box.model.dispose();
		}
	}

	@Override
	public boolean keyUp (int keycode) {
		if (Input.Keys.SPACE == keycode) {
			setupScene();
		}
		return super.keyUp(keycode);
	}

	class Box {
		private final OrientedBoundingBox orientedBoundingBox;
		public Model model;
		public ModelInstance instance;
		public Matrix4 movement;

		public boolean intersects = false;

		Box () {
			BoundingBox bounds = new BoundingBox(new Vector3(-0.5f, -0.5f, -0.5f), new Vector3(0.5f, 0.5f, 0.5f));
			OrientedBoundingBox orientedBoundingBox = new OrientedBoundingBox(bounds);
			this.orientedBoundingBox = orientedBoundingBox;
			model = buildModel(orientedBoundingBox);
			instance = new ModelInstance(model);

			buildMovement();
		}

		private void buildMovement () {
			Random random = new Random();
			float speed = random.nextFloat();
			float radius = 1 / 30f;

			movement = new Matrix4()
				.setToTranslation(new Vector3(random.nextFloat() * radius, random.nextFloat() * radius, random.nextFloat() * radius));

			switch (random.nextInt() % 3) {
			default:
				movement.rotate(new Quaternion(Vector3.X, speed));
				break;
			case 1:
				movement.rotate(new Quaternion(Vector3.Y, speed));
				break;
			case 2:
				movement.rotate(new Quaternion(Vector3.Z, speed));
			}

			// Update a few times to spread the boxes
			for (int i = 0; i < 100; i++) {
				update();
			}
		}

		public void update () {
			orientedBoundingBox.mul(movement);
			instance.transform.mul(movement);
		}

		private Model buildModel (OrientedBoundingBox orientedBoundingBox) {
			Material material = new Material(ColorAttribute.createDiffuse(COLOR_STANDARD));
			com.badlogic.gdx.graphics.g3d.utils.ModelBuilder mb = new com.badlogic.gdx.graphics.g3d.utils.ModelBuilder();
			mb.begin();
			MeshPartBuilder meshPartBuilder = mb.part("hitbox", GL20.GL_LINES, VertexAttributes.Usage.Position, material);
			BoxShapeBuilder.build(meshPartBuilder, orientedBoundingBox.getCorner000(new Vector3()),
				orientedBoundingBox.getCorner010(new Vector3()), orientedBoundingBox.getCorner100(new Vector3()),
				orientedBoundingBox.getCorner110(new Vector3()), orientedBoundingBox.getCorner001(new Vector3()),
				orientedBoundingBox.getCorner011(new Vector3()), orientedBoundingBox.getCorner101(new Vector3()),
				orientedBoundingBox.getCorner111(new Vector3()));
			return mb.end();
		}

		public void updateColor (Color color) {
			Material material = instance.materials.get(0);
			ColorAttribute attribute = (ColorAttribute)material.get(ColorAttribute.Diffuse);
			attribute.color.set(color);
		}
	}

}
