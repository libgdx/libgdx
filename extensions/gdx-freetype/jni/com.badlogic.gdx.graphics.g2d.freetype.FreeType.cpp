#include <com.badlogic.gdx.graphics.g2d.freetype.FreeType.h>

//@line:7

	#include <ft2build.h>
	#include FT_FREETYPE_H
	 JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getFaceFlags(JNIEnv* env, jclass clazz, jlong face) {


//@line:50

			return ((FT_Face)face)->face_flags;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getStyleFlags(JNIEnv* env, jclass clazz, jlong face) {


//@line:54

			return ((FT_Face)face)->style_flags;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getNumGlyphs(JNIEnv* env, jclass clazz, jlong face) {


//@line:58

			return ((FT_Face)face)->num_glyphs;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getAscender(JNIEnv* env, jclass clazz, jlong face) {


//@line:62

			return ((FT_Face)face)->ascender;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getDescender(JNIEnv* env, jclass clazz, jlong face) {


//@line:66

			return ((FT_Face)face)->descender;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getHeight(JNIEnv* env, jclass clazz, jlong face) {


//@line:70

			return ((FT_Face)face)->height;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getMaxAdvanceWidth(JNIEnv* env, jclass clazz, jlong face) {


//@line:74

			return ((FT_Face)face)->max_advance_width;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getMaxAdvanceHeight(JNIEnv* env, jclass clazz, jlong face) {


//@line:78

			return ((FT_Face)face)->max_advance_height;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getUnderlinePosition(JNIEnv* env, jclass clazz, jlong face) {


//@line:82

			return ((FT_Face)face)->underline_position;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getUnderlineThickness(JNIEnv* env, jclass clazz, jlong face) {


//@line:86

			return ((FT_Face)face)->underline_thickness;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getGlyph(JNIEnv* env, jclass clazz, jlong face) {


//@line:90

			return (jlong)((FT_Face)face)->glyph;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getSize(JNIEnv* env, jclass clazz, jlong face) {


//@line:94

			return (jlong)((FT_Face)face)->size;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getRows(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:112

			return ((FT_Bitmap*)bitmap)->rows;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getWidth(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:116

			return ((FT_Bitmap*)bitmap)->width;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getPitch(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:120

			return ((FT_Bitmap*)bitmap)->pitch;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getBuffer(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:124

			return (jlong)((FT_Bitmap*)bitmap)->buffer;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getNumGray(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:128

			return ((FT_Bitmap*)bitmap)->num_grays;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getPixelMode(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:132

			return ((FT_Bitmap*)bitmap)->pixel_mode;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getMetrics(JNIEnv* env, jclass clazz, jlong slot) {


//@line:142

			return (jlong)&((FT_GlyphSlot)slot)->metrics;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getLinearHoriAdvance(JNIEnv* env, jclass clazz, jlong slot) {


//@line:146

			return ((FT_GlyphSlot)slot)->linearHoriAdvance;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getLinearVertAdvance(JNIEnv* env, jclass clazz, jlong slot) {


//@line:150

			return ((FT_GlyphSlot)slot)->linearVertAdvance;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getAdvanceX(JNIEnv* env, jclass clazz, jlong slot) {


//@line:154

			return ((FT_GlyphSlot)slot)->advance.x;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getAdvanceY(JNIEnv* env, jclass clazz, jlong slot) {


//@line:158

			return ((FT_GlyphSlot)slot)->advance.y;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getFormat(JNIEnv* env, jclass clazz, jlong slot) {


//@line:162

			return ((FT_GlyphSlot)slot)->format;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getBitmap(JNIEnv* env, jclass clazz, jlong slot) {


//@line:166

			return (jlong)&((FT_GlyphSlot)slot)->bitmap;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getBitmapLeft(JNIEnv* env, jclass clazz, jlong slot) {


//@line:170

			return ((FT_GlyphSlot)slot)->bitmap_left;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getBitmapTop(JNIEnv* env, jclass clazz, jlong slot) {


//@line:174

			return ((FT_GlyphSlot)slot)->bitmap_top;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_initFreeType(JNIEnv* env, jclass clazz) {


//@line:238

		FT_Library library = 0;
		FT_Error error = FT_Init_FreeType(&library);
		if(error) return 0;
		else return (jlong)library;
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_doneFreeType(JNIEnv* env, jclass clazz, jlong library) {


//@line:245

		FT_Done_FreeType((FT_Library)library);
	

}

static inline jlong wrapped_Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_newMemoryFace
(JNIEnv* env, jclass clazz, jlong library, jbyteArray obj_data, jint dataSize, jint faceIndex, char* data) {

//@line:255

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


//@line:262

		FT_Done_Face((FT_Face)face);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_selectSize(JNIEnv* env, jclass clazz, jlong face, jint strike_index) {


//@line:266

		return !FT_Select_Size((FT_Face)face, strike_index);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_setCharSize(JNIEnv* env, jclass clazz, jlong face, jint charWidth, jint charHeight, jint horzResolution, jint vertResolution) {


//@line:270

		return !FT_Set_Char_Size((FT_Face)face, charWidth, charHeight, horzResolution, vertResolution);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_setPixelSizes(JNIEnv* env, jclass clazz, jlong face, jint pixelWidth, jint pixelHeight) {


//@line:274

		return !FT_Set_Pixel_Sizes((FT_Face)face, pixelWidth, pixelHeight);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_loadGlyph(JNIEnv* env, jclass clazz, jlong face, jint glyphIndex, jint loadFlags) {


//@line:294

		return !FT_Load_Glyph((FT_Face)face, glyphIndex, loadFlags);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_loadChar(JNIEnv* env, jclass clazz, jlong face, jint charCode, jint loadFlags) {


//@line:298

		return !FT_Load_Char((FT_Face)face, charCode, loadFlags);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_setTransform(JNIEnv* env, jclass clazz, jlong face, jint a, jint b, jint c, jint d, jint deltaX, jint deltaY) {


//@line:302

	
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_renderGlyph(JNIEnv* env, jclass clazz, jlong slot, jint renderMode) {


//@line:306

		return !FT_Render_Glyph((FT_GlyphSlot)slot, (FT_Render_Mode)renderMode);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_hasKerning(JNIEnv* env, jclass clazz, jlong face) {


//@line:321

   	return FT_HAS_KERNING(((FT_Face)face));
   

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_getKerning(JNIEnv* env, jclass clazz, jlong face, jint leftGlyph, jint rightGlyph, jint kernMode) {


//@line:325

   	FT_Vector kerning;
   	FT_Error error = FT_Get_Kerning((FT_Face)face, leftGlyph, rightGlyph, kernMode, &kerning);
   	if(error) return 0;
   	return kerning.x;
   

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_getCharIndex(JNIEnv* env, jclass clazz, jlong face, jint charCode) {


//@line:332

   	return FT_Get_Char_Index((FT_Face)face, charCode);
   

}

