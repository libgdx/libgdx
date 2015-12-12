package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.ScreenCapturer;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class ScreenCapturerTest extends GdxTest{
	SpriteBatch batch;
	Texture texture;

	@Override
	public void create () {
		batch = new SpriteBatch();
		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		
		/**
		 *  Files saved in the gdx-tests-lwjgl directory in 'Screenshot'
		 *  Created 3 timers to show: 
		 *  1st - fulllscreen screenshot with colors
		 *  2nd - fullscreen screenshot in grayscale
		 *  3rd - part of the screen in grayscale
		 *  4th - part of the screen in with grayscale disabled
		 */
		Timer.schedule(new Task(){
		    @Override
		    public void run() {
		       ScreenCapturer.saveScreenshot();
		    }
		}, 1);
		Timer.schedule(new Task(){
		    @Override
		    public void run() {
		   	 ScreenCapturer.setGrayScale();
		       ScreenCapturer.saveScreenshot();
		    }
		}, 2);
		Timer.schedule(new Task(){
		    @Override
		    public void run() {
		       ScreenCapturer.saveScreenshotZoom(80,80,300,300);
		    }
		}, 3);
		Timer.schedule(new Task(){
		    @Override
		    public void run() {
		   	 ScreenCapturer.disableGrayScale();
		       ScreenCapturer.saveScreenshotZoom(80,80,300,300);
		    }
		}, 4);
	}

	@Override
	public void resume () {
		
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(texture, 100, 100);
		batch.end();
	}

	@Override
	public void pause () {
		Gdx.app.log("FullscreenTest", "paused");
	}

	@Override
	public void dispose () {
		Gdx.app.log("FullscreenTest", "disposed");
	}
}
