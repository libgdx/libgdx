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

public class btRigidBody extends btCollisionObject {
	private long swigCPtr;

	protected btRigidBody (final String className, long cPtr, boolean cMemoryOwn) {
		super(className, DynamicsJNI.btRigidBody_SWIGUpcast(cPtr), cMemoryOwn);
		swigCPtr = cPtr;
	}

	/** Construct a new btRigidBody, normally you should not need this constructor it's intended for low-level usage. */
	public btRigidBody (long cPtr, boolean cMemoryOwn) {
		this("btRigidBody", cPtr, cMemoryOwn);
		construct();
	}

	@Override
	protected void reset (long cPtr, boolean cMemoryOwn) {
		if (!destroyed) destroy();
		super.reset(DynamicsJNI.btRigidBody_SWIGUpcast(swigCPtr = cPtr), cMemoryOwn);
	}

	public static long getCPtr (btRigidBody obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
	}

	@Override
	protected void finalize () throws Throwable {
		if (!destroyed) destroy();
		super.finalize();
	}

	@Override
	protected synchronized void delete () {
		if (swigCPtr != 0) {
			if (swigCMemOwn) {
				swigCMemOwn = false;
				DynamicsJNI.delete_btRigidBody(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

	protected btMotionState motionState;

	/** @return The existing instance for the specified pointer, or null if the instance doesn't exist */
	public static btRigidBody getInstance (final long swigCPtr) {
		return (btRigidBody)btCollisionObject.getInstance(swigCPtr);
	}

	/** @return The existing instance for the specified pointer, or a newly created instance if the instance didn't exist */
	public static btRigidBody getInstance (final long swigCPtr, boolean owner) {
		if (swigCPtr == 0) return null;
		btRigidBody result = getInstance(swigCPtr);
		if (result == null) result = new btRigidBody(swigCPtr, owner);
		return result;
	}

	public btRigidBody (btRigidBodyConstructionInfo constructionInfo) {
		this(false, constructionInfo);
		refCollisionShape(constructionInfo.getCollisionShape());
		refMotionState(constructionInfo.getMotionState());
	}

	public btRigidBody (float mass, btMotionState motionState, btCollisionShape collisionShape, Vector3 localInertia) {
		this(false, mass, motionState, collisionShape, localInertia);
		refCollisionShape(collisionShape);
		refMotionState(motionState);
	}

	public btRigidBody (float mass, btMotionState motionState, btCollisionShape collisionShape) {
		this(false, mass, motionState, collisionShape);
		refCollisionShape(collisionShape);
		refMotionState(motionState);
	}

	public void setMotionState (btMotionState motionState) {
		refMotionState(motionState);
		internalSetMotionState(motionState);
	}

	protected void refMotionState (btMotionState motionState) {
		if (this.motionState == motionState) return;
		if (this.motionState != null) this.motionState.release();
		this.motionState = motionState;
		if (this.motionState != null) this.motionState.obtain();
	}

	public btMotionState getMotionState () {
		return motionState;
	}

	@Override
	public void dispose () {
		if (motionState != null) motionState.release();
		motionState = null;
		super.dispose();
	}

	static public class btRigidBodyConstructionInfo extends BulletBase {
		private long swigCPtr;

		protected btRigidBodyConstructionInfo (final String className, long cPtr, boolean cMemoryOwn) {
			super(className, cPtr, cMemoryOwn);
			swigCPtr = cPtr;
		}

		/** Construct a new btRigidBodyConstructionInfo, normally you should not need this constructor it's intended for low-level
		 * usage. */
		public btRigidBodyConstructionInfo (long cPtr, boolean cMemoryOwn) {
			this("btRigidBodyConstructionInfo", cPtr, cMemoryOwn);
			construct();
		}

		@Override
		protected void reset (long cPtr, boolean cMemoryOwn) {
			if (!destroyed) destroy();
			super.reset(swigCPtr = cPtr, cMemoryOwn);
		}

		public static long getCPtr (btRigidBodyConstructionInfo obj) {
			return (obj == null) ? 0 : obj.swigCPtr;
		}

		@Override
		protected void finalize () throws Throwable {
			if (!destroyed) destroy();
			super.finalize();
		}

		@Override
		protected synchronized void delete () {
			if (swigCPtr != 0) {
				if (swigCMemOwn) {
					swigCMemOwn = false;
					DynamicsJNI.delete_btRigidBody_btRigidBodyConstructionInfo(swigCPtr);
				}
				swigCPtr = 0;
			}
			super.delete();
		}

		protected btMotionState motionState;

		public void setMotionState (btMotionState motionState) {
			refMotionState(motionState);
			setI_motionState(motionState);
		}

		protected void refMotionState (btMotionState motionState) {
			if (this.motionState == motionState) return;
			if (this.motionState != null) this.motionState.release();
			this.motionState = motionState;
			if (this.motionState != null) this.motionState.obtain();
		}

		public btMotionState getMotionState () {
			return motionState;
		}

		protected btCollisionShape collisionShape;

		public void setCollisionShape (btCollisionShape collisionShape) {
			refCollisionShape(collisionShape);
			setI_collisionShape(collisionShape);
		}

		protected void refCollisionShape (btCollisionShape shape) {
			if (collisionShape == shape) return;
			if (collisionShape != null) collisionShape.release();
			collisionShape = shape;
			if (collisionShape != null) collisionShape.obtain();
		}

		public btCollisionShape getCollisionShape () {
			return collisionShape;
		}

		public btRigidBodyConstructionInfo (float mass, btMotionState motionState, btCollisionShape collisionShape,
			Vector3 localInertia) {
			this(false, mass, motionState, collisionShape, localInertia);
			refMotionState(motionState);
			refCollisionShape(collisionShape);
		}

		public btRigidBodyConstructionInfo (float mass, btMotionState motionState, btCollisionShape collisionShape) {
			this(false, mass, motionState, collisionShape);
			refMotionState(motionState);
			refCollisionShape(collisionShape);
		}

		@Override
		public void dispose () {
			if (motionState != null) motionState.release();
			motionState = null;
			if (collisionShape != null) collisionShape.release();
			collisionShape = null;
			super.dispose();
		}

		public void setMass (float value) {
			DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_mass_set(swigCPtr, this, value);
		}

		public float getMass () {
			return DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_mass_get(swigCPtr, this);
		}

		private void setI_motionState (btMotionState value) {
			DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_i_motionState_set(swigCPtr, this, btMotionState.getCPtr(value),
				value);
		}

		private btMotionState getI_motionState () {
			long cPtr = DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_i_motionState_get(swigCPtr, this);
			return (cPtr == 0) ? null : new btMotionState(cPtr, false);
		}

		public void setStartWorldTransform (btTransform value) {
			DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_startWorldTransform_set(swigCPtr, this, btTransform.getCPtr(value),
				value);
		}

		public btTransform getStartWorldTransform () {
			long cPtr = DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_startWorldTransform_get(swigCPtr, this);
			return (cPtr == 0) ? null : new btTransform(cPtr, false);
		}

		private void setI_collisionShape (btCollisionShape value) {
			DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_i_collisionShape_set(swigCPtr, this, btCollisionShape.getCPtr(value),
				value);
		}

		private btCollisionShape getI_collisionShape () {
			long cPtr = DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_i_collisionShape_get(swigCPtr, this);
			return (cPtr == 0) ? null : btCollisionShape.newDerivedObject(cPtr, false);
		}

		public void setLocalInertia (btVector3 value) {
			DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_localInertia_set(swigCPtr, this, btVector3.getCPtr(value), value);
		}

		public btVector3 getLocalInertia () {
			long cPtr = DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_localInertia_get(swigCPtr, this);
			return (cPtr == 0) ? null : new btVector3(cPtr, false);
		}

		public void setLinearDamping (float value) {
			DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_linearDamping_set(swigCPtr, this, value);
		}

		public float getLinearDamping () {
			return DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_linearDamping_get(swigCPtr, this);
		}

		public void setAngularDamping (float value) {
			DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_angularDamping_set(swigCPtr, this, value);
		}

		public float getAngularDamping () {
			return DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_angularDamping_get(swigCPtr, this);
		}

		public void setFriction (float value) {
			DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_friction_set(swigCPtr, this, value);
		}

		public float getFriction () {
			return DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_friction_get(swigCPtr, this);
		}

		public void setRollingFriction (float value) {
			DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_rollingFriction_set(swigCPtr, this, value);
		}

		public float getRollingFriction () {
			return DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_rollingFriction_get(swigCPtr, this);
		}

		public void setSpinningFriction (float value) {
			DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_spinningFriction_set(swigCPtr, this, value);
		}

		public float getSpinningFriction () {
			return DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_spinningFriction_get(swigCPtr, this);
		}

		public void setRestitution (float value) {
			DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_restitution_set(swigCPtr, this, value);
		}

		public float getRestitution () {
			return DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_restitution_get(swigCPtr, this);
		}

		public void setLinearSleepingThreshold (float value) {
			DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_linearSleepingThreshold_set(swigCPtr, this, value);
		}

		public float getLinearSleepingThreshold () {
			return DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_linearSleepingThreshold_get(swigCPtr, this);
		}

		public void setAngularSleepingThreshold (float value) {
			DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_angularSleepingThreshold_set(swigCPtr, this, value);
		}

		public float getAngularSleepingThreshold () {
			return DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_angularSleepingThreshold_get(swigCPtr, this);
		}

		public void setAdditionalDamping (boolean value) {
			DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_additionalDamping_set(swigCPtr, this, value);
		}

		public boolean getAdditionalDamping () {
			return DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_additionalDamping_get(swigCPtr, this);
		}

		public void setAdditionalDampingFactor (float value) {
			DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_additionalDampingFactor_set(swigCPtr, this, value);
		}

		public float getAdditionalDampingFactor () {
			return DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_additionalDampingFactor_get(swigCPtr, this);
		}

		public void setAdditionalLinearDampingThresholdSqr (float value) {
			DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_additionalLinearDampingThresholdSqr_set(swigCPtr, this, value);
		}

		public float getAdditionalLinearDampingThresholdSqr () {
			return DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_additionalLinearDampingThresholdSqr_get(swigCPtr, this);
		}

		public void setAdditionalAngularDampingThresholdSqr (float value) {
			DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_additionalAngularDampingThresholdSqr_set(swigCPtr, this, value);
		}

		public float getAdditionalAngularDampingThresholdSqr () {
			return DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_additionalAngularDampingThresholdSqr_get(swigCPtr, this);
		}

		public void setAdditionalAngularDampingFactor (float value) {
			DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_additionalAngularDampingFactor_set(swigCPtr, this, value);
		}

		public float getAdditionalAngularDampingFactor () {
			return DynamicsJNI.btRigidBody_btRigidBodyConstructionInfo_additionalAngularDampingFactor_get(swigCPtr, this);
		}

		private btRigidBodyConstructionInfo (boolean dummy, float mass, btMotionState motionState, btCollisionShape collisionShape,
			Vector3 localInertia) {
			this(DynamicsJNI.new_btRigidBody_btRigidBodyConstructionInfo__SWIG_0(dummy, mass, btMotionState.getCPtr(motionState),
				motionState, btCollisionShape.getCPtr(collisionShape), collisionShape, localInertia), true);
		}

		private btRigidBodyConstructionInfo (boolean dummy, float mass, btMotionState motionState,
			btCollisionShape collisionShape) {
			this(DynamicsJNI.new_btRigidBody_btRigidBodyConstructionInfo__SWIG_1(dummy, mass, btMotionState.getCPtr(motionState),
				motionState, btCollisionShape.getCPtr(collisionShape), collisionShape), true);
		}

	}

	public void proceedToTransform (Matrix4 newTrans) {
		DynamicsJNI.btRigidBody_proceedToTransform(swigCPtr, this, newTrans);
	}

	public void predictIntegratedTransform (float step, Matrix4 predictedTransform) {
		DynamicsJNI.btRigidBody_predictIntegratedTransform(swigCPtr, this, step, predictedTransform);
	}

	public void saveKinematicState (float step) {
		DynamicsJNI.btRigidBody_saveKinematicState(swigCPtr, this, step);
	}

	public void applyGravity () {
		DynamicsJNI.btRigidBody_applyGravity(swigCPtr, this);
	}

	public void setGravity (Vector3 acceleration) {
		DynamicsJNI.btRigidBody_setGravity(swigCPtr, this, acceleration);
	}

	public Vector3 getGravity () {
		return DynamicsJNI.btRigidBody_getGravity(swigCPtr, this);
	}

	public void setDamping (float lin_damping, float ang_damping) {
		DynamicsJNI.btRigidBody_setDamping(swigCPtr, this, lin_damping, ang_damping);
	}

	public float getLinearDamping () {
		return DynamicsJNI.btRigidBody_getLinearDamping(swigCPtr, this);
	}

	public float getAngularDamping () {
		return DynamicsJNI.btRigidBody_getAngularDamping(swigCPtr, this);
	}

	public float getLinearSleepingThreshold () {
		return DynamicsJNI.btRigidBody_getLinearSleepingThreshold(swigCPtr, this);
	}

	public float getAngularSleepingThreshold () {
		return DynamicsJNI.btRigidBody_getAngularSleepingThreshold(swigCPtr, this);
	}

	public void applyDamping (float timeStep) {
		DynamicsJNI.btRigidBody_applyDamping(swigCPtr, this, timeStep);
	}

	public void setMassProps (float mass, Vector3 inertia) {
		DynamicsJNI.btRigidBody_setMassProps(swigCPtr, this, mass, inertia);
	}

	public Vector3 getLinearFactor () {
		return DynamicsJNI.btRigidBody_getLinearFactor(swigCPtr, this);
	}

	public void setLinearFactor (Vector3 linearFactor) {
		DynamicsJNI.btRigidBody_setLinearFactor(swigCPtr, this, linearFactor);
	}

	public float getInvMass () {
		return DynamicsJNI.btRigidBody_getInvMass(swigCPtr, this);
	}

	public Matrix3 getInvInertiaTensorWorld () {
		return DynamicsJNI.btRigidBody_getInvInertiaTensorWorld(swigCPtr, this);
	}

	public void integrateVelocities (float step) {
		DynamicsJNI.btRigidBody_integrateVelocities(swigCPtr, this, step);
	}

	public void setCenterOfMassTransform (Matrix4 xform) {
		DynamicsJNI.btRigidBody_setCenterOfMassTransform(swigCPtr, this, xform);
	}

	public void applyCentralForce (Vector3 force) {
		DynamicsJNI.btRigidBody_applyCentralForce(swigCPtr, this, force);
	}

	public Vector3 getTotalForce () {
		return DynamicsJNI.btRigidBody_getTotalForce(swigCPtr, this);
	}

	public Vector3 getTotalTorque () {
		return DynamicsJNI.btRigidBody_getTotalTorque(swigCPtr, this);
	}

	public Vector3 getInvInertiaDiagLocal () {
		return DynamicsJNI.btRigidBody_getInvInertiaDiagLocal(swigCPtr, this);
	}

	public void setInvInertiaDiagLocal (Vector3 diagInvInertia) {
		DynamicsJNI.btRigidBody_setInvInertiaDiagLocal(swigCPtr, this, diagInvInertia);
	}

	public void setSleepingThresholds (float linear, float angular) {
		DynamicsJNI.btRigidBody_setSleepingThresholds(swigCPtr, this, linear, angular);
	}

	public void applyTorque (Vector3 torque) {
		DynamicsJNI.btRigidBody_applyTorque(swigCPtr, this, torque);
	}

	public void applyForce (Vector3 force, Vector3 rel_pos) {
		DynamicsJNI.btRigidBody_applyForce(swigCPtr, this, force, rel_pos);
	}

	public void applyCentralImpulse (Vector3 impulse) {
		DynamicsJNI.btRigidBody_applyCentralImpulse(swigCPtr, this, impulse);
	}

	public void applyTorqueImpulse (Vector3 torque) {
		DynamicsJNI.btRigidBody_applyTorqueImpulse(swigCPtr, this, torque);
	}

	public void applyImpulse (Vector3 impulse, Vector3 rel_pos) {
		DynamicsJNI.btRigidBody_applyImpulse(swigCPtr, this, impulse, rel_pos);
	}

	public void clearForces () {
		DynamicsJNI.btRigidBody_clearForces(swigCPtr, this);
	}

	public void updateInertiaTensor () {
		DynamicsJNI.btRigidBody_updateInertiaTensor(swigCPtr, this);
	}

	public Vector3 getCenterOfMassPosition () {
		return DynamicsJNI.btRigidBody_getCenterOfMassPosition(swigCPtr, this);
	}

	public Quaternion getOrientation () {
		return DynamicsJNI.btRigidBody_getOrientation(swigCPtr, this);
	}

	public Matrix4 getCenterOfMassTransform () {
		return DynamicsJNI.btRigidBody_getCenterOfMassTransform(swigCPtr, this);
	}

	public Vector3 getLinearVelocity () {
		return DynamicsJNI.btRigidBody_getLinearVelocity(swigCPtr, this);
	}

	public Vector3 getAngularVelocity () {
		return DynamicsJNI.btRigidBody_getAngularVelocity(swigCPtr, this);
	}

	public void setLinearVelocity (Vector3 lin_vel) {
		DynamicsJNI.btRigidBody_setLinearVelocity(swigCPtr, this, lin_vel);
	}

	public void setAngularVelocity (Vector3 ang_vel) {
		DynamicsJNI.btRigidBody_setAngularVelocity(swigCPtr, this, ang_vel);
	}

	public Vector3 getVelocityInLocalPoint (Vector3 rel_pos) {
		return DynamicsJNI.btRigidBody_getVelocityInLocalPoint(swigCPtr, this, rel_pos);
	}

	public void translate (Vector3 v) {
		DynamicsJNI.btRigidBody_translate(swigCPtr, this, v);
	}

	public void getAabb (Vector3 aabbMin, Vector3 aabbMax) {
		DynamicsJNI.btRigidBody_getAabb(swigCPtr, this, aabbMin, aabbMax);
	}

	public float computeImpulseDenominator (Vector3 pos, Vector3 normal) {
		return DynamicsJNI.btRigidBody_computeImpulseDenominator(swigCPtr, this, pos, normal);
	}

	public float computeAngularImpulseDenominator (Vector3 axis) {
		return DynamicsJNI.btRigidBody_computeAngularImpulseDenominator(swigCPtr, this, axis);
	}

	public void updateDeactivation (float timeStep) {
		DynamicsJNI.btRigidBody_updateDeactivation(swigCPtr, this, timeStep);
	}

	public boolean wantsSleeping () {
		return DynamicsJNI.btRigidBody_wantsSleeping(swigCPtr, this);
	}

	public btBroadphaseProxy getBroadphaseProxyConst () {
		return btBroadphaseProxy.internalTemp(DynamicsJNI.btRigidBody_getBroadphaseProxyConst(swigCPtr, this), false);
	}

	public btBroadphaseProxy getBroadphaseProxy () {
		return btBroadphaseProxy.internalTemp(DynamicsJNI.btRigidBody_getBroadphaseProxy(swigCPtr, this), false);
	}

	public void setNewBroadphaseProxy (btBroadphaseProxy broadphaseProxy) {
		DynamicsJNI.btRigidBody_setNewBroadphaseProxy(swigCPtr, this, btBroadphaseProxy.getCPtr(broadphaseProxy), broadphaseProxy);
	}

	private btMotionState internalGetMotionState () {
		long cPtr = DynamicsJNI.btRigidBody_internalGetMotionState(swigCPtr, this);
		return (cPtr == 0) ? null : new btMotionState(cPtr, false);
	}

	private btMotionState getMotionStateConst () {
		long cPtr = DynamicsJNI.btRigidBody_getMotionStateConst(swigCPtr, this);
		return (cPtr == 0) ? null : new btMotionState(cPtr, false);
	}

	private void internalSetMotionState (btMotionState motionState) {
		DynamicsJNI.btRigidBody_internalSetMotionState(swigCPtr, this, btMotionState.getCPtr(motionState), motionState);
	}

	public void setContactSolverType (int value) {
		DynamicsJNI.btRigidBody_contactSolverType_set(swigCPtr, this, value);
	}

	public int getContactSolverType () {
		return DynamicsJNI.btRigidBody_contactSolverType_get(swigCPtr, this);
	}

	public void setFrictionSolverType (int value) {
		DynamicsJNI.btRigidBody_frictionSolverType_set(swigCPtr, this, value);
	}

	public int getFrictionSolverType () {
		return DynamicsJNI.btRigidBody_frictionSolverType_get(swigCPtr, this);
	}

	public void setAngularFactor (Vector3 angFac) {
		DynamicsJNI.btRigidBody_setAngularFactor__SWIG_0(swigCPtr, this, angFac);
	}

	public void setAngularFactor (float angFac) {
		DynamicsJNI.btRigidBody_setAngularFactor__SWIG_1(swigCPtr, this, angFac);
	}

	public Vector3 getAngularFactor () {
		return DynamicsJNI.btRigidBody_getAngularFactor(swigCPtr, this);
	}

	public boolean isInWorld () {
		return DynamicsJNI.btRigidBody_isInWorld(swigCPtr, this);
	}

	public void addConstraintRef (btTypedConstraint c) {
		DynamicsJNI.btRigidBody_addConstraintRef(swigCPtr, this, btTypedConstraint.getCPtr(c), c);
	}

	public void removeConstraintRef (btTypedConstraint c) {
		DynamicsJNI.btRigidBody_removeConstraintRef(swigCPtr, this, btTypedConstraint.getCPtr(c), c);
	}

	public btTypedConstraint getConstraintRef (int index) {
		long cPtr = DynamicsJNI.btRigidBody_getConstraintRef(swigCPtr, this, index);
		return (cPtr == 0) ? null : new btTypedConstraint(cPtr, false);
	}

	public int getNumConstraintRefs () {
		return DynamicsJNI.btRigidBody_getNumConstraintRefs(swigCPtr, this);
	}

	public void setFlags (int flags) {
		DynamicsJNI.btRigidBody_setFlags(swigCPtr, this, flags);
	}

	public int getFlags () {
		return DynamicsJNI.btRigidBody_getFlags(swigCPtr, this);
	}

	public Vector3 computeGyroscopicImpulseImplicit_World (float dt) {
		return DynamicsJNI.btRigidBody_computeGyroscopicImpulseImplicit_World(swigCPtr, this, dt);
	}

	public Vector3 computeGyroscopicImpulseImplicit_Body (float step) {
		return DynamicsJNI.btRigidBody_computeGyroscopicImpulseImplicit_Body(swigCPtr, this, step);
	}

	public Vector3 computeGyroscopicForceExplicit (float maxGyroscopicForce) {
		return DynamicsJNI.btRigidBody_computeGyroscopicForceExplicit(swigCPtr, this, maxGyroscopicForce);
	}

	public Vector3 getLocalInertia () {
		return DynamicsJNI.btRigidBody_getLocalInertia(swigCPtr, this);
	}

	private btRigidBody (boolean dummy, btRigidBody.btRigidBodyConstructionInfo constructionInfo) {
		this(DynamicsJNI.new_btRigidBody__SWIG_0(dummy, btRigidBody.btRigidBodyConstructionInfo.getCPtr(constructionInfo),
			constructionInfo), true);
	}

	private btRigidBody (boolean dummy, float mass, btMotionState motionState, btCollisionShape collisionShape,
		Vector3 localInertia) {
		this(DynamicsJNI.new_btRigidBody__SWIG_1(dummy, mass, btMotionState.getCPtr(motionState), motionState,
			btCollisionShape.getCPtr(collisionShape), collisionShape, localInertia), true);
	}

	private btRigidBody (boolean dummy, float mass, btMotionState motionState, btCollisionShape collisionShape) {
		this(DynamicsJNI.new_btRigidBody__SWIG_2(dummy, mass, btMotionState.getCPtr(motionState), motionState,
			btCollisionShape.getCPtr(collisionShape), collisionShape), true);
	}

}
