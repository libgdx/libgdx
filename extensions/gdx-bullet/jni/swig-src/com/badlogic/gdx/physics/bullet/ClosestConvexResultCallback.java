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

public class ClosestConvexResultCallback extends ConvexResultCallback {
  private long swigCPtr;

  protected ClosestConvexResultCallback(long cPtr, boolean cMemoryOwn) {
    super(gdxBulletJNI.ClosestConvexResultCallback_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(ClosestConvexResultCallback obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_ClosestConvexResultCallback(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public ClosestConvexResultCallback(Vector3 convexFromWorld, Vector3 convexToWorld) {
    this(gdxBulletJNI.new_ClosestConvexResultCallback(convexFromWorld, convexToWorld), true);
  }

  public void setM_convexFromWorld(btVector3 value) {
    gdxBulletJNI.ClosestConvexResultCallback_m_convexFromWorld_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getM_convexFromWorld() {
    long cPtr = gdxBulletJNI.ClosestConvexResultCallback_m_convexFromWorld_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setM_convexToWorld(btVector3 value) {
    gdxBulletJNI.ClosestConvexResultCallback_m_convexToWorld_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getM_convexToWorld() {
    long cPtr = gdxBulletJNI.ClosestConvexResultCallback_m_convexToWorld_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setM_hitNormalWorld(btVector3 value) {
    gdxBulletJNI.ClosestConvexResultCallback_m_hitNormalWorld_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getM_hitNormalWorld() {
    long cPtr = gdxBulletJNI.ClosestConvexResultCallback_m_hitNormalWorld_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setM_hitPointWorld(btVector3 value) {
    gdxBulletJNI.ClosestConvexResultCallback_m_hitPointWorld_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getM_hitPointWorld() {
    long cPtr = gdxBulletJNI.ClosestConvexResultCallback_m_hitPointWorld_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setM_hitCollisionObject(btCollisionObject value) {
    gdxBulletJNI.ClosestConvexResultCallback_m_hitCollisionObject_set(swigCPtr, this, btCollisionObject.getCPtr(value), value);
  }

  public btCollisionObject getM_hitCollisionObject() {
	long cPtr = gdxBulletJNI.ClosestConvexResultCallback_m_hitCollisionObject_get(swigCPtr, this);
	return (cPtr == 0) ? null : btCollisionObject.getInstance(cPtr, false);
}

}
