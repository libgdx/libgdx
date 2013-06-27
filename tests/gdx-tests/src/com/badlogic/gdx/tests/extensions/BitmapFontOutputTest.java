package com.badlogic.gdx.tests.extensions;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.SharedLibraryLoader;

public class BitmapFontOutputTest extends GdxTest {

	SpriteBatch batch;
	OrthographicCamera camera;
	BitmapFont font, smallFont;
	BitmapFontCache cache;
	
	BitmapFont ttfFont;
	
	
	public static void main(String[] argv) {
		try {
			new SharedLibraryLoader("../../gdx/libs/gdx-natives.jar").load("gdx");
			new SharedLibraryLoader("../../extensions/gdx-freetype/libs/gdx-freetype-natives.jar").load("gdx-freetype");
			
//			URL u = BitmapFontOutputTest.class.getResource("../../gdx-tests-android/assets/data/default.png");
//			System.out.println(u);
//			FileHandle h;
//			h = new FileHandle(new File(u.toURI()));
			
			FileHandle img = new FileHandle("../gdx-tests-android/assets/data/default.png");
			FileHandle fnt = new FileHandle("../gdx-tests-android/assets/data/default.fnt");
			
			FileHandle out = new FileHandle("TestFont.fnt");
			System.out.println(out.file().getCanonicalPath());
			Pixmap p = new Pixmap(img);
			
			BitmapFont.BitmapFontData data = new BitmapFont.BitmapFontData(fnt, false);
			
			BitmapFontWriter.write(data, new Pixmap[] {p}, out, null);
			
			p.dispose();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void create () {
		camera = new OrthographicCamera();
		batch = new SpriteBatch();
		
		Pixmap fontPixmap = new Pixmap(Gdx.files.internal("data/default.png"));
		
		TextureRegion tex = new TextureRegion(new Texture(fontPixmap));
		
		font = new BitmapFont(Gdx.files.internal("data/default.fnt"), tex);
		
		FileHandle outFile = Gdx.files.local("test.fnt");
		
		//BitmapFontWriter.write(font, new Pixmap[] { fontPixmap, fontPixmap }, outFile, null);
		
		fontPixmap.dispose();
		
//		System.out.println(outFile2.file().getParentFile());
		
		
		
		//smallFont = new BitmapFont(Gdx.files.internal("data/multipagefont.fnt"), false);
		
		//font = new BitmapFont(Gdx.files.internal("data/multipagefont.fnt"), false);
		//cache = new BitmapFontCache(font);
		
		
		/*
		FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("data/arial.ttf"));
		ttfFont = gen.generateFont(146);
		gen.dispose();
		*/
		
		
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		
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
