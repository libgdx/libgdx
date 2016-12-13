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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;

/** @author Alex Gladin **/
public class RepeatingTextureTest extends GdxTest {

	private SpriteBatch spriteBatch;
	private Texture texture;
	private float x;
	private float y;
	private float velocity = 10;

	@Override
	public void create () {
		spriteBatch = new SpriteBatch();
		
		Pixmap pixmap = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Format.RGBA8888);
		
		int starsCount = (int) (Math.random() * pixmap.getHeight());
		pixmap.setColor(Color.WHITE);
		for (int i = 0; i < starsCount; i++) {
			pixmap.drawPixel((int) (Math.random() * Gdx.graphics.getWidth()), (int) (Math.random() * Gdx.graphics.getHeight()), Color.WHITE.toIntBits());
		}
		
		texture = new Texture(pixmap);
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		pixmap.dispose();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		x += velocity * Gdx.graphics.getDeltaTime();
		
		spriteBatch.begin();
		spriteBatch.draw(texture, 0, y, (int) x, 0, texture.getWidth(), texture.getHeight());
		spriteBatch.end();
	}

	@Override
	public void dispose () {
		texture.dispose();
		spriteBatch.dispose();
	}

}
