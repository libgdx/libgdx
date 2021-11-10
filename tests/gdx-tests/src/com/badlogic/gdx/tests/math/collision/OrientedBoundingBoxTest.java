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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.tests.utils.GdxTest;

public class OrientedBoundingBoxTest extends GdxTest implements ApplicationListener {
	private OrientedBoundingBox orientedBoundingBox;
	private Model model;
	private ModelInstance instance;

	private PerspectiveCamera camera;
	private CameraInputController cameraController;
	private ModelBatch modelBatch;

	private boolean colliding = false;
	private static final Color STANDARD_COLOR = Color.BLUE;
	private static final Color HIGHLIGHT_COLOR = Color.GREEN;

	@Override
	public void create () {
		modelBatch = new ModelBatch();

		orientedBoundingBox = new OrientedBoundingBox(new Vector3(-1, -1, -1), new Vector3(1, 1, 1));
		orientedBoundingBox.orientation.set(new Quaternion(Vector3.Y, 20));

		model = buildModel();
		instance = new ModelInstance(model);

		setupCamera();
		Gdx.input.setInputProcessor(cameraController);
	}

	private Model buildModel () {
		Material material = new Material(ColorAttribute.createDiffuse(STANDARD_COLOR));
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

	private void setupCamera () {
		camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = 0.01f;
		camera.far = 100f;
		camera.position.set(5, 5, -5);
		camera.lookAt(Vector3.Zero);
		camera.update();

		cameraController = new CameraInputController(camera);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		cameraController.update();
		checkCollision();

		// Draw Box
		modelBatch.begin(camera);
		modelBatch.render(instance);
		modelBatch.end();
	}

	private void checkCollision () {
		Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
		boolean intersects = Intersector.intersectRayOrientedBoundsFast(ray, orientedBoundingBox);

		if (intersects && !colliding) {
			// Colliding the first time
			instance.materials.get(0).set(ColorAttribute.createDiffuse(HIGHLIGHT_COLOR));
			colliding = true;
		} else if (!intersects && colliding) {
			// Not colliding anymore
			instance.materials.get(0).set(ColorAttribute.createDiffuse(STANDARD_COLOR));
			colliding = false;
		}
	}

	@Override
	public void dispose () {
		modelBatch.dispose();
		model.dispose();
	}
}
