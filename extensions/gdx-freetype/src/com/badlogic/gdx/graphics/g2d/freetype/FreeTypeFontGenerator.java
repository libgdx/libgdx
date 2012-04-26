package com.badlogic.gdx.graphics.g2d.freetype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType.Bitmap;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType.Face;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType.GlyphMetrics;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType.GlyphSlot;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType.Library;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType.SizeMetrics;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Generates {@link BitmapFont} and {@link BitmapFontData} instances from TrueType font files.</p>
 * 
 * Usage example: 
 * <pre>
 * FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("myfont.ttf"));
 * BitmapFont font = gen.generateFont(16);
 * gen.dispose();
 * </pre>
 * 
 * The generator has to be disposed once it is no longer used. The returned {@link BitmapFont} 
 * instances are managed by the user and have to be disposed as usual.
 * 
 * @author mzechner
 *
 */
public class FreeTypeFontGenerator implements Disposable {
	public static final String DEFAULT_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"!`?'.,;:()[]{}<>|/@\\^$-%+=#_&~*\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u0089\u008A\u008B\u008C\u008D\u008E\u008F\u0090\u0091\u0092\u0093\u0094\u0095\u0096\u0097\u0098\u0099\u009A\u009B\u009C\u009D\u009E\u009F\u00A0\u00A1\u00A2\u00A3\u00A4\u00A5\u00A6\u00A7\u00A8\u00A9\u00AA\u00AB\u00AC\u00AD\u00AE\u00AF\u00B0\u00B1\u00B2\u00B3\u00B4\u00B5\u00B6\u00B7\u00B8\u00B9\u00BA\u00BB\u00BC\u00BD\u00BE\u00BF\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00C8\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF\u00D0\u00D1\u00D2\u00D3\u00D4\u00D5\u00D6\u00D7\u00D8\u00D9\u00DA\u00DB\u00DC\u00DD\u00DE\u00DF\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7\u00E8\u00E9\u00EA\u00EB\u00EC\u00ED\u00EE\u00EF\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6\u00F7\u00F8\u00F9\u00FA\u00FB\u00FC\u00FD\u00FE\u00FF";
	final Library library;
	final Face face;

	/**
	 * Creates a new generator from the given TrueType font file. Throws a {@link GdxRuntimeException}
	 * in case loading did not succeed.
	 * @param font the {@link FileHandle} to the TrueType font file
	 */
	public FreeTypeFontGenerator (FileHandle font) {
		library = FreeType.initFreeType();
		if (library == null) throw new GdxRuntimeException("Couldn't initialize FreeType");
		face = FreeType.newFace(library, font, 0);
		if (face == null) throw new GdxRuntimeException("Couldn't create face for font '" + font + "'");
		if (!FreeType.setPixelSizes(face, 0, 15)) throw new GdxRuntimeException("Couldn't set size for font '" + font + "'");
	}
	
	/**
	 * Generates a new {@link BitmapFont}, containing glyphs for the given characters. The size 
	 * is expressed in pixels. Throws a GdxRuntimeException in case the font could not be generated.
	 * Using big sizes might cause such an exception. All characters need to fit onto a single
	 * texture.
	 * @param size the size in pixels
	 * @param characters the characters the font should contain
	 * @param flip whether to flip the font horizontally, see {@link BitmapFont#BitmapFont(FileHandle, TextureRegion, boolean)
	 * @return
	 */
	public BitmapFont generateFont(int size, String characters, boolean flip) {
		FreeTypeBitmapFontData data = generateData(size, characters, flip);
		return new BitmapFont(data, data.getTextureRegion(), false);
	}
	
	/**
	 * Generates a new {@link BitmapFont}. The size is expressed in pixels. Throws
	 * a GdxRuntimeException in case the font could not be generated. Using big
	 * sizes might cause such an exception. All characters need to fit onto a single
	 * texture.
	 * 
	 * @param size the size of the font in pixels
	 * @return 
	 */
	public BitmapFont generateFont(int size) {
		return generateFont(size, DEFAULT_CHARS, false);
	}

	/**
	 * Generates a new {@link BitmapFontData} instance, expert usage only. Throws
	 * a GdxRuntimeException in case something went wrong.
	 * @param size the size in pixels
	 * @return
	 */
	public FreeTypeBitmapFontData generateData (int size) {
		return generateData(size, DEFAULT_CHARS, false);
	}
	
