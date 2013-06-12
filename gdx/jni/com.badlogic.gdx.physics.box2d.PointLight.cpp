#include <com.badlogic.gdx.physics.box2d.PointLight.h>

//@line:25

#include <Box2D/Collision/LightCalculus.h>
JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_PointLight_jniSetSensorFilter(JNIEnv* env, jobject object, jlong addr, jboolean shouldCollide) {


//@line:92

	((PointLight*) addr)->setSensorFilter(shouldCollide);


}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_PointLight_jniSetContactFilter(JNIEnv* env, jclass clazz, jshort categoryBits, jshort groupIndex, jshort maskBits) {


//@line:97

	PointLight::setContactFilter((short)categoryBits,(short)groupIndex,(short)maskBits);


}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_PointLight_jniComputeOcclusionMap__J_3FIFFFFF(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_points, jint nbPoints, jfloat x, jfloat y, jfloat distance, jfloat direction, jfloat coneSize) {
	float* points = (float*)env->GetPrimitiveArrayCritical(obj_points, 0);


//@line:102

	((PointLight*) addr)->computePoints(points, nbPoints, x, y, distance, direction, coneSize);

	env->ReleasePrimitiveArrayCritical(obj_points, points, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_PointLight_jniComputeOcclusionMap__J_3FIFFF(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_points, jint nbPoints, jfloat x, jfloat y, jfloat distance) {
	float* points = (float*)env->GetPrimitiveArrayCritical(obj_points, 0);


//@line:106

	((PointLight*) addr)->computePoints(points, nbPoints, x, y, distance);

	env->ReleasePrimitiveArrayCritical(obj_points, points, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_PointLight_jniReleaseLight(JNIEnv* env, jobject object, jlong addr) {


//@line:110

	delete ((PointLight*) addr);


}

