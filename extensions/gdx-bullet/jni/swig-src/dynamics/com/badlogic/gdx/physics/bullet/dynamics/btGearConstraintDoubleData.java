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

public class btGearConstraintDoubleData extends BulletBase {
	private long swigCPtr;
	
	protected btGearConstraintDoubleData(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btGearConstraintDoubleData, normally you should not need this constructor it's intended for low-level usage. */ 
	public btGearConstraintDoubleData(long cPtr, boolean cMemoryOwn) {
		this("btGearConstraintDoubleData", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btGearConstraintDoubleData obj) {
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
				DynamicsJNI.delete_btGearConstraintDoubleData(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setTypeConstraintData(btTypedConstraintDoubleData value) {
    DynamicsJNI.btGearConstraintDoubleData_typeConstraintData_set(swigCPtr, this, btTypedConstraintDoubleData.getCPtr(value), value);
  }

  public btTypedConstraintDoubleData getTypeConstraintData() {
    long cPtr = DynamicsJNI.btGearConstraintDoubleData_typeConstraintData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btTypedConstraintDoubleData(cPtr, false);
  }

  public void setAxisInA(btVector3DoubleData value) {
    DynamicsJNI.btGearConstraintDoubleData_axisInA_set(swigCPtr, this, btVector3DoubleData.getCPtr(value), value);
  }

  public btVector3DoubleData getAxisInA() {
    long cPtr = DynamicsJNI.btGearConstraintDoubleData_axisInA_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3DoubleData(cPtr, false);
  }

  public void setAxisInB(btVector3DoubleData value) {
    DynamicsJNI.btGearConstraintDoubleData_axisInB_set(swigCPtr, this, btVector3DoubleData.getCPtr(value), value);
  }

  public btVector3DoubleData getAxisInB() {
    long cPtr = DynamicsJNI.btGearConstraintDoubleData_axisInB_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3DoubleData(cPtr, false);
  }

  public void setRatio(double value) {
    DynamicsJNI.btGearConstraintDoubleData_ratio_set(swigCPtr, this, value);
  }

  public double getRatio() {
    return DynamicsJNI.btGearConstraintDoubleData_ratio_get(swigCPtr, this);
  }

  public btGearConstraintDoubleData() {
    this(DynamicsJNI.new_btGearConstraintDoubleData(), true);
  }

}
