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

public class btQuantizedBvhFloatData extends BulletBase {
	private long swigCPtr;
	
	protected btQuantizedBvhFloatData(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btQuantizedBvhFloatData(long cPtr, boolean cMemoryOwn) {
		this("btQuantizedBvhFloatData", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(btQuantizedBvhFloatData obj) {
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
				gdxBulletJNI.delete_btQuantizedBvhFloatData(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setBvhAabbMin(btVector3FloatData value) {
    gdxBulletJNI.btQuantizedBvhFloatData_bvhAabbMin_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getBvhAabbMin() {
    long cPtr = gdxBulletJNI.btQuantizedBvhFloatData_bvhAabbMin_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setBvhAabbMax(btVector3FloatData value) {
    gdxBulletJNI.btQuantizedBvhFloatData_bvhAabbMax_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getBvhAabbMax() {
    long cPtr = gdxBulletJNI.btQuantizedBvhFloatData_bvhAabbMax_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setBvhQuantization(btVector3FloatData value) {
    gdxBulletJNI.btQuantizedBvhFloatData_bvhQuantization_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getBvhQuantization() {
    long cPtr = gdxBulletJNI.btQuantizedBvhFloatData_bvhQuantization_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setCurNodeIndex(int value) {
    gdxBulletJNI.btQuantizedBvhFloatData_curNodeIndex_set(swigCPtr, this, value);
  }

  public int getCurNodeIndex() {
    return gdxBulletJNI.btQuantizedBvhFloatData_curNodeIndex_get(swigCPtr, this);
  }

  public void setUseQuantization(int value) {
    gdxBulletJNI.btQuantizedBvhFloatData_useQuantization_set(swigCPtr, this, value);
  }

  public int getUseQuantization() {
    return gdxBulletJNI.btQuantizedBvhFloatData_useQuantization_get(swigCPtr, this);
  }

  public void setNumContiguousLeafNodes(int value) {
    gdxBulletJNI.btQuantizedBvhFloatData_numContiguousLeafNodes_set(swigCPtr, this, value);
  }

  public int getNumContiguousLeafNodes() {
    return gdxBulletJNI.btQuantizedBvhFloatData_numContiguousLeafNodes_get(swigCPtr, this);
  }

  public void setNumQuantizedContiguousNodes(int value) {
    gdxBulletJNI.btQuantizedBvhFloatData_numQuantizedContiguousNodes_set(swigCPtr, this, value);
  }

  public int getNumQuantizedContiguousNodes() {
    return gdxBulletJNI.btQuantizedBvhFloatData_numQuantizedContiguousNodes_get(swigCPtr, this);
  }

  public void setContiguousNodesPtr(btOptimizedBvhNodeFloatData value) {
    gdxBulletJNI.btQuantizedBvhFloatData_contiguousNodesPtr_set(swigCPtr, this, btOptimizedBvhNodeFloatData.getCPtr(value), value);
  }

  public btOptimizedBvhNodeFloatData getContiguousNodesPtr() {
    long cPtr = gdxBulletJNI.btQuantizedBvhFloatData_contiguousNodesPtr_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btOptimizedBvhNodeFloatData(cPtr, false);
  }

  public void setQuantizedContiguousNodesPtr(btQuantizedBvhNodeData value) {
    gdxBulletJNI.btQuantizedBvhFloatData_quantizedContiguousNodesPtr_set(swigCPtr, this, btQuantizedBvhNodeData.getCPtr(value), value);
  }

  public btQuantizedBvhNodeData getQuantizedContiguousNodesPtr() {
    long cPtr = gdxBulletJNI.btQuantizedBvhFloatData_quantizedContiguousNodesPtr_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btQuantizedBvhNodeData(cPtr, false);
  }

  public void setSubTreeInfoPtr(btBvhSubtreeInfoData value) {
    gdxBulletJNI.btQuantizedBvhFloatData_subTreeInfoPtr_set(swigCPtr, this, btBvhSubtreeInfoData.getCPtr(value), value);
  }

  public btBvhSubtreeInfoData getSubTreeInfoPtr() {
    long cPtr = gdxBulletJNI.btQuantizedBvhFloatData_subTreeInfoPtr_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btBvhSubtreeInfoData(cPtr, false);
  }

  public void setTraversalMode(int value) {
    gdxBulletJNI.btQuantizedBvhFloatData_traversalMode_set(swigCPtr, this, value);
  }

  public int getTraversalMode() {
    return gdxBulletJNI.btQuantizedBvhFloatData_traversalMode_get(swigCPtr, this);
  }

  public void setNumSubtreeHeaders(int value) {
    gdxBulletJNI.btQuantizedBvhFloatData_numSubtreeHeaders_set(swigCPtr, this, value);
  }

  public int getNumSubtreeHeaders() {
    return gdxBulletJNI.btQuantizedBvhFloatData_numSubtreeHeaders_get(swigCPtr, this);
  }

  public btQuantizedBvhFloatData() {
    this(gdxBulletJNI.new_btQuantizedBvhFloatData(), true);
  }

}
