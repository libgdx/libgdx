package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.lights.Lights;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultRenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

/** Batches {@link Renderable} instances, fetches {@link Shader}s for them, sorts them and then renders them.
 * Fetching the shaders is done using a {@link ShaderProvider}, which defaults to {@link DefaultShaderProvider}.
 * Sorting the renderables is done using a {@link RenderableSorter}, which default to {@link DefaultRenderableSorter}. 
 * 
 * The OpenGL context between the {@link #begin(Camera)} and {@link #end()} call is maintained by the {@link RenderContext}.
 * 
 * To provide multiple {@link Renderable}s at once a {@link RenderableProvider} can be used, e.g. a {@link ModelInstance}. 
 * 
 * @author xoppa, badlogic */
public class ModelBatch implements Disposable {
	protected Camera camera;
	protected final Pool<Renderable> renderablesPool = new Pool<Renderable>() {
		@Override
		protected Renderable newObject () {
			return new Renderable();
		}

		@Override
		public Renderable obtain () {
			Renderable renderable = super.obtain();
			renderable.lights = null;
			renderable.material = null;
			renderable.mesh = null;
			renderable.shader = null;
			return renderable;
		}
	};
	/** list of Renderables to be rendered in the current batch **/
	protected final Array<Renderable> renderables = new Array<Renderable>();
	/** list of Renderables that can be put back into the pool **/
	protected final Array<Renderable> reuseableRenderables = new Array<Renderable>();
	/** the {@link RenderContext} **/
	protected final RenderContext context;
	private final boolean ownContext;
	/** the {@link ShaderProvider}, provides {@link Shader} instances for Renderables **/
	protected final ShaderProvider shaderProvider;
	/** the {@link RenderableSorter} **/
	protected final RenderableSorter sorter;
	
	private ModelBatch(RenderContext context, boolean ownContext, ShaderProvider shaderProvider, RenderableSorter sorter) {
		this.ownContext = ownContext;
		this.context = context;
		this.shaderProvider = shaderProvider;
		this.sorter = sorter;
	}
	
	/** Construct a ModelBatch, using this constructor makes you responsible for calling context.begin() and contact.end() yourself. 
	 * @param context The {@link RenderContext} to use.
	 * @param shaderProvider The {@link ShaderProvider} to use.
	 * @param sorter The {@link RenderableSorter} to use. */
	public ModelBatch(RenderContext context, ShaderProvider shaderProvider, RenderableSorter sorter) {
		this(context, false, shaderProvider, sorter);
	}
	
