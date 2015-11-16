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

public class btStorageResult extends btDiscreteCollisionDetectorInterface.Result {
	private long swigCPtr;
	
	protected btStorageResult(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, CollisionJNI.btStorageResult_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btStorageResult, normally you should not need this constructor it's intended for low-level usage. */
	public btStorageResult(long cPtr, boolean cMemoryOwn) {
		this("btStorageResult", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(CollisionJNI.btStorageResult_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btStorageResult obj) {
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
				CollisionJNI.delete_btStorageResult(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setNormalOnSurfaceB(btVector3 value) {
    CollisionJNI.btStorageResult_normalOnSurfaceB_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getNormalOnSurfaceB() {
    long cPtr = CollisionJNI.btStorageResult_normalOnSurfaceB_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setClosestPointInB(btVector3 value) {
    CollisionJNI.btStorageResult_closestPointInB_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getClosestPointInB() {
    long cPtr = CollisionJNI.btStorageResult_closestPointInB_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setDistance(float value) {
    CollisionJNI.btStorageResult_distance_set(swigCPtr, this, value);
  }

  public float getDistance() {
    return CollisionJNI.btStorageResult_distance_get(swigCPtr, this);
  }

}
