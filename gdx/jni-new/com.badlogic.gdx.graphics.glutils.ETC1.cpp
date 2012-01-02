#include <com.badlogic.gdx.graphics.glutils.ETC1.h>

//@line:188

	#include <etc1/etc1_utils.h>
	#include <stdlib.h>
	 JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_getCompressedDataSize(JNIEnv* env, jclass clazz, jint width, jint height) {


//@line:196

		return etc1_get_encoded_data_size(width, height);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_formatHeader(JNIEnv* env, jclass clazz, jobject obj_header, jint offset, jint width, jint height) {
	char* header = (char*)env->GetDirectBufferAddress(obj_header);


//@line:206

		etc1_pkm_format_header((etc1_byte*)header + offset, width, height);
	

}

static inline jint wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_getWidthPKM
(JNIEnv* env, jclass clazz, jobject obj_header, jint offset, char* header) {

//@line:213

		return etc1_pkm_get_width((etc1_byte*)header + offset);
	
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_getWidthPKM(JNIEnv* env, jclass clazz, jobject obj_header, jint offset) {
	char* header = (char*)env->GetDirectBufferAddress(obj_header);

	jint JNI_returnValue = wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_getWidthPKM(env, clazz, obj_header, offset, header);


	return JNI_returnValue;
}

static inline jint wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_getHeightPKM
(JNIEnv* env, jclass clazz, jobject obj_header, jint offset, char* header) {

//@line:220

		return etc1_pkm_get_height((etc1_byte*)header + offset);
	
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_getHeightPKM(JNIEnv* env, jclass clazz, jobject obj_header, jint offset) {
	char* header = (char*)env->GetDirectBufferAddress(obj_header);

	jint JNI_returnValue = wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_getHeightPKM(env, clazz, obj_header, offset, header);


	return JNI_returnValue;
}

static inline jboolean wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_isValidPKM
(JNIEnv* env, jclass clazz, jobject obj_header, jint offset, char* header) {

//@line:227

		return etc1_pkm_is_valid((etc1_byte*)header + offset) != 0?true:false;
	
}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_isValidPKM(JNIEnv* env, jclass clazz, jobject obj_header, jint offset) {
	char* header = (char*)env->GetDirectBufferAddress(obj_header);

	jboolean JNI_returnValue = wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_isValidPKM(env, clazz, obj_header, offset, header);


	return JNI_returnValue;
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_decodeImage(JNIEnv* env, jclass clazz, jobject obj_compressedData, jint offset, jobject obj_decodedData, jint offsetDec, jint width, jint height, jint pixelSize) {
	char* compressedData = (char*)env->GetDirectBufferAddress(obj_compressedData);
	char* decodedData = (char*)env->GetDirectBufferAddress(obj_decodedData);


//@line:241

		etc1_decode_image((etc1_byte*)compressedData + offset, (etc1_byte*)decodedData + offsetDec, width, height, pixelSize, width * pixelSize);
	

}

static inline jobject wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_encodeImage
(JNIEnv* env, jclass clazz, jobject obj_imageData, jint offset, jint width, jint height, jint pixelSize, char* imageData) {

//@line:252

		int compressedSize = etc1_get_encoded_data_size(width, height);
		etc1_byte* compressedData = (etc1_byte*)malloc(compressedSize);
		etc1_encode_image((etc1_byte*)imageData + offset, width, height, pixelSize, width * pixelSize, compressedData);
		return env->NewDirectByteBuffer(compressedData, compressedSize);
	
}

JNIEXPORT jobject JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_encodeImage(JNIEnv* env, jclass clazz, jobject obj_imageData, jint offset, jint width, jint height, jint pixelSize) {
	char* imageData = (char*)env->GetDirectBufferAddress(obj_imageData);

	jobject JNI_returnValue = wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_encodeImage(env, clazz, obj_imageData, offset, width, height, pixelSize, imageData);


	return JNI_returnValue;
}

static inline jobject wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_encodeImagePKM
(JNIEnv* env, jclass clazz, jobject obj_imageData, jint offset, jint width, jint height, jint pixelSize, char* imageData) {

//@line:266

		int compressedSize = etc1_get_encoded_data_size(width, height);
		etc1_byte* compressed = (etc1_byte*)malloc(compressedSize + ETC_PKM_HEADER_SIZE);
		etc1_pkm_format_header(compressed, width, height);
		etc1_encode_image((etc1_byte*)imageData + offset, width, height, pixelSize, width * pixelSize, compressed + ETC_PKM_HEADER_SIZE);
		return env->NewDirectByteBuffer(compressed, compressedSize + ETC_PKM_HEADER_SIZE);
	
}

JNIEXPORT jobject JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_encodeImagePKM(JNIEnv* env, jclass clazz, jobject obj_imageData, jint offset, jint width, jint height, jint pixelSize) {
	char* imageData = (char*)env->GetDirectBufferAddress(obj_imageData);

	jobject JNI_returnValue = wrapped_Java_com_badlogic_gdx_graphics_glutils_ETC1_encodeImagePKM(env, clazz, obj_imageData, offset, width, height, pixelSize, imageData);


	return JNI_returnValue;
}

