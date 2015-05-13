#include <com.btdstudio.glbase.IGLBase.h>

//@line:15

	    #include "Iglbase.h"
	    #include "Iobject.h"
	    #include "IpolygonMap.h"
	    #include "IanimationPlayer.h"
	    #include "renderEnums.h"
	    
	    #ifdef __ANDROID__
	    #include <android/log.h>
	    
	    int logi(char const* fmt, va_list arglist)
		{
		  return __android_log_vprint(ANDROID_LOG_INFO, "glbase", fmt, arglist);
		}
		
		int loge(char const* fmt, va_list arglist)
		{
		  return __android_log_vprint(ANDROID_LOG_ERROR, "glbase", fmt, arglist);
		}
	    #else
	    int log(char const* fmt, va_list arglist)
	    {
	    	int res = vprintf(fmt, arglist);
	    	printf("\n");
	    	fflush(stdout);
	    	return res;
	    }
	    #endif
    JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IGLBase_intialize(JNIEnv* env, jclass clazz, jint screenWidth, jint screenHeight) {


//@line:48
    
        // Set log function
        #ifdef __ANDROID__
        IGLBase::get()->setTraceFunction(&logi);
  		IGLBase::get()->setEtraceFunction(&loge);
        #else
        IGLBase::get()->setTraceFunction(&log);
  		IGLBase::get()->setEtraceFunction(&log);
        #endif
        
        IGLBase::get()->initialize(screenWidth, screenHeight);
    

}

static inline jint wrapped_Java_com_btdstudio_glbase_IGLBase_loadShader__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2
(JNIEnv* env, jclass clazz, jstring obj_vertexCode, jstring obj_fragmentCode, jstring obj_vShaderName, jstring obj_fShaderName, char* vertexCode, char* fragmentCode, char* vShaderName, char* fShaderName) {

//@line:99

    	return IGLBase::get()->loadShader(vertexCode, fragmentCode, vShaderName, fShaderName);
    
}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IGLBase_loadShader__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2(JNIEnv* env, jclass clazz, jstring obj_vertexCode, jstring obj_fragmentCode, jstring obj_vShaderName, jstring obj_fShaderName) {
	char* vertexCode = (char*)env->GetStringUTFChars(obj_vertexCode, 0);
	char* fragmentCode = (char*)env->GetStringUTFChars(obj_fragmentCode, 0);
	char* vShaderName = (char*)env->GetStringUTFChars(obj_vShaderName, 0);
	char* fShaderName = (char*)env->GetStringUTFChars(obj_fShaderName, 0);

	jint JNI_returnValue = wrapped_Java_com_btdstudio_glbase_IGLBase_loadShader__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2(env, clazz, obj_vertexCode, obj_fragmentCode, obj_vShaderName, obj_fShaderName, vertexCode, fragmentCode, vShaderName, fShaderName);

	env->ReleaseStringUTFChars(obj_vertexCode, vertexCode);
	env->ReleaseStringUTFChars(obj_fragmentCode, fragmentCode);
	env->ReleaseStringUTFChars(obj_vShaderName, vShaderName);
	env->ReleaseStringUTFChars(obj_fShaderName, fShaderName);

	return JNI_returnValue;
}

