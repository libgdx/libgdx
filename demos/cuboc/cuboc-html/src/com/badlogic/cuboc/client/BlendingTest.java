package com.badlogic.cuboc.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BlendingTest extends GwtApplication {

	@Override
	public GwtApplicationConfiguration getConfig () {
		GwtApplicationConfiguration config = new GwtApplicationConfiguration(640, 640);
		return config;
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return new ApplicationAdapter(){
			SpriteBatch batch;
			Texture texture;
			
			@Override
			public void create () {
				batch = new SpriteBatch();
				texture = new Texture(Gdx.files.internal("data/bob.png"));
			}

			@Override
			public void render () {
				Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
				Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
				batch.begin();
				batch.draw(texture, 0, 0);
				batch.disableBlending();
				batch.draw(texture, texture.getWidth(), 0);
				batch.enableBlending();
				batch.end();
			}
		};
	}

}
