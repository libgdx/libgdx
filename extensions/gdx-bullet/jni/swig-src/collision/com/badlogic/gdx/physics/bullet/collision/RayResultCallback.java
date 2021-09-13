/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.11
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.collision;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;

public class RayResultCallback extends BulletBase {
	private long swigCPtr;

	protected RayResultCallback (final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}

	/** Construct a new RayResultCallback, normally you should not need this constructor it's intended for low-level usage. */
	public RayResultCallback (long cPtr, boolean cMemoryOwn) {
		this("RayResultCallback", cPtr, cMemoryOwn);
		construct();
	}

	@Override
	protected void reset (long cPtr, boolean cMemoryOwn) {
		if (!destroyed) destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}

	public static long getCPtr (RayResultCallback obj) {
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
				CollisionJNI.delete_RayResultCallback(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

	protected void swigDirectorDisconnect () {
		swigCMemOwn = false;
		delete();
	}

	public void swigReleaseOwnership () {
		swigCMemOwn = false;
		CollisionJNI.RayResultCallback_change_ownership(this, swigCPtr, false);
	}

	public void swigTakeOwnership () {
		swigCMemOwn = true;
		CollisionJNI.RayResultCallback_change_ownership(this, swigCPtr, true);
	}

	public void setClosestHitFraction (float value) {
		CollisionJNI.RayResultCallback_closestHitFraction_set(swigCPtr, this, value);
	}

	public float getClosestHitFraction () {
		return CollisionJNI.RayResultCallback_closestHitFraction_get(swigCPtr, this);
	}

	public void setCollisionObject (btCollisionObject value) {
		CollisionJNI.RayResultCallback_collisionObject_set(swigCPtr, this, btCollisionObject.getCPtr(value), value);
	}

	public btCollisionObject getCollisionObject () {
		return btCollisionObject.getInstance(CollisionJNI.RayResultCallback_collisionObject_get(swigCPtr, this), false);
	}

	public void setCollisionFilterGroup (int value) {
		CollisionJNI.RayResultCallback_collisionFilterGroup_set(swigCPtr, this, value);
	}

	public int getCollisionFilterGroup () {
		return CollisionJNI.RayResultCallback_collisionFilterGroup_get(swigCPtr, this);
	}

	public void setCollisionFilterMask (int value) {
		CollisionJNI.RayResultCallback_collisionFilterMask_set(swigCPtr, this, value);
	}

	public int getCollisionFilterMask () {
		return CollisionJNI.RayResultCallback_collisionFilterMask_get(swigCPtr, this);
	}

	public void setFlags (long value) {
		CollisionJNI.RayResultCallback_flags_set(swigCPtr, this, value);
	}

	public long getFlags () {
		return CollisionJNI.RayResultCallback_flags_get(swigCPtr, this);
	}

	public boolean hasHit () {
		return CollisionJNI.RayResultCallback_hasHit(swigCPtr, this);
	}

	public RayResultCallback () {
		this(CollisionJNI.new_RayResultCallback(), true);
		CollisionJNI.RayResultCallback_director_connect(this, swigCPtr, swigCMemOwn, true);
	}

	public boolean needsCollision (btBroadphaseProxy proxy0) {
		return (getClass() == RayResultCallback.class)
			? CollisionJNI.RayResultCallback_needsCollision(swigCPtr, this, btBroadphaseProxy.getCPtr(proxy0), proxy0)
			: CollisionJNI.RayResultCallback_needsCollisionSwigExplicitRayResultCallback(swigCPtr, this,
				btBroadphaseProxy.getCPtr(proxy0), proxy0);
	}

	public float addSingleResult (LocalRayResult rayResult, boolean normalInWorldSpace) {
		return CollisionJNI.RayResultCallback_addSingleResult(swigCPtr, this, LocalRayResult.getCPtr(rayResult), rayResult,
			normalInWorldSpace);
	}

}
