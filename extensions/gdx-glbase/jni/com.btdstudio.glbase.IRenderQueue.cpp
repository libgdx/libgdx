#include <com.btdstudio.glbase.IRenderQueue.h>

//@line:6

	 	#include "IrenderQueue.h"
	    #include "drawCall.h"
	    #include "matrix.h"
    JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IRenderQueue_registerDrawCallNTV(JNIEnv* env, jclass clazz, jlong renderQueue, jlong drawCall) {


//@line:50

		DrawCall* dc = (DrawCall*)drawCall;
		((IRenderQueue*)renderQueue)->registerDrawCall(dc);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IRenderQueue_execRender(JNIEnv* env, jclass clazz, jlong renderQueue) {


//@line:55

		((IRenderQueue*)renderQueue)->execRender();
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IRenderQueue_setProjection(JNIEnv* env, jclass clazz, jlong renderQueue, jlong matrixHandle) {


//@line:59

		((IRenderQueue*)renderQueue)->setProjection((Matrix*)matrixHandle);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IRenderQueue_setView(JNIEnv* env, jclass clazz, jlong renderQueue, jlong matrixHandle) {


//@line:63

		((IRenderQueue*)renderQueue)->setView((Matrix*)matrixHandle);
	

}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IRenderQueue_getProjection(JNIEnv* env, jclass clazz, jlong renderQueue) {


//@line:67

		return (long long)((IRenderQueue*)renderQueue)->getProjection();
	

}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IRenderQueue_getView(JNIEnv* env, jclass clazz, jlong renderQueue) {


//@line:71

		return (long long)((IRenderQueue*)renderQueue)->getView();
	

}

static inline void wrapped_Java_com_btdstudio_glbase_IRenderQueue_setFog
(JNIEnv* env, jclass clazz, jlong renderQueue, jfloatArray obj_fogColor, jfloat fogNear, jfloat fogFar, float* fogColor) {

//@line:75

		return ((IRenderQueue*)renderQueue)->setFog(fogColor, fogNear, fogFar);
	
}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IRenderQueue_setFog(JNIEnv* env, jclass clazz, jlong renderQueue, jfloatArray obj_fogColor, jfloat fogNear, jfloat fogFar) {
	float* fogColor = (float*)env->GetPrimitiveArrayCritical(obj_fogColor, 0);

	wrapped_Java_com_btdstudio_glbase_IRenderQueue_setFog(env, clazz, renderQueue, obj_fogColor, fogNear, fogFar, fogColor);

	env->ReleasePrimitiveArrayCritical(obj_fogColor, fogColor, 0);

	return;
}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IRenderQueue_dispose(JNIEnv* env, jclass clazz, jlong handle) {


//@line:79

		delete (IRenderQueue*)handle;
	

}

