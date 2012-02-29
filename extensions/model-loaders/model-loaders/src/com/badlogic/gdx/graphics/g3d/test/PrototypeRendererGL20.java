package com.badlogic.gdx.graphics.g3d.test;

import com.badlogic.gdx.graphics.g3d.AnimatedModelInstance;
import com.badlogic.gdx.graphics.g3d.ModelRenderer;
import com.badlogic.gdx.graphics.g3d.StillModelInstance;
import com.badlogic.gdx.graphics.g3d.lights.LightManager;
import com.badlogic.gdx.graphics.g3d.model.AnimatedModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class PrototypeRendererGL20 implements ModelRenderer {

	static final int SIZE = 128;// TODO better way
	final private Array<StillModel> stillModelQueue = new Array<StillModel>(
			false, SIZE, StillModel.class);
	final private Array<StillModelInstance> stillModelInstances = new Array<StillModelInstance>(
			false, SIZE, StillModelInstance.class);

	final private Array<StillModel> animatedModelQueue = new Array<StillModel>(
			false, SIZE, StillModel.class);
	final private Array<AnimatedModelInstance> animatedModelInstances = new Array<AnimatedModelInstance>(
			false, SIZE, AnimatedModelInstance.class);

	private LightManager lightManager;
	private boolean drawing;
	private ShaderProgram shader;

	public void setLightManager(LightManager lightManager) {
		this.lightManager = lightManager;
		if (drawing)
			flush();
	}

	// TODO REMOVE THIS
	public void setShader(ShaderProgram shader) {
		this.shader = shader;
	}

	@Override
	public void begin() {
		drawing = true;

		// all setting has to be done before this

		// example: camera updating or updating lights positions
	}

	@Override
	public void draw(StillModel model, StillModelInstance instance) {
		// add render queue
		stillModelQueue.add(model);
		stillModelInstances.add(instance);
	}

	@Override
	public void draw(AnimatedModel model, AnimatedModelInstance instance) {
		// add animated render queue
	}

	@Override
	public void end() {

		// TODO how materials is accounted

		// batched frustum vs bounding box culling(if slow JNI) for all models,
		// Maybe at somewhere else,

		// sort models(submeshes??)to tranparent and opaque render queue, maybe
		// that can be done at flush?

		flush();
	}

	private void flush() {
		drawing = false;

		lightManager.applyGlobalLights(shader);

		// frustumculling via cullingManager

		// frustum culling for all point lights via cullingManager

		// find N nearest lights per model

		// sort opaque meshes from front to end, accuracy is not needed

		// draw all from opaque queu
		for (int i = 0; i < stillModelQueue.size; i++) {
			final StillModelInstance instance = stillModelInstances.items[i];

			shader.setUniformMatrix("u_modelMatrix", instance.getTransform(),
					false);
			// TODO fastest way to calculate normalsToWorld matrix? JNI
			// inversion and send with transpose flag?
			lightManager.calculateAndApplyLightsToModel(
					instance.getSortCenter(), shader);
			stillModelQueue.items[i].render(shader);
		}

		// if transparent queue is not empty enable blending(this force gpu to
		// flush and there is some time to sort)

		// sort transparent models(submeshes??) accuracy is needed

		// do drawing for transparent models

		// clear all queus
		stillModelQueue.clear();
		stillModelInstances.clear();
		animatedModelQueue.clear();
		animatedModelInstances.clear();
	}
}
