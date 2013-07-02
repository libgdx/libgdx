package com.badlogic.gdx.tests.extensions;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.tests.utils.GdxTest;

public class FontMapTest extends GdxTest {

	SpriteBatch batch;
	OrthographicCamera camera;
	
	public static final int[] FONT_SIZES = { 64, 32, 24, 18, 16, 12, 10 };
	public static final String DEFAULT_CHARS = FreeTypeFontGenerator.DEFAULT_CHARS + "Ã";
		
	public enum Fonts {
		Arial,
		ArialItalic;
	}
		
	FontMap<Fonts> fonts;
	
	@Override
	public void create () {
		camera = new OrthographicCamera();
		batch = new SpriteBatch(); 
		
		//fonts = new FontMap<Fonts>();
		
//		FontPacker<Fonts> pack = new FontPacker<Fonts>();
//		fonts.include(Fonts.Arial, Gdx.files.internal(""))
		
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
