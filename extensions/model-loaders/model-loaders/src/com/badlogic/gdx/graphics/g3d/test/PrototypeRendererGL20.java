
package com.badlogic.gdx.graphics.g3d.test;

import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g3d.AnimatedModelInstance;
import com.badlogic.gdx.graphics.g3d.ModelRenderer;
import com.badlogic.gdx.graphics.g3d.StillModelInstance;
import com.badlogic.gdx.graphics.g3d.experimental.MaterialShaderHandler;
import com.badlogic.gdx.graphics.g3d.lights.LightManager;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.GpuSkinningAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.MaterialAttribute;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.AnimatedModel;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.test.PrototypeRendererGL20.DrawableManager.Drawable;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

//stuff that happens
//0. render begin
//1. frustum culling
//1.1 if animated, animation is solved and..
//1.2. all models and instances are put to one queue
//3. render ends
//for all models
//5. batching involving shaders, materials and texture should happen.(impossible to do perfect.)
//4. closest lights are calculated per model
//6  models are rendered
//7. tranparency

public class PrototypeRendererGL20 implements ModelRenderer {

	static final int SIZE = 256;// TODO better way
	final private Array<Model> modelQueue = new Array<Model>(false, SIZE);
	final private Array<StillModelInstance> modelInstances = new Array<StillModelInstance>(false, SIZE);

	final MaterialShaderHandler materialShaderHandler;
	private LightManager lightManager;
	private boolean drawing;
	final private Matrix3 normalMatrix = new Matrix3();
	public Camera cam;

