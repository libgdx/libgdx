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

public class SoftBodyLinkData {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected SoftBodyLinkData(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(SoftBodyLinkData obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_SoftBodyLinkData(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setM_material(SoftBodyMaterialData value) {
    gdxBulletJNI.SoftBodyLinkData_m_material_set(swigCPtr, this, SoftBodyMaterialData.getCPtr(value), value);
  }

  public SoftBodyMaterialData getM_material() {
    long cPtr = gdxBulletJNI.SoftBodyLinkData_m_material_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SoftBodyMaterialData(cPtr, false);
  }

  public void setM_nodeIndices(int[] value) {
    gdxBulletJNI.SoftBodyLinkData_m_nodeIndices_set(swigCPtr, this, value);
  }

  public int[] getM_nodeIndices() {
    return gdxBulletJNI.SoftBodyLinkData_m_nodeIndices_get(swigCPtr, this);
  }

  public void setM_restLength(float value) {
    gdxBulletJNI.SoftBodyLinkData_m_restLength_set(swigCPtr, this, value);
  }

  public float getM_restLength() {
    return gdxBulletJNI.SoftBodyLinkData_m_restLength_get(swigCPtr, this);
  }

  public void setM_bbending(int value) {
    gdxBulletJNI.SoftBodyLinkData_m_bbending_set(swigCPtr, this, value);
  }

  public int getM_bbending() {
    return gdxBulletJNI.SoftBodyLinkData_m_bbending_get(swigCPtr, this);
  }

  public SoftBodyLinkData() {
    this(gdxBulletJNI.new_SoftBodyLinkData(), true);
  }

}
