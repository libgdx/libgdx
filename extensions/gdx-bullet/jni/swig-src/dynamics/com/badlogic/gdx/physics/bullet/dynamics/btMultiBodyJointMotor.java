/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
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

public class btMultiBodyJointMotor extends btMultiBodyConstraint {
	private long swigCPtr;
	
	protected btMultiBodyJointMotor(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, DynamicsJNI.btMultiBodyJointMotor_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btMultiBodyJointMotor, normally you should not need this constructor it's intended for low-level usage. */
	public btMultiBodyJointMotor(long cPtr, boolean cMemoryOwn) {
		this("btMultiBodyJointMotor", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(DynamicsJNI.btMultiBodyJointMotor_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btMultiBodyJointMotor obj) {
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
				DynamicsJNI.delete_btMultiBodyJointMotor(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btMultiBodyJointMotor(btMultiBody body, int link, float desiredVelocity, float maxMotorImpulse) {
    this(DynamicsJNI.new_btMultiBodyJointMotor__SWIG_0(btMultiBody.getCPtr(body), body, link, desiredVelocity, maxMotorImpulse), true);
  }

  public btMultiBodyJointMotor(btMultiBody body, int link, int linkDoF, float desiredVelocity, float maxMotorImpulse) {
    this(DynamicsJNI.new_btMultiBodyJointMotor__SWIG_1(btMultiBody.getCPtr(body), body, link, linkDoF, desiredVelocity, maxMotorImpulse), true);
  }

  public void setVelocityTarget(float velTarget, float kd) {
    DynamicsJNI.btMultiBodyJointMotor_setVelocityTarget__SWIG_0(swigCPtr, this, velTarget, kd);
  }

  public void setVelocityTarget(float velTarget) {
    DynamicsJNI.btMultiBodyJointMotor_setVelocityTarget__SWIG_1(swigCPtr, this, velTarget);
  }

  public void setPositionTarget(float posTarget, float kp) {
    DynamicsJNI.btMultiBodyJointMotor_setPositionTarget__SWIG_0(swigCPtr, this, posTarget, kp);
  }

  public void setPositionTarget(float posTarget) {
    DynamicsJNI.btMultiBodyJointMotor_setPositionTarget__SWIG_1(swigCPtr, this, posTarget);
  }

  public void setErp(float erp) {
    DynamicsJNI.btMultiBodyJointMotor_setErp(swigCPtr, this, erp);
  }

  public float getErp() {
    return DynamicsJNI.btMultiBodyJointMotor_getErp(swigCPtr, this);
  }

  public void setRhsClamp(float rhsClamp) {
    DynamicsJNI.btMultiBodyJointMotor_setRhsClamp(swigCPtr, this, rhsClamp);
  }

}
