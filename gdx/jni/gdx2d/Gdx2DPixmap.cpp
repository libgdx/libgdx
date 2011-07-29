#include "Gdx2DPixmap.h"
#include "gdx2d.h"

/*
 * Class:     com_badlogic_gdx_graphics_g2d_Gdx2DPixmap
 * Method:    load
 * Signature: ([J[BII)Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_load
  (JNIEnv *env, jclass, jlongArray nativeData, jbyteArray buffer, jint offset, jint len, jint req_format) {
	const unsigned char* p_buffer = (const unsigned char*)env->GetPrimitiveArrayCritical(buffer, 0) + offset;
	gdx2d_pixmap* pixmap = gdx2d_load(p_buffer, len, req_format);
	env->ReleasePrimitiveArrayCritical(buffer, (char*)p_buffer, 0);

	if(pixmap==0)
		return 0;

	jobject pixel_buffer = env->NewDirectByteBuffer((void*)pixmap->pixels, pixmap->width * pixmap->height * pixmap->format);
	jlong* p_native_data = (jlong*)env->GetPrimitiveArrayCritical(nativeData, 0);
	p_native_data[0] = (jlong)pixmap;
	p_native_data[1] = pixmap->width;
	p_native_data[2] = pixmap->height;
	p_native_data[3] = pixmap->format;
	env->ReleasePrimitiveArrayCritical(nativeData, p_native_data, 0);

	return pixel_buffer;
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_Gdx2DPixmap
 * Method:    newPixmap
 * Signature: ([JIII)Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_newPixmap
  (JNIEnv *env, jclass, jlongArray nativeData, jint width, jint height, jint format) {
	gdx2d_pixmap* pixmap = gdx2d_new(width, height, format);
	if(pixmap==0)
		return 0;

	jobject pixel_buffer = env->NewDirectByteBuffer((void*)pixmap->pixels, pixmap->width * pixmap->height * pixmap->format);
	jlong* p_native_data = (jlong*)env->GetPrimitiveArrayCritical(nativeData, 0);
	p_native_data[0] = (jlong)pixmap;
	p_native_data[1] = pixmap->width;
	p_native_data[2] = pixmap->height;
	p_native_data[3] = pixmap->format;
	env->ReleasePrimitiveArrayCritical(nativeData, p_native_data, 0);

	return pixel_buffer;
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_Gdx2DPixmap
 * Method:    free
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_free
  (JNIEnv *, jclass, jlong pixmap) {
	gdx2d_free((gdx2d_pixmap*)pixmap);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_Gdx2DPixmap
 * Method:    clear
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_clear
  (JNIEnv *, jclass, jlong pixmap, jint color) {
	gdx2d_clear((gdx2d_pixmap*)pixmap, color);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_Gdx2DPixmap
 * Method:    setPixel
 * Signature: (JIII)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_setPixel
  (JNIEnv *, jclass, jlong pixmap, jint x, jint y, jint color) {
	gdx2d_set_pixel((gdx2d_pixmap*)pixmap, x, y, color);
}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_getPixel
(JNIEnv *, jclass, jlong pixmap, jint x, jint y) {
	return gdx2d_get_pixel((gdx2d_pixmap*)pixmap, x, y);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_Gdx2DPixmap
 * Method:    drawLine
 * Signature: (JIIIII)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_drawLine
  (JNIEnv *, jclass, jlong pixmap, jint x, jint y, jint x2, jint y2, jint color) {
	gdx2d_draw_line((gdx2d_pixmap*)pixmap, x, y, x2, y2, color);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_Gdx2DPixmap
 * Method:    drawRect
 * Signature: (JIIIII)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_drawRect
  (JNIEnv *, jclass, jlong pixmap, jint x, jint y, jint width, jint height, jint color) {
	gdx2d_draw_rect((gdx2d_pixmap*)pixmap, x, y, width, height, color);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_Gdx2DPixmap
 * Method:    drawCircle
 * Signature: (JIIII)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_drawCircle
  (JNIEnv *, jclass, jlong pixmap, jint x, jint y, jint radius, jint color) {
	gdx2d_draw_circle((gdx2d_pixmap*)pixmap, x, y, radius, color);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_Gdx2DPixmap
 * Method:    fillRect
 * Signature: (JIIIII)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_fillRect
  (JNIEnv *, jclass, jlong pixmap, jint x, jint y, jint width, jint height, jint color) {
	gdx2d_fill_rect((gdx2d_pixmap*)pixmap, x, y, width, height, color);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_Gdx2DPixmap
 * Method:    fillCircle
 * Signature: (JIIII)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_fillCircle
  (JNIEnv *, jclass, jlong pixmap, jint x, jint y, jint radius, jint color) {
	gdx2d_fill_circle((gdx2d_pixmap*)pixmap, x, y, radius, color);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_Gdx2DPixmap
 * Method:    drawPixmap
 * Signature: (JJIIIIIIIIII)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_drawPixmap
  (JNIEnv *, jclass, jlong src, jlong dst, jint src_x, jint src_y, jint src_width, jint src_height, jint dst_x, jint dst_y, jint dst_width, jint dst_height) {
	gdx2d_draw_pixmap((gdx2d_pixmap*)src, (gdx2d_pixmap*)dst, src_x, src_y, src_width, src_height, dst_x, dst_y, dst_width, dst_height);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_Gdx2DPixmap
 * Method:    setBlend
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_setBlend
  (JNIEnv *, jclass, jint blend) {
	gdx2d_set_blend(blend);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_Gdx2DPixmap
 * Method:    setScale
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_setScale
  (JNIEnv *, jclass, jint scale) {
	gdx2d_set_scale(scale);
}
