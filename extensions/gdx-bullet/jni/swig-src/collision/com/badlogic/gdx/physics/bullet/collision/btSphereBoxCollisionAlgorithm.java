/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.11
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.collision;

import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.math.Vector3;

public class btSphereBoxCollisionAlgorithm extends btActivatingCollisionAlgorithm {
	private long swigCPtr;

	protected btSphereBoxCollisionAlgorithm (final String className, long cPtr, boolean cMemoryOwn) {
		super(className, CollisionJNI.btSphereBoxCollisionAlgorithm_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}

	/** Construct a new btSphereBoxCollisionAlgorithm, normally you should not need this constructor it's intended for low-level
	 * usage. */
	public btSphereBoxCollisionAlgorithm (long cPtr, boolean cMemoryOwn) {
		this("btSphereBoxCollisionAlgorithm", cPtr, cMemoryOwn);
		construct();
	}

	@Override
	protected void reset (long cPtr, boolean cMemoryOwn) {
		if (!destroyed) destroy();
		super.reset(CollisionJNI.btSphereBoxCollisionAlgorithm_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}

	public static long getCPtr (btSphereBoxCollisionAlgorithm obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
	}

	@Override
	protected void finalize () throws Throwable {
		if (!destroyed) destroy();
		super.finalize();
	}

	@Override
	protected synchronized void delete () {
		if (swigCPtr != 0) {
			if (swigCMemOwn) {
				swigCMemOwn = false;
				CollisionJNI.delete_btSphereBoxCollisionAlgorithm(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

	public btSphereBoxCollisionAlgorithm (btPersistentManifold mf, btCollisionAlgorithmConstructionInfo ci,
		btCollisionObjectWrapper body0Wrap, btCollisionObjectWrapper body1Wrap, boolean isSwapped) {
		this(CollisionJNI.new_btSphereBoxCollisionAlgorithm(btPersistentManifold.getCPtr(mf), mf,
			btCollisionAlgorithmConstructionInfo.getCPtr(ci), ci, btCollisionObjectWrapper.getCPtr(body0Wrap), body0Wrap,
			btCollisionObjectWrapper.getCPtr(body1Wrap), body1Wrap, isSwapped), true);
	}

	public boolean getSphereDistance (btCollisionObjectWrapper boxObjWrap, Vector3 v3PointOnBox, Vector3 normal,
		SWIGTYPE_p_float penetrationDepth, Vector3 v3SphereCenter, float fRadius, float maxContactDistance) {
		return CollisionJNI.btSphereBoxCollisionAlgorithm_getSphereDistance(swigCPtr, this,
			btCollisionObjectWrapper.getCPtr(boxObjWrap), boxObjWrap, v3PointOnBox, normal,
			SWIGTYPE_p_float.getCPtr(penetrationDepth), v3SphereCenter, fRadius, maxContactDistance);
	}

	public float getSpherePenetration (Vector3 boxHalfExtent, Vector3 sphereRelPos, Vector3 closestPoint, Vector3 normal) {
		return CollisionJNI.btSphereBoxCollisionAlgorithm_getSpherePenetration(swigCPtr, this, boxHalfExtent, sphereRelPos,
			closestPoint, normal);
	}

	static public class CreateFunc extends btCollisionAlgorithmCreateFunc {
		private long swigCPtr;

		protected CreateFunc (final String className, long cPtr, boolean cMemoryOwn) {
			super(className, CollisionJNI.btSphereBoxCollisionAlgorithm_CreateFunc_SWIGUpcast(cPtr), cMemoryOwn);
			swigCPtr = cPtr;
		}

		/** Construct a new CreateFunc, normally you should not need this constructor it's intended for low-level usage. */
		public CreateFunc (long cPtr, boolean cMemoryOwn) {
			this("CreateFunc", cPtr, cMemoryOwn);
			construct();
		}

		@Override
		protected void reset (long cPtr, boolean cMemoryOwn) {
			if (!destroyed) destroy();
			super.reset(CollisionJNI.btSphereBoxCollisionAlgorithm_CreateFunc_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
		}

		public static long getCPtr (CreateFunc obj) {
			return (obj == null) ? 0 : obj.swigCPtr;
		}

		@Override
		protected void finalize () throws Throwable {
			if (!destroyed) destroy();
			super.finalize();
		}

		@Override
		protected synchronized void delete () {
			if (swigCPtr != 0) {
				if (swigCMemOwn) {
					swigCMemOwn = false;
					CollisionJNI.delete_btSphereBoxCollisionAlgorithm_CreateFunc(swigCPtr);
				}
				swigCPtr = 0;
			}
			super.delete();
		}

		public CreateFunc () {
			this(CollisionJNI.new_btSphereBoxCollisionAlgorithm_CreateFunc(), true);
		}

	}

}
