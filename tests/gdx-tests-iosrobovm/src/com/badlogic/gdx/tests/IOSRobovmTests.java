package com.badlogic.gdx.tests;

import org.robovm.cocoatouch.foundation.NSAutoreleasePool;
import org.robovm.cocoatouch.uikit.UIApplication;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class IOSRobovmTests extends IOSApplication.Delegate {
	@Override
	protected IOSApplication createApplication() {
		IOSApplicationConfiguration config = new IOSApplicationConfiguration();
		return new IOSApplication(new ApplicationAdapter() {
			SpriteBatch batch;
			Texture texture;
			
			@Override
			public void create() {
				batch = new SpriteBatch();
				System.out.println("create: " + batch);
				texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
			}

			@Override
			public void render() {
				Gdx.gl.glClearColor(1, 1, 1, 1);
				Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
//				System.out.println("render: " + Thread.currentThread().getName());
				if(batch != null) {
					batch.begin();
					batch.draw(texture, 0, 0);
					batch.end();
				}
			}
			
		}, config);
	}

	public static void main(String[] argv) {
		NSAutoreleasePool pool = new NSAutoreleasePool();
		UIApplication.main(argv, null, IOSRobovmTests.class);
		pool.drain();
	}
}
