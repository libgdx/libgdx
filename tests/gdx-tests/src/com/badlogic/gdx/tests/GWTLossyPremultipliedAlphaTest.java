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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.tests.utils.GdxTest;

/**
 * This tests is used to illustrate how backing the Pixmap in GWT with a Canvas can cause problems.
 * @author barkholt
 *
 */
public class GWTLossyPremultipliedAlphaTest extends GdxTest {
	private SpriteBatch batch;
	private Texture goodTexture;
	private Texture badTexture;

	public void create () {
		batch = new SpriteBatch();
		// Load a texture with premultiplied alpha
		FileTextureData data = new FileTextureData(Gdx.files.internal("data/premultiplied_alpha_test.png"), null, null, false);
		goodTexture = new Texture(data);
		
		// Load the texture again. But this time, force the GWT implementation of Pixmap to move to a Canvas representation of the image
		Pixmap pixmap = new Pixmap(Gdx.files.internal("data/premultiplied_alpha_test.png"));
		pixmap.getPixel(0, 0);
		FileTextureData data1 = new FileTextureData(null, pixmap, null, false);
		badTexture = new Texture(data1);
	}

	public void render () {
		 Gdx.gl.glClearColor(0, 0, 0, 1);
       Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

       batch.begin();
       batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
       batch.draw(badTexture,
               0,
               Gdx.graphics.getHeight(),
               Gdx.graphics.getWidth() * 0.5f,
               -Gdx.graphics.getHeight());

       batch.draw(goodTexture,
               Gdx.graphics.getWidth() * 0.5f,
               Gdx.graphics.getHeight(),
               Gdx.graphics.getWidth() * 0.5f,
               -Gdx.graphics.getHeight());

       batch.end();
	}

	public boolean needsGL20 () {
		return false;
	}
}
