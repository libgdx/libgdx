/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.11
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

public class btGeneric6DofSpring2Constraint extends btTypedConstraint {
	private long swigCPtr;
	
	protected btGeneric6DofSpring2Constraint(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, DynamicsJNI.btGeneric6DofSpring2Constraint_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btGeneric6DofSpring2Constraint, normally you should not need this constructor it's intended for low-level usage. */
	public btGeneric6DofSpring2Constraint(long cPtr, boolean cMemoryOwn) {
		this("btGeneric6DofSpring2Constraint", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(DynamicsJNI.btGeneric6DofSpring2Constraint_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btGeneric6DofSpring2Constraint obj) {
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
				DynamicsJNI.delete_btGeneric6DofSpring2Constraint(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btGeneric6DofSpring2Constraint(btRigidBody rbA, btRigidBody rbB, Matrix4 frameInA, Matrix4 frameInB, int rotOrder) {
    this(DynamicsJNI.new_btGeneric6DofSpring2Constraint__SWIG_0(btRigidBody.getCPtr(rbA), rbA, btRigidBody.getCPtr(rbB), rbB, frameInA, frameInB, rotOrder), true);
  }

  public btGeneric6DofSpring2Constraint(btRigidBody rbA, btRigidBody rbB, Matrix4 frameInA, Matrix4 frameInB) {
    this(DynamicsJNI.new_btGeneric6DofSpring2Constraint__SWIG_1(btRigidBody.getCPtr(rbA), rbA, btRigidBody.getCPtr(rbB), rbB, frameInA, frameInB), true);
  }

  public btGeneric6DofSpring2Constraint(btRigidBody rbB, Matrix4 frameInB, int rotOrder) {
    this(DynamicsJNI.new_btGeneric6DofSpring2Constraint__SWIG_2(btRigidBody.getCPtr(rbB), rbB, frameInB, rotOrder), true);
  }

  public btGeneric6DofSpring2Constraint(btRigidBody rbB, Matrix4 frameInB) {
    this(DynamicsJNI.new_btGeneric6DofSpring2Constraint__SWIG_3(btRigidBody.getCPtr(rbB), rbB, frameInB), true);
  }

  public btRotationalLimitMotor2 getRotationalLimitMotor(int index) {
    long cPtr = DynamicsJNI.btGeneric6DofSpring2Constraint_getRotationalLimitMotor(swigCPtr, this, index);
    return (cPtr == 0) ? null : new btRotationalLimitMotor2(cPtr, false);
  }

  public btTranslationalLimitMotor2 getTranslationalLimitMotor() {
    long cPtr = DynamicsJNI.btGeneric6DofSpring2Constraint_getTranslationalLimitMotor(swigCPtr, this);
    return (cPtr == 0) ? null : new btTranslationalLimitMotor2(cPtr, false);
  }

  public void calculateTransforms(Matrix4 transA, Matrix4 transB) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_calculateTransforms__SWIG_0(swigCPtr, this, transA, transB);
  }

  public void calculateTransforms() {
    DynamicsJNI.btGeneric6DofSpring2Constraint_calculateTransforms__SWIG_1(swigCPtr, this);
  }

  public Matrix4 getCalculatedTransformA() {
	return DynamicsJNI.btGeneric6DofSpring2Constraint_getCalculatedTransformA(swigCPtr, this);
}

  public Matrix4 getCalculatedTransformB() {
	return DynamicsJNI.btGeneric6DofSpring2Constraint_getCalculatedTransformB(swigCPtr, this);
}

  public Matrix4 getFrameOffsetA() {
	return DynamicsJNI.btGeneric6DofSpring2Constraint_getFrameOffsetA__SWIG_0(swigCPtr, this);
}

  public Matrix4 getFrameOffsetB() {
	return DynamicsJNI.btGeneric6DofSpring2Constraint_getFrameOffsetB__SWIG_0(swigCPtr, this);
}

  public Vector3 getAxis(int axis_index) {
	return DynamicsJNI.btGeneric6DofSpring2Constraint_getAxis(swigCPtr, this, axis_index);
}

  public float getAngle(int axis_index) {
    return DynamicsJNI.btGeneric6DofSpring2Constraint_getAngle(swigCPtr, this, axis_index);
  }

  public float getRelativePivotPosition(int axis_index) {
    return DynamicsJNI.btGeneric6DofSpring2Constraint_getRelativePivotPosition(swigCPtr, this, axis_index);
  }

  public void setFrames(Matrix4 frameA, Matrix4 frameB) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setFrames(swigCPtr, this, frameA, frameB);
  }

  public void setLinearLowerLimit(Vector3 linearLower) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setLinearLowerLimit(swigCPtr, this, linearLower);
  }

  public void getLinearLowerLimit(Vector3 linearLower) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_getLinearLowerLimit(swigCPtr, this, linearLower);
  }

  public void setLinearUpperLimit(Vector3 linearUpper) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setLinearUpperLimit(swigCPtr, this, linearUpper);
  }

