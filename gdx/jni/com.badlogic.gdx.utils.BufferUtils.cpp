#include <com.badlogic.gdx.utils.BufferUtils.h>

//@line:497
 
	#include <stdio.h>
	#include <stdlib.h>
	#include <string.h>
	JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_freeMemory(JNIEnv* env, jclass clazz, jobject obj_buffer) {
	char* buffer = (char*)(obj_buffer?env->GetDirectBufferAddress(obj_buffer):0);


//@line:559

		free(buffer);
	 

}

JNIEXPORT jobject JNICALL Java_com_badlogic_gdx_utils_BufferUtils_newDisposableByteBuffer(JNIEnv* env, jclass clazz, jint numBytes) {


//@line:563

		return env->NewDirectByteBuffer((char*)malloc(numBytes), numBytes);
	

}

static inline jlong wrapped_Java_com_badlogic_gdx_utils_BufferUtils_getBufferAddress
(JNIEnv* env, jclass clazz, jobject obj_buffer, unsigned char* buffer) {

//@line:567

	    return (jlong) buffer;
	
}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_utils_BufferUtils_getBufferAddress(JNIEnv* env, jclass clazz, jobject obj_buffer) {
	unsigned char* buffer = (unsigned char*)(obj_buffer?env->GetDirectBufferAddress(obj_buffer):0);

	jlong JNI_returnValue = wrapped_Java_com_badlogic_gdx_utils_BufferUtils_getBufferAddress(env, clazz, obj_buffer, buffer);


	return JNI_returnValue;
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_clear(JNIEnv* env, jclass clazz, jobject obj_buffer, jint numBytes) {
	char* buffer = (char*)(obj_buffer?env->GetDirectBufferAddress(obj_buffer):0);


//@line:572

		memset(buffer, 0, numBytes);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3FLjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jfloatArray obj_src, jobject obj_dst, jint numFloats, jint offset) {
	unsigned char* dst = (unsigned char*)(obj_dst?env->GetDirectBufferAddress(obj_dst):0);
	float* src = (float*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:576

		memcpy(dst, src + offset, numFloats << 2 );
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3BILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jbyteArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)(obj_dst?env->GetDirectBufferAddress(obj_dst):0);
	char* src = (char*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:580

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3CILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jcharArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)(obj_dst?env->GetDirectBufferAddress(obj_dst):0);
	unsigned short* src = (unsigned short*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:584

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3SILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jshortArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)(obj_dst?env->GetDirectBufferAddress(obj_dst):0);
	short* src = (short*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:588

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	 
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3IILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jintArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)(obj_dst?env->GetDirectBufferAddress(obj_dst):0);
	int* src = (int*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:592

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3JILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jlongArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)(obj_dst?env->GetDirectBufferAddress(obj_dst):0);
	long long* src = (long long*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:596

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3FILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jfloatArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)(obj_dst?env->GetDirectBufferAddress(obj_dst):0);
	float* src = (float*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:600

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3DILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jdoubleArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* dst = (unsigned char*)(obj_dst?env->GetDirectBufferAddress(obj_dst):0);
	double* src = (double*)env->GetPrimitiveArrayCritical(obj_src, 0);


//@line:604

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	
	env->ReleasePrimitiveArrayCritical(obj_src, src, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni__Ljava_nio_Buffer_2ILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jobject obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes) {
	unsigned char* src = (unsigned char*)(obj_src?env->GetDirectBufferAddress(obj_src):0);
	unsigned char* dst = (unsigned char*)(obj_dst?env->GetDirectBufferAddress(obj_dst):0);


//@line:608

		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	

}


//@line:612

	template<size_t n1, size_t n2> void transform(float * const &src, float * const &m, float * const &dst) {}
	
	template<> inline void transform<4, 4>(float * const &src, float * const &m, float * const &dst) {
		const float x = src[0], y = src[1], z = src[2], w = src[3];
		dst[0] = x * m[ 0] + y * m[ 4] + z * m[ 8] + w * m[12]; 
		dst[1] = x * m[ 1] + y * m[ 5] + z * m[ 9] + w * m[13];
		dst[2] = x * m[ 2] + y * m[ 6] + z * m[10] + w * m[14];
		dst[3] = x * m[ 3] + y * m[ 7] + z * m[11] + w * m[15]; 
	}
	
	template<> inline void transform<3, 4>(float * const &src, float * const &m, float * const &dst) {
		const float x = src[0], y = src[1], z = src[2];
		dst[0] = x * m[ 0] + y * m[ 4] + z * m[ 8] + m[12]; 
		dst[1] = x * m[ 1] + y * m[ 5] + z * m[ 9] + m[13];
		dst[2] = x * m[ 2] + y * m[ 6] + z * m[10] + m[14]; 
	}
	
	template<> inline void transform<2, 4>(float * const &src, float * const &m, float * const &dst) {
		const float x = src[0], y = src[1];
		dst[0] = x * m[ 0] + y * m[ 4] + m[12]; 
		dst[1] = x * m[ 1] + y * m[ 5] + m[13]; 
	}
	
	template<> inline void transform<3, 3>(float * const &src, float * const &m, float * const &dst) {
		const float x = src[0], y = src[1], z = src[2];
		dst[0] = x * m[0] + y * m[3] + z * m[6]; 
		dst[1] = x * m[1] + y * m[4] + z * m[7];
		dst[2] = x * m[2] + y * m[5] + z * m[8]; 
	}
	
	template<> inline void transform<2, 3>(float * const &src, float * const &m, float * const &dst) {
		const float x = src[0], y = src[1];
		dst[0] = x * m[0] + y * m[3] + m[6]; 
		dst[1] = x * m[1] + y * m[4] + m[7]; 
	}
	
	template<size_t n1, size_t n2> void transform(float * const &v, int const &stride, int const &count, float * const &m, int offset) {
		for (int i = 0; i < count; i++) {
			transform<n1, n2>(&v[offset], m, &v[offset]);
			offset += stride;
		}
	}
	
	template<size_t n1, size_t n2> void transform(float * const &v, int const &stride, unsigned short * const &indices, int const &count, float * const &m, int offset) {
		for (int i = 0; i < count; i++) {
			transform<n1, n2>(&v[offset], m, &v[offset]);
			offset += stride;
		}
	}
	
	inline bool compare(float * const &lhs, float * const & rhs, const unsigned int &size, const float &epsilon) {
   	for (unsigned int i = 0; i < size; i++)
   		if ((*(unsigned int*)&lhs[i] != *(unsigned int*)&rhs[i]) && ((lhs[i] > rhs[i] ? lhs[i] - rhs[i] : rhs[i] - lhs[i]) > epsilon))
         	return false;
		return true;
	}
	
	long find(float * const &vertex, const unsigned int &size, float * const &vertices, const unsigned int &count, const float &epsilon) {
		for (unsigned int i = 0; i < count; i++)
			if (compare(&vertices[i*size], vertex, size, epsilon))
				return (long)i;
		return -1;
	}

	inline bool compare(float * const &lhs, float * const & rhs, const unsigned int &size) {
   	for (unsigned int i = 0; i < size; i++)
      	if ((*(unsigned int*)&lhs[i] != *(unsigned int*)&rhs[i]) && lhs[i] != rhs[i])
         	return false;
		return true;
	}
	
	long find(float * const &vertex, const unsigned int &size, float * const &vertices, const unsigned int &count) {
		for (unsigned int i = 0; i < count; i++)
			if (compare(&vertices[i*size], vertex, size))
				return (long)i;
		return -1;
	}

	inline unsigned int calcHash(float * const &vertex, const unsigned int &size) {
		unsigned int result = 0;
		for (unsigned int i = 0; i < size; ++i)
			result += ((*((unsigned int *)&vertex[i])) & 0xffffff80) >> (i & 0x7);
		return result & 0x7fffffff;
	}
	
	long find(float * const &vertex, const unsigned int &size, float * const &vertices, unsigned int * const &hashes, const unsigned int &count) {
		const unsigned int hash = calcHash(vertex, size);
		for (unsigned int i = 0; i < count; i++)
			if (hashes[i] == hash && compare(&vertices[i*size], vertex, size))
				return (long)i;
		return -1;
	}
	JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_transformV4M4Jni__Ljava_nio_Buffer_2II_3FI(JNIEnv* env, jclass clazz, jobject obj_data, jint strideInBytes, jint count, jfloatArray obj_matrix, jint offsetInBytes) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);
	float* matrix = (float*)env->GetPrimitiveArrayCritical(obj_matrix, 0);


//@line:707

		transform<4, 4>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);  
	
	env->ReleasePrimitiveArrayCritical(obj_matrix, matrix, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_transformV4M4Jni___3FII_3FI(JNIEnv* env, jclass clazz, jfloatArray obj_data, jint strideInBytes, jint count, jfloatArray obj_matrix, jint offsetInBytes) {
	float* data = (float*)env->GetPrimitiveArrayCritical(obj_data, 0);
	float* matrix = (float*)env->GetPrimitiveArrayCritical(obj_matrix, 0);


//@line:711

		transform<4, 4>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);  
	
	env->ReleasePrimitiveArrayCritical(obj_data, data, 0);
	env->ReleasePrimitiveArrayCritical(obj_matrix, matrix, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_transformV3M4Jni__Ljava_nio_Buffer_2II_3FI(JNIEnv* env, jclass clazz, jobject obj_data, jint strideInBytes, jint count, jfloatArray obj_matrix, jint offsetInBytes) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);
	float* matrix = (float*)env->GetPrimitiveArrayCritical(obj_matrix, 0);


//@line:715

		transform<3, 4>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	
	env->ReleasePrimitiveArrayCritical(obj_matrix, matrix, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_transformV3M4Jni___3FII_3FI(JNIEnv* env, jclass clazz, jfloatArray obj_data, jint strideInBytes, jint count, jfloatArray obj_matrix, jint offsetInBytes) {
	float* data = (float*)env->GetPrimitiveArrayCritical(obj_data, 0);
	float* matrix = (float*)env->GetPrimitiveArrayCritical(obj_matrix, 0);


//@line:719

		transform<3, 4>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	
	env->ReleasePrimitiveArrayCritical(obj_data, data, 0);
	env->ReleasePrimitiveArrayCritical(obj_matrix, matrix, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_transformV2M4Jni__Ljava_nio_Buffer_2II_3FI(JNIEnv* env, jclass clazz, jobject obj_data, jint strideInBytes, jint count, jfloatArray obj_matrix, jint offsetInBytes) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);
	float* matrix = (float*)env->GetPrimitiveArrayCritical(obj_matrix, 0);


//@line:723

		transform<2, 4>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	
	env->ReleasePrimitiveArrayCritical(obj_matrix, matrix, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_transformV2M4Jni___3FII_3FI(JNIEnv* env, jclass clazz, jfloatArray obj_data, jint strideInBytes, jint count, jfloatArray obj_matrix, jint offsetInBytes) {
	float* data = (float*)env->GetPrimitiveArrayCritical(obj_data, 0);
	float* matrix = (float*)env->GetPrimitiveArrayCritical(obj_matrix, 0);


//@line:727

		transform<2, 4>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	
	env->ReleasePrimitiveArrayCritical(obj_data, data, 0);
	env->ReleasePrimitiveArrayCritical(obj_matrix, matrix, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_transformV3M3Jni__Ljava_nio_Buffer_2II_3FI(JNIEnv* env, jclass clazz, jobject obj_data, jint strideInBytes, jint count, jfloatArray obj_matrix, jint offsetInBytes) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);
	float* matrix = (float*)env->GetPrimitiveArrayCritical(obj_matrix, 0);


//@line:731

		transform<3, 3>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	
	env->ReleasePrimitiveArrayCritical(obj_matrix, matrix, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_transformV3M3Jni___3FII_3FI(JNIEnv* env, jclass clazz, jfloatArray obj_data, jint strideInBytes, jint count, jfloatArray obj_matrix, jint offsetInBytes) {
	float* data = (float*)env->GetPrimitiveArrayCritical(obj_data, 0);
	float* matrix = (float*)env->GetPrimitiveArrayCritical(obj_matrix, 0);


//@line:735

		transform<3, 3>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	
	env->ReleasePrimitiveArrayCritical(obj_data, data, 0);
	env->ReleasePrimitiveArrayCritical(obj_matrix, matrix, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_transformV2M3Jni__Ljava_nio_Buffer_2II_3FI(JNIEnv* env, jclass clazz, jobject obj_data, jint strideInBytes, jint count, jfloatArray obj_matrix, jint offsetInBytes) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);
	float* matrix = (float*)env->GetPrimitiveArrayCritical(obj_matrix, 0);


//@line:739

		transform<2, 3>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	
	env->ReleasePrimitiveArrayCritical(obj_matrix, matrix, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_transformV2M3Jni___3FII_3FI(JNIEnv* env, jclass clazz, jfloatArray obj_data, jint strideInBytes, jint count, jfloatArray obj_matrix, jint offsetInBytes) {
	float* data = (float*)env->GetPrimitiveArrayCritical(obj_data, 0);
	float* matrix = (float*)env->GetPrimitiveArrayCritical(obj_matrix, 0);


//@line:743

		transform<2, 3>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	
	env->ReleasePrimitiveArrayCritical(obj_data, data, 0);
	env->ReleasePrimitiveArrayCritical(obj_matrix, matrix, 0);

}

static inline jlong wrapped_Java_com_badlogic_gdx_utils_BufferUtils_find__Ljava_nio_Buffer_2IILjava_nio_Buffer_2II
(JNIEnv* env, jclass clazz, jobject obj_vertex, jint vertexOffsetInBytes, jint strideInBytes, jobject obj_vertices, jint verticesOffsetInBytes, jint numVertices, unsigned char* vertex, unsigned char* vertices) {

//@line:747

		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices);
	
}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_utils_BufferUtils_find__Ljava_nio_Buffer_2IILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jobject obj_vertex, jint vertexOffsetInBytes, jint strideInBytes, jobject obj_vertices, jint verticesOffsetInBytes, jint numVertices) {
	unsigned char* vertex = (unsigned char*)(obj_vertex?env->GetDirectBufferAddress(obj_vertex):0);
	unsigned char* vertices = (unsigned char*)(obj_vertices?env->GetDirectBufferAddress(obj_vertices):0);

	jlong JNI_returnValue = wrapped_Java_com_badlogic_gdx_utils_BufferUtils_find__Ljava_nio_Buffer_2IILjava_nio_Buffer_2II(env, clazz, obj_vertex, vertexOffsetInBytes, strideInBytes, obj_vertices, verticesOffsetInBytes, numVertices, vertex, vertices);


	return JNI_returnValue;
}

static inline jlong wrapped_Java_com_badlogic_gdx_utils_BufferUtils_find___3FIILjava_nio_Buffer_2II
(JNIEnv* env, jclass clazz, jfloatArray obj_vertex, jint vertexOffsetInBytes, jint strideInBytes, jobject obj_vertices, jint verticesOffsetInBytes, jint numVertices, unsigned char* vertices, float* vertex) {

//@line:751

		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices);
	
}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_utils_BufferUtils_find___3FIILjava_nio_Buffer_2II(JNIEnv* env, jclass clazz, jfloatArray obj_vertex, jint vertexOffsetInBytes, jint strideInBytes, jobject obj_vertices, jint verticesOffsetInBytes, jint numVertices) {
	unsigned char* vertices = (unsigned char*)(obj_vertices?env->GetDirectBufferAddress(obj_vertices):0);
	float* vertex = (float*)env->GetPrimitiveArrayCritical(obj_vertex, 0);

	jlong JNI_returnValue = wrapped_Java_com_badlogic_gdx_utils_BufferUtils_find___3FIILjava_nio_Buffer_2II(env, clazz, obj_vertex, vertexOffsetInBytes, strideInBytes, obj_vertices, verticesOffsetInBytes, numVertices, vertices, vertex);

	env->ReleasePrimitiveArrayCritical(obj_vertex, vertex, 0);

	return JNI_returnValue;
}

static inline jlong wrapped_Java_com_badlogic_gdx_utils_BufferUtils_find__Ljava_nio_Buffer_2II_3FII
(JNIEnv* env, jclass clazz, jobject obj_vertex, jint vertexOffsetInBytes, jint strideInBytes, jfloatArray obj_vertices, jint verticesOffsetInBytes, jint numVertices, unsigned char* vertex, float* vertices) {

//@line:755

		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices);
	
}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_utils_BufferUtils_find__Ljava_nio_Buffer_2II_3FII(JNIEnv* env, jclass clazz, jobject obj_vertex, jint vertexOffsetInBytes, jint strideInBytes, jfloatArray obj_vertices, jint verticesOffsetInBytes, jint numVertices) {
	unsigned char* vertex = (unsigned char*)(obj_vertex?env->GetDirectBufferAddress(obj_vertex):0);
	float* vertices = (float*)env->GetPrimitiveArrayCritical(obj_vertices, 0);

	jlong JNI_returnValue = wrapped_Java_com_badlogic_gdx_utils_BufferUtils_find__Ljava_nio_Buffer_2II_3FII(env, clazz, obj_vertex, vertexOffsetInBytes, strideInBytes, obj_vertices, verticesOffsetInBytes, numVertices, vertex, vertices);

	env->ReleasePrimitiveArrayCritical(obj_vertices, vertices, 0);

	return JNI_returnValue;
}

static inline jlong wrapped_Java_com_badlogic_gdx_utils_BufferUtils_find___3FII_3FII
(JNIEnv* env, jclass clazz, jfloatArray obj_vertex, jint vertexOffsetInBytes, jint strideInBytes, jfloatArray obj_vertices, jint verticesOffsetInBytes, jint numVertices, float* vertex, float* vertices) {

//@line:759

		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices);
	
}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_utils_BufferUtils_find___3FII_3FII(JNIEnv* env, jclass clazz, jfloatArray obj_vertex, jint vertexOffsetInBytes, jint strideInBytes, jfloatArray obj_vertices, jint verticesOffsetInBytes, jint numVertices) {
	float* vertex = (float*)env->GetPrimitiveArrayCritical(obj_vertex, 0);
	float* vertices = (float*)env->GetPrimitiveArrayCritical(obj_vertices, 0);

	jlong JNI_returnValue = wrapped_Java_com_badlogic_gdx_utils_BufferUtils_find___3FII_3FII(env, clazz, obj_vertex, vertexOffsetInBytes, strideInBytes, obj_vertices, verticesOffsetInBytes, numVertices, vertex, vertices);

	env->ReleasePrimitiveArrayCritical(obj_vertex, vertex, 0);
	env->ReleasePrimitiveArrayCritical(obj_vertices, vertices, 0);

	return JNI_returnValue;
}

static inline jlong wrapped_Java_com_badlogic_gdx_utils_BufferUtils_find__Ljava_nio_Buffer_2IILjava_nio_Buffer_2IIF
(JNIEnv* env, jclass clazz, jobject obj_vertex, jint vertexOffsetInBytes, jint strideInBytes, jobject obj_vertices, jint verticesOffsetInBytes, jint numVertices, jfloat epsilon, unsigned char* vertex, unsigned char* vertices) {

//@line:763

		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices, epsilon);
	
}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_utils_BufferUtils_find__Ljava_nio_Buffer_2IILjava_nio_Buffer_2IIF(JNIEnv* env, jclass clazz, jobject obj_vertex, jint vertexOffsetInBytes, jint strideInBytes, jobject obj_vertices, jint verticesOffsetInBytes, jint numVertices, jfloat epsilon) {
	unsigned char* vertex = (unsigned char*)(obj_vertex?env->GetDirectBufferAddress(obj_vertex):0);
	unsigned char* vertices = (unsigned char*)(obj_vertices?env->GetDirectBufferAddress(obj_vertices):0);

	jlong JNI_returnValue = wrapped_Java_com_badlogic_gdx_utils_BufferUtils_find__Ljava_nio_Buffer_2IILjava_nio_Buffer_2IIF(env, clazz, obj_vertex, vertexOffsetInBytes, strideInBytes, obj_vertices, verticesOffsetInBytes, numVertices, epsilon, vertex, vertices);


	return JNI_returnValue;
}

static inline jlong wrapped_Java_com_badlogic_gdx_utils_BufferUtils_find___3FIILjava_nio_Buffer_2IIF
(JNIEnv* env, jclass clazz, jfloatArray obj_vertex, jint vertexOffsetInBytes, jint strideInBytes, jobject obj_vertices, jint verticesOffsetInBytes, jint numVertices, jfloat epsilon, unsigned char* vertices, float* vertex) {

//@line:767

		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices, epsilon);
	
}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_utils_BufferUtils_find___3FIILjava_nio_Buffer_2IIF(JNIEnv* env, jclass clazz, jfloatArray obj_vertex, jint vertexOffsetInBytes, jint strideInBytes, jobject obj_vertices, jint verticesOffsetInBytes, jint numVertices, jfloat epsilon) {
	unsigned char* vertices = (unsigned char*)(obj_vertices?env->GetDirectBufferAddress(obj_vertices):0);
	float* vertex = (float*)env->GetPrimitiveArrayCritical(obj_vertex, 0);

	jlong JNI_returnValue = wrapped_Java_com_badlogic_gdx_utils_BufferUtils_find___3FIILjava_nio_Buffer_2IIF(env, clazz, obj_vertex, vertexOffsetInBytes, strideInBytes, obj_vertices, verticesOffsetInBytes, numVertices, epsilon, vertices, vertex);

	env->ReleasePrimitiveArrayCritical(obj_vertex, vertex, 0);

	return JNI_returnValue;
}

static inline jlong wrapped_Java_com_badlogic_gdx_utils_BufferUtils_find__Ljava_nio_Buffer_2II_3FIIF
(JNIEnv* env, jclass clazz, jobject obj_vertex, jint vertexOffsetInBytes, jint strideInBytes, jfloatArray obj_vertices, jint verticesOffsetInBytes, jint numVertices, jfloat epsilon, unsigned char* vertex, float* vertices) {

//@line:771

		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices, epsilon);
	
}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_utils_BufferUtils_find__Ljava_nio_Buffer_2II_3FIIF(JNIEnv* env, jclass clazz, jobject obj_vertex, jint vertexOffsetInBytes, jint strideInBytes, jfloatArray obj_vertices, jint verticesOffsetInBytes, jint numVertices, jfloat epsilon) {
	unsigned char* vertex = (unsigned char*)(obj_vertex?env->GetDirectBufferAddress(obj_vertex):0);
	float* vertices = (float*)env->GetPrimitiveArrayCritical(obj_vertices, 0);

	jlong JNI_returnValue = wrapped_Java_com_badlogic_gdx_utils_BufferUtils_find__Ljava_nio_Buffer_2II_3FIIF(env, clazz, obj_vertex, vertexOffsetInBytes, strideInBytes, obj_vertices, verticesOffsetInBytes, numVertices, epsilon, vertex, vertices);

	env->ReleasePrimitiveArrayCritical(obj_vertices, vertices, 0);

	return JNI_returnValue;
}

static inline jlong wrapped_Java_com_badlogic_gdx_utils_BufferUtils_find___3FII_3FIIF
(JNIEnv* env, jclass clazz, jfloatArray obj_vertex, jint vertexOffsetInBytes, jint strideInBytes, jfloatArray obj_vertices, jint verticesOffsetInBytes, jint numVertices, jfloat epsilon, float* vertex, float* vertices) {

//@line:775

		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices, epsilon);
	
}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_utils_BufferUtils_find___3FII_3FIIF(JNIEnv* env, jclass clazz, jfloatArray obj_vertex, jint vertexOffsetInBytes, jint strideInBytes, jfloatArray obj_vertices, jint verticesOffsetInBytes, jint numVertices, jfloat epsilon) {
	float* vertex = (float*)env->GetPrimitiveArrayCritical(obj_vertex, 0);
	float* vertices = (float*)env->GetPrimitiveArrayCritical(obj_vertices, 0);

	jlong JNI_returnValue = wrapped_Java_com_badlogic_gdx_utils_BufferUtils_find___3FII_3FIIF(env, clazz, obj_vertex, vertexOffsetInBytes, strideInBytes, obj_vertices, verticesOffsetInBytes, numVertices, epsilon, vertex, vertices);

	env->ReleasePrimitiveArrayCritical(obj_vertex, vertex, 0);
	env->ReleasePrimitiveArrayCritical(obj_vertices, vertices, 0);

	return JNI_returnValue;
}

