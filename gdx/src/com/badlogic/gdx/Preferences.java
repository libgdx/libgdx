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
 * <p>
 * A Preference instance is a hash map holding different values. It is stored alongside your application (SharedPreferences on
 * Android, LocalStorage on GWT, on the desktop a Java Preferences file in a ".prefs" directory will be created, and on iOS an
 * NSMutableDictionary will be written to the given file). CAUTION: On the desktop platform, all libGDX applications share the
 * same ".prefs" directory. To avoid collisions use specific names like "com.myname.game1.settings" instead of "settings".
 * </p>
 * 
 * <p>
 * To persist changes made to a preferences instance {@link #flush()} has to be invoked. With the exception of Android, changes
 * are cached in memory prior to flushing. On iOS changes are not synchronized between different preferences instances.
 * </p>
 * 
 * <p>
 * Use {@link Application#getPreferences(String)} to look up a specific preferences instance. Note that on several backends the
 * preferences name will be used as the filename, so make sure the name is valid for a filename.
 * </p>
 * 
 * @author mzechner */
public interface Preferences {
	public Preferences putBoolean (String key, boolean val);

	public Preferences putInteger (String key, int val);

	public Preferences putLong (String key, long val);

	public Preferences putFloat (String key, float val);

	public Preferences putString (String key, String val);

	public Preferences put (Map<String, ?> vals);

	public boolean getBoolean (String key);

	public int getInteger (String key);

	public long getLong (String key);

	public float getFloat (String key);

	public String getString (String key);

	public boolean getBoolean (String key, boolean defValue);

	public int getInteger (String key, int defValue);

	public long getLong (String key, long defValue);

	public float getFloat (String key, float defValue);

	public String getString (String key, String defValue);

	/** Returns a read only Map<String, Object> with all the key, objects of the preferences. */
	public Map<String, ?> get ();

	public boolean contains (String key);

	public void clear ();

	public void remove (String key);

	/** Makes sure the preferences are persisted. */
	public void flush ();

	/** Asynchronously persists the preferences and reports the outcome through the callback.
	 * <p>
	 * Backends dispatch the write to a background thread and invoke the callback after the write completes. This method does not
	 * add any thread safety; callers must finish preference mutations before invoking it.
	 * </p>
	 * <p>
	 * The default implementation performs the write synchronously. This fallback keeps custom {@code Preferences} implementations
	 * source compatible; platform backends should override it with asynchronous execution. Callback exceptions propagate to the
	 * caller for the default implementation and on the callback thread for asynchronous implementations.
	 * </p>
	 * @param saveCallback invoked once with the outcome of the persist operation; must not be null */
	default public void flush (final PreferencesSaveCallback saveCallback) {
		if (saveCallback == null) throw new IllegalArgumentException("saveCallback must not be null");
		try {
			flush();
			saveCallback.onSuccess();
		} catch (Throwable t) {
			saveCallback.onFailure(PreferencesSaveResult.from(t), t);
		}
	}
}
