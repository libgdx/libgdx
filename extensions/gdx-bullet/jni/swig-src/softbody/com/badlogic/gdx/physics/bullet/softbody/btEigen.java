/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.softbody;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btEigen extends BulletBase {
	private long swigCPtr;
	
	protected btEigen(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btEigen, normally you should not need this constructor it's intended for low-level usage. */ 
	public btEigen(long cPtr, boolean cMemoryOwn) {
		this("btEigen", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btEigen obj) {
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
				SoftbodyJNI.delete_btEigen(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public static int system(Matrix3 a, SWIGTYPE_p_btMatrix3x3 vectors, btVector3 values) {
    return SoftbodyJNI.btEigen_system__SWIG_0(a, SWIGTYPE_p_btMatrix3x3.getCPtr(vectors), btVector3.getCPtr(values), values);
  }

  public static int system(Matrix3 a, SWIGTYPE_p_btMatrix3x3 vectors) {
    return SoftbodyJNI.btEigen_system__SWIG_1(a, SWIGTYPE_p_btMatrix3x3.getCPtr(vectors));
  }

  public btEigen() {
    this(SoftbodyJNI.new_btEigen(), true);
  }

}
