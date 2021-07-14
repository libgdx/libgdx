/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.linearmath;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btHashPtr extends BulletBase {
	private long swigCPtr;
	
	protected btHashPtr(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btHashPtr, normally you should not need this constructor it's intended for low-level usage. */ 
	public btHashPtr(long cPtr, boolean cMemoryOwn) {
		this("btHashPtr", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btHashPtr obj) {
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
				LinearMathJNI.delete_btHashPtr(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btHashPtr(long ptr) {
    this(LinearMathJNI.new_btHashPtr(ptr), true);
  }

  public long getPointer() {
    return LinearMathJNI.btHashPtr_getPointer(swigCPtr, this);
  }

  public boolean equals(btHashPtr other) {
    return LinearMathJNI.btHashPtr_equals(swigCPtr, this, btHashPtr.getCPtr(other), other);
  }

  public long getHash() {
    return LinearMathJNI.btHashPtr_getHash(swigCPtr, this);
  }

}
