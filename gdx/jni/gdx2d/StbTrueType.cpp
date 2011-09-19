#include "StbTrueType.h"

#define STB_TRUETYPE_IMPLEMENTATION
#include "stb_truetype.h"

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    initFont
 * Signature: ([BI)J
 */
JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_initFont(
		JNIEnv *env, jclass, jbyteArray data, jint offset) {
	const unsigned char* p_data =
			(const unsigned char*) env->GetPrimitiveArrayCritical(data, 0);
	stbtt_fontinfo* info = (stbtt_fontinfo*) malloc(sizeof(stbtt_fontinfo));
	int result = stbtt_InitFont(info, p_data, offset);
	env->ReleasePrimitiveArrayCritical(data, (char*) p_data, 0);
	if (result == 0) {
		free(info);
		return 0;
	} else {
		return (jlong) info;
	}
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    disposeFont
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_disposeFont
(JNIEnv *env, jclass, jlong addr) {
	free((void*)addr);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    findGlyphIndex
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_findGlyphIndex(
		JNIEnv *env, jclass, jlong addr, jint codePoint) {
	return stbtt_FindGlyphIndex((stbtt_fontinfo*) addr, codePoint);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    scaleForPixelHeight
 * Signature: (JF)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_scaleForPixelHeight(
		JNIEnv *env, jclass, jlong addr, jfloat pixels) {
	return stbtt_ScaleForPixelHeight((stbtt_fontinfo*) addr, pixels);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    getFontVMetrics
 * Signature: (J[I)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getFontVMetrics
(JNIEnv *env, jclass, jlong addr, jintArray metrics) {
	int ascent = 0;
	int descent = 0;
	int lineGap = 0;
	stbtt_GetFontVMetrics((stbtt_fontinfo*)addr, &ascent, &descent, &lineGap);

	int* p_metrics = (int*)env->GetPrimitiveArrayCritical(metrics, 0);
	p_metrics[0] = ascent;
	p_metrics[1] = descent;
	p_metrics[2] = lineGap;
	env->ReleasePrimitiveArrayCritical(metrics, p_metrics, 0);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    getCodepointHMetrics
 * Signature: (JI[I)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getCodepointHMetrics
(JNIEnv *env, jclass, jlong addr, jint codePoint, jintArray metrics) {
	int advanceWidth = 0;
	int leftSideBearing = 0;
	stbtt_GetCodepointHMetrics((stbtt_fontinfo*)addr, codePoint, &advanceWidth, &leftSideBearing);

	unsigned int* p_metrics = (unsigned int*)env->GetPrimitiveArrayCritical(metrics, 0);
	p_metrics[0] = advanceWidth;
	p_metrics[1] = leftSideBearing;
	env->ReleasePrimitiveArrayCritical(metrics, p_metrics, 0);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    getCodepointKernAdvance
 * Signature: (JII)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getCodepointKernAdvance(
		JNIEnv *env, jclass, jlong addr, jint char1, jint char2) {
	return stbtt_GetCodepointKernAdvance((stbtt_fontinfo*) addr, char1, char2);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    getCodePointBox
 * Signature: (JI[I)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getCodePointBox(
		JNIEnv *env, jclass, jlong addr, jint codePoint, jintArray box) {
	int x0, y0, x1, y1;
	x0 = y0 = x1 = y1 = 0;
	int result = stbtt_GetCodepointBox((stbtt_fontinfo*) addr, codePoint, &x0,
			&y0, &x1, &y1);

	int* p_box = (int*) env->GetPrimitiveArrayCritical(box, 0);
	p_box[0] = x0;
	p_box[1] = y0;
	p_box[2] = x1;
	p_box[3] = y1;
	env->ReleasePrimitiveArrayCritical(box, p_box, 0);
	return result;
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    getGlyphHMetrics
 * Signature: (JI[I)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getGlyphHMetrics
(JNIEnv *env, jclass, jlong addr, jint glyphIndex, jintArray metrics) {
	int advanceWidth = 0;
	int leftSideBearing = 0;
	stbtt_GetGlyphHMetrics((stbtt_fontinfo*)addr, glyphIndex, &advanceWidth, &leftSideBearing);

	unsigned int* p_metrics = (unsigned int*)env->GetPrimitiveArrayCritical(metrics, 0);
	p_metrics[0] = advanceWidth;
	p_metrics[1] = leftSideBearing;
	env->ReleasePrimitiveArrayCritical(metrics, p_metrics, 0);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    getGlyphKernAdvance
 * Signature: (JII)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getGlyphKernAdvance(
		JNIEnv *env, jclass, jlong addr, jint glyph1, jint glyph2) {
	return stbtt_GetGlyphKernAdvance((stbtt_fontinfo*) addr, glyph1, glyph2);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    getGlyphBox
 * Signature: (JI[I)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getGlyphBox(
		JNIEnv *env, jclass, jlong addr, jint glyphIndex, jintArray box) {
	int x0, y0, x1, y1;
	x0 = y0 = x1 = y1 = 0;
	int result = stbtt_GetGlyphBox((stbtt_fontinfo*) addr, glyphIndex, &x0, &y0,
			&x1, &y1);

	int* p_box = (int*) env->GetPrimitiveArrayCritical(box, 0);
	p_box[0] = x0;
	p_box[1] = y0;
	p_box[2] = x1;
	p_box[3] = y1;
	env->ReleasePrimitiveArrayCritical(box, p_box, 0);
	return result;
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    getCodePointShape
 * Signature: (JI[J)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getCodePointShape(
		JNIEnv *env, jclass, jlong addr, jint codePoint, jlongArray vertices) {
	stbtt_vertex* verticesAddr = 0;
	int result = stbtt_GetCodepointShape((stbtt_fontinfo*) addr, codePoint,
			&verticesAddr);

	jlong* p_vertices = (jlong*) env->GetPrimitiveArrayCritical(vertices, 0);
	p_vertices[0] = (jlong) verticesAddr;
	env->ReleasePrimitiveArrayCritical(vertices, p_vertices, 0);
	return result;
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    getGlyphShape
 * Signature: (JI[J)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getGlyphShape(
		JNIEnv *env, jclass, jlong addr, jint glyphIndex, jlongArray vertices) {
	stbtt_vertex* verticesAddr = 0;
	int result = stbtt_GetGlyphShape((stbtt_fontinfo*) addr, glyphIndex,
			&verticesAddr);

	jlong* p_vertices = (jlong*) env->GetPrimitiveArrayCritical(vertices, 0);
	p_vertices[0] = (jlong) verticesAddr;
	env->ReleasePrimitiveArrayCritical(vertices, p_vertices, 0);
	return result;
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    getShapeVertex
 * Signature: (JI[I)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getShapeVertex
(JNIEnv *env, jclass, jlong addr, jint index, jintArray vertex) {
	stbtt_vertex* vertices = (stbtt_vertex*)addr;

	jint* p_vertex = (jint*)env->GetPrimitiveArrayCritical(vertex, 0);
	p_vertex[0] = vertices[index].x;
	p_vertex[1] = vertices[index].y;
	p_vertex[2] = vertices[index].cx;
	p_vertex[3] = vertices[index].cy;
	p_vertex[4] = vertices[index].type;
	p_vertex[5] = vertices[index].padding;
	env->ReleasePrimitiveArrayCritical(vertex, p_vertex, 0);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    freeShape
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_freeShape
(JNIEnv *env, jclass, jlong addr, jlong verticesAddr) {
	stbtt_FreeShape((stbtt_fontinfo*)addr, (stbtt_vertex*)verticesAddr);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    makeCodepointBitmap
 * Signature: (JLjava/nio/ByteBuffer;IIIFFI)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_makeCodepointBitmap
(JNIEnv *env, jclass, jlong addr, jobject bitmap, jint bitmapWidth, jint bitmapHeight, jint bitmapStride, jfloat scaleX, jfloat scaleY, jint codePoint) {
	unsigned char * output = (unsigned char *)env->GetDirectBufferAddress(bitmap);
	stbtt_MakeCodepointBitmap((stbtt_fontinfo*)addr, output, bitmapWidth, bitmapHeight, bitmapStride, scaleX, scaleY, codePoint);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    getCodepointBitmapBox
 * Signature: (JIFF[I)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getCodepointBitmapBox
(JNIEnv *env, jclass, jlong addr, jint codepoint, jfloat scaleX, jfloat scaleY, jintArray box) {
	int x0, y0, x1, y1;
	x0 = y0 = x1 = y1 = 0;
	stbtt_GetCodepointBitmapBox((stbtt_fontinfo*)addr, codepoint, scaleX, scaleY, &x0, &y0, &x1, &y1);

	int* p_box = (int*)env->GetPrimitiveArrayCritical(box, 0);
	p_box[0] = x0;
	p_box[1] = y0;
	p_box[2] = x1;
	p_box[3] = y1;
	env->ReleasePrimitiveArrayCritical(box, p_box, 0);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    makeGlyphBitmap
 * Signature: (JLjava/nio/ByteBuffer;IIIFFFFI)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_makeGlyphBitmap
(JNIEnv *env, jclass, jlong addr, jobject bitmap, jint bitmapWidth, jint bitmapHeight, jint bitmapStride, jfloat scaleX, jfloat scaleY, jfloat shiftX, jfloat shiftY, jint glyphIndex) {
	unsigned char * output = (unsigned char *)env->GetDirectBufferAddress(bitmap);
	stbtt_MakeGlyphBitmap((stbtt_fontinfo*)addr, output, bitmapWidth, bitmapHeight, bitmapStride, scaleX, scaleY, shiftX, shiftY, glyphIndex);
}

/*
 * Class:     com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType
 * Method:    getGlyphBitmapBox
 * Signature: (JIFFFF[I)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_stbtt_StbTrueType_getGlyphBitmapBox
(JNIEnv *env, jclass, jlong addr, jint glyphIndex, jfloat scaleX, jfloat scaleY, jfloat shiftX, jfloat shiftY, jintArray box) {
	int x0, y0, x1, y1;
	x0 = y0 = x1 = y1 = 0;
	stbtt_GetGlyphBitmapBox((stbtt_fontinfo*)addr, glyphIndex, scaleX, scaleY, shiftX, shiftY, &x0, &y0, &x1, &y1);

	int* p_box = (int*)env->GetPrimitiveArrayCritical(box, 0);
	p_box[0] = x0;
	p_box[1] = y0;
	p_box[2] = x1;
	p_box[3] = y1;
	env->ReleasePrimitiveArrayCritical(box, p_box, 0);
}
