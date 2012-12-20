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

public class btPointCollector {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected btPointCollector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(btPointCollector obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btPointCollector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setM_normalOnBInWorld(btVector3 value) {
    gdxBulletJNI.btPointCollector_m_normalOnBInWorld_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getM_normalOnBInWorld() {
    long cPtr = gdxBulletJNI.btPointCollector_m_normalOnBInWorld_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setM_pointInWorld(btVector3 value) {
    gdxBulletJNI.btPointCollector_m_pointInWorld_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getM_pointInWorld() {
    long cPtr = gdxBulletJNI.btPointCollector_m_pointInWorld_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setM_distance(float value) {
    gdxBulletJNI.btPointCollector_m_distance_set(swigCPtr, this, value);
  }

  public float getM_distance() {
    return gdxBulletJNI.btPointCollector_m_distance_get(swigCPtr, this);
  }

  public void setM_hasResult(boolean value) {
    gdxBulletJNI.btPointCollector_m_hasResult_set(swigCPtr, this, value);
  }

  public boolean getM_hasResult() {
    return gdxBulletJNI.btPointCollector_m_hasResult_get(swigCPtr, this);
  }

  public btPointCollector() {
    this(gdxBulletJNI.new_btPointCollector(), true);
  }

  public void setShapeIdentifiersA(int partId0, int index0) {
    gdxBulletJNI.btPointCollector_setShapeIdentifiersA(swigCPtr, this, partId0, index0);
  }

  public void setShapeIdentifiersB(int partId1, int index1) {
    gdxBulletJNI.btPointCollector_setShapeIdentifiersB(swigCPtr, this, partId1, index1);
  }

  public void addContactPoint(Vector3 normalOnBInWorld, Vector3 pointInWorld, float depth) {
    gdxBulletJNI.btPointCollector_addContactPoint(swigCPtr, this, normalOnBInWorld, pointInWorld, depth);
  }

}
