/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
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

public class btPolyhedralContactClipping extends BulletBase {
	private long swigCPtr;
	
	protected btPolyhedralContactClipping(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btPolyhedralContactClipping, normally you should not need this constructor it's intended for low-level usage. */ 
	public btPolyhedralContactClipping(long cPtr, boolean cMemoryOwn) {
		this("btPolyhedralContactClipping", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btPolyhedralContactClipping obj) {
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
				CollisionJNI.delete_btPolyhedralContactClipping(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public static void clipHullAgainstHull(Vector3 separatingNormal, btConvexPolyhedron hullA, btConvexPolyhedron hullB, Matrix4 transA, Matrix4 transB, float minDist, float maxDist, btDiscreteCollisionDetectorInterface.Result resultOut) {
    CollisionJNI.btPolyhedralContactClipping_clipHullAgainstHull(separatingNormal, btConvexPolyhedron.getCPtr(hullA), hullA, btConvexPolyhedron.getCPtr(hullB), hullB, transA, transB, minDist, maxDist, btDiscreteCollisionDetectorInterface.Result.getCPtr(resultOut), resultOut);
  }

  public static void clipFaceAgainstHull(Vector3 separatingNormal, btConvexPolyhedron hullA, Matrix4 transA, btVector3Array worldVertsB1, float minDist, float maxDist, btDiscreteCollisionDetectorInterface.Result resultOut) {
    CollisionJNI.btPolyhedralContactClipping_clipFaceAgainstHull(separatingNormal, btConvexPolyhedron.getCPtr(hullA), hullA, transA, btVector3Array.getCPtr(worldVertsB1), worldVertsB1, minDist, maxDist, btDiscreteCollisionDetectorInterface.Result.getCPtr(resultOut), resultOut);
  }

  public static boolean findSeparatingAxis(btConvexPolyhedron hullA, btConvexPolyhedron hullB, Matrix4 transA, Matrix4 transB, Vector3 sep, btDiscreteCollisionDetectorInterface.Result resultOut) {
    return CollisionJNI.btPolyhedralContactClipping_findSeparatingAxis(btConvexPolyhedron.getCPtr(hullA), hullA, btConvexPolyhedron.getCPtr(hullB), hullB, transA, transB, sep, btDiscreteCollisionDetectorInterface.Result.getCPtr(resultOut), resultOut);
  }

  public static void clipFace(btVector3Array pVtxIn, btVector3Array ppVtxOut, Vector3 planeNormalWS, float planeEqWS) {
    CollisionJNI.btPolyhedralContactClipping_clipFace(btVector3Array.getCPtr(pVtxIn), pVtxIn, btVector3Array.getCPtr(ppVtxOut), ppVtxOut, planeNormalWS, planeEqWS);
  }

  public btPolyhedralContactClipping() {
    this(CollisionJNI.new_btPolyhedralContactClipping(), true);
  }

}
