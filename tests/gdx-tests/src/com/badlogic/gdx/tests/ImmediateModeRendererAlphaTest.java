/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.tests.utils.GdxTest;

public class ImmediateModeRendererAlphaTest extends GdxTest {	

	     ImmediateModeRenderer10 renderer;
	   
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
	      
	           this.renderer = new ImmediateModeRenderer10();

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
