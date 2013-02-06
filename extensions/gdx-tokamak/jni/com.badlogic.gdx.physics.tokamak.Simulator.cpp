#include <com.badlogic.gdx.physics.tokamak.Simulator.h>

//@line:21

	#include <tokamak.h>
	 JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_createSimulator(JNIEnv* env, jclass clazz, jint animatedBodiesCount, jint constraintBufferSize, jint constraintsCount, jint constraintSetsCount, jint controllersCount, jint geometriesCount, jint overlappedPairsCount, jint rigidBodiesCount, jint rigidParticleCount, jint sensorsCount, jint terrainNodesGrowByCount, jint terrainNodesStartCount, jfloat x, jfloat y, jfloat z) {


//@line:52

		neSimulatorSizeInfo sizeInfo;
		sizeInfo.animatedBodiesCount = animatedBodiesCount;
		sizeInfo.constraintBufferSize = constraintBufferSize;
		sizeInfo.constraintsCount = constraintsCount;
		sizeInfo.constraintSetsCount = constraintSetsCount;
		sizeInfo.controllersCount = controllersCount;
		sizeInfo.geometriesCount = geometriesCount;
		sizeInfo.overlappedPairsCount = overlappedPairsCount;
		sizeInfo.rigidBodiesCount = rigidBodiesCount;
		sizeInfo.rigidParticleCount = rigidParticleCount;
		sizeInfo.sensorsCount = sensorsCount;
		sizeInfo.terrainNodesGrowByCount = terrainNodesGrowByCount;
		sizeInfo.terrainNodesStartCount = terrainNodesStartCount;
		
		neV3 gravity; 
		gravity.Set(x, y, z);
			
		return (jlong)neSimulator::CreateSimulator(sizeInfo, 0, &gravity);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_disposeJni(JNIEnv* env, jclass clazz, jlong addr) {


//@line:78

		neSimulator::DestroySimulator((neSimulator*)addr);
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_createRigidBodyJni(JNIEnv* env, jclass clazz, jlong addr) {


//@line:88

		return (jlong)((neSimulator*)addr)->CreateRigidBody();
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_createRigidParticleJni(JNIEnv* env, jclass clazz, jlong addr) {


//@line:98

		return (jlong)((neSimulator*)addr)->CreateRigidParticle();
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_createAnimatedBodyJni(JNIEnv* env, jclass clazz, jlong addr) {


//@line:108

		return (jlong)((neSimulator*)addr)->CreateAnimatedBody();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_freeRigidBodyJni(JNIEnv* env, jclass clazz, jlong simAddr, jlong addr) {


//@line:116

		((neSimulator*)simAddr)->FreeRigidBody((neRigidBody*)addr);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_freeAnimatedBodyJni(JNIEnv* env, jclass clazz, jlong simAddr, jlong addr) {


//@line:124

		((neSimulator*)simAddr)->FreeAnimatedBody((neAnimatedBody*)addr);
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_getCollisionTableJNI(JNIEnv* env, jclass clazz, jlong addr) {


//@line:133

		return (jlong)((neSimulator*)addr)->GetCollisionTable();
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_setMaterialJni(JNIEnv* env, jclass clazz, jlong addr, jint index, jfloat friction, jfloat restitution) {


//@line:145

		((neSimulator*)addr)->SetMaterial(index, friction, restitution);
	

}

static inline jboolean wrapped_Java_com_badlogic_gdx_physics_tokamak_Simulator_getMaterialJni
(JNIEnv* env, jclass clazz, jlong addr, jint index, jfloatArray obj_materialTmp, float* materialTmp) {

//@line:156

		return ((neSimulator*)addr)->GetMaterial(index, materialTmp[0], materialTmp[1]);
	
}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_getMaterialJni(JNIEnv* env, jclass clazz, jlong addr, jint index, jfloatArray obj_materialTmp) {
	float* materialTmp = (float*)env->GetPrimitiveArrayCritical(obj_materialTmp, 0);

	jboolean JNI_returnValue = wrapped_Java_com_badlogic_gdx_physics_tokamak_Simulator_getMaterialJni(env, clazz, addr, index, obj_materialTmp, materialTmp);

	env->ReleasePrimitiveArrayCritical(obj_materialTmp, materialTmp, 0);

	return JNI_returnValue;
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_advanceJni__JFI(JNIEnv* env, jclass clazz, jlong addr, jfloat sec, jint nSteps) {


//@line:164

		((neSimulator*)addr)->Advance(sec, nSteps);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_advanceJni__JFFF(JNIEnv* env, jclass clazz, jlong addr, jfloat sec, jfloat minTimeStep, jfloat maxTimeStep) {


//@line:172

		((neSimulator*)addr)->Advance(sec, minTimeStep, maxTimeStep);
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_createJointJni__JJ(JNIEnv* env, jclass clazz, jlong addr, jlong addrBody) {


//@line:190

		return (jlong)((neSimulator*)addr)->CreateJoint((neRigidBody*)addrBody);
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_createJointJni__JJJ(JNIEnv* env, jclass clazz, jlong addr, jlong addrBodyA, jlong addrBodyB) {


//@line:200

		return (jlong)((neSimulator*)addr)->CreateJoint((neRigidBody*)addrBodyA, (neRigidBody*)addrBodyB);
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_createJointAnimatedBodyJni(JNIEnv* env, jclass clazz, jlong addr, jlong addrBodyA, jlong addrBodyB) {


//@line:210

		return (jlong)((neSimulator*)addr)->CreateJoint((neRigidBody*)addrBodyA, (neAnimatedBody*)addrBodyB);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_freeJointJni(JNIEnv* env, jclass clazz, jlong addr, jlong addrJoint) {


//@line:218

		((neSimulator*)addr)->FreeJoint((neJoint*)addrJoint);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_getGravityJni(JNIEnv* env, jclass clazz, jlong addr, jfloatArray obj_gravity) {
	float* gravity = (float*)env->GetPrimitiveArrayCritical(obj_gravity, 0);


//@line:230

		neV3 vec;
		vec = ((neSimulator*)addr)->Gravity();
		gravity[0] = vec.X();
		gravity[1] = vec.Y();
		gravity[2] = vec.Z();
	
	env->ReleasePrimitiveArrayCritical(obj_gravity, gravity, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_setGravityJni(JNIEnv* env, jclass clazz, jlong addr, jfloat x, jfloat y, jfloat z) {


//@line:246

		neV3 vec;
		vec.Set(x, y, z);
		((neSimulator*)addr)->Gravity(vec);
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_tokamak_Simulator_getMemoryAllocatedJni(JNIEnv* env, jclass clazz, jlong addr) {


//@line:292

		s32 memory;
		((neSimulator*)addr)->GetMemoryAllocated(memory);
		return memory;
	

}

