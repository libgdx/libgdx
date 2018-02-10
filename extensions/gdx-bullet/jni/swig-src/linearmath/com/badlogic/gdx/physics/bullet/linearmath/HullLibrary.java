/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
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

public class HullLibrary extends BulletBase {
	private long swigCPtr;
	
	protected HullLibrary(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new HullLibrary, normally you should not need this constructor it's intended for low-level usage. */ 
	public HullLibrary(long cPtr, boolean cMemoryOwn) {
		this("HullLibrary", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(HullLibrary obj) {
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
				LinearMathJNI.delete_HullLibrary(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setVertexIndexMapping(SWIGTYPE_p_btAlignedObjectArrayT_int_t value) {
    LinearMathJNI.HullLibrary_vertexIndexMapping_set(swigCPtr, this, SWIGTYPE_p_btAlignedObjectArrayT_int_t.getCPtr(value));
  }

  public SWIGTYPE_p_btAlignedObjectArrayT_int_t getVertexIndexMapping() {
    long cPtr = LinearMathJNI.HullLibrary_vertexIndexMapping_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_btAlignedObjectArrayT_int_t(cPtr, false);
  }

  public int CreateConvexHull(HullDesc desc, HullResult result) {
    return LinearMathJNI.HullLibrary_CreateConvexHull(swigCPtr, this, HullDesc.getCPtr(desc), desc, HullResult.getCPtr(result), result);
  }

  public int ReleaseResult(HullResult result) {
    return LinearMathJNI.HullLibrary_ReleaseResult(swigCPtr, this, HullResult.getCPtr(result), result);
  }

  public HullLibrary() {
    this(LinearMathJNI.new_HullLibrary(), true);
  }

}
