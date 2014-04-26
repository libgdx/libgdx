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

import java.util.Locale;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.i18n.ResourceBundle;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.reflect.ClassReflection;

/** Performs some tests with {@link ResourceBundle} and prints the results on the screen.
 * @author davebaol */
public class I18NTest extends GdxTest {
	String message = "";
	BitmapFont font;
	SpriteBatch batch;

	@Override
	public void create () {
		font = new BitmapFont();
		batch = new SpriteBatch();

		try {
			println("Default locale: " + Locale.getDefault());
			println("\n");
			println(getMessage("message1", Locale.ROOT));
			println(getMessage("message2", Locale.ROOT));
			println(getMessage("message1"));
			println(getMessage("message2"));
			println(getMessage("message1", new Locale("en", "US")));
			println(getMessage("message2", new Locale("en", "US")));
			println(getMessage("message1", new Locale("it", "IT")));
			println(getMessage("message2", new Locale("it", "IT")));
		} catch (Throwable t) {
			message = "FAILED: " + t.getMessage() + "\n";
			message += t.getClass();
			Gdx.app.error(I18NTest.class.getSimpleName(), "Error", t);
		}
	}

	private String getMessage (String baseName) {
		ResourceBundle rb = ResourceBundle.getBundle("data/i18n/" + baseName);
		return "Bundle: " + baseName + ", locale: default, msg: \"" + rb.getString("msg") + "\", rootMsg: \""
			+ rb.getString("rootMsg") + "\"";
	}

	private String getMessage (String baseName, Locale locale) {
		ResourceBundle rb = ResourceBundle.getBundle("data/i18n/" + baseName, locale);
		String localeStr = Locale.ROOT.equals(locale) ? "root" : locale.toString();
		return "Bundle: " + baseName + ", locale: " + localeStr + ", msg: \"" + rb.getString("msg") + "\", rootMsg: \""
			+ rb.getString("rootMsg") + "\"";
	}

	private void println (String line) {
		message += line + "\n";
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.drawMultiLine(batch, message, 20, Gdx.graphics.getHeight() - 20);
		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();

		// This is not mandatory. It's here for demonstration purposes only.
		ResourceBundle.clearCache();
	}
}
