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

public class InternalTickCallback {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected InternalTickCallback(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(InternalTickCallback obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_InternalTickCallback(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected void swigDirectorDisconnect() {
    swigCMemOwn = false;
    delete();
  }

  public void swigReleaseOwnership() {
    swigCMemOwn = false;
    gdxBulletJNI.InternalTickCallback_change_ownership(this, swigCPtr, false);
  }

  public void swigTakeOwnership() {
    swigCMemOwn = true;
    gdxBulletJNI.InternalTickCallback_change_ownership(this, swigCPtr, true);
  }

  public InternalTickCallback(btDynamicsWorld dynamicsWorld, boolean isPreTick) {
    this(gdxBulletJNI.new_InternalTickCallback__SWIG_0(btDynamicsWorld.getCPtr(dynamicsWorld), dynamicsWorld, isPreTick), true);
    gdxBulletJNI.InternalTickCallback_director_connect(this, swigCPtr, swigCMemOwn, true);
  }

  public InternalTickCallback(btDynamicsWorld dynamicsWorld) {
    this(gdxBulletJNI.new_InternalTickCallback__SWIG_1(btDynamicsWorld.getCPtr(dynamicsWorld), dynamicsWorld), true);
    gdxBulletJNI.InternalTickCallback_director_connect(this, swigCPtr, swigCMemOwn, true);
  }

  public InternalTickCallback() {
    this(gdxBulletJNI.new_InternalTickCallback__SWIG_2(), true);
    gdxBulletJNI.InternalTickCallback_director_connect(this, swigCPtr, swigCMemOwn, true);
  }

  public void onInternalTick(btDynamicsWorld dynamicsWorld, float timeStep) {
    if (getClass() == InternalTickCallback.class) gdxBulletJNI.InternalTickCallback_onInternalTick(swigCPtr, this, btDynamicsWorld.getCPtr(dynamicsWorld), dynamicsWorld, timeStep); else gdxBulletJNI.InternalTickCallback_onInternalTickSwigExplicitInternalTickCallback(swigCPtr, this, btDynamicsWorld.getCPtr(dynamicsWorld), dynamicsWorld, timeStep);
  }

  public void detach() {
    gdxBulletJNI.InternalTickCallback_detach__SWIG_0(swigCPtr, this);
  }

  public void attach(btDynamicsWorld dynamicsWorld, boolean isPreTick) {
    gdxBulletJNI.InternalTickCallback_attach__SWIG_0(swigCPtr, this, btDynamicsWorld.getCPtr(dynamicsWorld), dynamicsWorld, isPreTick);
  }

  public void attach() {
    gdxBulletJNI.InternalTickCallback_attach__SWIG_1(swigCPtr, this);
  }

  public static void detach(btDynamicsWorld dynamicsWorld, boolean isPreTick) {
    gdxBulletJNI.InternalTickCallback_detach__SWIG_1(btDynamicsWorld.getCPtr(dynamicsWorld), dynamicsWorld, isPreTick);
  }

}
