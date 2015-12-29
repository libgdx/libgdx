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

package com.badlogic.gdx.tests.lwjgl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.BulletTestCollection;
import com.badlogic.gdx.tests.DeltaTimeTest;
import com.badlogic.gdx.tests.LifeCycleTest;
import com.badlogic.gdx.tests.MusicTest;
import com.badlogic.gdx.tests.StageTest;
import com.badlogic.gdx.tests.TextInputDialogTest;
import com.badlogic.gdx.tests.UITest;
import com.badlogic.gdx.tests.bullet.BulletTest;
import com.badlogic.gdx.tests.g3d.Animation3DTest;
import com.badlogic.gdx.tests.g3d.BaseG3dHudTest;
import com.badlogic.gdx.tests.superkoalio.SuperKoalio;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

public class Lwjgl3DebugStarter {
	public static void main (String[] argv) {	
		GdxTest test = new GdxTest() {
			float r = 0;
			SpriteBatch batch;
			BitmapFont font;
			
			@Override
			public void create () {
				batch = new SpriteBatch();
				font = new BitmapFont();
				Gdx.input.setInputProcessor(new InputAdapter() {

					@Override
					public boolean keyDown (int keycode) {
						System.out.println("Key down: " + Keys.toString(keycode));
						return false;
					}

					@Override
					public boolean keyUp (int keycode) {
						System.out.println("Key up: " + Keys.toString(keycode));
						return false;
					}

					@Override
					public boolean keyTyped (char character) {
						System.out.println("Key typed: '" + character + "', " + (int)character);
						return false;
					}
				});
			}						

			@Override
			public void render () {				
				Gdx.gl.glClearColor(r, 0, 0, 1);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				batch.begin();
				font.draw(batch, Gdx.input.getDeltaX() + ", " + Gdx.input.getDeltaY(), 0, 20);
				batch.end();
				if(Gdx.input.justTouched()) {
					System.out.println("Just touched");
				}
				if(Gdx.input.isKeyJustPressed(Keys.ANY_KEY)) {
					System.out.println("Pressed any key");
				}
			}			
		};
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.width = 960;
		config.height = 600;
		config.vSyncEnabled = false;
		config.useHDPI = true;
		new Lwjgl3Application(test, config);
	}
}
