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
import com.badlogic.gdx.maps.gleed.GleedMap;
import com.badlogic.gdx.maps.gleed.GleedMapRenderer;
import com.badlogic.gdx.maps.loaders.GleedMapLoader;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Logger;

public class GLEEDTest extends GdxTest {
	
	enum State {
		Loading,
		Running
	}
	
	AssetManager manager;
	OrthographicCamera camera;
	GleedMapRenderer renderer;
	State state = State.Loading;
	BitmapFont fpsFont;
	SpriteBatch batch;
	
	@Override
	public boolean needsGL20() {
		return true;
	}
	
	@Override
	public void create() {
		super.create();
		manager = new AssetManager();
		camera = new OrthographicCamera(640, 480);
		camera.setToOrtho(false, 640, 480);
		camera.zoom = 2.0f;
		GleedMapLoader.setLoggingLevel(Logger.INFO);
		manager.setLoader(GleedMap.class, new GleedMapLoader(new InternalFileHandleResolver()));
		manager.load("data/gleedtest.xml", GleedMap.class);
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
		
		if (state == State.Loading && manager.update()) {
			state = State.Running;
			renderer = new GleedMapRenderer(manager.get("data/gleedtest.xml", GleedMap.class));
			fpsFont = manager.get("data/font.fnt", BitmapFont.class);
		}
		
		if (state == State.Running) {
			camera.update();
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

			batch.begin();
			fpsFont.draw(batch, "" + Gdx.graphics.getFramesPerSecond(), 610, 470);
			batch.end();
		}
		
	}
}