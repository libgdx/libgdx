package com.badlogic.gdx.graphics.g3d.test;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.AnimatedModelInstance;
import com.badlogic.gdx.graphics.g3d.ModelRenderer;
import com.badlogic.gdx.graphics.g3d.StillModelInstance;
import com.badlogic.gdx.graphics.g3d.lights.LightManager;
import com.badlogic.gdx.graphics.g3d.model.AnimatedModel;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

//stuff that happens
//0. render begin
//1. still and animated models are added to different queues
//2. render ends
//3. frustum culling
//4  animated models meshes are calculated and pushed to model queue
//5. closest lights are calculated
//6  models are rendered

//WIP
//how to choose shader
//when to compile shaders.
//is #ifdef scheme enough for shader combinations
//more stuff

public class PrototypeRendererGL20 implements ModelRenderer {

	static final int SIZE = 256;// TODO better way
	final private Array<Model> modelQueue = new Array<Model>(false, SIZE,
			Model.class);
	final private Array<StillModelInstance> stillModelInstances = new Array<StillModelInstance>(
			false, SIZE, StillModelInstance.class);

	final private Array<AnimatedModel> animatedModelQueue = new Array<AnimatedModel>(
			false, SIZE, AnimatedModel.class);
	final private Array<AnimatedModelInstance> animatedModelInstances = new Array<AnimatedModelInstance>(
			false, SIZE, AnimatedModelInstance.class);

	private LightManager lightManager;
	private boolean drawing;
	private ShaderProgram shader;
	final private Matrix3 normalMatrix = new Matrix3();
	public Camera cam;

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
		// add to render queue
		modelQueue.add(model);
		stillModelInstances.add(instance);
	}

	@Override
	public void draw(AnimatedModel model, AnimatedModelInstance instance) {
		animatedModelQueue.add(model);
		animatedModelInstances.add(instance);
	}

	@Override
	public void end() {

		// TODO how materials is accounted

		// batched frustum vs bounding sphere culling(if slow JNI) for all
		// models,
		// Maybe at somewhere else,
		// TODO move this to cullingManager
		if (cam != null) {
			for (int i = modelQueue.size - 1; i >= 0; i--) {
				final StillModelInstance instance = stillModelInstances.items[i];
				if (!cam.frustum.sphereInFrustum(instance.getSortCenter(),
						instance.getBoundingSphereRadius())) {
					stillModelInstances.removeIndex(i);
					modelQueue.removeIndex(i);
				}
			}
			for (int i = animatedModelInstances.size - 1; i >= 0; i--) {
				final StillModelInstance instance = animatedModelInstances.items[i];
				if (!cam.frustum.sphereInFrustum(instance.getSortCenter(),
						instance.getBoundingSphereRadius())) {
					animatedModelInstances.removeIndex(i);
					animatedModelQueue.removeIndex(i);
				}
			}
		}

		// sort models(submeshes??)to tranparent and opaque render queue, maybe
		// that can be done at flush?

		flush();
	}

	private void flush() {
		drawing = false;

		

		// frustum culling via cullingManager

		for (int i = 0; i < animatedModelQueue.size; i++) {
			final AnimatedModelInstance instance = animatedModelInstances.items[i];
			final String name = instance.getAnimation();
			final float time = instance.getAnimationTime();
			boolean looping = instance.isLooping();
			final AnimatedModel model = animatedModelQueue.items[i];
			model.setAnimation(name, time, looping);

			// move skinned models to drawing list
			modelQueue.add(model);
		}
		animatedModelQueue.clear();
		animatedModelInstances.clear();

		// sort opaque meshes from front to end, perfect accuracy is not needed
		shader.begin();
		
		lightManager.applyGlobalLights(shader);
		// find N nearest lights per model
		// draw all models from opaque queue
		for (int i = 0; i < modelQueue.size; i++) {
			final StillModelInstance instance = stillModelInstances.items[i];
			final Matrix4 modelMatrix = instance.getTransform();
			// if normals
			shader.setUniformMatrix("u_normalMatrix",
					normalMatrix.set(modelMatrix), false);
			shader.setUniformMatrix("u_modelMatrix", modelMatrix, false);
			// TODO fastest way to calculate normalsToWorld matrix? JNI
			// inversion and send with transpose flag?
			lightManager.calculateAndApplyLightsToModel(
					instance.getSortCenter(), shader);

			modelQueue.items[i].render(shader);

		}
		shader.end();

		// if transparent queue is not empty enable blending(this force gpu to
		// flush and there is some time to sort)

		// sort transparent models(submeshes??) accuracy is needed

		// do drawing for transparent models

		// clear all queus
		modelQueue.clear();
		stillModelInstances.clear();

	}
}
