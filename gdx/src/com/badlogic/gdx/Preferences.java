package com.badlogic.gdx;

import java.util.Map;

/**
 * A Preference instance is a hash map holding different
 * values. It is stored alongside your application (SharedPreferences
 * on Android, flat file in apps root directory on desktop). 
 * 
 * @author mzechner
 *
 */
public interface Preferences {
	public void putBoolean(String key, boolean val);	
	public void putInteger(String key, int val);
	public void putLong(String key, long val);
	public void putFloat(String key, float val);	
	public void putString(String key, String val);
	public void put(Map<String, ?> vals);
	
	public boolean getBoolean(String key);		
	public int getInteger(String key);
	public long getLong(String key);
	public float getFloat(String key);	
	public String getString(String key);
	public boolean getBoolean(String key, boolean defValue);		
	public int getInteger(String key, int defValue);
	public long getLong(String key, long defValue);
	public float getFloat(String key, float defValue);	
	public String getString(String key, String defValue);
	public Map<String, ?> get();
	
	public boolean contains(String key);
	public void clear();
}
