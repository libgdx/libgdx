/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btTransformFloatData {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected btTransformFloatData(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(btTransformFloatData obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btTransformFloatData(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setM_basis(SWIGTYPE_p_btMatrix3x3FloatData value) {
    gdxBulletJNI.btTransformFloatData_m_basis_set(swigCPtr, this, SWIGTYPE_p_btMatrix3x3FloatData.getCPtr(value));
  }

  public SWIGTYPE_p_btMatrix3x3FloatData getM_basis() {
    return new SWIGTYPE_p_btMatrix3x3FloatData(gdxBulletJNI.btTransformFloatData_m_basis_get(swigCPtr, this), true);
  }

  public void setM_origin(btVector3FloatData value) {
    gdxBulletJNI.btTransformFloatData_m_origin_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getM_origin() {
    long cPtr = gdxBulletJNI.btTransformFloatData_m_origin_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public btTransformFloatData() {
    this(gdxBulletJNI.new_btTransformFloatData(), true);
  }

}
