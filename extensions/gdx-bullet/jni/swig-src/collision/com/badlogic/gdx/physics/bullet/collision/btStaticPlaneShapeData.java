/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.8
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.collision;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btStaticPlaneShapeData extends BulletBase {
	private long swigCPtr;
	
	protected btStaticPlaneShapeData(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btStaticPlaneShapeData, normally you should not need this constructor it's intended for low-level usage. */ 
	public btStaticPlaneShapeData(long cPtr, boolean cMemoryOwn) {
		this("btStaticPlaneShapeData", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btStaticPlaneShapeData obj) {
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
				CollisionJNI.delete_btStaticPlaneShapeData(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setCollisionShapeData(btCollisionShapeData value) {
    CollisionJNI.btStaticPlaneShapeData_collisionShapeData_set(swigCPtr, this, btCollisionShapeData.getCPtr(value), value);
  }

  public btCollisionShapeData getCollisionShapeData() {
    long cPtr = CollisionJNI.btStaticPlaneShapeData_collisionShapeData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btCollisionShapeData(cPtr, false);
  }

  public void setLocalScaling(btVector3FloatData value) {
    CollisionJNI.btStaticPlaneShapeData_localScaling_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getLocalScaling() {
    long cPtr = CollisionJNI.btStaticPlaneShapeData_localScaling_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setPlaneNormal(btVector3FloatData value) {
    CollisionJNI.btStaticPlaneShapeData_planeNormal_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getPlaneNormal() {
    long cPtr = CollisionJNI.btStaticPlaneShapeData_planeNormal_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setPlaneConstant(float value) {
    CollisionJNI.btStaticPlaneShapeData_planeConstant_set(swigCPtr, this, value);
  }

  public float getPlaneConstant() {
    return CollisionJNI.btStaticPlaneShapeData_planeConstant_get(swigCPtr, this);
  }

  public void setPad(String value) {
    CollisionJNI.btStaticPlaneShapeData_pad_set(swigCPtr, this, value);
  }

  public String getPad() {
    return CollisionJNI.btStaticPlaneShapeData_pad_get(swigCPtr, this);
  }

  public btStaticPlaneShapeData() {
    this(CollisionJNI.new_btStaticPlaneShapeData(), true);
  }

}
