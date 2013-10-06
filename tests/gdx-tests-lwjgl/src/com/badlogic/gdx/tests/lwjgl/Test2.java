
package com.badlogic.gdx.tests.lwjgl;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

public class Test2 extends ApplicationAdapter {
	SpriteBatch batch;
	TextureRegion region;
	Pixmap screenCap;
	Texture screenCapTex;
	private int size = 2;

	public void create () {

		batch = new SpriteBatch();
		Pixmap p = new Pixmap(64, 64, Format.RGBA8888);
		p.setColor(Color.RED);
		p.fillRectangle(32, 32, size , size );
		region = new TextureRegion(new Texture(p), 32, 32, size , size );

		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		batch.begin();
		batch.draw(region, 1, 1, 256, 256);
		batch.end();
		screenCap = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		screenCapTex = new Texture(screenCap);
		System.out.println("size: " + size);
		System.out.println(Integer.toHexString(screenCap.getPixel(0, 0)));
		System.out.println(Integer.toHexString(screenCap.getPixel(1, 1)));
		System.out.println(Integer.toHexString(screenCap.getPixel(256, 256)));
		System.out.println(Integer.toHexString(screenCap.getPixel(257, 257)));
	}

	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		batch.begin();
		batch.draw(screenCapTex, 0, 0);
		batch.end();
	}

	public static void main (String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.useGL20 = true;
		new LwjglApplication(new Test2(), config);
	}
}
