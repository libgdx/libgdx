/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btBvhTriangleMeshShape extends btTriangleMeshShape {
  private long swigCPtr;

  protected btBvhTriangleMeshShape(long cPtr, boolean cMemoryOwn) {
    super(gdxBulletJNI.btBvhTriangleMeshShape_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(btBvhTriangleMeshShape obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete()  {
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


	btStridingMeshInterface meshInterface = null;
	
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
	
	/** Construct a new btBvhTriangleMeshShape based one or more supplied {@link com.badlogic.gdx.graphics.Mesh} instances.
	 * The specified meshes must be indexed and triangulated and must outlive this btBvhTriangleMeshShape.
     * The buffers for the vertices and indices are shared amonst both. */
	public btBvhTriangleMeshShape(boolean useQuantizedAabbCompression, final com.badlogic.gdx.graphics.Mesh... meshes) {
		this(true, new btTriangleIndexVertexArray(meshes), useQuantizedAabbCompression);
	}
	
	/** Construct a new btBvhTriangleMeshShape based one or more supplied {@link com.badlogic.gdx.graphics.Mesh} instances.
	 * The specified meshes must be indexed and triangulated and must outlive this btBvhTriangleMeshShape.
     * The buffers for the vertices and indices are shared amonst both. */
	public btBvhTriangleMeshShape(boolean useQuantizedAabbCompression, boolean buildBvh, final com.badlogic.gdx.graphics.Mesh... meshes) {
		this(true, new btTriangleIndexVertexArray(meshes), useQuantizedAabbCompression, buildBvh);
	}
	
	/** Construct a new btBvhTriangleMeshShape based one or more supplied {@link com.badlogic.gdx.graphics.Mesh} instances.
	 * The specified meshes must be indexed and triangulated and must outlive this btBvhTriangleMeshShape.
     * The buffers for the vertices and indices are shared amonst both. */
	public btBvhTriangleMeshShape(boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax, boolean buildBvh, final com.badlogic.gdx.graphics.Mesh... meshes) {
		this(true, new btTriangleIndexVertexArray(meshes), useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax, buildBvh);
	}
	
	/** Construct a new btBvhTriangleMeshShape based one or more supplied {@link com.badlogic.gdx.graphics.Mesh} instances.
	 * The specified meshes must be indexed and triangulated and must outlive this btBvhTriangleMeshShape.
     * The buffers for the vertices and indices are shared amonst both. */
	public btBvhTriangleMeshShape(boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax, final com.badlogic.gdx.graphics.Mesh... meshes) {
		this(true, new btTriangleIndexVertexArray(meshes), useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax);
	}
	
	/** Construct a new btBvhTriangleMeshShape based one or more supplied {@link com.badlogic.gdx.graphics.g3d.model.Model} instances.
	 * Only the triangulated submeshes are added, which must be indexed. The model must outlive this btTriangleIndexVertexArray.
     * The buffers for the vertices and indices are shared amonst both. */
	public btBvhTriangleMeshShape(boolean useQuantizedAabbCompression, final com.badlogic.gdx.graphics.g3d.model.Model... models) {
		this(true, new btTriangleIndexVertexArray(models), useQuantizedAabbCompression);
	}
	
	/** Construct a new btBvhTriangleMeshShape based one or more supplied {@link com.badlogic.gdx.graphics.g3d.model.Model} instances.
	 * Only the triangulated submeshes are added, which must be indexed. The model must outlive this btTriangleIndexVertexArray.
     * The buffers for the vertices and indices are shared amonst both. */
	public btBvhTriangleMeshShape(boolean useQuantizedAabbCompression, boolean buildBvh, final com.badlogic.gdx.graphics.g3d.model.Model... models) {
		this(true, new btTriangleIndexVertexArray(models), useQuantizedAabbCompression, buildBvh);
	}
	
	/** Construct a new btBvhTriangleMeshShape based one or more supplied {@link com.badlogic.gdx.graphics.g3d.model.Model} instances.
	 * Only the triangulated submeshes are added, which must be indexed. The model must outlive this btTriangleIndexVertexArray.
	 * The buffers for the vertices and indices are shared amonst both. */
	public btBvhTriangleMeshShape(boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax, boolean buildBvh, final com.badlogic.gdx.graphics.g3d.model.Model... models) {
		this(true, new btTriangleIndexVertexArray(models), useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax, buildBvh);
	}
	
	/** Construct a new btBvhTriangleMeshShape based one or more supplied {@link com.badlogic.gdx.graphics.g3d.model.Model} instances.
	 * Only the triangulated submeshes are added, which must be indexed. The model must outlive this btTriangleIndexVertexArray.
     * The buffers for the vertices and indices are shared amonst both. */
	public btBvhTriangleMeshShape(boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax, final com.badlogic.gdx.graphics.g3d.model.Model... models) {
		this(true, new btTriangleIndexVertexArray(models), useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax);
	}
		
	protected void dispose() {
		if (meshInterface != null)
			meshInterface.delete();
		meshInterface = null;
	}

  public btBvhTriangleMeshShape(btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression, boolean buildBvh) {
    this(gdxBulletJNI.new_btBvhTriangleMeshShape__SWIG_0(btStridingMeshInterface.getCPtr(meshInterface), meshInterface, useQuantizedAabbCompression, buildBvh), true);
  }

  public btBvhTriangleMeshShape(btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression) {
    this(gdxBulletJNI.new_btBvhTriangleMeshShape__SWIG_1(btStridingMeshInterface.getCPtr(meshInterface), meshInterface, useQuantizedAabbCompression), true);
  }

  public btBvhTriangleMeshShape(btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax, boolean buildBvh) {
    this(gdxBulletJNI.new_btBvhTriangleMeshShape__SWIG_2(btStridingMeshInterface.getCPtr(meshInterface), meshInterface, useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax, buildBvh), true);
  }

  public btBvhTriangleMeshShape(btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax) {
    this(gdxBulletJNI.new_btBvhTriangleMeshShape__SWIG_3(btStridingMeshInterface.getCPtr(meshInterface), meshInterface, useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax), true);
  }

  public boolean getOwnsBvh() {
    return gdxBulletJNI.btBvhTriangleMeshShape_getOwnsBvh(swigCPtr, this);
  }

  public void performRaycast(btTriangleCallback callback, Vector3 raySource, Vector3 rayTarget) {
    gdxBulletJNI.btBvhTriangleMeshShape_performRaycast(swigCPtr, this, btTriangleCallback.getCPtr(callback), callback, raySource, rayTarget);
  }

  public void performConvexcast(btTriangleCallback callback, Vector3 boxSource, Vector3 boxTarget, Vector3 boxMin, Vector3 boxMax) {
    gdxBulletJNI.btBvhTriangleMeshShape_performConvexcast(swigCPtr, this, btTriangleCallback.getCPtr(callback), callback, boxSource, boxTarget, boxMin, boxMax);
  }

  public void refitTree(Vector3 aabbMin, Vector3 aabbMax) {
    gdxBulletJNI.btBvhTriangleMeshShape_refitTree(swigCPtr, this, aabbMin, aabbMax);
  }

  public void partialRefitTree(Vector3 aabbMin, Vector3 aabbMax) {
    gdxBulletJNI.btBvhTriangleMeshShape_partialRefitTree(swigCPtr, this, aabbMin, aabbMax);
  }

  public btOptimizedBvh getOptimizedBvh() {
    long cPtr = gdxBulletJNI.btBvhTriangleMeshShape_getOptimizedBvh(swigCPtr, this);
    return (cPtr == 0) ? null : new btOptimizedBvh(cPtr, false);
  }

  public void setOptimizedBvh(btOptimizedBvh bvh, Vector3 localScaling) {
    gdxBulletJNI.btBvhTriangleMeshShape_setOptimizedBvh__SWIG_0(swigCPtr, this, btOptimizedBvh.getCPtr(bvh), bvh, localScaling);
  }

  public void setOptimizedBvh(btOptimizedBvh bvh) {
    gdxBulletJNI.btBvhTriangleMeshShape_setOptimizedBvh__SWIG_1(swigCPtr, this, btOptimizedBvh.getCPtr(bvh), bvh);
  }

  public void buildOptimizedBvh() {
    gdxBulletJNI.btBvhTriangleMeshShape_buildOptimizedBvh(swigCPtr, this);
  }

  public boolean usesQuantizedAabbCompression() {
    return gdxBulletJNI.btBvhTriangleMeshShape_usesQuantizedAabbCompression(swigCPtr, this);
  }

  public void setTriangleInfoMap(btTriangleInfoMap triangleInfoMap) {
    gdxBulletJNI.btBvhTriangleMeshShape_setTriangleInfoMap(swigCPtr, this, btTriangleInfoMap.getCPtr(triangleInfoMap), triangleInfoMap);
  }

  public btTriangleInfoMap getTriangleInfoMap() {
    long cPtr = gdxBulletJNI.btBvhTriangleMeshShape_getTriangleInfoMap__SWIG_0(swigCPtr, this);
    return (cPtr == 0) ? null : new btTriangleInfoMap(cPtr, false);
  }

  public void serializeSingleBvh(SWIGTYPE_p_btSerializer serializer) {
    gdxBulletJNI.btBvhTriangleMeshShape_serializeSingleBvh(swigCPtr, this, SWIGTYPE_p_btSerializer.getCPtr(serializer));
  }

  public void serializeSingleTriangleInfoMap(SWIGTYPE_p_btSerializer serializer) {
    gdxBulletJNI.btBvhTriangleMeshShape_serializeSingleTriangleInfoMap(swigCPtr, this, SWIGTYPE_p_btSerializer.getCPtr(serializer));
  }

}
