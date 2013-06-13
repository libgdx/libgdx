#include <com.box2dLight.box2dLight.Box2dLight.h>

//@line:27

#include <Box2D/Collision/LightCalculus.h>
JNIEXPORT void JNICALL Java_com_box2dLight_box2dLight_Box2dLight_jniSetLightMesh(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_segments, jfloat colorF, jboolean isGL20) {
	float* segments = (float*)env->GetPrimitiveArrayCritical(obj_segments, 0);


//@line:75

	((PointLight*) addr)->setLightMesh(segments,colorF,isGL20);
	
	env->ReleasePrimitiveArrayCritical(obj_segments, segments, 0);

}

JNIEXPORT void JNICALL Java_com_box2dLight_box2dLight_Box2dLight_jniSetShadowMesh(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_segments, jfloat colorF, jfloat softShadowLenght, jboolean isGL20) {
	float* segments = (float*)env->GetPrimitiveArrayCritical(obj_segments, 0);


//@line:79

	((PointLight*) addr)->setShadowMesh(segments,colorF,softShadowLenght,isGL20);
	
	env->ReleasePrimitiveArrayCritical(obj_segments, segments, 0);

}

JNIEXPORT void JNICALL Java_com_box2dLight_box2dLight_Box2dLight_jniSetSensorFilter(JNIEnv* env, jobject object, jlong addr, jboolean shouldCollide) {


//@line:113

	((PointLight*) addr)->setSensorFilter(shouldCollide);


}

JNIEXPORT void JNICALL Java_com_box2dLight_box2dLight_Box2dLight_jniSetContactFilter(JNIEnv* env, jclass clazz, jshort categoryBits, jshort groupIndex, jshort maskBits) {


//@line:118

	PointLight::setContactFilter((short)categoryBits,(short)groupIndex,(short)maskBits);


}

JNIEXPORT void JNICALL Java_com_box2dLight_box2dLight_Box2dLight_jniComputeOcclusion__JFFFFF(JNIEnv* env, jobject object, jlong addr, jfloat x, jfloat y, jfloat distance, jfloat direction, jfloat coneSize) {


//@line:123

	((PointLight*) addr)->computePoints(x, y, distance, direction, coneSize);


}

JNIEXPORT void JNICALL Java_com_box2dLight_box2dLight_Box2dLight_jniComputeOcclusion__JFFF(JNIEnv* env, jobject object, jlong addr, jfloat x, jfloat y, jfloat distance) {


//@line:127

	((PointLight*) addr)->computePoints(x, y, distance);


}

JNIEXPORT void JNICALL Java_com_box2dLight_box2dLight_Box2dLight_jniReleaseLight(JNIEnv* env, jobject object, jlong addr) {


//@line:131

	delete ((PointLight*) addr);


}

