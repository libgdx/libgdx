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
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class PreferencesTest extends GdxTest {
	public void create () {
		Preferences prefs = Gdx.app.getPreferences(".test");
		if (prefs.contains("bool")) {
			if (prefs.getBoolean("bool") != true) throw new GdxRuntimeException("bool failed");
			if (prefs.getInteger("int") != 1234) throw new GdxRuntimeException("int failed");
			if (prefs.getLong("long") != Long.MAX_VALUE) throw new GdxRuntimeException("long failed");
			if (prefs.getFloat("float") != 1.2345f) throw new GdxRuntimeException("float failed");
			if (!prefs.getString("string").equals("test!")) throw new GdxRuntimeException("string failed");
		}

		prefs.clear();
		prefs.putBoolean("bool", true);
		prefs.putInteger("int", 1234);
		prefs.putLong("long", Long.MAX_VALUE);
		prefs.putFloat("float", 1.2345f);
		prefs.putString("string", "test!");
		prefs.flush();

		if (prefs.getBoolean("bool") != true) throw new GdxRuntimeException("bool failed");
		if (prefs.getInteger("int") != 1234) throw new GdxRuntimeException("int failed");
		if (prefs.getLong("long") != Long.MAX_VALUE) throw new GdxRuntimeException("long failed");
		if (prefs.getFloat("float") != 1.2345f) throw new GdxRuntimeException("float failed");
		if (!prefs.getString("string").equals("test!")) throw new GdxRuntimeException("string failed");
	}
}
