/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.collision;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btGImpactShapeInterface extends btConcaveShape {
	private long swigCPtr;
	
	protected btGImpactShapeInterface(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, CollisionJNI.btGImpactShapeInterface_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btGImpactShapeInterface, normally you should not need this constructor it's intended for low-level usage. */
	public btGImpactShapeInterface(long cPtr, boolean cMemoryOwn) {
		this("btGImpactShapeInterface", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(CollisionJNI.btGImpactShapeInterface_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btGImpactShapeInterface obj) {
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
				CollisionJNI.delete_btGImpactShapeInterface(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void updateBound() {
    CollisionJNI.btGImpactShapeInterface_updateBound(swigCPtr, this);
  }

  public void postUpdate() {
    CollisionJNI.btGImpactShapeInterface_postUpdate(swigCPtr, this);
  }

  public btAABB getLocalBox() {
    return new btAABB(CollisionJNI.btGImpactShapeInterface_getLocalBox(swigCPtr, this), false);
  }

  public int getShapeType() {
    return CollisionJNI.btGImpactShapeInterface_getShapeType(swigCPtr, this);
  }

  public int getGImpactShapeType() {
    return CollisionJNI.btGImpactShapeInterface_getGImpactShapeType(swigCPtr, this);
  }

  public btGImpactQuantizedBvh getBoxSet() {
    long cPtr = CollisionJNI.btGImpactShapeInterface_getBoxSet(swigCPtr, this);
    return (cPtr == 0) ? null : new btGImpactQuantizedBvh(cPtr, false);
  }

  public boolean hasBoxSet() {
    return CollisionJNI.btGImpactShapeInterface_hasBoxSet(swigCPtr, this);
  }

  public btPrimitiveManagerBase getPrimitiveManager() {
    long cPtr = CollisionJNI.btGImpactShapeInterface_getPrimitiveManager(swigCPtr, this);
    return (cPtr == 0) ? null : new btPrimitiveManagerBase(cPtr, false);
  }

  public int getNumChildShapes() {
    return CollisionJNI.btGImpactShapeInterface_getNumChildShapes(swigCPtr, this);
  }

  public boolean childrenHasTransform() {
    return CollisionJNI.btGImpactShapeInterface_childrenHasTransform(swigCPtr, this);
  }

  public boolean needsRetrieveTriangles() {
    return CollisionJNI.btGImpactShapeInterface_needsRetrieveTriangles(swigCPtr, this);
  }

  public boolean needsRetrieveTetrahedrons() {
    return CollisionJNI.btGImpactShapeInterface_needsRetrieveTetrahedrons(swigCPtr, this);
  }

  public void getBulletTriangle(int prim_index, btTriangleShapeEx triangle) {
    CollisionJNI.btGImpactShapeInterface_getBulletTriangle(swigCPtr, this, prim_index, btTriangleShapeEx.getCPtr(triangle), triangle);
  }

  public void getBulletTetrahedron(int prim_index, btTetrahedronShapeEx tetrahedron) {
    CollisionJNI.btGImpactShapeInterface_getBulletTetrahedron(swigCPtr, this, prim_index, btTetrahedronShapeEx.getCPtr(tetrahedron), tetrahedron);
  }

  public void lockChildShapes() {
    CollisionJNI.btGImpactShapeInterface_lockChildShapes(swigCPtr, this);
  }

  public void unlockChildShapes() {
    CollisionJNI.btGImpactShapeInterface_unlockChildShapes(swigCPtr, this);
  }

  public void getPrimitiveTriangle(int index, btPrimitiveTriangle triangle) {
    CollisionJNI.btGImpactShapeInterface_getPrimitiveTriangle(swigCPtr, this, index, btPrimitiveTriangle.getCPtr(triangle), triangle);
  }

  public void getChildAabb(int child_index, Matrix4 t, Vector3 aabbMin, Vector3 aabbMax) {
    CollisionJNI.btGImpactShapeInterface_getChildAabb(swigCPtr, this, child_index, t, aabbMin, aabbMax);
  }

  public btCollisionShape getChildShape(int index) {
    long cPtr = CollisionJNI.btGImpactShapeInterface_getChildShape(swigCPtr, this, index);
    return (cPtr == 0) ? null : btCollisionShape.newDerivedObject(cPtr, false);
  }

  public btCollisionShape getChildShapeConst(int index) {
    long cPtr = CollisionJNI.btGImpactShapeInterface_getChildShapeConst(swigCPtr, this, index);
    return (cPtr == 0) ? null : btCollisionShape.newDerivedObject(cPtr, false);
  }

  public Matrix4 getChildTransform(int index) {
	return CollisionJNI.btGImpactShapeInterface_getChildTransform(swigCPtr, this, index);
}

  public void setChildTransform(int index, Matrix4 transform) {
    CollisionJNI.btGImpactShapeInterface_setChildTransform(swigCPtr, this, index, transform);
  }

  public void rayTest(Vector3 rayFrom, Vector3 rayTo, RayResultCallback resultCallback) {
    CollisionJNI.btGImpactShapeInterface_rayTest(swigCPtr, this, rayFrom, rayTo, RayResultCallback.getCPtr(resultCallback), resultCallback);
  }

  public void processAllTrianglesRay(btTriangleCallback arg0, Vector3 arg1, Vector3 arg2) {
    CollisionJNI.btGImpactShapeInterface_processAllTrianglesRay(swigCPtr, this, btTriangleCallback.getCPtr(arg0), arg0, arg1, arg2);
  }

}
