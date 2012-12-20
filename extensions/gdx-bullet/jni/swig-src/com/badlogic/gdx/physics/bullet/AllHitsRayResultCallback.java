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

public class AllHitsRayResultCallback extends RayResultCallback {
  private long swigCPtr;

  protected AllHitsRayResultCallback(long cPtr, boolean cMemoryOwn) {
    super(gdxBulletJNI.AllHitsRayResultCallback_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(AllHitsRayResultCallback obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_AllHitsRayResultCallback(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public AllHitsRayResultCallback(Vector3 rayFromWorld, Vector3 rayToWorld) {
    this(gdxBulletJNI.new_AllHitsRayResultCallback(rayFromWorld, rayToWorld), true);
  }

  public void setM_collisionObjects(SWIGTYPE_p_btAlignedObjectArrayT_btCollisionObject_const_p_t value) {
    gdxBulletJNI.AllHitsRayResultCallback_m_collisionObjects_set(swigCPtr, this, SWIGTYPE_p_btAlignedObjectArrayT_btCollisionObject_const_p_t.getCPtr(value));
  }

  public SWIGTYPE_p_btAlignedObjectArrayT_btCollisionObject_const_p_t getM_collisionObjects() {
    long cPtr = gdxBulletJNI.AllHitsRayResultCallback_m_collisionObjects_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_btAlignedObjectArrayT_btCollisionObject_const_p_t(cPtr, false);
  }

  public void setM_rayFromWorld(btVector3 value) {
    gdxBulletJNI.AllHitsRayResultCallback_m_rayFromWorld_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getM_rayFromWorld() {
    long cPtr = gdxBulletJNI.AllHitsRayResultCallback_m_rayFromWorld_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setM_rayToWorld(btVector3 value) {
    gdxBulletJNI.AllHitsRayResultCallback_m_rayToWorld_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getM_rayToWorld() {
    long cPtr = gdxBulletJNI.AllHitsRayResultCallback_m_rayToWorld_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setM_hitNormalWorld(SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t value) {
    gdxBulletJNI.AllHitsRayResultCallback_m_hitNormalWorld_set(swigCPtr, this, SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t.getCPtr(value));
  }

  public SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t getM_hitNormalWorld() {
    long cPtr = gdxBulletJNI.AllHitsRayResultCallback_m_hitNormalWorld_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t(cPtr, false);
  }

  public void setM_hitPointWorld(SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t value) {
    gdxBulletJNI.AllHitsRayResultCallback_m_hitPointWorld_set(swigCPtr, this, SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t.getCPtr(value));
  }

  public SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t getM_hitPointWorld() {
    long cPtr = gdxBulletJNI.AllHitsRayResultCallback_m_hitPointWorld_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_btAlignedObjectArrayT_btVector3_t(cPtr, false);
  }

  public void setM_hitFractions(SWIGTYPE_p_btAlignedObjectArrayT_float_t value) {
    gdxBulletJNI.AllHitsRayResultCallback_m_hitFractions_set(swigCPtr, this, SWIGTYPE_p_btAlignedObjectArrayT_float_t.getCPtr(value));
  }

  public SWIGTYPE_p_btAlignedObjectArrayT_float_t getM_hitFractions() {
    long cPtr = gdxBulletJNI.AllHitsRayResultCallback_m_hitFractions_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_btAlignedObjectArrayT_float_t(cPtr, false);
  }

}
