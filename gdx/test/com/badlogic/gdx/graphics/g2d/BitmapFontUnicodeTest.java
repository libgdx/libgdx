
package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun;
import com.badlogic.gdx.utils.Array;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.junit.Assert.*;

public class BitmapFontUnicodeTest {
	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void loadsSupplementaryGlyph () throws IOException {
		FileHandle file = new FileHandle(temporaryFolder.newFile("unicode.fnt"));
		file.writeString(
			"info face=Test size=16 padding=0,0,0,0 spacing=0,0\n"
				+ "common lineHeight=20 base=15 scaleW=32 scaleH=32 pages=1 packed=0\n" + "page id=0 file=\"unicode.png\"\n"
				+ "chars count=2\n" + "char id=65 x=0 y=0 width=8 height=10 xoffset=0 yoffset=0 xadvance=8 page=0 chnl=0\n"
				+ "char id=128736 x=8 y=0 width=8 height=10 xoffset=0 yoffset=0 xadvance=8 page=0 chnl=0\n" + "kernings count=0\n",
			false, "UTF-8");

		BitmapFontData data = new BitmapFontData(file, false);
		assertEquals(128736, data.getGlyph(128736).id);
		assertTrue(data.hasGlyph(128736));
		assertNull(data.getGlyph(128737));
		assertNull(data.getGlyph(-1));
		assertFalse(data.hasGlyph(0x110000));
	}

	@Test
	public void shapesPairsWithoutLeakingStateBetweenRuns () {
		BitmapFontData data = new BitmapFontData();
		Glyph latin = glyph('A');
		Glyph symbol = glyph(0x1F6E0);
		Glyph other = glyph(0x1F600);
		Glyph missing = glyph(0);
		data.setGlyph('A', latin);
		data.setGlyph(symbol.id, symbol);
		data.setGlyph(other.id, other);
		data.missingGlyph = missing;

		String text = "A\uD83D\uDEE0\uD83D\uDE00A";
		GlyphRun run = new GlyphRun();
		data.getGlyphs(run, text, 0, text.length(), null);
		assertEquals(4, run.glyphs.size);
		assertSame(latin, run.glyphs.get(0));
		assertSame(symbol, run.glyphs.get(1));
		assertSame(other, run.glyphs.get(2));
		assertSame(latin, run.glyphs.get(3));
		assertEquals(5, run.xAdvances.size);

		assertNull(data.getGlyph('\uD83D'));
		assertSame(latin, data.getGlyph('A'));
		run.glyphs.clear();
		run.xAdvances.clear();
		data.getGlyphs(run, "\uD83DA\uDEE0", 0, 3, null);
		assertEquals(3, run.glyphs.size);
		assertSame(missing, run.glyphs.get(0));
		assertSame(latin, run.glyphs.get(1));
		assertSame(missing, run.glyphs.get(2));

		run.glyphs.clear();
		run.xAdvances.clear();
		data.getGlyphs(run, "\uD83D\uDEE0", 0, 1, null);
		assertEquals(1, run.glyphs.size);
		assertSame(missing, run.glyphs.first());
	}

	@Test
	public void supplementaryGlyphDoesNotWrapAsBmpWhitespace () {
		BitmapFontData data = new BitmapFontData();
		Array<Glyph> glyphs = Array.with(glyph('A'), glyph(0x10020), glyph('A'));
		assertEquals(0, data.getWrapIndex(glyphs, 2));
	}

	private static Glyph glyph (int codePoint) {
		Glyph glyph = new Glyph();
		glyph.id = codePoint;
		glyph.width = 8;
		glyph.height = 10;
		glyph.xadvance = 8;
		return glyph;
	}
}
