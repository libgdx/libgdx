package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.test.Light;
import com.badlogic.gdx.graphics.g3d.utils.DefaultRenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

public class ModelBatch implements Disposable {
	protected Camera camera;
	protected final Pool<Renderable> renderablesPool = new Pool<Renderable>() {
		@Override
		protected Renderable newObject () {
			return new Renderable();
		}
	};
	/** list of Renderables to be rendered in the current batch **/
	protected final Array<Renderable> renderables = new Array<Renderable>();
	/** list of Renderables that can be put back into the pool **/
	protected final Array<Renderable> reuseableRenderables = new Array<Renderable>();
	/** the {@link RenderContext} **/
	protected final RenderContext context;
	/** the {@link ShaderProvider}, provides {@link Shader} instances for Renderables **/
	protected final ShaderProvider shaderProvider;
	/** the {@link RenderableSorter} **/
	protected final RenderableSorter sorter;
	
	/** Construct a BaseRenderBatch with the specified listener */
	public ModelBatch(RenderContext context, ShaderProvider shaderProvider, RenderableSorter sorter) {
		this.context = context;
		this.shaderProvider = shaderProvider;
		this.sorter = sorter;
	}
	
	/** Construct a BaseRenderBatch with the default implementation */
	public ModelBatch() {
		this(new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN)),
			  new DefaultShaderProvider(),
			  new DefaultRenderableSorter());
	}

	public void begin (Camera cam) {
		this.camera = cam;
	}

	public void end () {
		sorter.sort(renderables);
		context.begin();
		Shader currentShader = null;
		for (int i = 0; i < renderables.size; i++) {
			final Renderable renderable = renderables.get(i);
			renderable.shader = shaderProvider.getShader(renderable);
			if (currentShader != renderable.shader) {
				if (currentShader != null)
					currentShader.end();
				currentShader = renderable.shader;
				currentShader.begin(camera, context);
			}
			currentShader.render(renderable);
		}
		if (currentShader != null)
			currentShader.end();
		context.end();
		renderablesPool.freeAll(reuseableRenderables);
		reuseableRenderables.clear();
		renderables.clear();
		camera = null;
	}

	/** Add an instance to render */
	protected void render(final Renderable renderable) {
		renderable.shader = shaderProvider.getShader(renderable);
		renderable.mesh.setAutoBind(false);
		renderables.add(renderable);
	}
	
	public void render(final Model model, final Matrix4 transform) {
		render(model, transform, null, null);
	}
	
	public void render(final Model model, final Matrix4 transform, final Light[] lights) {
		render(model, transform, lights, null);
	}
	
	public void render(final Model model, final Matrix4 transform, final Light[] lights, final Shader shader) {
		int offset = renderables.size;
		model.getRenderables(renderables, renderablesPool);
		for (int i = offset; i < renderables.size; i++) {
			Renderable renderable = renderables.get(i);
			renderable.lights = lights;
			renderable.shader = shader;
			renderable.shader = shader;
//			renderable.transform; FIXME multiply transform!
			reuseableRenderables.add(renderable);
		}
	}

	@Override
	public void dispose () {
		shaderProvider.dispose();
	}
}
