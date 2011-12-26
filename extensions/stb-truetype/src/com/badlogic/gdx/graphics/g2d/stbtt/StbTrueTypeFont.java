
package com.badlogic.gdx.graphics.g2d.stbtt;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.stbtt.StbTrueType.Vertex;
import com.badlogic.gdx.jnigen.SharedLibraryLoader;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class StbTrueTypeFont implements Disposable {
	public static class FontVMetrics {
		public final int ascent;
		public final int descent;
		public final int lineGap;

		public FontVMetrics (int ascent, int descent, int lineGap) {
			this.ascent = ascent;
			this.descent = descent;
			this.lineGap = lineGap;
		}

		@Override
		public String toString () {
			return "FontVMetrics [ascent=" + ascent + ", descent=" + descent + ", lineGap=" + lineGap + "]";
		}
	}

	public static class HMetrics {
		public final int advance;
		public final int leftSideBearing;

		public HMetrics (int advance, int leftSideBearing) {
			this.advance = advance;
			this.leftSideBearing = leftSideBearing;
		}

		@Override
		public String toString () {
			return "HMetrics [advance=" + advance + ", leftSideBearing=" + leftSideBearing + "]";
		}
	}

	public static class Box {
		public int x0, y0, x1, y1;

		public Box (int x0, int y0, int x1, int y1) {
			this.x0 = x0;
			this.y0 = y0;
			this.x1 = x1;
			this.y1 = y1;
		}

		@Override
		public String toString () {
			return "Box [x0=" + x0 + ", y0=" + y0 + ", x1=" + x1 + ", y1=" + y1 + "]";
		}
	}

	public static class Bitmap implements Disposable {
		public final Pixmap pixmap;
		public final Box box;

		public Bitmap (Pixmap pixmap, Box box) {
			this.pixmap = pixmap;
			this.box = box;
		}

		@Override
		public void dispose () {
			pixmap.dispose();
		}

		@Override
		public String toString () {
			return "Bitmap [box=" + box + "]";
		}
	}

	final long addr;
	final int[] intArray = new int[8];
	final long[] longArray = new long[1];

	public StbTrueTypeFont (FileHandle file) {
		this(file.readBytes(), 0);
	}

	public StbTrueTypeFont (byte[] data, int offset) {
		new SharedLibraryLoader().load("stbtruetype");
		this.addr = StbTrueType.initFont(data, offset);
		if (this.addr == 0) throw new GdxRuntimeException("Couldn't load truetype font");
	}

	public void dispose () {
		StbTrueType.disposeFont(addr);
	}

	public int findGlyphIndex (int unicodeCodepoint) {
		return StbTrueType.findGlyphIndex(addr, unicodeCodepoint);
	}

	public float scaleForPixelHeight (float pixels) {
		return StbTrueType.scaleForPixelHeight(addr, pixels);
	}

	public FontVMetrics getFontVMetrics () {
		StbTrueType.getFontVMetrics(addr, intArray);
		return new FontVMetrics(intArray[0], intArray[1], intArray[2]);
	}

	public HMetrics getCodepointHMetrics (int codePoint) {
		StbTrueType.getCodepointHMetrics(addr, codePoint, intArray);
		return new HMetrics(intArray[0], intArray[1]);
	}

	public int getCodepointKernAdvance (int char1, int char2) {
		return StbTrueType.getCodepointKernAdvance(addr, char1, char2);
	}

	public Box getCodePointBox (int codePoint) {
		int glyphFound = StbTrueType.getCodePointBox(addr, codePoint, intArray);
		if (glyphFound == 0) return new Box(0, 0, 0, 0);
		return new Box(intArray[0], intArray[1], intArray[2], intArray[3]);
	}

	public HMetrics getGlyphHMetrics (int glyphIndex) {
		StbTrueType.getGlyphHMetrics(addr, glyphIndex, intArray);
		return new HMetrics(intArray[0], intArray[1]);
	}

	public int getGlyphKernAdvance (int glyph1, int glyph2) {
		return StbTrueType.getGlyphKernAdvance(addr, glyph1, glyph2);
	}

	public Box getGlyphBox (int glyphIndex) {
		int glyphFound = StbTrueType.getGlyphBox(addr, glyphIndex, intArray);
		if (glyphFound == 0) return new Box(0, 0, 0, 0);
		return new Box(intArray[0], intArray[1], intArray[2], intArray[3]);
	}

	public Vertex[] getCodePointShape (int codePoint) {
		int numVertices = StbTrueType.getCodePointShape(addr, codePoint, longArray);
		long verticesAddr = longArray[0];
		if (numVertices == 0) return new Vertex[0];

		Vertex[] vertices = new Vertex[numVertices];
		for (int i = 0; i < numVertices; i++) {
			StbTrueType.getShapeVertex(verticesAddr, i, intArray);
			vertices[i] = new Vertex(intArray[0], intArray[1], intArray[2], intArray[3], intArray[4], intArray[5]);
		}
		StbTrueType.freeShape(addr, verticesAddr);
		return vertices;
	}

	public Vertex[] getGlyphShape (int glyphIndex) {
		int numVertices = StbTrueType.getGlyphShape(addr, glyphIndex, longArray);
		long verticesAddr = longArray[0];
		if (numVertices == 0) return new Vertex[0];

		Vertex[] vertices = new Vertex[numVertices];
		for (int i = 0; i < numVertices; i++) {
			StbTrueType.getShapeVertex(verticesAddr, i, intArray);
			vertices[i] = new Vertex(intArray[0], intArray[1], intArray[2], intArray[3], intArray[4], intArray[5]);
		}
		StbTrueType.freeShape(addr, verticesAddr);
		return vertices;
	}

	public Bitmap makeCodepointBitmap (float scaleX, float scaleY, int codePoint) {
		StbTrueType.getCodepointBitmapBox(addr, codePoint, scaleX, scaleY, intArray);
		Box box = new Box(intArray[0], intArray[1], intArray[2], intArray[3]);
		if (box.x0 == 0 && box.y0 == 0 && box.x1 == 0 && box.y1 == 0) return null;

		Pixmap pixmap = new Pixmap(box.x1 - box.x0, box.y1 - box.y0, Format.Alpha);
		StbTrueType.makeCodepointBitmap(addr, pixmap.getPixels(), pixmap.getWidth(), pixmap.getHeight(), pixmap.getWidth(), scaleX,
			scaleY, codePoint);
		Pixmap pixmapRGBA = new Pixmap(box.x1 - box.x0, box.y1 - box.y0, Format.RGBA4444);
		pixmapRGBA.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
		pixmap.dispose();
		return new Bitmap(pixmapRGBA, box);
	}

	public Bitmap makeGlyphBitmap (float scaleX, float scaleY, float shiftX, float shiftY, int glyph) {
		StbTrueType.getGlyphBitmapBox(addr, glyph, scaleX, scaleY, shiftX, shiftY, intArray);
		Box box = new Box(intArray[0], intArray[1], intArray[2], intArray[3]);
		if (box.x0 == 0 && box.y0 == 0 && box.x1 == 0 && box.y1 == 0) return null;

		Pixmap pixmap = new Pixmap(box.x1 - box.x0, box.y1 - box.y0, Format.Alpha);
		StbTrueType.makeGlyphBitmap(addr, pixmap.getPixels(), pixmap.getWidth(), pixmap.getHeight(), pixmap.getWidth(), scaleX,
			scaleY, shiftX, shiftY, glyph);
		Pixmap pixmapRGBA = new Pixmap(box.x1 - box.x0, box.y1 - box.y0, Format.RGBA4444);
		pixmapRGBA.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
		pixmap.dispose();
		return new Bitmap(pixmapRGBA, box);
	}
	
	public static void main(String[] args) {
		new StbTrueTypeFont(new byte[10], 0);
	}
}
