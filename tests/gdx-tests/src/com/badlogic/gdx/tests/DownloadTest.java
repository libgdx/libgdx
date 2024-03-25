
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Queue;

public class DownloadTest extends GdxTest {
	Texture texture;
	SpriteBatch batch;
	Queue<String> urls = new Queue<>();

	@Override
	public void create () {
		urls.addLast("https://www.google.at/images/srpr/logo11w.png");
		urls.addLast("https://placekitten.com/200/300");
		urls.addLast("https://i.imgur.com/snfjsWx.png");

		batch = new SpriteBatch();
		Pixmap.downloadFromUrl(urls.removeFirst(), new Pixmap.DownloadPixmapResponseListener() {
			@Override
			public void downloadComplete (Pixmap pixmap) {
				texture = new Texture(new PixmapTextureData(pixmap, pixmap.getFormat(), false, false, true));
			}

			@Override
			public void downloadFailed (Throwable t) {
				Gdx.app.log("EmptyDownloadTest", "Failed, trying next", t);
				if (urls.notEmpty()) {
					Pixmap.downloadFromUrl(urls.removeFirst(), this);
				}
			}
		});
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		if (texture != null) batch.draw(texture, 0, 0);
		batch.end();
	}
}
