#include <com.badlogic.gdx.graphics.g2d.freetype.FreeType.h>

//@line:7

	#include <ft2build.h>
	#include FT_FREETYPE_H
	 JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getFaceFlags(JNIEnv* env, jclass clazz, jlong face) {


//@line:31

			return ((FT_Face)face)->face_flags;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getStyleFlags(JNIEnv* env, jclass clazz, jlong face) {


//@line:35

			return ((FT_Face)face)->style_flags;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getNumGlyphs(JNIEnv* env, jclass clazz, jlong face) {


//@line:39

			return ((FT_Face)face)->num_glyphs;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getAscender(JNIEnv* env, jclass clazz, jlong face) {


//@line:43

			return ((FT_Face)face)->ascender;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getDescender(JNIEnv* env, jclass clazz, jlong face) {


//@line:47

			return ((FT_Face)face)->descender;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getHeight(JNIEnv* env, jclass clazz, jlong face) {


//@line:51

			return ((FT_Face)face)->height;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getMaxAdvanceWidth(JNIEnv* env, jclass clazz, jlong face) {


//@line:55

			return ((FT_Face)face)->max_advance_width;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getMaxAdvanceHeight(JNIEnv* env, jclass clazz, jlong face) {


//@line:59

			return ((FT_Face)face)->max_advance_height;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getUnderlinePosition(JNIEnv* env, jclass clazz, jlong face) {


//@line:63

			return ((FT_Face)face)->underline_position;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getUnderlineThickness(JNIEnv* env, jclass clazz, jlong face) {


//@line:67

			return ((FT_Face)face)->underline_thickness;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getGlyph(JNIEnv* env, jclass clazz, jlong face) {


//@line:71

			return (jlong)((FT_Face)face)->glyph;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getSize(JNIEnv* env, jclass clazz, jlong face) {


//@line:75

			return (jlong)((FT_Face)face)->size;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getMetrics(JNIEnv* env, jclass clazz, jlong slot) {


//@line:85

			return (jlong)&((FT_GlyphSlot)slot)->metrics;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getLinearHoriAdvance(JNIEnv* env, jclass clazz, jlong slot) {


//@line:89

			return ((FT_GlyphSlot)slot)->linearHoriAdvance;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getLinearVertAdvance(JNIEnv* env, jclass clazz, jlong slot) {


//@line:93

			return ((FT_GlyphSlot)slot)->linearVertAdvance;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getAdvanceX(JNIEnv* env, jclass clazz, jlong slot) {


//@line:97

			return ((FT_GlyphSlot)slot)->advance.x;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getAdvanceY(JNIEnv* env, jclass clazz, jlong slot) {


//@line:101

			return ((FT_GlyphSlot)slot)->advance.y;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getFormat(JNIEnv* env, jclass clazz, jlong slot) {


//@line:105

			return ((FT_GlyphSlot)slot)->format;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getBitmap(JNIEnv* env, jclass clazz, jlong slot) {


//@line:109

			return (jlong)&((FT_GlyphSlot)slot)->bitmap;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getBitmapLeft(JNIEnv* env, jclass clazz, jlong slot) {


//@line:113

			return ((FT_GlyphSlot)slot)->bitmap_left;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getBitmapTop(JNIEnv* env, jclass clazz, jlong slot) {


//@line:117

			return ((FT_GlyphSlot)slot)->bitmap_top;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getRows(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:127

			return ((FT_Bitmap*)bitmap)->rows;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getWidth(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:131

			return ((FT_Bitmap*)bitmap)->width;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getPitch(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:135

			return ((FT_Bitmap*)bitmap)->pitch;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getBuffer(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:139

			return (jlong)((FT_Bitmap*)bitmap)->buffer;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getNumGray(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:143

			return ((FT_Bitmap*)bitmap)->num_grays;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getPixelMode(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:147

			return ((FT_Bitmap*)bitmap)->pixel_mode;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getWidth(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:157

			return ((FT_Glyph_Metrics*)metrics)->width;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getHeight(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:161

			return ((FT_Glyph_Metrics*)metrics)->height;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getHoriBearingX(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:165

			return ((FT_Glyph_Metrics*)metrics)->horiBearingX;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getHoriBearingY(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:169

			return ((FT_Glyph_Metrics*)metrics)->horiBearingY;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getHoriAdvance(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:173

			return ((FT_Glyph_Metrics*)metrics)->horiAdvance;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getVertBearingX(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:177

			return ((FT_Glyph_Metrics*)metrics)->vertBearingX;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getVertBearingY(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:181

			return ((FT_Glyph_Metrics*)metrics)->vertBearingY;
		 

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getVertAdvance(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:185

			return ((FT_Glyph_Metrics*)metrics)->vertAdvance;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_initFreeType(JNIEnv* env, jclass clazz) {


//@line:262

		FT_Library library = 0;
		FT_Error error = FT_Init_FreeType(&library);
		if(error) return 0;
		else return (jlong)library;
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_doneFreeType(JNIEnv* env, jclass clazz, jlong library) {


//@line:269

		FT_Done_FreeType((FT_Library)library);
	

}

static inline jlong wrapped_Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_newMemoryFace
(JNIEnv* env, jclass clazz, jlong library, jbyteArray obj_data, jint dataSize, jint faceIndex, char* data) {

//@line:273

		FT_Face face = 0;
		FT_Error error = FT_New_Memory_Face((FT_Library)library, (const FT_Byte*)data, dataSize, faceIndex, &face);
		if(error) return 0;
		else return (jlong)face; 
	
}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_newMemoryFace(JNIEnv* env, jclass clazz, jlong library, jbyteArray obj_data, jint dataSize, jint faceIndex) {
	char* data = (char*)env->GetPrimitiveArrayCritical(obj_data, 0);

	jlong JNI_returnValue = wrapped_Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_newMemoryFace(env, clazz, library, obj_data, dataSize, faceIndex, data);

	env->ReleasePrimitiveArrayCritical(obj_data, data, 0);

	return JNI_returnValue;
}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_doneFace(JNIEnv* env, jclass clazz, jlong face) {


//@line:280

		FT_Done_Face((FT_Face)face);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_selectSize(JNIEnv* env, jclass clazz, jlong face, jint strike_index) {


//@line:284

		return !FT_Select_Size((FT_Face)face, strike_index);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_setCharSize(JNIEnv* env, jclass clazz, jlong face, jint charWidth, jint charHeight, jint horzResolution, jint vertResolution) {


//@line:288

		return !FT_Set_Char_Size((FT_Face)face, charWidth, charHeight, horzResolution, vertResolution);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_setPixelSizes(JNIEnv* env, jclass clazz, jlong face, jint pixelWidth, jint pixelHeight) {


//@line:292

		return !FT_Set_Pixel_Sizes((FT_Face)face, pixelWidth, pixelHeight);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_loadGlyph(JNIEnv* env, jclass clazz, jlong face, jint glyphIndex, jint loadFlags) {


//@line:296

		return !FT_Load_Glyph((FT_Face)face, glyphIndex, loadFlags);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_loadChar(JNIEnv* env, jclass clazz, jlong face, jint charCode, jint loadFlags) {


//@line:300

		return !FT_Load_Char((FT_Face)face, charCode, loadFlags);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_setTransform(JNIEnv* env, jclass clazz, jlong face, jint a, jint b, jint c, jint d, jint deltaX, jint deltaY) {


//@line:304

	
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_renderGlyph(JNIEnv* env, jclass clazz, jlong slot, jint renderMode) {


//@line:308

		return !FT_Render_Glyph((FT_GlyphSlot)slot, (FT_Render_Mode)renderMode);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_hasKerning(JNIEnv* env, jclass clazz, jlong face) {


//@line:312

   	return FT_HAS_KERNING(((FT_Face)face));
   

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_getKerning(JNIEnv* env, jclass clazz, jlong face, jint leftGlyph, jint rightGlyph, jint kernMode) {


//@line:316

   	FT_Vector kerning;
   	FT_Error error = FT_Get_Kerning((FT_Face)face, leftGlyph, rightGlyph, kernMode, &kerning);
   	if(error) return 0;
   	return kerning.y;
   

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_getCharIndex(JNIEnv* env, jclass clazz, jlong face, jint charCode) {


//@line:323

   	return FT_Get_Char_Index((FT_Face)face, charCode);
   

}

