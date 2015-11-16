/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
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

public class btSolverConstraint extends BulletBase {
	private long swigCPtr;
	
	protected btSolverConstraint(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btSolverConstraint, normally you should not need this constructor it's intended for low-level usage. */ 
	public btSolverConstraint(long cPtr, boolean cMemoryOwn) {
		this("btSolverConstraint", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btSolverConstraint obj) {
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
				DynamicsJNI.delete_btSolverConstraint(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setRelpos1CrossNormal(btVector3 value) {
    DynamicsJNI.btSolverConstraint_relpos1CrossNormal_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getRelpos1CrossNormal() {
    long cPtr = DynamicsJNI.btSolverConstraint_relpos1CrossNormal_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setContactNormal1(btVector3 value) {
    DynamicsJNI.btSolverConstraint_contactNormal1_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getContactNormal1() {
    long cPtr = DynamicsJNI.btSolverConstraint_contactNormal1_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setRelpos2CrossNormal(btVector3 value) {
    DynamicsJNI.btSolverConstraint_relpos2CrossNormal_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getRelpos2CrossNormal() {
    long cPtr = DynamicsJNI.btSolverConstraint_relpos2CrossNormal_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setContactNormal2(btVector3 value) {
    DynamicsJNI.btSolverConstraint_contactNormal2_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getContactNormal2() {
    long cPtr = DynamicsJNI.btSolverConstraint_contactNormal2_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setAngularComponentA(btVector3 value) {
    DynamicsJNI.btSolverConstraint_angularComponentA_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getAngularComponentA() {
    long cPtr = DynamicsJNI.btSolverConstraint_angularComponentA_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setAngularComponentB(btVector3 value) {
    DynamicsJNI.btSolverConstraint_angularComponentB_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getAngularComponentB() {
    long cPtr = DynamicsJNI.btSolverConstraint_angularComponentB_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setAppliedPushImpulse(float value) {
    DynamicsJNI.btSolverConstraint_appliedPushImpulse_set(swigCPtr, this, value);
  }

  public float getAppliedPushImpulse() {
    return DynamicsJNI.btSolverConstraint_appliedPushImpulse_get(swigCPtr, this);
  }

  public void setAppliedImpulse(float value) {
    DynamicsJNI.btSolverConstraint_appliedImpulse_set(swigCPtr, this, value);
  }

  public float getAppliedImpulse() {
    return DynamicsJNI.btSolverConstraint_appliedImpulse_get(swigCPtr, this);
  }

  public void setFriction(float value) {
    DynamicsJNI.btSolverConstraint_friction_set(swigCPtr, this, value);
  }

  public float getFriction() {
    return DynamicsJNI.btSolverConstraint_friction_get(swigCPtr, this);
  }

  public void setJacDiagABInv(float value) {
    DynamicsJNI.btSolverConstraint_jacDiagABInv_set(swigCPtr, this, value);
  }

  public float getJacDiagABInv() {
    return DynamicsJNI.btSolverConstraint_jacDiagABInv_get(swigCPtr, this);
  }

  public void setRhs(float value) {
    DynamicsJNI.btSolverConstraint_rhs_set(swigCPtr, this, value);
  }

  public float getRhs() {
    return DynamicsJNI.btSolverConstraint_rhs_get(swigCPtr, this);
  }

  public void setCfm(float value) {
    DynamicsJNI.btSolverConstraint_cfm_set(swigCPtr, this, value);
  }

  public float getCfm() {
    return DynamicsJNI.btSolverConstraint_cfm_get(swigCPtr, this);
  }

  public void setLowerLimit(float value) {
    DynamicsJNI.btSolverConstraint_lowerLimit_set(swigCPtr, this, value);
  }

  public float getLowerLimit() {
    return DynamicsJNI.btSolverConstraint_lowerLimit_get(swigCPtr, this);
  }

  public void setUpperLimit(float value) {
    DynamicsJNI.btSolverConstraint_upperLimit_set(swigCPtr, this, value);
  }

  public float getUpperLimit() {
    return DynamicsJNI.btSolverConstraint_upperLimit_get(swigCPtr, this);
  }

  public void setRhsPenetration(float value) {
    DynamicsJNI.btSolverConstraint_rhsPenetration_set(swigCPtr, this, value);
  }

  public float getRhsPenetration() {
    return DynamicsJNI.btSolverConstraint_rhsPenetration_get(swigCPtr, this);
  }

  public void setOriginalContactPoint(long value) {
    DynamicsJNI.btSolverConstraint_originalContactPoint_set(swigCPtr, this, value);
  }

  public long getOriginalContactPoint() {
    return DynamicsJNI.btSolverConstraint_originalContactPoint_get(swigCPtr, this);
  }

  public void setUnusedPadding4(float value) {
    DynamicsJNI.btSolverConstraint_unusedPadding4_set(swigCPtr, this, value);
  }

  public float getUnusedPadding4() {
    return DynamicsJNI.btSolverConstraint_unusedPadding4_get(swigCPtr, this);
  }

  public void setNumRowsForNonContactConstraint(int value) {
    DynamicsJNI.btSolverConstraint_numRowsForNonContactConstraint_set(swigCPtr, this, value);
  }

  public int getNumRowsForNonContactConstraint() {
    return DynamicsJNI.btSolverConstraint_numRowsForNonContactConstraint_get(swigCPtr, this);
  }

  public void setOverrideNumSolverIterations(int value) {
    DynamicsJNI.btSolverConstraint_overrideNumSolverIterations_set(swigCPtr, this, value);
  }

  public int getOverrideNumSolverIterations() {
    return DynamicsJNI.btSolverConstraint_overrideNumSolverIterations_get(swigCPtr, this);
  }

  public void setFrictionIndex(int value) {
    DynamicsJNI.btSolverConstraint_frictionIndex_set(swigCPtr, this, value);
  }

  public int getFrictionIndex() {
    return DynamicsJNI.btSolverConstraint_frictionIndex_get(swigCPtr, this);
  }

  public void setSolverBodyIdA(int value) {
    DynamicsJNI.btSolverConstraint_solverBodyIdA_set(swigCPtr, this, value);
  }

  public int getSolverBodyIdA() {
    return DynamicsJNI.btSolverConstraint_solverBodyIdA_get(swigCPtr, this);
  }

  public void setSolverBodyIdB(int value) {
    DynamicsJNI.btSolverConstraint_solverBodyIdB_set(swigCPtr, this, value);
  }

  public int getSolverBodyIdB() {
    return DynamicsJNI.btSolverConstraint_solverBodyIdB_get(swigCPtr, this);
  }

  public btSolverConstraint() {
    this(DynamicsJNI.new_btSolverConstraint(), true);
  }

  public final static class btSolverConstraintType {
    public final static int BT_SOLVER_CONTACT_1D = 0;
    public final static int BT_SOLVER_FRICTION_1D = BT_SOLVER_CONTACT_1D + 1;
  }

}
