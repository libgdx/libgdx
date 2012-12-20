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

public class btTriangleInfoMapData {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected btTriangleInfoMapData(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(btTriangleInfoMapData obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btTriangleInfoMapData(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setM_hashTablePtr(SWIGTYPE_p_int value) {
    gdxBulletJNI.btTriangleInfoMapData_m_hashTablePtr_set(swigCPtr, this, SWIGTYPE_p_int.getCPtr(value));
  }

  public SWIGTYPE_p_int getM_hashTablePtr() {
    long cPtr = gdxBulletJNI.btTriangleInfoMapData_m_hashTablePtr_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_int(cPtr, false);
  }

  public void setM_nextPtr(SWIGTYPE_p_int value) {
    gdxBulletJNI.btTriangleInfoMapData_m_nextPtr_set(swigCPtr, this, SWIGTYPE_p_int.getCPtr(value));
  }

  public SWIGTYPE_p_int getM_nextPtr() {
    long cPtr = gdxBulletJNI.btTriangleInfoMapData_m_nextPtr_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_int(cPtr, false);
  }

  public void setM_valueArrayPtr(btTriangleInfoData value) {
    gdxBulletJNI.btTriangleInfoMapData_m_valueArrayPtr_set(swigCPtr, this, btTriangleInfoData.getCPtr(value), value);
  }

  public btTriangleInfoData getM_valueArrayPtr() {
    long cPtr = gdxBulletJNI.btTriangleInfoMapData_m_valueArrayPtr_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btTriangleInfoData(cPtr, false);
  }

  public void setM_keyArrayPtr(SWIGTYPE_p_int value) {
    gdxBulletJNI.btTriangleInfoMapData_m_keyArrayPtr_set(swigCPtr, this, SWIGTYPE_p_int.getCPtr(value));
  }

  public SWIGTYPE_p_int getM_keyArrayPtr() {
    long cPtr = gdxBulletJNI.btTriangleInfoMapData_m_keyArrayPtr_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_int(cPtr, false);
  }

  public void setM_convexEpsilon(float value) {
    gdxBulletJNI.btTriangleInfoMapData_m_convexEpsilon_set(swigCPtr, this, value);
  }

  public float getM_convexEpsilon() {
    return gdxBulletJNI.btTriangleInfoMapData_m_convexEpsilon_get(swigCPtr, this);
  }

  public void setM_planarEpsilon(float value) {
    gdxBulletJNI.btTriangleInfoMapData_m_planarEpsilon_set(swigCPtr, this, value);
  }

  public float getM_planarEpsilon() {
    return gdxBulletJNI.btTriangleInfoMapData_m_planarEpsilon_get(swigCPtr, this);
  }

  public void setM_equalVertexThreshold(float value) {
    gdxBulletJNI.btTriangleInfoMapData_m_equalVertexThreshold_set(swigCPtr, this, value);
  }

  public float getM_equalVertexThreshold() {
    return gdxBulletJNI.btTriangleInfoMapData_m_equalVertexThreshold_get(swigCPtr, this);
  }

  public void setM_edgeDistanceThreshold(float value) {
    gdxBulletJNI.btTriangleInfoMapData_m_edgeDistanceThreshold_set(swigCPtr, this, value);
  }

  public float getM_edgeDistanceThreshold() {
    return gdxBulletJNI.btTriangleInfoMapData_m_edgeDistanceThreshold_get(swigCPtr, this);
  }

  public void setM_zeroAreaThreshold(float value) {
    gdxBulletJNI.btTriangleInfoMapData_m_zeroAreaThreshold_set(swigCPtr, this, value);
  }

  public float getM_zeroAreaThreshold() {
    return gdxBulletJNI.btTriangleInfoMapData_m_zeroAreaThreshold_get(swigCPtr, this);
  }

  public void setM_nextSize(int value) {
    gdxBulletJNI.btTriangleInfoMapData_m_nextSize_set(swigCPtr, this, value);
  }

  public int getM_nextSize() {
    return gdxBulletJNI.btTriangleInfoMapData_m_nextSize_get(swigCPtr, this);
  }

  public void setM_hashTableSize(int value) {
    gdxBulletJNI.btTriangleInfoMapData_m_hashTableSize_set(swigCPtr, this, value);
  }

  public int getM_hashTableSize() {
    return gdxBulletJNI.btTriangleInfoMapData_m_hashTableSize_get(swigCPtr, this);
  }

  public void setM_numValues(int value) {
    gdxBulletJNI.btTriangleInfoMapData_m_numValues_set(swigCPtr, this, value);
  }

  public int getM_numValues() {
    return gdxBulletJNI.btTriangleInfoMapData_m_numValues_get(swigCPtr, this);
  }

  public void setM_numKeys(int value) {
    gdxBulletJNI.btTriangleInfoMapData_m_numKeys_set(swigCPtr, this, value);
  }

  public int getM_numKeys() {
    return gdxBulletJNI.btTriangleInfoMapData_m_numKeys_get(swigCPtr, this);
  }

  public void setM_padding(String value) {
    gdxBulletJNI.btTriangleInfoMapData_m_padding_set(swigCPtr, this, value);
  }

  public String getM_padding() {
    return gdxBulletJNI.btTriangleInfoMapData_m_padding_get(swigCPtr, this);
  }

  public btTriangleInfoMapData() {
    this(gdxBulletJNI.new_btTriangleInfoMapData(), true);
  }

}
