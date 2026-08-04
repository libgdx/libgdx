
package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.utils.Array;

import org.junit.Test;

import static org.junit.Assert.*;

/** Headless tests for tinting a {@link GlyphLayout} while it is added to a {@link BitmapFontCache}.
 *
 * <p>
 * The vertices produced by {@link BitmapFontCache#addText(GlyphLayout, float, float, Color)} must be identical to those produced
 * by adding the text and then calling {@link BitmapFontCache#tint(Color)}. */
public class BitmapFontCacheTest {
	static private final String CHARS = "abcdefghijklmnopqrstuvwxyz";
	static private final float X = 12.4f, Y = 30.7f;

	@Test
	public void nullTintIsUnchanged () {
		for (boolean integer : new boolean[] {false, true}) {
			BitmapFont font = newFont(integer, 1);
			GlyphLayout layout = new GlyphLayout(font, "[RED]abc[]defghi");

			BitmapFontCache expected = new BitmapFontCache(font, integer);
			expected.setText(layout, X, Y);

			BitmapFontCache actual = new BitmapFontCache(font, integer);
			actual.setText(layout, X, Y, null);

			assertVerticesEqual(expected, actual);
		}
	}

	@Test
	public void whiteTintIsUnchanged () {
		for (boolean integer : new boolean[] {false, true}) {
			BitmapFont font = newFont(integer, 1);
			GlyphLayout layout = new GlyphLayout(font, "[RED]abc[]defghi");

			BitmapFontCache expected = new BitmapFontCache(font, integer);
			expected.setText(layout, X, Y);

			BitmapFontCache actual = new BitmapFontCache(font, integer);
			actual.setText(layout, X, Y, Color.WHITE);

			assertVerticesEqual(expected, actual);
		}
	}

	@Test
	public void tintMatchesTintAfterAdd () {
		Color tint = new Color(0.75f, 1, 0.5f, 0.4f);
		for (String text : new String[] {"abcdefghi", "[RED]abc[]def[#4080c0aa]ghi"}) {
			for (boolean integer : new boolean[] {false, true}) {
				BitmapFont font = newFont(integer, 1);
				GlyphLayout layout = new GlyphLayout(font, text);

				BitmapFontCache expected = new BitmapFontCache(font, integer);
				expected.setText(layout, X, Y);
				float[] untinted = copyVertices(expected, 0);
				expected.tint(tint);

				BitmapFontCache actual = new BitmapFontCache(font, integer);
				actual.setText(layout, X, Y, tint);

				assertVerticesEqual(expected, actual);
				// Guard against both paths being no-ops.
				assertFalse("tint did not change any vertex color", equalVertices(untinted, expected, 0));
			}
		}
	}

	@Test
	public void multiPageTintMatchesTintAfterAdd () {
		Color tint = new Color(1, 1, 1, 0.25f);
		BitmapFont font = newFont(false, 3);
		GlyphLayout layout = new GlyphLayout(font, "[RED]abc[]defghijkl");

		BitmapFontCache expected = new BitmapFontCache(font, false);
		expected.setText(layout, X, Y);
		expected.tint(tint);

		BitmapFontCache actual = new BitmapFontCache(font, false);
		actual.setText(layout, X, Y, tint);

		assertEquals(3, actual.getPageCount());
		for (int page = 0; page < 3; page++)
			assertTrue("page " + page + " has no glyphs", actual.getVertexCount(page) > 0);
		assertVerticesEqual(expected, actual);
	}

	/** A tint applied while adding must not let a later {@link BitmapFontCache#tint(Color)} be skipped, not even when untinted
	 * text is added after it. */
	@Test
	public void tintAfterAddIsNotSkipped () {
		BitmapFont font = newFont(false, 1);
		GlyphLayout layout = new GlyphLayout(font, "abcdefghi");

		BitmapFontCache expected = new BitmapFontCache(font, false);
		expected.addText(layout, X, Y);
		expected.addText(layout, X, Y - 20);

		BitmapFontCache actual = new BitmapFontCache(font, false);
		actual.addText(layout, X, Y, new Color(1, 1, 1, 0.5f));
		actual.addText(layout, X, Y - 20);
		actual.tint(Color.WHITE);

		assertVerticesEqual(expected, actual);
	}

	// --- helpers ---

	/** Creates a font with hand made glyphs, so no texture and no GL context are needed. */
	static private BitmapFont newFont (boolean integer, int pageCount) {
		BitmapFontData data = new BitmapFontData();
		data.markupEnabled = true;
		// A non integer scale, so integer positions actually round.
		data.scaleX = 1.37f;
		data.scaleY = 0.83f;
		data.capHeight = 8;
		data.ascent = -1.5f;
		data.descent = -3;
		data.lineHeight = 16;
		data.down = -16;
		data.xHeight = 6;
		data.spaceXadvance = 5;

		Array<TextureRegion> regions = new Array();
		for (int i = 0; i < pageCount; i++)
			regions.add(new TextureRegion());

		// The glyphs are added after construction, so BitmapFont#load doesn't need a texture to compute the UVs.
		BitmapFont font = new BitmapFont(data, regions, integer);
		for (int i = 0, n = CHARS.length(); i < n; i++) {
			Glyph glyph = new Glyph();
			glyph.id = CHARS.charAt(i);
			glyph.width = 5 + i % 4;
			glyph.height = 7 + i % 3;
			glyph.xoffset = i % 3;
			glyph.yoffset = -(i % 5);
			glyph.xadvance = 6 + i % 4;
			glyph.page = i % pageCount;
			glyph.u = i / 64f;
			glyph.v = i / 32f;
			glyph.u2 = glyph.u + 0.01f;
			glyph.v2 = glyph.v + 0.02f;
			data.setGlyph(glyph.id, glyph);
		}
		return font;
	}

	static private float[] copyVertices (BitmapFontCache cache, int page) {
		float[] vertices = new float[cache.getVertexCount(page)];
		System.arraycopy(cache.getVertices(page), 0, vertices, 0, vertices.length);
		return vertices;
	}

	static private boolean equalVertices (float[] vertices, BitmapFontCache cache, int page) {
		if (vertices.length != cache.getVertexCount(page)) return false;
		float[] cacheVertices = cache.getVertices(page);
		for (int i = 0, n = vertices.length; i < n; i++)
			if (vertices[i] != cacheVertices[i]) return false;
		return true;
	}

	static private void assertVerticesEqual (BitmapFontCache expected, BitmapFontCache actual) {
		assertEquals("page count", expected.getPageCount(), actual.getPageCount());
		for (int page = 0, n = expected.getPageCount(); page < n; page++) {
			int count = expected.getVertexCount(page);
			assertEquals("page " + page + " vertex count", count, actual.getVertexCount(page));
			float[] expectedVertices = expected.getVertices(page), actualVertices = actual.getVertices(page);
			for (int i = 0; i < count; i++)
				assertEquals("page " + page + " vertex " + i, expectedVertices[i], actualVertices[i], 0);
		}
	}
}
