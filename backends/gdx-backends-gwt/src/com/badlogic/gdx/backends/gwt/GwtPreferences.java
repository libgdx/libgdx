package com.badlogic.gdx.backends.gwt;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.ObjectMap;

public class GwtPreferences implements Preferences {
	ObjectMap<String, Object> values = new ObjectMap<String, Object>();
	
	@Override
	public void putBoolean (String key, boolean val) {
		values.put(key, val);
	}

	@Override
	public void putInteger (String key, int val) {
		values.put(key, val);
	}

	@Override
	public void putLong (String key, long val) {
		values.put(key, val);
	}

	@Override
	public void putFloat (String key, float val) {
		values.put(key, val);
	}

	@Override
	public void putString (String key, String val) {
		values.put(key, val);
	}

	@Override
	public void put (Map<String, ?> vals) {
		for(String key: vals.keySet()) {
			values.put(key, vals.get(key));
		}
	}

	@Override
	public boolean getBoolean (String key) {
		Boolean v = (Boolean)values.get(key);
		return v == null? false: v;
	}

	@Override
	public int getInteger (String key) {
		Integer v = (Integer)values.get(key);
		return v == null? 0: v;
	}

	@Override
	public long getLong (String key) {
		Long v = (Long)values.get(key);
		return v == null? 0: v;
	}

	@Override
	public float getFloat (String key) {
		Float v = (Float)values.get(key);
		return v == null? 0: v;
	}

	@Override
	public String getString (String key) {
		return (String)values.get(key);
	}

	@Override
	public boolean getBoolean (String key, boolean defValue) {
		Boolean res = (Boolean)values.get(key);
		return res == null?defValue: res;
	}

	@Override
	public int getInteger (String key, int defValue) {
		Integer res = (Integer)values.get(key);
		return res == null?defValue: res;
	}

	@Override
	public long getLong (String key, long defValue) {
		Long res = (Long)values.get(key);
		return res == null?defValue: res;
	}

	@Override
	public float getFloat (String key, float defValue) {
		Float res = (Float)values.get(key);
		return res == null?defValue: res;
	}

	@Override
	public String getString (String key, String defValue) {
		String res = (String)values.get(key);
		return res == null?defValue: res;
	}

	@Override
	public Map<String, ?> get () {
		HashMap<String, Object> map = new HashMap<String, Object>();
		for(String key: values.keys()) {
			map.put(key, values.get(key));
		}
		return map;
	}

	@Override
	public boolean contains (String key) {
		return values.containsKey(key);
	}

	@Override
	public void clear () {
		values.clear();
	}

	@Override
	public void remove (String key) {
		values.remove(key);
	}

	@Override
	public void flush () {
		// FIXME
	}
}
