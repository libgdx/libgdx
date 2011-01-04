
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Gdx2DPixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Sprite;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.tests.utils.GdxTest;

public class Gdx2DTest extends GdxTest {	
	SpriteBatch batch;
	Sprite sprite;
	Texture texture;
	
	@Override public void create () {
		Gdx2DPixmap pixmap = Gdx2DPixmap.newPixmap(Gdx.files.internal("data/badlogic.jpg").read(), Gdx2DPixmap.GDX2D_FORMAT_RGB);
		texture = Gdx.graphics.newUnmanagedTexture(256, 256, Format.RGBA8888, TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		texture.bind();
		Gdx.gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGB, pixmap.getWidth(), pixmap.getHeight(), 0, GL10.GL_RGB, GL10.GL_UNSIGNED_BYTE, pixmap.getPixels());
		batch = new SpriteBatch();
		sprite = new Sprite(texture);
		sprite.setPosition(100, 50);
	}

	@Override public void render() {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		sprite.draw(batch);
		batch.end();
	}
	
	@Override public boolean needsGL20 () {
		return false;
	}
}
