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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.PixmapPacker.Page;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

/**
 * An example of packing many glyphs into a single texture atlas, using FreeTypeFontGenerator. 
 * This example also uses enum ordinals for fast access to a two-dimensional array, which stores BitmapFonts
 * by size and style. 
 * 
 * @author mattdesl AKA davedes
 */
public class FreeTypePackTest extends GdxTest {
	
	//Define font sizes here...
	static enum FontSize {
		Tiny(10),
		Small(12),
		Medium(16),
		Large(18),
		Huge(24);
		
		public final int size;
		
		FontSize(int size) {
			this.size = size;
		}
	}
	
	//Define font styles here...
	static enum FontStyle {
		Regular("data/arial.ttf"),
		Italic("data/arial-italic.ttf");
		
		public final String path;
		
		FontStyle(String path) {
			this.path = path;
		}
	}

	
	OrthographicCamera camera;
	SpriteBatch batch;
	Texture fontAtlasTexture;
	BitmapFont[][] fonts;
	String text;
	
	public static final int FONT_ATLAS_WIDTH = 1024;
	public static final int FONT_ATLAS_HEIGHT = 512;
	private static final boolean INTEGER = false;
	
	
	@Override
	public void create () {
		camera = new OrthographicCamera();
		batch = new SpriteBatch();
		
		long start = System.currentTimeMillis();
		int glyphCount = createFonts();
		long time = System.currentTimeMillis() - start;
		text = glyphCount+" glyphs packed in "+time+" ms";
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		float x = 10;
		float y = Gdx.graphics.getHeight()-10;
		
		//NOTE: Before production release on mobile, you should cache the array from values() 
		//inside the Enum in order to reduce allocations in the render loop.
		for (FontStyle style : FontStyle.values()) {
			for (FontSize size : FontSize.values()) {
				BitmapFont fnt = getFont(style, size);
				fnt.draw(batch, style.name()+" "+size.size+"pt: The quick brown fox jumps over the lazy dog", x, y);
				y -= fnt.getLineHeight() + 10;
			}
			y -= 50;
		}
		
		BitmapFont font = getFont(FontStyle.Regular, FontSize.Medium);
		font.draw(batch, text, 10, font.getCapHeight()+10);
		
		//draw all glyphs in background
		batch.setColor(1f,1f,1f,0.15f);
		batch.draw(fontAtlasTexture, 0, 0);
		batch.setColor(1f,1f,1f,1f);
		batch.end();
	}

	@Override
	public boolean needsGL20 () {
		return true;
	}
	

	@Override
	public void dispose () {
		super.dispose();
		fontAtlasTexture.dispose(); //dispose the texture since we own it
	}
	
	public BitmapFont getFont(FontStyle style, FontSize size) {
		return fonts[style.ordinal()][size.ordinal()];
	}
	
	protected int createFonts() {
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		//////// Steps to use multiple FreeTypeFontGenerators with a single texture atlas://////////////////////
		// 1. Create a new PixmapPacker big enough to fit all your desired glyphs
		// 2. Create a new FreeTypeFontGenerator for each file (i.e. font styles/families)
		// 3. Pack the data by specifying the PixmapPacker parameter to generateData
		//    Keep hold of the returned BitmapFontData for later
		// 4. Repeat for other sizes.
		// 5. Dispose the generator and repeat for other font styles/families
		// 6. Create a new Texture and TextureRegion from the Pixmap object from pixmapPacker.getPages().get(0).getPixmap()
		// 7. Dispose the PixmapPacker
		// 8. Use each BitmapFontData to construct a new BitmapFont, and specify your TextureRegion to the font constructor
		// 9. Dispose of the Texture upon application exit or when you are done using the font atlas
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//create the pixmap packer
		PixmapPacker packer = new PixmapPacker(FONT_ATLAS_WIDTH, FONT_ATLAS_HEIGHT, Format.RGBA8888, 2, false);
		
		Array<DataInfo> infoList = new Array<DataInfo>();
		
		//for each style...
		for (FontStyle style : FontStyle.values()) {
			//get the file for this style
			FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal(style.path));
			
			//For each size...
			for (FontSize s : FontSize.values()) {
				//we will use this later when generating the texture atlas
				DataInfo info = new DataInfo();
				info.size = s;
				info.style = style;
				
				//pack the glyphs into the atlas using the default chars
				info.data = gen.generateData(s.size, FreeTypeFontGenerator.DEFAULT_CHARS, false, packer);
				
				//store the info for later, when we generate the texture
				infoList.add(info);
			}
			
			//dispose of the generator once we're finished with this family
			gen.dispose();
		}
		
		//our app only supports a max of one page!
		if (packer.getPages().size > 1)
			Gdx.app.log("FreeTypePakcTest", "Could not fit all fonts into a single texture page");
		
		
		Page p = packer.getPages().get(0);
		
		//generate texture from pixmap packer
		fontAtlasTexture = new Texture(p.getPixmap(), p.getPixmap().getFormat(), false);
		fontAtlasTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		
		//get a region for our bitmap font constructor
		TextureRegion reg = new TextureRegion(fontAtlasTexture);
		
		//no more need for the PixmapPacker.. we can dispose it
		packer.dispose();
		
		int styleCount = FontStyle.values().length;
		int sizeCount = FontSize.values().length;
		
		fonts = new BitmapFont[styleCount][];
		
		//now we need to generate BitmapFonts ... 
		for (int i=0; i<infoList.size; i++) {
			DataInfo info = infoList.get(i);
			int styleIndex = info.style.ordinal();
			int sizeIndex = info.size.ordinal();
			if (fonts[styleIndex]==null)
				fonts[styleIndex] = new BitmapFont[sizeCount];
			fonts[styleIndex][sizeIndex] = new BitmapFont(info.data, reg, INTEGER);
		}
		return styleCount * sizeCount * FreeTypeFontGenerator.DEFAULT_CHARS.length();
	}
	
	class DataInfo {
		FontSize size;
		FontStyle style;
		BitmapFontData data;
	}
	
}
