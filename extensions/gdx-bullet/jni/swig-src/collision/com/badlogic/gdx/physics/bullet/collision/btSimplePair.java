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

public class btSimplePair extends BulletBase {
	private long swigCPtr;
	
	protected btSimplePair(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btSimplePair, normally you should not need this constructor it's intended for low-level usage. */ 
	public btSimplePair(long cPtr, boolean cMemoryOwn) {
		this("btSimplePair", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btSimplePair obj) {
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
				CollisionJNI.delete_btSimplePair(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btSimplePair(int indexA, int indexB) {
    this(CollisionJNI.new_btSimplePair(indexA, indexB), true);
  }

  public void setIndexA(int value) {
    CollisionJNI.btSimplePair_indexA_set(swigCPtr, this, value);
  }

  public int getIndexA() {
    return CollisionJNI.btSimplePair_indexA_get(swigCPtr, this);
  }

  public void setIndexB(int value) {
    CollisionJNI.btSimplePair_indexB_set(swigCPtr, this, value);
  }

  public int getIndexB() {
    return CollisionJNI.btSimplePair_indexB_get(swigCPtr, this);
  }

  public void setUserPointer(long value) {
    CollisionJNI.btSimplePair_userPointer_set(swigCPtr, this, value);
  }

  public long getUserPointer() {
    return CollisionJNI.btSimplePair_userPointer_get(swigCPtr, this);
  }

  public void setUserValue(int value) {
    CollisionJNI.btSimplePair_userValue_set(swigCPtr, this, value);
  }

  public int getUserValue() {
    return CollisionJNI.btSimplePair_userValue_get(swigCPtr, this);
  }

}
