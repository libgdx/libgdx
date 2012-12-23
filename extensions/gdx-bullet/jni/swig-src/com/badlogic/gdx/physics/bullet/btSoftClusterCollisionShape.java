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

public class btSoftClusterCollisionShape extends btConvexInternalShape {
  private long swigCPtr;

  protected btSoftClusterCollisionShape(long cPtr, boolean cMemoryOwn) {
    super(gdxBulletJNI.btSoftClusterCollisionShape_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(btSoftClusterCollisionShape obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btSoftClusterCollisionShape(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public void setM_cluster(SWIGTYPE_p_btSoftBody__Cluster value) {
    gdxBulletJNI.btSoftClusterCollisionShape_m_cluster_set(swigCPtr, this, SWIGTYPE_p_btSoftBody__Cluster.getCPtr(value));
  }

  public SWIGTYPE_p_btSoftBody__Cluster getM_cluster() {
    long cPtr = gdxBulletJNI.btSoftClusterCollisionShape_m_cluster_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_btSoftBody__Cluster(cPtr, false);
  }

  public btSoftClusterCollisionShape(SWIGTYPE_p_btSoftBody__Cluster cluster) {
    this(gdxBulletJNI.new_btSoftClusterCollisionShape(SWIGTYPE_p_btSoftBody__Cluster.getCPtr(cluster)), true);
  }

  public int getShapeType() {
    return gdxBulletJNI.btSoftClusterCollisionShape_getShapeType(swigCPtr, this);
  }

}
