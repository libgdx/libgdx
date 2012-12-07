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

public class btCompoundShape extends btCollisionShape {
  private long swigCPtr;

  protected btCompoundShape(long cPtr, boolean cMemoryOwn) {
    super(gdxBulletJNI.btCompoundShape_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(btCompoundShape obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btCompoundShape(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public btCompoundShape(boolean enableDynamicAabbTree) {
    this(gdxBulletJNI.new_btCompoundShape__SWIG_0(enableDynamicAabbTree), true);
  }

  public btCompoundShape() {
    this(gdxBulletJNI.new_btCompoundShape__SWIG_1(), true);
  }

  public void addChildShape(Matrix4 localTransform, btCollisionShape shape) {
    gdxBulletJNI.btCompoundShape_addChildShape(swigCPtr, this, localTransform, btCollisionShape.getCPtr(shape), shape);
  }

  public void removeChildShape(btCollisionShape shape) {
    gdxBulletJNI.btCompoundShape_removeChildShape(swigCPtr, this, btCollisionShape.getCPtr(shape), shape);
  }

  public void removeChildShapeByIndex(int childShapeindex) {
    gdxBulletJNI.btCompoundShape_removeChildShapeByIndex(swigCPtr, this, childShapeindex);
  }

  public int getNumChildShapes() {
    return gdxBulletJNI.btCompoundShape_getNumChildShapes(swigCPtr, this);
  }

  public btCollisionShape getChildShape(int index) {
    long cPtr = gdxBulletJNI.btCompoundShape_getChildShape__SWIG_0(swigCPtr, this, index);
    return (cPtr == 0) ? null : btCollisionShape.newDerivedObject(cPtr, false);
  }

  public Matrix4 getChildTransform(int index) {
	return gdxBulletJNI.btCompoundShape_getChildTransform__SWIG_0(swigCPtr, this, index);
}

  public void updateChildTransform(int childIndex, Matrix4 newChildTransform, boolean shouldRecalculateLocalAabb) {
    gdxBulletJNI.btCompoundShape_updateChildTransform__SWIG_0(swigCPtr, this, childIndex, newChildTransform, shouldRecalculateLocalAabb);
  }

  public void updateChildTransform(int childIndex, Matrix4 newChildTransform) {
    gdxBulletJNI.btCompoundShape_updateChildTransform__SWIG_1(swigCPtr, this, childIndex, newChildTransform);
  }

  public btCompoundShapeChild getChildList() {
    long cPtr = gdxBulletJNI.btCompoundShape_getChildList(swigCPtr, this);
    return (cPtr == 0) ? null : new btCompoundShapeChild(cPtr, false);
  }

  public void recalculateLocalAabb() {
    gdxBulletJNI.btCompoundShape_recalculateLocalAabb(swigCPtr, this);
  }

  public btDbvt getDynamicAabbTree() {
    long cPtr = gdxBulletJNI.btCompoundShape_getDynamicAabbTree__SWIG_0(swigCPtr, this);
    return (cPtr == 0) ? null : new btDbvt(cPtr, false);
  }

  public void createAabbTreeFromChildren() {
    gdxBulletJNI.btCompoundShape_createAabbTreeFromChildren(swigCPtr, this);
  }

  public void calculatePrincipalAxisTransform(float[] masses, Matrix4 principal, Vector3 inertia) {
    gdxBulletJNI.btCompoundShape_calculatePrincipalAxisTransform(swigCPtr, this, masses, principal, inertia);
  }

  public int getUpdateRevision() {
    return gdxBulletJNI.btCompoundShape_getUpdateRevision(swigCPtr, this);
  }

}
