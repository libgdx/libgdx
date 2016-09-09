#include <com.badlogic.gdx.graphics.g2d.Gdx2DPixmap.h>

//@line:270

	#include <gdx2d/gdx2d.h>
	#include <stdlib.h>
	 JNIEXPORT jobject JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_load(JNIEnv* env, jclass clazz, jlongArray nativeData, jbyteArray buffer, jint offset, jint len) {

//@line:275

		const unsigned char* p_buffer = (const unsigned char*)env->GetPrimitiveArrayCritical(buffer, 0);
		gdx2d_pixmap* pixmap = gdx2d_load(p_buffer + offset, len);
		env->ReleasePrimitiveArrayCritical(buffer, (char*)p_buffer, 0);

		if(pixmap==0)
			return 0;

		jobject pixel_buffer = env->NewDirectByteBuffer((void*)pixmap->pixels, pixmap->width * pixmap->height * gdx2d_bytes_per_pixel(pixmap->format));
		jlong* p_native_data = (jlong*)env->GetPrimitiveArrayCritical(nativeData, 0);
		p_native_data[0] = (jlong)pixmap;
		p_native_data[1] = pixmap->width;
		p_native_data[2] = pixmap->height;
		p_native_data[3] = pixmap->format;
		env->ReleasePrimitiveArrayCritical(nativeData, p_native_data, 0);

		return pixel_buffer;
	 
}

JNIEXPORT jobject JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_newPixmap(JNIEnv* env, jclass clazz, jlongArray nativeData, jint width, jint height, jint format) {

//@line:294

		gdx2d_pixmap* pixmap = gdx2d_new(width, height, format);
		if(pixmap==0)
			return 0;

		jobject pixel_buffer = env->NewDirectByteBuffer((void*)pixmap->pixels, pixmap->width * pixmap->height * gdx2d_bytes_per_pixel(pixmap->format));
		jlong* p_native_data = (jlong*)env->GetPrimitiveArrayCritical(nativeData, 0);
		p_native_data[0] = (jlong)pixmap;
		p_native_data[1] = pixmap->width;
		p_native_data[2] = pixmap->height;
		p_native_data[3] = pixmap->format;
		env->ReleasePrimitiveArrayCritical(nativeData, p_native_data, 0);

		return pixel_buffer;
	 
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_free(JNIEnv* env, jclass clazz, jlong pixmap) {


//@line:310

		gdx2d_free((gdx2d_pixmap*)pixmap);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_clear(JNIEnv* env, jclass clazz, jlong pixmap, jint color) {


//@line:314

		gdx2d_clear((gdx2d_pixmap*)pixmap, color);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_setPixel(JNIEnv* env, jclass clazz, jlong pixmap, jint x, jint y, jint color) {


//@line:318

		gdx2d_set_pixel((gdx2d_pixmap*)pixmap, x, y, color);
	 

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_getPixel(JNIEnv* env, jclass clazz, jlong pixmap, jint x, jint y) {


//@line:322

		return gdx2d_get_pixel((gdx2d_pixmap*)pixmap, x, y);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_drawLine(JNIEnv* env, jclass clazz, jlong pixmap, jint x, jint y, jint x2, jint y2, jint color) {


//@line:326

		gdx2d_draw_line((gdx2d_pixmap*)pixmap, x, y, x2, y2, color);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_drawRect(JNIEnv* env, jclass clazz, jlong pixmap, jint x, jint y, jint width, jint height, jint color) {


//@line:330

		gdx2d_draw_rect((gdx2d_pixmap*)pixmap, x, y, width, height, color);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_drawCircle(JNIEnv* env, jclass clazz, jlong pixmap, jint x, jint y, jint radius, jint color) {


//@line:334

		gdx2d_draw_circle((gdx2d_pixmap*)pixmap, x, y, radius, color);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_fillRect(JNIEnv* env, jclass clazz, jlong pixmap, jint x, jint y, jint width, jint height, jint color) {


//@line:338

		gdx2d_fill_rect((gdx2d_pixmap*)pixmap, x, y, width, height, color);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_fillCircle(JNIEnv* env, jclass clazz, jlong pixmap, jint x, jint y, jint radius, jint color) {


//@line:342

		gdx2d_fill_circle((gdx2d_pixmap*)pixmap, x, y, radius, color);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_fillTriangle(JNIEnv* env, jclass clazz, jlong pixmap, jint x1, jint y1, jint x2, jint y2, jint x3, jint y3, jint color) {


//@line:346

		gdx2d_fill_triangle((gdx2d_pixmap*)pixmap, x1, y1, x2, y2, x3, y3, color);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_drawPixmap(JNIEnv* env, jclass clazz, jlong src, jlong dst, jint srcX, jint srcY, jint srcWidth, jint srcHeight, jint dstX, jint dstY, jint dstWidth, jint dstHeight) {


//@line:351

		gdx2d_draw_pixmap((gdx2d_pixmap*)src, (gdx2d_pixmap*)dst, srcX, srcY, srcWidth, srcHeight, dstX, dstY, dstWidth, dstHeight);
		 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_setBlend(JNIEnv* env, jclass clazz, jint blend) {


//@line:355

		gdx2d_set_blend(blend);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_setScale(JNIEnv* env, jclass clazz, jint scale) {


//@line:359

		gdx2d_set_scale(scale);
	 

}

JNIEXPORT jstring JNICALL Java_com_badlogic_gdx_graphics_g2d_Gdx2DPixmap_getFailureReason(JNIEnv* env, jclass clazz) {


//@line:363

     return env->NewStringUTF(gdx2d_get_failure_reason());
	 

}

