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

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;

public class MeshBuilderTest extends BaseG3dHudTest {
	protected Environment environment;

	private static final int NUM_SCENES = 3;
	int sceneIndex = 0;
	int currentPrimitiveType = GL20.GL_TRIANGLES;
	int currentDivisionCount = 16;
	String sceneDescription;

	ModelBuilder mdlBuilder;
	MeshBuilder mshBuilder;
	Model baseModel;
	Model model;

	@Override
	public void create () {
		super.create();
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, -1.0f, -0.8f));

		cam.position.set(1, 1, 1);
		cam.lookAt(0, 0, 0);
		cam.update();
		showAxes = true;

		mdlBuilder = new ModelBuilder();
		mshBuilder = new MeshBuilder();

		onModelClicked("g3d/ship.obj");
	}

	private final BoundingBox bounds = new BoundingBox();

	@Override
	public void render () {
		super.render();
		fpsLabel.setY(fpsLabel.getHeight());
	}

	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		batch.render(instances, environment);
	}

	@Override
	protected void getStatus (StringBuilder stringBuilder) {
		super.getStatus(stringBuilder);

		stringBuilder.append(sceneDescription);
		stringBuilder.append("\n press space or menu to view next scene");
	}

	protected String currentlyLoading;

	@Override
	protected void onModelClicked (final String name) {
		if (name == null) return;

		currentlyLoading = "data/" + name;
		baseModel = null;
		assets.load(currentlyLoading, Model.class);
		loading = true;
	}

	@Override
	protected void onLoaded () {
		if (currentlyLoading == null || currentlyLoading.length() == 0) return;

		baseModel = assets.get(currentlyLoading, Model.class);
		currentlyLoading = null;
		setScene();
	}

	protected void setScene () {
		if (baseModel == null) return;

		if (model != null)
			model.dispose();
		int primType = currentPrimitiveType;
		Material material = new Material();
		float size = 5;
		int div = currentDivisionCount;

		mdlBuilder.begin();

		switch (sceneIndex) {
		case 0:
			modelsWindow.setVisible(false);
			sceneDescription = " - Simple line primitives";
			primType = GL20.GL_LINES;
			mshBuilder.begin(Usage.Position, primType);
			mshBuilder.setVertexTransform(transform.setToTranslation(-(size + 1), 0, 0));
			// draw a square
			mshBuilder.rect(	-size/2, -size/2, 0,
									-size/2, size/2, 0,
									size/2, size/2, 0,
									size/2, -size/2, 0,
									0, 1, 0);
			mshBuilder.setVertexTransform(transform.setToTranslation(0, -(size + 1), 0));
			// draw a cross
			mshBuilder.line(-size / 2, -size / 2, 0, size / 2, size / 2, 0);
			mshBuilder.line(-size / 2, size / 2, 0, size / 2, -size / 2, 0);
			mshBuilder.setVertexTransform(transform.setToTranslation((size + 1), 0, 0));
			// draw a circle
			mshBuilder.circle(size / 2, div, 0, 0, 0, 0, 0, 1, 0, 360);
			mshBuilder.setVertexTransform(transform.setToTranslation(0, (size + 1), 0));
			// draw a triangle
			mshBuilder.triangle(new Vector3(-size / 2, -size / 2, 0), new Vector3(size / 2, -size / 2, 0), new Vector3(0, size / 2, 0));
			break;

		case 1:
			modelsWindow.setVisible(false);
			sceneDescription = " - Simple surface primitives\n press P to change primitive type\n press +/- to change division count: "+currentDivisionCount;
			mshBuilder.begin(Usage.Position | Usage.Normal | Usage.TextureCoordinates, primType);
			mshBuilder.setVertexTransform(null);
			mshBuilder.box(size, size, size);
			mshBuilder.setVertexTransform(transform.setToTranslation(-(size + 1), 0, 0));
			mshBuilder.capsule(size / 2f, 2 * size, div);
			mshBuilder.setVertexTransform(transform.setToTranslation(0, 0, -(size + 1)));
			mshBuilder.cylinder(size, size, size, div);
			mshBuilder.setVertexTransform(transform.setToTranslation(size + 1, 0, 0));
			mshBuilder.cone(size, size, size, div);
			mshBuilder.setVertexTransform(transform.setToTranslation(0, size - 1, 0));
			mshBuilder.patch(	-size/2, size/4, size/2,
									size/2, 0, size/2,
									size/2, size/4, -size/2,
									-size/2, 0, -size/2, 0, 1, 0, div, div);
			mshBuilder.setVertexTransform(transform.setToTranslation(0, 0, size + 1));
			mshBuilder.sphere(size, size, size, div, div);
			mshBuilder.setVertexTransform(transform.idt());
			mshBuilder.arrow(0, (size+1), 0, 0, (size+1) + size, 0, size/32, size/16, div);
			break;

		case 2:
			modelsWindow.setVisible(true);
			sceneDescription = " - Mesh repeated with offsets, (only first mesh of model is used)";
			Mesh mesh = baseModel.meshParts.get(0).mesh;
			primType = baseModel.meshParts.get(0).primitiveType;
			material = baseModel.materials.get(0);
			mesh.calculateBoundingBox(bounds);
			Vector3 v = bounds.getDimensions();
			mshBuilder.begin(mesh.getVertexAttributes());
			mshBuilder.setVertexTransform(null);
			mshBuilder.mesh(mesh);
			mshBuilder.setVertexTransform(transform.setToTranslation(v.x, 0, 0));
			mshBuilder.mesh(mesh);
			mshBuilder.setVertexTransform(transform.setToTranslation(0, 0, v.z));
			mshBuilder.mesh(mesh);
			break;

		}

		mdlBuilder.part("model", mshBuilder.end(), primType, material);
		model = mdlBuilder.end();

		instances.clear();
		final ModelInstance instance = new ModelInstance(model);
		instance.transform = transform;
		instances.add(instance);

		instance.calculateBoundingBox(bounds);
		cam.position.set(1, 1, 1).nor().scl(bounds.getDimensions().len() * 0.75f + bounds.getCenter().len());
		cam.up.set(0, 1, 0);
		cam.lookAt(0, 0, 0);
		cam.far = 50f + bounds.getDimensions().len() * 2.0f;
		cam.update();
	}

	@Override
	public boolean keyUp (int keycode) {
		if (keycode == Keys.SPACE || keycode == Keys.MENU) {
			sceneIndex = (sceneIndex+1) % NUM_SCENES;
			setScene();
		}
		if (sceneIndex == 1) {
			if (keycode == Keys.P) {
				switch(currentPrimitiveType) {
				case GL20.GL_POINTS:
					currentPrimitiveType = GL20.GL_LINES;
					break;
				case GL20.GL_LINES:
					currentPrimitiveType = GL20.GL_TRIANGLES;
					break;
				case GL20.GL_TRIANGLES:
					currentPrimitiveType = GL20.GL_POINTS;
					break;
				}
				setScene();
			}
			if (keycode == Keys.PLUS) {
				currentDivisionCount = Math.min(Math.max(currentDivisionCount+1, 1), 32 );
				setScene();
			}
			if (keycode == Keys.MINUS) {
				currentDivisionCount = Math.min(Math.max(currentDivisionCount-1, 1), 32 );
				setScene();
			}
		}
		return super.keyUp(keycode);
	}
}
