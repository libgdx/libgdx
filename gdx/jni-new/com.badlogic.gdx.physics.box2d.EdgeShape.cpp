#include <com.badlogic.gdx.physics.box2d.EdgeShape.h>

//@line:9

#include <Box2d/Box2D.h>
	 JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_newEdgeShape(JNIEnv* env, jobject object) {


//@line:17

		return (jlong)(new b2EdgeShape());
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_jniSet(JNIEnv* env, jobject object, jlong addr, jfloat v1x, jfloat v1y, jfloat v2x, jfloat v2y) {


//@line:35

		b2EdgeShape* edge = (b2EdgeShape*)addr;
		edge->Set(b2Vec2(v1x, v1y), b2Vec2(v2x, v2y));
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_jniGetVertex1(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_vertex) {
	float* vertex = (float*)env->GetPrimitiveArrayCritical(obj_vertex, 0);


//@line:48

		b2EdgeShape* edge = (b2EdgeShape*)addr; 
		vertex[0] = edge->m_vertex1.x;
		vertex[1] = edge->m_vertex1.y;
	
	env->ReleasePrimitiveArrayCritical(obj_vertex, vertex, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_EdgeShape_jniGetVertex2(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_vertex) {
	float* vertex = (float*)env->GetPrimitiveArrayCritical(obj_vertex, 0);


//@line:60

		b2EdgeShape* edge = (b2EdgeShape*)addr;
		vertex[0] = edge->m_vertex2.x;
		vertex[1] = edge->m_vertex2.y;
	
	env->ReleasePrimitiveArrayCritical(obj_vertex, vertex, 0);

}

