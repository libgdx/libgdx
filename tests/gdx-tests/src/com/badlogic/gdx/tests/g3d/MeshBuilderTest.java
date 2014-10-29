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
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;

public class MeshBuilderTest extends BaseG3dHudTest {
	protected Environment environment;

	private static final int SCENE_LINE_PRIMITIVES		= 0;
	private static final int SCENE_SURFACE_PRIMITIVES	= 1;
	private static final int SCENE_SWEEP					= 2;
	private static final int SCENE_SWEEP_MOTION			= 3;
	private static final int SCENE_MESH_INSERTS			= 4;
	private static final int NUM_SCENES						= 5;

	int sceneIndex = 0;
	int currentPrimitiveType = GL20.GL_TRIANGLES;
	int currentDivisionCount = 16;
	String sceneDescription;

	ModelBuilder mdlBuilder;
	MeshBuilder mshBuilder;
	Model baseModel;
	Model model;
	Mesh currentMesh;

	CatmullRomSpline<Vector3> sweepPath;
	Polygon sweepShape;
	boolean shapeDirty;
	int sweepSides = 10;
	float sweepRadius = 1;
	float sweepStarRadius = 1.5f * sweepRadius;
	boolean profileContinuous = true;
	boolean profileSmooth = false;
	boolean pathSmooth = true;

	CatmullRomSpline<Vector3> wormPath;
	Polygon wormShape;
	Mesh wormMesh = null;
	float wormSize = 0.3f;
	float wormOffset = 0.09f;
	float[] wormColors;
	float time = 0;

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

		shapeDirty = true;
		Vector3[] pathPoints = new Vector3[]{
			new Vector3(-20, 0, -10),
			new Vector3(-10, 0, -10),
			new Vector3(0, 5, -10),
			new Vector3(10, 0, -10),
			new Vector3(10, 0, 10),
			new Vector3(20, 0, 10),
			};
		sweepPath = new CatmullRomSpline<Vector3>(pathPoints, false);

		wormShape = new Polygon(new float[]{ 0, 0, 0.5f, 0, 0, 0.5f });
		pathPoints = new Vector3[]{
			new Vector3(-10, 0, -10),
			new Vector3(0, 0, -10),
			new Vector3(10, 0, -10),
			new Vector3(10, 0, 10),
			new Vector3(0, 0, 10),
			new Vector3(10, 0, 0),
			};
		wormPath = new CatmullRomSpline<Vector3>(pathPoints, true);

