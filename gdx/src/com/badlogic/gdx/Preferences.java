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
package com.badlogic.gdx;

import java.util.Map;

/**
 * <p>A Preference instance is a hash map holding different
 * values. It is stored alongside your application (SharedPreferences
 * on Android, flat file in apps root directory on desktop).</p>
 * 
 * <p>
 * On the desktop the file will be located in the user directory. Make
 * sure you give the preferences instance a name that can be used as
 * a filename.
 * </p>
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
	
	/**
	 * Makes sure the preferences are persisted.
	 */
	public void flush();
}
