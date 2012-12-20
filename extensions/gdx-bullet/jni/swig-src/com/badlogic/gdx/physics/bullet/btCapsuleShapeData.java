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

public class btCapsuleShapeData {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected btCapsuleShapeData(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(btCapsuleShapeData obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btCapsuleShapeData(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setM_convexInternalShapeData(btConvexInternalShapeData value) {
    gdxBulletJNI.btCapsuleShapeData_m_convexInternalShapeData_set(swigCPtr, this, btConvexInternalShapeData.getCPtr(value), value);
  }

  public btConvexInternalShapeData getM_convexInternalShapeData() {
    long cPtr = gdxBulletJNI.btCapsuleShapeData_m_convexInternalShapeData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btConvexInternalShapeData(cPtr, false);
  }

  public void setM_upAxis(int value) {
    gdxBulletJNI.btCapsuleShapeData_m_upAxis_set(swigCPtr, this, value);
  }

  public int getM_upAxis() {
    return gdxBulletJNI.btCapsuleShapeData_m_upAxis_get(swigCPtr, this);
  }

  public void setM_padding(String value) {
    gdxBulletJNI.btCapsuleShapeData_m_padding_set(swigCPtr, this, value);
  }

  public String getM_padding() {
    return gdxBulletJNI.btCapsuleShapeData_m_padding_get(swigCPtr, this);
  }

  public btCapsuleShapeData() {
    this(gdxBulletJNI.new_btCapsuleShapeData(), true);
  }

}
