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

import java.util.Date;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.I18NBundle;

/** Performs some tests with {@link I18NBundle} and prints the results on the screen.
 * 
 * @author davebaol */
public class I18NMessageTest extends GdxTest {

	String message = "";
	BitmapFont font;
	SpriteBatch batch;
	I18NBundle rb_root;
	I18NBundle rb_default;
	I18NBundle rb_en;
	I18NBundle rb_it;
	I18NBundle rb_unsupported;
	Date now = new Date();

	@Override
	public void create () {
		font = new BitmapFont();
		batch = new SpriteBatch();

		try {
			FileHandle bfh = Gdx.files.internal("data/i18n/message1");
			rb_root = I18NBundle.createBundle(bfh, new Locale("", "", "")); // Locale.ROOT doesn't exist in Android API level 8
			rb_default = I18NBundle.createBundle(bfh);
			rb_en = I18NBundle.createBundle(bfh, new Locale("en", "US"));
			rb_it = I18NBundle.createBundle(bfh, new Locale("it", "IT"));
			rb_unsupported = I18NBundle.createBundle(bfh, new Locale("unsupported"));

			println("Default locale: " + Locale.getDefault());

			println("\n\n---- Parent chain test ----");
			println(getMessage("root", rb_root));
			println(getMessage("default", rb_default));
			println(getMessage("en", rb_en));
			println(getMessage("it", rb_it));
			println(getMessage("unsupported", rb_unsupported));

			println("\n\n---- Parametric message test ----");
			println(getParametricMessage("root", rb_root));
			println(getParametricMessage("default", rb_default));
			println(getParametricMessage("en", rb_en));
			println(getParametricMessage("it", rb_it));
			println(getParametricMessage("unsupported", rb_unsupported));

			Gdx.app.log("", message);

		} catch (Throwable t) {
			message = "FAILED: " + t.getMessage() + "\n";
			message += t.getClass();
			Gdx.app.error(I18NMessageTest.class.getSimpleName(), "Error", t);
		}
	}

	private String getMessage (String header, I18NBundle rb) {
		return header + " -> locale: " + rb.getLocale() + ", msg: \"" + rb.format("msg") + "\", rootMsg: \"" + rb.format("rootMsg")
			+ "\"";
	}

	private String getParametricMessage (String header, I18NBundle rb) {
		return header + " -> " + rb.format("msgWithArgs", "LibGDX", MathUtils.PI, now);
	}

	private void println (String line) {
		message += line + "\n";
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.draw(batch, message, 20, Gdx.graphics.getHeight() - 20);
		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
	}
}
