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

public class btEigen {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected btEigen(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(btEigen obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btEigen(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public static int system(Matrix3 a, SWIGTYPE_p_btMatrix3x3 vectors, btVector3 values) {
    return gdxBulletJNI.btEigen_system__SWIG_0(a, SWIGTYPE_p_btMatrix3x3.getCPtr(vectors), btVector3.getCPtr(values), values);
  }

  public static int system(Matrix3 a, SWIGTYPE_p_btMatrix3x3 vectors) {
    return gdxBulletJNI.btEigen_system__SWIG_1(a, SWIGTYPE_p_btMatrix3x3.getCPtr(vectors));
  }

  public btEigen() {
    this(gdxBulletJNI.new_btEigen(), true);
  }

}
