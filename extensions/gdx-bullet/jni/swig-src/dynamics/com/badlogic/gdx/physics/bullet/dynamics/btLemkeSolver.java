/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
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

public class btLemkeSolver extends btMLCPSolverInterface {
	private long swigCPtr;
	
	protected btLemkeSolver(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, DynamicsJNI.btLemkeSolver_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btLemkeSolver, normally you should not need this constructor it's intended for low-level usage. */
	public btLemkeSolver(long cPtr, boolean cMemoryOwn) {
		this("btLemkeSolver", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(DynamicsJNI.btLemkeSolver_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btLemkeSolver obj) {
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
				DynamicsJNI.delete_btLemkeSolver(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setMaxValue(float value) {
    DynamicsJNI.btLemkeSolver_maxValue_set(swigCPtr, this, value);
  }

  public float getMaxValue() {
    return DynamicsJNI.btLemkeSolver_maxValue_get(swigCPtr, this);
  }

  public void setDebugLevel(int value) {
    DynamicsJNI.btLemkeSolver_debugLevel_set(swigCPtr, this, value);
  }

  public int getDebugLevel() {
    return DynamicsJNI.btLemkeSolver_debugLevel_get(swigCPtr, this);
  }

  public void setMaxLoops(int value) {
    DynamicsJNI.btLemkeSolver_maxLoops_set(swigCPtr, this, value);
  }

  public int getMaxLoops() {
    return DynamicsJNI.btLemkeSolver_maxLoops_get(swigCPtr, this);
  }

  public void setUseLoHighBounds(boolean value) {
    DynamicsJNI.btLemkeSolver_useLoHighBounds_set(swigCPtr, this, value);
  }

  public boolean getUseLoHighBounds() {
    return DynamicsJNI.btLemkeSolver_useLoHighBounds_get(swigCPtr, this);
  }

  public btLemkeSolver() {
    this(DynamicsJNI.new_btLemkeSolver(), true);
  }

  public boolean solveMLCP(SWIGTYPE_p_btMatrixXT_float_t A, SWIGTYPE_p_btVectorXT_float_t b, SWIGTYPE_p_btVectorXT_float_t x, SWIGTYPE_p_btVectorXT_float_t lo, SWIGTYPE_p_btVectorXT_float_t hi, SWIGTYPE_p_btAlignedObjectArrayT_int_t limitDependency, int numIterations, boolean useSparsity) {
    return DynamicsJNI.btLemkeSolver_solveMLCP__SWIG_0(swigCPtr, this, SWIGTYPE_p_btMatrixXT_float_t.getCPtr(A), SWIGTYPE_p_btVectorXT_float_t.getCPtr(b), SWIGTYPE_p_btVectorXT_float_t.getCPtr(x), SWIGTYPE_p_btVectorXT_float_t.getCPtr(lo), SWIGTYPE_p_btVectorXT_float_t.getCPtr(hi), SWIGTYPE_p_btAlignedObjectArrayT_int_t.getCPtr(limitDependency), numIterations, useSparsity);
  }

  public boolean solveMLCP(SWIGTYPE_p_btMatrixXT_float_t A, SWIGTYPE_p_btVectorXT_float_t b, SWIGTYPE_p_btVectorXT_float_t x, SWIGTYPE_p_btVectorXT_float_t lo, SWIGTYPE_p_btVectorXT_float_t hi, SWIGTYPE_p_btAlignedObjectArrayT_int_t limitDependency, int numIterations) {
    return DynamicsJNI.btLemkeSolver_solveMLCP__SWIG_1(swigCPtr, this, SWIGTYPE_p_btMatrixXT_float_t.getCPtr(A), SWIGTYPE_p_btVectorXT_float_t.getCPtr(b), SWIGTYPE_p_btVectorXT_float_t.getCPtr(x), SWIGTYPE_p_btVectorXT_float_t.getCPtr(lo), SWIGTYPE_p_btVectorXT_float_t.getCPtr(hi), SWIGTYPE_p_btAlignedObjectArrayT_int_t.getCPtr(limitDependency), numIterations);
  }

}
