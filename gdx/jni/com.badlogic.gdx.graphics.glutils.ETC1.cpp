#include <com.badlogic.gdx.graphics.glutils.ETC1.h>

//@line:196

	#include <etc1/etc1_utils.h>
	#include <stdlib.h>
	 JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_getCompressedDataSize(JNIEnv* env, jclass clazz, jint width, jint height) {


//@line:204

		return etc1_get_encoded_data_size(width, height);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_formatHeader(JNIEnv* env, jclass clazz, jobject obj_header, jint offset, jint width, jint height) {
	char* header = (char*)(obj_header?env->GetDirectBufferAddress(obj_header):0);


//@line:214

		etc1_pkm_format_header((etc1_byte*)header + offset, width, height);
	

}

static inline jint wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_getWidthPKM
(JNIEnv* env, jclass clazz, jobject obj_header, jint offset, char* header) {

//@line:221

		return etc1_pkm_get_width((etc1_byte*)header + offset);
	
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_getWidthPKM(JNIEnv* env, jclass clazz, jobject obj_header, jint offset) {
	char* header = (char*)(obj_header?env->GetDirectBufferAddress(obj_header):0);

	jint JNI_returnValue = wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_getWidthPKM(env, clazz, obj_header, offset, header);


	return JNI_returnValue;
}

static inline jint wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_getHeightPKM
(JNIEnv* env, jclass clazz, jobject obj_header, jint offset, char* header) {

//@line:228

		return etc1_pkm_get_height((etc1_byte*)header + offset);
	
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_getHeightPKM(JNIEnv* env, jclass clazz, jobject obj_header, jint offset) {
	char* header = (char*)(obj_header?env->GetDirectBufferAddress(obj_header):0);

	jint JNI_returnValue = wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_getHeightPKM(env, clazz, obj_header, offset, header);


	return JNI_returnValue;
}

static inline jboolean wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_isValidPKM
(JNIEnv* env, jclass clazz, jobject obj_header, jint offset, char* header) {

//@line:235

		return etc1_pkm_is_valid((etc1_byte*)header + offset) != 0?true:false;
	
}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_isValidPKM(JNIEnv* env, jclass clazz, jobject obj_header, jint offset) {
	char* header = (char*)(obj_header?env->GetDirectBufferAddress(obj_header):0);

	jboolean JNI_returnValue = wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_isValidPKM(env, clazz, obj_header, offset, header);


	return JNI_returnValue;
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_decodeImage(JNIEnv* env, jclass clazz, jobject obj_compressedData, jint offset, jobject obj_decodedData, jint offsetDec, jint width, jint height, jint pixelSize) {
	char* compressedData = (char*)(obj_compressedData?env->GetDirectBufferAddress(obj_compressedData):0);
	char* decodedData = (char*)(obj_decodedData?env->GetDirectBufferAddress(obj_decodedData):0);


//@line:249

		etc1_decode_image((etc1_byte*)compressedData + offset, (etc1_byte*)decodedData + offsetDec, width, height, pixelSize, width * pixelSize);
	

}

static inline jobject wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_encodeImage
(JNIEnv* env, jclass clazz, jobject obj_imageData, jint offset, jint width, jint height, jint pixelSize, char* imageData) {

//@line:260

		int compressedSize = etc1_get_encoded_data_size(width, height);
		etc1_byte* compressedData = (etc1_byte*)malloc(compressedSize);
		etc1_encode_image((etc1_byte*)imageData + offset, width, height, pixelSize, width * pixelSize, compressedData);
		return env->NewDirectByteBuffer(compressedData, compressedSize);
	
}

JNIEXPORT jobject JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_encodeImage(JNIEnv* env, jclass clazz, jobject obj_imageData, jint offset, jint width, jint height, jint pixelSize) {
	char* imageData = (char*)(obj_imageData?env->GetDirectBufferAddress(obj_imageData):0);

	jobject JNI_returnValue = wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_encodeImage(env, clazz, obj_imageData, offset, width, height, pixelSize, imageData);


	return JNI_returnValue;
}

static inline jobject wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_encodeImagePKM
(JNIEnv* env, jclass clazz, jobject obj_imageData, jint offset, jint width, jint height, jint pixelSize, char* imageData) {

//@line:274

		int compressedSize = etc1_get_encoded_data_size(width, height);
		etc1_byte* compressed = (etc1_byte*)malloc(compressedSize + ETC_PKM_HEADER_SIZE);
		etc1_pkm_format_header(compressed, width, height);
		etc1_encode_image((etc1_byte*)imageData + offset, width, height, pixelSize, width * pixelSize, compressed + ETC_PKM_HEADER_SIZE);
		return env->NewDirectByteBuffer(compressed, compressedSize + ETC_PKM_HEADER_SIZE);
	
}

JNIEXPORT jobject JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_encodeImagePKM(JNIEnv* env, jclass clazz, jobject obj_imageData, jint offset, jint width, jint height, jint pixelSize) {
	char* imageData = (char*)(obj_imageData?env->GetDirectBufferAddress(obj_imageData):0);

	jobject JNI_returnValue = wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_encodeImagePKM(env, clazz, obj_imageData, offset, width, height, pixelSize, imageData);


	return JNI_returnValue;
}

