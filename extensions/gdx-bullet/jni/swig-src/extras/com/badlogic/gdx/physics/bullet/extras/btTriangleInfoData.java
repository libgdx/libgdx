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

public class btTriangleInfoData extends BulletBase {
	private long swigCPtr;
	
	protected btTriangleInfoData(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btTriangleInfoData, normally you should not need this constructor it's intended for low-level usage. */ 
	public btTriangleInfoData(long cPtr, boolean cMemoryOwn) {
		this("btTriangleInfoData", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btTriangleInfoData obj) {
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
				ExtrasJNI.delete_btTriangleInfoData(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setFlags(int value) {
    ExtrasJNI.btTriangleInfoData_flags_set(swigCPtr, this, value);
  }

  public int getFlags() {
    return ExtrasJNI.btTriangleInfoData_flags_get(swigCPtr, this);
  }

  public void setEdgeV0V1Angle(float value) {
    ExtrasJNI.btTriangleInfoData_edgeV0V1Angle_set(swigCPtr, this, value);
  }

  public float getEdgeV0V1Angle() {
    return ExtrasJNI.btTriangleInfoData_edgeV0V1Angle_get(swigCPtr, this);
  }

  public void setEdgeV1V2Angle(float value) {
    ExtrasJNI.btTriangleInfoData_edgeV1V2Angle_set(swigCPtr, this, value);
  }

  public float getEdgeV1V2Angle() {
    return ExtrasJNI.btTriangleInfoData_edgeV1V2Angle_get(swigCPtr, this);
  }

  public void setEdgeV2V0Angle(float value) {
    ExtrasJNI.btTriangleInfoData_edgeV2V0Angle_set(swigCPtr, this, value);
  }

  public float getEdgeV2V0Angle() {
    return ExtrasJNI.btTriangleInfoData_edgeV2V0Angle_get(swigCPtr, this);
  }

  public btTriangleInfoData() {
    this(ExtrasJNI.new_btTriangleInfoData(), true);
  }

}
