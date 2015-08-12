package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.utils.Array;

public class Scene {
	private Array<RenderableProvider> renderableProviders = new Array<RenderableProvider>();
	private Array<ModelInstance> instances = new Array<ModelInstance>();
	private Environment environment;
	private Camera camera;

	public Scene() {
		this(new Environment());
	}

	public Scene(Environment environment) {
		this.environment = environment;
	}

	public Scene add(BaseLight light) {
		environment.add(light);
		return this;
	}

	public Scene remove(BaseLight light) {
		environment.remove(light);
		return this;
	}

	public Scene add(RenderableProvider p) {
		renderableProviders.add(p);
		return this;
	}

	public Scene add(ModelInstance instance) {
		renderableProviders.add(instance);
		instances.add(instance);
		return this;
	}

	public Scene remove(RenderableProvider p) {
		renderableProviders.removeValue(p, true);
		return this;
	}

	public Scene remove(ModelInstance instance) {
		renderableProviders.removeValue(instance, true);
		instances.removeValue(instance, true);
		return this;
	}

	public Array<RenderableProvider> getRenderableProviders() {
		return renderableProviders;
	}

	public Array<ModelInstance> getInstances() {
		return instances;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public Scene setEnvironment(Environment environment) {
		this.environment = environment;
		return this;
	}

	public Scene setCamera(Camera camera) {
		this.camera = camera;
		return this;
	}

	public Camera getCamera() {
		return camera;
	}
}
