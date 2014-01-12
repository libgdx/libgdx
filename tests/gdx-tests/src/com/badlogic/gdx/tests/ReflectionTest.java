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
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Constructor;
import com.badlogic.gdx.utils.reflect.Method;

/** Performs some tests with {@link ClassReflection} and prints the results on the screen.
 * @author hneuer */
public class ReflectionTest extends GdxTest {
	String message = "";
	BitmapFont font;
	SpriteBatch batch;

	@Override
	public void create () {
		font = new BitmapFont();
		batch = new SpriteBatch();

		try {
			Vector2 fromDefaultConstructor = ClassReflection.newInstance(Vector2.class);
			println("From default constructor: " + fromDefaultConstructor);

			Method mSet = ClassReflection.getMethod(Vector2.class, "set", float.class, float.class);
			mSet.invoke(fromDefaultConstructor, 10, 11);
			println("Set to 10/11: " + fromDefaultConstructor);

			Constructor copyConstroctor = ClassReflection.getConstructor(Vector2.class, Vector2.class);
			Vector2 fromCopyConstructor = (Vector2)copyConstroctor.newInstance(fromDefaultConstructor);
			println("From copy constructor: " + fromCopyConstructor);

			Method mMul = ClassReflection.getMethod(Vector2.class, "mul", float.class);
			println("Multiplied by 2; " + mMul.invoke(fromCopyConstructor, 2));

			Method mNor = ClassReflection.getMethod(Vector2.class, "nor");
			println("Normalized: " + mNor.invoke(fromCopyConstructor));

			println("JSON serialized: " + new Json().toJson(fromCopyConstructor));
		} catch (Exception e) {
			message = "FAILED: " + e.getMessage();
		}

	}

	private void println (String line) {
		message += line + "\n";
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.drawMultiLine(batch, message, 20, Gdx.graphics.getHeight() - 20);
		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
	}

	@Override
	public boolean needsGL20 () {
		return true;
	}

}
