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

import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.BufferUtils;

public class PixmapTest extends GdxTest {
	Pixmap pixmap;
	Texture texture;
	SpriteBatch batch;
	TextureRegion region;
	Pixmap pixmapCustom;
	Texture textureCustom;
	TextureRegion regionCustom;

	public void create () {
		// Create an empty dynamic pixmap
		pixmap = new Pixmap(800, 480, Pixmap.Format.RGBA8888);
		pixmapCustom = new Pixmap(256, 256, Pixmap.Format.RGBA8888);

		ByteBuffer buffer = BufferUtils.newByteBuffer(pixmapCustom.getWidth() * pixmapCustom.getHeight() * 4);
		for (int y = 0; y < pixmapCustom.getHeight(); y++) {
			for (int x = 0; x < pixmapCustom.getWidth(); x++) {
				buffer.put((byte)x);
				buffer.put((byte)y);
				buffer.put((byte)0);
				buffer.put((byte)255);
			}
		}
		buffer.flip();
		pixmapCustom.setPixels(buffer);
		textureCustom = new Texture(pixmapCustom);
		regionCustom = new TextureRegion(textureCustom);

		// Create a texture to contain the pixmap
		texture = new Texture(1024, 1024, Pixmap.Format.RGBA8888); // Pixmap.Format.RGBA8888);
		texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Linear);
		texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

		pixmap.setColor(1.0f, 0.0f, 0.0f, 1.0f); // Red
		pixmap.drawLine(0, 0, 100, 100);

		pixmap.setColor(0.0f, 0.0f, 1.0f, 1.0f); // Blue
		pixmap.drawLine(100, 100, 200, 0);

		pixmap.setColor(0.0f, 1.0f, 0.0f, 1.0f); // Green
		pixmap.drawLine(100, 0, 100, 100);

		pixmap.setColor(1.0f, 1.0f, 1.0f, 1.0f); // White
		pixmap.drawCircle(400, 300, 100);

		// Blit the composited overlay to a texture
		texture.draw(pixmap, 0, 0);
		region = new TextureRegion(texture, 0, 0, 800, 480);
		batch = new SpriteBatch();

		Pixmap pixmap = new Pixmap(512, 1024, Pixmap.Format.RGBA8888);
		for (int y = 0; y < pixmap.getHeight(); y++) { // 1024
			for (int x = 0; x < pixmap.getWidth(); x++) { // 512
				pixmap.getPixel(x, y);
			}
		}
		pixmap.dispose();
	}

	public void render () {
		ScreenUtils.clear(0.6f, 0.6f, 0.6f, 1);
		batch.begin();
		batch.draw(region, 0, 0);
		batch.draw(regionCustom, 0, 0);
		batch.end();
	}
}