static inline jint wrapped_Java_com_btdstudio_glbase_IGLBase_loadShader__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2
(JNIEnv* env, jclass clazz, jstring obj_vertexCode, jstring obj_fragmentCode, jstring obj_programName, char* vertexCode, char* fragmentCode, char* programName) {

//@line:103

    	return IGLBase::get()->loadShader(vertexCode, fragmentCode, programName);
    
}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IGLBase_loadShader__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2(JNIEnv* env, jclass clazz, jstring obj_vertexCode, jstring obj_fragmentCode, jstring obj_programName) {
	char* vertexCode = (char*)env->GetStringUTFChars(obj_vertexCode, 0);
	char* fragmentCode = (char*)env->GetStringUTFChars(obj_fragmentCode, 0);
	char* programName = (char*)env->GetStringUTFChars(obj_programName, 0);

	jint JNI_returnValue = wrapped_Java_com_btdstudio_glbase_IGLBase_loadShader__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2(env, clazz, obj_vertexCode, obj_fragmentCode, obj_programName, vertexCode, fragmentCode, programName);

	env->ReleaseStringUTFChars(obj_vertexCode, vertexCode);
	env->ReleaseStringUTFChars(obj_fragmentCode, fragmentCode);
	env->ReleaseStringUTFChars(obj_programName, programName);

	return JNI_returnValue;
}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IGLBase_addShaderUniform(JNIEnv* env, jclass clazz, jint shaderId, jint uniformPos, jstring obj_uniformName) {
	char* uniformName = (char*)env->GetStringUTFChars(obj_uniformName, 0);


//@line:107

    	IGLBase::get()->addShaderUniform(shaderId, uniformPos, uniformName);
    
	env->ReleaseStringUTFChars(obj_uniformName, uniformName);

}

static inline jint wrapped_Java_com_btdstudio_glbase_IGLBase_getShaderProgramID
(JNIEnv* env, jclass clazz, jstring obj_shaderName, char* shaderName) {

//@line:111

    	return IGLBase::get()->getShaderProgramID(shaderName);
    
}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IGLBase_getShaderProgramID(JNIEnv* env, jclass clazz, jstring obj_shaderName) {
	char* shaderName = (char*)env->GetStringUTFChars(obj_shaderName, 0);

	jint JNI_returnValue = wrapped_Java_com_btdstudio_glbase_IGLBase_getShaderProgramID(env, clazz, obj_shaderName, shaderName);

	env->ReleaseStringUTFChars(obj_shaderName, shaderName);

	return JNI_returnValue;
}