	/**
	 * Generates a new {@link BitmapFontData} instance, expert usage only. Throws
	 * a GdxRuntimeException in case something went wrong.
	 * 
	 * @param size the size in pixels
	 * @param characters the characters the font should contain
	 * @param flip whether to flip the font horizontally, see {@link BitmapFont#BitmapFont(FileHandle, TextureRegion, boolean)
	 * @return
	 */
	public FreeTypeBitmapFontData generateData (int size, String characters, boolean flip) {
		FreeTypeBitmapFontData data = new FreeTypeBitmapFontData();
		if (!FreeType.setPixelSizes(face, 0, size)) throw new GdxRuntimeException("Couldn't set size for font");

		// set general font data
		SizeMetrics fontMetrics = face.getSize().getMetrics();
		data.flipped = flip;
		data.ascent = FreeType.toInt(fontMetrics.getAscender());
		data.descent = FreeType.toInt(fontMetrics.getDescender());
		data.lineHeight = FreeType.toInt(fontMetrics.getHeight());
		float baseLine = data.ascent;

		// determine space width and set glyph
		if (FreeType.loadChar(face, ' ', FreeType.FT_LOAD_DEFAULT)) {
			data.spaceWidth = FreeType.toInt(face.getGlyph().getMetrics().getHoriAdvance());
		} else {
			data.spaceWidth = face.getMaxAdvanceWidth(); // FIXME possibly very wrong :)
		}
		Glyph spaceGlyph = new Glyph();
		spaceGlyph.xadvance = (int)data.spaceWidth;
		data.setGlyph(' ', spaceGlyph);

		// determine x-height
		for (char xChar : BitmapFont.xChars) {
			if (!FreeType.loadChar(face, xChar, FreeType.FT_LOAD_DEFAULT)) continue;
			data.xHeight = FreeType.toInt(face.getGlyph().getMetrics().getHeight());
			break;
		}
		if (data.xHeight == 0) throw new GdxRuntimeException("No x-height character found in font");
		for (char capChar : BitmapFont.capChars) {
			if (!FreeType.loadChar(face, capChar, FreeType.FT_LOAD_DEFAULT)) continue;
			data.capHeight = FreeType.toInt(face.getGlyph().getMetrics().getHeight());
			break;
		}

		// determine cap height
		if (data.capHeight == 1) throw new GdxRuntimeException("No cap character found in font");
		data.ascent = data.ascent - data.capHeight;
		data.down = -data.lineHeight;
		if (flip) {
			data.ascent = -data.ascent;
			data.down = -data.down;
		}

		// generate the glyphs
		int maxGlyphHeight = (int)Math.ceil(data.lineHeight);
		int pageWidth = MathUtils.nextPowerOfTwo((int)Math.sqrt(maxGlyphHeight * maxGlyphHeight * characters.length()));
		PixmapPacker atlas = new PixmapPacker(pageWidth, pageWidth, Format.RGBA8888, 2, false);
		for (int i = 0; i < characters.length(); i++) {
			char c = characters.charAt(i);
			if (!FreeType.loadChar(face, c, FreeType.FT_LOAD_DEFAULT)) { 
				Gdx.app.log("FreeTypeFontGenerator", "Couldn't load char '" + c + "'"); 
				continue; 
			}
			if (!FreeType.renderGlyph(face.getGlyph(), FreeType.FT_RENDER_MODE_NORMAL)) {
				Gdx.app.log("FreeTypeFontGenerator", "Couldn't render char '" + c + "'");
				continue;
			}
			GlyphSlot slot = face.getGlyph();
			GlyphMetrics metrics = slot.getMetrics();
			Bitmap bitmap = slot.getBitmap();
			Pixmap pixmap = bitmap.getPixmap(Format.RGBA8888);
			Rectangle rect = atlas.pack("" + c, pixmap);
			Glyph glyph = new Glyph();
			glyph.width = pixmap.getWidth();
			glyph.height = pixmap.getHeight();
			glyph.xoffset = slot.getBitmapLeft();
			glyph.yoffset = flip ? -slot.getBitmapTop() + (int)baseLine : -(glyph.height - slot.getBitmapTop()) - (int)baseLine;
			glyph.xadvance = FreeType.toInt(metrics.getHoriAdvance());
			glyph.srcX = (int)rect.x;
			glyph.srcY = (int)rect.y;
			data.setGlyph(c, glyph);
			pixmap.dispose();
		}

		// generate kerning
		for (int i = 0; i < characters.length(); i++) {
			for (int j = 0; j < characters.length(); j++) {
				char firstChar = characters.charAt(i);
				Glyph first = data.getGlyph(firstChar);
				if (first == null) continue;
				char secondChar = characters.charAt(j);
				Glyph second = data.getGlyph(secondChar);
				if (second == null) continue;
				int kerning = FreeType.getKerning(face, FreeType.getCharIndex(face, firstChar),
					FreeType.getCharIndex(face, secondChar), 0);
				if (kerning == 0) continue;
				first.setKerning(secondChar, FreeType.toInt(kerning));
			}
		}

		TextureAtlas textureAtlas = atlas.generateTextureAtlas(TextureFilter.Nearest, TextureFilter.Nearest, false);
		data.region = new TextureRegion(textureAtlas.getRegions().get(0).getTexture());
		return data;
	}

	/**
	 * Cleans up all resources of the generator. Call this if you no longer
	 * use the generator.
	 */
	@Override
	public void dispose () {
		FreeType.doneFace(face);
		FreeType.doneFreeType(library);
	}
	
	/**
	 * {@link BitmapFontData} used for fonts generated via the {@link FreeTypeFontGenerator}. The
	 * texture storing the glyphs is held in memory, thus the {@link #getImagePath()} and {@link #getFontFile()}
	 * methods will return null.
	 *  
	 * @author mzechner
	 *
	 */
	public static class FreeTypeBitmapFontData extends BitmapFontData {
		TextureRegion region;

		public TextureRegion getTextureRegion () {
			return region;
		}
	}
}
