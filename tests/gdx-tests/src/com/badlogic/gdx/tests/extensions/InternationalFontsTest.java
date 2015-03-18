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

package com.badlogic.gdx.tests.extensions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.tests.utils.GdxTest;

/** Shows how to use fonts for languages other than english. Note that only alphabets with a humble amount of glyphs can be used
 * (100 is about the maximum we'd recommend). All western languages fit into that category, some asian languages might work as
 * well, e.g. Korean. Any right-to-left languages like arabic won't work, neither do languages like Japanese or Chinese due to
 * their huge amount of glyphs which don't fit into a single texture.</p>
 * 
 * Note that you don't have to use the FreeType extension for this, you can generate fonts with Hiero as well. The FreeType
 * extension allows you to generate fonts at runtime for different screen densities. It is not portable to GWT!</p>
 * 
 * For each script examplified below we only use a few characters from the respective alphabet. You'll have to pass in a string
 * containing all the printable characters of that language that you want to use!
 * 
 * @author mzechner */
public class InternationalFontsTest extends GdxTest {
	OrthographicCamera cam;
	SpriteBatch batch;
	BitmapFont koreanFont;
	BitmapFont cyrillicFont;
	BitmapFont thaiFont;

	@Override
	public void create () {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/unbom.ttf"));

		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 18;
		parameter.characters = "한국어/조선�?";

		koreanFont = generator.generateFont(parameter);
		generator.dispose();

		parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS;

		generator = new FreeTypeFontGenerator(Gdx.files.internal("data/russkij.ttf"));
		cyrillicFont = generator.generateFont(parameter);
		generator.dispose();

		parameter.characters = "วรณยุ�?ต์";

		generator = new FreeTypeFontGenerator(Gdx.files.internal("data/garuda.ttf"));
		thaiFont = generator.generateFont(parameter);
		generator.dispose();

		batch = new SpriteBatch();

		cam = new OrthographicCamera();
		cam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.update();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(cam.combined);
		batch.begin();
		koreanFont.draw(batch, "한국어/조선�?", 0, 22);
		cyrillicFont.draw(batch, "cyrillic text", 0, 44);
		thaiFont.draw(batch, "วรรณยุ�?ต์", 0, 66);
		batch.end();
	}

	@Override
	public void resize (int width, int height) {
		cam.setToOrtho(false, width, height);
	}

}
