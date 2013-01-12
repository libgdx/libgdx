package com.badlogic.gdx.graphics.g3d.xoppa;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.graphics.g3d.xoppa.utils.ExclusiveTextures;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class RenderBatch {
	protected RenderContext context;
	protected RenderBatchListener listener;
	protected Camera camera;
	protected final Array<RenderInstance> instances = new Array<RenderInstance>();
	protected final Pool<RenderInstance> instancePool = new Pool<RenderInstance>() {
		@Override
		protected RenderInstance newObject () {
			return new RenderInstance();
		}
	};
	
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
		instancePool.freeAll(instances);
		instances.clear();
		camera = null;
	}

	/** Add an instance to render, the shader property of the instance will be overwritten by this method */
	public void addInstance(final RenderInstance instance, final Shader shader) {
		instance.shader = listener.getShader(instance, shader);
		instance.mesh.setAutoBind(false);
		instances.add(instance);
	}

	/** Add an instance to render, the shader property of the instance will be overwritten by this method */
	public void addInstance(final RenderInstance instance) {
		addInstance(instance, null);
	}
	
	// Helper methods to convert meshes and models to render instances:
	
	public void addMesh (final SubMesh mesh, final Matrix4 transform) {
		transform.getTranslation(Vector3.tmp);
		float dist = Vector3.tmp2.set(Vector3.tmp.x - camera.position.x, Vector3.tmp.y - camera.position.y , Vector3.tmp.z - camera.position.z).len();
		if (Vector3.tmp2.div(dist).dot(camera.direction) < 0)
			dist = -dist;
		addMesh(mesh, transform, dist);
	}
	
	public void addMesh (final SubMesh mesh, final Matrix4 transform, float distance) {
		final RenderInstance instance = instancePool.obtain();
		instance.distance = distance;
		instance.material = mesh.material;
		instance.mesh = mesh.mesh;
		instance.meshPartOffset = 0;
		instance.meshPartSize = mesh.mesh.getMaxIndices() > 0 ? mesh.mesh.getNumIndices() : mesh.mesh.getNumVertices();
		instance.primitiveType = mesh.primitiveType;
		instance.transform = transform;
		instance.shader = null;
		addInstance(instance);
	}

	public void addModel(final Model model, final Matrix4 transform) {
		SubMesh[] meshes = model.getSubMeshes();
		for (int i = 0; i < meshes.length; i++)
			addMesh(meshes[i], transform);
	}
	
	public void addModel(final Model model, final Matrix4 transform, float distance) {
		SubMesh[] meshes = model.getSubMeshes();
		for (int i = 0; i < meshes.length; i++)
			addMesh(meshes[i], transform, distance);		
	}
}
