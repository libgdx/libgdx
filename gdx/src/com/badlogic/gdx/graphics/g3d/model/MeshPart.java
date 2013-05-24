package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;

/**
 * A mesh part is composed of a subset of vertices
 * of a {@link Mesh}, along with the primitive type.
 * The vertices subset is described by an offset into
 * the Mesh's indices array and the number of vertices.
 * @author badlogic
 *
 */
public class MeshPart {
	/** unique id within model, may be null (FIXME?) **/
	public String id;
	/** the primitive type, OpenGL constant like GL_TRIANGLES **/
	public int primitiveType;
	/** the offset into a Mesh's indices array **/
	public int indexOffset;
	/** the number of vertices that make up this part **/
	public int numVertices;
	/** the Mesh the part references, also stored in {@link Model} **/
	public Mesh mesh;
}
