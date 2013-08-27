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
	
	@Override
	public boolean equals (Object arg0) {
		if (arg0 == null) return false;
		if (arg0 == this) return true;
		if (!(arg0 instanceof MeshPart)) return false;
		final MeshPart other = (MeshPart)arg0;
		return other.mesh == mesh && other.primitiveType == primitiveType && 
			other.indexOffset == indexOffset && other.numVertices == numVertices;
	}
}
