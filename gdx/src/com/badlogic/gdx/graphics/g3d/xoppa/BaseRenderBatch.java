package com.badlogic.gdx.graphics.g3d.xoppa;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class BaseRenderBatch implements RenderBatch {
	protected BatchRenderer renderer;
	protected final Array<RenderInstance> instances = new Array<RenderInstance>();
	
	public BaseRenderBatch(BatchRenderer renderer) {
		this.renderer = renderer;
	}
	
	protected final Pool<RenderInstance> instancePool = new Pool<RenderInstance>() {
		@Override
		protected RenderInstance newObject () {
			return new RenderInstance();
		}
	};
	
	protected Camera camera;

	@Override
	public void begin (Camera cam) {
		this.camera = cam;
	}

	@Override
	public void end () {
		instances.sort(renderer);
		renderer.render(camera, instances);
		instancePool.freeAll(instances);
		instances.clear();
		camera = null;
	}

	@Override
	public void addMesh (final SubMesh mesh, final Matrix4 transform) {
		transform.getTranslation(Vector3.tmp);
		float dist = Vector3.tmp2.set(Vector3.tmp.x - camera.position.x, Vector3.tmp.y - camera.position.y , Vector3.tmp.z - camera.position.z).len();
		if (Vector3.tmp2.div(dist).dot(camera.direction) < 0)
			dist = -dist;
		addMesh(mesh, transform, dist);
	}
	
	@Override
	public void addMesh (final SubMesh mesh, final Matrix4 transform, float distance) {
		final RenderInstance instance = instancePool.obtain();
		instance.distance = distance;
		instance.material = mesh.material;
		instance.mesh = mesh.mesh;
		instance.primitiveType = mesh.primitiveType;
		instance.transform = transform;
		instance.shader = null;
		addInstance(instance);
	}

	@Override
	public void addModel(final Model model, final Matrix4 transform) {
		SubMesh[] meshes = model.getSubMeshes();
		for (int i = 0; i < meshes.length; i++)
			addMesh(meshes[i], transform);
	}
	
	@Override
	public void addModel(final Model model, final Matrix4 transform, float distance) {
		SubMesh[] meshes = model.getSubMeshes();
		for (int i = 0; i < meshes.length; i++)
			addMesh(meshes[i], transform, distance);		
	}
	
	public void addInstance(final RenderInstance instance) {
		if (instance.shader == null)
			instance.shader =  renderer.getShader(instance);
		instance.mesh.setAutoBind(false);
		instances.add(instance);
	}
}
