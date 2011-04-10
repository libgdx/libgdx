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
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;

public class SpritePerformanteTest2 extends GdxTest {
	static final int SPRITES = 50000;
	Texture image;
	Texture image2;
	Sprite[] sprites;	
	SpriteBatch batch;
	float timePassed = 0;
	int frames = 0;
	
	@Override public void create() {
		image = new Texture(Gdx.files.internal("data/badlogicsmall.jpg"));
		image.setFilter(TextureFilter.Linear, TextureFilter.Linear);			
		image2 = new Texture(Gdx.files.internal("data/bobargb8888-32x32.png"));
		image2.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		sprites = new Sprite[SPRITES];
		for(int i = 0; i < SPRITES; i++) {
			Sprite sprite = new Sprite(i < SPRITES / 2?image:image2);
			sprite.setPosition((float)Math.random() * 800, (float)Math.random() * 600);
			sprites[i] = sprite;
		}
		
		batch = new SpriteBatch();		
	}
	
	@Override public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		float elapsed = Gdx.graphics.getDeltaTime();
		float scale = timePassed>0.5?1-timePassed / 2:0.5f + timePassed / 2;
		
		batch.begin();
		batch.disableBlending();
		for(int i = 0; i < SPRITES; i++) {
			if(i == SPRITES / 2)
				batch.enableBlending();					
			sprites[i].setRotation(sprites[i].getRotation() + elapsed * 45);			
			sprites[i].setScale(scale, scale);
			sprites[i].draw(batch);			
		}
		batch.end();			
		
		timePassed += elapsed;
		frames++;
		if(timePassed > 1.0f) {
			Gdx.app.log("SpritePerformanceTest2", "fps: " + frames);
			timePassed = 0;
			frames = 0;
		}
	}

	@Override public boolean needsGL20 () {
		return false;
	}
}
