#include <com.btdstudio.glbase.IMergeGroup.h>

//@line:6

	    #include "ImergeGroup.h"
	    #include "IpolygonMap.h"
	    #include "matrix.h"
	    #include "types.h"
    JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IMergeGroup_startGroup(JNIEnv* env, jclass clazz, jlong handle) {


//@line:83

		((IMergeGroup*)handle)->startGroup();
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IMergeGroup_endGroup(JNIEnv* env, jclass clazz, jlong handle) {


//@line:87

		((IMergeGroup*)handle)->endGroup();
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IMergeGroup_addPolygonMap(JNIEnv* env, jclass clazz, jlong handle, jlong pmHandle, jint targetPmId, jfloat x, jfloat y, jfloat z) {


//@line:91

		IPolygonMap* pm = (IPolygonMap*)pmHandle;
		((IMergeGroup*)handle)->addPolygonMap(pm, targetPmId, x, y, z);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IMergeGroup_addObject__JJIFFF(JNIEnv* env, jclass clazz, jlong handle, jlong objHandle, jint targetPmId, jfloat x, jfloat y, jfloat z) {


//@line:96

		IObject* obj = (IObject*)objHandle;
		((IMergeGroup*)handle)->addObject(obj, targetPmId, x, y, z);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IMergeGroup_addObject__JJIJ(JNIEnv* env, jclass clazz, jlong handle, jlong objHandle, jint targetPmId, jlong matrixHandle) {


//@line:101

		Matrix* matrix = (Matrix*)matrixHandle;
		
		IObject* obj = (IObject*)objHandle;
		((IMergeGroup*)handle)->addObject(obj, targetPmId, matrix);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IMergeGroup_addTriangles(JNIEnv* env, jclass clazz, jlong handle, jint vertices, jint triangles, jfloatArray obj_points, jshortArray obj_indices, jfloatArray obj_uvs) {
	float* points = (float*)env->GetPrimitiveArrayCritical(obj_points, 0);
	short* indices = (short*)env->GetPrimitiveArrayCritical(obj_indices, 0);
	float* uvs = (float*)env->GetPrimitiveArrayCritical(obj_uvs, 0);


//@line:109

		((IMergeGroup*)handle)->addTriangles(vertices, triangles, points, (const unsigned short*)indices, uvs);
	
	env->ReleasePrimitiveArrayCritical(obj_points, points, 0);
	env->ReleasePrimitiveArrayCritical(obj_indices, indices, 0);
	env->ReleasePrimitiveArrayCritical(obj_uvs, uvs, 0);

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IMergeGroup_addSprite__JFFFI(JNIEnv* env, jclass clazz, jlong handle, jfloat x, jfloat y, jfloat z, jint texture) {


//@line:113

		((IMergeGroup*)handle)->addSprite(x, y, z, texture);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IMergeGroup_addSprite__JFFFFFIFFFFZ(JNIEnv* env, jclass clazz, jlong handle, jfloat x, jfloat y, jfloat z, jfloat w, jfloat h, jint texture, jfloat sx, jfloat sy, jfloat sw, jfloat sh, jboolean flipV) {


//@line:118

		((IMergeGroup*)handle)->addSprite(x, y, z, w, h, texture, sx, sy, sw, sh, flipV);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IMergeGroup_addSprite__JIFFFFJZ(JNIEnv* env, jclass clazz, jlong handle, jint texture, jfloat sx, jfloat sy, jfloat sw, jfloat sh, jlong matrixHandle, jboolean flipV) {


//@line:123

		Matrix* transform = (Matrix*)matrixHandle;
		((IMergeGroup*)handle)->addSprite(texture, sx, sy, sw, sh, transform, flipV);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IMergeGroup_addSpriteUV(JNIEnv* env, jclass clazz, jlong handle, jfloat x, jfloat y, jfloat z, jfloat w, jfloat h, jfloat u, jfloat v, jfloat u2, jfloat v2) {


//@line:129

		((IMergeGroup*)handle)->addSpriteUV(x, y, z, w, h, u, v, u2, v2);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IMergeGroup_addQuad(JNIEnv* env, jclass clazz, jlong handle, jfloat v1x, jfloat v1y, jfloat v1u, jfloat v1v, jfloat v2x, jfloat v2y, jfloat v2u, jfloat v2v, jfloat v3x, jfloat v3y, jfloat v3u, jfloat v3v, jfloat v4x, jfloat v4y, jfloat v4u, jfloat v4v, jfloat z) {


//@line:139

		SimpleVertex v1 = { v1x, v1y, v1u, v1v };
		SimpleVertex v2 = { v2x, v2y, v2u, v2v };
		SimpleVertex v3 = { v3x, v3y, v3u, v3v };
		SimpleVertex v4 = { v4x, v4y, v4u, v4v };
		
		((IMergeGroup*)handle)->addQuad(&v1, &v2, &v3, &v4, z);
	

}

