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

public class btSoftBodyCollisionShape extends btConcaveShape {
	private long swigCPtr;
	
	protected btSoftBodyCollisionShape(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, SoftbodyJNI.btSoftBodyCollisionShape_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btSoftBodyCollisionShape, normally you should not need this constructor it's intended for low-level usage. */
	public btSoftBodyCollisionShape(long cPtr, boolean cMemoryOwn) {
		this("btSoftBodyCollisionShape", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(SoftbodyJNI.btSoftBodyCollisionShape_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btSoftBodyCollisionShape obj) {
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
				SoftbodyJNI.delete_btSoftBodyCollisionShape(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setBody(btSoftBody value) {
    SoftbodyJNI.btSoftBodyCollisionShape_body_set(swigCPtr, this, btSoftBody.getCPtr(value), value);
  }

  public btSoftBody getBody() {
    long cPtr = SoftbodyJNI.btSoftBodyCollisionShape_body_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btSoftBody(cPtr, false);
  }

  public btSoftBodyCollisionShape(btSoftBody backptr) {
    this(SoftbodyJNI.new_btSoftBodyCollisionShape(btSoftBody.getCPtr(backptr), backptr), true);
  }

}
