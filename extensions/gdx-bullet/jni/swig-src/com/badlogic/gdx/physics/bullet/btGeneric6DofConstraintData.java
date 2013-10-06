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

public class btGeneric6DofConstraintData extends BulletBase {
	private long swigCPtr;
	
	protected btGeneric6DofConstraintData(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btGeneric6DofConstraintData(long cPtr, boolean cMemoryOwn) {
		this("btGeneric6DofConstraintData", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(btGeneric6DofConstraintData obj) {
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
				gdxBulletJNI.delete_btGeneric6DofConstraintData(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setTypeConstraintData(btTypedConstraintData value) {
    gdxBulletJNI.btGeneric6DofConstraintData_typeConstraintData_set(swigCPtr, this, btTypedConstraintData.getCPtr(value), value);
  }

  public btTypedConstraintData getTypeConstraintData() {
    long cPtr = gdxBulletJNI.btGeneric6DofConstraintData_typeConstraintData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btTypedConstraintData(cPtr, false);
  }

  public void setRbAFrame(btTransformFloatData value) {
    gdxBulletJNI.btGeneric6DofConstraintData_rbAFrame_set(swigCPtr, this, btTransformFloatData.getCPtr(value), value);
  }

  public btTransformFloatData getRbAFrame() {
    long cPtr = gdxBulletJNI.btGeneric6DofConstraintData_rbAFrame_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btTransformFloatData(cPtr, false);
  }

  public void setRbBFrame(btTransformFloatData value) {
    gdxBulletJNI.btGeneric6DofConstraintData_rbBFrame_set(swigCPtr, this, btTransformFloatData.getCPtr(value), value);
  }

  public btTransformFloatData getRbBFrame() {
    long cPtr = gdxBulletJNI.btGeneric6DofConstraintData_rbBFrame_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btTransformFloatData(cPtr, false);
  }

  public void setLinearUpperLimit(btVector3FloatData value) {
    gdxBulletJNI.btGeneric6DofConstraintData_linearUpperLimit_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getLinearUpperLimit() {
    long cPtr = gdxBulletJNI.btGeneric6DofConstraintData_linearUpperLimit_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setLinearLowerLimit(btVector3FloatData value) {
    gdxBulletJNI.btGeneric6DofConstraintData_linearLowerLimit_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getLinearLowerLimit() {
    long cPtr = gdxBulletJNI.btGeneric6DofConstraintData_linearLowerLimit_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setAngularUpperLimit(btVector3FloatData value) {
    gdxBulletJNI.btGeneric6DofConstraintData_angularUpperLimit_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getAngularUpperLimit() {
    long cPtr = gdxBulletJNI.btGeneric6DofConstraintData_angularUpperLimit_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setAngularLowerLimit(btVector3FloatData value) {
    gdxBulletJNI.btGeneric6DofConstraintData_angularLowerLimit_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getAngularLowerLimit() {
    long cPtr = gdxBulletJNI.btGeneric6DofConstraintData_angularLowerLimit_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setUseLinearReferenceFrameA(int value) {
    gdxBulletJNI.btGeneric6DofConstraintData_useLinearReferenceFrameA_set(swigCPtr, this, value);
  }

  public int getUseLinearReferenceFrameA() {
    return gdxBulletJNI.btGeneric6DofConstraintData_useLinearReferenceFrameA_get(swigCPtr, this);
  }

  public void setUseOffsetForConstraintFrame(int value) {
    gdxBulletJNI.btGeneric6DofConstraintData_useOffsetForConstraintFrame_set(swigCPtr, this, value);
  }

  public int getUseOffsetForConstraintFrame() {
    return gdxBulletJNI.btGeneric6DofConstraintData_useOffsetForConstraintFrame_get(swigCPtr, this);
  }

  public btGeneric6DofConstraintData() {
    this(gdxBulletJNI.new_btGeneric6DofConstraintData(), true);
  }

}
