/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
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

public class btDiscreteDynamicsWorld extends btDynamicsWorld {
	private long swigCPtr;
	
	protected btDiscreteDynamicsWorld(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, DynamicsJNI.btDiscreteDynamicsWorld_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btDiscreteDynamicsWorld, normally you should not need this constructor it's intended for low-level usage. */
	public btDiscreteDynamicsWorld(long cPtr, boolean cMemoryOwn) {
		this("btDiscreteDynamicsWorld", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(DynamicsJNI.btDiscreteDynamicsWorld_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}
	
	public static long getCPtr(btDiscreteDynamicsWorld obj) {
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
				DynamicsJNI.delete_btDiscreteDynamicsWorld(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btDiscreteDynamicsWorld(btDispatcher dispatcher, btBroadphaseInterface pairCache, btConstraintSolver constraintSolver, btCollisionConfiguration collisionConfiguration) {
    this(DynamicsJNI.new_btDiscreteDynamicsWorld(btDispatcher.getCPtr(dispatcher), dispatcher, btBroadphaseInterface.getCPtr(pairCache), pairCache, btConstraintSolver.getCPtr(constraintSolver), constraintSolver, btCollisionConfiguration.getCPtr(collisionConfiguration), collisionConfiguration), true);
  }

  public int stepSimulation(float timeStep, int maxSubSteps, float fixedTimeStep) {
    return DynamicsJNI.btDiscreteDynamicsWorld_stepSimulation__SWIG_0(swigCPtr, this, timeStep, maxSubSteps, fixedTimeStep);
  }

  public int stepSimulation(float timeStep, int maxSubSteps) {
    return DynamicsJNI.btDiscreteDynamicsWorld_stepSimulation__SWIG_1(swigCPtr, this, timeStep, maxSubSteps);
  }

  public int stepSimulation(float timeStep) {
    return DynamicsJNI.btDiscreteDynamicsWorld_stepSimulation__SWIG_2(swigCPtr, this, timeStep);
  }

  public void synchronizeSingleMotionState(btRigidBody body) {
    DynamicsJNI.btDiscreteDynamicsWorld_synchronizeSingleMotionState(swigCPtr, this, btRigidBody.getCPtr(body), body);
  }

  public void addConstraint(btTypedConstraint constraint, boolean disableCollisionsBetweenLinkedBodies) {
    DynamicsJNI.btDiscreteDynamicsWorld_addConstraint__SWIG_0(swigCPtr, this, btTypedConstraint.getCPtr(constraint), constraint, disableCollisionsBetweenLinkedBodies);
  }

  public void addConstraint(btTypedConstraint constraint) {
    DynamicsJNI.btDiscreteDynamicsWorld_addConstraint__SWIG_1(swigCPtr, this, btTypedConstraint.getCPtr(constraint), constraint);
  }

  public btSimulationIslandManager getSimulationIslandManager() {
    long cPtr = DynamicsJNI.btDiscreteDynamicsWorld_getSimulationIslandManager__SWIG_0(swigCPtr, this);
    return (cPtr == 0) ? null : new btSimulationIslandManager(cPtr, false);
  }

  public btCollisionWorld getCollisionWorld() {
    long cPtr = DynamicsJNI.btDiscreteDynamicsWorld_getCollisionWorld(swigCPtr, this);
    return (cPtr == 0) ? null : new btCollisionWorld(cPtr, false);
  }

  public void addCollisionObject(btCollisionObject collisionObject, short collisionFilterGroup, short collisionFilterMask) {
    DynamicsJNI.btDiscreteDynamicsWorld_addCollisionObject__SWIG_0(swigCPtr, this, btCollisionObject.getCPtr(collisionObject), collisionObject, collisionFilterGroup, collisionFilterMask);
  }

  public void addCollisionObject(btCollisionObject collisionObject, short collisionFilterGroup) {
    DynamicsJNI.btDiscreteDynamicsWorld_addCollisionObject__SWIG_1(swigCPtr, this, btCollisionObject.getCPtr(collisionObject), collisionObject, collisionFilterGroup);
  }

  public void addCollisionObject(btCollisionObject collisionObject) {
    DynamicsJNI.btDiscreteDynamicsWorld_addCollisionObject__SWIG_2(swigCPtr, this, btCollisionObject.getCPtr(collisionObject), collisionObject);
  }

  public void addRigidBody(btRigidBody body) {
    DynamicsJNI.btDiscreteDynamicsWorld_addRigidBody__SWIG_0(swigCPtr, this, btRigidBody.getCPtr(body), body);
  }

  public void addRigidBody(btRigidBody body, short group, short mask) {
    DynamicsJNI.btDiscreteDynamicsWorld_addRigidBody__SWIG_1(swigCPtr, this, btRigidBody.getCPtr(body), body, group, mask);
  }

  public void debugDrawConstraint(btTypedConstraint constraint) {
    DynamicsJNI.btDiscreteDynamicsWorld_debugDrawConstraint(swigCPtr, this, btTypedConstraint.getCPtr(constraint), constraint);
  }

  public btTypedConstraint getConstraint(int index) {
    long cPtr = DynamicsJNI.btDiscreteDynamicsWorld_getConstraint__SWIG_0(swigCPtr, this, index);
    return (cPtr == 0) ? null : new btTypedConstraint(cPtr, false);
  }

  public void applyGravity() {
    DynamicsJNI.btDiscreteDynamicsWorld_applyGravity(swigCPtr, this);
  }

  public void setNumTasks(int numTasks) {
    DynamicsJNI.btDiscreteDynamicsWorld_setNumTasks(swigCPtr, this, numTasks);
  }

  public void updateVehicles(float timeStep) {
    DynamicsJNI.btDiscreteDynamicsWorld_updateVehicles(swigCPtr, this, timeStep);
  }

  public void setSynchronizeAllMotionStates(boolean synchronizeAll) {
    DynamicsJNI.btDiscreteDynamicsWorld_setSynchronizeAllMotionStates(swigCPtr, this, synchronizeAll);
  }

  public boolean getSynchronizeAllMotionStates() {
    return DynamicsJNI.btDiscreteDynamicsWorld_getSynchronizeAllMotionStates(swigCPtr, this);
  }

  public void setApplySpeculativeContactRestitution(boolean enable) {
    DynamicsJNI.btDiscreteDynamicsWorld_setApplySpeculativeContactRestitution(swigCPtr, this, enable);
  }

  public boolean getApplySpeculativeContactRestitution() {
    return DynamicsJNI.btDiscreteDynamicsWorld_getApplySpeculativeContactRestitution(swigCPtr, this);
  }

  public void setLatencyMotionStateInterpolation(boolean latencyInterpolation) {
    DynamicsJNI.btDiscreteDynamicsWorld_setLatencyMotionStateInterpolation(swigCPtr, this, latencyInterpolation);
  }

  public boolean getLatencyMotionStateInterpolation() {
    return DynamicsJNI.btDiscreteDynamicsWorld_getLatencyMotionStateInterpolation(swigCPtr, this);
  }

}
