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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.StreamUtils;

public class TextureDownloadTest extends GdxTest {
	TextureRegion image;
	BitmapFont font;
	SpriteBatch batch;

	@Override
	public void create () {
		new Thread(new Runnable() {
			/** Downloads the content of the specified url to the array. The array has to be big enough. */
			private int download (byte[] out, String url) {
				InputStream in = null;
				try {
					HttpURLConnection conn = null;
					conn = (HttpURLConnection)new URL(url).openConnection();
					conn.setDoInput(true);
					conn.setDoOutput(false);
					conn.setUseCaches(true);
					conn.connect();
					in = conn.getInputStream();
					int readBytes = 0;
					while (true) {
						int length = in.read(out, readBytes, out.length - readBytes);
						if (length == -1) break;
						readBytes += length;
					}
					return readBytes;
				} catch (Exception ex) {
					return 0;
				} finally {
					StreamUtils.closeQuietly(in);
				}
			}

			@Override
			public void run () {
				byte[] bytes = new byte[200 * 1024]; // assuming the content is not bigger than 200kb.
				int numBytes = download(bytes, "http://www.badlogicgames.com/wordpress/wp-content/uploads/2012/01/badlogic-new.png");
				if (numBytes != 0) {
					// load the pixmap, make it a power of two if necessary (not needed for GL ES 2.0!)
					Pixmap pixmap = new Pixmap(bytes, 0, numBytes);
					final int originalWidth = pixmap.getWidth();
					final int originalHeight = pixmap.getHeight();
					int width = MathUtils.nextPowerOfTwo(pixmap.getWidth());
					int height = MathUtils.nextPowerOfTwo(pixmap.getHeight());
					final Pixmap potPixmap = new Pixmap(width, height, pixmap.getFormat());
					potPixmap.setBlending(Blending.None);
					potPixmap.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
					pixmap.dispose();
					Gdx.app.postRunnable(new Runnable() {
						@Override
						public void run () {
							image = new TextureRegion(new Texture(potPixmap), 0, 0, originalWidth, originalHeight);
						}
					});
				}
			}
		}).start();

		font = new BitmapFont();
		batch = new SpriteBatch();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (image != null) {
			batch.begin();
			batch.draw(image, 100, 100);
			batch.end();
		} else {
			batch.begin();
			font.draw(batch, "Downloading...", 100, 100);
			batch.end();
		}
	}
}
