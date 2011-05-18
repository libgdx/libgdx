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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class TextureDownloadTest extends GdxTest {
	@Override
	public boolean needsGL20() {
		return false;
	}

	Texture potTexture;
	TextureRegion nonPotTexture;
	SpriteBatch batch;

	public static class URLHandle extends FileHandle {
		final URL url; 
		
		public URLHandle(String url) {
			try {
				this.url = new URL(url);
			} catch(Exception e) {
				throw new GdxRuntimeException("Couldn't create URLHandle for '" + url + "'", e);
			}
		}
		
		@Override
		public FileHandle child(String name) {
			return null;
		}

		@Override
		public FileHandle parent() {
			return null;
		}		
		
		public InputStream read () {
			try {
				return url.openStream();
			} catch (IOException e) {
				throw new GdxRuntimeException("Couldn't read URL '" + url.toString() + "'");
			}
		}
	}
	
	@Override public void create() {
		// POT image, mananged
		potTexture = new Texture(new URLHandle("http://libgdx.badlogicgames.com/bob.png"));
	
		// non-POT image, not managed!
		Pixmap pixmap = new Pixmap(new URLHandle("http://libgdx.badlogicgames.com/libgdx.png"));
		int width = MathUtils.nextPowerOfTwo(pixmap.getWidth());
		int height = MathUtils.nextPowerOfTwo(pixmap.getHeight());
		Pixmap potPixmap = new Pixmap(width, height, pixmap.getFormat());
		potPixmap.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
		nonPotTexture = new TextureRegion(new Texture(potPixmap), 0, 0, pixmap.getWidth(), pixmap.getHeight());
		pixmap.dispose();
		potPixmap.dispose();
		
		batch = new SpriteBatch();
	}
	
	@Override public void render() {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(potTexture, 100, 100);
		batch.draw(nonPotTexture, 200, 200);
		batch.end();
	}
}
