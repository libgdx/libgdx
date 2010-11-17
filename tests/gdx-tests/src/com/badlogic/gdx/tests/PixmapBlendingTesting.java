package com.badlogic.gdx.tests;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Sprite;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tests.utils.GdxTest;

public class PixmapBlendingTesting extends GdxTest {
   private SpriteBatch spriteBatch;
   private Texture text;
   private Sprite logoSprite, test3, test4;
   private Pixmap pixD, pixS1, pixS2;
   
   InputProcessor inputProcessor;
   
   @Override
   public void create () {
      if (spriteBatch != null) return;
      spriteBatch = new SpriteBatch();

      Matrix4 transform = new Matrix4();      
      transform.setToTranslation(0, Gdx.graphics.getHeight(), 0);
      transform.mul(new Matrix4().setToScaling(1,-1,1));
      spriteBatch.setTransformMatrix(transform);      

      
      pixS1 = Gdx.graphics.newPixmap(Gdx.files.getFileHandle("data/test4.png", Files.FileType.Internal));
      pixS2 = Gdx.graphics.newPixmap(Gdx.files.getFileHandle("data/test3.png", Files.FileType.Internal));
      pixD = Gdx.graphics.newPixmap(64, 128,Pixmap.Format.RGBA8888);
                  
      pixD.drawPixmap(pixS1, 0, 0, 0, 0, 76, 76);   
      pixD.drawPixmap(pixS2, 0, 0, 0, 0, 76, 76);
      
      try {
		ImageIO.write(((BufferedImage)pixD.getNativePixmap()), ".png", new File("out.png"));
	} catch (IOException e) {
		e.printStackTrace();
	}
      
      logoSprite = new Sprite(Gdx.graphics.newUnmanagedTexture(pixD,
            TextureFilter.Nearest, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge));
         logoSprite.flip(false, true);                    
      }

   @Override
   public void render () {
      
      GL10 gl = Gdx.graphics.getGL10();
      gl.glClearColor(0,1,0,1);
      gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
            
      spriteBatch.begin();            
      logoSprite.draw(spriteBatch);
      spriteBatch.end();
      
   }

public boolean needsGL20 () {
   return false;
}
}
