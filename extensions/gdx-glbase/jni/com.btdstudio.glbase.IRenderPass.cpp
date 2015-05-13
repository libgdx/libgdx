#include <com.btdstudio.glbase.IRenderPass.h>

//@line:5

	    #include "IrenderPass.h"
	    #include <string.h>
    JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IRenderPass_getMumLength(JNIEnv* env, jclass clazz, jlong handle) {


//@line:61

		return strlen(((IRenderPass*)handle)->getMuM());
	

}

JNIEXPORT jchar JNICALL Java_com_btdstudio_glbase_IRenderPass_getMum(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:65

		return ((IRenderPass*)handle)->getMuM()[pos];
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IRenderPass_getIdLength(JNIEnv* env, jclass clazz, jlong handle) {


//@line:69

		return strlen(((IRenderPass*)handle)->getId());
	

}

JNIEXPORT jchar JNICALL Java_com_btdstudio_glbase_IRenderPass_getId(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:73

		return ((IRenderPass*)handle)->getId()[pos];
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IRenderPass_getUniformsNum(JNIEnv* env, jclass clazz, jlong handle) {


//@line:77

		return ((IRenderPass*)handle)->getUniformsNum();
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IRenderPass_getUniformNameLength(JNIEnv* env, jclass clazz, jlong handle, jint uniformIndex) {


//@line:81

		return strlen(((IRenderPass*)handle)->getUniformName(uniformIndex));
	

}

JNIEXPORT jchar JNICALL Java_com_btdstudio_glbase_IRenderPass_getUniformName(JNIEnv* env, jclass clazz, jlong handle, jint uniformIndex, jint pos) {


//@line:85

		return ((IRenderPass*)handle)->getUniformName(uniformIndex)[pos];
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IRenderPass_getUniformSize(JNIEnv* env, jclass clazz, jlong handle, jint uniformIndex) {


//@line:89

		return ((IRenderPass*)handle)->getUniformSize(uniformIndex);
	

}

JNIEXPORT jfloat JNICALL Java_com_btdstudio_glbase_IRenderPass_getUniformValue(JNIEnv* env, jclass clazz, jlong handle, jint uniformIndex, jint pos) {


//@line:93

		return ((IRenderPass*)handle)->getUniformValues(uniformIndex)[pos];
	

}

