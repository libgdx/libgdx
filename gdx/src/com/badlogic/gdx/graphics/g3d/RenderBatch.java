package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.old.model.Model;
import com.badlogic.gdx.graphics.g3d.old.model.SubMesh;
import com.badlogic.gdx.graphics.g3d.test.InterimModel;
import com.badlogic.gdx.graphics.g3d.test.Light;
import com.badlogic.gdx.graphics.g3d.test.NewModel;
import com.badlogic.gdx.graphics.g3d.utils.ExclusiveTextures;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class RenderBatch {
	protected RenderContext context;
	protected RenderBatchListener listener;
	protected Camera camera;
	// TODO: perhaps its better to use a sorted list?
	protected final Array<RenderInstance> instances = new Array<RenderInstance>();
	
	/** Construct a BaseRenderBatch with the specified listener */
	public RenderBatch(RenderBatchListener listener, ExclusiveTextures textures) {
		this.listener = listener;
		this.context = new RenderContext(textures);
	}
	
	/** Construct a BaseRenderBatch with the default implementation and the specified texture range */
	public RenderBatch(ExclusiveTextures textures) {
		this(new RenderBatchAdapter(), textures);
	}
	
	/** Construct a BaseRenderBatch with the default implementation */
	public RenderBatch() {
		this((ExclusiveTextures)null);
	}

	public void begin (Camera cam) {
		this.camera = cam;
	}

	public void end () {
		instances.sort(listener);
		context.begin();
		Shader currentShader = null;
		for (int i = 0; i < instances.size; i++) {
			final RenderInstance instance = instances.get(i);
			if (currentShader != instance.shader) {
				if (currentShader != null)
					currentShader.end();
				currentShader = instance.shader;
				currentShader.begin(camera, context);
			}
			currentShader.render(instance);
		}
		if (currentShader != null)
			currentShader.end();
		context.end();
		RenderInstance.pool.freeAll(instances);
		instances.clear();
		camera = null;
	}

	/** Add an instance to render, the shader property of the instance will be overwritten by this method */
	protected void addInstance(final RenderInstance instance, final Shader shader) {
		instance.shader = listener.getShader(instance, shader);
		instance.renderable.mesh.setAutoBind(false);
		instances.add(instance);
	}
	
	public void addModelPart(final Renderable renderable, final Matrix4 transform, final float distance, final Light[] lights, final Shader shader) {
		addInstance(RenderInstance.pool.obtain(renderable, transform, distance, lights, null), shader);
	}
	
	public void addModel(final NewModel model, final Matrix4 transform) {
		addModel(model, transform, null, null);
	}
	
	public void addModel(final NewModel model, final Matrix4 transform, final Light[] lights) {
		addModel(model, transform, lights, null);
	}
	
	final Vector3 posTmp = new Vector3();
	public void addModel(final NewModel model, final Matrix4 transform, final Light[] lights, final Shader shader) {
		transform.getTranslation(posTmp);
		posTmp.sub(camera.position);
		float dist = posTmp.len();
		float dot = dist > 0? posTmp.scl(1 / dist).dot(camera.direction): 0;
		if (dot < 0)
			dist = -dist;
		Iterable<Renderable> parts = model.getParts(dist);
		if (instances != null)
			for (Renderable part : parts)
				addModelPart(part, transform, dist, lights, null);
	}
}
