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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btCollisionDispatcher extends btDispatcher {
	private long swigCPtr;
	
	protected btCollisionDispatcher(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, CollisionJNI.btCollisionDispatcher_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btCollisionDispatcher, normally you should not need this constructor it's intended for low-level usage. */
	public btCollisionDispatcher(long cPtr, boolean cMemoryOwn) {
		this("btCollisionDispatcher", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(CollisionJNI.btCollisionDispatcher_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btCollisionDispatcher obj) {
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
				CollisionJNI.delete_btCollisionDispatcher(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public int getDispatcherFlags() {
    return CollisionJNI.btCollisionDispatcher_getDispatcherFlags(swigCPtr, this);
  }

  public void setDispatcherFlags(int flags) {
    CollisionJNI.btCollisionDispatcher_setDispatcherFlags(swigCPtr, this, flags);
  }

  public void registerCollisionCreateFunc(int proxyType0, int proxyType1, btCollisionAlgorithmCreateFunc createFunc) {
    CollisionJNI.btCollisionDispatcher_registerCollisionCreateFunc(swigCPtr, this, proxyType0, proxyType1, btCollisionAlgorithmCreateFunc.getCPtr(createFunc), createFunc);
  }

  public void registerClosestPointsCreateFunc(int proxyType0, int proxyType1, btCollisionAlgorithmCreateFunc createFunc) {
    CollisionJNI.btCollisionDispatcher_registerClosestPointsCreateFunc(swigCPtr, this, proxyType0, proxyType1, btCollisionAlgorithmCreateFunc.getCPtr(createFunc), createFunc);
  }

  public btPersistentManifold getManifoldByIndexInternalConst(int index) {
    long cPtr = CollisionJNI.btCollisionDispatcher_getManifoldByIndexInternalConst(swigCPtr, this, index);
    return (cPtr == 0) ? null : new btPersistentManifold(cPtr, false);
  }

  public btCollisionDispatcher(btCollisionConfiguration collisionConfiguration) {
    this(CollisionJNI.new_btCollisionDispatcher(btCollisionConfiguration.getCPtr(collisionConfiguration), collisionConfiguration), true);
  }

  public void setNearCallback(SWIGTYPE_p_f_r_btBroadphasePair_r_btCollisionDispatcher_r_q_const__btDispatcherInfo__void nearCallback) {
    CollisionJNI.btCollisionDispatcher_setNearCallback(swigCPtr, this, SWIGTYPE_p_f_r_btBroadphasePair_r_btCollisionDispatcher_r_q_const__btDispatcherInfo__void.getCPtr(nearCallback));
  }

  public SWIGTYPE_p_f_r_btBroadphasePair_r_btCollisionDispatcher_r_q_const__btDispatcherInfo__void getNearCallback() {
    long cPtr = CollisionJNI.btCollisionDispatcher_getNearCallback(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_f_r_btBroadphasePair_r_btCollisionDispatcher_r_q_const__btDispatcherInfo__void(cPtr, false);
  }

  public static void defaultNearCallback(btBroadphasePair collisionPair, btCollisionDispatcher dispatcher, btDispatcherInfo dispatchInfo) {
    CollisionJNI.btCollisionDispatcher_defaultNearCallback(collisionPair, btCollisionDispatcher.getCPtr(dispatcher), dispatcher, btDispatcherInfo.getCPtr(dispatchInfo), dispatchInfo);
  }

  public btCollisionConfiguration getCollisionConfiguration() {
    long cPtr = CollisionJNI.btCollisionDispatcher_getCollisionConfiguration(swigCPtr, this);
    return (cPtr == 0) ? null : new btCollisionConfiguration(cPtr, false);
  }

  public btCollisionConfiguration getCollisionConfigurationConst() {
    long cPtr = CollisionJNI.btCollisionDispatcher_getCollisionConfigurationConst(swigCPtr, this);
    return (cPtr == 0) ? null : new btCollisionConfiguration(cPtr, false);
  }

  public void setCollisionConfiguration(btCollisionConfiguration config) {
    CollisionJNI.btCollisionDispatcher_setCollisionConfiguration(swigCPtr, this, btCollisionConfiguration.getCPtr(config), config);
  }

  public final static class DispatcherFlags {
    public final static int CD_STATIC_STATIC_REPORTED = 1;
    public final static int CD_USE_RELATIVE_CONTACT_BREAKING_THRESHOLD = 2;
    public final static int CD_DISABLE_CONTACTPOOL_DYNAMIC_ALLOCATION = 4;
  }

}
