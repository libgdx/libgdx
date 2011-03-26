package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tests.utils.GdxTest;

public class SimpleAnimationTest extends GdxTest implements InputProcessor {

	@Override public boolean needsGL20 () {
		return false;
	}

   private Animation currentWalk;
   private float currentFrameTime;
   private Vector2 position;
   
   private Texture tex;
   
   private Animation downWalk;
   private Animation leftWalk;
   private Animation rightWalk;
   private Animation upWalk;
   
   private SpriteBatch spriteBatch;
   
   private static final float ANIMATION_SPEED = 0.2f;
   
   @Override
   public void create()
   {
      Gdx.input.setInputProcessor(this);
      tex = new Texture(Gdx.files.internal("data/animation.png"));
      TextureRegion[][] regions = TextureRegion.split(tex, 32, 48);
      TextureRegion[] downWalkReg = regions[0];
      TextureRegion[] leftWalkReg = regions[1];
      TextureRegion[] rightWalkReg = regions[2];
      TextureRegion[] upWalkReg = regions[3];
      downWalk = new Animation(ANIMATION_SPEED, downWalkReg);
      leftWalk = new Animation(ANIMATION_SPEED, leftWalkReg);
      rightWalk = new Animation(ANIMATION_SPEED, rightWalkReg);
      upWalk = new Animation(ANIMATION_SPEED, upWalkReg);
      
      currentWalk = leftWalk;
      currentFrameTime = 0.0f;
      
      spriteBatch = new SpriteBatch();
      position = new Vector2();
   }

   @Override
   public void render() 
   {
      Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
      currentFrameTime += Gdx.graphics.getDeltaTime();
      
      spriteBatch.begin();
      TextureRegion frame = currentWalk.getKeyFrame(currentFrameTime, true);
      spriteBatch.draw(frame, position.x, position.y);
      spriteBatch.end();
   }


   @Override
   public boolean touchDown(int x, int y, int pointer, int button) {
      position.x = x;
      position.y = -y + 48;
      //System.out.println(position);
      return true;
   }

	@Override public boolean keyDown (int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean keyTyped (char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean touchUp (int x, int y, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean touchDragged (int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean touchMoved (int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean scrolled (int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}
