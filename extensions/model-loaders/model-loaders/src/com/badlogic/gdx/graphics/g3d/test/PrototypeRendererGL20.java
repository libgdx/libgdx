
package com.badlogic.gdx.graphics.g3d.test;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.AnimatedModelInstance;
import com.badlogic.gdx.graphics.g3d.ModelRenderer;
import com.badlogic.gdx.graphics.g3d.StillModelInstance;
import com.badlogic.gdx.graphics.g3d.experimental.MaterialShaderHandler;
import com.badlogic.gdx.graphics.g3d.lights.LightManager;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.AnimatedModel;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonSubMesh;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

//stuff that happens
//0. render begin
//1. frustum culling
//1.1 if animated, animation is solved and..
//1.2. all models and instances are put to one queue
//3. render ends
//for all models
//5. batching involving shaders, materials and texture should happen.(impossible to do perfect.)
//4. closest lights are calculated per model
//6  model are rendered

//WIP
//tranparency and more batching

public class PrototypeRendererGL20 implements ModelRenderer {

	static final int SIZE = 256;// TODO better way
	final private Array<Model> modelQueue = new Array<Model>(false, SIZE, Model.class);
	final private Array<StillModelInstance> modelInstances = new Array<StillModelInstance>(false, SIZE, StillModelInstance.class);

	final private MaterialShaderHandler materialShaderHandler;
	private LightManager lightManager;
	private boolean drawing;
	private ShaderProgram currentShader;
	private Material currentMaterial;
	final private Matrix3 normalMatrix = new Matrix3();
	public Camera cam;

	// TODO maybe there is better way
	public PrototypeRendererGL20 (LightManager lightManager) {
		this.lightManager = lightManager;
		materialShaderHandler = new MaterialShaderHandler(lightManager);
	}

	@Override
	public void begin () {
		drawing = true;
		// all setting has to be done before this
		// example: camera updating or updating lights positions
	}

	@Override
	public void draw (StillModel model, StillModelInstance instance) {
		if (cam != null) if (!cam.frustum.sphereInFrustum(instance.getSortCenter(), instance.getBoundingSphereRadius())) return;
		modelQueue.add(model);
		modelInstances.add(instance);
	}

	@Override
	public void draw (AnimatedModel model, AnimatedModelInstance instance) {

		if (cam != null) if (!cam.frustum.sphereInFrustum(instance.getSortCenter(), instance.getBoundingSphereRadius())) return;
		model.setAnimation(instance.getAnimation(), instance.getAnimationTime(), instance.isLooping());

		// move skinned models to drawing list
		modelQueue.add(model);
		modelInstances.add(instance);
	}

	@Override
	public void end () {
		// maybe rethink this :)
		flush();
	}

	private void flush () {
		// sort opaque meshes from front to end, perfect accuracy is not needed

		// find N nearest lights per model
		// draw all models from opaque queue
		for (int i = 0; i < modelQueue.size; i++) {
			final StillModelInstance instance = modelInstances.items[i];
			final Matrix4 modelMatrix = instance.getTransform();
			final Vector3 center = instance.getSortCenter();
			lightManager.calculateLights(center.x, center.y, center.z);

			boolean matrixChanged = true;
			final Model model = modelQueue.items[i];
			final SubMesh subMeshes[] = model.getSubMeshes();
			final Material materials[] = instance.getMaterials();

			final int len = subMeshes.length;
			for (int j = 0; j < len; j++) {
				final SubMesh subMesh = subMeshes[j];
				final Material material = materials != null ? materials[j] : subMesh.material;
				if (bindShader(material) || matrixChanged) {
					currentShader.setUniformMatrix("u_normalMatrix", normalMatrix.set(modelMatrix), false);
					currentShader.setUniformMatrix("u_modelMatrix", modelMatrix, false);
				}
				if (material != currentMaterial) {
					currentMaterial = material;
					currentMaterial.bind(currentShader);
				}
				subMesh.getMesh().render(currentShader, subMesh.primitiveType);
			}
		}
		currentMaterial = null;
		if (currentShader != null) {
			currentShader.end();
			currentShader = null;
		}
		// if transparent queue is not empty enable blending(this force gpu to
		// flush and there is some time to sort)

		// sort transparent models(submeshes??) accuracy is needed

		// do drawing for transparent models

		// clear all queus
		modelQueue.clear();
		modelInstances.clear();
		drawing = false;
	}

	/** @param material
	 * @return true if new shader was binded */
	boolean bindShader (Material material) {

		if (material.shader == null) material.shader = materialShaderHandler.getShader(material);

		if (material.shader == currentShader) return false;

		currentShader = material.shader;
		currentShader.begin();

		lightManager.applyGlobalLights(currentShader);
		lightManager.applyLights(currentShader);
		currentShader.setUniformMatrix("u_projectionViewMatrix", cam.combined);
		currentShader.setUniformf("camPos", cam.position.x, cam.position.y, cam.position.z);
		return true;
	}

	public void dispose () {
		materialShaderHandler.dispose();
	}
}
