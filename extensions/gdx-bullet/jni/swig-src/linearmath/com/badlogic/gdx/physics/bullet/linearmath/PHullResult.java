/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.linearmath;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class PHullResult extends BulletBase {
	private long swigCPtr;
	
	protected PHullResult(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new PHullResult, normally you should not need this constructor it's intended for low-level usage. */ 
	public PHullResult(long cPtr, boolean cMemoryOwn) {
		this("PHullResult", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(PHullResult obj) {
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
				LinearMathJNI.delete_PHullResult(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public PHullResult() {
    this(LinearMathJNI.new_PHullResult(), true);
  }

  public void setMVcount(long value) {
    LinearMathJNI.PHullResult_mVcount_set(swigCPtr, this, value);
  }

  public long getMVcount() {
    return LinearMathJNI.PHullResult_mVcount_get(swigCPtr, this);
  }

  public void setMIndexCount(long value) {
    LinearMathJNI.PHullResult_mIndexCount_set(swigCPtr, this, value);
  }

  public long getMIndexCount() {
    return LinearMathJNI.PHullResult_mIndexCount_get(swigCPtr, this);
  }

  public void setMFaceCount(long value) {
    LinearMathJNI.PHullResult_mFaceCount_set(swigCPtr, this, value);
  }

  public long getMFaceCount() {
    return LinearMathJNI.PHullResult_mFaceCount_get(swigCPtr, this);
  }

  public void setMVertices(btVector3 value) {
    LinearMathJNI.PHullResult_mVertices_set(swigCPtr, this, btVector3.getCPtr(value), value);
  }

  public btVector3 getMVertices() {
    long cPtr = LinearMathJNI.PHullResult_mVertices_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3(cPtr, false);
  }

  public void setIndices(SWIGTYPE_p_btAlignedObjectArrayT_unsigned_int_t value) {
    LinearMathJNI.PHullResult_Indices_set(swigCPtr, this, SWIGTYPE_p_btAlignedObjectArrayT_unsigned_int_t.getCPtr(value));
  }

  public SWIGTYPE_p_btAlignedObjectArrayT_unsigned_int_t getIndices() {
    long cPtr = LinearMathJNI.PHullResult_Indices_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_btAlignedObjectArrayT_unsigned_int_t(cPtr, false);
  }

}
