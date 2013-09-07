/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btGhostObject extends btCollisionObject {
	private long swigCPtr;
	
	protected btGhostObject(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, gdxBulletJNI.btGhostObject_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btGhostObject(long cPtr, boolean cMemoryOwn) {
		this("btGhostObject", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(btGhostObject obj) {
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
				gdxBulletJNI.delete_btGhostObject(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btGhostObject() {
    this(gdxBulletJNI.new_btGhostObject(), true);
  }

  public void convexSweepTest(btConvexShape castShape, Matrix4 convexFromWorld, Matrix4 convexToWorld, ConvexResultCallback resultCallback, float allowedCcdPenetration) {
    gdxBulletJNI.btGhostObject_convexSweepTest__SWIG_0(swigCPtr, this, btConvexShape.getCPtr(castShape), castShape, convexFromWorld, convexToWorld, ConvexResultCallback.getCPtr(resultCallback), resultCallback, allowedCcdPenetration);
  }

  public void convexSweepTest(btConvexShape castShape, Matrix4 convexFromWorld, Matrix4 convexToWorld, ConvexResultCallback resultCallback) {
    gdxBulletJNI.btGhostObject_convexSweepTest__SWIG_1(swigCPtr, this, btConvexShape.getCPtr(castShape), castShape, convexFromWorld, convexToWorld, ConvexResultCallback.getCPtr(resultCallback), resultCallback);
  }

  public void rayTest(Vector3 rayFromWorld, Vector3 rayToWorld, RayResultCallback resultCallback) {
    gdxBulletJNI.btGhostObject_rayTest(swigCPtr, this, rayFromWorld, rayToWorld, RayResultCallback.getCPtr(resultCallback), resultCallback);
  }

  public void addOverlappingObjectInternal(btBroadphaseProxy otherProxy, btBroadphaseProxy thisProxy) {
    gdxBulletJNI.btGhostObject_addOverlappingObjectInternal__SWIG_0(swigCPtr, this, btBroadphaseProxy.getCPtr(otherProxy), otherProxy, btBroadphaseProxy.getCPtr(thisProxy), thisProxy);
  }

  public void addOverlappingObjectInternal(btBroadphaseProxy otherProxy) {
    gdxBulletJNI.btGhostObject_addOverlappingObjectInternal__SWIG_1(swigCPtr, this, btBroadphaseProxy.getCPtr(otherProxy), otherProxy);
  }

  public void removeOverlappingObjectInternal(btBroadphaseProxy otherProxy, btDispatcher dispatcher, btBroadphaseProxy thisProxy) {
    gdxBulletJNI.btGhostObject_removeOverlappingObjectInternal__SWIG_0(swigCPtr, this, btBroadphaseProxy.getCPtr(otherProxy), otherProxy, btDispatcher.getCPtr(dispatcher), dispatcher, btBroadphaseProxy.getCPtr(thisProxy), thisProxy);
  }

  public void removeOverlappingObjectInternal(btBroadphaseProxy otherProxy, btDispatcher dispatcher) {
    gdxBulletJNI.btGhostObject_removeOverlappingObjectInternal__SWIG_1(swigCPtr, this, btBroadphaseProxy.getCPtr(otherProxy), otherProxy, btDispatcher.getCPtr(dispatcher), dispatcher);
  }

  public int getNumOverlappingObjects() {
    return gdxBulletJNI.btGhostObject_getNumOverlappingObjects(swigCPtr, this);
  }

  public btCollisionObject getOverlappingObject(int index) {
	return btCollisionObject.getInstance(gdxBulletJNI.btGhostObject_getOverlappingObject__SWIG_0(swigCPtr, this, index), false);
}

  public btCollisionObjectArray getOverlappingPairs() {
    return new btCollisionObjectArray(gdxBulletJNI.btGhostObject_getOverlappingPairs__SWIG_0(swigCPtr, this), false);
  }

  public static btGhostObject upcast(btCollisionObject colObj) {
    long cPtr = gdxBulletJNI.btGhostObject_upcast__SWIG_0(btCollisionObject.getCPtr(colObj), colObj);
    return (cPtr == 0) ? null : new btGhostObject(cPtr, false);
  }

}
