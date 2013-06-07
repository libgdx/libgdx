/**
 * Maintain a copy of the data
 */
%module btIndexedMesh

%{
#include <BulletCollision/CollisionShapes/btTriangleIndexVertexArray.h>
%}

%extend btIndexedMesh {
	void setTriangleIndexBase(short *data) {
		$self->m_triangleIndexBase = (unsigned char*)data;
	}
	void setVertexBase(float *data) {
		$self->m_vertexBase = (unsigned char*)data;
	}
};

%typemap(javaimports) btIndexedMesh %{
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
%}

%typemap(javacode) btIndexedMesh %{
	/** Construct a new btIndexedMesh based on the supplied {@link Mesh}
	 * The specified mesh must be indexed and triangulated and must outlive this btIndexedMesh.
	 * The buffers for the vertices and indices are shared amonst both. */
	public btIndexedMesh(final Mesh mesh) {
		this();
		set(mesh);
	}
	
	/** Construct a new btIndexedMesh based on the supplied {@link Mesh}
	 * The specified mesh must be indexed and triangulated and must outlive this btIndexedMesh.
	 * The buffers for the vertices and indices are shared amonst both. */
	public btIndexedMesh(final Mesh mesh, int offset, int count) {
		this();
		set(mesh, offset, count);
	}
	
	/** Convenience method to set this btIndexedMesh to the specified {@link Mesh} 
	 * The specified mesh must be indexed and triangulated and must outlive this btIndexedMesh.
	 * The buffers for the vertices and indices are shared amonst both. */
	public void set(final Mesh mesh) {
		set(mesh, 0, mesh.getNumIndices());
	}

	/** Convenience method to set this btIndexedMesh to the specified {@link Mesh} 
	 * The specified mesh must be indexed and triangulated and must outlive this btIndexedMesh.
	 * The buffers for the vertices and indices are shared amonst both. */
	public void set(final Mesh mesh, int offset, int count) {
		if ((count <= 0) || ((count % 3) != 0))
			throw new com.badlogic.gdx.utils.GdxRuntimeException("Mesh must be indexed and triangulated");
		java.nio.FloatBuffer buf = mesh.getVerticesBuffer();
		java.nio.ShortBuffer ind = mesh.getIndicesBuffer();
		VertexAttribute posAttr = mesh.getVertexAttribute(Usage.Position);
		if (posAttr == null)
			throw new com.badlogic.gdx.utils.GdxRuntimeException("Mesh doesn't have a position attribute");
		final int pos = buf.position();
		buf.position(posAttr.offset);
		setM_indexType(PHY_ScalarType.PHY_SHORT);
		setM_numTriangles(count/3);
		setM_numVertices(mesh.getNumVertices());
		setM_triangleIndexStride(6);
		setM_vertexStride(mesh.getVertexSize());
		setM_vertexType(PHY_ScalarType.PHY_FLOAT);
		final int ipos = ind.position();
		ind.position(offset);
		setTriangleIndexBase(ind);
		setVertexBase(buf);
		ind.position(ipos);
		buf.position(pos);
	}
%}

%typemap(javaimports) btTriangleIndexVertexArray %{
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
%}

%typemap(javacode) btTriangleIndexVertexArray %{
	com.badlogic.gdx.utils.Array<btIndexedMesh> meshes = null;
	
	/** Construct a new btTriangleIndexVertexArray based one or more supplied {@link com.badlogic.gdx.graphics.Mesh} instances.
	 * The specified meshes must be indexed and triangulated and must outlive this btTriangleIndexVertexArray.
     * The buffers for the vertices and indices are shared amongst both. */
	public btTriangleIndexVertexArray(final com.badlogic.gdx.graphics.Mesh... meshes) {
		this();
		addMesh(meshes);
	}
	
	/** Construct a new btTriangleIndexVertexArray based on one or more {@link Model} instances.
	 * Only the triangulated submeshes are added, which must be indexed. The model must outlive this btTriangleIndexVertexArray.
     * The buffers for the vertices and indices are shared amongst both. */
	public btTriangleIndexVertexArray(final Model... models) {
		this();
		addModel(models);
	}

	/** Add one or more {@link com.badlogic.gdx.graphics.Mesh} instances to this btTriangleIndexVertexArray. 
	 * The specified meshes must be indexed and triangulated and must outlive this btTriangleIndexVertexArray.
     * The buffers for the vertices and indices are shared amongst both. */
	public void addMesh(final com.badlogic.gdx.graphics.Mesh... meshes) {
		for (int i = 0; i < meshes.length; i++)
			addIndexedMesh(new btIndexedMesh(meshes[i]), PHY_ScalarType.PHY_SHORT, true);
	}
	
	/** Add one or more {@link Model} instances to this btTriangleIndexVertexArray.
	 * Only the triangulated submeshes are added, which must be indexed. The model must outlive this btTriangleIndexVertexArray.
     * The buffers for the vertices and indices are shared amongst both. */
	public void addModel(final Model... models) {
		for (int i = 0; i < models.length; i++) {
			for (int j = 0; j < models[i].meshParts.size; j++) {
				com.badlogic.gdx.graphics.g3d.model.MeshPart mp = models[i].meshParts.get(j);
				if (mp.primitiveType == com.badlogic.gdx.graphics.GL10.GL_TRIANGLES)
					addIndexedMesh(new btIndexedMesh(mp.mesh, mp.indexOffset, mp.numVertices), PHY_ScalarType.PHY_SHORT, true);
			}
		}
	}
	
	/** @param managed If true this btTriangleIndexVertexArray will maintain a reference to the {@link btIndexedMesh}
	 * and will delete it when this btTriangleIndexVertexArray is deleted. */
	public void addIndexedMesh(final btIndexedMesh mesh, int indexType, boolean managed) {
		addIndexedMesh(mesh, indexType);
		if (managed) {
			if (meshes == null)
				meshes = new com.badlogic.gdx.utils.Array<btIndexedMesh>();
			meshes.add(mesh);
		}
	}

	/** @param managed If true this btTriangleIndexVertexArray will maintain a reference to the {@link btIndexedMesh}
	 * and will delete it when this btTriangleIndexVertexArray is deleted. */
	public void addIndexedMesh(final btIndexedMesh mesh, boolean managed) {
		addIndexedMesh(mesh);
		if (managed) {
			if (meshes == null)
				meshes = new com.badlogic.gdx.utils.Array<btIndexedMesh>();
			meshes.add(mesh);
		}
	}
	
	protected void dispose() {
		if (meshes != null) {
			for (int i = 0; i < meshes.size; i++)
				meshes.get(i).delete();
			meshes.clear();
			meshes = null;
		}
	}
%}
%typemap(javadestruct_derived, methodname="delete", methodmodifiers="public synchronized") btTriangleIndexVertexArray %{ {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btTriangleIndexVertexArray(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
	dispose();
  }
%}

%include "BulletCollision/CollisionShapes/btTriangleIndexVertexArray.h"
