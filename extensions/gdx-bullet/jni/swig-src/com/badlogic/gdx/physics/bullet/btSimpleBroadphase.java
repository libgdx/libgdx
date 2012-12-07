/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.8
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btSimpleBroadphase extends btBroadphaseInterface {
  private long swigCPtr;

  protected btSimpleBroadphase(long cPtr, boolean cMemoryOwn) {
    super(gdxBulletJNI.btSimpleBroadphase_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(btSimpleBroadphase obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btSimpleBroadphase(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public btSimpleBroadphase(int maxProxies, btOverlappingPairCache overlappingPairCache) {
    this(gdxBulletJNI.new_btSimpleBroadphase__SWIG_0(maxProxies, btOverlappingPairCache.getCPtr(overlappingPairCache), overlappingPairCache), true);
  }

  public btSimpleBroadphase(int maxProxies) {
    this(gdxBulletJNI.new_btSimpleBroadphase__SWIG_1(maxProxies), true);
  }

  public btSimpleBroadphase() {
    this(gdxBulletJNI.new_btSimpleBroadphase__SWIG_2(), true);
  }

  public static boolean aabbOverlap(btSimpleBroadphaseProxy proxy0, btSimpleBroadphaseProxy proxy1) {
    return gdxBulletJNI.btSimpleBroadphase_aabbOverlap(btSimpleBroadphaseProxy.getCPtr(proxy0), proxy0, btSimpleBroadphaseProxy.getCPtr(proxy1), proxy1);
  }

  public void rayTest(Vector3 rayFrom, Vector3 rayTo, btBroadphaseRayCallback rayCallback, Vector3 aabbMin, Vector3 aabbMax) {
    gdxBulletJNI.btSimpleBroadphase_rayTest__SWIG_0(swigCPtr, this, rayFrom, rayTo, btBroadphaseRayCallback.getCPtr(rayCallback), rayCallback, aabbMin, aabbMax);
  }

  public void rayTest(Vector3 rayFrom, Vector3 rayTo, btBroadphaseRayCallback rayCallback, Vector3 aabbMin) {
    gdxBulletJNI.btSimpleBroadphase_rayTest__SWIG_1(swigCPtr, this, rayFrom, rayTo, btBroadphaseRayCallback.getCPtr(rayCallback), rayCallback, aabbMin);
  }

  public void rayTest(Vector3 rayFrom, Vector3 rayTo, btBroadphaseRayCallback rayCallback) {
    gdxBulletJNI.btSimpleBroadphase_rayTest__SWIG_2(swigCPtr, this, rayFrom, rayTo, btBroadphaseRayCallback.getCPtr(rayCallback), rayCallback);
  }

  public btOverlappingPairCache getOverlappingPairCache() {
    long cPtr = gdxBulletJNI.btSimpleBroadphase_getOverlappingPairCache__SWIG_0(swigCPtr, this);
    return (cPtr == 0) ? null : new btOverlappingPairCache(cPtr, false);
  }

  public boolean testAabbOverlap(btBroadphaseProxy proxy0, btBroadphaseProxy proxy1) {
    return gdxBulletJNI.btSimpleBroadphase_testAabbOverlap(swigCPtr, this, btBroadphaseProxy.getCPtr(proxy0), proxy0, btBroadphaseProxy.getCPtr(proxy1), proxy1);
  }

}
