/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.11
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.dynamics;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btMultiBodyLinkDoubleData extends BulletBase {
	private long swigCPtr;
	
	protected btMultiBodyLinkDoubleData(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btMultiBodyLinkDoubleData, normally you should not need this constructor it's intended for low-level usage. */ 
	public btMultiBodyLinkDoubleData(long cPtr, boolean cMemoryOwn) {
		this("btMultiBodyLinkDoubleData", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btMultiBodyLinkDoubleData obj) {
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
				DynamicsJNI.delete_btMultiBodyLinkDoubleData(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setZeroRotParentToThis(btQuaternionDoubleData value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_zeroRotParentToThis_set(swigCPtr, this, btQuaternionDoubleData.getCPtr(value), value);
  }

  public btQuaternionDoubleData getZeroRotParentToThis() {
    long cPtr = DynamicsJNI.btMultiBodyLinkDoubleData_zeroRotParentToThis_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btQuaternionDoubleData(cPtr, false);
  }

  public void setParentComToThisComOffset(btVector3DoubleData value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_parentComToThisComOffset_set(swigCPtr, this, btVector3DoubleData.getCPtr(value), value);
  }

  public btVector3DoubleData getParentComToThisComOffset() {
    long cPtr = DynamicsJNI.btMultiBodyLinkDoubleData_parentComToThisComOffset_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3DoubleData(cPtr, false);
  }

  public void setThisPivotToThisComOffset(btVector3DoubleData value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_thisPivotToThisComOffset_set(swigCPtr, this, btVector3DoubleData.getCPtr(value), value);
  }

  public btVector3DoubleData getThisPivotToThisComOffset() {
    long cPtr = DynamicsJNI.btMultiBodyLinkDoubleData_thisPivotToThisComOffset_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3DoubleData(cPtr, false);
  }

  public void setJointAxisTop(btVector3DoubleData value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_jointAxisTop_set(swigCPtr, this, btVector3DoubleData.getCPtr(value), value);
  }

  public btVector3DoubleData getJointAxisTop() {
    long cPtr = DynamicsJNI.btMultiBodyLinkDoubleData_jointAxisTop_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3DoubleData(cPtr, false);
  }

  public void setJointAxisBottom(btVector3DoubleData value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_jointAxisBottom_set(swigCPtr, this, btVector3DoubleData.getCPtr(value), value);
  }

  public btVector3DoubleData getJointAxisBottom() {
    long cPtr = DynamicsJNI.btMultiBodyLinkDoubleData_jointAxisBottom_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3DoubleData(cPtr, false);
  }

  public void setLinkInertia(btVector3DoubleData value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_linkInertia_set(swigCPtr, this, btVector3DoubleData.getCPtr(value), value);
  }

  public btVector3DoubleData getLinkInertia() {
    long cPtr = DynamicsJNI.btMultiBodyLinkDoubleData_linkInertia_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3DoubleData(cPtr, false);
  }

  public void setLinkMass(double value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_linkMass_set(swigCPtr, this, value);
  }

  public double getLinkMass() {
    return DynamicsJNI.btMultiBodyLinkDoubleData_linkMass_get(swigCPtr, this);
  }

  public void setParentIndex(int value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_parentIndex_set(swigCPtr, this, value);
  }

  public int getParentIndex() {
    return DynamicsJNI.btMultiBodyLinkDoubleData_parentIndex_get(swigCPtr, this);
  }

  public void setJointType(int value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_jointType_set(swigCPtr, this, value);
  }

  public int getJointType() {
    return DynamicsJNI.btMultiBodyLinkDoubleData_jointType_get(swigCPtr, this);
  }

  public void setDofCount(int value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_dofCount_set(swigCPtr, this, value);
  }

  public int getDofCount() {
    return DynamicsJNI.btMultiBodyLinkDoubleData_dofCount_get(swigCPtr, this);
  }

  public void setPosVarCount(int value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_posVarCount_set(swigCPtr, this, value);
  }

  public int getPosVarCount() {
    return DynamicsJNI.btMultiBodyLinkDoubleData_posVarCount_get(swigCPtr, this);
  }

  public void setJointPos(double[] value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_jointPos_set(swigCPtr, this, value);
  }

  public double[] getJointPos() {
    return DynamicsJNI.btMultiBodyLinkDoubleData_jointPos_get(swigCPtr, this);
}

  public void setJointVel(double[] value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_jointVel_set(swigCPtr, this, value);
  }

  public double[] getJointVel() {
    return DynamicsJNI.btMultiBodyLinkDoubleData_jointVel_get(swigCPtr, this);
}

  public void setJointTorque(double[] value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_jointTorque_set(swigCPtr, this, value);
  }

  public double[] getJointTorque() {
    return DynamicsJNI.btMultiBodyLinkDoubleData_jointTorque_get(swigCPtr, this);
}

  public void setJointDamping(double value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_jointDamping_set(swigCPtr, this, value);
  }

  public double getJointDamping() {
    return DynamicsJNI.btMultiBodyLinkDoubleData_jointDamping_get(swigCPtr, this);
  }

  public void setJointFriction(double value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_jointFriction_set(swigCPtr, this, value);
  }

  public double getJointFriction() {
    return DynamicsJNI.btMultiBodyLinkDoubleData_jointFriction_get(swigCPtr, this);
  }

  public void setJointLowerLimit(double value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_jointLowerLimit_set(swigCPtr, this, value);
  }

  public double getJointLowerLimit() {
    return DynamicsJNI.btMultiBodyLinkDoubleData_jointLowerLimit_get(swigCPtr, this);
  }

  public void setJointUpperLimit(double value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_jointUpperLimit_set(swigCPtr, this, value);
  }

  public double getJointUpperLimit() {
    return DynamicsJNI.btMultiBodyLinkDoubleData_jointUpperLimit_get(swigCPtr, this);
  }

  public void setJointMaxForce(double value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_jointMaxForce_set(swigCPtr, this, value);
  }

  public double getJointMaxForce() {
    return DynamicsJNI.btMultiBodyLinkDoubleData_jointMaxForce_get(swigCPtr, this);
  }

  public void setJointMaxVelocity(double value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_jointMaxVelocity_set(swigCPtr, this, value);
  }

  public double getJointMaxVelocity() {
    return DynamicsJNI.btMultiBodyLinkDoubleData_jointMaxVelocity_get(swigCPtr, this);
  }

  public void setLinkName(String value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_linkName_set(swigCPtr, this, value);
  }

  public String getLinkName() {
    return DynamicsJNI.btMultiBodyLinkDoubleData_linkName_get(swigCPtr, this);
  }

  public void setJointName(String value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_jointName_set(swigCPtr, this, value);
  }

  public String getJointName() {
    return DynamicsJNI.btMultiBodyLinkDoubleData_jointName_get(swigCPtr, this);
  }

  public void setLinkCollider(btCollisionObjectDoubleData value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_linkCollider_set(swigCPtr, this, btCollisionObjectDoubleData.getCPtr(value), value);
  }

  public btCollisionObjectDoubleData getLinkCollider() {
    long cPtr = DynamicsJNI.btMultiBodyLinkDoubleData_linkCollider_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btCollisionObjectDoubleData(cPtr, false);
  }

  public void setPaddingPtr(String value) {
    DynamicsJNI.btMultiBodyLinkDoubleData_paddingPtr_set(swigCPtr, this, value);
  }

  public String getPaddingPtr() {
    return DynamicsJNI.btMultiBodyLinkDoubleData_paddingPtr_get(swigCPtr, this);
  }

  public btMultiBodyLinkDoubleData() {
    this(DynamicsJNI.new_btMultiBodyLinkDoubleData(), true);
  }

}
