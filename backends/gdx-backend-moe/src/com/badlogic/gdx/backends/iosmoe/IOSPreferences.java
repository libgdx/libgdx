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

package com.badlogic.gdx.backends.iosmoe;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import apple.NSObject;
import apple.foundation.NSMutableDictionary;
import apple.foundation.NSNumber;
import apple.foundation.NSString;
import org.moe.natj.objc.ObjCRuntime;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IOSPreferences implements Preferences {
	NSMutableDictionary<String, Object> nsDictionary;
	File file;

	public IOSPreferences (NSMutableDictionary<String, Object> nsDictionary, String filePath) {
		this.nsDictionary = nsDictionary;
		this.file = new File(filePath);
	}

	@Override
	public Preferences putBoolean (String key, boolean val) {
		nsDictionary.put(key, NSNumber.numberWithBool(val));
		return this;
	}

	@Override
	public Preferences putInteger (String key, int val) {
		nsDictionary.put(key, NSNumber.numberWithInt(val));
		return this;
	}

	@Override
	public Preferences putLong (String key, long val) {
		nsDictionary.put(key, NSNumber.numberWithLongLong(val));
		return this;
	}

	@Override
	public Preferences putFloat (String key, float val) {
		nsDictionary.put(key, NSNumber.numberWithFloat(val));
		return this;
	}

	@Override
	public Preferences putString (String key, String val) {
		nsDictionary.put(key, NSString.stringWithString(val));
		return this;
	}

	@Override
	public Preferences put (Map<String, ?> vals) {
		Set<String> keySet = vals.keySet();
		for (String key : keySet) {
			Object value = vals.get(key);
			if (value instanceof String) {
				putString(key, (String)value);
			} else if (value instanceof Boolean) {
				putBoolean(key, (Boolean)value);
			} else if (value instanceof Integer) {
				putInteger(key, (Integer)value);
			} else if (value instanceof Long) {
				putLong(key, (Long)value);
			} else if (value instanceof Float) {
				putFloat(key, (Float)value);
			}
		}
		return this;
	}

	@Override
	public boolean getBoolean (String key) {
		NSNumber value = (NSNumber)nsDictionary.get(key);
		if (value == null) return false;
		return value.boolValue();
	}

	@Override
	public int getInteger (String key) {
		NSNumber value = (NSNumber)nsDictionary.get(key);
		if (value == null) return 0;
		return value.intValue();
	}

	@Override
	public long getLong (String key) {
		NSNumber value = (NSNumber)nsDictionary.get(key);
		if (value == null) return 0L;
		return value.longLongValue();
	}

	@Override
	public float getFloat (String key) {
		NSNumber value = (NSNumber)nsDictionary.get(key);
		if (value == null) return 0f;
		return value.floatValue();
	}

	@Override
	public String getString (String key) {
		//Implicit mapping from NSString to String apparently?
		Object value = nsDictionary.get(key);
		if (value == null) return "";
		return value.toString();
	}

	@Override
	public boolean getBoolean (String key, boolean defValue) {
		if (!contains(key)) return defValue;
		return getBoolean(key);
	}

	@Override
	public int getInteger (String key, int defValue) {
		if (!contains(key)) return defValue;
		return getInteger(key);
	}

	@Override
	public long getLong (String key, long defValue) {
		if (!contains(key)) return defValue;
		return getLong(key);
	}

	@Override
	public float getFloat (String key, float defValue) {
		if (!contains(key)) return defValue;
		return getFloat(key);
	}

	@Override
	public String getString (String key, String defValue) {
		if (!contains(key)) return defValue;
		return getString(key);
	}

	@Override
	public Map<String, ?> get () {
		Map<String, Object> map = new HashMap<String, Object>();
		for (String key : nsDictionary.keySet()) {
			Object value = nsDictionary.get(key);
			map.put(key, value);
		}
		return map;
	}

	@Override
	public boolean contains (String key) {
		return nsDictionary.containsKey(key);
	}

	@Override
	public void clear () {
		nsDictionary.clear();
	}

	@Override
	public void remove (String key) {
		nsDictionary.remove(key);
	}

	private NSString convertKey (String key) {
		return NSString.alloc().initWithString(key);
	}

	@Override
	public void flush () {
		ObjCRuntime.autoreleasepool(new Runnable() {
			@Override
			public void run() {
				if (!nsDictionary.writeToFileAtomically(file.getAbsolutePath(), false)) {
					Gdx.app.debug("IOSPreferences", "Failed to write NSDictionary to file " + file);
				}
			}
		});
	}
}
