/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests.extensions;

import java.util.EnumMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

/** An advanced example of packing many glyphs into a single texture atlas, using FreeTypeFontGenerator.
 * 
 * This example uses enum ordinals for fast access to a two-dimensional array, which stores BitmapFonts by size and style. A more
 * flexible solution might be to use an OjectMap and and IntMap instead.
 * 
 * @author mattdesl AKA davedes */
public class FreeTypePackTest extends GdxTest {

	// Define font sizes here...
	static enum FontSize {
		Tiny(10), Small(12), Medium(16), Large(20), Huge(24), ReallyHuge(28), JustTooBig(64);

		public final int size;

		FontSize (int size) {
			this.size = size;
		}
	}

	// Define font styles here...
	static enum FontStyle {
		Regular("data/arial.ttf"), Italic("data/arial-italic.ttf");

		public final String path;

		FontStyle (String path) {
			this.path = path;
		}
	}

	OrthographicCamera camera;
	SpriteBatch batch;
	Array<TextureRegion> regions;
	String text;

	FontMap<BitmapFont> fontMap;

	public static final int FONT_ATLAS_WIDTH = 1024;
	public static final int FONT_ATLAS_HEIGHT = 512;

	// whether to use integer coords for BitmapFont...
	private static final boolean INTEGER = false;

	// Our demo doesn't need any fancy characters.
	// Note: the set in FreeTypeFontGenerator.DEFAULT_CHARS is more extensive
	// Also note that this string must be contained of unique characters; no duplicates!
	public static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz\n1234567890"
		+ "\"!`?'.,;:()[]{}<>|/@\\^$-%+=#_&~*";

	@Override
	public void create () {
		camera = new OrthographicCamera();
		batch = new SpriteBatch();

		long start = System.currentTimeMillis();
		int glyphCount = createFonts();
		long time = System.currentTimeMillis() - start;
		text = glyphCount + " glyphs packed in " + regions.size + " page(s) in " + time + " ms";

	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		float x = 10;
		float y = Gdx.graphics.getHeight() - 10;

		int renderCalls = 0;

		// NOTE: Before production release on mobile, you should cache the array from values()
		// inside the Enum in order to reduce allocations in the render loop.
		for (FontStyle style : FontStyle.values()) {
			for (FontSize size : FontSize.values()) {
				BitmapFont fnt = getFont(style, size);

				fnt.draw(batch, style.name() + " " + size.size + "pt: The quick brown fox jumps over the lazy dog", x, y);
				y -= fnt.getLineHeight() + 10;
			}
			y -= 20;
		}

		BitmapFont font = getFont(FontStyle.Regular, FontSize.Medium);
		font.draw(batch, text, 10, font.getCapHeight() + 10);

		// draw all glyphs in background
		batch.setColor(1f, 1f, 1f, 0.15f);
		batch.draw(regions.first(), 0, 0);
		batch.setColor(1f, 1f, 1f, 1f);
		batch.end();
	}

	@Override
	public void dispose () {
		super.dispose();
		for (TextureRegion r : regions)
			r.getTexture().dispose(); // dispose the texture since we own it
		batch.dispose();
	}

	// Utility method to grab a font by style/size pair
	public BitmapFont getFont (FontStyle style, FontSize size) {
		return fontMap.get(style).get(size);
	}

	protected int createFonts () {
		// //////////////////////////////////////////////////////////////////////////////////////////////////////
		// //////Steps to use multiple FreeTypeFontGenerators with a single texture atlas://////////////////////
		// 1. Create a new PixmapPacker big enough to fit all your desired glyphs
		// 2. Create a new FreeTypeFontGenerator for each file (i.e. font styles/families)
		// 3. Pack the data by specifying the PixmapPacker parameter to generateData
		// Keep hold of the returned BitmapFontData for later
		// 4. Repeat for other sizes.
		// 5. Dispose the generator and repeat for other font styles/families
		// 6. Get the TextureRegion(s) from the packer using packer.updateTextureRegions()
		// 7. Dispose the PixmapPacker
		// 8. Use each BitmapFontData to construct a new BitmapFont, and specify your TextureRegion(s) to the font constructor
		// 9. Dispose of the Texture upon application exit or when you are done using the font atlas
		// //////////////////////////////////////////////////////////////////////////////////////////////////////

		// create the pixmap packer
		PixmapPacker packer = new PixmapPacker(FONT_ATLAS_WIDTH, FONT_ATLAS_HEIGHT, Format.RGBA8888, 2, false);

		// we need to load all the BitmapFontDatas before we can start loading BitmapFonts
		FontMap<BitmapFontData> dataMap = new FontMap<BitmapFontData>();

		// for each style...
		for (FontStyle style : FontStyle.values()) {
			// get the file for this style
			FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal(style.path));

			// For each size...
			for (FontSize size : FontSize.values()) {
				// pack the glyphs into the atlas using the default chars
				FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
				fontParameter.size = size.size;
				fontParameter.packer = packer;
				fontParameter.characters = CHARACTERS;
				BitmapFontData data = gen.generateData(fontParameter);

				// store the info for later, when we generate the texture
				dataMap.get(style).put(size, data);
			}

			// dispose of the generator once we're finished with this family
			gen.dispose();
		}

		// Get regions from our packer
		regions = new Array<TextureRegion>();
		packer.updateTextureRegions(regions, TextureFilter.Nearest, TextureFilter.Nearest, false);

		// No more need for our CPU-based pixmap packer, as our textures are now on GPU
		packer.dispose();

		// Now we can create our fonts...
		fontMap = new FontMap<BitmapFont>();

		int fontCount = 0;

		// for each style...
		for (FontStyle style : FontStyle.values()) {
			// For each size...
			for (FontSize size : FontSize.values()) {
				// get the data for this style/size pair
				BitmapFontData data = dataMap.get(style).get(size);

				// create a BitmapFont from the data and shared texture
				BitmapFont bmFont = new BitmapFont(data, regions, INTEGER);

				// place the font into our map of loaded fonts
				fontMap.get(style).put(size, bmFont);

				fontCount++;
			}
		}

		// for the demo, show how many glyphs we loaded
		return fontCount * CHARACTERS.length();
	}

	// We use a nested EnumMap for fast access
	class FontMap<T> extends EnumMap<FontStyle, EnumMap<FontSize, T>> {

		public FontMap () {
			super(FontStyle.class);

			// create the enum map for each FontSize
			for (FontStyle style : FontStyle.values()) {
				put(style, new EnumMap<FontSize, T>(FontSize.class));
			}
		}
	}
}
