package com.badlogic.gdx.backends.android;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.badlogic.gdx.Preferences;

public class AndroidPreferences implements Preferences {
	SharedPreferences sharedPrefs;
	
	AndroidPreferences(SharedPreferences preferences) {
		this.sharedPrefs = preferences;
	}

	@Override public void putBoolean (String key, boolean val) {
		Editor edit = this.sharedPrefs.edit();
		edit.putBoolean(key, val);
		edit.commit();
	}

	@Override public void putInteger (String key, int val) {
		Editor edit = this.sharedPrefs.edit();
		edit.putInt(key, val);
		edit.commit();
	}

	@Override public void putLong (String key, long val) {
		Editor edit = this.sharedPrefs.edit();
		edit.putLong(key, val);
		edit.commit();
	}

	@Override public void putFloat (String key, float val) {
		Editor edit = this.sharedPrefs.edit();
		edit.putFloat(key, val);
		edit.commit();
	}
	
	@Override public void putString(String key, String val) {
		Editor edit = this.sharedPrefs.edit();
		edit.putString(key, val);
		edit.commit();
	}

	@Override public void put (Map<String, ?> vals) {
		Editor edit = this.sharedPrefs.edit();
		for(Entry<String, ?> val: vals.entrySet()) {
			if(val.getValue() instanceof Boolean)
				putBoolean(val.getKey(), (Boolean)val.getValue());
			if(val.getValue() instanceof Integer)
				putInteger(val.getKey(), (Integer)val.getValue());
			if(val.getValue() instanceof Long)
				putLong(val.getKey(), (Long)val.getValue());
			if(val.getValue() instanceof String)
				putString(val.getKey(), (String)val.getValue());
			if(val.getValue() instanceof Float) 
				putFloat(val.getKey(), (Float)val.getValue());
		}
		edit.commit();
	}

	@Override public boolean getBoolean (String key) {		
		return sharedPrefs.getBoolean(key, false);
	}

	@Override public int getInteger (String key) {
		return sharedPrefs.getInt(key, 0);
	}

	@Override public long getLong (String key) {
		return sharedPrefs.getLong(key, 0);
	}

	@Override public float getFloat (String key) {
		return sharedPrefs.getFloat(key, 0);
	}

	@Override public String getString (String key) {
		return sharedPrefs.getString(key, "");
	}

	@Override public boolean getBoolean (String key, boolean defValue) {
		return sharedPrefs.getBoolean(key, defValue);
	}

	@Override public int getInteger (String key, int defValue) {
		return sharedPrefs.getInt(key, defValue);
	}

	@Override public long getLong (String key, long defValue) {
		return sharedPrefs.getLong(key, defValue);
	}

	@Override public float getFloat (String key, float defValue) {
		return sharedPrefs.getFloat(key, defValue);
	}

	@Override public String getString (String key, String defValue) {
		return sharedPrefs.getString(key, defValue);
	}

	@Override public Map<String, ?> get () {		
		return sharedPrefs.getAll();		
	}

	@Override public boolean contains (String key) {
		return sharedPrefs.contains(key);
	}

	@Override public void clear () {
		Editor edit = sharedPrefs.edit();
		edit.clear();
		edit.commit();
	}
	
	@Override public void flush () {		
	}
}
