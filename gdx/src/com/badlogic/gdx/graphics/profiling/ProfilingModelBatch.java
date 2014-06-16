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

package com.badlogic.gdx.graphics.profiling;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;

/** Keeps track of how many tris have been rendered since the last call to {@link #reset()}.
 * @author Daniel Holderbaum */
public class ProfilingModelBatch extends ModelBatch {

	/** The amount of tris that have been rendered so far. */
	public int tris;

	/** Construct a ModelBatch, using this constructor makes you responsible for calling context.begin() and context.end() yourself.
	 * @param context The {@link RenderContext} to use.
	 * @param shaderProvider The {@link ShaderProvider} to use, will be disposed when this ModelBatch is disposed.
	 * @param sorter The {@link RenderableSorter} to use. */
	public ProfilingModelBatch (final RenderContext context, final ShaderProvider shaderProvider, final RenderableSorter sorter) {
		super(context, shaderProvider, sorter);
	}

	/** Construct a ModelBatch, using this constructor makes you responsible for calling context.begin() and context.end() yourself.
	 * @param context The {@link RenderContext} to use.
	 * @param shaderProvider The {@link ShaderProvider} to use, will be disposed when this ModelBatch is disposed. */
	public ProfilingModelBatch (final RenderContext context, final ShaderProvider shaderProvider) {
		super(context, shaderProvider);
	}

	/** Construct a ModelBatch, using this constructor makes you responsible for calling context.begin() and context.end() yourself.
	 * @param context The {@link RenderContext} to use.
	 * @param sorter The {@link RenderableSorter} to use. */
	public ProfilingModelBatch (final RenderContext context, final RenderableSorter sorter) {
		super(context, sorter);
	}

	/** Construct a ModelBatch, using this constructor makes you responsible for calling context.begin() and context.end() yourself.
	 * @param context The {@link RenderContext} to use. */
	public ProfilingModelBatch (final RenderContext context) {
		super(context);
	}

	/** Construct a ModelBatch
	 * @param shaderProvider The {@link ShaderProvider} to use, will be disposed when this ModelBatch is disposed.
	 * @param sorter The {@link RenderableSorter} to use. */
	public ProfilingModelBatch (final ShaderProvider shaderProvider, final RenderableSorter sorter) {
		super(shaderProvider, sorter);
	}

	/** Construct a ModelBatch
	 * @param sorter The {@link RenderableSorter} to use. */
	public ProfilingModelBatch (final RenderableSorter sorter) {
		super(sorter);
	}

	/** Construct a ModelBatch
	 * @param shaderProvider The {@link ShaderProvider} to use, will be disposed when this ModelBatch is disposed. */
	public ProfilingModelBatch (final ShaderProvider shaderProvider) {
		super(shaderProvider);
	}

	/** Construct a ModelBatch with the default implementation and the specified ubershader. See {@link DefaultShader} for more
	 * information about using a custom ubershader. Requires OpenGL ES 2.0.
	 * @param vertexShader The {@link FileHandle} of the vertex shader to use.
	 * @param fragmentShader The {@link FileHandle} of the fragment shader to use. */
	public ProfilingModelBatch (final FileHandle vertexShader, final FileHandle fragmentShader) {
		super(new DefaultShaderProvider(vertexShader, fragmentShader));
	}

	/** Construct a ModelBatch with the default implementation and the specified ubershader. See {@link DefaultShader} for more
	 * information about using a custom ubershader. Requires OpenGL ES 2.0.
	 * @param vertexShader The vertex shader to use.
	 * @param fragmentShader The fragment shader to use. */
	public ProfilingModelBatch (final String vertexShader, final String fragmentShader) {
		super(new DefaultShaderProvider(vertexShader, fragmentShader));
	}

	/** Construct a ModelBatch with the default implementation */
	public ProfilingModelBatch () {
		super();
	}

	@Override
	public void end () {
		for (Renderable renderable : renderables) {
			tris += renderable.meshPartSize / 3;
		}

		super.end();
	}

	/** Resets the tri counter. Should be called once per frame. */
	public void reset () {
		tris = 0;
	}

}
