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

public class btBoxShape extends btPolyhedralConvexShape {
	private long swigCPtr;
	
	protected btBoxShape(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, CollisionJNI.btBoxShape_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btBoxShape, normally you should not need this constructor it's intended for low-level usage. */
	public btBoxShape(long cPtr, boolean cMemoryOwn) {
		this("btBoxShape", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(CollisionJNI.btBoxShape_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btBoxShape obj) {
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
				CollisionJNI.delete_btBoxShape(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public long operatorNew(long sizeInBytes) {
    return CollisionJNI.btBoxShape_operatorNew__SWIG_0(swigCPtr, this, sizeInBytes);
  }

  public void operatorDelete(long ptr) {
    CollisionJNI.btBoxShape_operatorDelete__SWIG_0(swigCPtr, this, ptr);
  }

  public long operatorNew(long arg0, long ptr) {
    return CollisionJNI.btBoxShape_operatorNew__SWIG_1(swigCPtr, this, arg0, ptr);
  }

  public void operatorDelete(long arg0, long arg1) {
    CollisionJNI.btBoxShape_operatorDelete__SWIG_1(swigCPtr, this, arg0, arg1);
  }

  public long operatorNewArray(long sizeInBytes) {
    return CollisionJNI.btBoxShape_operatorNewArray__SWIG_0(swigCPtr, this, sizeInBytes);
  }

  public void operatorDeleteArray(long ptr) {
    CollisionJNI.btBoxShape_operatorDeleteArray__SWIG_0(swigCPtr, this, ptr);
  }

  public long operatorNewArray(long arg0, long ptr) {
    return CollisionJNI.btBoxShape_operatorNewArray__SWIG_1(swigCPtr, this, arg0, ptr);
  }

  public void operatorDeleteArray(long arg0, long arg1) {
    CollisionJNI.btBoxShape_operatorDeleteArray__SWIG_1(swigCPtr, this, arg0, arg1);
  }

  public Vector3 getHalfExtentsWithMargin() {
	return CollisionJNI.btBoxShape_getHalfExtentsWithMargin(swigCPtr, this);
}

  public Vector3 getHalfExtentsWithoutMargin() {
	return CollisionJNI.btBoxShape_getHalfExtentsWithoutMargin(swigCPtr, this);
}

  public btBoxShape(Vector3 boxHalfExtents) {
    this(CollisionJNI.new_btBoxShape(boxHalfExtents), true);
  }

  public void getPlaneEquation(btVector4 plane, int i) {
    CollisionJNI.btBoxShape_getPlaneEquation(swigCPtr, this, btVector4.getCPtr(plane), plane, i);
  }

}
