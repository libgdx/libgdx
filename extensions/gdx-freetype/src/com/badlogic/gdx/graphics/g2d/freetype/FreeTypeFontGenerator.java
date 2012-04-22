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
	public static final String DEFAULT_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"!`?'.,;:()[]{}<>|/@\\^$-%+=#_&~* ¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ";
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

		TextureAtlas textureAtlas = atlas.generateTextureAtlas(TextureFilter.Nearest, TextureFilter.Nearest);
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
