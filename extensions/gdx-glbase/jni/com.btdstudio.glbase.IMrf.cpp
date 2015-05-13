#include <com.btdstudio.glbase.IMrf.h>

//@line:8

	    #include "Imrf.h"
	    #include "IrenderQueue.h"
	    #include "Iobject.h"
	    #include "drawCall.h"
	    #include <string.h>
	    
	    // Temp drawcall list
	    int tempDcNum;
	    DrawCall* tempDcList;
    JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IMrf_initialize(JNIEnv* env, jclass clazz, jint screenWidth, jint screenHeight) {


//@line:30

		IMrf::initialize(screenWidth, screenHeight);
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IMrf_getFramebuffer(JNIEnv* env, jclass clazz, jint renderTarget) {


//@line:34

		return IMrf::getFramebuffer(renderTarget);
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IMrf_getDrawCallsNum(JNIEnv* env, jclass clazz) {


//@line:86

		return tempDcNum;
	

}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IMrf_getDrawCall(JNIEnv* env, jclass clazz, jint pos) {


//@line:90

		return (long long)&tempDcList[pos];
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IMrf_registerDrawCalls(JNIEnv* env, jclass clazz, jlong handle, jlong queueHandle, jlong paramsHandle, jlong objectHandle) {


//@line:94

		IRenderQueue* queue = (IRenderQueue*)queueHandle;
		BasicRenderParameters* params = (BasicRenderParameters*)paramsHandle;
		IObject* object = (IObject*)objectHandle;
		
		((IMrf*)handle)->registerDrawCalls(queue, params, object);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IMrf_prepareDrawCalls(JNIEnv* env, jclass clazz, jlong handle, jlong paramsHandle, jlong objectHandle) {


//@line:102

		BasicRenderParameters* params = (BasicRenderParameters*)paramsHandle;
		IObject* object = (IObject*)objectHandle;
		
		tempDcNum = ((IMrf*)handle)->prepareDrawCalls(tempDcList, params, object);
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IMrf_getPassesNum(JNIEnv* env, jclass clazz, jlong handle) {


//@line:109

		return ((IMrf*)handle)->getPassesNum();
	

}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IMrf_getPass(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:113

		return (long long)((IMrf*)handle)->getPass(pos);
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IMrf_getIdLength(JNIEnv* env, jclass clazz, jlong handle) {


//@line:117

		return strlen(((IMrf*)handle)->getId());
	

}

JNIEXPORT jchar JNICALL Java_com_btdstudio_glbase_IMrf_getId(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:121

		return ((IMrf*)handle)->getId()[pos];
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IMrf_setUniforms(JNIEnv* env, jclass clazz, jlong handle) {


//@line:125

		((IMrf*)handle)->setUniforms();
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IMrf_dispose(JNIEnv* env, jclass clazz, jlong handle) {


//@line:129

		delete (IMrf*)handle;
	

}

