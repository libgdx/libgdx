
package com.badlogic.gdx.graphics.g3d.test;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g3d.AnimatedModelInstance;
import com.badlogic.gdx.graphics.g3d.ModelRenderer;
import com.badlogic.gdx.graphics.g3d.StillModelInstance;
import com.badlogic.gdx.graphics.g3d.experimental.MaterialShaderHandler;
import com.badlogic.gdx.graphics.g3d.lights.LightManager;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.MaterialAttribute;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
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

	private ShaderProgram currentShader;
	final private TextureAttribute lastTexture[] = new TextureAttribute[TextureAttribute.MAX_TEXTURE_UNITS];

	private void flush () {
		// sort opaque meshes from front to end, perfect accuracy is not needed

		Material currentMaterial = null;
		// find N nearest lights per model
		// draw all models from opaque queue
		for (int i = 0; i < modelQueue.size; i++) {
			final StillModelInstance instance = modelInstances.items[i];
			final Vector3 center = instance.getSortCenter();
			lightManager.calculateLights(center.x, center.y, center.z);
			final Matrix4 modelMatrix = instance.getTransform();
			normalMatrix.set(modelMatrix);

			final SubMesh subMeshes[] = modelQueue.items[i].getSubMeshes();
			final Material materials[] = instance.getMaterials();

			boolean matrixChanged = true;
			for (int j = 0; j < subMeshes.length; j++) {

				final SubMesh subMesh = subMeshes[j];
				final Material material = materials != null ? materials[j] : subMesh.material;
				if (bindShader(material) || matrixChanged) {
					currentShader.setUniformMatrix("u_normalMatrix", normalMatrix, false);
					currentShader.setUniformMatrix("u_modelMatrix", modelMatrix, false);
					currentMaterial = null;
				}
				if ((material != null) && (material != currentMaterial)) {
					currentMaterial = material;
					for (int k = 0, len = currentMaterial.attributes.length; k < len; k++) {
						final MaterialAttribute atrib = currentMaterial.attributes[k];

						// special case for textures. really important to batch these
						if (atrib instanceof TextureAttribute) {
							final TextureAttribute texAtrib = (TextureAttribute)atrib;
							if (!texAtrib.equals(lastTexture[texAtrib.unit])) {
								lastTexture[texAtrib.unit] = texAtrib;
								texAtrib.texture.bind(texAtrib.unit);
								Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, texAtrib.minFilter);
								Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, texAtrib.magFilter);
								Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, texAtrib.uWrap);
								Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, texAtrib.vWrap);
							}
							// this needed to be done if shader is changed.
							currentShader.setUniformi(texAtrib.name, texAtrib.unit);
						} else {
							atrib.bind(currentShader);
						}
					}
				}
				subMesh.getMesh().render(currentShader, subMesh.primitiveType);
			}

		}
		currentShader.end();
		currentShader = null;
		for (int i = 0, len = TextureAttribute.MAX_TEXTURE_UNITS; i < len; i++)
			lastTexture[i] = null;

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
