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

public class btBroadphaseInterface extends BulletBase {
	private long swigCPtr;
	
	protected btBroadphaseInterface(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btBroadphaseInterface(long cPtr, boolean cMemoryOwn) {
		this("btBroadphaseInterface", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btBroadphaseInterface obj) {
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
				gdxBulletJNI.delete_btBroadphaseInterface(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btBroadphaseProxy createProxy(Vector3 aabbMin, Vector3 aabbMax, int shapeType, SWIGTYPE_p_void userPtr, short collisionFilterGroup, short collisionFilterMask, btDispatcher dispatcher, SWIGTYPE_p_void multiSapProxy) {
    long cPtr = gdxBulletJNI.btBroadphaseInterface_createProxy(swigCPtr, this, aabbMin, aabbMax, shapeType, SWIGTYPE_p_void.getCPtr(userPtr), collisionFilterGroup, collisionFilterMask, btDispatcher.getCPtr(dispatcher), dispatcher, SWIGTYPE_p_void.getCPtr(multiSapProxy));
    return (cPtr == 0) ? null : new btBroadphaseProxy(cPtr, false);
  }

  public void destroyProxy(btBroadphaseProxy proxy, btDispatcher dispatcher) {
    gdxBulletJNI.btBroadphaseInterface_destroyProxy(swigCPtr, this, btBroadphaseProxy.getCPtr(proxy), proxy, btDispatcher.getCPtr(dispatcher), dispatcher);
  }

  public void setAabb(btBroadphaseProxy proxy, Vector3 aabbMin, Vector3 aabbMax, btDispatcher dispatcher) {
    gdxBulletJNI.btBroadphaseInterface_setAabb(swigCPtr, this, btBroadphaseProxy.getCPtr(proxy), proxy, aabbMin, aabbMax, btDispatcher.getCPtr(dispatcher), dispatcher);
  }

  public void getAabb(btBroadphaseProxy proxy, Vector3 aabbMin, Vector3 aabbMax) {
    gdxBulletJNI.btBroadphaseInterface_getAabb(swigCPtr, this, btBroadphaseProxy.getCPtr(proxy), proxy, aabbMin, aabbMax);
  }

  public void rayTest(Vector3 rayFrom, Vector3 rayTo, btBroadphaseRayCallback rayCallback, Vector3 aabbMin, Vector3 aabbMax) {
    gdxBulletJNI.btBroadphaseInterface_rayTest__SWIG_0(swigCPtr, this, rayFrom, rayTo, btBroadphaseRayCallback.getCPtr(rayCallback), rayCallback, aabbMin, aabbMax);
  }

  public void rayTest(Vector3 rayFrom, Vector3 rayTo, btBroadphaseRayCallback rayCallback, Vector3 aabbMin) {
    gdxBulletJNI.btBroadphaseInterface_rayTest__SWIG_1(swigCPtr, this, rayFrom, rayTo, btBroadphaseRayCallback.getCPtr(rayCallback), rayCallback, aabbMin);
  }

  public void rayTest(Vector3 rayFrom, Vector3 rayTo, btBroadphaseRayCallback rayCallback) {
    gdxBulletJNI.btBroadphaseInterface_rayTest__SWIG_2(swigCPtr, this, rayFrom, rayTo, btBroadphaseRayCallback.getCPtr(rayCallback), rayCallback);
  }

  public void aabbTest(Vector3 aabbMin, Vector3 aabbMax, btBroadphaseAabbCallback callback) {
    gdxBulletJNI.btBroadphaseInterface_aabbTest(swigCPtr, this, aabbMin, aabbMax, btBroadphaseAabbCallback.getCPtr(callback), callback);
  }

  public void calculateOverlappingPairs(btDispatcher dispatcher) {
    gdxBulletJNI.btBroadphaseInterface_calculateOverlappingPairs(swigCPtr, this, btDispatcher.getCPtr(dispatcher), dispatcher);
  }

  public btOverlappingPairCache getOverlappingPairCache() {
    long cPtr = gdxBulletJNI.btBroadphaseInterface_getOverlappingPairCache__SWIG_0(swigCPtr, this);
    return (cPtr == 0) ? null : new btOverlappingPairCache(cPtr, false);
  }

  public void getBroadphaseAabb(Vector3 aabbMin, Vector3 aabbMax) {
    gdxBulletJNI.btBroadphaseInterface_getBroadphaseAabb(swigCPtr, this, aabbMin, aabbMax);
  }

  public void resetPool(btDispatcher dispatcher) {
    gdxBulletJNI.btBroadphaseInterface_resetPool(swigCPtr, this, btDispatcher.getCPtr(dispatcher), dispatcher);
  }

  public void printStats() {
    gdxBulletJNI.btBroadphaseInterface_printStats(swigCPtr, this);
  }

}
