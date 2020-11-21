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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultRenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FlushablePool;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

/** Batches {@link Renderable} instances, fetches {@link Shader}s for them, sorts them and then renders them. Fetching the shaders
 * is done using a {@link ShaderProvider}, which defaults to {@link DefaultShaderProvider}. Sorting the renderables is done using
 * a {@link RenderableSorter}, which default to {@link DefaultRenderableSorter}.
 * 
 * The OpenGL context between the {@link #begin(Camera)} and {@link #end()} call is maintained by the {@link RenderContext}.
 * 
 * To provide multiple {@link Renderable}s at once a {@link RenderableProvider} can be used, e.g. a {@link ModelInstance}.
 * 
 * @author xoppa, badlogic */
public class ModelBatch implements Disposable {
	protected static class RenderablePool extends FlushablePool<Renderable> {
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
			renderable.userData = null;
			return renderable;
		}
	}

	protected Camera camera;
	protected final RenderablePool renderablesPool = new RenderablePool();
	/** list of Renderables to be rendered in the current batch **/
	protected final Array<Renderable> renderables = new Array<Renderable>();
	/** the {@link RenderContext} **/
	protected final RenderContext context;
	private final boolean ownContext;
	/** the {@link ShaderProvider}, provides {@link Shader} instances for Renderables **/
	protected final ShaderProvider shaderProvider;
	/** the {@link RenderableSorter} **/
	protected final RenderableSorter sorter;

	/** Construct a ModelBatch, using this constructor makes you responsible for calling context.begin() and context.end() yourself.
	 * @param context The {@link RenderContext} to use.
	 * @param shaderProvider The {@link ShaderProvider} to use, will be disposed when this ModelBatch is disposed.
	 * @param sorter The {@link RenderableSorter} to use. */
	public ModelBatch (final RenderContext context, final ShaderProvider shaderProvider, final RenderableSorter sorter) {
		this.sorter = (sorter == null) ? new DefaultRenderableSorter() : sorter;
		this.ownContext = (context == null);
		this.context = (context == null) ? new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.LRU, 1)) : context;
		this.shaderProvider = (shaderProvider == null) ? new DefaultShaderProvider() : shaderProvider;
	}

	/** Construct a ModelBatch, using this constructor makes you responsible for calling context.begin() and context.end() yourself.
	 * @param context The {@link RenderContext} to use.
	 * @param shaderProvider The {@link ShaderProvider} to use, will be disposed when this ModelBatch is disposed. */
	public ModelBatch (final RenderContext context, final ShaderProvider shaderProvider) {
		this(context, shaderProvider, null);
	}

	/** Construct a ModelBatch, using this constructor makes you responsible for calling context.begin() and context.end() yourself.
	 * @param context The {@link RenderContext} to use.
	 * @param sorter The {@link RenderableSorter} to use. */
	public ModelBatch (final RenderContext context, final RenderableSorter sorter) {
		this(context, null, sorter);
	}

	/** Construct a ModelBatch, using this constructor makes you responsible for calling context.begin() and context.end() yourself.
	 * @param context The {@link RenderContext} to use. */
	public ModelBatch (final RenderContext context) {
		this(context, null, null);
	}

	/** Construct a ModelBatch
	 * @param shaderProvider The {@link ShaderProvider} to use, will be disposed when this ModelBatch is disposed.
	 * @param sorter The {@link RenderableSorter} to use. */
	public ModelBatch (final ShaderProvider shaderProvider, final RenderableSorter sorter) {
		this(null, shaderProvider, sorter);
	}

	/** Construct a ModelBatch
	 * @param sorter The {@link RenderableSorter} to use. */
	public ModelBatch (final RenderableSorter sorter) {
		this(null, null, sorter);
	}

	/** Construct a ModelBatch
	 * @param shaderProvider The {@link ShaderProvider} to use, will be disposed when this ModelBatch is disposed. */
	public ModelBatch (final ShaderProvider shaderProvider) {
		this(null, shaderProvider, null);
	}

	/** Construct a ModelBatch with the default implementation and the specified ubershader. See {@link DefaultShader} for more
	 * information about using a custom ubershader. Requires OpenGL ES 2.0.
	 * @param vertexShader The {@link FileHandle} of the vertex shader to use.
	 * @param fragmentShader The {@link FileHandle} of the fragment shader to use. */
	public ModelBatch (final FileHandle vertexShader, final FileHandle fragmentShader) {
		this(null, new DefaultShaderProvider(vertexShader, fragmentShader), null);
	}

	/** Construct a ModelBatch with the default implementation and the specified ubershader. See {@link DefaultShader} for more
	 * information about using a custom ubershader. Requires OpenGL ES 2.0.
	 * @param vertexShader The vertex shader to use.
	 * @param fragmentShader The fragment shader to use. */
	public ModelBatch (final String vertexShader, final String fragmentShader) {
		this(null, new DefaultShaderProvider(vertexShader, fragmentShader), null);
	}

	/** Construct a ModelBatch with the default implementation */
	public ModelBatch () {
		this(null, null, null);
	}

	/** Start rendering one or more {@link Renderable}s. Use one of the render() methods to provide the renderables. Must be
	 * followed by a call to {@link #end()}. The OpenGL context must not be altered between {@link #begin(Camera)} and
	 * {@link #end()}.
	 * @param cam The {@link Camera} to be used when rendering and sorting. */
	public void begin (final Camera cam) {
		if (camera != null) throw new GdxRuntimeException("Call end() first.");
		camera = cam;
		if (ownContext) context.begin();
	}

	/** Change the camera in between {@link #begin(Camera)} and {@link #end()}. This causes the batch to be flushed. Can only be
	 * called after the call to {@link #begin(Camera)} and before the call to {@link #end()}.
	 * @param cam The new camera to use. */
	public void setCamera (final Camera cam) {
		if (camera == null) throw new GdxRuntimeException("Call begin() first.");
		if (renderables.size > 0) flush();
		camera = cam;
	}

	/** Provides access to the current camera in between {@link #begin(Camera)} and {@link #end()}. Do not change the camera's
	 * values. Use {@link #setCamera(Camera)}, if you need to change the camera.
	 * @return The current camera being used or null if called outside {@link #begin(Camera)} and {@link #end()}. */
	public Camera getCamera () {
		return camera;
	}

	/** Checks whether the {@link RenderContext} returned by {@link #getRenderContext()} is owned and managed by this ModelBatch.
	 * When the RenderContext isn't owned by the ModelBatch, you are responsible for calling the {@link RenderContext#begin()} and
	 * {@link RenderContext#end()} methods yourself, as well as disposing the RenderContext.
	 * @return True if this ModelBatch owns the RenderContext, false otherwise. */
	public boolean ownsRenderContext () {
		return ownContext;
	}

	/** @return the {@link RenderContext} used by this ModelBatch. */
	public RenderContext getRenderContext () {
		return context;
	}

	/** @return the {@link ShaderProvider} used by this ModelBatch. */
	public ShaderProvider getShaderProvider () {
		return shaderProvider;
	}

	/** @return the {@link RenderableSorter} used by this ModelBatch. */
	public RenderableSorter getRenderableSorter () {
		return sorter;
	}

	/** Flushes the batch, causing all {@link Renderable}s in the batch to be rendered. Can only be called after the call to
	 * {@link #begin(Camera)} and before the call to {@link #end()}. */
	public void flush () {
		sorter.sort(camera, renderables);
		Shader currentShader = null;
		for (int i = 0; i < renderables.size; i++) {
			final Renderable renderable = renderables.get(i);
			if (currentShader != renderable.shader) {
				if (currentShader != null) currentShader.end();
				currentShader = renderable.shader;
				currentShader.begin(camera, context);
			}
			currentShader.render(renderable);
		}
		if (currentShader != null) currentShader.end();
		renderablesPool.flush();
		renderables.clear();
	}

	/** End rendering one or more {@link Renderable}s. Must be called after a call to {@link #begin(Camera)}. This will flush the
	 * batch, causing any renderables provided using one of the render() methods to be rendered. After a call to this method the
	 * OpenGL context can be altered again. */
	public void end () {
		flush();
		if (ownContext) context.end();
		camera = null;
	}

	/** Add a single {@link Renderable} to the batch. The {@link ShaderProvider} will be used to fetch a suitable {@link Shader}.
	 * Can only be called after a call to {@link #begin(Camera)} and before a call to {@link #end()}.
	 * @param renderable The {@link Renderable} to be added. */
	public void render (final Renderable renderable) {
		renderable.shader = shaderProvider.getShader(renderable);
		renderables.add(renderable);
	}

	/** Calls {@link RenderableProvider#getRenderables(Array, Pool)} and adds all returned {@link Renderable} instances to the
	 * current batch to be rendered. Can only be called after a call to {@link #begin(Camera)} and before a call to {@link #end()}.
	 * @param renderableProvider the renderable provider */
	public void render (final RenderableProvider renderableProvider) {
		final int offset = renderables.size;
		renderableProvider.getRenderables(renderables, renderablesPool);
		for (int i = offset; i < renderables.size; i++) {
			Renderable renderable = renderables.get(i);
			renderable.shader = shaderProvider.getShader(renderable);
		}
	}

	/** Calls {@link RenderableProvider#getRenderables(Array, Pool)} and adds all returned {@link Renderable} instances to the
	 * current batch to be rendered. Can only be called after a call to {@link #begin(Camera)} and before a call to {@link #end()}.
	 * @param renderableProviders one or more renderable providers */
	public <T extends RenderableProvider> void render (final Iterable<T> renderableProviders) {
		for (final RenderableProvider renderableProvider : renderableProviders)
			render(renderableProvider);
	}

	/** Calls {@link RenderableProvider#getRenderables(Array, Pool)} and adds all returned {@link Renderable} instances to the
	 * current batch to be rendered. Any environment set on the returned renderables will be replaced with the given environment.
	 * Can only be called after a call to {@link #begin(Camera)} and before a call to {@link #end()}.
	 * @param renderableProvider the renderable provider
	 * @param environment the {@link Environment} to use for the renderables */
	public void render (final RenderableProvider renderableProvider, final Environment environment) {
		final int offset = renderables.size;
		renderableProvider.getRenderables(renderables, renderablesPool);
		for (int i = offset; i < renderables.size; i++) {
			Renderable renderable = renderables.get(i);
			renderable.environment = environment;
			renderable.shader = shaderProvider.getShader(renderable);
		}
	}

	/** Calls {@link RenderableProvider#getRenderables(Array, Pool)} and adds all returned {@link Renderable} instances to the
	 * current batch to be rendered. Any environment set on the returned renderables will be replaced with the given environment.
	 * Can only be called after a call to {@link #begin(Camera)} and before a call to {@link #end()}.
	 * @param renderableProviders one or more renderable providers
	 * @param environment the {@link Environment} to use for the renderables */
	public <T extends RenderableProvider> void render (final Iterable<T> renderableProviders, final Environment environment) {
		for (final RenderableProvider renderableProvider : renderableProviders)
			render(renderableProvider, environment);
	}

	/** Calls {@link RenderableProvider#getRenderables(Array, Pool)} and adds all returned {@link Renderable} instances to the
	 * current batch to be rendered. Any shaders set on the returned renderables will be replaced with the given {@link Shader}.
	 * Can only be called after a call to {@link #begin(Camera)} and before a call to {@link #end()}.
	 * @param renderableProvider the renderable provider
	 * @param shader the shader to use for the renderables */
	public void render (final RenderableProvider renderableProvider, final Shader shader) {
		final int offset = renderables.size;
		renderableProvider.getRenderables(renderables, renderablesPool);
		for (int i = offset; i < renderables.size; i++) {
			Renderable renderable = renderables.get(i);
			renderable.shader = shader;
			renderable.shader = shaderProvider.getShader(renderable);
		}
	}

	/** Calls {@link RenderableProvider#getRenderables(Array, Pool)} and adds all returned {@link Renderable} instances to the
	 * current batch to be rendered. Any shaders set on the returned renderables will be replaced with the given {@link Shader}.
	 * Can only be called after a call to {@link #begin(Camera)} and before a call to {@link #end()}.
	 * @param renderableProviders one or more renderable providers
	 * @param shader the shader to use for the renderables */
	public <T extends RenderableProvider> void render (final Iterable<T> renderableProviders, final Shader shader) {
		for (final RenderableProvider renderableProvider : renderableProviders)
			render(renderableProvider, shader);
	}

	/** Calls {@link RenderableProvider#getRenderables(Array, Pool)} and adds all returned {@link Renderable} instances to the
	 * current batch to be rendered. Any environment set on the returned renderables will be replaced with the given environment.
	 * Any shaders set on the returned renderables will be replaced with the given {@link Shader}. Can only be called after a call
	 * to {@link #begin(Camera)} and before a call to {@link #end()}.
	 * @param renderableProvider the renderable provider
	 * @param environment the {@link Environment} to use for the renderables
	 * @param shader the shader to use for the renderables */
	public void render (final RenderableProvider renderableProvider, final Environment environment, final Shader shader) {
		final int offset = renderables.size;
		renderableProvider.getRenderables(renderables, renderablesPool);
		for (int i = offset; i < renderables.size; i++) {
			Renderable renderable = renderables.get(i);
			renderable.environment = environment;
			renderable.shader = shader;
			renderable.shader = shaderProvider.getShader(renderable);
		}
	}

	/** Calls {@link RenderableProvider#getRenderables(Array, Pool)} and adds all returned {@link Renderable} instances to the
	 * current batch to be rendered. Any environment set on the returned renderables will be replaced with the given environment.
	 * Any shaders set on the returned renderables will be replaced with the given {@link Shader}. Can only be called after a call
	 * to {@link #begin(Camera)} and before a call to {@link #end()}.
	 * @param renderableProviders one or more renderable providers
	 * @param environment the {@link Environment} to use for the renderables
	 * @param shader the shader to use for the renderables */
	public <T extends RenderableProvider> void render (final Iterable<T> renderableProviders, final Environment environment,
		final Shader shader) {
		for (final RenderableProvider renderableProvider : renderableProviders)
			render(renderableProvider, environment, shader);
	}

	@Override
	public void dispose () {
		shaderProvider.dispose();
	}
}
