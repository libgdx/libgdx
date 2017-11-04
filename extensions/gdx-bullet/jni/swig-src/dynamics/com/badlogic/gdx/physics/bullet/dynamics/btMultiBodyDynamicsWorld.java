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

public class btMultiBodyDynamicsWorld extends btDiscreteDynamicsWorld {
	private long swigCPtr;
	
	protected btMultiBodyDynamicsWorld(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, DynamicsJNI.btMultiBodyDynamicsWorld_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btMultiBodyDynamicsWorld, normally you should not need this constructor it's intended for low-level usage. */
	public btMultiBodyDynamicsWorld(long cPtr, boolean cMemoryOwn) {
		this("btMultiBodyDynamicsWorld", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(DynamicsJNI.btMultiBodyDynamicsWorld_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btMultiBodyDynamicsWorld obj) {
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
				DynamicsJNI.delete_btMultiBodyDynamicsWorld(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btMultiBodyDynamicsWorld(btDispatcher dispatcher, btBroadphaseInterface pairCache, btMultiBodyConstraintSolver constraintSolver, btCollisionConfiguration collisionConfiguration) {
    this(DynamicsJNI.new_btMultiBodyDynamicsWorld(btDispatcher.getCPtr(dispatcher), dispatcher, btBroadphaseInterface.getCPtr(pairCache), pairCache, btMultiBodyConstraintSolver.getCPtr(constraintSolver), constraintSolver, btCollisionConfiguration.getCPtr(collisionConfiguration), collisionConfiguration), true);
  }

  public void addMultiBody(btMultiBody body, int group, int mask) {
    DynamicsJNI.btMultiBodyDynamicsWorld_addMultiBody__SWIG_0(swigCPtr, this, btMultiBody.getCPtr(body), body, group, mask);
  }

  public void addMultiBody(btMultiBody body, int group) {
    DynamicsJNI.btMultiBodyDynamicsWorld_addMultiBody__SWIG_1(swigCPtr, this, btMultiBody.getCPtr(body), body, group);
  }

  public void addMultiBody(btMultiBody body) {
    DynamicsJNI.btMultiBodyDynamicsWorld_addMultiBody__SWIG_2(swigCPtr, this, btMultiBody.getCPtr(body), body);
  }

  public void removeMultiBody(btMultiBody body) {
    DynamicsJNI.btMultiBodyDynamicsWorld_removeMultiBody(swigCPtr, this, btMultiBody.getCPtr(body), body);
  }

  public int getNumMultibodies() {
    return DynamicsJNI.btMultiBodyDynamicsWorld_getNumMultibodies(swigCPtr, this);
  }

  public btMultiBody getMultiBody(int mbIndex) {
    long cPtr = DynamicsJNI.btMultiBodyDynamicsWorld_getMultiBody(swigCPtr, this, mbIndex);
    return (cPtr == 0) ? null : new btMultiBody(cPtr, false);
  }

  public btMultiBody getMultiBodyConst(int mbIndex) {
    long cPtr = DynamicsJNI.btMultiBodyDynamicsWorld_getMultiBodyConst(swigCPtr, this, mbIndex);
    return (cPtr == 0) ? null : new btMultiBody(cPtr, false);
  }

  public void addMultiBodyConstraint(btMultiBodyConstraint constraint) {
    DynamicsJNI.btMultiBodyDynamicsWorld_addMultiBodyConstraint(swigCPtr, this, btMultiBodyConstraint.getCPtr(constraint), constraint);
  }

  public int getNumMultiBodyConstraints() {
    return DynamicsJNI.btMultiBodyDynamicsWorld_getNumMultiBodyConstraints(swigCPtr, this);
  }

  public btMultiBodyConstraint getMultiBodyConstraint(int constraintIndex) {
    long cPtr = DynamicsJNI.btMultiBodyDynamicsWorld_getMultiBodyConstraint(swigCPtr, this, constraintIndex);
    return (cPtr == 0) ? null : new btMultiBodyConstraint(cPtr, false);
  }

  public btMultiBodyConstraint getMultiBodyConstraintConst(int constraintIndex) {
    long cPtr = DynamicsJNI.btMultiBodyDynamicsWorld_getMultiBodyConstraintConst(swigCPtr, this, constraintIndex);
    return (cPtr == 0) ? null : new btMultiBodyConstraint(cPtr, false);
  }

  public void removeMultiBodyConstraint(btMultiBodyConstraint constraint) {
    DynamicsJNI.btMultiBodyDynamicsWorld_removeMultiBodyConstraint(swigCPtr, this, btMultiBodyConstraint.getCPtr(constraint), constraint);
  }

  public void integrateTransforms(float timeStep) {
    DynamicsJNI.btMultiBodyDynamicsWorld_integrateTransforms(swigCPtr, this, timeStep);
  }

  public void debugDrawMultiBodyConstraint(btMultiBodyConstraint constraint) {
    DynamicsJNI.btMultiBodyDynamicsWorld_debugDrawMultiBodyConstraint(swigCPtr, this, btMultiBodyConstraint.getCPtr(constraint), constraint);
  }

  public void forwardKinematics() {
    DynamicsJNI.btMultiBodyDynamicsWorld_forwardKinematics(swigCPtr, this);
  }

  public void clearMultiBodyConstraintForces() {
    DynamicsJNI.btMultiBodyDynamicsWorld_clearMultiBodyConstraintForces(swigCPtr, this);
  }

  public void clearMultiBodyForces() {
    DynamicsJNI.btMultiBodyDynamicsWorld_clearMultiBodyForces(swigCPtr, this);
  }

}
