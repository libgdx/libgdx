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

public class btShortIntIndexData {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected btShortIntIndexData(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(btShortIntIndexData obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btShortIntIndexData(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setM_value(short value) {
    gdxBulletJNI.btShortIntIndexData_m_value_set(swigCPtr, this, value);
  }

  public short getM_value() {
    return gdxBulletJNI.btShortIntIndexData_m_value_get(swigCPtr, this);
  }

  public void setM_pad(String value) {
    gdxBulletJNI.btShortIntIndexData_m_pad_set(swigCPtr, this, value);
  }

  public String getM_pad() {
    return gdxBulletJNI.btShortIntIndexData_m_pad_get(swigCPtr, this);
  }

  public btShortIntIndexData() {
    this(gdxBulletJNI.new_btShortIntIndexData(), true);
  }

}