	DrawableManager drawableManager = new DrawableManager();

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
		drawableManager.add(model, instance);
	}

	@Override
	public void draw (AnimatedModel model, AnimatedModelInstance instance) {
		if (cam != null) if (!cam.frustum.sphereInFrustum(instance.getSortCenter(), instance.getBoundingSphereRadius())) return;

		drawableManager.add(model, instance);
	}

	@Override
	public void end () {
		if (Gdx.graphics.isGL20Available())
			flush();
		else {
			// TODO fixed pipeline
		}
	}

	private ShaderProgram currentShader;
	final private TextureAttribute lastTexture[] = new TextureAttribute[TextureAttribute.MAX_TEXTURE_UNITS];

	private void flush () {

		// opaque is sorted front to back
		// transparent is sorted back to front
		drawableManager.drawables.sort(opaqueSorter);
		for (int i = drawableManager.drawables.size; --i >= 0;) {

			final Drawable drawable = drawableManager.drawables.get(i);

			final Vector3 center = drawable.sortCenter;
			lightManager.calculateLights(center.x, center.y, center.z);

			final Matrix4 modelMatrix = drawable.transform;
			normalMatrix.set(modelMatrix);

			if (drawable.isAnimated)
				((AnimatedModel)(drawable.model)).setAnimation(drawable.animation, drawable.animationTime, drawable.isLooping);

			final SubMesh subMeshes[] = drawable.model.getSubMeshes();

			boolean matrixChanged = true;
			for (int j = 0; j < subMeshes.length; j++) {

				final SubMesh subMesh = subMeshes[j];
				final Material material = drawable.materials.get(j);

				// bind new shader if material can't use old one
				final boolean shaderChanged = bindShader(material);

				if (shaderChanged || matrixChanged) {
					currentShader.setUniformMatrix("u_normalMatrix", normalMatrix, false);
					currentShader.setUniformMatrix("u_modelMatrix", modelMatrix, false);
					matrixChanged = false;
				}

				for (int k = 0, len = material.getNumberOfAttributes(); k < len; k++) {
					final MaterialAttribute atrib = material.getAttribute(k);

					// special case for textures. really important to batch these
					if (atrib instanceof TextureAttribute) {
						final TextureAttribute texAtrib = (TextureAttribute)atrib;
						if (!texAtrib.texturePortionEquals(lastTexture[texAtrib.unit])) {
							lastTexture[texAtrib.unit] = texAtrib;
							texAtrib.bind(currentShader);
						} else {
							// need to be done, shader textureAtribute name could be changed.
							currentShader.setUniformi(texAtrib.name, texAtrib.unit);
						}
					} else if (atrib instanceof GpuSkinningAttribute) {
						GpuSkinningAttribute gpuAttrib = (GpuSkinningAttribute) atrib;
						gpuAttrib.setModelMatrix(modelMatrix);
						gpuAttrib.bind(currentShader);
					} else {
						atrib.bind(currentShader);
					}
				}

				// finally render current submesh
				subMesh.getMesh().render(currentShader, subMesh.primitiveType);
			}
		}

		// if transparent queue is not empty enable blending(this force gpu to
		// flush and there is some time to sort)
		if (drawableManager.drawablesBlended.size > 0) renderBlended();

		// cleaning

		if (currentShader != null) {
			currentShader.end();
			currentShader = null;
		}
		for (int i = 0, len = TextureAttribute.MAX_TEXTURE_UNITS; i < len; i++)
			lastTexture[i] = null;
		// clear all queus

		drawing = false;

		drawableManager.clear();
	}

	/** @param material
	 * @return true if new shader was binded */
	boolean bindShader (Material material) {
		ShaderProgram shader = material.getShader();
		if (shader == currentShader) return false;

		currentShader = shader;
		currentShader.begin();

		lightManager.applyGlobalLights(currentShader);
		lightManager.applyLights(currentShader);
		currentShader.setUniformMatrix("u_projectionViewMatrix", cam.combined);
		currentShader.setUniformf("camPos", cam.position.x, cam.position.y, cam.position.z, 1.2f / cam.far);
		currentShader.setUniformf("camDir", cam.direction.x, cam.direction.y, cam.direction.z);
		return true;
	}

	public void dispose () {
		materialShaderHandler.dispose();
	}

	private void renderBlended () {

		Gdx.gl.glEnable(GL10.GL_BLEND);
		final Array<Drawable> transparentDrawables = drawableManager.drawablesBlended;
		transparentDrawables.sort();

		// find N nearest lights per model
		// draw all models from opaque queue

		int lastSrcBlend = -1;
		int lastDstBlend = -1;

		for (int i = 0, size = transparentDrawables.size; i < size; i++) {

			final Drawable drawable = transparentDrawables.get(i);

			final Vector3 center = drawable.sortCenter;
			lightManager.calculateLights(center.x, center.y, center.z);

			final Matrix4 modelMatrix = drawable.transform;
			normalMatrix.set(modelMatrix);

			if (drawable.isAnimated)
				((AnimatedModel)(drawable.model)).setAnimation(drawable.animation, drawable.animationTime, drawable.isLooping);

			final SubMesh subMeshes[] = drawable.model.getSubMeshes();

			boolean matrixChanged = true;
			for (int j = 0; j < subMeshes.length; j++) {

				final SubMesh subMesh = subMeshes[j];
				final Material material = drawable.materials.get(j);

				// bind new shader if material can't use old one
				final boolean shaderChanged = bindShader(material);

				if (shaderChanged || matrixChanged) {
					currentShader.setUniformMatrix("u_normalMatrix", normalMatrix, false);
					currentShader.setUniformMatrix("u_modelMatrix", modelMatrix, false);
					matrixChanged = false;
				}

				for (int k = 0, len = material.getNumberOfAttributes(); k < len; k++) {
					final MaterialAttribute atrib = material.getAttribute(k);

					// yet another instanceof. TODO is there any better way to do this? maybe stuffing this to material
					if (atrib instanceof BlendingAttribute) {
						final BlendingAttribute blending = (BlendingAttribute)atrib;
						if (blending.blendSrcFunc != lastSrcBlend || blending.blendDstFunc != lastDstBlend) {
							atrib.bind(currentShader);
							lastSrcBlend = blending.blendSrcFunc;
							lastDstBlend = blending.blendDstFunc;
						}
					} else if (atrib instanceof TextureAttribute) {
						// special case for textures. really important to batch these
						final TextureAttribute texAtrib = (TextureAttribute)atrib;
						if (!texAtrib.texturePortionEquals(lastTexture[texAtrib.unit])) {
							lastTexture[texAtrib.unit] = texAtrib;
							texAtrib.bind(currentShader);
						} else {
							// need to be done, shader textureAtribute name could be changed.
							currentShader.setUniformi(texAtrib.name, texAtrib.unit);
						}
					} else if (atrib instanceof GpuSkinningAttribute) {
						final GpuSkinningAttribute gpuAtrib = (GpuSkinningAttribute)atrib;
						gpuAtrib.setModelMatrix(modelMatrix);
						gpuAtrib.bind(currentShader);
					} else {
						atrib.bind(currentShader);
					}
				}
				// finally render current submesh
				subMesh.getMesh().render(currentShader, subMesh.primitiveType);
			}
		}
		Gdx.gl.glDisable(GL10.GL_BLEND);
	}

	class DrawableManager {
		Pool<Drawable> drawablePool = new Pool<Drawable>() {
			@Override
			protected Drawable newObject () {
				return new Drawable();
			}
		};
		Pool<Material> materialPool = new Pool<Material>() {
			@Override
			protected Material newObject () {
				return new Material();
			}
		};
		Array<Drawable> drawables = new Array<Drawable>();
		Array<Drawable> drawablesBlended = new Array<Drawable>();

		public void add (StillModel model, StillModelInstance instance) {
			Drawable drawable = drawablePool.obtain();
			drawable.set(model, instance);

			if (drawable.blending)
				drawablesBlended.add(drawable);
			else
				drawables.add(drawable);
		}

		public void add (AnimatedModel model, AnimatedModelInstance instance) {
			Drawable drawable = drawablePool.obtain();
			drawable.set(model, instance);

			if (drawable.blending)
				drawablesBlended.add(drawable);
			else
				drawables.add(drawable);
		}

		public void clear () {
			clear(drawables);
			clear(drawablesBlended);
		}

		private void clear (Array<Drawable> drawables) {
			while (drawables.size > 0) {
				final Drawable drawable = drawables.pop();

				// return all materials and attribuets to the pools
				while (drawable.materials.size > 0) {
					final Material material = drawable.materials.pop();

					for(int i=0; i<material.getNumberOfAttributes(); i++){
						MaterialAttribute attribute = material.getAttribute(i);
						attribute.free();
					}
					material.clearAttributes();
					
					material.resetShader();
					materialPool.free(material);
				}
				// reset the drawable and return it to the drawable pool
				drawablePool.free(drawable);
			}
		}

		/** A drawable is a copy of the state of the model and instance passed to either
		 * {@link PrototypeRendererGL20#draw(AnimatedModel, AnimatedModelInstance)} or
		 * {@link PrototypeRendererGL20#draw(StillModel, StillModelInstance)}. It is used in {@link PrototypeRendererGL20#flush()}
		 * to do material and depth sorting for blending without having to deal with the API client changing any attributes of a
		 * model or instance in between draw calls.
		 * @author mzechner */
		class Drawable implements Comparable<Drawable> {
			private static final int PRIORITY_DISCRETE_STEPS = 256;
			Model model;
			final Matrix4 transform = new Matrix4();
			final Vector3 sortCenter = new Vector3();
			final Array<Material> materials = new Array<Material>(2);
			boolean isAnimated;
			String animation;
			float animationTime;
			boolean isLooping;
			boolean blending;
			int distance;
			int firstShaderHash;
			int modelHash;

			public void set (StillModel model, StillModelInstance instance) {
				setCommon(model, instance);
				isAnimated = false;
			}

			public void set (AnimatedModel model, AnimatedModelInstance instance) {
				setCommon(model, instance);
				isAnimated = true;
				animation = instance.getAnimation();
				animationTime = instance.getAnimationTime();
				isLooping = instance.isLooping();
			}

			private void setCommon (Model model, StillModelInstance instance) {
				this.model = model;
				modelHash = model.hashCode();
				// transform.set(instance.getTransform().val);
				System.arraycopy(instance.getTransform().val, 0, transform.val, 0, 16);

				sortCenter.set(instance.getSortCenter());
				distance = (int)(PRIORITY_DISCRETE_STEPS * sortCenter.dst(cam.position));
				if (instance.getMaterials() != null) {
					for (Material material : instance.getMaterials()) {
						/* TODO: Readd method in material
						if (material.getShader() == null) material.generateShader(materialShaderHandler);
						*/
						
						final Material copy = materialPool.obtain();
						copy.setPooled(material);
						materials.add(copy);
					}
				} else {
					for (SubMesh subMesh : model.getSubMeshes()) {
						final Material material = subMesh.material;
						/* TODO: Readd method in material
						if (material.getShader() == null) material.generateShader(materialShaderHandler);
						*/
						
						final Material copy = materialPool.obtain();
						copy.setPooled(material);
						materials.add(copy);

					}
				}
				blending = false;
				for (Material mat : materials) {
					if (mat.isNeedBlending()) {
						blending = true;
						break;
					}
				}
				if (materials.size > 0) firstShaderHash = materials.get(0).getShader().hashCode();

			}

			@Override
			public int compareTo (Drawable other) {
				return other.distance - this.distance;
			}
		}
	}

	public static final Comparator<Drawable> opaqueSorter = new Comparator<Drawable>() {

		public int compare (Drawable a, Drawable b) {
			if (a.firstShaderHash != b.firstShaderHash) return b.firstShaderHash - a.firstShaderHash;

			if (a.modelHash != b.modelHash) return b.modelHash - a.modelHash;

			return b.distance - a.distance;
		}

	};

}
