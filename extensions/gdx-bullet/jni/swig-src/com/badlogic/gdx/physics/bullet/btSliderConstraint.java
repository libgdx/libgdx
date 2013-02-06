/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.8
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btSliderConstraint extends btTypedConstraint {
  private long swigCPtr;

  protected btSliderConstraint(long cPtr, boolean cMemoryOwn) {
    super(gdxBulletJNI.btSliderConstraint_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(btSliderConstraint obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btSliderConstraint(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public btSliderConstraint(btRigidBody rbA, btRigidBody rbB, Matrix4 frameInA, Matrix4 frameInB, boolean useLinearReferenceFrameA) {
    this(gdxBulletJNI.new_btSliderConstraint__SWIG_0(btRigidBody.getCPtr(rbA), rbA, btRigidBody.getCPtr(rbB), rbB, frameInA, frameInB, useLinearReferenceFrameA), true);
  }

  public btSliderConstraint(btRigidBody rbB, Matrix4 frameInB, boolean useLinearReferenceFrameA) {
    this(gdxBulletJNI.new_btSliderConstraint__SWIG_1(btRigidBody.getCPtr(rbB), rbB, frameInB, useLinearReferenceFrameA), true);
  }

  public void getInfo1NonVirtual(SWIGTYPE_p_btTypedConstraint__btConstraintInfo1 info) {
    gdxBulletJNI.btSliderConstraint_getInfo1NonVirtual(swigCPtr, this, SWIGTYPE_p_btTypedConstraint__btConstraintInfo1.getCPtr(info));
  }

  public void getInfo2NonVirtual(btConstraintInfo2 info, Matrix4 transA, Matrix4 transB, Vector3 linVelA, Vector3 linVelB, float rbAinvMass, float rbBinvMass) {
    gdxBulletJNI.btSliderConstraint_getInfo2NonVirtual(swigCPtr, this, btConstraintInfo2.getCPtr(info), info, transA, transB, linVelA, linVelB, rbAinvMass, rbBinvMass);
  }

  public btRigidBody getRigidBodyA() {
    return new btRigidBody(gdxBulletJNI.btSliderConstraint_getRigidBodyA(swigCPtr, this), false);
  }

  public btRigidBody getRigidBodyB() {
    return new btRigidBody(gdxBulletJNI.btSliderConstraint_getRigidBodyB(swigCPtr, this), false);
  }

  public Matrix4 getCalculatedTransformA() {
	return gdxBulletJNI.btSliderConstraint_getCalculatedTransformA(swigCPtr, this);
}

  public Matrix4 getCalculatedTransformB() {
	return gdxBulletJNI.btSliderConstraint_getCalculatedTransformB(swigCPtr, this);
}

  public Matrix4 getFrameOffsetA() {
	return gdxBulletJNI.btSliderConstraint_getFrameOffsetA__SWIG_0(swigCPtr, this);
}

  public Matrix4 getFrameOffsetB() {
	return gdxBulletJNI.btSliderConstraint_getFrameOffsetB__SWIG_0(swigCPtr, this);
}

  public float getLowerLinLimit() {
    return gdxBulletJNI.btSliderConstraint_getLowerLinLimit(swigCPtr, this);
  }

  public void setLowerLinLimit(float lowerLimit) {
    gdxBulletJNI.btSliderConstraint_setLowerLinLimit(swigCPtr, this, lowerLimit);
  }

  public float getUpperLinLimit() {
    return gdxBulletJNI.btSliderConstraint_getUpperLinLimit(swigCPtr, this);
  }

  public void setUpperLinLimit(float upperLimit) {
    gdxBulletJNI.btSliderConstraint_setUpperLinLimit(swigCPtr, this, upperLimit);
  }

  public float getLowerAngLimit() {
    return gdxBulletJNI.btSliderConstraint_getLowerAngLimit(swigCPtr, this);
  }

  public void setLowerAngLimit(float lowerLimit) {
    gdxBulletJNI.btSliderConstraint_setLowerAngLimit(swigCPtr, this, lowerLimit);
  }

  public float getUpperAngLimit() {
    return gdxBulletJNI.btSliderConstraint_getUpperAngLimit(swigCPtr, this);
  }

  public void setUpperAngLimit(float upperLimit) {
    gdxBulletJNI.btSliderConstraint_setUpperAngLimit(swigCPtr, this, upperLimit);
  }

  public boolean getUseLinearReferenceFrameA() {
    return gdxBulletJNI.btSliderConstraint_getUseLinearReferenceFrameA(swigCPtr, this);
  }

  public float getSoftnessDirLin() {
    return gdxBulletJNI.btSliderConstraint_getSoftnessDirLin(swigCPtr, this);
  }

  public float getRestitutionDirLin() {
    return gdxBulletJNI.btSliderConstraint_getRestitutionDirLin(swigCPtr, this);
  }

  public float getDampingDirLin() {
    return gdxBulletJNI.btSliderConstraint_getDampingDirLin(swigCPtr, this);
  }

  public float getSoftnessDirAng() {
    return gdxBulletJNI.btSliderConstraint_getSoftnessDirAng(swigCPtr, this);
  }

  public float getRestitutionDirAng() {
    return gdxBulletJNI.btSliderConstraint_getRestitutionDirAng(swigCPtr, this);
  }

  public float getDampingDirAng() {
    return gdxBulletJNI.btSliderConstraint_getDampingDirAng(swigCPtr, this);
  }

  public float getSoftnessLimLin() {
    return gdxBulletJNI.btSliderConstraint_getSoftnessLimLin(swigCPtr, this);
  }

  public float getRestitutionLimLin() {
    return gdxBulletJNI.btSliderConstraint_getRestitutionLimLin(swigCPtr, this);
  }

  public float getDampingLimLin() {
    return gdxBulletJNI.btSliderConstraint_getDampingLimLin(swigCPtr, this);
  }

  public float getSoftnessLimAng() {
    return gdxBulletJNI.btSliderConstraint_getSoftnessLimAng(swigCPtr, this);
  }

  public float getRestitutionLimAng() {
    return gdxBulletJNI.btSliderConstraint_getRestitutionLimAng(swigCPtr, this);
  }

  public float getDampingLimAng() {
    return gdxBulletJNI.btSliderConstraint_getDampingLimAng(swigCPtr, this);
  }

  public float getSoftnessOrthoLin() {
    return gdxBulletJNI.btSliderConstraint_getSoftnessOrthoLin(swigCPtr, this);
  }

  public float getRestitutionOrthoLin() {
    return gdxBulletJNI.btSliderConstraint_getRestitutionOrthoLin(swigCPtr, this);
  }

  public float getDampingOrthoLin() {
    return gdxBulletJNI.btSliderConstraint_getDampingOrthoLin(swigCPtr, this);
  }

  public float getSoftnessOrthoAng() {
    return gdxBulletJNI.btSliderConstraint_getSoftnessOrthoAng(swigCPtr, this);
  }

  public float getRestitutionOrthoAng() {
    return gdxBulletJNI.btSliderConstraint_getRestitutionOrthoAng(swigCPtr, this);
  }

  public float getDampingOrthoAng() {
    return gdxBulletJNI.btSliderConstraint_getDampingOrthoAng(swigCPtr, this);
  }

  public void setSoftnessDirLin(float softnessDirLin) {
    gdxBulletJNI.btSliderConstraint_setSoftnessDirLin(swigCPtr, this, softnessDirLin);
  }

  public void setRestitutionDirLin(float restitutionDirLin) {
    gdxBulletJNI.btSliderConstraint_setRestitutionDirLin(swigCPtr, this, restitutionDirLin);
  }

  public void setDampingDirLin(float dampingDirLin) {
    gdxBulletJNI.btSliderConstraint_setDampingDirLin(swigCPtr, this, dampingDirLin);
  }

  public void setSoftnessDirAng(float softnessDirAng) {
    gdxBulletJNI.btSliderConstraint_setSoftnessDirAng(swigCPtr, this, softnessDirAng);
  }

  public void setRestitutionDirAng(float restitutionDirAng) {
    gdxBulletJNI.btSliderConstraint_setRestitutionDirAng(swigCPtr, this, restitutionDirAng);
  }

  public void setDampingDirAng(float dampingDirAng) {
    gdxBulletJNI.btSliderConstraint_setDampingDirAng(swigCPtr, this, dampingDirAng);
  }

  public void setSoftnessLimLin(float softnessLimLin) {
    gdxBulletJNI.btSliderConstraint_setSoftnessLimLin(swigCPtr, this, softnessLimLin);
  }

  public void setRestitutionLimLin(float restitutionLimLin) {
    gdxBulletJNI.btSliderConstraint_setRestitutionLimLin(swigCPtr, this, restitutionLimLin);
  }

  public void setDampingLimLin(float dampingLimLin) {
    gdxBulletJNI.btSliderConstraint_setDampingLimLin(swigCPtr, this, dampingLimLin);
  }

  public void setSoftnessLimAng(float softnessLimAng) {
    gdxBulletJNI.btSliderConstraint_setSoftnessLimAng(swigCPtr, this, softnessLimAng);
  }

  public void setRestitutionLimAng(float restitutionLimAng) {
    gdxBulletJNI.btSliderConstraint_setRestitutionLimAng(swigCPtr, this, restitutionLimAng);
  }

  public void setDampingLimAng(float dampingLimAng) {
    gdxBulletJNI.btSliderConstraint_setDampingLimAng(swigCPtr, this, dampingLimAng);
  }

  public void setSoftnessOrthoLin(float softnessOrthoLin) {
    gdxBulletJNI.btSliderConstraint_setSoftnessOrthoLin(swigCPtr, this, softnessOrthoLin);
  }

  public void setRestitutionOrthoLin(float restitutionOrthoLin) {
    gdxBulletJNI.btSliderConstraint_setRestitutionOrthoLin(swigCPtr, this, restitutionOrthoLin);
  }

  public void setDampingOrthoLin(float dampingOrthoLin) {
    gdxBulletJNI.btSliderConstraint_setDampingOrthoLin(swigCPtr, this, dampingOrthoLin);
  }

  public void setSoftnessOrthoAng(float softnessOrthoAng) {
    gdxBulletJNI.btSliderConstraint_setSoftnessOrthoAng(swigCPtr, this, softnessOrthoAng);
  }

  public void setRestitutionOrthoAng(float restitutionOrthoAng) {
    gdxBulletJNI.btSliderConstraint_setRestitutionOrthoAng(swigCPtr, this, restitutionOrthoAng);
  }

  public void setDampingOrthoAng(float dampingOrthoAng) {
    gdxBulletJNI.btSliderConstraint_setDampingOrthoAng(swigCPtr, this, dampingOrthoAng);
  }

  public void setPoweredLinMotor(boolean onOff) {
    gdxBulletJNI.btSliderConstraint_setPoweredLinMotor(swigCPtr, this, onOff);
  }

  public boolean getPoweredLinMotor() {
    return gdxBulletJNI.btSliderConstraint_getPoweredLinMotor(swigCPtr, this);
  }

  public void setTargetLinMotorVelocity(float targetLinMotorVelocity) {
    gdxBulletJNI.btSliderConstraint_setTargetLinMotorVelocity(swigCPtr, this, targetLinMotorVelocity);
  }

  public float getTargetLinMotorVelocity() {
    return gdxBulletJNI.btSliderConstraint_getTargetLinMotorVelocity(swigCPtr, this);
  }

  public void setMaxLinMotorForce(float maxLinMotorForce) {
    gdxBulletJNI.btSliderConstraint_setMaxLinMotorForce(swigCPtr, this, maxLinMotorForce);
  }

  public float getMaxLinMotorForce() {
    return gdxBulletJNI.btSliderConstraint_getMaxLinMotorForce(swigCPtr, this);
  }

  public void setPoweredAngMotor(boolean onOff) {
    gdxBulletJNI.btSliderConstraint_setPoweredAngMotor(swigCPtr, this, onOff);
  }

  public boolean getPoweredAngMotor() {
    return gdxBulletJNI.btSliderConstraint_getPoweredAngMotor(swigCPtr, this);
  }

  public void setTargetAngMotorVelocity(float targetAngMotorVelocity) {
    gdxBulletJNI.btSliderConstraint_setTargetAngMotorVelocity(swigCPtr, this, targetAngMotorVelocity);
  }

  public float getTargetAngMotorVelocity() {
    return gdxBulletJNI.btSliderConstraint_getTargetAngMotorVelocity(swigCPtr, this);
  }

  public void setMaxAngMotorForce(float maxAngMotorForce) {
    gdxBulletJNI.btSliderConstraint_setMaxAngMotorForce(swigCPtr, this, maxAngMotorForce);
  }

  public float getMaxAngMotorForce() {
    return gdxBulletJNI.btSliderConstraint_getMaxAngMotorForce(swigCPtr, this);
  }

  public float getLinearPos() {
    return gdxBulletJNI.btSliderConstraint_getLinearPos(swigCPtr, this);
  }

  public float getAngularPos() {
    return gdxBulletJNI.btSliderConstraint_getAngularPos(swigCPtr, this);
  }

  public boolean getSolveLinLimit() {
    return gdxBulletJNI.btSliderConstraint_getSolveLinLimit(swigCPtr, this);
  }

  public float getLinDepth() {
    return gdxBulletJNI.btSliderConstraint_getLinDepth(swigCPtr, this);
  }

  public boolean getSolveAngLimit() {
    return gdxBulletJNI.btSliderConstraint_getSolveAngLimit(swigCPtr, this);
  }

  public float getAngDepth() {
    return gdxBulletJNI.btSliderConstraint_getAngDepth(swigCPtr, this);
  }

  public void calculateTransforms(Matrix4 transA, Matrix4 transB) {
    gdxBulletJNI.btSliderConstraint_calculateTransforms(swigCPtr, this, transA, transB);
  }

  public void testLinLimits() {
    gdxBulletJNI.btSliderConstraint_testLinLimits(swigCPtr, this);
  }

  public void testAngLimits() {
    gdxBulletJNI.btSliderConstraint_testAngLimits(swigCPtr, this);
  }

  public Vector3 getAncorInA() {
	return gdxBulletJNI.btSliderConstraint_getAncorInA(swigCPtr, this);
}

  public Vector3 getAncorInB() {
	return gdxBulletJNI.btSliderConstraint_getAncorInB(swigCPtr, this);
}

  public boolean getUseFrameOffset() {
    return gdxBulletJNI.btSliderConstraint_getUseFrameOffset(swigCPtr, this);
  }

  public void setUseFrameOffset(boolean frameOffsetOnOff) {
    gdxBulletJNI.btSliderConstraint_setUseFrameOffset(swigCPtr, this, frameOffsetOnOff);
  }

  public void setFrames(Matrix4 frameA, Matrix4 frameB) {
    gdxBulletJNI.btSliderConstraint_setFrames(swigCPtr, this, frameA, frameB);
  }

  public void setParam(int num, float value, int axis) {
    gdxBulletJNI.btSliderConstraint_setParam__SWIG_0(swigCPtr, this, num, value, axis);
  }

  public void setParam(int num, float value) {
    gdxBulletJNI.btSliderConstraint_setParam__SWIG_1(swigCPtr, this, num, value);
  }

  public float getParam(int num, int axis) {
    return gdxBulletJNI.btSliderConstraint_getParam__SWIG_0(swigCPtr, this, num, axis);
  }

  public float getParam(int num) {
    return gdxBulletJNI.btSliderConstraint_getParam__SWIG_1(swigCPtr, this, num);
  }

}
