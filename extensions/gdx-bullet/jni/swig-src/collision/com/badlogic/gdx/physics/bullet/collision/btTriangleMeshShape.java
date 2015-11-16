/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
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

public class btTriangleMeshShape extends btConcaveShape {
	private long swigCPtr;
	
	protected btTriangleMeshShape(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, CollisionJNI.btTriangleMeshShape_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btTriangleMeshShape, normally you should not need this constructor it's intended for low-level usage. */
	public btTriangleMeshShape(long cPtr, boolean cMemoryOwn) {
		this("btTriangleMeshShape", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(CollisionJNI.btTriangleMeshShape_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btTriangleMeshShape obj) {
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
				CollisionJNI.delete_btTriangleMeshShape(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public Vector3 localGetSupportingVertex(Vector3 vec) {
	return CollisionJNI.btTriangleMeshShape_localGetSupportingVertex(swigCPtr, this, vec);
}

  public Vector3 localGetSupportingVertexWithoutMargin(Vector3 vec) {
	return CollisionJNI.btTriangleMeshShape_localGetSupportingVertexWithoutMargin(swigCPtr, this, vec);
}

  public void recalcLocalAabb() {
    CollisionJNI.btTriangleMeshShape_recalcLocalAabb(swigCPtr, this);
  }

  public btStridingMeshInterface getMeshInterface() {
    long cPtr = CollisionJNI.btTriangleMeshShape_getMeshInterface__SWIG_0(swigCPtr, this);
    return (cPtr == 0) ? null : new btStridingMeshInterface(cPtr, false);
  }

  public Vector3 getLocalAabbMin() {
	return CollisionJNI.btTriangleMeshShape_getLocalAabbMin(swigCPtr, this);
}

  public Vector3 getLocalAabbMax() {
	return CollisionJNI.btTriangleMeshShape_getLocalAabbMax(swigCPtr, this);
}

}
