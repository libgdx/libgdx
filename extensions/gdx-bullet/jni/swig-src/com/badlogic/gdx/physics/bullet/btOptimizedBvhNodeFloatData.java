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

public class btOptimizedBvhNodeFloatData {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected btOptimizedBvhNodeFloatData(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(btOptimizedBvhNodeFloatData obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btOptimizedBvhNodeFloatData(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setM_aabbMinOrg(btVector3FloatData value) {
    gdxBulletJNI.btOptimizedBvhNodeFloatData_m_aabbMinOrg_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getM_aabbMinOrg() {
    long cPtr = gdxBulletJNI.btOptimizedBvhNodeFloatData_m_aabbMinOrg_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setM_aabbMaxOrg(btVector3FloatData value) {
    gdxBulletJNI.btOptimizedBvhNodeFloatData_m_aabbMaxOrg_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getM_aabbMaxOrg() {
    long cPtr = gdxBulletJNI.btOptimizedBvhNodeFloatData_m_aabbMaxOrg_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setM_escapeIndex(int value) {
    gdxBulletJNI.btOptimizedBvhNodeFloatData_m_escapeIndex_set(swigCPtr, this, value);
  }

  public int getM_escapeIndex() {
    return gdxBulletJNI.btOptimizedBvhNodeFloatData_m_escapeIndex_get(swigCPtr, this);
  }

  public void setM_subPart(int value) {
    gdxBulletJNI.btOptimizedBvhNodeFloatData_m_subPart_set(swigCPtr, this, value);
  }

  public int getM_subPart() {
    return gdxBulletJNI.btOptimizedBvhNodeFloatData_m_subPart_get(swigCPtr, this);
  }

  public void setM_triangleIndex(int value) {
    gdxBulletJNI.btOptimizedBvhNodeFloatData_m_triangleIndex_set(swigCPtr, this, value);
  }

  public int getM_triangleIndex() {
    return gdxBulletJNI.btOptimizedBvhNodeFloatData_m_triangleIndex_get(swigCPtr, this);
  }

  public void setM_pad(String value) {
    gdxBulletJNI.btOptimizedBvhNodeFloatData_m_pad_set(swigCPtr, this, value);
  }

  public String getM_pad() {
    return gdxBulletJNI.btOptimizedBvhNodeFloatData_m_pad_get(swigCPtr, this);
  }

  public btOptimizedBvhNodeFloatData() {
    this(gdxBulletJNI.new_btOptimizedBvhNodeFloatData(), true);
  }

}
