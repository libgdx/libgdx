package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.tests.utils.GdxTest;

public class ImmediateModeRendererAlphaTest extends GdxTest {	

	     ImmediateModeRenderer renderer;
	   
	     Texture background;
	     TextureRegion bgTR;
	     Sprite sprBg;
	   
	     SpriteBatch batcher;
	   
	     @Override
	     public void create() {

	          this.background = new Texture(Gdx.files.internal("data/badlogic.jpg"));

	          this.background.setFilter(TextureFilter.Linear, TextureFilter.Linear);
	      
	          this.bgTR = new TextureRegion(this.background, 0,0,256,256);
	      
	          this.sprBg = new Sprite(this.background,0,0,640,400);
	      
	          this.batcher = new SpriteBatch();
	      
	           this.renderer = new ImmediateModeRenderer();

	     }

	     @Override
	     public void render() {
	          Gdx.gl.glClearColor(0.0f,0.0f,0.0f,1.0f);
	          Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
	      
	          this.batcher.begin();
	          this.batcher.draw(this.bgTR, 0.0f,0.0f);
	          this.batcher.end();
	      
	          Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	          Gdx.gl.glEnable(GL10.GL_BLEND);
	      
	          renderer.begin(GL10.GL_LINE_STRIP);
	      
	          renderer.color(1.0f,0.0f,0.0f,0.5f);
	          renderer.vertex(20.0f,20.0f,0.0f);
	          renderer.color(1.0f,0.0f,0.0f,0.5f);
	          renderer.vertex(160.0f,20.0f,0.0f);
	          renderer.color(1.0f,0.0f,0.0f,0.5f);
	          renderer.vertex(160.0f,60.0f,0.0f);
	          renderer.color(1.0f,0.0f,0.0f,0.5f);
	          renderer.vertex(20.0f,60.0f,0.0f);
	          renderer.color(1.0f,0.0f,0.0f,0.5f);
	          renderer.vertex(20.0f,20.0f,0.0f);

	          renderer.end();
	        
	          Gdx.gl.glDisable(GL10.GL_BLEND);      
	      
	     }

		@Override
		public boolean needsGL20() {
			return false;
		}
}
