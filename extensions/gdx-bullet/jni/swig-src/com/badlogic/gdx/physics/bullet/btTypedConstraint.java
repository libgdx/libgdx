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

public class btTypedConstraint extends btTypedObject {
	private long swigCPtr;
	
	protected btTypedConstraint(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, gdxBulletJNI.btTypedConstraint_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btTypedConstraint(long cPtr, boolean cMemoryOwn) {
		this("btTypedConstraint", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(gdxBulletJNI.btTypedConstraint_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btTypedConstraint obj) {
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
				gdxBulletJNI.delete_btTypedConstraint(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public static btRigidBody getFixedBody() {
    return new btRigidBody(gdxBulletJNI.btTypedConstraint_getFixedBody(), false);
  }

  public int getOverrideNumSolverIterations() {
    return gdxBulletJNI.btTypedConstraint_getOverrideNumSolverIterations(swigCPtr, this);
  }

  public void setOverrideNumSolverIterations(int overideNumIterations) {
    gdxBulletJNI.btTypedConstraint_setOverrideNumSolverIterations(swigCPtr, this, overideNumIterations);
  }

  public void buildJacobian() {
    gdxBulletJNI.btTypedConstraint_buildJacobian(swigCPtr, this);
  }

  public void setupSolverConstraint(SWIGTYPE_p_btAlignedObjectArrayT_btSolverConstraint_t ca, int solverBodyA, int solverBodyB, float timeStep) {
    gdxBulletJNI.btTypedConstraint_setupSolverConstraint(swigCPtr, this, SWIGTYPE_p_btAlignedObjectArrayT_btSolverConstraint_t.getCPtr(ca), solverBodyA, solverBodyB, timeStep);
  }

  public void getInfo1(SWIGTYPE_p_btTypedConstraint__btConstraintInfo1 info) {
    gdxBulletJNI.btTypedConstraint_getInfo1(swigCPtr, this, SWIGTYPE_p_btTypedConstraint__btConstraintInfo1.getCPtr(info));
  }

  public void getInfo2(btConstraintInfo2 info) {
    gdxBulletJNI.btTypedConstraint_getInfo2(swigCPtr, this, btConstraintInfo2.getCPtr(info), info);
  }

  public void internalSetAppliedImpulse(float appliedImpulse) {
    gdxBulletJNI.btTypedConstraint_internalSetAppliedImpulse(swigCPtr, this, appliedImpulse);
  }

  public float internalGetAppliedImpulse() {
    return gdxBulletJNI.btTypedConstraint_internalGetAppliedImpulse(swigCPtr, this);
  }

  public float getBreakingImpulseThreshold() {
    return gdxBulletJNI.btTypedConstraint_getBreakingImpulseThreshold(swigCPtr, this);
  }

  public void setBreakingImpulseThreshold(float threshold) {
    gdxBulletJNI.btTypedConstraint_setBreakingImpulseThreshold(swigCPtr, this, threshold);
  }

  public boolean isEnabled() {
    return gdxBulletJNI.btTypedConstraint_isEnabled(swigCPtr, this);
  }

  public void setEnabled(boolean enabled) {
    gdxBulletJNI.btTypedConstraint_setEnabled(swigCPtr, this, enabled);
  }

  public void solveConstraintObsolete(btSolverBody arg0, btSolverBody arg1, float arg2) {
    gdxBulletJNI.btTypedConstraint_solveConstraintObsolete(swigCPtr, this, btSolverBody.getCPtr(arg0), arg0, btSolverBody.getCPtr(arg1), arg1, arg2);
  }

  public btRigidBody getRigidBodyA() {
    return new btRigidBody(gdxBulletJNI.btTypedConstraint_getRigidBodyA__SWIG_0(swigCPtr, this), false);
  }

  public btRigidBody getRigidBodyB() {
    return new btRigidBody(gdxBulletJNI.btTypedConstraint_getRigidBodyB__SWIG_0(swigCPtr, this), false);
  }

  public int getUserConstraintType() {
    return gdxBulletJNI.btTypedConstraint_getUserConstraintType(swigCPtr, this);
  }

  public void setUserConstraintType(int userConstraintType) {
    gdxBulletJNI.btTypedConstraint_setUserConstraintType(swigCPtr, this, userConstraintType);
  }

  public void setUserConstraintId(int uid) {
    gdxBulletJNI.btTypedConstraint_setUserConstraintId(swigCPtr, this, uid);
  }

  public int getUserConstraintId() {
    return gdxBulletJNI.btTypedConstraint_getUserConstraintId(swigCPtr, this);
  }

  public void setUserConstraintPtr(SWIGTYPE_p_void ptr) {
    gdxBulletJNI.btTypedConstraint_setUserConstraintPtr(swigCPtr, this, SWIGTYPE_p_void.getCPtr(ptr));
  }

  public SWIGTYPE_p_void getUserConstraintPtr() {
    long cPtr = gdxBulletJNI.btTypedConstraint_getUserConstraintPtr(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public void setJointFeedback(btJointFeedback jointFeedback) {
    gdxBulletJNI.btTypedConstraint_setJointFeedback(swigCPtr, this, btJointFeedback.getCPtr(jointFeedback), jointFeedback);
  }

  public btJointFeedback getJointFeedback() {
    long cPtr = gdxBulletJNI.btTypedConstraint_getJointFeedback__SWIG_0(swigCPtr, this);
    return (cPtr == 0) ? null : new btJointFeedback(cPtr, false);
  }

  public int getUid() {
    return gdxBulletJNI.btTypedConstraint_getUid(swigCPtr, this);
  }

  public boolean needsFeedback() {
    return gdxBulletJNI.btTypedConstraint_needsFeedback(swigCPtr, this);
  }

  public void enableFeedback(boolean needsFeedback) {
    gdxBulletJNI.btTypedConstraint_enableFeedback(swigCPtr, this, needsFeedback);
  }

  public float getAppliedImpulse() {
    return gdxBulletJNI.btTypedConstraint_getAppliedImpulse(swigCPtr, this);
  }

  public int getConstraintType() {
    return gdxBulletJNI.btTypedConstraint_getConstraintType(swigCPtr, this);
  }

  public void setDbgDrawSize(float dbgDrawSize) {
    gdxBulletJNI.btTypedConstraint_setDbgDrawSize(swigCPtr, this, dbgDrawSize);
  }

  public float getDbgDrawSize() {
    return gdxBulletJNI.btTypedConstraint_getDbgDrawSize(swigCPtr, this);
  }

  public void setParam(int num, float value, int axis) {
    gdxBulletJNI.btTypedConstraint_setParam__SWIG_0(swigCPtr, this, num, value, axis);
  }

  public void setParam(int num, float value) {
    gdxBulletJNI.btTypedConstraint_setParam__SWIG_1(swigCPtr, this, num, value);
  }

  public float getParam(int num, int axis) {
    return gdxBulletJNI.btTypedConstraint_getParam__SWIG_0(swigCPtr, this, num, axis);
  }

  public float getParam(int num) {
    return gdxBulletJNI.btTypedConstraint_getParam__SWIG_1(swigCPtr, this, num);
  }

  public int calculateSerializeBufferSize() {
    return gdxBulletJNI.btTypedConstraint_calculateSerializeBufferSize(swigCPtr, this);
  }

  public String serialize(SWIGTYPE_p_void dataBuffer, SWIGTYPE_p_btSerializer serializer) {
    return gdxBulletJNI.btTypedConstraint_serialize(swigCPtr, this, SWIGTYPE_p_void.getCPtr(dataBuffer), SWIGTYPE_p_btSerializer.getCPtr(serializer));
  }

}
