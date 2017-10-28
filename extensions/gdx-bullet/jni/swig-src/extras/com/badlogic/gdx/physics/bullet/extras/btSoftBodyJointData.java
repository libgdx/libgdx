/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.extras;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.inversedynamics.MultiBodyTree;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btContactSolverInfo;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;

public class btSoftBodyJointData extends BulletBase {
	private long swigCPtr;
	
	protected btSoftBodyJointData(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btSoftBodyJointData, normally you should not need this constructor it's intended for low-level usage. */ 
	public btSoftBodyJointData(long cPtr, boolean cMemoryOwn) {
		this("btSoftBodyJointData", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btSoftBodyJointData obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
	}

	@Override
	protected void finalize() throws Throwable {
		if (!destroyed)
			destroy();
		super.finalize();
	}

  @Override protected synchronized void delete() {
		if (swigCPtr != 0) {
			if (swigCMemOwn) {
				swigCMemOwn = false;
				ExtrasJNI.delete_btSoftBodyJointData(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setBodyA(long value) {
    ExtrasJNI.btSoftBodyJointData_bodyA_set(swigCPtr, this, value);
  }

  public long getBodyA() {
    return ExtrasJNI.btSoftBodyJointData_bodyA_get(swigCPtr, this);
  }

  public void setBodyB(long value) {
    ExtrasJNI.btSoftBodyJointData_bodyB_set(swigCPtr, this, value);
  }

  public long getBodyB() {
    return ExtrasJNI.btSoftBodyJointData_bodyB_get(swigCPtr, this);
  }

  public void setRefs(btVector3FloatData value) {
    ExtrasJNI.btSoftBodyJointData_refs_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getRefs() {
    long cPtr = ExtrasJNI.btSoftBodyJointData_refs_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setCfm(float value) {
    ExtrasJNI.btSoftBodyJointData_cfm_set(swigCPtr, this, value);
  }

  public float getCfm() {
    return ExtrasJNI.btSoftBodyJointData_cfm_get(swigCPtr, this);
  }

  public void setErp(float value) {
    ExtrasJNI.btSoftBodyJointData_erp_set(swigCPtr, this, value);
  }

  public float getErp() {
    return ExtrasJNI.btSoftBodyJointData_erp_get(swigCPtr, this);
  }

  public void setSplit(float value) {
    ExtrasJNI.btSoftBodyJointData_split_set(swigCPtr, this, value);
  }

  public float getSplit() {
    return ExtrasJNI.btSoftBodyJointData_split_get(swigCPtr, this);
  }

  public void setDelete(int value) {
    ExtrasJNI.btSoftBodyJointData_delete_set(swigCPtr, this, value);
  }

  public int getDelete() {
    return ExtrasJNI.btSoftBodyJointData_delete_get(swigCPtr, this);
  }

  public void setRelPosition(btVector3FloatData value) {
    ExtrasJNI.btSoftBodyJointData_relPosition_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getRelPosition() {
    long cPtr = ExtrasJNI.btSoftBodyJointData_relPosition_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setBodyAtype(int value) {
    ExtrasJNI.btSoftBodyJointData_bodyAtype_set(swigCPtr, this, value);
  }

  public int getBodyAtype() {
    return ExtrasJNI.btSoftBodyJointData_bodyAtype_get(swigCPtr, this);
  }

  public void setBodyBtype(int value) {
    ExtrasJNI.btSoftBodyJointData_bodyBtype_set(swigCPtr, this, value);
  }

  public int getBodyBtype() {
    return ExtrasJNI.btSoftBodyJointData_bodyBtype_get(swigCPtr, this);
  }

  public void setJointType(int value) {
    ExtrasJNI.btSoftBodyJointData_jointType_set(swigCPtr, this, value);
  }

  public int getJointType() {
    return ExtrasJNI.btSoftBodyJointData_jointType_get(swigCPtr, this);
  }

  public void setPad(int value) {
    ExtrasJNI.btSoftBodyJointData_pad_set(swigCPtr, this, value);
  }

  public int getPad() {
    return ExtrasJNI.btSoftBodyJointData_pad_get(swigCPtr, this);
  }

  public btSoftBodyJointData() {
    this(ExtrasJNI.new_btSoftBodyJointData(), true);
  }

}
