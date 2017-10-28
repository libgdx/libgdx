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

public class btOptimizedBvhNodeFloatData extends BulletBase {
	private long swigCPtr;
	
	protected btOptimizedBvhNodeFloatData(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btOptimizedBvhNodeFloatData, normally you should not need this constructor it's intended for low-level usage. */ 
	public btOptimizedBvhNodeFloatData(long cPtr, boolean cMemoryOwn) {
		this("btOptimizedBvhNodeFloatData", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btOptimizedBvhNodeFloatData obj) {
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
				ExtrasJNI.delete_btOptimizedBvhNodeFloatData(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setAabbMinOrg(btVector3FloatData value) {
    ExtrasJNI.btOptimizedBvhNodeFloatData_aabbMinOrg_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getAabbMinOrg() {
    long cPtr = ExtrasJNI.btOptimizedBvhNodeFloatData_aabbMinOrg_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setAabbMaxOrg(btVector3FloatData value) {
    ExtrasJNI.btOptimizedBvhNodeFloatData_aabbMaxOrg_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getAabbMaxOrg() {
    long cPtr = ExtrasJNI.btOptimizedBvhNodeFloatData_aabbMaxOrg_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setEscapeIndex(int value) {
    ExtrasJNI.btOptimizedBvhNodeFloatData_escapeIndex_set(swigCPtr, this, value);
  }

  public int getEscapeIndex() {
    return ExtrasJNI.btOptimizedBvhNodeFloatData_escapeIndex_get(swigCPtr, this);
  }

  public void setSubPart(int value) {
    ExtrasJNI.btOptimizedBvhNodeFloatData_subPart_set(swigCPtr, this, value);
  }

  public int getSubPart() {
    return ExtrasJNI.btOptimizedBvhNodeFloatData_subPart_get(swigCPtr, this);
  }

  public void setTriangleIndex(int value) {
    ExtrasJNI.btOptimizedBvhNodeFloatData_triangleIndex_set(swigCPtr, this, value);
  }

  public int getTriangleIndex() {
    return ExtrasJNI.btOptimizedBvhNodeFloatData_triangleIndex_get(swigCPtr, this);
  }

  public void setPad(String value) {
    ExtrasJNI.btOptimizedBvhNodeFloatData_pad_set(swigCPtr, this, value);
  }

  public String getPad() {
    return ExtrasJNI.btOptimizedBvhNodeFloatData_pad_get(swigCPtr, this);
  }

  public btOptimizedBvhNodeFloatData() {
    this(ExtrasJNI.new_btOptimizedBvhNodeFloatData(), true);
  }

}
