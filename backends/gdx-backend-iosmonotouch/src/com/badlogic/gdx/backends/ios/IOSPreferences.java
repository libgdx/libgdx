
package com.badlogic.gdx.backends.ios;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import cli.MonoTouch.Foundation.NSBundle;
import cli.MonoTouch.Foundation.NSDictionary;
import cli.MonoTouch.Foundation.NSNumber;
import cli.MonoTouch.Foundation.NSObject;
import cli.MonoTouch.Foundation.NSString;
import cli.MonoTouch.Foundation.NSUserDefaults;

import com.badlogic.gdx.Preferences;

public class IOSPreferences implements Preferences {

	NSDictionary nsDictionary;
	String filePath;

	public IOSPreferences (NSDictionary nsDictionary, String filePath) {
		this.nsDictionary = nsDictionary;
		this.filePath = filePath;
	}

	@Override
	public void putBoolean (String key, boolean val) {
		nsDictionary.Add(key, NSNumber.FromBoolean(val));
	}

	@Override
	public void putInteger (String key, int val) {
		nsDictionary.Add(key, NSNumber.FromInt32(val));
	}

	@Override
	public void putLong (String key, long val) {
		nsDictionary.Add(key, NSNumber.FromInt64(val));
	}

	@Override
	public void putFloat (String key, float val) {
		nsDictionary.Add(key, NSNumber.FromFloat(val));
	}

	@Override
	public void putString (String key, String val) {
		nsDictionary.Add(key, new NSString(val));
	}

	@Override
	public void put (Map<String, ?> vals) {
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
	}

	@Override
	public boolean getBoolean (String key) {
		NSNumber value = (NSNumber)nsDictionary.get_Item(key);
		return value.get_BoolValue();
	}

	@Override
	public int getInteger (String key) {
		NSNumber value = (NSNumber)nsDictionary.get_Item(key);
		return value.get_Int32Value();
	}

	@Override
	public long getLong (String key) {
		NSNumber value = (NSNumber)nsDictionary.get_Item(key);
		return value.get_Int64Value();
	}

	@Override
	public float getFloat (String key) {
		NSNumber value = (NSNumber)nsDictionary.get_Item(key);
		return value.get_FloatValue();
	}

	@Override
	public String getString (String key) {
		NSString value = (NSString)nsDictionary.get_Item(key);
		return value.ToString();
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
		// Map<String, Object> map = new HashMap<String, Object>();
		//
		// NSObject[] keys = nsDictionary.get_Keys();
		// for (NSObject nsObject : keys) {
		// map.put(((NSString)key).ToString(), get)
		// }
		//
		// for (Entry<Object, Object> val : properties.entrySet()) {
		// if (val.getValue() instanceof Boolean)
		// map.put((String)val.getKey(), (Boolean)Boolean.parseBoolean((String)val.getValue()));
		// if (val.getValue() instanceof Integer) map.put((String)val.getKey(), (Integer)Integer.parseInt((String)val.getValue()));
		// if (val.getValue() instanceof Long) map.put((String)val.getKey(), (Long)Long.parseLong((String)val.getValue()));
		// if (val.getValue() instanceof String) map.put((String)val.getKey(), (String)val.getValue());
		// if (val.getValue() instanceof Float) map.put((String)val.getKey(), (Float)Float.parseFloat((String)val.getValue()));
		// }
		// return map;
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public boolean contains (String key) {
		return nsDictionary.Contains(key);
	}

	@Override
	public void clear () {
		nsDictionary.Clear();
	}

	@Override
	public void remove (String key) {
		nsDictionary.Remove(key);
	}

	@Override
	public void flush () {
		nsDictionary.WriteToFile(filePath, true);
	}

}
