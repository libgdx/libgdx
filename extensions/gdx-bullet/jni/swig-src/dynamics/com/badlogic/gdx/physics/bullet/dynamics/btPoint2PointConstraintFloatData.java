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

public class btPoint2PointConstraintFloatData extends BulletBase {
	private long swigCPtr;
	
	protected btPoint2PointConstraintFloatData(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btPoint2PointConstraintFloatData, normally you should not need this constructor it's intended for low-level usage. */ 
	public btPoint2PointConstraintFloatData(long cPtr, boolean cMemoryOwn) {
		this("btPoint2PointConstraintFloatData", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btPoint2PointConstraintFloatData obj) {
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
				DynamicsJNI.delete_btPoint2PointConstraintFloatData(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setTypeConstraintData(btTypedConstraintData value) {
    DynamicsJNI.btPoint2PointConstraintFloatData_typeConstraintData_set(swigCPtr, this, btTypedConstraintData.getCPtr(value), value);
  }

  public btTypedConstraintData getTypeConstraintData() {
    long cPtr = DynamicsJNI.btPoint2PointConstraintFloatData_typeConstraintData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btTypedConstraintData(cPtr, false);
  }

  public void setPivotInA(btVector3FloatData value) {
    DynamicsJNI.btPoint2PointConstraintFloatData_pivotInA_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getPivotInA() {
    long cPtr = DynamicsJNI.btPoint2PointConstraintFloatData_pivotInA_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setPivotInB(btVector3FloatData value) {
    DynamicsJNI.btPoint2PointConstraintFloatData_pivotInB_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getPivotInB() {
    long cPtr = DynamicsJNI.btPoint2PointConstraintFloatData_pivotInB_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public btPoint2PointConstraintFloatData() {
    this(DynamicsJNI.new_btPoint2PointConstraintFloatData(), true);
  }

}
