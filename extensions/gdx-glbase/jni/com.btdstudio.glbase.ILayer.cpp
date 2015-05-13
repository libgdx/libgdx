#include <com.btdstudio.glbase.ILayer.h>

//@line:5

	 	#include "types.h"
	    #include "Ilayer.h"
	    #include "arrayList.h"
	    #include "macros.h"
	    #include <string.h>
	    
	    // Temp
	    ArrayList tempList(128);
    JNIEXPORT void JNICALL Java_com_btdstudio_glbase_ILayer_addMatrixIndicesName(JNIEnv* env, jclass clazz, jstring obj_name) {
	char* name = (char*)env->GetStringUTFChars(obj_name, 0);


//@line:123

		tempList.add(strdup2(name));
	
	env->ReleaseStringUTFChars(obj_name, name);

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_ILayer_setMatrixIndicesNames(JNIEnv* env, jclass clazz, jlong handle) {


//@line:127

		((ILayer*)handle)->setMatrixIndicesNames(&tempList);
		
		// Clear array
		for( int i=0; i<tempList.getSize(); i++ ){
			delete (char*)tempList.get(i);
		}
		
		tempList.clear();
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_ILayer_getNameLength(JNIEnv* env, jclass clazz, jlong handle) {


//@line:138

		return strlen(((ILayer*)handle)->getName());
	

}

JNIEXPORT jchar JNICALL Java_com_btdstudio_glbase_ILayer_getName(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:142

		return ((ILayer*)handle)->getName()[pos];
	

}

JNIEXPORT jfloat JNICALL Java_com_btdstudio_glbase_ILayer_getBoundingBox(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:146

		return ((ILayer*)handle)->getBoundingBox()[pos];
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_ILayer_getPointsNum(JNIEnv* env, jclass clazz, jlong handle) {


//@line:150

		return ((ILayer*)handle)->getPointsNum();
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_ILayer_getPolygonMapsNum(JNIEnv* env, jclass clazz, jlong handle) {


//@line:154

		return ((ILayer*)handle)->getPolygonMaps()->getSize();
	

}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_ILayer_getPolygonMap(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:158

		return (long long)((ILayer*)handle)->getPolygonMaps()->get(pos);
	

}

JNIEXPORT jfloat JNICALL Java_com_btdstudio_glbase_ILayer_getPointsBuffer(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:162

		return ((ILayer*)handle)->getPointsBuffer()[pos];
	

}

JNIEXPORT jfloat JNICALL Java_com_btdstudio_glbase_ILayer_getVcolorsBuffer(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:166

		return ((ILayer*)handle)->getVcolorsBuffer()[pos];
	

}

JNIEXPORT jfloat JNICALL Java_com_btdstudio_glbase_ILayer_getNormalsBuffer(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:170

		return ((ILayer*)handle)->getNormalsBuffer()[pos];
	

}

JNIEXPORT jbyte JNICALL Java_com_btdstudio_glbase_ILayer_getMatrixIndicesBuffer(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:174

		return ((ILayer*)handle)->getMatrixIndicesBuffer()[pos];
	

}

JNIEXPORT jfloat JNICALL Java_com_btdstudio_glbase_ILayer_getMatrixWeightsBuffer(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:178

		return ((ILayer*)handle)->getMatrixWeightsBuffer()[pos];
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_ILayer_commitPointsChanges(JNIEnv* env, jclass clazz, jlong handle, jfloatArray obj_points) {
	float* points = (float*)env->GetPrimitiveArrayCritical(obj_points, 0);


//@line:182

		float* oldPoints = ((ILayer*)handle)->getPointsBuffer();
		int numPoints = ((ILayer*)handle)->getPointsNum();
		memcpy(oldPoints, points,  numPoints*3*sizeof(float));
		
		((ILayer*)handle)->commitPointsChanges();
	
	env->ReleasePrimitiveArrayCritical(obj_points, points, 0);

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_ILayer_commitVcolorChanges(JNIEnv* env, jclass clazz, jlong handle, jfloatArray obj_values) {
	float* values = (float*)env->GetPrimitiveArrayCritical(obj_values, 0);


//@line:190

		float* old = ((ILayer*)handle)->getVcolorsBuffer();
		int numPoints = ((ILayer*)handle)->getPointsNum();
		memcpy(old, values,  numPoints*4*sizeof(float));
		
		((ILayer*)handle)->commitVcolorsChanges();
	
	env->ReleasePrimitiveArrayCritical(obj_values, values, 0);

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_ILayer_commitNormalsChanges(JNIEnv* env, jclass clazz, jlong handle, jfloatArray obj_values) {
	float* values = (float*)env->GetPrimitiveArrayCritical(obj_values, 0);


//@line:198

		float* old = ((ILayer*)handle)->getNormalsBuffer();
		int numPoints = ((ILayer*)handle)->getPointsNum();
		memcpy(old, values,  numPoints*3*sizeof(float));
		
		((ILayer*)handle)->commitNormalsChanges();
	
	env->ReleasePrimitiveArrayCritical(obj_values, values, 0);

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_ILayer_commitMatrixIndicesChanges(JNIEnv* env, jclass clazz, jlong handle, jbyteArray obj_values) {
	char* values = (char*)env->GetPrimitiveArrayCritical(obj_values, 0);


//@line:206

		byte* old = ((ILayer*)handle)->getMatrixIndicesBuffer();
		int numPoints = ((ILayer*)handle)->getPointsNum();
		memcpy(old, values,  numPoints*4*sizeof(byte));
		
		((ILayer*)handle)->commitMatrixIndicesChanges();
	
	env->ReleasePrimitiveArrayCritical(obj_values, values, 0);

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_ILayer_commitMatrixWeightsChanges(JNIEnv* env, jclass clazz, jlong handle, jfloatArray obj_values) {
	float* values = (float*)env->GetPrimitiveArrayCritical(obj_values, 0);


//@line:214

		float* old = ((ILayer*)handle)->getMatrixWeightsBuffer();
		int numPoints = ((ILayer*)handle)->getPointsNum();
		memcpy(old, values,  numPoints*4*sizeof(float));
		
		((ILayer*)handle)->commitMatrixWeightsChanges();
	
	env->ReleasePrimitiveArrayCritical(obj_values, values, 0);

}

