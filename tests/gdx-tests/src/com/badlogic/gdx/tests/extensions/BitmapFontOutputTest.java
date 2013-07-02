package com.badlogic.gdx.tests.extensions;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.tests.extensions.BitmapFontWriter.FontInfo;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SharedLibraryLoader;

public class BitmapFontOutputTest extends GdxTest {

	SpriteBatch batch;
	OrthographicCamera camera;
	BitmapFont font, smallFont;
	BitmapFontCache cache;
	
	BitmapFont ttfFont;
	BitmapFont ttfFont2;
	Texture tex;
	public static final int[] FONT_SIZES = { 64, 32, 24, 18, 16, 12, 10 };
	public static final String DEFAULT_CHARS = FreeTypeFontGenerator.DEFAULT_CHARS + "�";
		
	//Generates a TTF font and saves it to the file system
	public static void run(String[] argv) throws Exception {
		try {
			new SharedLibraryLoader("../../gdx/libs/gdx-natives.jar").load("gdx");
			new SharedLibraryLoader("../../extensions/gdx-freetype/libs/gdx-freetype-natives.jar").load("gdx-freetype");
			
			
//			FileHandle img = new FileHandle("../gdx-tests-android/assets/data/arial-15_00.png");
//			FileHandle fnt = new FileHandle("../gdx-tests-android/assets/data/arial-15.fnt");
			
			//FileHandle out = new FileHandle(new File("TestFont.fnt").getCanonicalPath());
			
			
						
//			BitmapFont.BitmapFontData data = new BitmapFont.BitmapFontData(fnt, true);
			
//			BitmapFontWriter.write(data, new Pixmap[] {p}, out, null);
			
			//packer.dispose();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void create () {
		camera = new OrthographicCamera();
		batch = new SpriteBatch(); 
		
		//pixmap packer will hold the image data for glyphs
		PixmapPacker packer = new PixmapPacker(512, 512, Format.RGBA8888, 2, false);
		
		FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("data/arial.ttf"));
		
		//generate font data for each size
		Array<BitmapFontData> dataList = new Array<BitmapFontData>();
		for (int size : FONT_SIZES) {
			BitmapFontData d = gen.generateData(size, DEFAULT_CHARS, false, packer);
			dataList.add( d );
		}
		gen.dispose();
		
		//get the pixmap from each page in the packer
		Pixmap[] px = new Pixmap[packer.getPages().size];
		for (int i=0; i<px.length; i++) {
			px[i] = packer.getPages().get(i).getPixmap();
		}
		
		//scaleW and scaleH parameters for our font file
		int scaleW = px[0].getWidth();
		int scaleH = px[0].getHeight();
		
		//this is our output font file
		FileHandle fntOut = new FileHandle("../gdx-tests-android/assets/data/");
		
		//write the pixmaps, getting a string of refs to their files
		String[] refs = BitmapFontWriter.writePixmaps(px, fntOut, "font-output");
		
		//now write each font definition 
		for (int i=0; i<dataList.size; i++) {
			int size = FONT_SIZES[i];
			
			//the info is optional, but produces a cleaner looking .fnt file
			FontInfo info = new FontInfo("Arial", size);
			
			//write the glyph & kerning information
			BitmapFontWriter.write(dataList.get(i), refs, fntOut.child("font-output-"+size+"pt.fnt"), info, scaleW, scaleH);
		}
		
		//Test font 1 by producing Textures from the pixmaps
		TextureRegion[] regs = new TextureRegion[px.length];
		for (int i=0; i<regs.length; i++) {
			regs[i] = new TextureRegion( new Texture(px[i]) );
		}
		ttfFont = new BitmapFont(dataList.get(0), regs, false);
		
		//And test the output...
		FileHandle fntResult = Gdx.files.internal("data/font-output-"+FONT_SIZES[0]+"pt.fnt");
		if (fntResult.exists())
			ttfFont2 = new BitmapFont(fntResult, false);
	}
	
	//TODO:
	//1. Make a tool that loads a definition of fonts and spits out FNT and PNG files
	//   ------ i.e. A command-line Hiero
	//2. Move BitmapFontWriter to tools
	//3. Make a FontPack type of utililty for loading multiple fonts on the fly, and then generating a file
	
	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		ttfFont.draw(batch, "Hello world! Â§", 0, Gdx.graphics.getHeight()-10);
		if (ttfFont2!=null)
			ttfFont2.draw(batch, "Hello world! Â§", 0, Gdx.graphics.getHeight()-15-ttfFont.getLineHeight());
		
//		batch.draw(tex, 0, 0);
		batch.end();
	}

	@Override
	public boolean needsGL20 () {
		return true;
	}
	

	@Override
	public void dispose () {
		super.dispose();
		batch.dispose();
	}
}