		onModelClicked("g3d/ship.obj");
	}

	private final BoundingBox bounds = new BoundingBox();

	@Override
	public void render () {
		super.render();
		// move label as some scenes have extra commands
		fpsLabel.setY(fpsLabel.getPrefHeight()/2);
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
		setScene(true);
	}

	@Override
	public void render (Array<ModelInstance> instances) {

		if (sceneIndex == SCENE_SWEEP_MOTION) {
			time += 0.01f;
			wormOffset = (wormOffset + 0.007f) % 1f;
			rebuildWorm();
		}

		super.render(instances);
	}

	private float[] getRandomColors(int numColors) {

		float[] colors = new float[3*numColors];
		for (int i = 0; i < colors.length; i++)
			colors[i] = MathUtils.random();
		return colors;
	}

	private Polygon buildPoly (int sides, float radius, float starRadius) {

		float[] vertices = new float[2 * sides];
		float seg = MathUtils.PI2 / (float)sides;
		float turn = 0;
		for (int i = 0; i < vertices.length; i += 2) {
			turn += seg;
			float r = ((i % 4) == 0 ? radius : starRadius);
			vertices[i  ] = r * MathUtils.cos(turn);
			vertices[i+1] = r * MathUtils.sin(turn);
		}
		return new Polygon(vertices);
	}

	private void rebuildSweep() {

		if (shapeDirty) {
			shapeDirty = false;
			sweepShape = buildPoly(sweepSides, sweepRadius, sweepStarRadius);
		}

		mshBuilder.begin(Usage.Position | Usage.Normal | Usage.Color, GL20.GL_TRIANGLES);
		mshBuilder.setProfileShape(sweepShape.getTransformedVertices(), profileContinuous, profileSmooth);
		mshBuilder.sweep(sweepPath, pathSmooth, 32, 1f, 1f);
		if (currentMesh != null)
			mshBuilder.end(currentMesh);
	}

	private void rebuildWorm () {

		mshBuilder.begin(Usage.Position | Usage.Normal | Usage.Color, GL20.GL_TRIANGLES);
		mshBuilder.setGradientColor(wormColors, true);
		mshBuilder.setScaleInterpolation(Interpolation.sine, 1f, 4f, true);
		mshBuilder.setProfileShape(wormShape.getTransformedVertices(), true, false);
		// choose an interval that gives a creeping motion
		float end = wormOffset + wormSize*(0.6f+0.4f*MathUtils.sin(time*MathUtils.PI2));
		mshBuilder.sweep(wormPath, true, wormOffset, end, 32, 1f, 1f);
		if (currentMesh != null)
			mshBuilder.end(currentMesh);
	}

	protected void setScene (boolean resetCamera) {
		if (baseModel == null) return;

		if (model != null)
			model.dispose();
		int primType = currentPrimitiveType;
		Material material = new Material();
		float size = 5;
		int div = currentDivisionCount;

		mdlBuilder.begin();

		// reset mesh building params
		mshBuilder.setVertexTransform(null);
		mshBuilder.setScaleInterpolation(null, 0, 0, false);
		mshBuilder.setGradientColor(null, false);

		switch (sceneIndex) {
		case SCENE_LINE_PRIMITIVES:
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

		case SCENE_SURFACE_PRIMITIVES:
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

		case SCENE_SWEEP:
			modelsWindow.setVisible(false);
			sceneDescription = " - Polygon profile swept along a path\n press +/- to change polygon side count: "+sweepSides+
				"\n press 1 to toggle path smoothing "+(pathSmooth ? "off" : "on")+
				"\n press 2 to toggle profile smoothing "+(profileSmooth ? "off" : "on")+
				"\n press 3 to toggle profile continuity "+(profileContinuous ? "off" : "on")+
				"\n press 4 to toggle profile shape to "+(sweepRadius == sweepStarRadius ? "star" : "polygon");
			currentMesh = null;
			rebuildSweep();
			break;

		case SCENE_SWEEP_MOTION:
			modelsWindow.setVisible(false);
			sceneDescription = " - Triangle profile swept along a path with varying interval";
			currentMesh = null;
			wormColors = getRandomColors(MathUtils.random(3, 32));
			rebuildWorm();
			break;

		case SCENE_MESH_INSERTS:
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

		currentMesh = mshBuilder.end();
		mdlBuilder.part("model", currentMesh, primType, material);
		model = mdlBuilder.end();

		instances.clear();
		final ModelInstance instance = new ModelInstance(model);
		instance.transform = transform;
		instances.add(instance);

		if (!resetCamera)
			return;

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
			setScene(true);
		}
		if (sceneIndex == SCENE_SURFACE_PRIMITIVES) {
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
				setScene(false);
			}
			if (keycode == Keys.PLUS) {
				currentDivisionCount = Math.min(Math.max(currentDivisionCount+1, 1), 32);
				setScene(false);
			}
			if (keycode == Keys.MINUS) {
				currentDivisionCount = Math.min(Math.max(currentDivisionCount-1, 1), 32);
				setScene(false);
			}
		} else if (sceneIndex == SCENE_SWEEP) {
			if (keycode == Keys.NUM_1) {
				pathSmooth = !pathSmooth;
				setScene(false);
			}
			if (keycode == Keys.NUM_2) {
				profileSmooth = !profileSmooth;
				setScene(false);
			}
			if (keycode == Keys.NUM_3) {
				profileContinuous = !profileContinuous;
				setScene(false);
			}
			if (keycode == Keys.NUM_4) {
				sweepStarRadius = (sweepStarRadius == sweepRadius ? 1.5f * sweepRadius : sweepRadius);
				shapeDirty = true;
				setScene(false);
			}
			if (keycode == Keys.PLUS) {
				sweepSides = Math.min(Math.max(sweepSides+1, 3), 32);
				shapeDirty = true;
				setScene(false);
			}
			if (keycode == Keys.MINUS) {
				sweepSides = Math.min(Math.max(sweepSides-1, 3), 32);
				shapeDirty = true;
				setScene(false);
			}
		}
		return super.keyUp(keycode);
	}
}
