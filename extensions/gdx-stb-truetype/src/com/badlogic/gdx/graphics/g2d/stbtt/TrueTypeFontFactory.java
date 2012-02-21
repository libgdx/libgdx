package com.badlogic.gdx.graphics.g2d.stbtt;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.stbtt.StbTrueTypeFont;
import com.badlogic.gdx.graphics.g2d.stbtt.StbTrueTypeFont.Bitmap;
import com.badlogic.gdx.graphics.g2d.stbtt.StbTrueTypeFont.Box;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class TrueTypeFontFactory {

	public static final int TTFF_DEFAULT_FONT_PADDING = 1;
	public static final int TTFF_DEFAULT_MIN_TEXTURESIZE = 64;
	public static final int TTFF_DEFAULT_MAX_TEXTURESIZE = 1024;
	public static final boolean TTFF_DEFAULT_GENERATEKERNINGS = false;
	public static final boolean TTFF_DEFAULT_IGNOREGLYPHOVERFLOW = true;
	public static final TextureFilter TTFF_DEFAULT_TEXTUREFILTER = TextureFilter.Nearest;

	public static BitmapFont createBitmapFont(FileHandle fontFile,
			String characters, float worldWidth, float worldHeight,
			float fontPixelsHeight, float viewportWidth, float viewportHeight) {
		return createBitmapFont(fontFile, characters, worldWidth, worldHeight,
				fontPixelsHeight, viewportWidth, viewportHeight,
				TTFF_DEFAULT_FONT_PADDING, TTFF_DEFAULT_MIN_TEXTURESIZE,
				TTFF_DEFAULT_GENERATEKERNINGS,
				TTFF_DEFAULT_IGNOREGLYPHOVERFLOW, TTFF_DEFAULT_MAX_TEXTURESIZE,
				TTFF_DEFAULT_MAX_TEXTURESIZE, TTFF_DEFAULT_TEXTUREFILTER,
				TTFF_DEFAULT_TEXTUREFILTER);
	}

	public static BitmapFont createBitmapFont(FileHandle fontFile,
			String characters, float worldWidth, float worldHeight,
			float fontPixelsHeight, float viewportWidth, float viewportHeight,
			int fontPadding, int minFontTextureSize, boolean generateKernings,
			boolean ignoreGlyphOverflow, int maxFontTextureSizeW,
			int maxFontTextureSizeH, TextureFilter minTextureFilter,
			TextureFilter magTextureFilter) {

		HashMap<Character, CharacterRegion> characterRegions = new HashMap<Character, CharacterRegion>();

		StbTrueTypeFont stbTrueTypeFont = new StbTrueTypeFont(fontFile);

		float[] fontScales = generateFontScales(stbTrueTypeFont, worldWidth,
				worldHeight, fontPixelsHeight, viewportWidth, viewportHeight);

		float fontScaleW = fontScales[0];
		float fontScaleH = fontScales[1];

		Bitmap[] glyphBitmaps = generateGlyphs(stbTrueTypeFont, characters,
				fontScaleW, fontScaleH);

		Texture fontTexture = generateFontTexture(stbTrueTypeFont, fontScaleW,
				glyphBitmaps, characterRegions, characters, fontPadding,
				minFontTextureSize, ignoreGlyphOverflow, maxFontTextureSizeW,
				maxFontTextureSizeH, minTextureFilter, magTextureFilter);

		HashMap<Character[], Integer> kernings = null;
		if (generateKernings) {
			kernings = new HashMap<Character[], Integer>();
			generateKernings(stbTrueTypeFont, kernings, characterRegions,
					fontScaleW);
		}

		for (int i = 0; i < glyphBitmaps.length; i++) {
			if (glyphBitmaps[i] != null) {
				glyphBitmaps[i].dispose();
			}
		}

		InputStream hieroFontFileBuffer = generateHieroFontFile(
				stbTrueTypeFont, characterRegions, fontScaleH, kernings);

		stbTrueTypeFont.dispose();

		FileHandle fH = new TrueTypeFontHieroFileHandle(hieroFontFileBuffer);

		BitmapFontData bitmapFontData = new BitmapFontData(fH, false);

		TextureRegion region = new TextureRegion(fontTexture);

		return new BitmapFont(bitmapFontData, region, false);
	}

	public static float getPixelPerfectFontScale(float fontPixelsHeight,
			float worldSize, float viewportSize) {

		return 1f / (viewportSize / worldSize);
	}

	private TrueTypeFontFactory() {
		// this is a factory class =)
	}

	private static float[] generateFontScales(StbTrueTypeFont stbTrueTypeFont,
			float worldWidth, float worldHeight, float fontPixelsHeight,
			float deviceWidth, float deviceHeight) {

		float fontSizeDeviceW;
		float fontSizeDeviceH;
		fontSizeDeviceW = deviceWidth * fontPixelsHeight / worldWidth;
		fontSizeDeviceH = deviceHeight * fontPixelsHeight / worldHeight;

		float[] retValues = new float[4];
		retValues[0] = stbTrueTypeFont.scaleForPixelHeight(fontSizeDeviceW);
		retValues[1] = stbTrueTypeFont.scaleForPixelHeight(fontSizeDeviceH);
		retValues[2] = fontSizeDeviceW / fontPixelsHeight;
		retValues[3] = fontSizeDeviceH / fontPixelsHeight;

		return retValues;
	}

	private static Bitmap[] generateGlyphs(StbTrueTypeFont stbTrueTypeFont,
			String fontCharacters, float fontScaleW, float fontScaleH) {

		char[] characters = fontCharacters.toCharArray();
		int nCharacters = characters.length;

		Bitmap[] glyphBitmaps = new Bitmap[nCharacters];

		for (int i = 0; i < nCharacters; i++) {
			int glyphCode = stbTrueTypeFont.findGlyphIndex(characters[i]);
			if (glyphCode == 0) {
				continue;
			}
			glyphBitmaps[i] = stbTrueTypeFont.makeGlyphBitmap(fontScaleW,
					fontScaleH, 0, 0, glyphCode);
		}
		return glyphBitmaps;
	}

	private static Texture generateFontTexture(StbTrueTypeFont stbTrueTypeFont,
			float fontScale, Bitmap[] glyphBitmaps,
			HashMap<Character, CharacterRegion> characterRegions,
			String fontCharacters, int padding, int minFontTextureSize,
			boolean ignoreGlyphOverflow, float maxFontTextureSizeW,
			float maxFontTextureSizeH, TextureFilter minFilter,
			TextureFilter magFilter) {

		char[] characters = fontCharacters.toCharArray();
		int nCharacters = characters.length;

		int fontTextureSizeW = minFontTextureSize;
		int fontTextureSizeH = minFontTextureSize;

		PTNode root = new PTNode();
		root.rL = 0;
		root.rT = 0;
		root.rR = fontTextureSizeW;
		root.rB = fontTextureSizeH;

		for (int imageID = 0; imageID < nCharacters; imageID++) {
			final Bitmap glyphBitmap = glyphBitmaps[imageID];

			if (glyphBitmap == null) {
				continue;
			}

			int glyphWidth = glyphBitmap.pixmap.getWidth() + padding * 2;
			int glyphHeight = glyphBitmap.pixmap.getHeight() + padding * 2;

			int[] glyphPos = root.insertImage(imageID, glyphWidth, glyphHeight);

			if (glyphPos == null) {
				// hmm fontTextureSize too small
				// verify if we can expand
				if (fontTextureSizeW >= maxFontTextureSizeW
						&& fontTextureSizeH >= maxFontTextureSizeH) {
					// no more expansion allowed T_T
					if (!ignoreGlyphOverflow) {
						for (int t = 0; t < glyphBitmaps.length; t++) {
							if (glyphBitmaps[t] != null) {
								glyphBitmaps[t].dispose();	
							}
						}
						stbTrueTypeFont.dispose();
						throw new GdxRuntimeException("Character \'"
								+ characters[imageID]
								+ "\' did not fit inside font texture.");
					} else {
						// skip glyph...
						continue;
					}
				}
				root = new PTNode();
				root.rL = 0;
				root.rT = 0;

				if ((minFilter == TextureFilter.Linear || minFilter == TextureFilter.Nearest)
						&& (magFilter == TextureFilter.Linear || magFilter == TextureFilter.Nearest)) {
					if (fontTextureSizeH > fontTextureSizeW
							&& fontTextureSizeW < maxFontTextureSizeW) {
						fontTextureSizeW *= 2;
					} else {
						fontTextureSizeH *= 2;
					}
				} else {
					fontTextureSizeH *= 2;
					fontTextureSizeW *= 2;
				}
				root.rR = fontTextureSizeW;
				root.rB = fontTextureSizeH;

				imageID = -1;
				continue;
			}

			final Box glyphBox = glyphBitmap.box;

			CharacterRegion cRR = new CharacterRegion(
					glyphPos[0] + padding,
					glyphPos[1] + padding,
					glyphWidth - 2 * padding,
					glyphHeight - 2 * padding,
					glyphBox.x0,
					glyphBox.y0,
					(int) ((stbTrueTypeFont
							.getCodepointHMetrics(characters[imageID]).advance * fontScale) + 0.5f));

			characterRegions.put(characters[imageID], cRR);
		}

		Texture fontTexture = new Texture(root.getTextureWidth(),
				root.getTextureHeight(), Format.RGBA4444);
		fontTexture.setFilter(minFilter, magFilter);

		Set<Character> keySet = characterRegions.keySet();
		for (Character KEY : keySet) {
			for (int i = 0; i < nCharacters; i++) {
				if (characters[i] == KEY) {
					CharacterRegion cR = characterRegions.get(KEY);
					fontTexture.draw(glyphBitmaps[i].pixmap, cR.x, cR.y);
					break;
				}
			}
		}

		return fontTexture;
	}

	private static void generateKernings(StbTrueTypeFont stbTrueTypeFont,
			HashMap<Character[], Integer> kernings,
			final HashMap<Character, CharacterRegion> characterRegions,
			float fontScale) {

		final Character[] keys = new Character[characterRegions.size()];
		characterRegions.keySet().toArray(keys);
		final int n = keys.length;

		for (int i = 0; i < n; i++) {
			final char I = keys[i];
			for (int j = i; j < n; j++) {
				final char J = keys[j];
				int kerning = stbTrueTypeFont.getCodepointKernAdvance(I, J);
				kerning = (int) (kerning * fontScale + 0.5f);
				if (kerning != 0) {
					kernings.put(new Character[] { I, J }, kerning);
				}

				// do the same, but invert it =)
				kerning = stbTrueTypeFont.getCodepointKernAdvance(J, I);
				kerning = (int) (kerning * fontScale + 0.5f);
				if (kerning != 0) {
					kernings.put(new Character[] { J, I }, kerning);
				}
			}

		}
	}

	private static InputStream generateHieroFontFile(
			StbTrueTypeFont stbTrueTypeFont,
			HashMap<Character, CharacterRegion> characterRegions,
			float fontScaleH, HashMap<Character[], Integer> kernings) {
		final StringBuilder buffer = new StringBuilder();
		final String nl = "\n";

		int lineHeight = (int) ((stbTrueTypeFont.getFontVMetrics().ascent - stbTrueTypeFont
				.getFontVMetrics().descent) * fontScaleH + 0.5f);
		int base = (int) (stbTrueTypeFont.getFontVMetrics().ascent * fontScaleH + 0.5f);
		buffer.append(nl); // gdx ignores first line
		buffer.append("common lineHeight=").append(lineHeight).append(" base=")
				.append(base).append(" ignored ignored").append(nl);
		buffer.append("page id=0 file=\"ignored.ignored\"").append(nl);
		buffer.append("chars count=").append(characterRegions.size() + 1)
				.append(nl); /*- +1 == space */
		buffer.append(
				"char id=32   x=0     y=0     width=0     height=0     xoffset=0     yoffset=")
				.append(lineHeight).append("    xadvance=")
				.append(lineHeight / 2).append("     page=0  chnl=0")
				.append(nl);

		if (characterRegions.size() == 0) {
			buffer.append(
					"char id=33   x=0     y=0     width=1     height=1     xoffset=0     yoffset=")
					.append(lineHeight).append("    xadvance=")
					.append(lineHeight / 2).append("     page=0  chnl=0")
					.append(nl);
		}
		Set<Character> keySet = characterRegions.keySet();
		final String s1 = "char id=";
		final String s2 = "   x=";
		final String s3 = "     y=";
		final String s4 = "     width=";
		final String s5 = "     height=";
		final String s6 = "     xoffset=";
		final String s7 = "     yoffset=";
		final String s8 = "    xadvance=";
		final String s9 = "     page=0  chnl=0";
		for (Character KEY : keySet) {
			final CharacterRegion cR = characterRegions.get(KEY);
			buffer.append(s1).append((int) KEY).append(s2).append(cR.x)
					.append(s3).append(cR.y).append(s4).append(cR.w).append(s5)
					.append(cR.h).append(s6).append(cR.xOffset).append(s7)
					.append(cR.yOffset).append(s8).append(cR.xAdvance)
					.append(s9).append(nl);
		}

		if (kernings == null || kernings.size() == 0) {
			buffer.append("kernings count=-1").append(nl);
		} else {
			buffer.append("kernings count=").append(kernings.size()).append(nl);

			final String k1 = "kerning first=";
			final String k2 = "  second=";
			final String k3 = "  amount=";
			final Set<Character[]> kerningKey = kernings.keySet();
			for (Character[] KEY : kerningKey) {
				buffer.append(k1).append((int) KEY[0]).append(k2)
						.append((int) KEY[1]).append(k3)
						.append(kernings.get(KEY)).append(nl);
			}
		}

		return new ByteArrayInputStream(buffer.toString().getBytes());
	}

	public static class CharacterRegion {
		// SPRITE VALUES
		public final int x;
		public final int y;
		public final int w;
		public final int h;

		// BOX VALUES
		public final int xOffset;
		public final int yOffset;
		public final int xAdvance;

		public CharacterRegion(int x, int y, int w, int h, int xOffset,
				int yOffset, int xAdvance) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.xAdvance = xAdvance;
		}
	}

	public static class PTNode {
		public int imageID = -1;
		public boolean leaf = true;
		public PTNode childA;
		public PTNode childB;
		public int rL;
		public int rT;
		public int rR;
		public int rB;

		public int[] insertImage(int imageID, int imageWidth, int imageHeight) {
			PTNode node = insertImageRecursive(this, imageID, imageWidth,
					imageHeight);
			// if did not find space
			if (node == null) {
				return null;
			}

			int[] pos = new int[2];
			/*-
			 *         x
			 *   *----->
			 *   |
			 *   |
			 *   |
			 * y V
			 *
			 */
			pos[0] = node.rL;
			pos[1] = node.rT;
			return pos;
		}

		public int getTextureWidth() {
			return this.rR;
		}

		public int getTextureHeight() {
			return this.rB;
		}

		private static PTNode insertImageRecursive(PTNode node, int imageID,
				int imageWidth, int imageHeight) {
			if (!node.leaf) {
				PTNode newNode = insertImageRecursive(node.childA, imageID,
						imageWidth, imageHeight);
				if (newNode != null) {
					return newNode;
				}

				return insertImageRecursive(node.childB, imageID, imageWidth,
						imageHeight);
			}
			// its a leaf =)
			// if there is an image here already!
			if (node.imageID != -1) {
				return null;
			}

			final int rWidth = node.rR - node.rL;
			final int rHeight = node.rB - node.rT;

			// if space is too small for the image -_-
			if (rWidth < imageWidth || rHeight < imageHeight) {
				return null;
			}

			// if it fits perfectly, thats the spot <3
			if (rWidth == imageWidth && rHeight == imageHeight) {
				node.leaf = true;
				node.imageID = imageID;
				return node;
			}

			// if none of the above, split the rectangular xD
			PTNode childA = new PTNode();
			PTNode childB = new PTNode();

			// decide which way to split
			int dW = rWidth - imageWidth;
			int dH = rHeight - imageHeight;

			if (dW > dH) {
				childA.rL = node.rL;
				childA.rT = node.rT;
				childA.rR = node.rL + imageWidth;
				childA.rB = node.rB;

				childB.rL = node.rL + imageWidth;
				childB.rT = node.rT;
				childB.rR = node.rR;
				childB.rB = node.rB;
			} else {
				childA.rL = node.rL;
				childA.rT = node.rT;
				childA.rR = node.rR;
				childA.rB = node.rT + imageHeight;

				childB.rL = node.rL;
				childB.rT = node.rT + imageHeight;
				childB.rR = node.rR;
				childB.rB = node.rB;
			}

			node.leaf = false;
			node.childA = childA;
			node.childB = childB;

			return insertImageRecursive(node.childA, imageID, imageWidth,
					imageHeight);
		}
	}

	public static class TrueTypeFontHieroFileHandle extends FileHandle {
		private InputStream stringBuffer;

		public TrueTypeFontHieroFileHandle(InputStream stringBuffer) {
			this.stringBuffer = stringBuffer;
		}

		@Override
		public InputStream read() {
			return this.stringBuffer;
		}

		@Override
		public String path() {
			return "ignore.ignore";
		}

		@Override
		public FileHandle child(String name) {
			return this;
		}

		@Override
		public FileHandle parent() {
			return this;
		}

		@Override
		public String toString() {
			return "";
		}

	}
}
