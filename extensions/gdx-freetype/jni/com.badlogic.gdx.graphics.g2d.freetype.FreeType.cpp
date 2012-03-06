#include <com.badlogic.gdx.graphics.g2d.freetype.FreeType.h>

//@line:14

	#include <ft2build.h>
	#include FT_FREETYPE_H
	 JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getFaceFlags(JNIEnv* env, jclass clazz, jlong face) {


//@line:42

			return ((FT_Face)face)->face_flags;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getStyleFlags(JNIEnv* env, jclass clazz, jlong face) {


//@line:50

			return ((FT_Face)face)->style_flags;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getNumGlyphs(JNIEnv* env, jclass clazz, jlong face) {


//@line:58

			return ((FT_Face)face)->num_glyphs;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getAscender(JNIEnv* env, jclass clazz, jlong face) {


//@line:66

			return ((FT_Face)face)->ascender;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getDescender(JNIEnv* env, jclass clazz, jlong face) {


//@line:74

			return ((FT_Face)face)->descender;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getHeight(JNIEnv* env, jclass clazz, jlong face) {


//@line:82

			return ((FT_Face)face)->height;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getMaxAdvanceWidth(JNIEnv* env, jclass clazz, jlong face) {


//@line:90

			return ((FT_Face)face)->max_advance_width;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getMaxAdvanceHeight(JNIEnv* env, jclass clazz, jlong face) {


//@line:98

			return ((FT_Face)face)->max_advance_height;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getUnderlinePosition(JNIEnv* env, jclass clazz, jlong face) {


//@line:106

			return ((FT_Face)face)->underline_position;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getUnderlineThickness(JNIEnv* env, jclass clazz, jlong face) {


//@line:114

			return ((FT_Face)face)->underline_thickness;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getGlyph(JNIEnv* env, jclass clazz, jlong face) {


//@line:122

			return (jlong)((FT_Face)face)->glyph;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getSize(JNIEnv* env, jclass clazz, jlong face) {


//@line:130

			return (jlong)((FT_Face)face)->size;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Size_getMetrics(JNIEnv* env, jclass clazz, jlong address) {


//@line:144

			return (jlong)&((FT_Size)address)->metrics;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024SizeMetrics_getXppem(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:158

			return ((FT_Size_Metrics*)metrics)->x_ppem;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024SizeMetrics_getYppem(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:166

			return ((FT_Size_Metrics*)metrics)->y_ppem;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024SizeMetrics_getXscale(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:174

			return ((FT_Size_Metrics*)metrics)->x_scale;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024SizeMetrics_getYscale(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:182

			return ((FT_Size_Metrics*)metrics)->x_scale;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024SizeMetrics_getAscender(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:190

			return ((FT_Size_Metrics*)metrics)->ascender;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024SizeMetrics_getDescender(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:198

			return ((FT_Size_Metrics*)metrics)->descender;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024SizeMetrics_getHeight(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:206

			return ((FT_Size_Metrics*)metrics)->height;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024SizeMetrics_getMaxAdvance(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:214

			return ((FT_Size_Metrics*)metrics)->max_advance;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getMetrics(JNIEnv* env, jclass clazz, jlong slot) {


//@line:228

			return (jlong)&((FT_GlyphSlot)slot)->metrics;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getLinearHoriAdvance(JNIEnv* env, jclass clazz, jlong slot) {


//@line:236

			return ((FT_GlyphSlot)slot)->linearHoriAdvance;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getLinearVertAdvance(JNIEnv* env, jclass clazz, jlong slot) {


//@line:244

			return ((FT_GlyphSlot)slot)->linearVertAdvance;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getAdvanceX(JNIEnv* env, jclass clazz, jlong slot) {


//@line:252

			return ((FT_GlyphSlot)slot)->advance.x;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getAdvanceY(JNIEnv* env, jclass clazz, jlong slot) {


//@line:260

			return ((FT_GlyphSlot)slot)->advance.y;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getFormat(JNIEnv* env, jclass clazz, jlong slot) {


//@line:268

			return ((FT_GlyphSlot)slot)->format;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getBitmap(JNIEnv* env, jclass clazz, jlong slot) {


//@line:276

			FT_GlyphSlot glyph = ((FT_GlyphSlot)slot);
			return (jlong)&(glyph->bitmap);
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getBitmapLeft(JNIEnv* env, jclass clazz, jlong slot) {


//@line:285

			return ((FT_GlyphSlot)slot)->bitmap_left;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getBitmapTop(JNIEnv* env, jclass clazz, jlong slot) {


//@line:293

			return ((FT_GlyphSlot)slot)->bitmap_top;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getRows(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:307

			return ((FT_Bitmap*)bitmap)->rows;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getWidth(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:315

			return ((FT_Bitmap*)bitmap)->width;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getPitch(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:323

			return ((FT_Bitmap*)bitmap)->pitch;
		

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_convert(JNIEnv* env, jclass clazz, jobject obj_src, jobject obj_dst, jint numPixels) {
	char* src = (char*)env->GetDirectBufferAddress(obj_src);
	char* dst = (char*)env->GetDirectBufferAddress(obj_dst);


//@line:340

			unsigned int* dest = (unsigned int*)dst;
			for(int i = 0; i <  numPixels; i++) {
				unsigned int pixel = ((src[i] << 16) | 0xff0000) | ((src[i] << 8) | 0xff00) | (src[i] | 0xff);
				dest[i] = pixel;
			}
		

}

JNIEXPORT jobject JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getBuffer(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:348

			FT_Bitmap* bmp = (FT_Bitmap*)bitmap;
			return env->NewDirectByteBuffer((void*)bmp->buffer, bmp->rows * abs(bmp->pitch));
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getNumGray(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:357

			return ((FT_Bitmap*)bitmap)->num_grays;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getPixelMode(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:365

			return ((FT_Bitmap*)bitmap)->pixel_mode;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getWidth(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:379

			return ((FT_Glyph_Metrics*)metrics)->width;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getHeight(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:387

			return ((FT_Glyph_Metrics*)metrics)->height;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getHoriBearingX(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:395

			return ((FT_Glyph_Metrics*)metrics)->horiBearingX;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getHoriBearingY(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:403

			return ((FT_Glyph_Metrics*)metrics)->horiBearingY;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getHoriAdvance(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:411

			return ((FT_Glyph_Metrics*)metrics)->horiAdvance;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getVertBearingX(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:419

			return ((FT_Glyph_Metrics*)metrics)->vertBearingX;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getVertBearingY(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:427

			return ((FT_Glyph_Metrics*)metrics)->vertBearingY;
		 

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getVertAdvance(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:435

			return ((FT_Glyph_Metrics*)metrics)->vertAdvance;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_initFreeTypeJni(JNIEnv* env, jclass clazz) {


//@line:519

		FT_Library library = 0;
		FT_Error error = FT_Init_FreeType(&library);
		if(error) return 0;
		else return (jlong)library;
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_doneFreeType(JNIEnv* env, jclass clazz, jlong library) {


//@line:530

		FT_Done_FreeType((FT_Library)library);
	

}

static inline jlong wrapped_Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_newMemoryFace
(JNIEnv* env, jclass clazz, jlong library, jbyteArray obj_data, jint dataSize, jint faceIndex, char* data) {

//@line:545

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


//@line:556

		FT_Done_Face((FT_Face)face);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_selectSize(JNIEnv* env, jclass clazz, jlong face, jint strike_index) {


//@line:564

		return !FT_Select_Size((FT_Face)face, strike_index);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_setCharSize(JNIEnv* env, jclass clazz, jlong face, jint charWidth, jint charHeight, jint horzResolution, jint vertResolution) {


//@line:572

		return !FT_Set_Char_Size((FT_Face)face, charWidth, charHeight, horzResolution, vertResolution);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_setPixelSizes(JNIEnv* env, jclass clazz, jlong face, jint pixelWidth, jint pixelHeight) {


//@line:580

		return !FT_Set_Pixel_Sizes((FT_Face)face, pixelWidth, pixelHeight);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_loadGlyph(JNIEnv* env, jclass clazz, jlong face, jint glyphIndex, jint loadFlags) {


//@line:588

		return !FT_Load_Glyph((FT_Face)face, glyphIndex, loadFlags);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_loadChar(JNIEnv* env, jclass clazz, jlong face, jint charCode, jint loadFlags) {


//@line:596

		return !FT_Load_Char((FT_Face)face, charCode, loadFlags);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_renderGlyph(JNIEnv* env, jclass clazz, jlong slot, jint renderMode) {


//@line:604

		return !FT_Render_Glyph((FT_GlyphSlot)slot, (FT_Render_Mode)renderMode);
	

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_hasKerning(JNIEnv* env, jclass clazz, jlong face) {


//@line:612

   	return FT_HAS_KERNING(((FT_Face)face));
   

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_getKerning(JNIEnv* env, jclass clazz, jlong face, jint leftGlyph, jint rightGlyph, jint kernMode) {


//@line:620

   	FT_Vector kerning;
   	FT_Error error = FT_Get_Kerning((FT_Face)face, leftGlyph, rightGlyph, kernMode, &kerning);
   	if(error) return 0;
   	return kerning.x;
   

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_getCharIndex(JNIEnv* env, jclass clazz, jlong face, jint charCode) {


//@line:631

   	return FT_Get_Char_Index((FT_Face)face, charCode);
   

}

