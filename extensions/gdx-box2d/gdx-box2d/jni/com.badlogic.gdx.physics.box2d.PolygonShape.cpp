#include <com.badlogic.gdx.physics.box2d.PolygonShape.h>

//@line:23

     #include <Box2D/Box2D.h>
	 JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_physics_box2d_PolygonShape_newPolygonShape(JNIEnv* env, jobject object) {


//@line:36

		b2PolygonShape* poly = new b2PolygonShape();
		return (jlong)poly;
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_PolygonShape_jniSet(JNIEnv* env, jobject object, jlong addr, jfloatArray obj_verts, jint offset, jint len) {
	float* verts = (float*)env->GetPrimitiveArrayCritical(obj_verts, 0);


//@line:70

		b2PolygonShape* poly = (b2PolygonShape*)addr;
		int numVertices = len / 2;
		b2Vec2* verticesOut = new b2Vec2[numVertices];
		for(int i = 0; i < numVertices; i++) { 
			verticesOut[i] = b2Vec2(verts[(i<<1) + offset], verts[(i<<1) + offset + 1]);
		}
		poly->Set(verticesOut, numVertices);
		delete[] verticesOut;
	 
	env->ReleasePrimitiveArrayCritical(obj_verts, verts, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_PolygonShape_jniSetAsBox__JFF(JNIEnv* env, jobject object, jlong addr, jfloat hx, jfloat hy) {


//@line:88

		b2PolygonShape* poly = (b2PolygonShape*)addr;
		poly->SetAsBox(hx, hy);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_PolygonShape_jniSetAsBox__JFFFFF(JNIEnv* env, jobject object, jlong addr, jfloat hx, jfloat hy, jfloat centerX, jfloat centerY, jfloat angle) {


//@line:102

		b2PolygonShape* poly = (b2PolygonShape*)addr;
		poly->SetAsBox( hx, hy, b2Vec2( centerX, centerY ), angle );
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_physics_box2d_PolygonShape_jniGetVertexCount(JNIEnv* env, jobject object, jlong addr) {


//@line:112

		b2PolygonShape* poly = (b2PolygonShape*)addr;
		return poly->GetVertexCount();
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_physics_box2d_PolygonShape_jniGetVertex(JNIEnv* env, jobject object, jlong addr, jint index, jfloatArray obj_verts) {
	float* verts = (float*)env->GetPrimitiveArrayCritical(obj_verts, 0);


//@line:128

		b2PolygonShape* poly = (b2PolygonShape*)addr;
		const b2Vec2 v = poly->GetVertex( index );
		verts[0] = v.x;
		verts[1] = v.y;
	
	env->ReleasePrimitiveArrayCritical(obj_verts, verts, 0);

}

