#include <com.badlogic.gdx.physics.box2d.EdgeShape.h>

//@line:25

#include <Box2D/Box2D.h>
	 JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_newEdgeShape(JNIEnv* env, jobject object) {


//@line:33

		return (jlong)(new b2EdgeShape());
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_jniSet(JNIEnv* env, jobject object, jlong addr, jfloat v1x, jfloat v1y, jfloat v2x, jfloat v2y) {


//@line:51

		b2EdgeShape* edge = (b2EdgeShape*)addr;
		edge->Set(b2Vec2(v1x, v1y), b2Vec2(v2x, v2y));
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_jniGetVertex1(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_vertex) {
	float* vertex = (float*)env->GetPrimitiveArrayCritical(obj_vertex, 0);


//@line:64

		b2EdgeShape* edge = (b2EdgeShape*)addr; 
		vertex[0] = edge->m_vertex1.x;
		vertex[1] = edge->m_vertex1.y;
	
	env->ReleasePrimitiveArrayCritical(obj_vertex, vertex, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_jniGetVertex2(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_vertex) {
	float* vertex = (float*)env->GetPrimitiveArrayCritical(obj_vertex, 0);


//@line:76

		b2EdgeShape* edge = (b2EdgeShape*)addr;
		vertex[0] = edge->m_vertex2.x;
		vertex[1] = edge->m_vertex2.y;
	
	env->ReleasePrimitiveArrayCritical(obj_vertex, vertex, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_jniGetVertex0(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_vertex) {
	float* vertex = (float*)env->GetPrimitiveArrayCritical(obj_vertex, 0);


//@line:88

		b2EdgeShape* edge = (b2EdgeShape*)addr;
		vertex[0] = edge->m_vertex0.x;
		vertex[1] = edge->m_vertex0.y;
	
	env->ReleasePrimitiveArrayCritical(obj_vertex, vertex, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_jniSetVertex0(JNIEnv* env, jobject object, jlong addr, jfloat x, jfloat y) {


//@line:102

		b2EdgeShape* edge = (b2EdgeShape*)addr;
		edge->m_vertex0.x = x;
		edge->m_vertex0.y = y;
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_jniGetVertex3(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_vertex) {
	float* vertex = (float*)env->GetPrimitiveArrayCritical(obj_vertex, 0);


//@line:114

		b2EdgeShape* edge = (b2EdgeShape*)addr;
		vertex[0] = edge->m_vertex3.x;
		vertex[1] = edge->m_vertex3.y;
	
	env->ReleasePrimitiveArrayCritical(obj_vertex, vertex, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_jniSetVertex3(JNIEnv* env, jobject object, jlong addr, jfloat x, jfloat y) {


//@line:128

		b2EdgeShape* edge = (b2EdgeShape*)addr;
		edge->m_vertex3.x = x;
		edge->m_vertex3.y = y;
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_jniHasVertex0(JNIEnv* env, jobject object, jlong addr) {


//@line:138

		b2EdgeShape* edge = (b2EdgeShape*)addr;
		return edge->m_hasVertex0;
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_jniSetHasVertex0(JNIEnv* env, jobject object, jlong addr, jboolean hasVertex0) {


//@line:147

		b2EdgeShape* edge = (b2EdgeShape*)addr;
		edge->m_hasVertex0 = hasVertex0;
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_jniHasVertex3(JNIEnv* env, jobject object, jlong addr) {


//@line:156

		b2EdgeShape* edge = (b2EdgeShape*)addr;
		return edge->m_hasVertex3;
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_jniSetHasVertex3(JNIEnv* env, jobject object, jlong addr, jboolean hasVertex3) {


//@line:165

		b2EdgeShape* edge = (b2EdgeShape*)addr;
		edge->m_hasVertex3 = hasVertex3;
	

}

