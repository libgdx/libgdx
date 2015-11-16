/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
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

public class btCollisionObjectDoubleData extends BulletBase {
	private long swigCPtr;
	
	protected btCollisionObjectDoubleData(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btCollisionObjectDoubleData, normally you should not need this constructor it's intended for low-level usage. */ 
	public btCollisionObjectDoubleData(long cPtr, boolean cMemoryOwn) {
		this("btCollisionObjectDoubleData", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btCollisionObjectDoubleData obj) {
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
				CollisionJNI.delete_btCollisionObjectDoubleData(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setBroadphaseHandle(long value) {
    CollisionJNI.btCollisionObjectDoubleData_broadphaseHandle_set(swigCPtr, this, value);
  }

  public long getBroadphaseHandle() {
    return CollisionJNI.btCollisionObjectDoubleData_broadphaseHandle_get(swigCPtr, this);
  }

  public void setCollisionShape(long value) {
    CollisionJNI.btCollisionObjectDoubleData_collisionShape_set(swigCPtr, this, value);
  }

  public long getCollisionShape() {
    return CollisionJNI.btCollisionObjectDoubleData_collisionShape_get(swigCPtr, this);
  }

  public void setRootCollisionShape(btCollisionShapeData value) {
    CollisionJNI.btCollisionObjectDoubleData_rootCollisionShape_set(swigCPtr, this, btCollisionShapeData.getCPtr(value), value);
  }

  public btCollisionShapeData getRootCollisionShape() {
    long cPtr = CollisionJNI.btCollisionObjectDoubleData_rootCollisionShape_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btCollisionShapeData(cPtr, false);
  }

  public void setName(String value) {
    CollisionJNI.btCollisionObjectDoubleData_name_set(swigCPtr, this, value);
  }

  public String getName() {
    return CollisionJNI.btCollisionObjectDoubleData_name_get(swigCPtr, this);
  }

  public void setWorldTransform(btTransformDoubleData value) {
    CollisionJNI.btCollisionObjectDoubleData_worldTransform_set(swigCPtr, this, btTransformDoubleData.getCPtr(value), value);
  }

  public btTransformDoubleData getWorldTransform() {
    long cPtr = CollisionJNI.btCollisionObjectDoubleData_worldTransform_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btTransformDoubleData(cPtr, false);
  }

  public void setInterpolationWorldTransform(btTransformDoubleData value) {
    CollisionJNI.btCollisionObjectDoubleData_interpolationWorldTransform_set(swigCPtr, this, btTransformDoubleData.getCPtr(value), value);
  }

  public btTransformDoubleData getInterpolationWorldTransform() {
    long cPtr = CollisionJNI.btCollisionObjectDoubleData_interpolationWorldTransform_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btTransformDoubleData(cPtr, false);
  }

  public void setInterpolationLinearVelocity(btVector3DoubleData value) {
    CollisionJNI.btCollisionObjectDoubleData_interpolationLinearVelocity_set(swigCPtr, this, btVector3DoubleData.getCPtr(value), value);
  }

  public btVector3DoubleData getInterpolationLinearVelocity() {
    long cPtr = CollisionJNI.btCollisionObjectDoubleData_interpolationLinearVelocity_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3DoubleData(cPtr, false);
  }

  public void setInterpolationAngularVelocity(btVector3DoubleData value) {
    CollisionJNI.btCollisionObjectDoubleData_interpolationAngularVelocity_set(swigCPtr, this, btVector3DoubleData.getCPtr(value), value);
  }

  public btVector3DoubleData getInterpolationAngularVelocity() {
    long cPtr = CollisionJNI.btCollisionObjectDoubleData_interpolationAngularVelocity_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3DoubleData(cPtr, false);
  }

  public void setAnisotropicFriction(btVector3DoubleData value) {
    CollisionJNI.btCollisionObjectDoubleData_anisotropicFriction_set(swigCPtr, this, btVector3DoubleData.getCPtr(value), value);
  }

  public btVector3DoubleData getAnisotropicFriction() {
    long cPtr = CollisionJNI.btCollisionObjectDoubleData_anisotropicFriction_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3DoubleData(cPtr, false);
  }

  public void setContactProcessingThreshold(double value) {
    CollisionJNI.btCollisionObjectDoubleData_contactProcessingThreshold_set(swigCPtr, this, value);
  }

  public double getContactProcessingThreshold() {
    return CollisionJNI.btCollisionObjectDoubleData_contactProcessingThreshold_get(swigCPtr, this);
  }

  public void setDeactivationTime(double value) {
    CollisionJNI.btCollisionObjectDoubleData_deactivationTime_set(swigCPtr, this, value);
  }

  public double getDeactivationTime() {
    return CollisionJNI.btCollisionObjectDoubleData_deactivationTime_get(swigCPtr, this);
  }

  public void setFriction(double value) {
    CollisionJNI.btCollisionObjectDoubleData_friction_set(swigCPtr, this, value);
  }

  public double getFriction() {
    return CollisionJNI.btCollisionObjectDoubleData_friction_get(swigCPtr, this);
  }

  public void setRollingFriction(double value) {
    CollisionJNI.btCollisionObjectDoubleData_rollingFriction_set(swigCPtr, this, value);
  }

  public double getRollingFriction() {
    return CollisionJNI.btCollisionObjectDoubleData_rollingFriction_get(swigCPtr, this);
  }

  public void setRestitution(double value) {
    CollisionJNI.btCollisionObjectDoubleData_restitution_set(swigCPtr, this, value);
  }

  public double getRestitution() {
    return CollisionJNI.btCollisionObjectDoubleData_restitution_get(swigCPtr, this);
  }

  public void setHitFraction(double value) {
    CollisionJNI.btCollisionObjectDoubleData_hitFraction_set(swigCPtr, this, value);
  }

  public double getHitFraction() {
    return CollisionJNI.btCollisionObjectDoubleData_hitFraction_get(swigCPtr, this);
  }

  public void setCcdSweptSphereRadius(double value) {
    CollisionJNI.btCollisionObjectDoubleData_ccdSweptSphereRadius_set(swigCPtr, this, value);
  }

  public double getCcdSweptSphereRadius() {
    return CollisionJNI.btCollisionObjectDoubleData_ccdSweptSphereRadius_get(swigCPtr, this);
  }

  public void setCcdMotionThreshold(double value) {
    CollisionJNI.btCollisionObjectDoubleData_ccdMotionThreshold_set(swigCPtr, this, value);
  }

  public double getCcdMotionThreshold() {
    return CollisionJNI.btCollisionObjectDoubleData_ccdMotionThreshold_get(swigCPtr, this);
  }

  public void setHasAnisotropicFriction(int value) {
    CollisionJNI.btCollisionObjectDoubleData_hasAnisotropicFriction_set(swigCPtr, this, value);
  }

  public int getHasAnisotropicFriction() {
    return CollisionJNI.btCollisionObjectDoubleData_hasAnisotropicFriction_get(swigCPtr, this);
  }

  public void setCollisionFlags(int value) {
    CollisionJNI.btCollisionObjectDoubleData_collisionFlags_set(swigCPtr, this, value);
  }

  public int getCollisionFlags() {
    return CollisionJNI.btCollisionObjectDoubleData_collisionFlags_get(swigCPtr, this);
  }

  public void setIslandTag1(int value) {
    CollisionJNI.btCollisionObjectDoubleData_islandTag1_set(swigCPtr, this, value);
  }

  public int getIslandTag1() {
    return CollisionJNI.btCollisionObjectDoubleData_islandTag1_get(swigCPtr, this);
  }

  public void setCompanionId(int value) {
    CollisionJNI.btCollisionObjectDoubleData_companionId_set(swigCPtr, this, value);
  }

  public int getCompanionId() {
    return CollisionJNI.btCollisionObjectDoubleData_companionId_get(swigCPtr, this);
  }

  public void setActivationState1(int value) {
    CollisionJNI.btCollisionObjectDoubleData_activationState1_set(swigCPtr, this, value);
  }

  public int getActivationState1() {
    return CollisionJNI.btCollisionObjectDoubleData_activationState1_get(swigCPtr, this);
  }

  public void setInternalType(int value) {
    CollisionJNI.btCollisionObjectDoubleData_internalType_set(swigCPtr, this, value);
  }

  public int getInternalType() {
    return CollisionJNI.btCollisionObjectDoubleData_internalType_get(swigCPtr, this);
  }

  public void setCheckCollideWith(int value) {
    CollisionJNI.btCollisionObjectDoubleData_checkCollideWith_set(swigCPtr, this, value);
  }

  public int getCheckCollideWith() {
    return CollisionJNI.btCollisionObjectDoubleData_checkCollideWith_get(swigCPtr, this);
  }

  public void setPadding(String value) {
    CollisionJNI.btCollisionObjectDoubleData_padding_set(swigCPtr, this, value);
  }

  public String getPadding() {
    return CollisionJNI.btCollisionObjectDoubleData_padding_get(swigCPtr, this);
  }

  public btCollisionObjectDoubleData() {
    this(CollisionJNI.new_btCollisionObjectDoubleData(), true);
  }

}
