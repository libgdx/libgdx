package com.badlogic.gdx.backends.jogl;

import java.util.Map;

import com.badlogic.gdx.Preferences;

public class JoglPreferences implements Preferences {
	String name;

	JoglPreferences(String name) {
		this.name = name;
	}
	
	@Override public void putBoolean (String key, boolean val) {
	}

	@Override public void putInteger (String key, int val) {
	}

	@Override public void putLong (String key, long val) {
	}

	@Override public void putFloat (String key, float val) {
	}

	@Override public void putString (String key, String val) {
	}

	@Override public void put (Map<String, ?> vals) {
	}

	@Override public boolean getBoolean (String key) {
		return false;
	}

	@Override public int getInteger (String key) {
		return 0;
	}

	@Override public long getLong (String key) {
		return 0;
	}

	@Override public float getFloat (String key) {
		return 0;
	}

	@Override public String getString (String key) {
		return null;
	}

	@Override public boolean getBoolean (String key, boolean defValue) {
		return false;
	}

	@Override public int getInteger (String key, int defValue) {
		return 0;
	}

	@Override public long getLong (String key, long defValue) {
		return 0;
	}

	@Override public float getFloat (String key, float defValue) {
		return 0;
	}

	@Override public String getString (String key, String defValue) {
		return null;
	}

	@Override public Map<String, ?> get () {
		return null;
	}

	@Override public boolean contains (String key) {
		return false;
	}

	@Override public void clear () {
	}
}
