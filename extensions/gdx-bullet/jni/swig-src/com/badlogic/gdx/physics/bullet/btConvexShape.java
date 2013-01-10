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

public class btConvexShape extends btCollisionShape {
  private long swigCPtr;

  protected btConvexShape(long cPtr, boolean cMemoryOwn) {
    super(gdxBulletJNI.btConvexShape_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(btConvexShape obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btConvexShape(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public Vector3 localGetSupportingVertex(Vector3 vec) {
	return gdxBulletJNI.btConvexShape_localGetSupportingVertex(swigCPtr, this, vec);
}

  public Vector3 localGetSupportingVertexWithoutMargin(Vector3 vec) {
	return gdxBulletJNI.btConvexShape_localGetSupportingVertexWithoutMargin(swigCPtr, this, vec);
}

  public Vector3 localGetSupportVertexWithoutMarginNonVirtual(Vector3 vec) {
	return gdxBulletJNI.btConvexShape_localGetSupportVertexWithoutMarginNonVirtual(swigCPtr, this, vec);
}

  public Vector3 localGetSupportVertexNonVirtual(Vector3 vec) {
	return gdxBulletJNI.btConvexShape_localGetSupportVertexNonVirtual(swigCPtr, this, vec);
}

  public float getMarginNonVirtual() {
    return gdxBulletJNI.btConvexShape_getMarginNonVirtual(swigCPtr, this);
  }

  public void getAabbNonVirtual(Matrix4 t, Vector3 aabbMin, Vector3 aabbMax) {
    gdxBulletJNI.btConvexShape_getAabbNonVirtual(swigCPtr, this, t, aabbMin, aabbMax);
  }

  public void project(Matrix4 trans, Vector3 dir, SWIGTYPE_p_float min, SWIGTYPE_p_float max) {
    gdxBulletJNI.btConvexShape_project(swigCPtr, this, trans, dir, SWIGTYPE_p_float.getCPtr(min), SWIGTYPE_p_float.getCPtr(max));
  }

  public void batchedUnitVectorGetSupportingVertexWithoutMargin(btVector3 vectors, btVector3 supportVerticesOut, int numVectors) {
    gdxBulletJNI.btConvexShape_batchedUnitVectorGetSupportingVertexWithoutMargin(swigCPtr, this, btVector3.getCPtr(vectors), vectors, btVector3.getCPtr(supportVerticesOut), supportVerticesOut, numVectors);
  }

  public void getAabbSlow(Matrix4 t, Vector3 aabbMin, Vector3 aabbMax) {
    gdxBulletJNI.btConvexShape_getAabbSlow(swigCPtr, this, t, aabbMin, aabbMax);
  }

  public int getNumPreferredPenetrationDirections() {
    return gdxBulletJNI.btConvexShape_getNumPreferredPenetrationDirections(swigCPtr, this);
  }

  public void getPreferredPenetrationDirection(int index, Vector3 penetrationVector) {
    gdxBulletJNI.btConvexShape_getPreferredPenetrationDirection(swigCPtr, this, index, penetrationVector);
  }

}
