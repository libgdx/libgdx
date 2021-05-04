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

package com.badlogic.gdx.tests.lwjgl3;

import java.awt.image.BufferedImage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;

public class Lwjgl3DebugStarter {
	public static void main (String[] argv) throws NoSuchFieldException, SecurityException, ClassNotFoundException {
		GdxTest test = new GdxTest() {
			float r = 0;
			SpriteBatch batch;
			BitmapFont font;
			FPSLogger fps = new FPSLogger();
			Texture texture;
			
			@Override
			public void create () {			
				BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_4BYTE_ABGR);
				texture = new Texture("data/badlogic.jpg");
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
						
						if(character == 'f') {
							Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
//							DisplayMode[] modes = Gdx.graphics.getDisplayModes();
//							for(DisplayMode mode: modes) {
//								if(mode.width == 1920 && mode.height == 1080) {
//									Gdx.graphics.setFullscreenMode(mode);
//									break;
//								}
//							}
						}
						if(character == 'w') {
							Gdx.graphics.setWindowedMode(MathUtils.random(400, 800), MathUtils.random(400, 800));
						}
						if(character == 'e') {
							throw new GdxRuntimeException("derp");
						}			
						if(character == 'c') {
							Gdx.input.setCursorCatched(!Gdx.input.isCursorCatched());
						}
						Lwjgl3Window window = ((Lwjgl3Graphics)Gdx.graphics).getWindow();
						if(character == 'v') {
							window.setVisible(false);
						}
						if(character == 's') {
							window.setVisible(true);
						}
						if(character == 'q') {
							window.closeWindow();
						}
						if(character == 'i') {
							window.iconifyWindow();
						}
						if(character == 'm') {
							window.maximizeWindow();
						}
						if(character == 'r') {
							window.restoreWindow();
						}
						if(character == 'u') {
							Gdx.net.openURI("https://google.com");
						}
						return false;
					}										
				});
			}
			
			long start = System.nanoTime();

			@Override
			public void render () {
				ScreenUtils.clear(1, 0, 0, 1);
				HdpiUtils.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				batch.begin();
				font.draw(batch, Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight() + ", " +
									  Gdx.graphics.getBackBufferWidth() + "x" + Gdx.graphics.getBackBufferHeight() + ", " +
									  Gdx.input.getX() + ", " + Gdx.input.getY() + ", " + 
									  Gdx.input.getDeltaX() + ", " + Gdx.input.getDeltaY(), 0, 20);				
				batch.draw(texture, Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
				batch.end();
				fps.log();
			}

			@Override
			public void resize (int width, int height) {
				Gdx.app.log("Test", "Resized " + width + "x" + height);
			}

			@Override
			public void resume () {
				Gdx.app.log("Test", "resuming");
			}

			@Override
			public void pause () {
				Gdx.app.log("Test", "pausing");
			}

			@Override
			public void dispose () {
				Gdx.app.log("Test", "disposing");
			}
		};
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(640, 480);
		config.setWindowListener(new Lwjgl3WindowListener() {
			@Override
			public void created(Lwjgl3Window window) {
				Gdx.app.log("Window", "created");
			}

			@Override
			public void iconified (boolean isIconified) {
				Gdx.app.log("Window", "iconified: "+ (isIconified ? "true" : "false"));
			}
			
			@Override
			public void maximized (boolean isMaximized) {
				Gdx.app.log("Window", "maximized: " + (isMaximized ? "true" : "false"));
			}

			@Override
			public void focusLost () {
				Gdx.app.log("Window", "focus lost");
			}

			@Override
			public void focusGained () {
				Gdx.app.log("Window", "focus gained");
			}

			@Override
			public boolean closeRequested () {
				Gdx.app.log("Window", "closing");
				return false;
			}

			@Override
			public void filesDropped (String[] files) {	
				for (String file : files){
					Gdx.app.log("Window", "File dropped: " + file);
				}
			}

			@Override
			public void refreshRequested() {
				Gdx.app.log("Window", "refreshRequested");
			}
		});
		for(DisplayMode mode: Lwjgl3ApplicationConfiguration.getDisplayModes()) {
			System.out.println(mode.width + "x" + mode.height);
		}	

		System.setProperty("java.awt.headless", "true"); 
		new Lwjgl3Application(test, config);
	}
}