JNIEXPORT jboolean JNICALL Java_com_btdstudio_glbase_IGLBase_hasMyUniform(JNIEnv* env, jclass clazz, jint shaderID, jint uniformPos) {


//@line:115

    	return IGLBase::get()->hasMyUniform(shaderID, uniformPos);
    

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IGLBase_deleteTexture(JNIEnv* env, jclass clazz, jint texture) {


//@line:140

    	return IGLBase::get()->deleteTexture(texture);
    

}

static inline jint wrapped_Java_com_btdstudio_glbase_IGLBase_createTexture
(JNIEnv* env, jclass clazz, jint width, jint height, jint format, jint pixelFormat, jboolean repeat, jbyteArray obj_data, jboolean filterLinear, char* data) {

//@line:170

	    return IGLBase::get()->createTexture(width, height, format, pixelFormat, repeat, (byte*)data, filterLinear);
	
}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IGLBase_createTexture(JNIEnv* env, jclass clazz, jint width, jint height, jint format, jint pixelFormat, jboolean repeat, jbyteArray obj_data, jboolean filterLinear) {
	char* data = (char*)env->GetPrimitiveArrayCritical(obj_data, 0);

	jint JNI_returnValue = wrapped_Java_com_btdstudio_glbase_IGLBase_createTexture(env, clazz, width, height, format, pixelFormat, repeat, obj_data, filterLinear, data);

	env->ReleasePrimitiveArrayCritical(obj_data, data, 0);

	return JNI_returnValue;
}

static inline jint wrapped_Java_com_btdstudio_glbase_IGLBase_createTextureCube
(JNIEnv* env, jclass clazz, jint width, jint height, jint format, jint pixelFormat, jboolean repeat, jbyteArray obj_data1, jbyteArray obj_data2, jbyteArray obj_data3, jbyteArray obj_data4, jbyteArray obj_data5, jbyteArray obj_data6, jboolean filterLinear, char* data1, char* data2, char* data3, char* data4, char* data5, char* data6) {

//@line:176

		byte* data[6] = { (byte*)data1, (byte*)data2, (byte*)data3, (byte*)data4, (byte*)data5, (byte*)data6 };
		return IGLBase::get()->createTextureCube(width, height, format, pixelFormat, repeat, data, filterLinear);
	
}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IGLBase_createTextureCube(JNIEnv* env, jclass clazz, jint width, jint height, jint format, jint pixelFormat, jboolean repeat, jbyteArray obj_data1, jbyteArray obj_data2, jbyteArray obj_data3, jbyteArray obj_data4, jbyteArray obj_data5, jbyteArray obj_data6, jboolean filterLinear) {
	char* data1 = (char*)env->GetPrimitiveArrayCritical(obj_data1, 0);
	char* data2 = (char*)env->GetPrimitiveArrayCritical(obj_data2, 0);
	char* data3 = (char*)env->GetPrimitiveArrayCritical(obj_data3, 0);
	char* data4 = (char*)env->GetPrimitiveArrayCritical(obj_data4, 0);
	char* data5 = (char*)env->GetPrimitiveArrayCritical(obj_data5, 0);
	char* data6 = (char*)env->GetPrimitiveArrayCritical(obj_data6, 0);

	jint JNI_returnValue = wrapped_Java_com_btdstudio_glbase_IGLBase_createTextureCube(env, clazz, width, height, format, pixelFormat, repeat, obj_data1, obj_data2, obj_data3, obj_data4, obj_data5, obj_data6, filterLinear, data1, data2, data3, data4, data5, data6);

	env->ReleasePrimitiveArrayCritical(obj_data1, data1, 0);
	env->ReleasePrimitiveArrayCritical(obj_data2, data2, 0);
	env->ReleasePrimitiveArrayCritical(obj_data3, data3, 0);
	env->ReleasePrimitiveArrayCritical(obj_data4, data4, 0);
	env->ReleasePrimitiveArrayCritical(obj_data5, data5, 0);
	env->ReleasePrimitiveArrayCritical(obj_data6, data6, 0);

	return JNI_returnValue;
}

static inline jboolean wrapped_Java_com_btdstudio_glbase_IGLBase_copySubImage
(JNIEnv* env, jclass clazz, jint texture, jint offsetX, jint offsetY, jint width, jint height, jint format, jint pixelFormat, jbyteArray obj_subData, char* subData) {

//@line:182

		return IGLBase::get()->copySubImage(texture, offsetX, offsetY, width, height, format, pixelFormat, (byte*)subData);
	
}

JNIEXPORT jboolean JNICALL Java_com_btdstudio_glbase_IGLBase_copySubImage(JNIEnv* env, jclass clazz, jint texture, jint offsetX, jint offsetY, jint width, jint height, jint format, jint pixelFormat, jbyteArray obj_subData) {
	char* subData = (char*)env->GetPrimitiveArrayCritical(obj_subData, 0);

	jboolean JNI_returnValue = wrapped_Java_com_btdstudio_glbase_IGLBase_copySubImage(env, clazz, texture, offsetX, offsetY, width, height, format, pixelFormat, obj_subData, subData);

	env->ReleasePrimitiveArrayCritical(obj_subData, subData, 0);

	return JNI_returnValue;
}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IGLBase_createFBO(JNIEnv* env, jclass clazz) {


//@line:186

    	return IGLBase::get()->createFBO();
    

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IGLBase_setFBOTexture(JNIEnv* env, jclass clazz, jint fbo, jint texture, jboolean createDepth) {


//@line:190

    	IGLBase::get()->setFBOTexture(fbo, texture, createDepth);
    

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IGLBase_setAnimationParent(JNIEnv* env, jclass clazz, jint child, jint parent) {


//@line:202

    	IGLBase::get()->setAnimationParent(child, parent);
    

}

JNIEXPORT jboolean JNICALL Java_com_btdstudio_glbase_IGLBase_deleteAnimation(JNIEnv* env, jclass clazz, jint animationID) {


//@line:206

    	return IGLBase::get()->deleteAnimation(animationID);
    

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IGLBase_setViewport(JNIEnv* env, jclass clazz, jint framebuffer, jint left, jint top, jint right, jint bottom) {


//@line:234

		IGLBase::get()->setViewport(framebuffer, left, top, right, bottom);
	

}

JNIEXPORT jboolean JNICALL Java_com_btdstudio_glbase_IGLBase_getFboUsed(JNIEnv* env, jclass clazz, jint index) {


//@line:280

		return IGLBase::get()->getFboUsed()[index];
	

}

JNIEXPORT jdouble JNICALL Java_com_btdstudio_glbase_IGLBase_getTime(JNIEnv* env, jclass clazz) {


//@line:284

		return IGLBase::get()->getTime();
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IGLBase_onResume(JNIEnv* env, jclass clazz) {


//@line:288

		IGLBase::get()->onResume();
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IGLBase_updateTimer(JNIEnv* env, jclass clazz, jint millis) {


//@line:292

		timeval t;
		t.tv_sec = 0;
		t.tv_usec = millis*1000;
		
		IGLBase::get()->updateTimer(t);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IGLBase_resetGLState(JNIEnv* env, jclass clazz) {


//@line:300

		IGLBase::get()->resetGLState();
	

}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IGLBase_createRenderQueueNTV(JNIEnv* env, jclass clazz) {


//@line:306

    	return (long long)IGLBase::get()->createRenderQueue();
    

}

static inline jlong wrapped_Java_com_btdstudio_glbase_IGLBase_loadBo3NTV
(JNIEnv* env, jclass clazz, jbyteArray obj_data, jint length, jboolean gpuOnly, char* data) {

//@line:310

    	return (long long)IGLBase::get()->loadBo3((byte*)data, length, gpuOnly);
    
}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IGLBase_loadBo3NTV(JNIEnv* env, jclass clazz, jbyteArray obj_data, jint length, jboolean gpuOnly) {
	char* data = (char*)env->GetPrimitiveArrayCritical(obj_data, 0);

	jlong JNI_returnValue = wrapped_Java_com_btdstudio_glbase_IGLBase_loadBo3NTV(env, clazz, obj_data, length, gpuOnly, data);

	env->ReleasePrimitiveArrayCritical(obj_data, data, 0);

	return JNI_returnValue;
}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IGLBase_subObjectNTV(JNIEnv* env, jclass clazz, jlong object, jint vertexOffset, jint triangleOffset, jint vertexLength, jint triangleLength, jint layer, jint polygonMap) {


//@line:315

    	IObject* parent = (IObject*)object;
    	return (long long)IGLBase::get()->subObject(parent, vertexOffset, triangleOffset, 
    			vertexLength, triangleLength, layer, polygonMap);
    

}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IGLBase_createMergeGroupNTV__JII(JNIEnv* env, jclass clazz, jlong object, jint maxVertices, jint polygonMaps) {


//@line:321

    	IObject* parent = (IObject*)object;
    	return (long long)IGLBase::get()->createMergeGroup(parent, maxVertices, polygonMaps);
    

}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IGLBase_createMergeGroupNTV__I(JNIEnv* env, jclass clazz, jint maxVertices) {


//@line:326

		return (long long)IGLBase::get()->createMergeGroup(maxVertices);
	

}

static inline jint wrapped_Java_com_btdstudio_glbase_IGLBase_loadAnimation
(JNIEnv* env, jclass clazz, jbyteArray obj_bsFile, jint bsFileLength, jbyteArray obj_binFile, jint binFileLength, char* bsFile, char* binFile) {

//@line:330

    	return IGLBase::get()->loadAnimation(bsFile, bsFileLength, (byte*)binFile, binFileLength);
    
}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IGLBase_loadAnimation(JNIEnv* env, jclass clazz, jbyteArray obj_bsFile, jint bsFileLength, jbyteArray obj_binFile, jint binFileLength) {
	char* bsFile = (char*)env->GetPrimitiveArrayCritical(obj_bsFile, 0);
	char* binFile = (char*)env->GetPrimitiveArrayCritical(obj_binFile, 0);

	jint JNI_returnValue = wrapped_Java_com_btdstudio_glbase_IGLBase_loadAnimation(env, clazz, obj_bsFile, bsFileLength, obj_binFile, binFileLength, bsFile, binFile);

	env->ReleasePrimitiveArrayCritical(obj_bsFile, bsFile, 0);
	env->ReleasePrimitiveArrayCritical(obj_binFile, binFile, 0);

	return JNI_returnValue;
}

static inline jint wrapped_Java_com_btdstudio_glbase_IGLBase_loadAnimationNTV
(JNIEnv* env, jclass clazz, jbyteArray obj_bbmFile, jint bbmFileLength, char* bbmFile) {

//@line:334

    	return IGLBase::get()->loadAnimation((byte*)bbmFile, bbmFileLength);
    
}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IGLBase_loadAnimationNTV(JNIEnv* env, jclass clazz, jbyteArray obj_bbmFile, jint bbmFileLength) {
	char* bbmFile = (char*)env->GetPrimitiveArrayCritical(obj_bbmFile, 0);

	jint JNI_returnValue = wrapped_Java_com_btdstudio_glbase_IGLBase_loadAnimationNTV(env, clazz, obj_bbmFile, bbmFileLength, bbmFile);

	env->ReleasePrimitiveArrayCritical(obj_bbmFile, bbmFile, 0);

	return JNI_returnValue;
}

static inline jlong wrapped_Java_com_btdstudio_glbase_IGLBase_loadMrfNTV
(JNIEnv* env, jclass clazz, jstring obj_mrfFile, jboolean skipSetUniforms, char* mrfFile) {

//@line:338

		return (long long)IGLBase::get()->loadMrf(mrfFile, skipSetUniforms);
	
}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IGLBase_loadMrfNTV(JNIEnv* env, jclass clazz, jstring obj_mrfFile, jboolean skipSetUniforms) {
	char* mrfFile = (char*)env->GetStringUTFChars(obj_mrfFile, 0);

	jlong JNI_returnValue = wrapped_Java_com_btdstudio_glbase_IGLBase_loadMrfNTV(env, clazz, obj_mrfFile, skipSetUniforms, mrfFile);

	env->ReleaseStringUTFChars(obj_mrfFile, mrfFile);

	return JNI_returnValue;
}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IGLBase_loadAnimationPlayerNTV(JNIEnv* env, jclass clazz, jint animation, jlong object) {


//@line:342

		IObject* target = (IObject*)object;
		return (long long)IGLBase::get()->loadAnimationPlayer(animation, target);
	

}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IGLBase_loadAnimationPlayerPMNTV(JNIEnv* env, jclass clazz, jint animation, jlong pm) {


//@line:347

		IPolygonMap* target = (IPolygonMap*)pm;
		return (long long)IGLBase::get()->loadAnimationPlayer(animation, target);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IGLBase_adaptTo(JNIEnv* env, jclass clazz, jlong animationPlayerHandle, jlong objectHandle) {


//@line:352

		IAnimationPlayer* player = (IAnimationPlayer*)animationPlayerHandle;
		IObject* object= (IObject*)objectHandle;
		IGLBase::get()->adaptTo(player, object);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IGLBase_adaptToPM(JNIEnv* env, jclass clazz, jlong animationPlayerHandle, jlong pmHandle) {


//@line:358

		IAnimationPlayer* player = (IAnimationPlayer*)animationPlayerHandle;
		IPolygonMap* pm = (IPolygonMap*)pmHandle;
		IGLBase::get()->adaptTo(player, pm);
	

}

static inline jint wrapped_Java_com_btdstudio_glbase_IGLBase_loadTextureNTV
(JNIEnv* env, jclass clazz, jbyteArray obj_data, jint length, jboolean repeat, jboolean mipmap, jboolean gpuOnly, char* data) {

//@line:364

		return IGLBase::get()->loadTexture((byte*)data, length, repeat, mipmap, gpuOnly);
	
}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IGLBase_loadTextureNTV(JNIEnv* env, jclass clazz, jbyteArray obj_data, jint length, jboolean repeat, jboolean mipmap, jboolean gpuOnly) {
	char* data = (char*)env->GetPrimitiveArrayCritical(obj_data, 0);

	jint JNI_returnValue = wrapped_Java_com_btdstudio_glbase_IGLBase_loadTextureNTV(env, clazz, obj_data, length, repeat, mipmap, gpuOnly, data);

	env->ReleasePrimitiveArrayCritical(obj_data, data, 0);

	return JNI_returnValue;
}

static inline jint wrapped_Java_com_btdstudio_glbase_IGLBase_loadTextureCubeNTV
(JNIEnv* env, jclass clazz, jbyteArray obj_data1, jbyteArray obj_data2, jbyteArray obj_data3, jbyteArray obj_data4, jbyteArray obj_data5, jbyteArray obj_data6, jintArray obj_length, jboolean mipmap, char* data1, char* data2, char* data3, char* data4, char* data5, char* data6, int* length) {

//@line:369

    	byte* data[6] = { (byte*)data1, (byte*)data2, (byte*)data3, (byte*)data4, (byte*)data5, (byte*)data6 };
    	return IGLBase::get()->loadTextureCube(data, length, mipmap);
    
}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IGLBase_loadTextureCubeNTV(JNIEnv* env, jclass clazz, jbyteArray obj_data1, jbyteArray obj_data2, jbyteArray obj_data3, jbyteArray obj_data4, jbyteArray obj_data5, jbyteArray obj_data6, jintArray obj_length, jboolean mipmap) {
	char* data1 = (char*)env->GetPrimitiveArrayCritical(obj_data1, 0);
	char* data2 = (char*)env->GetPrimitiveArrayCritical(obj_data2, 0);
	char* data3 = (char*)env->GetPrimitiveArrayCritical(obj_data3, 0);
	char* data4 = (char*)env->GetPrimitiveArrayCritical(obj_data4, 0);
	char* data5 = (char*)env->GetPrimitiveArrayCritical(obj_data5, 0);
	char* data6 = (char*)env->GetPrimitiveArrayCritical(obj_data6, 0);
	int* length = (int*)env->GetPrimitiveArrayCritical(obj_length, 0);

	jint JNI_returnValue = wrapped_Java_com_btdstudio_glbase_IGLBase_loadTextureCubeNTV(env, clazz, obj_data1, obj_data2, obj_data3, obj_data4, obj_data5, obj_data6, obj_length, mipmap, data1, data2, data3, data4, data5, data6, length);

	env->ReleasePrimitiveArrayCritical(obj_data1, data1, 0);
	env->ReleasePrimitiveArrayCritical(obj_data2, data2, 0);
	env->ReleasePrimitiveArrayCritical(obj_data3, data3, 0);
	env->ReleasePrimitiveArrayCritical(obj_data4, data4, 0);
	env->ReleasePrimitiveArrayCritical(obj_data5, data5, 0);
	env->ReleasePrimitiveArrayCritical(obj_data6, data6, 0);
	env->ReleasePrimitiveArrayCritical(obj_length, length, 0);

	return JNI_returnValue;
}

static inline jlong wrapped_Java_com_btdstudio_glbase_IGLBase_acquireMyUniformNTV___3FI
(JNIEnv* env, jclass clazz, jfloatArray obj_vector, jint numComponents, float* vector) {

//@line:374

    	return (long long)IGLBase::get()->acquireMyUniform(vector, numComponents);
    
}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IGLBase_acquireMyUniformNTV___3FI(JNIEnv* env, jclass clazz, jfloatArray obj_vector, jint numComponents) {
	float* vector = (float*)env->GetPrimitiveArrayCritical(obj_vector, 0);

	jlong JNI_returnValue = wrapped_Java_com_btdstudio_glbase_IGLBase_acquireMyUniformNTV___3FI(env, clazz, obj_vector, numComponents, vector);

	env->ReleasePrimitiveArrayCritical(obj_vector, vector, 0);

	return JNI_returnValue;
}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IGLBase_acquireMyUniformNTV__II(JNIEnv* env, jclass clazz, jint texture, jint glactive) {


//@line:378

		return (long long)IGLBase::get()->acquireMyUniform(texture, glactive);
     

}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IGLBase_acquireDrawCallNTV__J(JNIEnv* env, jclass clazz, jlong pmHandle) {


//@line:382

    	IPolygonMap* pm = (IPolygonMap*)pmHandle;
		return (long long)IGLBase::get()->acquireDrawCall(pm);
	 

}

static inline jlong wrapped_Java_com_btdstudio_glbase_IGLBase_acquireDrawCallNTV___3F
(JNIEnv* env, jclass clazz, jfloatArray obj_bbox, float* bbox) {

//@line:387

		return (long long)IGLBase::get()->acquireDrawCall(bbox);
	 
}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IGLBase_acquireDrawCallNTV___3F(JNIEnv* env, jclass clazz, jfloatArray obj_bbox) {
	float* bbox = (float*)env->GetPrimitiveArrayCritical(obj_bbox, 0);

	jlong JNI_returnValue = wrapped_Java_com_btdstudio_glbase_IGLBase_acquireDrawCallNTV___3F(env, clazz, obj_bbox, bbox);

	env->ReleasePrimitiveArrayCritical(obj_bbox, bbox, 0);

	return JNI_returnValue;
}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IGLBase_acquireDrawCallNTV__I(JNIEnv* env, jclass clazz, jint numParticles) {


//@line:391

		return (long long)IGLBase::get()->acquireDrawCall(numParticles);
	 

}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IGLBase_acquireDrawCallNTV__(JNIEnv* env, jclass clazz) {


//@line:395

    	return (long long)IGLBase::get()->acquireDrawCall();
    

}

static inline jlong wrapped_Java_com_btdstudio_glbase_IGLBase_acquireDrawCallNTV__I_3F
(JNIEnv* env, jclass clazz, jint mode, jfloatArray obj_color, float* color) {

//@line:399

		return (long long)IGLBase::get()->acquireDrawCall((RenderEnums::ClearMode)mode, color);
	
}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IGLBase_acquireDrawCallNTV__I_3F(JNIEnv* env, jclass clazz, jint mode, jfloatArray obj_color) {
	float* color = (float*)env->GetPrimitiveArrayCritical(obj_color, 0);

	jlong JNI_returnValue = wrapped_Java_com_btdstudio_glbase_IGLBase_acquireDrawCallNTV__I_3F(env, clazz, mode, obj_color, color);

	env->ReleasePrimitiveArrayCritical(obj_color, color, 0);

	return JNI_returnValue;
}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IGLBase_getTextureInfoWidth(JNIEnv* env, jclass clazz, jint texture) {


//@line:403

		return IGLBase::get()->getTextureInfo(texture).width;
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IGLBase_getTextureInfoHeight(JNIEnv* env, jclass clazz, jint texture) {


//@line:407

		return IGLBase::get()->getTextureInfo(texture).height;
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IGLBase_getTextureInfoFormat(JNIEnv* env, jclass clazz, jint texture) {


//@line:411

		return IGLBase::get()->getTextureInfo(texture).format;
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IGLBase_getTextureInfoPixelFormat(JNIEnv* env, jclass clazz, jint texture) {


//@line:415

		return IGLBase::get()->getTextureInfo(texture).pixelFormat;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IGLBase_flushNTV(JNIEnv* env, jclass clazz) {


//@line:419

		IGLBase::get()->flush();
	

}

