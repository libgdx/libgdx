package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.test.Light;
import com.badlogic.gdx.graphics.g3d.test.NewModel;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.g3d.utils.RenderInstancePool;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class ModelBatch {
	protected RenderContext context;
	protected RenderBatchListener listener;
	protected Camera camera;
	protected final Array<RenderInstance> instances = new Array<RenderInstance>();	
	protected final Pool<Renderable> renderablesPool = new Pool<Renderable>() {
		@Override
		protected Renderable newObject () {
			return new Renderable();
		}
	};
	private final RenderInstancePool renderInstancePool = new RenderInstancePool();
	/** used in {@link #addModel(Model, Matrix4, Light[], Shader)} to retrieve Renderables **/
	protected final Array<Renderable> renderables = new Array<Renderable>();
	
	/** Construct a BaseRenderBatch with the specified listener */
	public ModelBatch(RenderBatchListener listener, DefaultTextureBinder textures) {
		this.listener = listener;
		this.context = new RenderContext(textures);
	}
	
	/** Construct a BaseRenderBatch with the default implementation and the specified texture range */
	public ModelBatch(DefaultTextureBinder textures) {
		this(new RenderBatchAdapter(), textures);
	}
	
	/** Construct a BaseRenderBatch with the default implementation */
	public ModelBatch() {
		this(new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN));
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
		renderablesPool.freeAll(renderables);
		renderables.clear();
		renderInstancePool.freeAll(instances);
		instances.clear();
		camera = null;
	}

	/** Add an instance to render, the shader property of the instance will be overwritten by this method */
	protected void addInstance(final RenderInstance instance, final Shader shader) {
		instance.shader = listener.getShader(instance, shader);
		instance.renderable.mesh.setAutoBind(false);
		instances.add(instance);
	}
	
	public void addRenderable(final Renderable renderable, final Matrix4 transform, final float distance, final Light[] lights, final Shader shader) {
		addInstance(renderInstancePool.obtain(renderable, transform, distance, lights, null), shader);
	}
	
	public void addModel(final Model model, final Matrix4 transform) {
		addModel(model, transform, null, null);
	}
	
	public void addModel(final Model model, final Matrix4 transform, final Light[] lights) {
		addModel(model, transform, lights, null);
	}
	
	public void addModel(final Model model, final Matrix4 transform, final Light[] lights, final Shader shader) {
		int offset = renderables.size;
		model.getRenderables(renderables, renderablesPool);
		for (int i = offset; i < renderables.size; i++) {
			addRenderable(renderables.get(i), transform, 0, lights, null);
		}
	}
}
