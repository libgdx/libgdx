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

public class btCharacterControllerInterface extends btActionInterface {
  private long swigCPtr;

  protected btCharacterControllerInterface(long cPtr, boolean cMemoryOwn) {
    super(gdxBulletJNI.btCharacterControllerInterface_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(btCharacterControllerInterface obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btCharacterControllerInterface(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public void setWalkDirection(Vector3 walkDirection) {
    gdxBulletJNI.btCharacterControllerInterface_setWalkDirection(swigCPtr, this, walkDirection);
  }

  public void setVelocityForTimeInterval(Vector3 velocity, float timeInterval) {
    gdxBulletJNI.btCharacterControllerInterface_setVelocityForTimeInterval(swigCPtr, this, velocity, timeInterval);
  }

  public void reset() {
    gdxBulletJNI.btCharacterControllerInterface_reset(swigCPtr, this);
  }

  public void warp(Vector3 origin) {
    gdxBulletJNI.btCharacterControllerInterface_warp(swigCPtr, this, origin);
  }

  public void preStep(btCollisionWorld collisionWorld) {
    gdxBulletJNI.btCharacterControllerInterface_preStep(swigCPtr, this, btCollisionWorld.getCPtr(collisionWorld), collisionWorld);
  }

  public void playerStep(btCollisionWorld collisionWorld, float dt) {
    gdxBulletJNI.btCharacterControllerInterface_playerStep(swigCPtr, this, btCollisionWorld.getCPtr(collisionWorld), collisionWorld, dt);
  }

  public boolean canJump() {
    return gdxBulletJNI.btCharacterControllerInterface_canJump(swigCPtr, this);
  }

  public void jump() {
    gdxBulletJNI.btCharacterControllerInterface_jump(swigCPtr, this);
  }

  public boolean onGround() {
    return gdxBulletJNI.btCharacterControllerInterface_onGround(swigCPtr, this);
  }

}
