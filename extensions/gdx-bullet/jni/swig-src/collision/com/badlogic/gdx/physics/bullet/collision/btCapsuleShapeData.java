/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
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

public class btCapsuleShapeData extends BulletBase {
	private long swigCPtr;
	
	protected btCapsuleShapeData(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btCapsuleShapeData, normally you should not need this constructor it's intended for low-level usage. */ 
	public btCapsuleShapeData(long cPtr, boolean cMemoryOwn) {
		this("btCapsuleShapeData", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btCapsuleShapeData obj) {
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
				CollisionJNI.delete_btCapsuleShapeData(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setConvexInternalShapeData(btConvexInternalShapeData value) {
    CollisionJNI.btCapsuleShapeData_convexInternalShapeData_set(swigCPtr, this, btConvexInternalShapeData.getCPtr(value), value);
  }

  public btConvexInternalShapeData getConvexInternalShapeData() {
    long cPtr = CollisionJNI.btCapsuleShapeData_convexInternalShapeData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btConvexInternalShapeData(cPtr, false);
  }

  public void setUpAxis(int value) {
    CollisionJNI.btCapsuleShapeData_upAxis_set(swigCPtr, this, value);
  }

  public int getUpAxis() {
    return CollisionJNI.btCapsuleShapeData_upAxis_get(swigCPtr, this);
  }

  public void setPadding(String value) {
    CollisionJNI.btCapsuleShapeData_padding_set(swigCPtr, this, value);
  }

  public String getPadding() {
    return CollisionJNI.btCapsuleShapeData_padding_get(swigCPtr, this);
  }

  public btCapsuleShapeData() {
    this(CollisionJNI.new_btCapsuleShapeData(), true);
  }

}
