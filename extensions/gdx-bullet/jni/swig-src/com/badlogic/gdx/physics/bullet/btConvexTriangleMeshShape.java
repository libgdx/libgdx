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

public class btConvexTriangleMeshShape extends btPolyhedralConvexAabbCachingShape {
  private long swigCPtr;

  protected btConvexTriangleMeshShape(long cPtr, boolean cMemoryOwn) {
    super(gdxBulletJNI.btConvexTriangleMeshShape_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(btConvexTriangleMeshShape obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btConvexTriangleMeshShape(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public btConvexTriangleMeshShape(btStridingMeshInterface meshInterface, boolean calcAabb) {
    this(gdxBulletJNI.new_btConvexTriangleMeshShape__SWIG_0(btStridingMeshInterface.getCPtr(meshInterface), meshInterface, calcAabb), true);
  }

  public btConvexTriangleMeshShape(btStridingMeshInterface meshInterface) {
    this(gdxBulletJNI.new_btConvexTriangleMeshShape__SWIG_1(btStridingMeshInterface.getCPtr(meshInterface), meshInterface), true);
  }

  public btStridingMeshInterface getMeshInterface() {
    long cPtr = gdxBulletJNI.btConvexTriangleMeshShape_getMeshInterface__SWIG_0(swigCPtr, this);
    return (cPtr == 0) ? null : new btStridingMeshInterface(cPtr, false);
  }

  public void calculatePrincipalAxisTransform(Matrix4 principal, Vector3 inertia, SWIGTYPE_p_float volume) {
    gdxBulletJNI.btConvexTriangleMeshShape_calculatePrincipalAxisTransform(swigCPtr, this, principal, inertia, SWIGTYPE_p_float.getCPtr(volume));
  }

}
