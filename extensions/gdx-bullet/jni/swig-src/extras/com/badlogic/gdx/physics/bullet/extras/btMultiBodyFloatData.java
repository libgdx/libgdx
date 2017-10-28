/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.extras;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.inversedynamics.MultiBodyTree;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btContactSolverInfo;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;

public class btMultiBodyFloatData extends BulletBase {
	private long swigCPtr;
	
	protected btMultiBodyFloatData(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btMultiBodyFloatData, normally you should not need this constructor it's intended for low-level usage. */ 
	public btMultiBodyFloatData(long cPtr, boolean cMemoryOwn) {
		this("btMultiBodyFloatData", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btMultiBodyFloatData obj) {
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
				ExtrasJNI.delete_btMultiBodyFloatData(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setBaseName(String value) {
    ExtrasJNI.btMultiBodyFloatData_baseName_set(swigCPtr, this, value);
  }

  public String getBaseName() {
    return ExtrasJNI.btMultiBodyFloatData_baseName_get(swigCPtr, this);
  }

  public void setLinks(btMultiBodyLinkFloatData value) {
    ExtrasJNI.btMultiBodyFloatData_links_set(swigCPtr, this, btMultiBodyLinkFloatData.getCPtr(value), value);
  }

  public btMultiBodyLinkFloatData getLinks() {
    long cPtr = ExtrasJNI.btMultiBodyFloatData_links_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btMultiBodyLinkFloatData(cPtr, false);
  }

  public void setBaseCollider(btCollisionObjectFloatData value) {
    ExtrasJNI.btMultiBodyFloatData_baseCollider_set(swigCPtr, this, btCollisionObjectFloatData.getCPtr(value), value);
  }

  public btCollisionObjectFloatData getBaseCollider() {
    long cPtr = ExtrasJNI.btMultiBodyFloatData_baseCollider_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btCollisionObjectFloatData(cPtr, false);
  }

  public void setBaseWorldTransform(btTransformFloatData value) {
    ExtrasJNI.btMultiBodyFloatData_baseWorldTransform_set(swigCPtr, this, btTransformFloatData.getCPtr(value), value);
  }

  public btTransformFloatData getBaseWorldTransform() {
    long cPtr = ExtrasJNI.btMultiBodyFloatData_baseWorldTransform_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btTransformFloatData(cPtr, false);
  }

  public void setBaseInertia(btVector3FloatData value) {
    ExtrasJNI.btMultiBodyFloatData_baseInertia_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getBaseInertia() {
    long cPtr = ExtrasJNI.btMultiBodyFloatData_baseInertia_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setBaseMass(float value) {
    ExtrasJNI.btMultiBodyFloatData_baseMass_set(swigCPtr, this, value);
  }

  public float getBaseMass() {
    return ExtrasJNI.btMultiBodyFloatData_baseMass_get(swigCPtr, this);
  }

  public void setNumLinks(int value) {
    ExtrasJNI.btMultiBodyFloatData_numLinks_set(swigCPtr, this, value);
  }

  public int getNumLinks() {
    return ExtrasJNI.btMultiBodyFloatData_numLinks_get(swigCPtr, this);
  }

  public btMultiBodyFloatData() {
    this(ExtrasJNI.new_btMultiBodyFloatData(), true);
  }

}
