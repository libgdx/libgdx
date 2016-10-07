/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.10
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

public class btGeneric6DofConstraintDoubleData2 extends BulletBase {
	private long swigCPtr;
	
	protected btGeneric6DofConstraintDoubleData2(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btGeneric6DofConstraintDoubleData2, normally you should not need this constructor it's intended for low-level usage. */ 
	public btGeneric6DofConstraintDoubleData2(long cPtr, boolean cMemoryOwn) {
		this("btGeneric6DofConstraintDoubleData2", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btGeneric6DofConstraintDoubleData2 obj) {
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
				DynamicsJNI.delete_btGeneric6DofConstraintDoubleData2(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setTypeConstraintData(btTypedConstraintDoubleData value) {
    DynamicsJNI.btGeneric6DofConstraintDoubleData2_typeConstraintData_set(swigCPtr, this, btTypedConstraintDoubleData.getCPtr(value), value);
  }

  public btTypedConstraintDoubleData getTypeConstraintData() {
    long cPtr = DynamicsJNI.btGeneric6DofConstraintDoubleData2_typeConstraintData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btTypedConstraintDoubleData(cPtr, false);
  }

  public void setRbAFrame(btTransformDoubleData value) {
    DynamicsJNI.btGeneric6DofConstraintDoubleData2_rbAFrame_set(swigCPtr, this, btTransformDoubleData.getCPtr(value), value);
  }

  public btTransformDoubleData getRbAFrame() {
    long cPtr = DynamicsJNI.btGeneric6DofConstraintDoubleData2_rbAFrame_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btTransformDoubleData(cPtr, false);
  }

  public void setRbBFrame(btTransformDoubleData value) {
    DynamicsJNI.btGeneric6DofConstraintDoubleData2_rbBFrame_set(swigCPtr, this, btTransformDoubleData.getCPtr(value), value);
  }

  public btTransformDoubleData getRbBFrame() {
    long cPtr = DynamicsJNI.btGeneric6DofConstraintDoubleData2_rbBFrame_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btTransformDoubleData(cPtr, false);
  }

  public void setLinearUpperLimit(btVector3DoubleData value) {
    DynamicsJNI.btGeneric6DofConstraintDoubleData2_linearUpperLimit_set(swigCPtr, this, btVector3DoubleData.getCPtr(value), value);
  }

  public btVector3DoubleData getLinearUpperLimit() {
    long cPtr = DynamicsJNI.btGeneric6DofConstraintDoubleData2_linearUpperLimit_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3DoubleData(cPtr, false);
  }

  public void setLinearLowerLimit(btVector3DoubleData value) {
    DynamicsJNI.btGeneric6DofConstraintDoubleData2_linearLowerLimit_set(swigCPtr, this, btVector3DoubleData.getCPtr(value), value);
  }

  public btVector3DoubleData getLinearLowerLimit() {
    long cPtr = DynamicsJNI.btGeneric6DofConstraintDoubleData2_linearLowerLimit_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3DoubleData(cPtr, false);
  }

  public void setAngularUpperLimit(btVector3DoubleData value) {
    DynamicsJNI.btGeneric6DofConstraintDoubleData2_angularUpperLimit_set(swigCPtr, this, btVector3DoubleData.getCPtr(value), value);
  }

  public btVector3DoubleData getAngularUpperLimit() {
    long cPtr = DynamicsJNI.btGeneric6DofConstraintDoubleData2_angularUpperLimit_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3DoubleData(cPtr, false);
  }

  public void setAngularLowerLimit(btVector3DoubleData value) {
    DynamicsJNI.btGeneric6DofConstraintDoubleData2_angularLowerLimit_set(swigCPtr, this, btVector3DoubleData.getCPtr(value), value);
  }

  public btVector3DoubleData getAngularLowerLimit() {
    long cPtr = DynamicsJNI.btGeneric6DofConstraintDoubleData2_angularLowerLimit_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3DoubleData(cPtr, false);
  }

  public void setUseLinearReferenceFrameA(int value) {
    DynamicsJNI.btGeneric6DofConstraintDoubleData2_useLinearReferenceFrameA_set(swigCPtr, this, value);
  }

  public int getUseLinearReferenceFrameA() {
    return DynamicsJNI.btGeneric6DofConstraintDoubleData2_useLinearReferenceFrameA_get(swigCPtr, this);
  }

  public void setUseOffsetForConstraintFrame(int value) {
    DynamicsJNI.btGeneric6DofConstraintDoubleData2_useOffsetForConstraintFrame_set(swigCPtr, this, value);
  }

  public int getUseOffsetForConstraintFrame() {
    return DynamicsJNI.btGeneric6DofConstraintDoubleData2_useOffsetForConstraintFrame_get(swigCPtr, this);
  }

  public btGeneric6DofConstraintDoubleData2() {
    this(DynamicsJNI.new_btGeneric6DofConstraintDoubleData2(), true);
  }

}
