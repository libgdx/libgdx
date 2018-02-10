/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.dynamics;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btMultiBodyGearConstraint extends btMultiBodyConstraint {
	private long swigCPtr;
	
	protected btMultiBodyGearConstraint(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, DynamicsJNI.btMultiBodyGearConstraint_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btMultiBodyGearConstraint, normally you should not need this constructor it's intended for low-level usage. */
	public btMultiBodyGearConstraint(long cPtr, boolean cMemoryOwn) {
		this("btMultiBodyGearConstraint", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(DynamicsJNI.btMultiBodyGearConstraint_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btMultiBodyGearConstraint obj) {
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
				DynamicsJNI.delete_btMultiBodyGearConstraint(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btMultiBodyGearConstraint(btMultiBody bodyA, int linkA, btMultiBody bodyB, int linkB, Vector3 pivotInA, Vector3 pivotInB, Matrix3 frameInA, Matrix3 frameInB) {
    this(DynamicsJNI.new_btMultiBodyGearConstraint(btMultiBody.getCPtr(bodyA), bodyA, linkA, btMultiBody.getCPtr(bodyB), bodyB, linkB, pivotInA, pivotInB, frameInA, frameInB), true);
  }

  public Vector3 getPivotInA() {
	return DynamicsJNI.btMultiBodyGearConstraint_getPivotInA(swigCPtr, this);
}

  public void setPivotInA(Vector3 pivotInA) {
    DynamicsJNI.btMultiBodyGearConstraint_setPivotInA(swigCPtr, this, pivotInA);
  }

  public Vector3 getPivotInB() {
	return DynamicsJNI.btMultiBodyGearConstraint_getPivotInB(swigCPtr, this);
}

  public Matrix3 getFrameInA() {
	return DynamicsJNI.btMultiBodyGearConstraint_getFrameInA(swigCPtr, this);
}

  public void setFrameInA(Matrix3 frameInA) {
    DynamicsJNI.btMultiBodyGearConstraint_setFrameInA(swigCPtr, this, frameInA);
  }

  public Matrix3 getFrameInB() {
	return DynamicsJNI.btMultiBodyGearConstraint_getFrameInB(swigCPtr, this);
}

}
