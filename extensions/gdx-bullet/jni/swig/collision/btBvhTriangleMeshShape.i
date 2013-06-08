/**
 * Maintain a copy of the data
 */
%module btBvhTriangleMeshShape

%{
#include <BulletCollision/CollisionShapes/btBvhTriangleMeshShape.h>
%}

%typemap(javaimports) btBvhTriangleMeshShape %{
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
%}

%typemap(javacode) btBvhTriangleMeshShape %{
	protected btStridingMeshInterface meshInterface = null;
	
	/** @param managed If true this btBvhTriangleMeshShape will keep a reference to the {@link btStridingMeshInterface}
	 * and deletes it when this btBvhTriangleMeshShape gets deleted. */
	public btBvhTriangleMeshShape(boolean managed, btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression) {
		this(meshInterface, useQuantizedAabbCompression);
		this.meshInterface = meshInterface;
	}

	/** @param managed If true this btBvhTriangleMeshShape will keep a reference to the {@link btStridingMeshInterface}
	 * and deletes it when this btBvhTriangleMeshShape gets deleted. */
	public btBvhTriangleMeshShape(boolean managed, btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression, boolean buildBvh) {
		this(meshInterface, useQuantizedAabbCompression, buildBvh);
		this.meshInterface = meshInterface;
	}
	
	/** @param managed If true this btBvhTriangleMeshShape will keep a reference to the {@link btStridingMeshInterface}
	 * and deletes it when this btBvhTriangleMeshShape gets deleted. */
	public btBvhTriangleMeshShape(boolean managed, btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax, boolean buildBvh) {
		this(meshInterface, useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax, buildBvh);
		this.meshInterface = meshInterface;
	}
	
	/** @param managed If true this btBvhTriangleMeshShape will keep a reference to the {@link btStridingMeshInterface}
	 * and deletes it when this btBvhTriangleMeshShape gets deleted. */
	public btBvhTriangleMeshShape(boolean managed, btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax) {
		this(meshInterface, useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax);
		this.meshInterface = meshInterface;
	}
	
	/** Construct a new btBvhTriangleMeshShape based one or more supplied {@link Mesh} instances.
	 * The specified meshes must be indexed and triangulated and must outlive this btBvhTriangleMeshShape.
     * The buffers for the vertices and indices are shared amonst both. */
	public btBvhTriangleMeshShape(boolean useQuantizedAabbCompression, final Mesh... meshes) {
		this(true, new btTriangleIndexVertexArray(meshes), useQuantizedAabbCompression);
	}
	
	/** Construct a new btBvhTriangleMeshShape based one or more supplied {@link Mesh} instances.
	 * The specified meshes must be indexed and triangulated and must outlive this btBvhTriangleMeshShape.
     * The buffers for the vertices and indices are shared amonst both. */
	public btBvhTriangleMeshShape(boolean useQuantizedAabbCompression, boolean buildBvh, final Mesh... meshes) {
		this(true, new btTriangleIndexVertexArray(meshes), useQuantizedAabbCompression, buildBvh);
	}
	
	/** Construct a new btBvhTriangleMeshShape based one or more supplied {@link Mesh} instances.
	 * The specified meshes must be indexed and triangulated and must outlive this btBvhTriangleMeshShape.
     * The buffers for the vertices and indices are shared amonst both. */
	public btBvhTriangleMeshShape(boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax, boolean buildBvh, final Mesh... meshes) {
		this(true, new btTriangleIndexVertexArray(meshes), useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax, buildBvh);
	}
	
	/** Construct a new btBvhTriangleMeshShape based one or more supplied {@link Mesh} instances.
	 * The specified meshes must be indexed and triangulated and must outlive this btBvhTriangleMeshShape.
     * The buffers for the vertices and indices are shared amonst both. */
	public btBvhTriangleMeshShape(boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax, final Mesh... meshes) {
		this(true, new btTriangleIndexVertexArray(meshes), useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax);
	}
	
	/** Construct a new btBvhTriangleMeshShape based one or more supplied {@link Model} instances.
	 * Only the triangulated submeshes are added, which must be indexed. The model must outlive this btTriangleIndexVertexArray.
     * The buffers for the vertices and indices are shared amonst both. */
	public btBvhTriangleMeshShape(boolean useQuantizedAabbCompression, final Model... models) {
		this(true, new btTriangleIndexVertexArray(models), useQuantizedAabbCompression);
	}
	
	/** Construct a new btBvhTriangleMeshShape based one or more supplied {@link Model} instances.
	 * Only the triangulated submeshes are added, which must be indexed. The model must outlive this btTriangleIndexVertexArray.
     * The buffers for the vertices and indices are shared amonst both. */
	public btBvhTriangleMeshShape(boolean useQuantizedAabbCompression, boolean buildBvh, final Model... models) {
		this(true, new btTriangleIndexVertexArray(models), useQuantizedAabbCompression, buildBvh);
	}
	
	/** Construct a new btBvhTriangleMeshShape based one or more supplied {@link Model} instances.
	 * Only the triangulated submeshes are added, which must be indexed. The model must outlive this btTriangleIndexVertexArray.
	 * The buffers for the vertices and indices are shared amonst both. */
	public btBvhTriangleMeshShape(boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax, boolean buildBvh, final Model... models) {
		this(true, new btTriangleIndexVertexArray(models), useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax, buildBvh);
	}
	
	/** Construct a new btBvhTriangleMeshShape based one or more supplied {@link Model} instances.
	 * Only the triangulated submeshes are added, which must be indexed. The model must outlive this btTriangleIndexVertexArray.
     * The buffers for the vertices and indices are shared amonst both. */
	public btBvhTriangleMeshShape(boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax, final Model... models) {
		this(true, new btTriangleIndexVertexArray(models), useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax);
	}
		
	protected void dispose() {
		if (meshInterface != null)
			meshInterface.delete();
		meshInterface = null;
	}
%}
%typemap(javadestruct_derived, methodname="delete", methodmodifiers="public synchronized") btBvhTriangleMeshShape %{ {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btBvhTriangleMeshShape(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
	dispose();
  }
%}

%include "BulletCollision/CollisionShapes/btBvhTriangleMeshShape.h"