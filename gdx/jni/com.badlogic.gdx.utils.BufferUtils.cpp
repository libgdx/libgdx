#include <com.badlogic.gdx.utils.BufferUtils.h>

//@line:270
 
	#include <stdio.h>
	#include <stdlib.h>
	#include <string.h>
	JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_freeMemory(JNIEnv* env, jclass clazz, jobject obj_buffer) {
	char* buffer = (char*)(obj_buffer?env->GetDirectBufferAddress(obj_buffer):0);


//@line:332

		free(buffer);
	 

}

JNIEXPORT jobject JNICALL Java_com_badlogic_gdx_utils_BufferUtils_newDisposableByteBuffer(JNIEnv* env, jclass clazz, jint numBytes) {


//@line:336

		char* ptr = (char*)malloc(numBytes);
		return env->NewDirectByteBuffer(ptr, numBytes);
	

}

static inline jlong wrapped_Java_com_badlogic_gdx_utils_BufferUtils_getBufferAddress
(JNIEnv* env, jclass clazz, jobject obj_buffer, unsigned char* buffer) {

//@line:341

	    return (jlong) buffer;
	
}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_utils_BufferUtils_getBufferAddress(JNIEnv* env, jclass clazz, jobject obj_buffer) {
	unsigned char* buffer = (unsigned char*)(obj_buffer?env->GetDirectBufferAddress(obj_buffer):0);

	jlong JNI_returnValue = wrapped_Java_com_badlogic_gdx_utils_BufferUtils_getBufferAddress(env, clazz, obj_buffer, buffer);


	return JNI_returnValue;
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_clear(JNIEnv* env, jclass clazz, jobject obj_buffer, jint numBytes) {
	char* buffer = (char*)(obj_buffer?env->GetDirectBufferAddress(obj_buffer):0);


//@line:346

		memset(buffer, 0, numBytes);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3FLjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jfloatArray obj_src, jobject obj_dst, jint numFloats, jint offset) {
	unsigned char* dst = (unsigned char*)(obj_dst?env->GetDirectBufferAddress(obj_dst):0);
	float* src = (float*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:350

		memcpy(dst, src + offset, numFloats << 2 );
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3BILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jbyteArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)(obj_dst?env->GetDirectBufferAddress(obj_dst):0);
	char* src = (char*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:354

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3CILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jcharArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)(obj_dst?env->GetDirectBufferAddress(obj_dst):0);
	unsigned short* src = (unsigned short*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:358

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3SILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jshortArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)(obj_dst?env->GetDirectBufferAddress(obj_dst):0);
	short* src = (short*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:362

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	 
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3IILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jintArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)(obj_dst?env->GetDirectBufferAddress(obj_dst):0);
	int* src = (int*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:366

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3JILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jlongArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)(obj_dst?env->GetDirectBufferAddress(obj_dst):0);
	long long* src = (long long*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:370

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3FILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jfloatArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)(obj_dst?env->GetDirectBufferAddress(obj_dst):0);
	float* src = (float*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:374

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3DILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jdoubleArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)(obj_dst?env->GetDirectBufferAddress(obj_dst):0);
	double* src = (double*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:378

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni__Ljava_nio_Buffer_2ILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jobject obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* src = (unsigned char*)(obj_src?env->GetDirectBufferAddress(obj_src):0);
	unsigned char* dst = (unsigned char*)(obj_dst?env->GetDirectBufferAddress(obj_dst):0);


//@line:382

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_transformV4M4Jni(JNIEnv* env, jclass clazz, jobject obj_data, jint offsetInBytes, jint strideInBytes, jint count, jfloatArray obj_matrix) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);
	float* matrix = (float*)env->GetPrimitiveArrayCritical(obj_matrix, 0);


//@line:386

		const int stride = strideInBytes / 4;
		const int offset = offsetInBytes / 4;
		float *d = (float*)data;
		float *m = (float*)matrix;
		for (int i = 0; i < count; ++i) {
			const int idx = offset + i * stride;
			const float x = d[idx    ];
			const float y = d[idx + 1];
			const float z = d[idx + 2];
			const float w = d[idx + 3];
			d[idx  ] = x * m[ 0] + y * m[ 4] + z * m[ 8] + w * m[12]; 
			d[idx+1] = x * m[ 1] + y * m[ 5] + z * m[ 9] + w * m[13];
			d[idx+2] = x * m[ 2] + y * m[ 6] + z * m[10] + w * m[14];
			d[idx+3] = x * m[ 3] + y * m[ 7] + z * m[11] + w * m[15];
		}
	
	env->ReleasePrimitiveArrayCritical(obj_matrix, matrix, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_transformV3M4Jni(JNIEnv* env, jclass clazz, jobject obj_data, jint offsetInBytes, jint strideInBytes, jint count, jfloatArray obj_matrix) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);
	float* matrix = (float*)env->GetPrimitiveArrayCritical(obj_matrix, 0);


//@line:404

		const int stride = strideInBytes / 4;
		const int offset = offsetInBytes / 4;
		float *d = (float*)data;
		float *m = (float*)matrix;
		for (int i = 0; i < count; ++i) {
			const int idx = offset + i * stride;
			const float x = d[idx    ];
			const float y = d[idx + 1];
			const float z = d[idx + 2];
			d[idx  ] = x * m[ 0] + y * m[ 4] + z * m[ 8] + m[12]; 
			d[idx+1] = x * m[ 1] + y * m[ 5] + z * m[ 9] + m[13];
			d[idx+2] = x * m[ 2] + y * m[ 6] + z * m[10] + m[14];
		}
	 
	env->ReleasePrimitiveArrayCritical(obj_matrix, matrix, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_transformV2M4Jni(JNIEnv* env, jclass clazz, jobject obj_data, jint offsetInBytes, jint strideInBytes, jint count, jfloatArray obj_matrix) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);
	float* matrix = (float*)env->GetPrimitiveArrayCritical(obj_matrix, 0);


//@line:420

		const int stride = strideInBytes / 4;
		const int offset = offsetInBytes / 4;
		float *d = (float*)data;
		float *m = (float*)matrix;
		for (int i = 0; i < count; ++i) {
			const int idx = offset + i * stride;
			const float x = d[idx    ];
			const float y = d[idx + 1];
			d[idx  ] = x * m[ 0] + y * m[ 4] + m[12]; 
			d[idx+1] = x * m[ 1] + y * m[ 5] + m[13];
		}
	 
	env->ReleasePrimitiveArrayCritical(obj_matrix, matrix, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_transformV3M3Jni(JNIEnv* env, jclass clazz, jobject obj_data, jint offsetInBytes, jint strideInBytes, jint count, jfloatArray obj_matrix) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);
	float* matrix = (float*)env->GetPrimitiveArrayCritical(obj_matrix, 0);


//@line:434

		const int stride = strideInBytes / 4;
		const int offset = offsetInBytes / 4;
		float *d = (float*)data;
		float *m = (float*)matrix;
		for (int i = 0; i < count; ++i) {
			const int idx = offset + i * stride;
			const float x = d[idx    ];
			const float y = d[idx + 1];
			const float z = d[idx + 2];
			d[idx  ] = x * m[0] + y * m[3] + z * m[6]; 
			d[idx+1] = x * m[1] + y * m[4] + z * m[7];
			d[idx+2] = x * m[2] + y * m[5] + z * m[8];
		}
	 
	env->ReleasePrimitiveArrayCritical(obj_matrix, matrix, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_transformV2M3Jni(JNIEnv* env, jclass clazz, jobject obj_data, jint offsetInBytes, jint strideInBytes, jint count, jfloatArray obj_matrix) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);
	float* matrix = (float*)env->GetPrimitiveArrayCritical(obj_matrix, 0);


//@line:450

		const int stride = strideInBytes / 4;
		const int offset = offsetInBytes / 4;
		float *d = (float*)data;
		float *m = (float*)matrix;
		for (int i = 0; i < count; ++i) {
			const int idx = offset + i * stride;
			const float x = d[idx    ];
			const float y = d[idx + 1];
			d[idx  ] = x * m[0] + y * m[3] + m[6]; 
			d[idx+1] = x * m[1] + y * m[4] + m[7];
		}
	 
	env->ReleasePrimitiveArrayCritical(obj_matrix, matrix, 0);

}