  public void getLinearUpperLimit(Vector3 linearUpper) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_getLinearUpperLimit(swigCPtr, this, linearUpper);
  }

  public void setAngularLowerLimit(Vector3 angularLower) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setAngularLowerLimit(swigCPtr, this, angularLower);
  }

  public void setAngularLowerLimitReversed(Vector3 angularLower) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setAngularLowerLimitReversed(swigCPtr, this, angularLower);
  }

  public void getAngularLowerLimit(Vector3 angularLower) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_getAngularLowerLimit(swigCPtr, this, angularLower);
  }

  public void getAngularLowerLimitReversed(Vector3 angularLower) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_getAngularLowerLimitReversed(swigCPtr, this, angularLower);
  }

  public void setAngularUpperLimit(Vector3 angularUpper) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setAngularUpperLimit(swigCPtr, this, angularUpper);
  }

  public void setAngularUpperLimitReversed(Vector3 angularUpper) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setAngularUpperLimitReversed(swigCPtr, this, angularUpper);
  }

  public void getAngularUpperLimit(Vector3 angularUpper) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_getAngularUpperLimit(swigCPtr, this, angularUpper);
  }

  public void getAngularUpperLimitReversed(Vector3 angularUpper) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_getAngularUpperLimitReversed(swigCPtr, this, angularUpper);
  }

  public void setLimit(int axis, float lo, float hi) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setLimit(swigCPtr, this, axis, lo, hi);
  }

  public void setLimitReversed(int axis, float lo, float hi) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setLimitReversed(swigCPtr, this, axis, lo, hi);
  }

  public boolean isLimited(int limitIndex) {
    return DynamicsJNI.btGeneric6DofSpring2Constraint_isLimited(swigCPtr, this, limitIndex);
  }

  public void setRotationOrder(int order) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setRotationOrder(swigCPtr, this, order);
  }

  public int getRotationOrder() {
    return DynamicsJNI.btGeneric6DofSpring2Constraint_getRotationOrder(swigCPtr, this);
  }

  public void setAxis(Vector3 axis1, Vector3 axis2) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setAxis(swigCPtr, this, axis1, axis2);
  }

  public void setBounce(int index, float bounce) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setBounce(swigCPtr, this, index, bounce);
  }

  public void enableMotor(int index, boolean onOff) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_enableMotor(swigCPtr, this, index, onOff);
  }

  public void setServo(int index, boolean onOff) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setServo(swigCPtr, this, index, onOff);
  }

  public void setTargetVelocity(int index, float velocity) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setTargetVelocity(swigCPtr, this, index, velocity);
  }

  public void setServoTarget(int index, float target) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setServoTarget(swigCPtr, this, index, target);
  }

  public void setMaxMotorForce(int index, float force) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setMaxMotorForce(swigCPtr, this, index, force);
  }

  public void enableSpring(int index, boolean onOff) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_enableSpring(swigCPtr, this, index, onOff);
  }

  public void setStiffness(int index, float stiffness, boolean limitIfNeeded) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setStiffness__SWIG_0(swigCPtr, this, index, stiffness, limitIfNeeded);
  }

  public void setStiffness(int index, float stiffness) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setStiffness__SWIG_1(swigCPtr, this, index, stiffness);
  }

  public void setDamping(int index, float damping, boolean limitIfNeeded) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setDamping__SWIG_0(swigCPtr, this, index, damping, limitIfNeeded);
  }

  public void setDamping(int index, float damping) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setDamping__SWIG_1(swigCPtr, this, index, damping);
  }

  public void setEquilibriumPoint() {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setEquilibriumPoint__SWIG_0(swigCPtr, this);
  }

  public void setEquilibriumPoint(int index) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setEquilibriumPoint__SWIG_1(swigCPtr, this, index);
  }

  public void setEquilibriumPoint(int index, float val) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setEquilibriumPoint__SWIG_2(swigCPtr, this, index, val);
  }

  public void setParam(int num, float value, int axis) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setParam__SWIG_0(swigCPtr, this, num, value, axis);
  }

  public void setParam(int num, float value) {
    DynamicsJNI.btGeneric6DofSpring2Constraint_setParam__SWIG_1(swigCPtr, this, num, value);
  }

  public float getParam(int num, int axis) {
    return DynamicsJNI.btGeneric6DofSpring2Constraint_getParam__SWIG_0(swigCPtr, this, num, axis);
  }

  public float getParam(int num) {
    return DynamicsJNI.btGeneric6DofSpring2Constraint_getParam__SWIG_1(swigCPtr, this, num);
  }

  public static float btGetMatrixElem(Matrix3 mat, int index) {
    return DynamicsJNI.btGeneric6DofSpring2Constraint_btGetMatrixElem(mat, index);
  }

  public static boolean matrixToEulerXYZ(Matrix3 mat, Vector3 xyz) {
    return DynamicsJNI.btGeneric6DofSpring2Constraint_matrixToEulerXYZ(mat, xyz);
  }

  public static boolean matrixToEulerXZY(Matrix3 mat, Vector3 xyz) {
    return DynamicsJNI.btGeneric6DofSpring2Constraint_matrixToEulerXZY(mat, xyz);
  }

  public static boolean matrixToEulerYXZ(Matrix3 mat, Vector3 xyz) {
    return DynamicsJNI.btGeneric6DofSpring2Constraint_matrixToEulerYXZ(mat, xyz);
  }

  public static boolean matrixToEulerYZX(Matrix3 mat, Vector3 xyz) {
    return DynamicsJNI.btGeneric6DofSpring2Constraint_matrixToEulerYZX(mat, xyz);
  }

  public static boolean matrixToEulerZXY(Matrix3 mat, Vector3 xyz) {
    return DynamicsJNI.btGeneric6DofSpring2Constraint_matrixToEulerZXY(mat, xyz);
  }

  public static boolean matrixToEulerZYX(Matrix3 mat, Vector3 xyz) {
    return DynamicsJNI.btGeneric6DofSpring2Constraint_matrixToEulerZYX(mat, xyz);
  }

}
