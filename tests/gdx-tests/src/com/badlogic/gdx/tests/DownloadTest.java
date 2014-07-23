package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.tests.utils.GdxTest;

public class DownloadTest extends GdxTest {
	Texture texture;
	SpriteBatch batch;

	@Override
	public void create () {
		batch = new SpriteBatch();
		HttpRequest request = new HttpRequest(HttpMethods.GET);
		request.setUrl("https://www.google.at/images/srpr/logo11w.png");
		Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
			@Override
			public void handleHttpResponse (HttpResponse httpResponse) {
				final byte[] bytes = httpResponse.getResult();
				
				Gdx.app.postRunnable(new Runnable() {
					@Override
					public void run () {
						Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
						texture = new Texture(new PixmapTextureData(pixmap, pixmap.getFormat(), false, false, true));	
					}
				});		
			}
			
			@Override
			public void failed (Throwable t) {
				t.printStackTrace();
				Gdx.app.log("EmptyDownloadTest", "Failed", t);
			}
			
			@Override
			public void cancelled () {
				Gdx.app.log("EmptyDownloadTest", "Cancelled");
			}
		});
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		if(texture != null) batch.draw(texture, 0, 0);
		batch.end();
	}
}