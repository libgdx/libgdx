#include <com.box2dLight.box2dLight.Box2dLight.h>

//@line:27

#include <Box2D/Collision/LightCalculus.h>
JNIEXPORT void JNICALL Java_com_box2dLight_box2dLight_Box2dLight_jniSetSensorFilter(JNIEnv* env, jobject object, jlong addr, jboolean shouldCollide) {


//@line:95

	((PointLight*) addr)->setSensorFilter(shouldCollide);


}

JNIEXPORT void JNICALL Java_com_box2dLight_box2dLight_Box2dLight_jniSetContactFilter(JNIEnv* env, jclass clazz, jshort categoryBits, jshort groupIndex, jshort maskBits) {


//@line:100

	PointLight::setContactFilter((short)categoryBits,(short)groupIndex,(short)maskBits);


}

JNIEXPORT void JNICALL Java_com_box2dLight_box2dLight_Box2dLight_jniComputeOcclusionMap__J_3FIFFFFF(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_points, jint nbPoints, jfloat x, jfloat y, jfloat distance, jfloat direction, jfloat coneSize) {
	float* points = (float*)env->GetPrimitiveArrayCritical(obj_points, 0);


//@line:105

	((PointLight*) addr)->computePoints(points, nbPoints, x, y, distance, direction, coneSize);

	env->ReleasePrimitiveArrayCritical(obj_points, points, 0);

}

JNIEXPORT void JNICALL Java_com_box2dLight_box2dLight_Box2dLight_jniComputeOcclusionMap__J_3FIFFF(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_points, jint nbPoints, jfloat x, jfloat y, jfloat distance) {
	float* points = (float*)env->GetPrimitiveArrayCritical(obj_points, 0);


//@line:109

	((PointLight*) addr)->computePoints(points, nbPoints, x, y, distance);

	env->ReleasePrimitiveArrayCritical(obj_points, points, 0);

}

JNIEXPORT void JNICALL Java_com_box2dLight_box2dLight_Box2dLight_jniReleaseLight(JNIEnv* env, jobject object, jlong addr) {


//@line:113

	delete ((PointLight*) addr);


}

