
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

	static native long initFont (byte[] data, int offset);

	static native void disposeFont (long info);

	static native int findGlyphIndex (long info, int unicodeCodepoint);

	static native float scaleForPixelHeight (long info, float pixels);

	static native void getFontVMetrics (long info, int[] metrics);

	static native void getCodepointHMetrics (long info, int codePoint, int[] metrics);

	static native int getCodepointKernAdvance (long info, int char1, int char2);

	static native int getCodePointBox (long info, int codePoint, int[] box);

	static native void getGlyphHMetrics (long info, int glyphIndex, int[] metrics);

	static native int getGlyphKernAdvance (long info, int glyph1, int glyph2);

	static native int getGlyphBox (long info, int glyphIndex, int[] box);

	static native int getCodePointShape (long info, int codePoint, long[] vertices);

	static native int getGlyphShape (long info, int codePoint, long[] vertices);

	static native void getShapeVertex (long vertices, int index, int[] vertex);

	static native void freeShape (long info, long vertices);

	static native void makeCodepointBitmap (long info, ByteBuffer bitmap, int bitmapWidth, int bitmapHeight, int bitmapStride,
		float scaleX, float scaleY, int codepoint);

	static native void getCodepointBitmapBox (long info, int codePoint, float scaleX, float scaleY, int[] box);

	static native void makeGlyphBitmap (long info, ByteBuffer bitmap, int bitmapWidth, int bitmapHeight, int bitmapStride,
		float scaleX, float scaleY, float shiftX, float shiftY, int glyph);

	static native void getGlyphBitmapBox (long info, int glyph, float scaleX, float scaleY, float shiftX, float shiftY, int[] box);
}
