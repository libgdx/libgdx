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

import java.util.Arrays;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.input.RemoteInput;
import com.badlogic.gdx.tests.utils.GdxTest;

public class RemoteTest extends GdxTest implements ApplicationListener, InputProcessor {	
	BitmapFont font;
	SpriteBatch batch;
	ImmediateModeRenderer10 renderer;
	String ips;
	
	@Override public void create () {
		RemoteInput receiver = new RemoteInput();		
		Gdx.input = receiver;		
		
		ips = Arrays.toString(receiver.getIPs());
		Gdx.input.setInputProcessor(this);		
		batch = new SpriteBatch();
		font = new BitmapFont();
		renderer = new ImmediateModeRenderer10();
	}

	@Override public void resume () {
		
	}

	@Override public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.drawMultiLine(batch, "ip: " + ips + "\n" +  
											"accel:" + Gdx.input.getAccelerometerX() + "," + 
														 Gdx.input.getAccelerometerY() + "," + 
														 Gdx.input.getAccelerometerZ() + "\n" + 
										   "compass: " + Gdx.input.getAzimuth() + "," + 
										   				  Gdx.input.getPitch() + "," +
										   				  Gdx.input.getRoll() + "\n" +
														 "fps: " + Gdx.graphics.getFramesPerSecond(), 10, 130);
		batch.end();
		
		renderer.begin(GL10.GL_TRIANGLES);
		for(int i = 0; i < 10; i++) {		
			if(Gdx.input.isTouched(i)) {
				renderer.color(1, 0, 0, 0);
				renderer.vertex(Gdx.input.getX(i) - 20, Gdx.graphics.getHeight() - Gdx.input.getY(i) - 20, 0);
				renderer.color(1, 0, 0, 0);
				renderer.vertex(Gdx.input.getX(i) + 20, Gdx.graphics.getHeight() - Gdx.input.getY(i) - 20, 0);
				renderer.color(1, 0, 0, 0);
				renderer.vertex(Gdx.input.getX(i), Gdx.graphics.getHeight() - Gdx.input.getY(i) + 20, 0);
			}
		}
		renderer.end();
	}
	
	@Override public boolean keyDown (int keycode) {
		Gdx.app.log("Input Test", "key down: " + keycode);
		return false;
	}

	@Override public boolean keyTyped (char character) {
		Gdx.app.log("Input Test", "key typed: '" + character + "'");
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		Gdx.app.log("Input Test", "key up: " + keycode);
		return false;
	}
	
	@Override public boolean touchDown (int x, int y, int pointer, int button) {
		Gdx.app.log("Input Test", "touch down: " + x + ", " + y + ", pointer: " + pointer);		
		return false;
	}

	@Override public boolean touchDragged (int x, int y, int pointer) {
		Gdx.app.log("Input Test", "touch dragged: " + x + ", " + y + ", pointer: " + pointer);		
		return false;
	}

	@Override public boolean touchUp (int x, int y, int pointer, int button) {
		Gdx.app.log("Input Test", "touch up: " + x + ", " + y + ", pointer: " + pointer);			
		return false;
	}

	@Override public boolean touchMoved (int x, int y) {
		Gdx.app.log("Input Test", "touch moved: " + x + ", " + y);
		return false;
	}

	@Override public boolean scrolled (int amount) {
		Gdx.app.log("Input Test", "scrolled: " + amount);
		return false;
	}

	@Override public boolean needsGL20 () {
		return false;
	}
}
