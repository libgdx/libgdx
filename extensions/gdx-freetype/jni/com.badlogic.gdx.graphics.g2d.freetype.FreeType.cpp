#include <com.badlogic.gdx.graphics.g2d.freetype.FreeType.h>

//@line:38

	#include <ft2build.h>
	#include FT_FREETYPE_H
	#include FT_STROKER_H
	
	static jint lastError = 0;	
	 JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_getLastErrorCode(JNIEnv* env, jclass clazz) {


//@line:50

		return lastError;
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Library_doneFreeType(JNIEnv* env, jclass clazz, jlong library) {


//@line:78

			FT_Done_FreeType((FT_Library)library);
		

}

static inline jlong wrapped_Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Library_newMemoryFace
(JNIEnv* env, jclass clazz, jlong library, jobject obj_data, jint dataSize, jint faceIndex, char* data) {

//@line:131

			FT_Face face = 0;
			FT_Error error = FT_New_Memory_Face((FT_Library)library, (const FT_Byte*)data, dataSize, faceIndex, &face);
			if(error) {
				lastError = error;
				return 0;
			}
			else return (jlong)face;
		
}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Library_newMemoryFace(JNIEnv* env, jclass clazz, jlong library, jobject obj_data, jint dataSize, jint faceIndex) {
	char* data = (char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);

	jlong JNI_returnValue = wrapped_Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Library_newMemoryFace(env, clazz, library, obj_data, dataSize, faceIndex, data);


	return JNI_returnValue;
}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Library_strokerNew(JNIEnv* env, jclass clazz, jlong library) {


//@line:147

			FT_Stroker stroker;
			FT_Error error = FT_Stroker_New((FT_Library)library, &stroker);
			if(error) {
				lastError = error;
				return 0;
			}
			else return (jlong)stroker;
		

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_doneFace(JNIEnv* env, jclass clazz, jlong face) {


//@line:177

			FT_Done_Face((FT_Face)face);
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getFaceFlags(JNIEnv* env, jclass clazz, jlong face) {


//@line:185

			return ((FT_Face)face)->face_flags;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getStyleFlags(JNIEnv* env, jclass clazz, jlong face) {


//@line:193

			return ((FT_Face)face)->style_flags;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getNumGlyphs(JNIEnv* env, jclass clazz, jlong face) {


//@line:201

			return ((FT_Face)face)->num_glyphs;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getAscender(JNIEnv* env, jclass clazz, jlong face) {


//@line:209

			return ((FT_Face)face)->ascender;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getDescender(JNIEnv* env, jclass clazz, jlong face) {


//@line:217

			return ((FT_Face)face)->descender;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getHeight(JNIEnv* env, jclass clazz, jlong face) {


//@line:225

			return ((FT_Face)face)->height;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getMaxAdvanceWidth(JNIEnv* env, jclass clazz, jlong face) {


//@line:233

			return ((FT_Face)face)->max_advance_width;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getMaxAdvanceHeight(JNIEnv* env, jclass clazz, jlong face) {


//@line:241

			return ((FT_Face)face)->max_advance_height;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getUnderlinePosition(JNIEnv* env, jclass clazz, jlong face) {


//@line:249

			return ((FT_Face)face)->underline_position;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getUnderlineThickness(JNIEnv* env, jclass clazz, jlong face) {


//@line:257

			return ((FT_Face)face)->underline_thickness;
		

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_selectSize(JNIEnv* env, jclass clazz, jlong face, jint strike_index) {


//@line:265

			return !FT_Select_Size((FT_Face)face, strike_index);
		

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_setCharSize(JNIEnv* env, jclass clazz, jlong face, jint charWidth, jint charHeight, jint horzResolution, jint vertResolution) {


//@line:273

			return !FT_Set_Char_Size((FT_Face)face, charWidth, charHeight, horzResolution, vertResolution);
		

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_setPixelSizes(JNIEnv* env, jclass clazz, jlong face, jint pixelWidth, jint pixelHeight) {


//@line:281

			return !FT_Set_Pixel_Sizes((FT_Face)face, pixelWidth, pixelHeight);
		

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_loadGlyph(JNIEnv* env, jclass clazz, jlong face, jint glyphIndex, jint loadFlags) {


//@line:289

			return !FT_Load_Glyph((FT_Face)face, glyphIndex, loadFlags);
		

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_loadChar(JNIEnv* env, jclass clazz, jlong face, jint charCode, jint loadFlags) {


//@line:297

			return !FT_Load_Char((FT_Face)face, charCode, loadFlags);
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getGlyph(JNIEnv* env, jclass clazz, jlong face) {


//@line:305

			return (jlong)((FT_Face)face)->glyph;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getSize(JNIEnv* env, jclass clazz, jlong face) {


//@line:313

			return (jlong)((FT_Face)face)->size;
		

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_hasKerning(JNIEnv* env, jclass clazz, jlong face) {


//@line:321

			return FT_HAS_KERNING(((FT_Face)face));
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getKerning(JNIEnv* env, jclass clazz, jlong face, jint leftGlyph, jint rightGlyph, jint kernMode) {


//@line:329

			FT_Vector kerning;
			FT_Error error = FT_Get_Kerning((FT_Face)face, leftGlyph, rightGlyph, kernMode, &kerning);
			if(error) return 0;
			return kerning.x;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Face_getCharIndex(JNIEnv* env, jclass clazz, jlong face, jint charCode) {


//@line:340

			return FT_Get_Char_Index((FT_Face)face, charCode);
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Size_getMetrics(JNIEnv* env, jclass clazz, jlong address) {


//@line:355

			return (jlong)&((FT_Size)address)->metrics;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024SizeMetrics_getXppem(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:369

			return ((FT_Size_Metrics*)metrics)->x_ppem;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024SizeMetrics_getYppem(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:377

			return ((FT_Size_Metrics*)metrics)->y_ppem;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024SizeMetrics_getXscale(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:385

			return ((FT_Size_Metrics*)metrics)->x_scale;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024SizeMetrics_getYscale(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:393

			return ((FT_Size_Metrics*)metrics)->x_scale;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024SizeMetrics_getAscender(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:401

			return ((FT_Size_Metrics*)metrics)->ascender;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024SizeMetrics_getDescender(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:409

			return ((FT_Size_Metrics*)metrics)->descender;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024SizeMetrics_getHeight(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:417

			return ((FT_Size_Metrics*)metrics)->height;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024SizeMetrics_getMaxAdvance(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:425

			return ((FT_Size_Metrics*)metrics)->max_advance;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getMetrics(JNIEnv* env, jclass clazz, jlong slot) {


//@line:439

			return (jlong)&((FT_GlyphSlot)slot)->metrics;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getLinearHoriAdvance(JNIEnv* env, jclass clazz, jlong slot) {


//@line:447

			return ((FT_GlyphSlot)slot)->linearHoriAdvance;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getLinearVertAdvance(JNIEnv* env, jclass clazz, jlong slot) {


//@line:455

			return ((FT_GlyphSlot)slot)->linearVertAdvance;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getAdvanceX(JNIEnv* env, jclass clazz, jlong slot) {


//@line:463

			return ((FT_GlyphSlot)slot)->advance.x;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getAdvanceY(JNIEnv* env, jclass clazz, jlong slot) {


//@line:471

			return ((FT_GlyphSlot)slot)->advance.y;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getFormat(JNIEnv* env, jclass clazz, jlong slot) {


//@line:479

			return ((FT_GlyphSlot)slot)->format;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getBitmap(JNIEnv* env, jclass clazz, jlong slot) {


//@line:487

			FT_GlyphSlot glyph = ((FT_GlyphSlot)slot);
			return (jlong)&(glyph->bitmap);
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getBitmapLeft(JNIEnv* env, jclass clazz, jlong slot) {


//@line:496

			return ((FT_GlyphSlot)slot)->bitmap_left;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getBitmapTop(JNIEnv* env, jclass clazz, jlong slot) {


//@line:504

			return ((FT_GlyphSlot)slot)->bitmap_top;
		

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_renderGlyph(JNIEnv* env, jclass clazz, jlong slot, jint renderMode) {


//@line:512

			return !FT_Render_Glyph((FT_GlyphSlot)slot, (FT_Render_Mode)renderMode);
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphSlot_getGlyph(JNIEnv* env, jclass clazz, jlong glyphSlot) {


//@line:522

			FT_Glyph glyph;
			FT_Error error = FT_Get_Glyph((FT_GlyphSlot)glyphSlot, &glyph);
			if(error) {
				lastError = error;
				return 0;
			}
			else return (jlong)glyph;
		

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Glyph_done(JNIEnv* env, jclass clazz, jlong glyph) {


//@line:545

			FT_Done_Glyph((FT_Glyph)glyph);
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Glyph_strokeBorder(JNIEnv* env, jclass clazz, jlong glyph, jlong stroker, jboolean inside) {


//@line:553

			FT_Glyph border_glyph = (FT_Glyph)glyph;
			FT_Glyph_StrokeBorder(&border_glyph, (FT_Stroker)stroker, inside, 1);
			return (jlong)border_glyph;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Glyph_toBitmap(JNIEnv* env, jclass clazz, jlong glyph, jint renderMode) {


//@line:566

			FT_Glyph bitmap = (FT_Glyph)glyph;
			FT_Error error = FT_Glyph_To_Bitmap(&bitmap, (FT_Render_Mode)renderMode, NULL, 1);
			if(error) {
				lastError = error;
				return 0;
			}
			return (jlong)bitmap;
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Glyph_getBitmap(JNIEnv* env, jclass clazz, jlong glyph) {


//@line:583

			FT_BitmapGlyph glyph_bitmap = ((FT_BitmapGlyph)glyph);
			return (jlong)&(glyph_bitmap->bitmap);
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Glyph_getLeft(JNIEnv* env, jclass clazz, jlong glyph) {


//@line:595

			FT_BitmapGlyph glyph_bitmap = ((FT_BitmapGlyph)glyph);
			return glyph_bitmap->left;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Glyph_getTop(JNIEnv* env, jclass clazz, jlong glyph) {


//@line:607

			FT_BitmapGlyph glyph_bitmap = ((FT_BitmapGlyph)glyph);
			return glyph_bitmap->top;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getRows(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:623

			return ((FT_Bitmap*)bitmap)->rows;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getWidth(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:631

			return ((FT_Bitmap*)bitmap)->width;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getPitch(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:639

			return ((FT_Bitmap*)bitmap)->pitch;
		

}

JNIEXPORT jobject JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getBuffer(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:654

			FT_Bitmap* bmp = (FT_Bitmap*)bitmap;
			return env->NewDirectByteBuffer((void*)bmp->buffer, bmp->rows * abs(bmp->pitch));
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getNumGray(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:729

			return ((FT_Bitmap*)bitmap)->num_grays;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Bitmap_getPixelMode(JNIEnv* env, jclass clazz, jlong bitmap) {


//@line:737

			return ((FT_Bitmap*)bitmap)->pixel_mode;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getWidth(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:751

			return ((FT_Glyph_Metrics*)metrics)->width;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getHeight(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:759

			return ((FT_Glyph_Metrics*)metrics)->height;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getHoriBearingX(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:767

			return ((FT_Glyph_Metrics*)metrics)->horiBearingX;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getHoriBearingY(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:775

			return ((FT_Glyph_Metrics*)metrics)->horiBearingY;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getHoriAdvance(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:783

			return ((FT_Glyph_Metrics*)metrics)->horiAdvance;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getVertBearingX(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:791

			return ((FT_Glyph_Metrics*)metrics)->vertBearingX;
		

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getVertBearingY(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:799

			return ((FT_Glyph_Metrics*)metrics)->vertBearingY;
		 

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024GlyphMetrics_getVertAdvance(JNIEnv* env, jclass clazz, jlong metrics) {


//@line:807

			return ((FT_Glyph_Metrics*)metrics)->vertAdvance;
		

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Stroker_set(JNIEnv* env, jclass clazz, jlong stroker, jint radius, jint lineCap, jint lineJoin, jint miterLimit) {


//@line:821

			FT_Stroker_Set((FT_Stroker)stroker, radius, (FT_Stroker_LineCap)lineCap, (FT_Stroker_LineJoin)lineJoin, miterLimit);
		

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_00024Stroker_done(JNIEnv* env, jclass clazz, jlong stroker) {


//@line:830

			FT_Stroker_Done((FT_Stroker)stroker);
		

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_graphics_g2d_freetype_FreeType_initFreeTypeJni(JNIEnv* env, jclass clazz) {


//@line:932

		FT_Library library = 0;
		FT_Error error = FT_Init_FreeType(&library);
		if(error) {
			lastError = error;
			return 0;
		}
		else return (jlong)library;
	

}

