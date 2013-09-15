package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.lights.Lights;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/** A renderable defines a world transform, the {@link Mesh} to render
 * along with the offset into the mesh's indices and the number of indices to use plus the
 * primitive type to render the part of the mesh with. Finally, a renderable defines
 * a {@link Material} to be applied to the mesh.</p>
 * 
 * Renderables can be rendered via a {@link ModelBatch}, either directly, or by passing a
 * {@link RenderableProvider} like {@link ModelInstance} to the RenderBatch. 
 * A ModelInstance returns all Renderables via its {@link ModelInstance#getRenderables(Array, Pool)} method.</p>
 * 
 * When using a ModelBatch to render a renderable, The renderable and all its values must not be changed
 * in between the call to {@link ModelBatch#begin(com.badlogic.gdx.graphics.Camera)} and {@link ModelBatch#end()}.</p>
 * 
 * When the {@link #shader} member of the Renderable is set, the {@link ShaderProvider} of the {@link ModelBatch}
 * may try to use that shader instead of the default shader. Therefor, to assure the default shader is used, the
 * {@link #shader} member must be set to null. 
 * @author badlogic, xoppa */
public class Renderable {
	/** the model transform **/
	public final Matrix4 worldTransform = new Matrix4();
	/** the {@link Mesh} to render **/
	public Mesh mesh;
	/** the offset into the mesh's indices **/
	public int meshPartOffset;
	/** the number of indices/vertices to use **/
	public int meshPartSize;
	/** the primitive type, encoded as an OpenGL constant, like {@link GL20#GL_TRIANGLES} **/
	public int primitiveType;
	/** the {@link Material} to be applied to the mesh **/
	public Material material;
	/** the bones transformations used for skinning, or null if not applicable */  
	public Matrix4 bones[];
	/** the {@link Lights} to be used to render this Renderable, may be null **/
	public Lights lights;
	/** the {@link Shader} to be used to render this Renderable, may be null.
	 * It is not guaranteed that the shader will be used, the used {@link ShaderProvider} is responsible
	 * for actually choosing the correct shader to use. **/
	public Shader shader;
	/** user definable value. */
	public Object userData;
}