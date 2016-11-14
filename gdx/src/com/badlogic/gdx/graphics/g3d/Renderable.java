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

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/** A Renderable contains all information about a single render instruction (typically a draw call).</p>
 * 
 * It defines what (the shape), how (the material) and where (the transform) should be rendered by which shader.</p>
 * 
 * The shape is defined using the mesh, meshPartOffset, meshPartSize and primitiveType members. This matches the members of the
 * {@link MeshPart} class. The meshPartOffset is used to specify the offset within the mesh and the meshPartSize is used to
 * specify the part (in total number of vertices) to render. If the mesh is indexed (which is when {@link Mesh#getNumIndices()} >
 * 0) then both values are in number of indices within the indices array of the mesh, otherwise they are in number of vertices
 * within the vertices array of the mesh. Note that some classes might require the mesh to be indexed.</p>
 * 
 * The {@link #material} and (optional) {@link #environment} values are combined to specify how the shape should look like.
 * Typically these are used to specify uniform values or other OpenGL state changes. When a value is present in both the
 * {@link #material} and {@link #environment}, then the value of the {@link #material} will be used.</p>
 * 
 * Renderables can be rendered directly using a {@link Shader} (in which case the {@link #shader} member is ignored). Though more
 * typically Renderables are rendered via a {@link ModelBatch}, either directly, or by passing a {@link RenderableProvider} like
 * {@link ModelInstance} to the RenderBatch.</p>
 * 
 * A ModelInstance returns all Renderables via its {@link ModelInstance#getRenderables(Array, Pool)} method. In which case the
 * value of {@link ModelInstance#userData} will be set to the {@link #userData} member. The {@link #userData} member can be used
 * to pass additional data to the shader. However, in most scenario's it is advised to use the {@link #material} or
 * {@link #environment} member with custom {@link Attribute}s to pass data to the shader.</p>
 * 
 * In some cases, (for example for non-hierarchical basic game objects requiring only a single draw call) it is possible to extend
 * the Renderable class and add additional fields to pass to the shader. While extending the Renderable class can be useful, the
 * shader should not rely on it. Similar to the {@link #userData} member it is advised to use the {@link #material} and
 * {@link #environment} members to pass data to the shader.</p>
 * 
 * When using a ModelBatch to render a Renderable, The Renderable and all its values must not be changed in between the call to
 * {@link ModelBatch#begin(com.badlogic.gdx.graphics.Camera)} and {@link ModelBatch#end()}. Therefor Renderable instances cannot
 * be reused for multiple render calls.</p>
 * 
 * When the {@link #shader} member of the Renderable is set, the {@link ShaderProvider} of the {@link ModelBatch} may decide to
 * use that shader instead of the default shader. Therefor, to assure the default shader is used, the {@link #shader} member must
 * be set to null.</p>
 * @author badlogic, xoppa */
public class Renderable {
	/** Used to specify the transformations (like translation, scale and rotation) to apply to the shape. In other words: it is used
	 * to transform the vertices from model space into world space. **/
	public final Matrix4 worldTransform = new Matrix4();
	/** The {@link MeshPart} that contains the shape to render **/
	public final MeshPart meshPart = new MeshPart();
	/** The {@link Material} to be applied to the shape (part of the mesh), must not be null.
	 * @see #environment **/
	public Material material;
	/** The {@link Environment} to be used to render this Renderable, may be null. When specified it will be combined by the shader
	 * with the {@link #material}. When both the material and environment contain an attribute of the same type, the attribute of
	 * the material will be used. **/
	public Environment environment;
	/** The bone transformations used for skinning, or null if not applicable. When specified and the mesh contains one or more
	 * {@link com.badlogic.gdx.graphics.VertexAttributes.Usage#BoneWeight} vertex attributes, then the BoneWeight index is used as
	 * index in the array. If the array isn't large enough then the identity matrix is used. Each BoneWeight weight is used to
	 * combine multiple bones into a single transformation matrix, which is used to transform the vertex to model space. In other
	 * words: the bone transformation is applied prior to the {@link #worldTransform}. */
	public Matrix4 bones[];
	/** The {@link Shader} to be used to render this Renderable using a {@link ModelBatch}, may be null. It is not guaranteed that
	 * the shader will be used, the used {@link ShaderProvider} is responsible for actually choosing the correct shader to use. **/
	public Shader shader;
	/** User definable value, may be null. */
	public Object userData;

	public Renderable set (Renderable renderable) {
		worldTransform.set(renderable.worldTransform);
		material = renderable.material;
		meshPart.set(renderable.meshPart);
		bones = renderable.bones;
		environment = renderable.environment;
		shader = renderable.shader;
		userData = renderable.userData;
		return this;
	}
}
