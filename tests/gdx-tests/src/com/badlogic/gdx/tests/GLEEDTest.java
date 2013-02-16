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
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.gleed.GleedMapLoader;
import com.badlogic.gdx.maps.gleed.GleedMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Logger;

public class GLEEDTest extends GdxTest {
	
	enum State {
		Loading,
		Running
	}
	
	final int VIRTUAL_WIDTH = 1280;
	final int VIRTUAL_HEIGHT = 720;
	final float ASPECT_RATIO = (float)VIRTUAL_WIDTH/(float)VIRTUAL_HEIGHT;
	
	AssetManager manager;
	OrthographicCamera camera;
	GleedMapRenderer renderer;
	State state = State.Loading;
	BitmapFont fpsFont;
	SpriteBatch batch;
	Rectangle viewport;
	
	@Override
	public boolean needsGL20() {
		return true;
	}
	
	@Override
	public void create() {
		super.create();
		manager = new AssetManager();
		camera = new OrthographicCamera(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
		camera.zoom = 2.0f;
		GleedMapLoader.setLoggingLevel(Logger.INFO);
		manager.setLoader(Map.class, new GleedMapLoader(new InternalFileHandleResolver()));
		manager.load("data/gleedtest.xml", Map.class);
		manager.load("data/font.fnt", BitmapFont.class);
		batch = new SpriteBatch();
	}
	
	@Override
	public void dispose() {
		batch.dispose();
	}
	
	@Override
	public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		
		Gdx.gl.glViewport((int) viewport.x, (int) viewport.y, (int) viewport.width, (int) viewport.height);
		
		if (state == State.Loading && manager.update()) {
			state = State.Running;
			renderer = new GleedMapRenderer(manager.get("data/gleedtest.xml", Map.class));
			fpsFont = manager.get("data/font.fnt", BitmapFont.class);
		}
		
		if (state == State.Running) {
			renderer.begin();
			renderer.render(camera);
			renderer.end();
			
			if (Gdx.input.isKeyPressed(Keys.UP)) {
				camera.position.y += 5.0f;
			}
			else if (Gdx.input.isKeyPressed(Keys.DOWN)) {
				camera.position.y -= 5.0f;
			}
			
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				camera.position.x += 5.0f;
			}
			else if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				camera.position.x -= 5.0f;
			}
			
			if (Gdx.input.isKeyPressed(Keys.A)) {
				camera.zoom += 0.05f;
			}
			else if (Gdx.input.isKeyPressed(Keys.S)) {
				camera.zoom -= 0.05f;
			}
			
			if (Gdx.input.isTouched()) {
				if (Gdx.input.getX() < VIRTUAL_WIDTH * 0.2f) {
					camera.position.x -= 5.0f;
				}
				else if (Gdx.input.getX() > VIRTUAL_WIDTH * 0.8f) {
					camera.position.x += 5.0f;
				}
				
				if (Gdx.input.getY() < VIRTUAL_HEIGHT * 0.2) {
					camera.position.y += 5.0f;
				}
				else if (Gdx.input.getY() > VIRTUAL_HEIGHT * 0.8f) {
					camera.position.y -= 5.0f;
				}
			}

			batch.begin();
			fpsFont.draw(batch, "" + Gdx.graphics.getFramesPerSecond(), VIRTUAL_WIDTH - 30, VIRTUAL_HEIGHT - 30);
			batch.end();
		}
	}
	
	@Override
	public void resize(int arg0, int arg1) {
	  float aspectRatio = (float)arg0/(float)arg1;
	  float scale = 1f;
	  Vector2 crop = new Vector2(0f, 0f);
	  
	  if(aspectRatio > ASPECT_RATIO)
	  {
	      scale = (float)arg1 / (float)VIRTUAL_HEIGHT;
	      crop.x = (arg0 - VIRTUAL_WIDTH * scale) / 2.0f;
	  }
	  else if(aspectRatio < ASPECT_RATIO)
	  {
	      scale = (float)arg0 / (float)VIRTUAL_WIDTH;
	      crop.y = (arg1 - VIRTUAL_HEIGHT * scale) / 2.0f;
	  }
	  else
	  {
	      scale = (float)arg0/(float)VIRTUAL_WIDTH;
	  }
	
	  float w = (float)VIRTUAL_WIDTH * scale;
	  float h = (float)VIRTUAL_HEIGHT * scale;
	  viewport = new Rectangle(crop.x, crop.y, w, h);
	}
}