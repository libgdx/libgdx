
package com.badlogic.gdx.graphics.g2d.stbtt;

import java.nio.ByteBuffer;

public class StbTrueType {
	public static final int STBTT_VMOVE = 1;
	public static final int STBTT_VLINE = 2;
	public static final int STBTT_VCURVE = 3;

	public static class Vertex {
		int x, y, cx, cy;
		int type, padding;

		public Vertex (int x, int y, int cx, int cy, int type, int padding) {
			this.x = x;
			this.y = y;
			this.cx = cx;
			this.cy = cy;
			this.type = type;
			this.padding = padding;
		}
	}
	
	/*JNI
	#define STB_TRUETYPE_IMPLEMENTATION
	#include <stb_truetype.h>
	 */

	static native long initFont (byte[] data, int offset); /*
		stbtt_fontinfo* info = (stbtt_fontinfo*)malloc(sizeof(stbtt_fontinfo));
		int result = stbtt_InitFont(info, (const unsigned char*)data, offset);
		if(!result) {
			free(info);
			return 0;
		} else {
			return (jlong)info;
		}
	*/

	static native void disposeFont (long info); /*
		free((void*)info);
	*/

	static native int findGlyphIndex (long info, int unicodeCodepoint); /*
		return stbtt_FindGlyphIndex((stbtt_fontinfo*)info, unicodeCodepoint);
	*/

	static native float scaleForPixelHeight (long info, float pixels); /*
		return stbtt_ScaleForPixelHeight((stbtt_fontinfo*)info, pixels);
	*/

	static native void getFontVMetrics (long info, int[] metrics); /*
		int ascent = 0;
		int descent = 0;
		int lineGap = 0;
		stbtt_GetFontVMetrics((stbtt_fontinfo*)info, &ascent, &descent, &lineGap);
		metrics[0] = ascent;
		metrics[1] = descent;
		metrics[2] = lineGap;
	*/

	static native void getCodepointHMetrics (long info, int codePoint, int[] metrics); /*
		int advanceWidth = 0;
		int leftSideBearing = 0;
		stbtt_GetCodepointHMetrics((stbtt_fontinfo*)info, codePoint, &advanceWidth, &leftSideBearing);
		metrics[0] = advanceWidth;
		metrics[1] = leftSideBearing;
	*/

	static native int getCodepointKernAdvance (long info, int char1, int char2); /*
		return stbtt_GetCodepointKernAdvance((stbtt_fontinfo*)info, char1, char2);	
	*/

	static native int getCodePointBox (long info, int codePoint, int[] box); /*
		int x0, y0, x1, y1;
		x0 = y0 = x1 = y1 = 0;
		int result = stbtt_GetCodepointBox((stbtt_fontinfo*)info, codePoint, &x0, &y0, &x1, &y1);	
		box[0] = x0;
		box[1] = y0;
		box[2] = x1;
		box[3] = y1;
		%jnigen-cleanup%
		return result;
	*/

	static native void getGlyphHMetrics (long info, int glyphIndex, int[] metrics); /*
		int advanceWidth = 0;
		int leftSideBearing = 0;
		stbtt_GetGlyphHMetrics((stbtt_fontinfo*)info, glyphIndex, &advanceWidth, &leftSideBearing);

		metrics[0] = advanceWidth;
		metrics[1] = leftSideBearing;
	*/

	static native int getGlyphKernAdvance (long info, int glyph1, int glyph2); /*
		return stbtt_GetGlyphKernAdvance((stbtt_fontinfo*)info, glyph1, glyph2);
	*/

	static native int getGlyphBox (long info, int glyphIndex, int[] box); /*
		int x0, y0, x1, y1;
		x0 = y0 = x1 = y1 = 0;
		int result = stbtt_GetGlyphBox((stbtt_fontinfo*)info, glyphIndex, &x0, &y0, &x1, &y1);
		box[0] = x0;
		box[1] = y0;
		box[2] = x1;
		box[3] = y1;
		%jnigen-cleanup%
		return result;
	*/

	static native int getCodePointShape (long info, int codePoint, long[] vertices); /*
		stbtt_vertex* verticesAddr = 0;
		int result = stbtt_GetCodepointShape((stbtt_fontinfo*)info, codePoint, &verticesAddr);
		vertices[0] = (jlong)verticesAddr;
		%jnigen-cleanup%
		return result;
	*/

	static native int getGlyphShape (long info, int glyphIndex, long[] vertices); /*
		stbtt_vertex* verticesAddr = 0;
		int result = stbtt_GetGlyphShape((stbtt_fontinfo*)info, glyphIndex, &verticesAddr);
		vertices[0] = (jlong)verticesAddr;
		%jnigen-cleanup%
		return result;
	*/

	static native void getShapeVertex (long vertices, int index, int[] vertex); /*
		stbtt_vertex* verts = (stbtt_vertex*)vertices;
		vertex[0] = verts[index].x;
		vertex[1] = verts[index].y;
		vertex[2] = verts[index].cx;
		vertex[3] = verts[index].cy;
		vertex[4] = verts[index].type;
		vertex[5] = verts[index].padding;
	*/

	static native void freeShape (long info, long vertices); /*
		stbtt_FreeShape((stbtt_fontinfo*)info, (stbtt_vertex*)vertices);
	*/

	static native void makeCodepointBitmap (long info, ByteBuffer bitmap, int bitmapWidth, int bitmapHeight, int bitmapStride,
		float scaleX, float scaleY, int codePoint); /*
		stbtt_MakeCodepointBitmap((stbtt_fontinfo*)info, (unsigned char*)bitmap, bitmapWidth, bitmapHeight, bitmapStride, scaleX, scaleY, codePoint);
	*/

	static native void getCodepointBitmapBox (long info, int codePoint, float scaleX, float scaleY, int[] box); /*
		int x0, y0, x1, y1;
		x0 = y0 = x1 = y1 = 0;
		stbtt_GetCodepointBitmapBox((stbtt_fontinfo*)info, codePoint, scaleX, scaleY, &x0, &y0, &x1, &y1);
		box[0] = x0;
		box[1] = y0;
		box[2] = x1;
		box[3] = y1;
	*/

	static native void makeGlyphBitmap (long info, ByteBuffer bitmap, int bitmapWidth, int bitmapHeight, int bitmapStride,
		float scaleX, float scaleY, float shiftX, float shiftY, int glyph); /*
		stbtt_MakeGlyphBitmap((stbtt_fontinfo*)info, (unsigned char*)bitmap, bitmapWidth, bitmapHeight, bitmapStride, scaleX, scaleY, shiftX, shiftY, glyph);
	*/

	static native void getGlyphBitmapBox (long info, int glyph, float scaleX, float scaleY, float shiftX, float shiftY, int[] box); /*
		int x0, y0, x1, y1;
		x0 = y0 = x1 = y1 = 0;
		stbtt_GetGlyphBitmapBox((stbtt_fontinfo*)info, glyph, scaleX, scaleY, shiftX, shiftY, &x0, &y0, &x1, &y1);
		box[0] = x0;
		box[1] = y0;
		box[2] = x1;
		box[3] = y1;
	*/
}
