package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.tests.utils.GdxTest;

public class PixmapTest extends GdxTest {

	@Override public boolean needsGL20 () {
		return false;
	}

	Pixmap pixmap;
	Texture texture;
	SpriteBatch batch;
	TextureRegion region;
	
	public void create() {
		 // Create an empty dynamic pixmap 
      pixmap = new Pixmap (800, 480, Pixmap.Format.RGBA8888); // Pixmap.Format.RGBA8888);

      // Create a texture to contain the pixmap
      texture = new Texture (1024, 1024, Pixmap.Format.RGBA8888); //Pixmap.Format.RGBA8888);   
      texture.setFilter (Texture.TextureFilter.Nearest, Texture.TextureFilter.Linear);
      texture.setWrap   (Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            
      pixmap.setColor(1.0f, 0.0f, 0.0f, 1.0f);   // Red
      pixmap.drawLine(0, 0, 100, 100);
      
      pixmap.setColor(0.0f, 0.0f, 1.0f, 1.0f);   // Blue
      pixmap.drawLine(100, 100, 200, 0);
      
      pixmap.setColor(0.0f, 1.0f, 0.0f, 1.0f);    // Green
      pixmap.drawLine(100, 0, 100, 100);
      
      pixmap.setColor(1.0f, 1.0f, 1.0f, 1.0f);    // White
      pixmap.drawCircle(400, 300, 16);
      
      // Blit the composited overlay to a texture
      texture.draw (pixmap, 0,0);
      region = new TextureRegion(texture, 0, 0, 800, 480);
      batch = new SpriteBatch();
	}
	
	public void render() {
		Gdx.gl.glClearColor(0.6f, 0.6f, 0.6f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw (region, 0, 0);
		batch.end();
	}
}
