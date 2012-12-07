/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.8
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btConvexPlaneCollisionAlgorithm extends btCollisionAlgorithm {
  private long swigCPtr;

  protected btConvexPlaneCollisionAlgorithm(long cPtr, boolean cMemoryOwn) {
    super(gdxBulletJNI.btConvexPlaneCollisionAlgorithm_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(btConvexPlaneCollisionAlgorithm obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btConvexPlaneCollisionAlgorithm(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public btConvexPlaneCollisionAlgorithm(btPersistentManifold mf, btCollisionAlgorithmConstructionInfo ci, SWIGTYPE_p_btCollisionObjectWrapper body0Wrap, SWIGTYPE_p_btCollisionObjectWrapper body1Wrap, boolean isSwapped, int numPerturbationIterations, int minimumPointsPerturbationThreshold) {
    this(gdxBulletJNI.new_btConvexPlaneCollisionAlgorithm(btPersistentManifold.getCPtr(mf), mf, btCollisionAlgorithmConstructionInfo.getCPtr(ci), ci, SWIGTYPE_p_btCollisionObjectWrapper.getCPtr(body0Wrap), SWIGTYPE_p_btCollisionObjectWrapper.getCPtr(body1Wrap), isSwapped, numPerturbationIterations, minimumPointsPerturbationThreshold), true);
  }

  public void collideSingleContact(Quaternion perturbeRot, SWIGTYPE_p_btCollisionObjectWrapper body0Wrap, SWIGTYPE_p_btCollisionObjectWrapper body1Wrap, btDispatcherInfo dispatchInfo, btManifoldResult resultOut) {
    gdxBulletJNI.btConvexPlaneCollisionAlgorithm_collideSingleContact(swigCPtr, this, perturbeRot, SWIGTYPE_p_btCollisionObjectWrapper.getCPtr(body0Wrap), SWIGTYPE_p_btCollisionObjectWrapper.getCPtr(body1Wrap), btDispatcherInfo.getCPtr(dispatchInfo), dispatchInfo, btManifoldResult.getCPtr(resultOut), resultOut);
  }

}
