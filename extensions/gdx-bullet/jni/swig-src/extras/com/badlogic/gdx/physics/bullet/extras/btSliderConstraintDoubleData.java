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

public class btSliderConstraintDoubleData extends BulletBase {
	private long swigCPtr;
	
	protected btSliderConstraintDoubleData(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btSliderConstraintDoubleData, normally you should not need this constructor it's intended for low-level usage. */ 
	public btSliderConstraintDoubleData(long cPtr, boolean cMemoryOwn) {
		this("btSliderConstraintDoubleData", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btSliderConstraintDoubleData obj) {
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
				ExtrasJNI.delete_btSliderConstraintDoubleData(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setTypeConstraintData(btTypedConstraintDoubleData value) {
    ExtrasJNI.btSliderConstraintDoubleData_typeConstraintData_set(swigCPtr, this, btTypedConstraintDoubleData.getCPtr(value), value);
  }

  public btTypedConstraintDoubleData getTypeConstraintData() {
    long cPtr = ExtrasJNI.btSliderConstraintDoubleData_typeConstraintData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btTypedConstraintDoubleData(cPtr, false);
  }

  public void setRbAFrame(btTransformDoubleData value) {
    ExtrasJNI.btSliderConstraintDoubleData_rbAFrame_set(swigCPtr, this, btTransformDoubleData.getCPtr(value), value);
  }

  public btTransformDoubleData getRbAFrame() {
    long cPtr = ExtrasJNI.btSliderConstraintDoubleData_rbAFrame_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btTransformDoubleData(cPtr, false);
  }

  public void setRbBFrame(btTransformDoubleData value) {
    ExtrasJNI.btSliderConstraintDoubleData_rbBFrame_set(swigCPtr, this, btTransformDoubleData.getCPtr(value), value);
  }

  public btTransformDoubleData getRbBFrame() {
    long cPtr = ExtrasJNI.btSliderConstraintDoubleData_rbBFrame_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btTransformDoubleData(cPtr, false);
  }

  public void setLinearUpperLimit(double value) {
    ExtrasJNI.btSliderConstraintDoubleData_linearUpperLimit_set(swigCPtr, this, value);
  }

  public double getLinearUpperLimit() {
    return ExtrasJNI.btSliderConstraintDoubleData_linearUpperLimit_get(swigCPtr, this);
  }

  public void setLinearLowerLimit(double value) {
    ExtrasJNI.btSliderConstraintDoubleData_linearLowerLimit_set(swigCPtr, this, value);
  }

  public double getLinearLowerLimit() {
    return ExtrasJNI.btSliderConstraintDoubleData_linearLowerLimit_get(swigCPtr, this);
  }

  public void setAngularUpperLimit(double value) {
    ExtrasJNI.btSliderConstraintDoubleData_angularUpperLimit_set(swigCPtr, this, value);
  }

  public double getAngularUpperLimit() {
    return ExtrasJNI.btSliderConstraintDoubleData_angularUpperLimit_get(swigCPtr, this);
  }

  public void setAngularLowerLimit(double value) {
    ExtrasJNI.btSliderConstraintDoubleData_angularLowerLimit_set(swigCPtr, this, value);
  }

  public double getAngularLowerLimit() {
    return ExtrasJNI.btSliderConstraintDoubleData_angularLowerLimit_get(swigCPtr, this);
  }

  public void setUseLinearReferenceFrameA(int value) {
    ExtrasJNI.btSliderConstraintDoubleData_useLinearReferenceFrameA_set(swigCPtr, this, value);
  }

  public int getUseLinearReferenceFrameA() {
    return ExtrasJNI.btSliderConstraintDoubleData_useLinearReferenceFrameA_get(swigCPtr, this);
  }

  public void setUseOffsetForConstraintFrame(int value) {
    ExtrasJNI.btSliderConstraintDoubleData_useOffsetForConstraintFrame_set(swigCPtr, this, value);
  }

  public int getUseOffsetForConstraintFrame() {
    return ExtrasJNI.btSliderConstraintDoubleData_useOffsetForConstraintFrame_get(swigCPtr, this);
  }

  public btSliderConstraintDoubleData() {
    this(ExtrasJNI.new_btSliderConstraintDoubleData(), true);
  }

}