	/** Construct a ModelBatch, using this constructor makes you responsible for calling context.begin() and contact.end() yourself.
	 * @param shaderProvider The {@link ShaderProvider} to use. */
	public ModelBatch(ShaderProvider shaderProvider) {
		this(new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.WEIGHTED, 1)),
				true,
				shaderProvider,
				new DefaultRenderableSorter());
	}
	
	/** Construct a ModelBatch with the default implementation and the specified ubershader. See {@link DefaultShader} for
	 * more information about using a custom ubershader. Requires OpenGL ES 2.0.
	 * @param vertexShader The {@link FileHandle} of the vertex shader to use.
	 * @param fragmentShader The {@link FileHandle} of the fragment shader to use. */
	public ModelBatch(final FileHandle vertexShader, final FileHandle fragmentShader) {
		this(new DefaultShaderProvider(vertexShader, fragmentShader));
	}
	
	/** Construct a ModelBatch with the default implementation and the specified ubershader. See {@link DefaultShader} for
	 * more information about using a custom ubershader. Requires OpenGL ES 2.0.
	 * @param vertexShader The vertex shader to use.
	 * @param fragmentShader The fragment shader to use. */
	public ModelBatch(final String vertexShader, final String fragmentShader) {
		this(new DefaultShaderProvider(vertexShader, fragmentShader));
	}
	
	/** Construct a ModelBatch with the default implementation */
	public ModelBatch() {
		this(new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN, 1)),
				true,
				new DefaultShaderProvider(),
				new DefaultRenderableSorter());
	}

	/** Start rendering one or more {@link Renderable}s. Use one of the render() methods to provide the renderables. 
	 * Must be followed by a call to {@link #end()}. The OpenGL context must not be altered between 
	 * {@link #begin(Camera)} and {@link #end()}.
	 * @param cam The {@link Camera} to be used when rendering and sorting. */
	public void begin (final Camera cam) {
		if (camera != null)
			throw new GdxRuntimeException("Call end() first.");
		camera = cam;
		if (ownContext)
			context.begin();
	}
	
	/** Change the camera in between {@link #begin(Camera)} and {@link #end()}. This causes the batch to be flushed.
	 * Can only be called after the call to {@link #begin(Camera)} and before the call to {@link #end()}.
	 * @param cam The new camera to use. */
	public void setCamera(final Camera cam) {
		if (camera == null)
			throw new GdxRuntimeException("Call begin() first.");
		if (renderables.size > 0)
			flush();
		camera = cam;
	}
	
	/** Flushes the batch, causing all {@link Renderable}s in the batch to be rendered. Can only be called after the
	 * call to {@link #begin(Camera)} and before the call to {@link #end()}. */
	public void flush() {
		sorter.sort(camera, renderables);
		Shader currentShader = null;
		for (int i = 0; i < renderables.size; i++) {
			final Renderable renderable = renderables.get(i);
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
		renderablesPool.freeAll(reuseableRenderables);
		reuseableRenderables.clear();
		renderables.clear();
	}

	/** End rendering one or more {@link Renderable}s. Must be called after a call to {@link #begin(Camera)}.
	 * This will flush the batch, causing any renderables provided using one of the render() methods to be rendered.
	 * After a call to this method the OpenGL context can be altered again. */
	public void end () {
		flush();
		if (ownContext)
			context.end();
		camera = null;
	}

	/** Add a single {@link Renderable} to the batch. The {@link ShaderProvider} will be used to fetch a suitable
	 * {@link Shader}. Can only be called after a call to {@link #begin(Camera)} and before a call to {@link #end()}.
	 * @param renderable The {@link Renderable} to be added. */
	public void render(final Renderable renderable) {
		renderable.shader = shaderProvider.getShader(renderable);
		renderable.mesh.setAutoBind(false);
		renderables.add(renderable);
	}
		
	/** Calls {@link RenderableProvider#getRenderables(Array, Pool)} and adds all returned {@link Renderable} 
	 * instances to the current batch to be rendered. Can only be called after a call to {@link #begin(Camera)}
	 * and before a call to {@link #end()}.
	 * @param renderableProvider the renderable provider */
	public void render(final RenderableProvider renderableProvider) {
		render(renderableProvider, null, null);
	}
	
	/** Calls {@link RenderableProvider#getRenderables(Array, Pool)} and adds all returned {@link Renderable}
	 * instances to the current batch to be rendered. Can only be called after a call to {@link #begin(Camera)}
	 * and before a call to {@link #end()}.
	 * @param renderableProviders one or more renderable providers */
	public <T extends RenderableProvider> void render(final Iterable<T> renderableProviders) {
		render(renderableProviders, null, null);
	}
	
	/** Calls {@link RenderableProvider#getRenderables(Array, Pool)} and adds all returned {@link Renderable}
	 * instances to the current batch to be rendered. Any lights set on the returned renderables will be replaced
	 * with the given lights. Can only be called after a call to {@link #begin(Camera)} and before a call to {@link #end()}.
	 * @param renderableProvider the renderable provider
	 * @param lights the lights to use for the renderables */
	public void render(final RenderableProvider renderableProvider, final Lights lights) {
		render(renderableProvider, lights, null);
	}
	
	/** Calls {@link RenderableProvider#getRenderables(Array, Pool)} and adds all returned {@link Renderable}
	 * instances to the current batch to be rendered. Any lights set on the returned renderables will be replaced
	 * with the given lights. Can only be called after a call to {@link #begin(Camera)} and before a call to {@link #end()}.
	 * @param renderableProviders one or more renderable providers
	 * @param lights the lights to use for the renderables */
	public <T extends RenderableProvider> void render(final Iterable<T> renderableProviders, final Lights lights) {
		render(renderableProviders, lights, null);
	}
	
	/** Calls {@link RenderableProvider#getRenderables(Array, Pool)} and adds all returned {@link Renderable}
	 * instances to the current batch to be rendered. Any shaders set on the returned renderables will be replaced
	 * with the given {@link Shader}. Can only be called after a call to {@link #begin(Camera)} and before a call to {@link #end()}.
	 * @param renderableProvider the renderable provider
	 * @param shader the shader to use for the renderables */
	public void render(final RenderableProvider renderableProvider, final Shader shader) {
		render(renderableProvider, null, shader);
	}
	
	/** Calls {@link RenderableProvider#getRenderables(Array, Pool)} and adds all returned {@link Renderable}
	 * instances to the current batch to be rendered. Any shaders set on the returned renderables will be replaced
	 * with the given {@link Shader}. Can only be called after a call to {@link #begin(Camera)} and before a call to {@link #end()}.
	 * @param renderableProviders one or more renderable providers
	 * @param shader the shader to use for the renderables */
	public <T extends RenderableProvider> void render(final Iterable<T> renderableProviders, final Shader shader) {
		render(renderableProviders, null, shader);
	}

	/** Calls {@link RenderableProvider#getRenderables(Array, Pool)} and adds all returned {@link Renderable}
	 * instances to the current batch to be rendered. Any lights set on the returned renderables will be replaced
	 * with the given lights. Any shaders set on the returned renderables will be replaced with the given {@link Shader}. 
	 * Can only be called after a call to {@link #begin(Camera)} and before a call to {@link #end()}.
	 * @param renderableProvider the renderable provider
	 * @param lights the lights to use for the renderables
	 * @param shader the shader to use for the renderables */
	public void render(final RenderableProvider renderableProvider, final Lights lights, final Shader shader) {
		final int offset = renderables.size;
		renderableProvider.getRenderables(renderables, renderablesPool);
		for (int i = offset; i < renderables.size; i++) {
			Renderable renderable = renderables.get(i);
			renderable.lights = lights;
			renderable.shader = shader;
			renderable.shader = shaderProvider.getShader(renderable);
			reuseableRenderables.add(renderable);
		}
	}
	
	/** Calls {@link RenderableProvider#getRenderables(Array, Pool)} and adds all returned {@link Renderable}
	 * instances to the current batch to be rendered. Any lights set on the returned renderables will be replaced
	 * with the given lights. Any shaders set on the returned renderables will be replaced with the given {@link Shader}.
	 * Can only be called after a call to {@link #begin(Camera)} and before a call to {@link #end()}.
	 * @param renderableProviders one or more renderable providers
	 * @param lights the lights to use for the renderables
	 * @param shader the shader to use for the renderables */
	public <T extends RenderableProvider> void render(final Iterable<T> renderableProviders, final Lights lights, final Shader shader) {
		for (final RenderableProvider renderableProvider : renderableProviders)
			render(renderableProvider, lights, shader);
	}

	@Override
	public void dispose () {
		shaderProvider.dispose();
	}
}
