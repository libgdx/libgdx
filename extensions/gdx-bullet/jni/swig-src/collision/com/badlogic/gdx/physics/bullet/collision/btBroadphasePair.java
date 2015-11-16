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

public class btBroadphasePair extends BulletBase {
	private long swigCPtr;
	
	protected btBroadphasePair(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btBroadphasePair, normally you should not need this constructor it's intended for low-level usage. */ 
	public btBroadphasePair(long cPtr, boolean cMemoryOwn) {
		this("btBroadphasePair", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btBroadphasePair obj) {
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
				CollisionJNI.delete_btBroadphasePair(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

	/** Temporary instance, use by native methods that return a btBroadphasePair instance */
	protected final static btBroadphasePair temp = new btBroadphasePair(0, false);
	public static btBroadphasePair internalTemp(long cPtr, boolean own) {
		temp.reset(cPtr, own);
		return temp;
	}
	/** Pool of btBroadphasePair instances, used by director interface to provide the arguments. */
	protected static final com.badlogic.gdx.utils.Pool<btBroadphasePair> pool = new com.badlogic.gdx.utils.Pool<btBroadphasePair>() {
		@Override
		protected btBroadphasePair newObject() {
			return new btBroadphasePair(0, false);
		}
	};
	/** Reuses a previous freed instance or creates a new instance and set it to reflect the specified native object */
	public static btBroadphasePair obtain(long cPtr, boolean own) {
		final btBroadphasePair result = pool.obtain();
		result.reset(cPtr, own);
		return result;
	}
	/** delete the native object if required and allow the instance to be reused by the obtain method */
	public static void free(final btBroadphasePair inst) {
		inst.dispose();
		pool.free(inst);
	}

  public btBroadphasePair() {
    this(CollisionJNI.new_btBroadphasePair__SWIG_0(), true);
  }

  public btBroadphasePair(btBroadphasePair other) {
    this(CollisionJNI.new_btBroadphasePair__SWIG_1(other), true);
  }

  public btBroadphasePair(btBroadphaseProxy proxy0, btBroadphaseProxy proxy1) {
    this(CollisionJNI.new_btBroadphasePair__SWIG_2(proxy0, proxy1), true);
  }

  public void setPProxy0(btBroadphaseProxy value) {
    CollisionJNI.btBroadphasePair_pProxy0_set(swigCPtr, this, btBroadphaseProxy.getCPtr(value), value);
  }

  public btBroadphaseProxy getPProxy0() {
	return btBroadphaseProxy.internalTemp(CollisionJNI.btBroadphasePair_pProxy0_get(swigCPtr, this), false);
}

  public void setPProxy1(btBroadphaseProxy value) {
    CollisionJNI.btBroadphasePair_pProxy1_set(swigCPtr, this, btBroadphaseProxy.getCPtr(value), value);
  }

  public btBroadphaseProxy getPProxy1() {
	return btBroadphaseProxy.internalTemp(CollisionJNI.btBroadphasePair_pProxy1_get(swigCPtr, this), false);
}

  public void setAlgorithm(btCollisionAlgorithm value) {
    CollisionJNI.btBroadphasePair_algorithm_set(swigCPtr, this, btCollisionAlgorithm.getCPtr(value), value);
  }

  public btCollisionAlgorithm getAlgorithm() {
    long cPtr = CollisionJNI.btBroadphasePair_algorithm_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btCollisionAlgorithm(cPtr, false);
  }

  public void setInternalInfo1(long value) {
    CollisionJNI.btBroadphasePair_internalInfo1_set(swigCPtr, this, value);
  }

  public long getInternalInfo1() {
    return CollisionJNI.btBroadphasePair_internalInfo1_get(swigCPtr, this);
  }

  public void setInternalTmpValue(int value) {
    CollisionJNI.btBroadphasePair_internalTmpValue_set(swigCPtr, this, value);
  }

  public int getInternalTmpValue() {
    return CollisionJNI.btBroadphasePair_internalTmpValue_get(swigCPtr, this);
  }

}
