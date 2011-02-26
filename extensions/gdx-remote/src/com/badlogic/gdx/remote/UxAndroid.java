
package com.badlogic.gdx.remote;

import javax.microedition.khronos.opengles.GL10;

import android.os.Bundle;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.RemoteInput;
import com.badlogic.gdx.input.RemoteSender;

public class UxAndroid extends AndroidApplication {
	String IP = null;
	int PORT = 0;
	
	@Override public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		IP = bundle.getString("ip");
		PORT = bundle.getInt("port");
		initialize(new ApplicationListener() {

			RemoteSender sender;
			BitmapFont font;
			SpriteBatch batch;

			@Override public void create () {
				sender = new RemoteSender(IP, PORT);				
				batch = new SpriteBatch();
				font = new BitmapFont();
			}
			
			@Override public void resume () {
			}

			@Override public void resize (int width, int height) {
				batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
			}

			@Override public void render () {
				sender.sendUpdate();
				Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
				batch.begin();
				font.draw(batch, "accel:" + Gdx.input.getAccelerometerX() + ", " + 
									  Gdx.input.getAccelerometerY() + ", " + 
									  Gdx.input.getAccelerometerZ() + ", fps: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
				batch.end();
			}			

			@Override public void pause () {

			}

			@Override public void dispose () {

			}
		}, false);
	}
}
