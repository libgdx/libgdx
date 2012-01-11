package com.badlogic.gdx.physics.tokamak;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class RigidBody extends NativeObject {
	/*JNI
	#include <tokamak.h> 
	 */
	
	private Object userData;
	
	RigidBody(long addr) {
		super(addr);
	}
	
	public float getMass() {
		return getMassJni(addr);
	}
	
	private static native float getMassJni(long addr); /*
		return ((neRigidBody*)addr)->GetMass();
	*/

	public void	setMass(float mass) { 
		setMassJni(addr, mass);
	}
	
	private static native void setMassJni(long addr, float mass); /*
		((neRigidBody*)addr)->SetMass(mass);
	*/
	
	public void setInertiaTensor(Matrix4 tensor) {
		// FIXME
	}

	public void setInertiaTensor(Vector3 tensor) { 
		setInertiaTensorJni(addr, tensor.x, tensor.y, tensor.z);
	}

	private static native void setInertiaTensorJni(long addr, float x, float y, float z); /*
		neV3 vec;
		vec.Set(x, y, z);
		((neRigidBody*)addr)->SetInertiaTensor(vec);
	*/

	public void setCollisionID(int cid) {
		setCollisionIdJni(addr, cid);
	}
	
	private static native void setCollisionIdJni(long addr, int cid); /*
		((neRigidBody*)addr)->SetCollisionID(cid);
	*/

	public int getCollisionID() { 
		return getCollisionIJni(addr);
	}
	
	private static native int getCollisionIJni(long addr); /*
		return ((neRigidBody*)addr)->GetCollisionID();
	*/

	public void setUserData(Object userData) { 
		this.userData = userData;
	}
	
	public Object getUserData() { 
		return userData;
	}
	
	public int getGeometryCount() { 
		return getGeometryCountJni(addr);
	}
	
	private static native int getGeometryCountJni(long addr); /*
		return ((neRigidBody*)addr)->GetGeometryCount();
	*/
	
//
//	void	SetLinearDamping(float damp) { }	
//
//	float		GetLinearDamping() { }
//
//	void	SetAngularDamping(float damp) { }	
//
//	float		GetAngularDamping() { }
//
//	void	SetSleepingParameter(float sleepingParam) { }
//
//	float		GetSleepingParameter() { }
//
////collision geometries, sensors and controllers
//	
//	Geometry	AddGeometry() { }
//
//	boolean			RemoveGeometry(Geometry g) { }
//
//	void			BeginIterateGeometry() { }
//
//	Geometry	GetNextGeometry() { }
//
//	RigidBody	BreakGeometry(Geometry g) { }
//
//	void			UseCustomCollisionDetection(boolean yes,  Matrix4 obb, float boundingRadius) { }
//	
//	boolean			UseCustomCollisionDetection() { }
//
//	Sensor		AddSensor() { }
//
//	boolean			RemoveSensor(Sensor s) { }
//	
//	void			BeginIterateSensor() { }
//
//	Sensor		GetNextSensor() { }
//
//	RigidBodyController AddController(RigidBodyControllerCallback controller, int period) { }
//
//	boolean			RemoveController(RigidBodyController rbController) { }
//
//	void			BeginIterateController() { }
//
//	RigidBodyController GetNextController() { }
//
////spatial states
//	Vector3	GetPos() { }
//	
//	void	SetPos(Vector3 p) { }
//	
//	Matrix4	GetRotationM3() { }
//	
//	Quaternion		GetRotationQ() { }
//	
//	void	SetRotation(Matrix4 m) { }
//	
//	void	SetRotation(Quaternion q) { }
//	
//	Matrix4	GetTransform() { }
//
////dynamic states
//	Vector3	GetVelocity() { }
//	
//	void	SetVelocity(Vector3 v) { }
//	
//	Vector3	GetAngularVelocity() { }
//	
//	Vector3	GetAngularMomentum() { }
//	
//	void	SetAngularMomentum(Vector3 am) { }
//
//	Vector3	GetVelocityAtPoint(Vector3 pt) { }
//
////functions
//	void	UpdateBoundingInfo() { }
//	
//	void	UpdateInertiaTensor() { }
//	
//	void	SetForce(Vector3 force) { }
//
//	void	SetTorque(Vector3 torque) { }
//	
//	void	SetForce(Vector3 force, Vector3 pos) { }
//
//	Vector3	GetForce() { }
//
//	Vector3	GetTorque() { }
//
//	void	ApplyImpulse(Vector3 impulse) { }
//
//	void	ApplyImpulse(Vector3 impulse, Vector3 pos) { }
//
//	void	ApplyTwist(Vector3 twist) { }
//
//	void	GravityEnable(boolean yes) { }
//
//	boolean	GravityEnable() { }
//
//	// collide with any body which connected to this body indirectly
//	
//	void	CollideConnected(boolean yes) { } 
//
//	boolean	CollideConnected() { }
//
//	// collide with any body which connected to this body directly
//
//	void	CollideDirectlyConnected(boolean yes) { }
//
//	boolean	CollideDirectlyConnected() { }
//
//	void	Active(boolean yes, RigidBody hint ) { }
//
//	void	Active(boolean yes, AnimatedBody hint ) { }
//
//	boolean	Active() { }
//
//	boolean	IsIdle() { }
}
