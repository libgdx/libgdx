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

public class btCollisionDispatcherMt extends btCollisionDispatcher {
	private long swigCPtr;
	
	protected btCollisionDispatcherMt(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, CollisionJNI.btCollisionDispatcherMt_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btCollisionDispatcherMt, normally you should not need this constructor it's intended for low-level usage. */
	public btCollisionDispatcherMt(long cPtr, boolean cMemoryOwn) {
		this("btCollisionDispatcherMt", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(CollisionJNI.btCollisionDispatcherMt_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btCollisionDispatcherMt obj) {
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
				CollisionJNI.delete_btCollisionDispatcherMt(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btCollisionDispatcherMt(btCollisionConfiguration config, int grainSize) {
    this(CollisionJNI.new_btCollisionDispatcherMt__SWIG_0(btCollisionConfiguration.getCPtr(config), config, grainSize), true);
  }

  public btCollisionDispatcherMt(btCollisionConfiguration config) {
    this(CollisionJNI.new_btCollisionDispatcherMt__SWIG_1(btCollisionConfiguration.getCPtr(config), config), true);
  }

}
