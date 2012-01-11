#include <com.badlogic.gdx.physics.tokamak.RigidBody.h>

//@line:7

	#include <tokamak.h> 
	 JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_physics_tokamak_RigidBody_getMassJni(JNIEnv* env, jclass clazz, jlong addr) {


//@line:21

		return ((neRigidBody*)addr)->GetMass();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_tokamak_RigidBody_setMassJni(JNIEnv* env, jclass clazz, jlong addr, jfloat mass) {


//@line:29

		((neRigidBody*)addr)->SetMass(mass);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_tokamak_RigidBody_setInertiaTensorJni(JNIEnv* env, jclass clazz, jlong addr, jfloat x, jfloat y, jfloat z) {


//@line:41

		neV3 vec;
		vec.Set(x, y, z);
		((neRigidBody*)addr)->SetInertiaTensor(vec);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_tokamak_RigidBody_setCollisionIdJni(JNIEnv* env, jclass clazz, jlong addr, jint cid) {


//@line:51

		((neRigidBody*)addr)->SetCollisionID(cid);
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_tokamak_RigidBody_getCollisionIJni(JNIEnv* env, jclass clazz, jlong addr) {


//@line:59

		return ((neRigidBody*)addr)->GetCollisionID();
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_tokamak_RigidBody_getGeometryCountJni(JNIEnv* env, jclass clazz, jlong addr) {


//@line:75

		return ((neRigidBody*)addr)->GeometryCount();
	

}

