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

package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.utils.Array;

/**
 * The scene contains all the renderable providers,
 * the camera and the environment
 * It allows to transfert all the rendering context between objects
 * @author realitix
 */
public class Scene {
	/** Array of all the renderable providers */
	private Array<RenderableProvider> renderableProviders = new Array<RenderableProvider>();
	/** Subset of renderable providers composed of model instances */
	private Array<ModelInstance> instances = new Array<ModelInstance>();
	/** Environment */
	private Environment environment;
	/** Camera */
	private Camera camera;

	/**
	 * Construct a scene with default environment
	 */
	public Scene() {
		this(new Environment());
	}

	/**
	 * Construct a scene with custom environment
	 * @param environment Custom environment
	 */
	public Scene(Environment environment) {
		this.environment = environment;
	}

	/**
	 * Add light in the environment
	 * @param light Light to add
	 * @return Scene
	 */
	public Scene add(BaseLight light) {
		environment.add(light);
		return this;
	}

	/**
	 * Remove light from environment
	 * @param light Light to remove
	 * @return Scene
	 */
	public Scene remove(BaseLight light) {
		environment.remove(light);
		return this;
	}

	/**
	 * Add renderable provider into the scene
	 * @param p Renderable provider
	 * @return Scene
	 */
	public Scene add(RenderableProvider p) {
		renderableProviders.add(p);
		return this;
	}

	/**
	 * Add model instance to the scene
	 * @param instance Model instance
	 * @return Scene
	 */
	public Scene add(ModelInstance instance) {
		renderableProviders.add(instance);
		instances.add(instance);
		return this;
	}

	/**
	 * Remove renderable provider from scene
	 * @param p Renderable provider
	 * @return Scene
	 */
	public Scene remove(RenderableProvider p) {
		renderableProviders.removeValue(p, true);
		return this;
	}

	/**
	 * Remove model instance from scene
	 * @param instance Model instance
	 * @return Scene
	 */
	public Scene remove(ModelInstance instance) {
		renderableProviders.removeValue(instance, true);
		instances.removeValue(instance, true);
		return this;
	}

	/**
	 * Return all the renderable providers of the scene
	 * @return Array<RenderableProvider>
	 */
	public Array<RenderableProvider> getRenderableProviders() {
		return renderableProviders;
	}

	/**
	 * Return all the model instances of the scene
	 * @return Array<ModelInstance>
	 */
	public Array<ModelInstance> getInstances() {
		return instances;
	}

	/**
	 * Return environment
	 * @return Environment
	 */
	public Environment getEnvironment() {
		return environment;
	}

	/**
	 * Set custom environment
	 * @param environment
	 * @return Scene
	 */
	public Scene setEnvironment(Environment environment) {
		this.environment = environment;
		return this;
	}

	/**
	 * Set camera of the scene
	 * @param camera Camera
	 * @return Scene
	 */
	public Scene setCamera(Camera camera) {
		this.camera = camera;
		return this;
	}

	/**
	 * Get the camera
	 * @return Scene
	 */
	public Camera getCamera() {
		return camera;
	}
}
