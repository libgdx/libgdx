package com.badlogicgames.superjumper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class HelpScreen3 extends Screen {
	OrthographicCamera guiCam;
   SpriteBatch batcher;
   Rectangle nextBounds;
   Vector2 touchPoint;
   Texture helpImage;
   TextureRegion helpRegion;    
   
   public HelpScreen3(Game game) {
       super(game);
       
       guiCam = new OrthographicCamera();
       guiCam.setViewport(320, 480);
       guiCam.getPosition().set(320 / 2, 480 / 2, 0);
       nextBounds = new Rectangle(320 - 64, 0, 64, 64);
       touchPoint = new Vector2();
       batcher = new SpriteBatch();
       helpImage = Assets.loadTexture("data/help3.png" );
       helpRegion = new TextureRegion(helpImage, 0, 0, 320, 480);
   }
   
   @Override
   public void resume() {        
   }
   
   @Override
   public void pause() {
       helpImage.dispose();
   }

   @Override
   public void update(float deltaTime) {
  	 if(Gdx.input.justTouched()) {            
           guiCam.getScreenToWorld(Gdx.input.getX(), Gdx.input.getY(), touchPoint);            
                       
            if(OverlapTester.pointInRectangle(nextBounds, touchPoint)) {
                Assets.playSound(Assets.clickSound);
                game.setScreen(new HelpScreen4(game));
                return;
            }            
       }
   }

   @Override
   public void present(float deltaTime) {
      GLCommon gl = Gdx.gl;
      gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
      guiCam.setMatrices();
      
      gl.glEnable(GL10.GL_TEXTURE_2D);
      
      batcher.disableBlending();
      batcher.begin();
      batcher.draw(helpRegion, 0, 0, 320, 480);
      batcher.end();
      
      batcher.enableBlending();
      batcher.begin();          
      batcher.draw(Assets.arrow, 320, 0, -64, 64);
      batcher.end();
      
      gl.glDisable(GL10.GL_BLEND);
   }

   @Override
   public void dispose() {
   }
}

