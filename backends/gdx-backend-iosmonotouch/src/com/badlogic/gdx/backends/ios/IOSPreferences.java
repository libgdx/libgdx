package com.badlogic.gdx.backends.ios;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Preferences;

// FIXME implement this
public class IOSPreferences implements Preferences {

	@Override
	public void putBoolean (String key, boolean val) {
		// TODO Auto-generated method stub
	}

	@Override
	public void putInteger (String key, int val) {
		// TODO Auto-generated method stub
	}

	@Override
	public void putLong (String key, long val) {
		// TODO Auto-generated method stub
	}

	@Override
	public void putFloat (String key, float val) {
		// TODO Auto-generated method stub
	}

	@Override
	public void putString (String key, String val) {
		// TODO Auto-generated method stub
	}

	@Override
	public void put (Map<String, ?> vals) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean getBoolean (String key) {
		return false;
	}

	@Override
	public int getInteger (String key) {
		return 0;
	}

	@Override
	public long getLong (String key) {
		return 0;
	}

	@Override
	public float getFloat (String key) {
		return 0;
	}

	@Override
	public String getString (String key) {
		return null;
	}

	@Override
	public boolean getBoolean (String key, boolean defValue) {
		return defValue;
	}

	@Override
	public int getInteger (String key, int defValue) {
		return defValue;
	}

	@Override
	public long getLong (String key, long defValue) {
		return defValue;
	}

	@Override
	public float getFloat (String key, float defValue) {
		return defValue;
	}

	@Override
	public String getString (String key, String defValue) {
		return defValue;
	}

	@Override
	public Map<String, ?> get () {
		return new HashMap();
	}

	@Override
	public boolean contains (String key) {
		return false;
	}

	@Override
	public void clear () {
		
	}

	@Override
	public void remove (String key) {
	}

	@Override
	public void flush () {
	}
}
