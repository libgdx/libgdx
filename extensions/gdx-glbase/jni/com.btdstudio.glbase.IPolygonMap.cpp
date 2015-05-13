#include <com.btdstudio.glbase.IPolygonMap.h>

//@line:5

	 	#include "IpolygonMap.h"
	    #include <string.h>
    JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IPolygonMap_getSurface(JNIEnv* env, jclass clazz, jlong handle) {


//@line:60

		return (long long)((IPolygonMap*)handle)->getSurface();
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IPolygonMap_getUvsNum(JNIEnv* env, jclass clazz, jlong handle) {


//@line:64

		return ((IPolygonMap*)handle)->getUvsBufferLength();
	

}

JNIEXPORT jfloat JNICALL Java_com_btdstudio_glbase_IPolygonMap_getUvsBuffer(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:68

		return ((IPolygonMap*)handle)->getUvsBuffer()[pos];
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IPolygonMap_getPolygonsNum(JNIEnv* env, jclass clazz, jlong handle) {


//@line:72

		return ((IPolygonMap*)handle)->getPolygonsBufferLength();
	

}

JNIEXPORT jshort JNICALL Java_com_btdstudio_glbase_IPolygonMap_getPolygonsBuffer(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:76

		return ((IPolygonMap*)handle)->getPolygonsBuffer()[pos];
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IPolygonMap_commitUVsChanges(JNIEnv* env, jclass clazz, jlong handle, jfloatArray obj_values) {
	float* values = (float*)env->GetPrimitiveArrayCritical(obj_values, 0);


//@line:80

		float* old = ((IPolygonMap*)handle)->getUvsBuffer();
		int numPoints = ((IPolygonMap*)handle)->getUvsBufferLength();
		memcpy(old, values,  numPoints*2*sizeof(float));
		
		((IPolygonMap*)handle)->commitUvsChanges();
	
	env->ReleasePrimitiveArrayCritical(obj_values, values, 0);

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IPolygonMap_commitPolygonsChanges(JNIEnv* env, jclass clazz, jlong handle, jshortArray obj_values) {
	short* values = (short*)env->GetPrimitiveArrayCritical(obj_values, 0);


//@line:88

		unsigned short* old = ((IPolygonMap*)handle)->getPolygonsBuffer();
		int numPoints = ((IPolygonMap*)handle)->getPolygonsBufferLength();
		memcpy(old, values,  numPoints*3*sizeof(unsigned short));
		
		((IPolygonMap*)handle)->commitPolygonsChanges();
	
	env->ReleasePrimitiveArrayCritical(obj_values, values, 0);

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IPolygonMap_getSurfaceNameLength(JNIEnv* env, jclass clazz, jlong handle) {


//@line:96

		return strlen(((IPolygonMap*)handle)->getSurfaceName());
	

}

JNIEXPORT jchar JNICALL Java_com_btdstudio_glbase_IPolygonMap_getSurfaceName(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:100

		return ((IPolygonMap*)handle)->getSurfaceName()[pos];
	

}

