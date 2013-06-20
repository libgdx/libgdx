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

public class btSortedOverlappingPairCache extends btOverlappingPairCache {
  private long swigCPtr;

  protected btSortedOverlappingPairCache(long cPtr, boolean cMemoryOwn) {
    super(gdxBulletJNI.btSortedOverlappingPairCache_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(btSortedOverlappingPairCache obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btSortedOverlappingPairCache(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public btSortedOverlappingPairCache() {
    this(gdxBulletJNI.new_btSortedOverlappingPairCache(), true);
  }

  public boolean needsBroadphaseCollision(btBroadphaseProxy proxy0, btBroadphaseProxy proxy1) {
    return gdxBulletJNI.btSortedOverlappingPairCache_needsBroadphaseCollision(swigCPtr, this, btBroadphaseProxy.getCPtr(proxy0), proxy0, btBroadphaseProxy.getCPtr(proxy1), proxy1);
  }

  public btBroadphasePairArray getOverlappingPairArray() {
    return new btBroadphasePairArray(gdxBulletJNI.btSortedOverlappingPairCache_getOverlappingPairArray__SWIG_0(swigCPtr, this), false);
  }

  public btBroadphasePair getOverlappingPairArrayPtr() {
    long cPtr = gdxBulletJNI.btSortedOverlappingPairCache_getOverlappingPairArrayPtr__SWIG_0(swigCPtr, this);
    return (cPtr == 0) ? null : new btBroadphasePair(cPtr, false);
  }

  public btOverlapFilterCallback getOverlapFilterCallback() {
    long cPtr = gdxBulletJNI.btSortedOverlappingPairCache_getOverlapFilterCallback(swigCPtr, this);
    return (cPtr == 0) ? null : new btOverlapFilterCallback(cPtr, false);
  }

}
