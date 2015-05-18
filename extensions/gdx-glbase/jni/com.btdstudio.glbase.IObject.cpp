#include <com.btdstudio.glbase.IObject.h>

//@line:8

	 	#include "macros.h"
	    #include "Iobject.h"
	    #include "IrenderQueue.h"
	    #include "drawCall.h"
	    #include <string.h>
	    
	    // Temp
	    ArrayList tempList2(128);
	    int tempDcNum2;
	    DrawCall* tempDcList2;
    JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IObject_addMatrixIndicesName(JNIEnv* env, jclass clazz, jstring obj_name) {
	char* name = (char*)env->GetStringUTFChars(obj_name, 0);


//@line:165

		tempList2.add(strdup2(name));
	
	env->ReleaseStringUTFChars(obj_name, name);

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IObject_setMatrixIndicesNames(JNIEnv* env, jclass clazz, jlong handle) {


//@line:169

		((IObject*)handle)->setMatrixIndicesNames(&tempList2);
		
		// Clear array
		for( int i=0; i<tempList2.getSize(); i++ ){
			delete (char*)tempList2.get(i);
		}
		
		tempList2.clear();
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IObject_getIdLength(JNIEnv* env, jclass clazz, jlong handle) {


//@line:180

		return strlen(((IObject*)handle)->getId());
	

}

JNIEXPORT jchar JNICALL Java_com_btdstudio_glbase_IObject_getId(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:184

		return ((IObject*)handle)->getId()[pos];
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IObject_getNameLength(JNIEnv* env, jclass clazz, jlong handle) {


//@line:188

		return strlen(((IObject*)handle)->getName());
	

}

JNIEXPORT jchar JNICALL Java_com_btdstudio_glbase_IObject_getName(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:192

		return ((IObject*)handle)->getName()[pos];
	 

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IObject_getVersionLength(JNIEnv* env, jclass clazz, jlong handle) {


//@line:196

		return strlen(((IObject*)handle)->getVersion());
	

}

JNIEXPORT jchar JNICALL Java_com_btdstudio_glbase_IObject_getVersion(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:200

		return ((IObject*)handle)->getVersion()[pos];
	 

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IObject_getMetainfoLength(JNIEnv* env, jclass clazz, jlong handle) {


//@line:204

		return strlen(((IObject*)handle)->getMetainfo());
	

}

JNIEXPORT jchar JNICALL Java_com_btdstudio_glbase_IObject_getMetainfo(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:208

		return ((IObject*)handle)->getMetainfo()[pos];
	 

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IObject_getFilenameLength(JNIEnv* env, jclass clazz, jlong handle) {


//@line:212

		return strlen(((IObject*)handle)->getFilename());
	

}

JNIEXPORT jchar JNICALL Java_com_btdstudio_glbase_IObject_getFilename(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:216

		return ((IObject*)handle)->getFilename()[pos];
	 

}

JNIEXPORT jfloat JNICALL Java_com_btdstudio_glbase_IObject_getBoundingBox(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:220

		return ((IObject*)handle)->getBoundingBox()[pos];
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IObject_getLeftBitShift(JNIEnv* env, jclass clazz, jlong handle) {


//@line:224

		return ((IObject*)handle)->getLeftBitShift();
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IObject_getShadeModelLength(JNIEnv* env, jclass clazz, jlong handle) {


//@line:228

		return strlen(((IObject*)handle)->getShadeModel());
	

}

JNIEXPORT jchar JNICALL Java_com_btdstudio_glbase_IObject_getShadeModel(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:232

		return ((IObject*)handle)->getShadeModel()[pos];
	 

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IObject_getShadeValueLength(JNIEnv* env, jclass clazz, jlong handle) {


//@line:236

		return strlen(((IObject*)handle)->getShadeValue());
	

}

JNIEXPORT jchar JNICALL Java_com_btdstudio_glbase_IObject_getShadeValue(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:240

		return ((IObject*)handle)->getShadeValue()[pos];
	 

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IObject_getLayersNum(JNIEnv* env, jclass clazz, jlong handle) {


//@line:244

		return ((IObject*)handle)->getLayers()->getSize();
	

}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IObject_getLayer(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:248

		return (long long)((IObject*)handle)->getLayers()->get(pos);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IObject_setTexture__JIII(JNIEnv* env, jclass clazz, jlong handle, jint layerIdx, jint polygonMapIdx, jint texture) {


//@line:252

		((IObject*)handle)->setTexture(layerIdx, polygonMapIdx, texture);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IObject_setTexture__JII(JNIEnv* env, jclass clazz, jlong handle, jint polygonMapIdx, jint texture) {


//@line:256

		((IObject*)handle)->setTexture(polygonMapIdx, texture);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IObject_addDrawCalls(JNIEnv* env, jclass clazz, jlong handle, jlong renderQueueHandle, jlong renderParamsHandle) {


//@line:260

		IRenderQueue* queue = (IRenderQueue*)renderQueueHandle;
		RenderParameters* params = (RenderParameters*)renderParamsHandle;
		
		((IObject*)handle)->addDrawCalls(queue, params);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IObject_prepareDrawCalls(JNIEnv* env, jclass clazz, jlong handle, jlong paramsHandle) {


//@line:267

		RenderParameters* params = (RenderParameters*)paramsHandle;		
		tempDcNum2 = ((IObject*)handle)->prepareDrawCalls(tempDcList2, params);
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_IObject_getDrawCallsNum(JNIEnv* env, jclass clazz) {


//@line:272

		return tempDcNum2;
	

}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_IObject_getDrawCall(JNIEnv* env, jclass clazz, jint pos) {


//@line:276

		return (long long)&tempDcList2[pos];
	 

}

JNIEXPORT jboolean JNICALL Java_com_btdstudio_glbase_IObject_initRenderEnv(JNIEnv* env, jclass clazz, jlong handle) {


//@line:280

		return ((IObject*)handle)->initRenderEnv();
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IObject_releaseOriginalData(JNIEnv* env, jclass clazz, jlong handle) {


//@line:284

		((IObject*)handle)->releaseOriginalData();
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IObject_dispose(JNIEnv* env, jclass clazz, jlong handle) {


//@line:288

		delete (IObject*)handle;
	

}

