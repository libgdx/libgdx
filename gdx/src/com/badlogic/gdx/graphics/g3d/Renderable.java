package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.lights.Lights;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;

/**
 * A renderable defines a world transform, the {@link Mesh} to render
 * along with the offset into the mesh's indices and the number of indices to use plus the
 * primitive type to render the part of the mesh with. Finally, a renderable defines
 * a {@link Material} to be applied to the mesh.</p>
 * 
 * Renderables can be rendered via a {@link ModelBatch}, either directly, or by passing a
 * {@link ModelInstance} to the RenderBatch. A Model returns all Renderables via its {@link ModelInstance#getRenderables(com.badlogic.gdx.utils.Array, com.badlogic.gdx.utils.Pool)} method.
 * @author badlogic
 *
 */
public class Renderable {
	/** the model transform **/
	public final Matrix4 worldTransform = new Matrix4();
	/** the mesh to render **/
	public Mesh mesh;
	/** the offset into the mesh's indices **/
	public int meshPartOffset;
	/** the number of indices/vertices to use **/
	public int meshPartSize;
	/** the primitive type, encoded as an OpenGL constant, like {@link GL20#GL_TRIANGLES} **/
	public int primitiveType;
	/** the material to be applied to the mesh **/
	public Material material;
	/** the bones transformations used for skinning, or null if not applicable */  
	public Matrix4 bones[];
	/** the lights to be used to render this Renderable, may be null **/
	public Lights lights;
	/** the Shader to be used to render this Renderable, may be null **/
	public Shader shader;
	/** user definable value. */
	public Object userData;
}