/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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

public class btTypedConstraintData extends BulletBase {
	private long swigCPtr;
	
	protected btTypedConstraintData(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btTypedConstraintData(long cPtr, boolean cMemoryOwn) {
		this("btTypedConstraintData", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(btTypedConstraintData obj) {
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
				gdxBulletJNI.delete_btTypedConstraintData(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setRbA(btRigidBodyFloatData value) {
    gdxBulletJNI.btTypedConstraintData_rbA_set(swigCPtr, this, btRigidBodyFloatData.getCPtr(value), value);
  }

  public btRigidBodyFloatData getRbA() {
    long cPtr = gdxBulletJNI.btTypedConstraintData_rbA_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btRigidBodyFloatData(cPtr, false);
  }

  public void setRbB(btRigidBodyFloatData value) {
    gdxBulletJNI.btTypedConstraintData_rbB_set(swigCPtr, this, btRigidBodyFloatData.getCPtr(value), value);
  }

  public btRigidBodyFloatData getRbB() {
    long cPtr = gdxBulletJNI.btTypedConstraintData_rbB_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btRigidBodyFloatData(cPtr, false);
  }

  public void setName(String value) {
    gdxBulletJNI.btTypedConstraintData_name_set(swigCPtr, this, value);
  }

  public String getName() {
    return gdxBulletJNI.btTypedConstraintData_name_get(swigCPtr, this);
  }

  public void setObjectType(int value) {
    gdxBulletJNI.btTypedConstraintData_objectType_set(swigCPtr, this, value);
  }

  public int getObjectType() {
    return gdxBulletJNI.btTypedConstraintData_objectType_get(swigCPtr, this);
  }

  public void setUserConstraintType(int value) {
    gdxBulletJNI.btTypedConstraintData_userConstraintType_set(swigCPtr, this, value);
  }

  public int getUserConstraintType() {
    return gdxBulletJNI.btTypedConstraintData_userConstraintType_get(swigCPtr, this);
  }

  public void setUserConstraintId(int value) {
    gdxBulletJNI.btTypedConstraintData_userConstraintId_set(swigCPtr, this, value);
  }

  public int getUserConstraintId() {
    return gdxBulletJNI.btTypedConstraintData_userConstraintId_get(swigCPtr, this);
  }

  public void setNeedsFeedback(int value) {
    gdxBulletJNI.btTypedConstraintData_needsFeedback_set(swigCPtr, this, value);
  }

  public int getNeedsFeedback() {
    return gdxBulletJNI.btTypedConstraintData_needsFeedback_get(swigCPtr, this);
  }

  public void setAppliedImpulse(float value) {
    gdxBulletJNI.btTypedConstraintData_appliedImpulse_set(swigCPtr, this, value);
  }

  public float getAppliedImpulse() {
    return gdxBulletJNI.btTypedConstraintData_appliedImpulse_get(swigCPtr, this);
  }

  public void setDbgDrawSize(float value) {
    gdxBulletJNI.btTypedConstraintData_dbgDrawSize_set(swigCPtr, this, value);
  }

  public float getDbgDrawSize() {
    return gdxBulletJNI.btTypedConstraintData_dbgDrawSize_get(swigCPtr, this);
  }

  public void setDisableCollisionsBetweenLinkedBodies(int value) {
    gdxBulletJNI.btTypedConstraintData_disableCollisionsBetweenLinkedBodies_set(swigCPtr, this, value);
  }

  public int getDisableCollisionsBetweenLinkedBodies() {
    return gdxBulletJNI.btTypedConstraintData_disableCollisionsBetweenLinkedBodies_get(swigCPtr, this);
  }

  public void setOverrideNumSolverIterations(int value) {
    gdxBulletJNI.btTypedConstraintData_overrideNumSolverIterations_set(swigCPtr, this, value);
  }

  public int getOverrideNumSolverIterations() {
    return gdxBulletJNI.btTypedConstraintData_overrideNumSolverIterations_get(swigCPtr, this);
  }

  public void setBreakingImpulseThreshold(float value) {
    gdxBulletJNI.btTypedConstraintData_breakingImpulseThreshold_set(swigCPtr, this, value);
  }

  public float getBreakingImpulseThreshold() {
    return gdxBulletJNI.btTypedConstraintData_breakingImpulseThreshold_get(swigCPtr, this);
  }

  public void setIsEnabled(int value) {
    gdxBulletJNI.btTypedConstraintData_isEnabled_set(swigCPtr, this, value);
  }

  public int getIsEnabled() {
    return gdxBulletJNI.btTypedConstraintData_isEnabled_get(swigCPtr, this);
  }

  public btTypedConstraintData() {
    this(gdxBulletJNI.new_btTypedConstraintData(), true);
  }

}
