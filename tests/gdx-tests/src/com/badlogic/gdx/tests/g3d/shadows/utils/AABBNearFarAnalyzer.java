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

package com.badlogic.gdx.tests.g3d.shadows.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Sphere;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/** Compute near and far plane based on renderable providers passed in constructor. Renderable providers array should contains only
 * renderable in camera frustum.
 * @author realitix */
public class AABBNearFarAnalyzer implements NearFarAnalyzer {
	/** Near and far initialization before computation. You should put the same values as the main camera */
	public static float CAMERA_NEAR = 0.1f;
	public static float CAMERA_FAR = 1000;

	// @TODO Merge renderable pools (ModelBatch)
	protected static class RenderablePool extends Pool<Renderable> {
		protected Array<Renderable> obtained = new Array<Renderable>();

		@Override
		protected Renderable newObject () {
			return new Renderable();
		}

		@Override
		public Renderable obtain () {
			Renderable renderable = super.obtain();
			renderable.environment = null;
			renderable.material = null;
			renderable.meshPart.set("", null, 0, 0, 0);
			renderable.shader = null;
			obtained.add(renderable);
			return renderable;
		}

		public void flush () {
			super.freeAll(obtained);
			obtained.clear();
		}
	}

	protected final RenderablePool renderablesPool = new RenderablePool();
	/** list of Renderables to be rendered in the current batch **/
	protected final Array<Renderable> renderables = new Array<Renderable>();

	/** Renderable providers array */
	protected Array<RenderableProvider> renderableProviders;

	/** Objects used for computation */
	protected BoundingBox bb1 = new BoundingBox();
	protected Sphere sphere = new Sphere(new Vector3(), 0);
	protected Vector3 tmpV = new Vector3();

	/** Create new AABBNearFarAnalyzer.
	 * @param renderableProviders Array of renderable providers */
	public AABBNearFarAnalyzer (Array<RenderableProvider> renderableProviders) {
		this.renderableProviders = renderableProviders;
	}

	@Override
	public void analyze (BaseLight light, Camera camera) {
		getRenderables();
		prepareCamera(camera);

		bb1.inf();
		for (Renderable renderable : renderables) {
			// Center
			renderable.worldTransform.getTranslation(tmpV);
			tmpV.add(renderable.meshPart.center);
			sphere.center.set(tmpV);

			// Radius
			sphere.radius = renderable.meshPart.radius;

			if (camera.frustum.sphereInFrustum(sphere)) {
				bb1.ext(sphere);
			}
		}

		computeResult(bb1, camera);
		renderables.clear();
	}

	protected void getRenderables () {
		for (RenderableProvider renderableProvider : renderableProviders) {
			renderableProvider.getRenderables(renderables, renderablesPool);
		}
	}

	/** Initialize camera before computation.
	 * @param camera Camera to compute. */
	protected void prepareCamera (Camera camera) {
		camera.near = AABBNearFarAnalyzer.CAMERA_NEAR;
		camera.far = AABBNearFarAnalyzer.CAMERA_FAR;
		camera.update();
	}

	/** Compute final result.
	 * @param bb BoundingBox encompassing instances
	 * @param camera Camera to compute */
	protected void computeResult (BoundingBox bb, Camera camera) {
		bb1.getBoundingSphere(sphere);
		float distance = sphere.center.dst(camera.position);
		float near = distance - sphere.radius;
		float far = distance + sphere.radius;

		if (near <= 0) near = CAMERA_NEAR;
		if (far <= 0) far = CAMERA_FAR;

		camera.near = near;
		camera.far = far;
		camera.update();
	}
}
