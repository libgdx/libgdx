/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.2
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

public class ClosestNotMeConvexResultCallback extends ClosestConvexResultCallback {
	private long swigCPtr;
	
	protected ClosestNotMeConvexResultCallback(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, CollisionJNI.ClosestNotMeConvexResultCallback_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new ClosestNotMeConvexResultCallback, normally you should not need this constructor it's intended for low-level usage. */
	public ClosestNotMeConvexResultCallback(long cPtr, boolean cMemoryOwn) {
		this("ClosestNotMeConvexResultCallback", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(CollisionJNI.ClosestNotMeConvexResultCallback_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(ClosestNotMeConvexResultCallback obj) {
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
				CollisionJNI.delete_ClosestNotMeConvexResultCallback(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setMe(btCollisionObject value) {
    CollisionJNI.ClosestNotMeConvexResultCallback_me_set(swigCPtr, this, btCollisionObject.getCPtr(value), value);
  }

  public btCollisionObject getMe() {
	return btCollisionObject.getInstance(CollisionJNI.ClosestNotMeConvexResultCallback_me_get(swigCPtr, this), false);
}

  public void setAllowedPenetration(float value) {
    CollisionJNI.ClosestNotMeConvexResultCallback_allowedPenetration_set(swigCPtr, this, value);
  }

  public float getAllowedPenetration() {
    return CollisionJNI.ClosestNotMeConvexResultCallback_allowedPenetration_get(swigCPtr, this);
  }

  public ClosestNotMeConvexResultCallback(btCollisionObject me, Vector3 fromA, Vector3 toA) {
    this(CollisionJNI.new_ClosestNotMeConvexResultCallback(btCollisionObject.getCPtr(me), me, fromA, toA), true);
  }

  public boolean needsCollision(btBroadphaseProxy proxy0) {
    return CollisionJNI.ClosestNotMeConvexResultCallback_needsCollision(swigCPtr, this, btBroadphaseProxy.getCPtr(proxy0), proxy0);
  }

}
