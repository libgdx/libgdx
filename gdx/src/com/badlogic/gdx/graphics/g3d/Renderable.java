package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.materials.NewMaterial;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;

/**
 * A renderable encapsulates all necessary information to render a {@link Node} within
 * a {@link Model}. A renderable defines a world {@link #transform}, the {@link Mesh} to render
 * along with the offset into the mesh's indices and the number of indices to use plus the
 * primitive type to render the part of the mesh with. Finally, a renderable defines
 * a {@link NewMaterial} to be applied to the mesh.</p>
 * 
 * Renderables can be rendered via a {@link ModelBatch}, either directly, or by passing a
 * {@link Model} to the RenderBatch. A Model returns all Renderables via its {@link Model#getRenderables(com.badlogic.gdx.utils.Array, com.badlogic.gdx.utils.Pool)} method.
 * @author badlogic
 *
 */
public class Renderable {
	/** the world transform **/
	public Matrix4 transform = new Matrix4();
	/** the mesh to render **/
	public Mesh mesh;
	/** the offset into the mesh's indices **/
	public int meshPartOffset;
	/** the number of indices/vertices to use **/
	public int meshPartSize;
	/** the primitive type, encoded as an OpenGL constant, like {@link GL20#GL_TRIANGLES} **/
	public int primitiveType;
	/** the material to be applied to the mesh **/
	public NewMaterial material;
}