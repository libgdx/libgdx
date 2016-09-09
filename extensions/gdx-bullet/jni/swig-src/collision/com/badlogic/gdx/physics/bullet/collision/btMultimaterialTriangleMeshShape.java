/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.collision;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btMultimaterialTriangleMeshShape extends btBvhTriangleMeshShape {
	private long swigCPtr;
	
	protected btMultimaterialTriangleMeshShape(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, CollisionJNI.btMultimaterialTriangleMeshShape_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btMultimaterialTriangleMeshShape, normally you should not need this constructor it's intended for low-level usage. */
	public btMultimaterialTriangleMeshShape(long cPtr, boolean cMemoryOwn) {
		this("btMultimaterialTriangleMeshShape", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(CollisionJNI.btMultimaterialTriangleMeshShape_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btMultimaterialTriangleMeshShape obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
	}

	@Override
	protected void finalize() throws Throwable {
		if (!destroyed)
			destroy();
		super.finalize();
	}

  @Override protected synchronized void delete() {
		if (swigCPtr != 0) {
			if (swigCMemOwn) {
				swigCMemOwn = false;
				CollisionJNI.delete_btMultimaterialTriangleMeshShape(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btMultimaterialTriangleMeshShape(btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression, boolean buildBvh) {
    this(CollisionJNI.new_btMultimaterialTriangleMeshShape__SWIG_0(btStridingMeshInterface.getCPtr(meshInterface), meshInterface, useQuantizedAabbCompression, buildBvh), true);
  }

  public btMultimaterialTriangleMeshShape(btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression) {
    this(CollisionJNI.new_btMultimaterialTriangleMeshShape__SWIG_1(btStridingMeshInterface.getCPtr(meshInterface), meshInterface, useQuantizedAabbCompression), true);
  }

  public btMultimaterialTriangleMeshShape(btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax, boolean buildBvh) {
    this(CollisionJNI.new_btMultimaterialTriangleMeshShape__SWIG_2(btStridingMeshInterface.getCPtr(meshInterface), meshInterface, useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax, buildBvh), true);
  }

  public btMultimaterialTriangleMeshShape(btStridingMeshInterface meshInterface, boolean useQuantizedAabbCompression, Vector3 bvhAabbMin, Vector3 bvhAabbMax) {
    this(CollisionJNI.new_btMultimaterialTriangleMeshShape__SWIG_3(btStridingMeshInterface.getCPtr(meshInterface), meshInterface, useQuantizedAabbCompression, bvhAabbMin, bvhAabbMax), true);
  }

  public btMaterial getMaterialProperties(int partID, int triIndex) {
    long cPtr = CollisionJNI.btMultimaterialTriangleMeshShape_getMaterialProperties(swigCPtr, this, partID, triIndex);
    return (cPtr == 0) ? null : new btMaterial(cPtr, false);
  }

}
