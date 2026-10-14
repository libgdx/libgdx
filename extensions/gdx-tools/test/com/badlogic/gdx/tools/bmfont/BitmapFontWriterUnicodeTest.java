
package com.badlogic.gdx.tools.bmfont;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.tools.bmfont.BitmapFontWriter.OutputFormat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.junit.Assert.*;

public class BitmapFontWriterUnicodeTest {
	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void writesAndReloadsSupplementaryGlyphs () throws IOException {
		BitmapFontData data = new BitmapFontData();
		data.capHeight = 10;
		data.ascent = 5;
		data.lineHeight = 20;
		for (int id : new int[] {'A', 0x1F6E0, Character.MAX_CODE_POINT}) {
			Glyph glyph = new Glyph();
			glyph.id = id;
			glyph.width = 8;
			glyph.height = 10;
			glyph.xadvance = 8;
			data.setGlyph(id, glyph);
		}

		FileHandle file = new FileHandle(temporaryFolder.newFile("unicode.fnt"));
		OutputFormat previous = BitmapFontWriter.getOutputFormat();
		try {
			BitmapFontWriter.setOutputFormat(OutputFormat.Text);
			BitmapFontWriter.writeFont(data, new String[] {"unicode.png"}, file, null, 32, 32);
		} finally {
			BitmapFontWriter.setOutputFormat(previous);
		}

		String output = file.readString();
		assertTrue(output.contains("chars count=3"));
		BitmapFontData loaded = new BitmapFontData(file, false);
		assertEquals('A', loaded.getGlyph('A').id);
		assertEquals(0x1F6E0, loaded.getGlyph(0x1F6E0).id);
		assertEquals(Character.MAX_CODE_POINT, loaded.getGlyph(Character.MAX_CODE_POINT).id);
	}
}
