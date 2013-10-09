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

public class btConvexPenetrationDepthSolver extends BulletBase {
	private long swigCPtr;
	
	protected btConvexPenetrationDepthSolver(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btConvexPenetrationDepthSolver(long cPtr, boolean cMemoryOwn) {
		this("btConvexPenetrationDepthSolver", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btConvexPenetrationDepthSolver obj) {
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
				gdxBulletJNI.delete_btConvexPenetrationDepthSolver(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public boolean calcPenDepth(SWIGTYPE_p_btSimplexSolverInterface simplexSolver, btConvexShape convexA, btConvexShape convexB, Matrix4 transA, Matrix4 transB, Vector3 v, Vector3 pa, Vector3 pb, btIDebugDraw debugDraw, btStackAlloc stackAlloc) {
    return gdxBulletJNI.btConvexPenetrationDepthSolver_calcPenDepth(swigCPtr, this, SWIGTYPE_p_btSimplexSolverInterface.getCPtr(simplexSolver), btConvexShape.getCPtr(convexA), convexA, btConvexShape.getCPtr(convexB), convexB, transA, transB, v, pa, pb, btIDebugDraw.getCPtr(debugDraw), debugDraw, btStackAlloc.getCPtr(stackAlloc), stackAlloc);
  }

}
