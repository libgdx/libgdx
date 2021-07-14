/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
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

public class btBvhSubtreeInfoData extends BulletBase {
	private long swigCPtr;
	
	protected btBvhSubtreeInfoData(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btBvhSubtreeInfoData, normally you should not need this constructor it's intended for low-level usage. */ 
	public btBvhSubtreeInfoData(long cPtr, boolean cMemoryOwn) {
		this("btBvhSubtreeInfoData", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btBvhSubtreeInfoData obj) {
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
				CollisionJNI.delete_btBvhSubtreeInfoData(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setRootNodeIndex(int value) {
    CollisionJNI.btBvhSubtreeInfoData_rootNodeIndex_set(swigCPtr, this, value);
  }

  public int getRootNodeIndex() {
    return CollisionJNI.btBvhSubtreeInfoData_rootNodeIndex_get(swigCPtr, this);
  }

  public void setSubtreeSize(int value) {
    CollisionJNI.btBvhSubtreeInfoData_subtreeSize_set(swigCPtr, this, value);
  }

  public int getSubtreeSize() {
    return CollisionJNI.btBvhSubtreeInfoData_subtreeSize_get(swigCPtr, this);
  }

  public void setQuantizedAabbMin(int[] value) {
    CollisionJNI.btBvhSubtreeInfoData_quantizedAabbMin_set(swigCPtr, this, value);
  }

  public int[] getQuantizedAabbMin() {
    return CollisionJNI.btBvhSubtreeInfoData_quantizedAabbMin_get(swigCPtr, this);
  }

  public void setQuantizedAabbMax(int[] value) {
    CollisionJNI.btBvhSubtreeInfoData_quantizedAabbMax_set(swigCPtr, this, value);
  }

  public int[] getQuantizedAabbMax() {
    return CollisionJNI.btBvhSubtreeInfoData_quantizedAabbMax_get(swigCPtr, this);
  }

  public btBvhSubtreeInfoData() {
    this(CollisionJNI.new_btBvhSubtreeInfoData(), true);
  }

}
