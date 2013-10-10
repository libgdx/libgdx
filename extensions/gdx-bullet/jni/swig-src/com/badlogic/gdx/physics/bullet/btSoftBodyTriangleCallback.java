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

public class btSoftBodyTriangleCallback extends btTriangleCallback {
	private long swigCPtr;
	
	protected btSoftBodyTriangleCallback(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, gdxBulletJNI.btSoftBodyTriangleCallback_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btSoftBodyTriangleCallback(long cPtr, boolean cMemoryOwn) {
		this("btSoftBodyTriangleCallback", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(gdxBulletJNI.btSoftBodyTriangleCallback_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btSoftBodyTriangleCallback obj) {
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
				gdxBulletJNI.delete_btSoftBodyTriangleCallback(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setTriangleCount(int value) {
    gdxBulletJNI.btSoftBodyTriangleCallback_triangleCount_set(swigCPtr, this, value);
  }

  public int getTriangleCount() {
    return gdxBulletJNI.btSoftBodyTriangleCallback_triangleCount_get(swigCPtr, this);
  }

  public btSoftBodyTriangleCallback(btDispatcher dispatcher, btCollisionObjectWrapper body0Wrap, btCollisionObjectWrapper body1Wrap, boolean isSwapped) {
    this(gdxBulletJNI.new_btSoftBodyTriangleCallback(btDispatcher.getCPtr(dispatcher), dispatcher, btCollisionObjectWrapper.getCPtr(body0Wrap), body0Wrap, btCollisionObjectWrapper.getCPtr(body1Wrap), body1Wrap, isSwapped), true);
  }

  public void setTimeStepAndCounters(float collisionMarginTriangle, btCollisionObjectWrapper triObjWrap, btDispatcherInfo dispatchInfo, btManifoldResult resultOut) {
    gdxBulletJNI.btSoftBodyTriangleCallback_setTimeStepAndCounters(swigCPtr, this, collisionMarginTriangle, btCollisionObjectWrapper.getCPtr(triObjWrap), triObjWrap, btDispatcherInfo.getCPtr(dispatchInfo), dispatchInfo, btManifoldResult.getCPtr(resultOut), resultOut);
  }

  public void clearCache() {
    gdxBulletJNI.btSoftBodyTriangleCallback_clearCache(swigCPtr, this);
  }

  public Vector3 getAabbMin() {
	return gdxBulletJNI.btSoftBodyTriangleCallback_getAabbMin(swigCPtr, this);
}

  public Vector3 getAabbMax() {
	return gdxBulletJNI.btSoftBodyTriangleCallback_getAabbMax(swigCPtr, this);
}

}
