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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class TextureDataTest extends GdxTest {
	private SpriteBatch spriteBatch;
	private Sprite sprite;

	public void create () {
		spriteBatch = new SpriteBatch();

		sprite = new Sprite(new Texture(new TextureData() {
			public void load () {
				FileHandle file = Gdx.files.internal("data/raw.bin");
				InputStream input = file.read();
				ByteBuffer buffer = ByteBuffer.allocateDirect((int)file.length());
				buffer.order(ByteOrder.nativeOrder());
				byte[] bytes = new byte[1024];
				try {
					while (true) {
						int length = input.read(bytes);
						if (length == -1) break;
						buffer.put(bytes, 0, length);
					}
				} catch (IOException ex) {
					throw new GdxRuntimeException(ex);
				}
				buffer.flip();
				Gdx.gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, getWidth(), getHeight(), 0, GL10.GL_RGBA,
					GL10.GL_UNSIGNED_SHORT_4_4_4_4, buffer);
			}

			public int getWidth () {
				return 512;
			}

			public int getHeight () {
				return 512;
			}
		}));
	}

	public void render () {
		spriteBatch.begin();
		sprite.draw(spriteBatch);
		spriteBatch.end();
	}

	public boolean needsGL20 () {
		return false;
	}
}
