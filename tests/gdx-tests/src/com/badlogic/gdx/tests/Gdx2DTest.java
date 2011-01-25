
package com.badlogic.gdx.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;

public class Gdx2DTest extends GdxTest {	
	SpriteBatch batch;
	List<Sprite> sprites;	
	
	Gdx2DPixmap createPixmap(int width, int height, int format) {
		return Gdx2DPixmap.newPixmap(width, height, format);
	}
	
	Texture textureFromPixmap(Gdx2DPixmap pixmap) {
		Texture texture = Gdx.graphics.newUnmanagedTexture(pixmap.getWidth(), pixmap.getHeight(), Format.RGB565, TextureFilter.Nearest, TextureFilter.Nearest, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		texture.bind();
		Gdx.gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, pixmap.getGLInternalFormat(), 
								  pixmap.getWidth(), pixmap.getHeight(), 0, pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
		return texture;
	}
	
	@Override public void create () {	
		batch = new SpriteBatch();
		sprites = new ArrayList<Sprite>();
		Gdx2DPixmap alpha = createPixmap(32, 32, Gdx2DPixmap.GDX2D_FORMAT_ALPHA);				
		Gdx2DPixmap luminanceAlpha = createPixmap(32, 32, Gdx2DPixmap.GDX2D_FORMAT_LUMINANCE_ALPHA);					
		Gdx2DPixmap rgb565 = createPixmap(32, 32, Gdx2DPixmap.GDX2D_FORMAT_RGB565);
		Gdx2DPixmap rgba4444 = createPixmap(32, 32, Gdx2DPixmap.GDX2D_FORMAT_RGBA4444);		
		Gdx2DPixmap rgb888 = createPixmap(32, 32, Gdx2DPixmap.GDX2D_FORMAT_RGB888);		
		Gdx2DPixmap rgba8888 = createPixmap(32, 32, Gdx2DPixmap.GDX2D_FORMAT_RGBA8888);
		Gdx2DPixmap composite = createPixmap(256, 32, Gdx2DPixmap.GDX2D_FORMAT_RGB565);
		
		Gdx2DPixmap.setBlend(1);
		
		alpha.clear(Color.rgba8888(1, 1, 1, 0.1f));
		alpha.setPixel(16, 16, Color.rgba8888(1, 1, 1, 1));		
//		if(alpha.getPixel(16, 16) != Color.rgba8888(1, 1, 1, 1f)) throw new RuntimeException("alpha error");
//		if(alpha.getPixel(15, 16) != Color.rgba8888(1, 1, 1, 0.1f)) throw new RuntimeException("alpha error");
		alpha.drawLine(0, 0, 31, 31, Color.rgba8888(1, 1, 1, 1));
		alpha.drawRect(10, 10, 5, 7, Color.rgba8888(1, 1, 1, 0.5f));		
		alpha.fillRect(20, 10, 5, 7, Color.rgba8888(1, 1, 1, 0.5f));
		alpha.drawCircle(16, 16, 10, Color.rgba8888(1, 1, 1, 1));
		alpha.fillCircle(16, 16, 6, Color.rgba8888(1, 1, 1, 1));
		
		luminanceAlpha.clear(Color.rgba8888(1, 1, 1, 0.1f));
		luminanceAlpha.setPixel(16, 16, Color.rgba8888(1, 1, 1.0f, 1.0f));
//		if(luminanceAlpha.getPixel(16, 16) != Color.rgba8888(1, 1, 1, 1)) throw new RuntimeException("luminance alpha error");
//		if(luminanceAlpha.getPixel(15, 16) != Color.rgba8888(1, 1, 1, 0.1f)) throw new RuntimeException("luminance alpha error");
		luminanceAlpha.drawLine(0, 0, 31, 31, Color.rgba8888(1, 1, 1, 1));
		luminanceAlpha.drawRect(10, 10, 5, 7, Color.rgba8888(1, 1, 1, 0.5f));
		luminanceAlpha.fillRect(20, 10, 5, 7, Color.rgba8888(1, 1, 1, 0.5f));
		luminanceAlpha.drawCircle(16, 16, 10, Color.rgba8888(1, 1, 1, 1));
		luminanceAlpha.fillCircle(16, 16, 6, Color.rgba8888(1, 1, 1, 1));
		
		rgb565.clear(Color.rgba8888(1, 0, 0, 1));
		rgb565.setPixel(16, 16, Color.rgba8888(0, 0, 1, 1));
//		if(rgb565.getPixel(16, 16) != Color.rgba8888(0, 0, 1, 1)) throw new RuntimeException("rgb565 error");
//		if(rgb565.getPixel(31, 31) != Color.rgba8888(1, 0, 0, 1)) throw new RuntimeException("rgb565 error");
		rgb565.drawLine(0,0,32,32, Color.rgba8888(0, 1, 0, 1));
		rgb565.drawRect(10, 10, 5, 7, Color.rgba8888(1, 1, 0, 0.5f));
		rgb565.fillRect(20, 10, 5, 7, Color.rgba8888(0, 1, 1, 0.5f));
		rgb565.drawCircle(16, 16, 10, Color.rgba8888(1, 0, 1, 1));
		rgb565.fillCircle(16, 16, 6, Color.rgba8888(1, 0, 1, 1));
		
		rgba4444.clear(Color.rgba8888(1, 0, 0, 1));
		rgba4444.setPixel(16, 16, Color.rgba8888(0, 0, 1, 1));
//		if(rgba4444.getPixel(16, 16) != Color.rgba8888(0, 0, 1, 1)) throw new RuntimeException("rgba4444 error");
//		if(rgba4444.getPixel(15, 16) != 0xff0000ff) throw new RuntimeException("rgba4444 error"); // lut will not be 100% correct
		rgba4444.drawLine(0,0,31,31, Color.rgba8888(0, 1, 0, 1));
		rgba4444.drawRect(10, 10, 5, 7, Color.rgba8888(1, 1, 0, 0.5f));
		rgba4444.fillRect(20, 10, 5, 7, Color.rgba8888(0, 1, 1, 0.5f));
		rgba4444.drawCircle(16, 16, 10, Color.rgba8888(1, 0, 1, 1));
		rgba4444.fillCircle(16, 16, 6, Color.rgba8888(1, 0, 1, 0.5f));
		
		rgb888.clear(Color.rgba8888(1, 0, 0, 1));
		rgb888.setPixel(16, 16, Color.rgba8888(0, 0, 1, 1));
//		if(rgb888.getPixel(16, 16) != Color.rgba8888(0, 0, 1, 1)) throw new RuntimeException("rgb888 error");
//		if(rgb888.getPixel(15, 16) != Color.rgba8888(1, 0, 0, 1)) throw new RuntimeException("rgb888 error");
		rgb888.drawLine(0,0,31,31, Color.rgba8888(0, 1, 0, 1));
		rgb888.drawRect(10, 10, 5, 7, Color.rgba8888(1, 1, 0, 0.5f));
		rgb888.fillRect(20, 10, 5, 7, Color.rgba8888(0, 1, 1, 0.5f));
		rgb888.drawCircle(16, 16, 10, Color.rgba8888(1, 0, 1, 1));
		rgb888.fillCircle(16, 16, 6, Color.rgba8888(1, 0, 1, 1));
		
		rgba8888.clear(Color.rgba8888(1, 0, 0, 1));
		rgba8888.setPixel(16, 16, Color.rgba8888(0, 0, 1, 1));
//		if(rgba8888.getPixel(16, 16) != Color.rgba8888(0, 0, 1, 1)) throw new RuntimeException("rgba8888 error");
//		if(rgba8888.getPixel(15, 16) != Color.rgba8888(1, 0, 0, 1)) throw new RuntimeException("rgba8888 error");
		rgba8888.drawLine(0,0,31,31,Color.rgba8888(0, 1, 0, 1));
		rgba8888.drawRect(10, 10, 5, 7, Color.rgba8888(1, 1, 0, 0.5f));
		rgba8888.fillRect(20, 10, 5, 7, Color.rgba8888(0, 1, 1, 0.5f));
		rgba8888.drawCircle(16, 16, 10, Color.rgba8888(1, 0, 1, 1));
		rgba8888.fillCircle(16, 16, 6, Color.rgba8888(1, 0, 1, 0.5f));
		
		Gdx2DPixmap.setBlend(0);
		composite.clear(Color.rgba8888(1, 1, 1, 1));
		composite.drawPixmap(alpha, 0, 0, 0, 0, 32, 32);
		composite.drawPixmap(luminanceAlpha, 0, 0, 32, 0, 32, 32);
		composite.drawPixmap(rgb565, 0, 0, 64, 0, 32, 32);
		composite.drawPixmap(rgba4444, 0, 0, 96, 0, 32, 32);
		composite.drawPixmap(rgb888, 0, 0, 128, 0, 32, 32);
		composite.drawPixmap(rgba8888, 0, 0, 160, 0, 32, 32);
			
//		Format[] formats = { Format.Alpha, Format.RGB565, Format.RGBA4444, Format.RGBA8888 };
//		int[] gdxFormats = { Gdx2DPixmap.GDX2D_FORMAT_ALPHA, Gdx2DPixmap.GDX2D_FORMAT_RGB565, Gdx2DPixmap.GDX2D_FORMAT_RGBA4444, Gdx2DPixmap.GDX2D_FORMAT_RGBA8888 };
//		for(int format = 0; format < formats.length; format++) {
//			Gdx2DPixmap gdxPixmap = Gdx2DPixmap.newPixmap(256, 256, gdxFormats[format]);
//			Pixmap pixmap = Gdx.graphics.newPixmap(256, 256, formats[format]);
//			Random rand = new Random(0);
//			Random rand2 = new Random(0);
//			final int RUNS = 1000;
//			long startTime = System.nanoTime();		
//			for(int i = 0; i < RUNS; i++) {
////				gdxPixmap.clear(0xffffffff);
//				gdxPixmap.drawLine((int)(rand.nextFloat()*256), ((int)rand.nextFloat()*256), 
//										 (int)(rand.nextFloat()*256), ((int)rand.nextFloat()*256), 0xffffffff);
//				gdxPixmap.drawCircle(((int)rand.nextFloat()*256), ((int)rand.nextFloat()*256), ((int)rand.nextFloat() * 128), 0xffffffff);
//				gdxPixmap.drawRect((int)(rand.nextFloat()*256), ((int)rand.nextFloat()*256), 
//					 					 (int)(rand.nextFloat()*256), ((int)rand.nextFloat()*256), 0xffffffff);
//				gdxPixmap.fillCircle(((int)rand.nextFloat()*256), ((int)rand.nextFloat()*256), ((int)rand.nextFloat() * 128), 0xffffffff);
//				gdxPixmap.fillRect((int)(rand.nextFloat()*256), ((int)rand.nextFloat()*256), 
//					 					 (int)(rand.nextFloat()*256), ((int)rand.nextFloat()*256), 0xffffffff);
//			}
//			Gdx.app.log("Gdx2DTest", "format: " + gdxPixmap.getFormatString());
//			Gdx.app.log("Gdx2DTest", "gdx2d: " + (System.nanoTime()-startTime) / 1000000000.0f);
//	
//			startTime = System.nanoTime();		
//			for(int i = 0; i < RUNS; i++) {
//				pixmap.setColor(1, 1, 1, 1);
////				pixmap.fill();
//				pixmap.setColor(1, 1, 1, 1);
//				pixmap.drawLine((int)(rand2.nextFloat()*256), ((int)rand2.nextFloat()*256), 
//										 (int)(rand2.nextFloat()*256), ((int)rand2.nextFloat()*256));
//				pixmap.drawCircle(((int)rand2.nextFloat()*256), ((int)rand2.nextFloat()*256), ((int)rand2.nextFloat() * 128));
//				pixmap.drawRectangle((int)(rand2.nextFloat()*256), ((int)rand2.nextFloat()*256), 
//					 					 (int)(rand2.nextFloat()*256), ((int)rand2.nextFloat()*256));
//				pixmap.fillCircle(((int)rand2.nextFloat()*256), ((int)rand2.nextFloat()*256), ((int)rand2.nextFloat() * 128));
//				pixmap.fillRectangle((int)(rand2.nextFloat()*256), ((int)rand2.nextFloat()*256), 
//					 					 (int)(rand2.nextFloat()*256), ((int)rand2.nextFloat()*256));
//			}
//			Gdx.app.log("Gdx2DTest", "native: " + (System.nanoTime()-startTime) / 1000000000.0f);
//			pixmap.dispose();
//			gdxPixmap.dispose();
//		}
		
		sprites.add(new Sprite(textureFromPixmap(alpha)));		
		sprites.add(new Sprite(textureFromPixmap(luminanceAlpha)));
		sprites.add(new Sprite(textureFromPixmap(rgb565)));
		sprites.add(new Sprite(textureFromPixmap(rgba4444)));
		sprites.add(new Sprite(textureFromPixmap(rgb888)));
		sprites.add(new Sprite(textureFromPixmap(rgba8888)));
		sprites.add(new Sprite(textureFromPixmap(composite)));		
		
		
		sprites.get(0).setPosition(10, 10);
		sprites.get(1).setPosition(50, 10);
		sprites.get(2).setPosition(90, 10);
		sprites.get(3).setPosition(130, 10);
		sprites.get(4).setPosition(170, 10);
		sprites.get(5).setPosition(210, 10);
		sprites.get(6).setPosition(10, 50);
	}

	@Override public void render() {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.disableBlending();
		batch.begin();
		for(int i = 0; i < sprites.size(); i++) {
			sprites.get(i).draw(batch);
		}
		batch.end();
	}
	
	@Override public boolean needsGL20 () {
		return false;
	}
}
