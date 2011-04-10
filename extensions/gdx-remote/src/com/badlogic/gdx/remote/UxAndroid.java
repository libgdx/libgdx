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
package com.badlogic.gdx.remote;

import javax.microedition.khronos.opengles.GL10;

import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.RemoteSender;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class UxAndroid extends AndroidApplication {
	String IP = null;
	int PORT = 0;
	RemoteSender sender;
	
	@Override public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		IP = bundle.getString("ip");
		PORT = Integer.parseInt(bundle.getString("port"));
		Log.d("UxAndroid", "ip: " + IP + ", port: " + PORT);
		initialize(new ApplicationListener() {
			
			BitmapFont font;
			SpriteBatch batch;			

			@Override public void create () {
				new Thread(new Runnable() {

					@Override public void run () {
						try {
							RemoteSender sender = new RemoteSender(IP, PORT);
							synchronized(UxAndroid.this) {
								UxAndroid.this.sender = sender;
							}
						} catch(GdxRuntimeException e) {					
						}						
					}
					
				}).start();
				
				batch = new SpriteBatch();
				font = new BitmapFont();
			}
			
			@Override public void resume () {
			}

			@Override public void resize (int width, int height) {
				batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
			}

			@Override public void render () {
				boolean connected = false;
				synchronized(UxAndroid.this) {
					if(sender != null) {
						sender.sendUpdate();
						connected = sender.isConnected();
					}
				}
				
				Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
				batch.begin();				
				if(connected) {
					font.draw(batch, "accel:" + Gdx.input.getAccelerometerX() + ", " + 
										  Gdx.input.getAccelerometerY() + ", " + 
										  Gdx.input.getAccelerometerZ() + ", fps: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
				} else {
					font.draw(batch, "No connection to " + IP + ":" + PORT, 10, 20);
				}
				batch.end();							
			}			

			@Override public void pause () {

			}

			@Override public void dispose () {

			}
		}, false);
	}
}
