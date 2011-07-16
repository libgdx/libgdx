#include <etc1/ETC1.h>
#include <etc1/etc1_utils.h>
#include <stdlib.h>


JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_getCompressedDataSize
  (JNIEnv *, jclass, jint width, jint height) {
  return etc1_get_encoded_data_size(width, height);
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_formatHeader
  (JNIEnv *env, jclass, jobject header, jint offset, jint width, jint height) {
	etc1_byte* headerPtr = (etc1_byte*)env->GetDirectBufferAddress(header) + offset;
	etc1_pkm_format_header(headerPtr, width, height);
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_getWidthPKM
  (JNIEnv *env, jclass, jobject header, jint offset) {
	etc1_byte* headerPtr = (etc1_byte*)env->GetDirectBufferAddress(header) + offset;
	return etc1_pkm_get_width(headerPtr);
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_getHeightPKM
  (JNIEnv *env, jclass, jobject header, jint offset) {
	etc1_byte* headerPtr = (etc1_byte*)env->GetDirectBufferAddress(header) + offset;
	return etc1_pkm_get_height(headerPtr);
}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_isValidPKM
  (JNIEnv *env, jclass, jobject header, jint offset) {
	etc1_byte* headerPtr = (etc1_byte*)env->GetDirectBufferAddress(header) + offset;
	return etc1_pkm_is_valid(headerPtr) != 0?true:false;
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_decodeImage
  (JNIEnv *env, jclass, jobject compressedData, jint offset, jobject decodedData, jint offsetDec, jint width, jint height, jint pixelSize) {
	etc1_byte* comprPtr = (etc1_byte*)env->GetDirectBufferAddress(compressedData) + offset;
	etc1_byte* imgPtr = (etc1_byte*)env->GetDirectBufferAddress(decodedData) + offsetDec;
	etc1_decode_image(comprPtr, imgPtr, width, height, pixelSize, width * pixelSize);
}

JNIEXPORT jobject JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_encodeImage
  (JNIEnv *env, jclass, jobject imageData, jint offset, jint width, jint height, jint pixelSize) {
	etc1_byte* imgPtr = (etc1_byte*)env->GetDirectBufferAddress(imageData) + offset;
	int compressedSize = etc1_get_encoded_data_size(width, height);
	etc1_byte* comprPtr = (etc1_byte*)malloc(compressedSize);
	etc1_encode_image(imgPtr, width, height, pixelSize, width * pixelSize, comprPtr);
	return env->NewDirectByteBuffer(comprPtr, compressedSize);
}

JNIEXPORT jobject JNICALL Java_com_badlogic_gdx_graphics_glutils_ETC1_encodeImagePKM
  (JNIEnv *env, jclass, jobject imageData, jint offset, jint width, jint height, jint pixelSize) {
  	etc1_byte* imgPtr = (etc1_byte*)env->GetDirectBufferAddress(imageData) + offset;
	int compressedSize = etc1_get_encoded_data_size(width, height);
	etc1_byte* comprPtr = (etc1_byte*)malloc(compressedSize + ETC_PKM_HEADER_SIZE);
	etc1_pkm_format_header(comprPtr, width, height);
	etc1_encode_image(imgPtr, width, height, pixelSize, width * pixelSize, comprPtr + ETC_PKM_HEADER_SIZE);
	return env->NewDirectByteBuffer(comprPtr, compressedSize + ETC_PKM_HEADER_SIZE);
}