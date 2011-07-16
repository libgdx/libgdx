package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ETC1;
import com.badlogic.gdx.graphics.glutils.ETC1.ETC1Data;
import com.badlogic.gdx.tests.utils.GdxTest;

public class ETC1Test extends GdxTest {

	@Override
	public boolean needsGL20() {
		return false;
	}
	
	Texture img1;
	Texture img2;
	SpriteBatch batch;

	@Override public void create() {
		Pixmap pixmap565 = new Pixmap(32, 32, Format.RGB565);
		pixmap565.setColor(1, 0, 0, 1);
		pixmap565.fill();
		pixmap565.setColor(0, 1, 0, 1);
		pixmap565.drawLine(0, 0, 32, 32);
		pixmap565.drawLine(0, 32, 32, 0);
		Pixmap pixmap888 = new Pixmap(Gdx.files.internal("data/badlogic.jpg"));
		
		ETC1Data encodedImage565 = ETC1.encodeImage(pixmap565);
		ETC1Data encodedImage888 = ETC1.encodeImagePKM(pixmap888);
		
		Gdx.app.log("ETC1Test", "rgb565: " + encodedImage565);
		Gdx.app.log("ETC1Test", "rgb888: " + encodedImage888);
		
		pixmap565.dispose();
		pixmap888.dispose();
		
		pixmap565 = ETC1.decodeImage(encodedImage888, Format.RGB565);
		pixmap888 = ETC1.decodeImage(encodedImage565, Format.RGB888);
		
		encodedImage565.dispose();
		encodedImage888.dispose();
		
		img1 = new Texture(pixmap565);
		img2 = new Texture(pixmap888);
		batch = new SpriteBatch();
		
		pixmap565.dispose();
		pixmap888.dispose();
	}
	
	@Override public void render() {
		batch.begin();
		batch.draw(img1, 0, 0);
		batch.draw(img2, img1.getWidth() + 10, 0);
		batch.end();
	}
}