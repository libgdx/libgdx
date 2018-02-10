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

public class btPairSet extends btGimPairArray {
	private long swigCPtr;
	
	protected btPairSet(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, CollisionJNI.btPairSet_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btPairSet, normally you should not need this constructor it's intended for low-level usage. */
	public btPairSet(long cPtr, boolean cMemoryOwn) {
		this("btPairSet", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(CollisionJNI.btPairSet_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btPairSet obj) {
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
				CollisionJNI.delete_btPairSet(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btPairSet() {
    this(CollisionJNI.new_btPairSet(), true);
  }

  public void push_pair(int index1, int index2) {
    CollisionJNI.btPairSet_push_pair(swigCPtr, this, index1, index2);
  }

  public void push_pair_inv(int index1, int index2) {
    CollisionJNI.btPairSet_push_pair_inv(swigCPtr, this, index1, index2);
  }

}
