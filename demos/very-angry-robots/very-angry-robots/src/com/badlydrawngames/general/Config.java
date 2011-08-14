/*
 * Copyright 2011 Rod Hyde (rod@badlydrawngames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlydrawngames.general;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/** Provides a simple way to tweak the game configuration via attributes. Ideally this would be backed with a
 * <code>ConfigProvider</code> or similar.
 * @author Rod */
public class Config {

	private static final String PROPERTIES_FILE = "data/veryangryrobots.properties";
	private static Properties properties;

	private Config () {
	}

	private static Properties instance () {
		if (null == properties) {
			properties = new Properties();
			FileHandle fh = Gdx.files.internal(PROPERTIES_FILE);
			InputStream inStream = fh.read();
			try {
				properties.load(inStream);
				inStream.close();
			} catch (IOException e) {
				if (inStream != null) {
					try {
						inStream.close();
					} catch (IOException ex) {
					}
				}
			}
		}
		return properties;
	}

	public static int asInt (String name, int fallback) {
		String v = instance().getProperty(name);
		if (v == null) return fallback;
		return Integer.parseInt(v);
	}

	public static float asFloat (String name, float fallback) {
		String v = instance().getProperty(name);
		if (v == null) return fallback;
		return Float.parseFloat(v);
	}

	public static String asString (String name, String fallback) {
		String v = instance().getProperty(name);
		if (v == null) return fallback;
		return v;
	}
}
