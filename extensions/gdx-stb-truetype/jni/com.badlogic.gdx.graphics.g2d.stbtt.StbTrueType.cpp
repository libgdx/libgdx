#include <com.badlogic.gdx.graphics.g2d.stbtt.StbTrueType.h>
	#define STB_TRUETYPE_IMPLEMENTATION	#include <stb_truetype.h>	 JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_initFont
(JNIEnv* env, jclass clazz, jbyteArray obj_data, jint offset) {
	char* data = (char*)env->GetPrimitiveArrayCritical(obj_data, 0);

	stbtt_fontinfo* info = (stbtt_fontinfo*)malloc(sizeof(stbtt_fontinfo));
	int result = stbtt_InitFont(info, (const unsigned char*)data, offset);
		env->ReleasePrimitiveArrayCritical(obj_data, data, 0);

	if(!result) {
		free(info);
		return 0;
	} else {
		return (jlong)info;
	}
	
}
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_disposeFont
(JNIEnv* env, jclass clazz, jlong info) {

	free((void*)info);
	
}
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_findGlyphIndex
(JNIEnv* env, jclass clazz, jlong info, jint unicodeCodepoint) {

	return stbtt_FindGlyphIndex((stbtt_fontinfo*)info, unicodeCodepoint);
	
}
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_scaleForPixelHeight
(JNIEnv* env, jclass clazz, jlong info, jfloat pixels) {

	return stbtt_ScaleForPixelHeight((stbtt_fontinfo*)info, pixels);
	
}
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getFontVMetrics
(JNIEnv* env, jclass clazz, jlong info, jintArray obj_metrics) {
	int* metrics = (int*)env->GetPrimitiveArrayCritical(obj_metrics, 0);

	int ascent = 0;
	int descent = 0;
	int lineGap = 0;
	stbtt_GetFontVMetrics((stbtt_fontinfo*)info, &ascent, &descent, &lineGap);
	metrics[0] = ascent;
	metrics[1] = descent;
	metrics[2] = lineGap;
	
	env->ReleasePrimitiveArrayCritical(obj_metrics, metrics, 0);
}
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getCodepointHMetrics
(JNIEnv* env, jclass clazz, jlong info, jint codePoint, jintArray obj_metrics) {
	int* metrics = (int*)env->GetPrimitiveArrayCritical(obj_metrics, 0);

	int advanceWidth = 0;
	int leftSideBearing = 0;
	stbtt_GetCodepointHMetrics((stbtt_fontinfo*)info, codePoint, &advanceWidth, &leftSideBearing);
	metrics[0] = advanceWidth;
	metrics[1] = leftSideBearing;
	
	env->ReleasePrimitiveArrayCritical(obj_metrics, metrics, 0);
}
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getCodepointKernAdvance
(JNIEnv* env, jclass clazz, jlong info, jint char1, jint char2) {

	return stbtt_GetCodepointKernAdvance((stbtt_fontinfo*)info, char1, char2);	
	
}
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getCodePointBox
(JNIEnv* env, jclass clazz, jlong info, jint codePoint, jintArray obj_box) {
	int* box = (int*)env->GetPrimitiveArrayCritical(obj_box, 0);

	int x0, y0, x1, y1;
	x0 = y0 = x1 = y1 = 0;
	int result = stbtt_GetCodepointBox((stbtt_fontinfo*)info, codePoint, &x0, &y0, &x1, &y1);	
	box[0] = x0;
	box[1] = y0;
	box[2] = x1;
	box[3] = y1;
		env->ReleasePrimitiveArrayCritical(obj_box, box, 0);

	return result;
	
}
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getGlyphHMetrics
(JNIEnv* env, jclass clazz, jlong info, jint glyphIndex, jintArray obj_metrics) {
	int* metrics = (int*)env->GetPrimitiveArrayCritical(obj_metrics, 0);

	int advanceWidth = 0;
	int leftSideBearing = 0;
	stbtt_GetGlyphHMetrics((stbtt_fontinfo*)info, glyphIndex, &advanceWidth, &leftSideBearing);

	metrics[0] = advanceWidth;
	metrics[1] = leftSideBearing;
	
	env->ReleasePrimitiveArrayCritical(obj_metrics, metrics, 0);
}
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getGlyphKernAdvance
(JNIEnv* env, jclass clazz, jlong info, jint glyph1, jint glyph2) {

	return stbtt_GetGlyphKernAdvance((stbtt_fontinfo*)info, glyph1, glyph2);
	
}
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getGlyphBox
(JNIEnv* env, jclass clazz, jlong info, jint glyphIndex, jintArray obj_box) {
	int* box = (int*)env->GetPrimitiveArrayCritical(obj_box, 0);

	int x0, y0, x1, y1;
	x0 = y0 = x1 = y1 = 0;
	int result = stbtt_GetGlyphBox((stbtt_fontinfo*)info, glyphIndex, &x0, &y0, &x1, &y1);
	box[0] = x0;
	box[1] = y0;
	box[2] = x1;
	box[3] = y1;
		env->ReleasePrimitiveArrayCritical(obj_box, box, 0);

	return result;
	
}
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getCodePointShape
(JNIEnv* env, jclass clazz, jlong info, jint codePoint, jlongArray obj_vertices) {
	long long* vertices = (long long*)env->GetPrimitiveArrayCritical(obj_vertices, 0);

	stbtt_vertex* verticesAddr = 0;
	int result = stbtt_GetCodepointShape((stbtt_fontinfo*)info, codePoint, &verticesAddr);
	vertices[0] = (jlong)verticesAddr;
		env->ReleasePrimitiveArrayCritical(obj_vertices, vertices, 0);

	return result;
	
}
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getGlyphShape
(JNIEnv* env, jclass clazz, jlong info, jint glyphIndex, jlongArray obj_vertices) {
	long long* vertices = (long long*)env->GetPrimitiveArrayCritical(obj_vertices, 0);

	stbtt_vertex* verticesAddr = 0;
	int result = stbtt_GetGlyphShape((stbtt_fontinfo*)info, glyphIndex, &verticesAddr);
	vertices[0] = (jlong)verticesAddr;
		env->ReleasePrimitiveArrayCritical(obj_vertices, vertices, 0);

	return result;
	
}
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getShapeVertex
(JNIEnv* env, jclass clazz, jlong vertices, jint index, jintArray obj_vertex) {
	int* vertex = (int*)env->GetPrimitiveArrayCritical(obj_vertex, 0);

	stbtt_vertex* verts = (stbtt_vertex*)vertices;
	vertex[0] = verts[index].x;
	vertex[1] = verts[index].y;
	vertex[2] = verts[index].cx;
	vertex[3] = verts[index].cy;
	vertex[4] = verts[index].type;
	vertex[5] = verts[index].padding;
	
	env->ReleasePrimitiveArrayCritical(obj_vertex, vertex, 0);
}
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_freeShape
(JNIEnv* env, jclass clazz, jlong info, jlong vertices) {

	stbtt_FreeShape((stbtt_fontinfo*)info, (stbtt_vertex*)vertices);
	
}
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_makeCodepointBitmap
(JNIEnv* env, jclass clazz, jlong info, jobject obj_bitmap, jint bitmapWidth, jint bitmapHeight, jint bitmapStride, jfloat scaleX, jfloat scaleY, jint codePoint) {
	char* bitmap = (char*)env->GetDirectBufferAddress(obj_bitmap);

	stbtt_MakeCodepointBitmap((stbtt_fontinfo*)info, (unsigned char*)bitmap, bitmapWidth, bitmapHeight, bitmapStride, scaleX, scaleY, codePoint);
	
}
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getCodepointBitmapBox
(JNIEnv* env, jclass clazz, jlong info, jint codePoint, jfloat scaleX, jfloat scaleY, jintArray obj_box) {
	int* box = (int*)env->GetPrimitiveArrayCritical(obj_box, 0);

	int x0, y0, x1, y1;
	x0 = y0 = x1 = y1 = 0;
	stbtt_GetCodepointBitmapBox((stbtt_fontinfo*)info, codePoint, scaleX, scaleY, &x0, &y0, &x1, &y1);
	box[0] = x0;
	box[1] = y0;
	box[2] = x1;
	box[3] = y1;
	
	env->ReleasePrimitiveArrayCritical(obj_box, box, 0);
}
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_makeGlyphBitmap
(JNIEnv* env, jclass clazz, jlong info, jobject obj_bitmap, jint bitmapWidth, jint bitmapHeight, jint bitmapStride, jfloat scaleX, jfloat scaleY, jfloat shiftX, jfloat shiftY, jint glyph) {
	char* bitmap = (char*)env->GetDirectBufferAddress(obj_bitmap);

	stbtt_MakeGlyphBitmap((stbtt_fontinfo*)info, (unsigned char*)bitmap, bitmapWidth, bitmapHeight, bitmapStride, scaleX, scaleY, shiftX, shiftY, glyph);
	
}
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getGlyphBitmapBox
(JNIEnv* env, jclass clazz, jlong info, jint glyph, jfloat scaleX, jfloat scaleY, jfloat shiftX, jfloat shiftY, jintArray obj_box) {
	int* box = (int*)env->GetPrimitiveArrayCritical(obj_box, 0);

	int x0, y0, x1, y1;
	x0 = y0 = x1 = y1 = 0;
	stbtt_GetGlyphBitmapBox((stbtt_fontinfo*)info, glyph, scaleX, scaleY, shiftX, shiftY, &x0, &y0, &x1, &y1);
	box[0] = x0;
	box[1] = y0;
	box[2] = x1;
	box[3] = y1;
	
	env->ReleasePrimitiveArrayCritical(obj_box, box, 0);
}
