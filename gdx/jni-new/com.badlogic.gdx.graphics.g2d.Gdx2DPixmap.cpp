#include <com.badlogic.gdx.graphics.g2d.Gdx2DPixmap.h>

//@line:238

	#include <gdx2d/gdx2d.h>
	#include <stdlib.h>
	 static inline jobject wrapped_Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_load
(JNIEnv* env, jclass clazz, jlongArray obj_nativeData, jbyteArray obj_buffer, jint offset, jint len, jint requestedFormat, long long* nativeData, char* buffer) {

//@line:243

		gdx2d_pixmap* pixmap = gdx2d_load((const unsigned char*)buffer + offset, len, requestedFormat);
	
		if(pixmap==0)
			return 0;
	
		jobject pixel_buffer = env->NewDirectByteBuffer((void*)pixmap->pixels, pixmap->width * pixmap->height * gdx2d_bytes_per_pixel(pixmap->format));
		nativeData[0] = (jlong)pixmap;
		nativeData[1] = pixmap->width;
		nativeData[2] = pixmap->height;
		nativeData[3] = pixmap->format;
	
		return pixel_buffer;
	
}

JNIEXPORT jobject JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_load(JNIEnv* env, jclass clazz, jlongArray obj_nativeData, jbyteArray obj_buffer, jint offset, jint len, jint requestedFormat) {
	long long* nativeData = (long long*)env->GetPrimitiveArrayCritical(obj_nativeData, 0);
	char* buffer = (char*)env->GetPrimitiveArrayCritical(obj_buffer, 0);

	jobject JNI_returnValue = wrapped_Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_load(env, clazz, obj_nativeData, obj_buffer, offset, len, requestedFormat, nativeData, buffer);

	env->ReleasePrimitiveArrayCritical(obj_nativeData, nativeData, 0);
	env->ReleasePrimitiveArrayCritical(obj_buffer, buffer, 0);

	return JNI_returnValue;
}

static inline jobject wrapped_Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_newPixmap
(JNIEnv* env, jclass clazz, jlongArray obj_nativeData, jint width, jint height, jint format, long long* nativeData) {

//@line:258

		gdx2d_pixmap* pixmap = gdx2d_new(width, height, format);
		if(pixmap==0)
			return 0;
	
		jobject pixel_buffer = env->NewDirectByteBuffer((void*)pixmap->pixels, pixmap->width * pixmap->height * gdx2d_bytes_per_pixel(pixmap->format));
		nativeData[0] = (jlong)pixmap;
		nativeData[1] = pixmap->width;
		nativeData[2] = pixmap->height;
		nativeData[3] = pixmap->format;
	
		return pixel_buffer;
	
}

JNIEXPORT jobject JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_newPixmap(JNIEnv* env, jclass clazz, jlongArray obj_nativeData, jint width, jint height, jint format) {
	long long* nativeData = (long long*)env->GetPrimitiveArrayCritical(obj_nativeData, 0);

	jobject JNI_returnValue = wrapped_Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_newPixmap(env, clazz, obj_nativeData, width, height, format, nativeData);

	env->ReleasePrimitiveArrayCritical(obj_nativeData, nativeData, 0);

	return JNI_returnValue;
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_free(JNIEnv* env, jclass clazz, jlong pixmap) {


//@line:272

		gdx2d_free((gdx2d_pixmap*)pixmap);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_clear(JNIEnv* env, jclass clazz, jlong pixmap, jint color) {


//@line:276

		gdx2d_clear((gdx2d_pixmap*)pixmap, color);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_setPixel(JNIEnv* env, jclass clazz, jlong pixmap, jint x, jint y, jint color) {


//@line:280

		gdx2d_set_pixel((gdx2d_pixmap*)pixmap, x, y, color);
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_getPixel(JNIEnv* env, jclass clazz, jlong pixmap, jint x, jint y) {


//@line:284

		return gdx2d_get_pixel((gdx2d_pixmap*)pixmap, x, y);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_drawLine(JNIEnv* env, jclass clazz, jlong pixmap, jint x, jint y, jint x2, jint y2, jint color) {


//@line:288

		gdx2d_draw_line((gdx2d_pixmap*)pixmap, x, y, x2, y2, color);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_drawRect(JNIEnv* env, jclass clazz, jlong pixmap, jint x, jint y, jint width, jint height, jint color) {


//@line:292

		gdx2d_draw_rect((gdx2d_pixmap*)pixmap, x, y, width, height, color);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_drawCircle(JNIEnv* env, jclass clazz, jlong pixmap, jint x, jint y, jint radius, jint color) {


//@line:296

		gdx2d_draw_circle((gdx2d_pixmap*)pixmap, x, y, radius, color);	
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_fillRect(JNIEnv* env, jclass clazz, jlong pixmap, jint x, jint y, jint width, jint height, jint color) {


//@line:300

		gdx2d_fill_rect((gdx2d_pixmap*)pixmap, x, y, width, height, color);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_fillCircle(JNIEnv* env, jclass clazz, jlong pixmap, jint x, jint y, jint radius, jint color) {


//@line:304

		gdx2d_fill_circle((gdx2d_pixmap*)pixmap, x, y, radius, color);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_drawPixmap(JNIEnv* env, jclass clazz, jlong src, jlong dst, jint srcX, jint srcY, jint srcWidth, jint srcHeight, jint dstX, jint dstY, jint dstWidth, jint dstHeight) {


//@line:309

		gdx2d_draw_pixmap((gdx2d_pixmap*)src, (gdx2d_pixmap*)dst, srcX, srcY, srcWidth, srcHeight, dstX, dstY, dstWidth, dstHeight);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_setBlend(JNIEnv* env, jclass clazz, jint blend) {


//@line:313

		gdx2d_set_blend(blend);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_setScale(JNIEnv* env, jclass clazz, jint scale) {


//@line:317

		gdx2d_set_scale(scale);
	

}

