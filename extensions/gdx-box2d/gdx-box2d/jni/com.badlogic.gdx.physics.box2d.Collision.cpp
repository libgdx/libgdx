#include <com.badlogic.gdx.physics.box2d.Collision.h>

//@line:22

#include <Box2D/Box2D.h>

	static void fillTransform(b2Transform &xf, const float *fields) {
		xf.p.x = fields[0];
		xf.p.y = fields[1];
		xf.q.c = fields[2];
		xf.q.s = fields[3];
	}
	 JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_Collision_jniCreateManifold(JNIEnv* env, jclass clazz) {


//@line:35

		// NOTE: this leaks (if the class is initialized)
		return (jlong)(new b2Manifold);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Collision_jniCollideCircles(JNIEnv* env, jclass clazz, jlong addr, jlong shapeA, jfloatArray obj_transform1, jlong shapeB, jfloatArray obj_transform2) {
	float* transform1 = (float*)env->GetPrimitiveArrayCritical(obj_transform1, 0);
	float* transform2 = (float*)env->GetPrimitiveArrayCritical(obj_transform2, 0);


//@line:51

		b2Transform xfA, xfB;
		fillTransform(xfA, transform1);
		fillTransform(xfB, transform2);

		b2CollideCircles((b2Manifold*)addr, (b2CircleShape*)shapeA, xfA, (b2CircleShape*)shapeB, xfB);
	
	env->ReleasePrimitiveArrayCritical(obj_transform1, transform1, 0);
	env->ReleasePrimitiveArrayCritical(obj_transform2, transform2, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Collision_jniCollidePolygonAndCircle(JNIEnv* env, jclass clazz, jlong addr, jlong shapeA, jfloatArray obj_transform1, jlong shapeB, jfloatArray obj_transform2) {
	float* transform1 = (float*)env->GetPrimitiveArrayCritical(obj_transform1, 0);
	float* transform2 = (float*)env->GetPrimitiveArrayCritical(obj_transform2, 0);


//@line:66

		b2Transform xfA, xfB;
		fillTransform(xfA, transform1);
		fillTransform(xfB, transform2);

		b2CollidePolygonAndCircle((b2Manifold*)addr, (b2PolygonShape*)shapeA, xfA, (b2CircleShape*)shapeB, xfB);
	
	env->ReleasePrimitiveArrayCritical(obj_transform1, transform1, 0);
	env->ReleasePrimitiveArrayCritical(obj_transform2, transform2, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Collision_jniCollidePolygons(JNIEnv* env, jclass clazz, jlong addr, jlong shapeA, jfloatArray obj_transform1, jlong shapeB, jfloatArray obj_transform2) {
	float* transform1 = (float*)env->GetPrimitiveArrayCritical(obj_transform1, 0);
	float* transform2 = (float*)env->GetPrimitiveArrayCritical(obj_transform2, 0);


//@line:81

		b2Transform xfA, xfB;
		fillTransform(xfA, transform1);
		fillTransform(xfB, transform2);

		b2CollidePolygons((b2Manifold*)addr, (b2PolygonShape*)shapeA, xfA, (b2PolygonShape*)shapeB, xfB);
	
	env->ReleasePrimitiveArrayCritical(obj_transform1, transform1, 0);
	env->ReleasePrimitiveArrayCritical(obj_transform2, transform2, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Collision_jniCollideEdgeAndCircle(JNIEnv* env, jclass clazz, jlong addr, jlong shapeA, jfloatArray obj_transform1, jlong shapeB, jfloatArray obj_transform2) {
	float* transform1 = (float*)env->GetPrimitiveArrayCritical(obj_transform1, 0);
	float* transform2 = (float*)env->GetPrimitiveArrayCritical(obj_transform2, 0);


//@line:96

		b2Transform xfA, xfB;
		fillTransform(xfA, transform1);
		fillTransform(xfB, transform2);

		b2CollideEdgeAndCircle((b2Manifold*)addr, (b2EdgeShape*)shapeA, xfA, (b2CircleShape*)shapeB, xfB);
	
	env->ReleasePrimitiveArrayCritical(obj_transform1, transform1, 0);
	env->ReleasePrimitiveArrayCritical(obj_transform2, transform2, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_Collision_jniCollideEdgeAndPolygon(JNIEnv* env, jclass clazz, jlong addr, jlong shapeA, jfloatArray obj_transform1, jlong shapeB, jfloatArray obj_transform2) {
	float* transform1 = (float*)env->GetPrimitiveArrayCritical(obj_transform1, 0);
	float* transform2 = (float*)env->GetPrimitiveArrayCritical(obj_transform2, 0);


//@line:111

		b2Transform xfA, xfB;
		fillTransform(xfA, transform1);
		fillTransform(xfB, transform2);

		b2CollideEdgeAndPolygon((b2Manifold*)addr, (b2EdgeShape*)shapeA, xfA, (b2PolygonShape*)shapeB, xfB);
	
	env->ReleasePrimitiveArrayCritical(obj_transform1, transform1, 0);
	env->ReleasePrimitiveArrayCritical(obj_transform2, transform2, 0);

}

static inline jboolean wrapped_Java_com_badlogic_gdx_physics_box2d_Collision_jniTestOverlap
(JNIEnv* env, jclass clazz, jlong shapeA, jint indexA, jlong shapeB, jint indexB, jfloatArray obj_transform1, jfloatArray obj_transform2, float* transform1, float* transform2) {

//@line:124

		b2Transform xfA, xfB;
		fillTransform(xfA, transform1);
		fillTransform(xfB, transform2);

		return b2TestOverlap((b2Shape*)shapeA, indexA, (b2Shape*)shapeB, indexB, xfA, xfB);
	
}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_Collision_jniTestOverlap(JNIEnv* env, jclass clazz, jlong shapeA, jint indexA, jlong shapeB, jint indexB, jfloatArray obj_transform1, jfloatArray obj_transform2) {
	float* transform1 = (float*)env->GetPrimitiveArrayCritical(obj_transform1, 0);
	float* transform2 = (float*)env->GetPrimitiveArrayCritical(obj_transform2, 0);

	jboolean JNI_returnValue = wrapped_Java_com_badlogic_gdx_physics_box2d_Collision_jniTestOverlap(env, clazz, shapeA, indexA, shapeB, indexB, obj_transform1, obj_transform2, transform1, transform2);

	env->ReleasePrimitiveArrayCritical(obj_transform1, transform1, 0);
	env->ReleasePrimitiveArrayCritical(obj_transform2, transform2, 0);

	return JNI_returnValue;
}

