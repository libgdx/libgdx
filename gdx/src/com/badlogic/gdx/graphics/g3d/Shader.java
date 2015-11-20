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
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

/** Interface which is used to render one or more {@link Renderable}s.</p>
 * 
 * A Shader is responsible for the actual rendering of an {@link Renderable}. Typically, when using OpenGL ES 2.0 or higher, it
 * encapsulates a {@link ShaderProgram} and takes care of all OpenGL calls necessary to render the {@link Renderable}. When using
 * OpenGL ES 1.x it takes care of the fixed pipeline.</p>
 * 
 * To start rendering the {@link #begin(Camera, RenderContext)} method must be called. After which the {@link #end()} method must
 * be called to stop rendering. In between one or more calls to the {@link #render(Renderable)} method can be made to render a
 * {@link Renderable}. The {@link #render(Renderable)} method must not be called before a call to
 * {@link #begin(Camera, RenderContext)} or after a call to {@link #end()}. Each Shader needs exclusive access to the OpenGL state
 * and {@link RenderContext} between the {@link #begin(Camera, RenderContext)} and {@link #end()} methods, therefore only one
 * shader can be used at a time (they must not be nested).</p>
 * 
 * A specific Shader instance might be (and usually is) dedicated to a specific type of {@link Renderable}. For example it might
 * use a {@link ShaderProgram} that is compiled with uniforms (shader input) for specific {@link Attribute} types. Therefore the
 * {@link #canRender(Renderable)} method can be used to check if the Shader instance can be used for a specific {@link Renderable}
 * . Rendering a {@link Renderable} using a Shader for which {@link #canRender(Renderable)} returns false might result in
 * unpredicted behavior or crash the application.</p>
 * 
 * To manage multiple shaders and create a new shader when required, a {@link ShaderProvider} can be used. Therefore, in practice,
 * a specific Shader implementation is usually accompanied by a specific {@link ShaderProvider} implementation (usually extending
 * {@link BaseShaderProvider}).</p>
 * 
 * When a Shader is constructed, the {@link #init()} method must be called before it can be used. Most commonly, the
 * {@link #init()} method compiles the {@link ShaderProgram}, fetches uniform locations and performs other preparations for usage
 * of the Shader. When the shader is no longer needed, it must disposed using the {@link Disposable#dispose()} method. This, for
 * example, disposed (unloads for memory) the used {@link ShaderProgram}.</p>
 * @author Xoppa */
public interface Shader extends Disposable {
	/** Initializes the Shader, must be called before the Shader can be used. This typically compiles a {@link ShaderProgram},
	 * fetches uniform locations and performs other preparations for usage of the Shader. */
	void init ();

	/** Compare this shader against the other, used for sorting, light weight shaders are rendered first. */
	int compareTo (Shader other); // TODO: probably better to add some weight value to sort on

	/** Checks whether this shader is intended to render the {@link Renderable}. Use this to make sure a call to the
	 * {@link #render(Renderable)} method will succeed. This is expected to be a fast, non-blocking method. Note that this method
	 * will only return true if it is intended to be used. Even when it returns false the Shader might still be capable of
	 * rendering, but it's not preferred to do so.
	 * @param instance The renderable to check against this shader.
	 * @return true if this shader is intended to render the {@link Renderable}, false otherwise. */
	boolean canRender (Renderable instance);

	/** Initializes the context for exclusive rendering by this shader. Use the {@link #render(Renderable)} method to render a
	 * {@link Renderable}. When done rendering the {@link #end()} method must be called.
	 * @param camera The camera to use when rendering
	 * @param context The context to be used, which must be exclusive available for the shader until the call to the {@link #end()}
	 *           method. */
	void begin (Camera camera, RenderContext context);

	/** Renders the {@link Renderable}, must be called between {@link #begin(Camera, RenderContext)} and {@link #end()}. The Shader
	 * instance might not be able to render every type of {@link Renderable}s. Use the {@link #canRender(Renderable)} method to
	 * check if the Shader is capable of rendering a specific {@link Renderable}.
	 * @param renderable The renderable to render, all required fields (e.g. {@link Renderable#material} and others) must be set.
	 *           The {@link Renderable#shader} field will be ignored. */
	void render (final Renderable renderable);

	/** Cleanup the context so other shaders can render. Must be called when done rendering using the {@link #render(Renderable)}
	 * method, which must be preceded by a call to {@link #begin(Camera, RenderContext)}. After a call to this method an call to
	 * the {@link #render(Renderable)} method will fail until the {@link #begin(Camera, RenderContext)} is called. */
	void end ();
}
