package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;

public class TextureFormatTest extends GdxTest {

	@Override public boolean needsGL20 () {
		return false;
	}

	Texture[] nonMipMapped = new Texture[6];
	Texture[] mipMapped = new Texture[6];	
	SpriteBatch batch;
	
	@Override
	public void create() {
		FileHandle file = Gdx.files.internal("data/bobargb8888-32x32.png");
		nonMipMapped[0] = new Texture(file, Format.Alpha, false);
		nonMipMapped[1] = new Texture(file, Format.LuminanceAlpha, false);
		nonMipMapped[2] = new Texture(file, Format.RGB888, false);
		nonMipMapped[3] = new Texture(file, Format.RGB565, false);
		nonMipMapped[4] = new Texture(file, Format.RGBA8888, false);
		nonMipMapped[5] = new Texture(file, Format.RGBA4444, false);
		
		mipMapped[0] = new Texture(file, Format.Alpha, true);
		mipMapped[1] = new Texture(file, Format.LuminanceAlpha, true);
		mipMapped[2] = new Texture(file, Format.RGB888, true);
		mipMapped[3] = new Texture(file, Format.RGB565, true);
		mipMapped[4] = new Texture(file, Format.RGBA8888, true);
		mipMapped[5] = new Texture(file, Format.RGBA4444, true);
		
		batch = new SpriteBatch();
	}
	
	@Override
	public void render() {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		for(int i = 0; i < 6; i++) {
			batch.draw(nonMipMapped[i], i * 32, 0);
		}
		for(int i = 0; i < 6; i++) {
			batch.draw(mipMapped[i], i * 32, 32);
		}
		batch.end();
	}
}
