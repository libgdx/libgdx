#include <com.badlogic.gdx.physics.box2d.ChainShape.h>

//@line:23

#include <Box2D/Box2D.h>
	 JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_ChainShape_newChainShape(JNIEnv* env, jobject object) {


//@line:32

		return (jlong)(new b2ChainShape());
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_ChainShape_jniCreateLoop(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_verts, jint numVertices) {
	float* verts = (float*)env->GetPrimitiveArrayCritical(obj_verts, 0);


//@line:64

		b2ChainShape* chain = (b2ChainShape*)addr;
		b2Vec2* verticesOut = new b2Vec2[numVertices];
		for( int i = 0; i < numVertices; i++ )
			verticesOut[i] = b2Vec2(verts[i<<1], verts[(i<<1)+1]);
		chain->CreateLoop( verticesOut, numVertices );
		delete[] verticesOut;
	
	env->ReleasePrimitiveArrayCritical(obj_verts, verts, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_ChainShape_jniCreateChain(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_verts, jint numVertices) {
	float* verts = (float*)env->GetPrimitiveArrayCritical(obj_verts, 0);


//@line:91

		b2ChainShape* chain = (b2ChainShape*)addr;
		b2Vec2* verticesOut = new b2Vec2[numVertices];
		for( int i = 0; i < numVertices; i++ )
			verticesOut[i] = b2Vec2(verts[i<<1], verts[(i<<1)+1]);
		chain->CreateChain( verticesOut, numVertices );
		delete[] verticesOut;
	
	env->ReleasePrimitiveArrayCritical(obj_verts, verts, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_ChainShape_jniSetPrevVertex(JNIEnv* env, jobject object, jlong addr, jfloat x, jfloat y) {


//@line:110

		b2ChainShape* chain = (b2ChainShape*)addr;
		chain->SetPrevVertex(b2Vec2(x, y));
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_ChainShape_jniSetNextVertex(JNIEnv* env, jobject object, jlong addr, jfloat x, jfloat y) {


//@line:125

		b2ChainShape* chain = (b2ChainShape*)addr;
		chain->SetNextVertex(b2Vec2(x, y));
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_ChainShape_jniGetVertexCount(JNIEnv* env, jobject object, jlong addr) {


//@line:135

		b2ChainShape* chain = (b2ChainShape*)addr;
		return chain->GetVertexCount();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_ChainShape_jniGetVertex(JNIEnv* env, jobject object, jlong addr, jint index, jfloatArray obj_verts) {
	float* verts = (float*)env->GetPrimitiveArrayCritical(obj_verts, 0);


//@line:151

		b2ChainShape* chain = (b2ChainShape*)addr;
		const b2Vec2 v = chain->GetVertex( index );
		verts[0] = v.x;
		verts[1] = v.y;
	
	env->ReleasePrimitiveArrayCritical(obj_verts, verts, 0);

}

