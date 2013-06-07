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

public class btDynamicsWorldDoubleData {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected btDynamicsWorldDoubleData(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(btDynamicsWorldDoubleData obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btDynamicsWorldDoubleData(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setM_solverInfo(btContactSolverInfoDoubleData value) {
    gdxBulletJNI.btDynamicsWorldDoubleData_m_solverInfo_set(swigCPtr, this, btContactSolverInfoDoubleData.getCPtr(value), value);
  }

  public btContactSolverInfoDoubleData getM_solverInfo() {
    long cPtr = gdxBulletJNI.btDynamicsWorldDoubleData_m_solverInfo_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btContactSolverInfoDoubleData(cPtr, false);
  }

  public void setM_gravity(btVector3DoubleData value) {
    gdxBulletJNI.btDynamicsWorldDoubleData_m_gravity_set(swigCPtr, this, btVector3DoubleData.getCPtr(value), value);
  }

  public btVector3DoubleData getM_gravity() {
    long cPtr = gdxBulletJNI.btDynamicsWorldDoubleData_m_gravity_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3DoubleData(cPtr, false);
  }

  public btDynamicsWorldDoubleData() {
    this(gdxBulletJNI.new_btDynamicsWorldDoubleData(), true);
  }

}
