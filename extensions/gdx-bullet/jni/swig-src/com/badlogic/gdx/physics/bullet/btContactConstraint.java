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

public class btContactConstraint extends btTypedConstraint {
  private long swigCPtr;

  protected btContactConstraint(long cPtr, boolean cMemoryOwn) {
    super(gdxBulletJNI.btContactConstraint_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(btContactConstraint obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btContactConstraint(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public void setContactManifold(btPersistentManifold contactManifold) {
    gdxBulletJNI.btContactConstraint_setContactManifold(swigCPtr, this, btPersistentManifold.getCPtr(contactManifold), contactManifold);
  }

  public btPersistentManifold getContactManifold() {
    long cPtr = gdxBulletJNI.btContactConstraint_getContactManifold__SWIG_0(swigCPtr, this);
    return (cPtr == 0) ? null : new btPersistentManifold(cPtr, false);
  }

}
