/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btSoftBodySolver {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected btSoftBodySolver(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(btSoftBodySolver obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btSoftBodySolver(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public int getSolverType() {
    return gdxBulletJNI.btSoftBodySolver_getSolverType(swigCPtr, this);
  }

  public boolean checkInitialized() {
    return gdxBulletJNI.btSoftBodySolver_checkInitialized(swigCPtr, this);
  }

  public void optimize(SWIGTYPE_p_btAlignedObjectArrayT_btSoftBody_p_t softBodies, boolean forceUpdate) {
    gdxBulletJNI.btSoftBodySolver_optimize__SWIG_0(swigCPtr, this, SWIGTYPE_p_btAlignedObjectArrayT_btSoftBody_p_t.getCPtr(softBodies), forceUpdate);
  }

  public void optimize(SWIGTYPE_p_btAlignedObjectArrayT_btSoftBody_p_t softBodies) {
    gdxBulletJNI.btSoftBodySolver_optimize__SWIG_1(swigCPtr, this, SWIGTYPE_p_btAlignedObjectArrayT_btSoftBody_p_t.getCPtr(softBodies));
  }

  public void copyBackToSoftBodies(boolean bMove) {
    gdxBulletJNI.btSoftBodySolver_copyBackToSoftBodies__SWIG_0(swigCPtr, this, bMove);
  }

  public void copyBackToSoftBodies() {
    gdxBulletJNI.btSoftBodySolver_copyBackToSoftBodies__SWIG_1(swigCPtr, this);
  }

  public void predictMotion(float solverdt) {
    gdxBulletJNI.btSoftBodySolver_predictMotion(swigCPtr, this, solverdt);
  }

  public void solveConstraints(float solverdt) {
    gdxBulletJNI.btSoftBodySolver_solveConstraints(swigCPtr, this, solverdt);
  }

  public void updateSoftBodies() {
    gdxBulletJNI.btSoftBodySolver_updateSoftBodies(swigCPtr, this);
  }

  public void processCollision(btSoftBody arg0, SWIGTYPE_p_btCollisionObjectWrapper arg1) {
    gdxBulletJNI.btSoftBodySolver_processCollision__SWIG_0(swigCPtr, this, btSoftBody.getCPtr(arg0), arg0, SWIGTYPE_p_btCollisionObjectWrapper.getCPtr(arg1));
  }

  public void processCollision(btSoftBody arg0, btSoftBody arg1) {
    gdxBulletJNI.btSoftBodySolver_processCollision__SWIG_1(swigCPtr, this, btSoftBody.getCPtr(arg0), arg0, btSoftBody.getCPtr(arg1), arg1);
  }

  public void setNumberOfPositionIterations(int iterations) {
    gdxBulletJNI.btSoftBodySolver_setNumberOfPositionIterations(swigCPtr, this, iterations);
  }

  public int getNumberOfPositionIterations() {
    return gdxBulletJNI.btSoftBodySolver_getNumberOfPositionIterations(swigCPtr, this);
  }

  public void setNumberOfVelocityIterations(int iterations) {
    gdxBulletJNI.btSoftBodySolver_setNumberOfVelocityIterations(swigCPtr, this, iterations);
  }

  public int getNumberOfVelocityIterations() {
    return gdxBulletJNI.btSoftBodySolver_getNumberOfVelocityIterations(swigCPtr, this);
  }

  public float getTimeScale() {
    return gdxBulletJNI.btSoftBodySolver_getTimeScale(swigCPtr, this);
  }

  public final static class SolverTypes {
    public final static int DEFAULT_SOLVER = 0;
    public final static int CPU_SOLVER = DEFAULT_SOLVER + 1;
    public final static int CL_SOLVER = CPU_SOLVER + 1;
    public final static int CL_SIMD_SOLVER = CL_SOLVER + 1;
    public final static int DX_SOLVER = CL_SIMD_SOLVER + 1;
    public final static int DX_SIMD_SOLVER = DX_SOLVER + 1;
  }

}
