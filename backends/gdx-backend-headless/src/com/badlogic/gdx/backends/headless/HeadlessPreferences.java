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

package com.badlogic.gdx.backends.headless;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class HeadlessPreferences implements Preferences {
	private final String name;
	private final Properties properties = new Properties();
	private final FileHandle file;

	public HeadlessPreferences(String name, String directory) {
		this(new HeadlessFileHandle(new File(directory, name), FileType.External));
	}

	public HeadlessPreferences(FileHandle file) {
		this.name = file.name();
		this.file = file;
		if (!file.exists()) return;
		InputStream in = null;
		try {
			in = new BufferedInputStream(file.read());
			properties.loadFromXML(in);
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			StreamUtils.closeQuietly(in);
		}
	}

	@Override
	public Preferences putBoolean (String key, boolean val) {
		properties.put(key, Boolean.toString(val));
		return this;
	}

	@Override
	public Preferences putInteger (String key, int val) {
		properties.put(key, Integer.toString(val));
		return this;
	}

	@Override
	public Preferences putLong (String key, long val) {
		properties.put(key, Long.toString(val));
		return this;
	}

	@Override
	public Preferences putFloat (String key, float val) {
		properties.put(key, Float.toString(val));
		return this;
	}

	@Override
	public Preferences putString (String key, String val) {
		properties.put(key, val);
		return this;
	}

	@Override
	public Preferences put (Map<String, ?> vals) {
		for (Entry<String, ?> val : vals.entrySet()) {
			if (val.getValue() instanceof Boolean) putBoolean(val.getKey(), (Boolean)val.getValue());
			if (val.getValue() instanceof Integer) putInteger(val.getKey(), (Integer)val.getValue());
			if (val.getValue() instanceof Long) putLong(val.getKey(), (Long)val.getValue());
			if (val.getValue() instanceof String) putString(val.getKey(), (String)val.getValue());
			if (val.getValue() instanceof Float) putFloat(val.getKey(), (Float)val.getValue());
		}
		return this;
	}

	@Override
	public boolean getBoolean (String key) {
		return getBoolean(key, false);
	}

	@Override
	public int getInteger (String key) {
		return getInteger(key, 0);
	}

	@Override
	public long getLong (String key) {
		return getLong(key, 0);
	}

	@Override
	public float getFloat (String key) {
		return getFloat(key, 0);
	}

	@Override
	public String getString (String key) {
		return getString(key, "");
	}

	@Override
	public boolean getBoolean (String key, boolean defValue) {
		return Boolean.parseBoolean(properties.getProperty(key, Boolean.toString(defValue)));
	}

	@Override
	public int getInteger (String key, int defValue) {
		return Integer.parseInt(properties.getProperty(key, Integer.toString(defValue)));
	}

	@Override
	public long getLong (String key, long defValue) {
		return Long.parseLong(properties.getProperty(key, Long.toString(defValue)));
	}

	@Override
	public float getFloat (String key, float defValue) {
		return Float.parseFloat(properties.getProperty(key, Float.toString(defValue)));
	}

	@Override
	public String getString (String key, String defValue) {
		return properties.getProperty(key, defValue);
	}

	@Override
	public Map<String, ?> get () {
		Map<String, Object> map = new HashMap<String, Object>();
		for (Entry<Object, Object> val : properties.entrySet()) {
			if (val.getValue() instanceof Boolean)
				map.put((String)val.getKey(), (Boolean)Boolean.parseBoolean((String)val.getValue()));
			if (val.getValue() instanceof Integer) map.put((String)val.getKey(), (Integer)Integer.parseInt((String)val.getValue()));
			if (val.getValue() instanceof Long) map.put((String)val.getKey(), (Long)Long.parseLong((String)val.getValue()));
			if (val.getValue() instanceof String) map.put((String)val.getKey(), (String)val.getValue());
			if (val.getValue() instanceof Float) map.put((String)val.getKey(), (Float)Float.parseFloat((String)val.getValue()));
		}

		return map;
	}

	@Override
	public boolean contains (String key) {
		return properties.containsKey(key);
	}

	@Override
	public void clear () {
		properties.clear();
	}

	@Override
	public void flush () {
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(file.write(false));
			properties.storeToXML(out, null);
		} catch (Exception ex) {
			throw new GdxRuntimeException("Error writing preferences: " + file, ex);
		} finally {
			StreamUtils.closeQuietly(out);
		}
	}

	@Override
	public void remove (String key) {
		properties.remove(key);
	}
}